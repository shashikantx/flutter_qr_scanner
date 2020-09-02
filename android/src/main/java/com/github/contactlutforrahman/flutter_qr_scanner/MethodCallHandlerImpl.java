package com.github.contactlutforrahman.flutter_qr_scanner;

import android.Manifest;
import android.app.Activity;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import android.hardware.camera2.CameraAccessException;

import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.Result;
import com.github.contactlutforrahman.flutter_qr_scanner.CameraPermissions;
import com.github.contactlutforrahman.flutter_qr_scanner.CameraPermissions.PermissionsRegistry;
import com.github.contactlutforrahman.flutter_qr_scanner.ReadingInstance;
import com.github.contactlutforrahman.flutter_qr_scanner.BarcodeFormats;
import com.github.contactlutforrahman.flutter_qr_scanner.CameraLensDirection;
import io.flutter.view.TextureRegistry;

final class MethodCallHandlerImpl implements MethodChannel.MethodCallHandler, QrReader.QRReaderStartedCallback, QrReaderCallbacks {
    private final Activity activity;
    private final BinaryMessenger messenger;
    private final CameraPermissions cameraPermissions;
    private final CameraPermissions.PermissionsRegistry permissionsRegistry;
    private final TextureRegistry textureRegistry;
    private MethodChannel methodChannel;
    private static final String TAG = "FlutterQrScannerPlugin";
    private static final int REQUEST_PERMISSION = 1;
    private Integer lastHeartbeatTimeout;
    private boolean waitingForPermissionResult;
    private boolean permissionDenied;
    private ReadingInstance readingInstance;

    MethodCallHandlerImpl(
        Activity activity,
        BinaryMessenger messenger,
        CameraPermissions cameraPermissions,
        PermissionsRegistry permissionsAdder,
        TextureRegistry textureRegistry) {
        this.activity = activity;
        this.messenger = messenger;
        this.cameraPermissions = cameraPermissions;
        this.permissionsRegistry = permissionsAdder;
        this.textureRegistry = textureRegistry;

        methodChannel = new MethodChannel(messenger, "com.github.contactlutforrahman/flutter_qr_scanner");
        methodChannel.setMethodCallHandler(this);
    }

    @Override
    public void onMethodCall(@NonNull MethodCall methodCall, @NonNull final Result result) {

        switch (methodCall.method) {
            case "start": {
                cameraPermissions.requestPermissions(activity, permissionsRegistry, (String errCode, String errDesc) -> {
                    if (errCode == null) {
                        try {
                            permissionDenied = false;
                        } catch (Exception e) {
                            handleException(e, result);
                        }
                    } else {
                        result.error(errCode, errDesc, null);
                    }
                });
                if (permissionDenied) {
                    permissionDenied = false;
                    result.error("QRREADER_ERROR", "noPermission", null);
                } else if (readingInstance != null) {
                     stopReader();
//                     result.error("ALREADY_RUNNING", "Start cannot be called when already running", "");
                    initiateCamera(methodCall, result);

                } else {
                    initiateCamera(methodCall, result);
                }
                break;
            }
            case "stop": {
                if (readingInstance != null && !waitingForPermissionResult) {
                    stopReader();
                }
                result.success(null);
                break;
            }
            case "heartbeat": {
                if (readingInstance != null) {
                    readingInstance.reader.heartBeat();
                }
                result.success(null);
                break;
            }
            default:
                result.notImplemented();
        }
    }

    void initiateCamera(@NonNull MethodCall methodCall, @NonNull final Result result){

        lastHeartbeatTimeout = methodCall.argument("heartbeatTimeout");
        Integer targetWidth = methodCall.argument("targetWidth");
        Integer targetHeight = methodCall.argument("targetHeight");
        List<String> formatStrings = methodCall.argument("formats");
        String cameraLensDirectionString = methodCall.argument("cameraLensDirection");
        CameraLensDirection cameraLensDirection = CameraLensDirection.get(cameraLensDirectionString);


        if (targetWidth == null || targetHeight == null) {
            result.error("INVALID_ARGUMENT", "Missing a required argument", "Expecting targetWidth, targetHeight, and optionally heartbeatTimeout");
            return;
        }

        int barcodeFormats = BarcodeFormats.intFromStringList(formatStrings);

        TextureRegistry.SurfaceTextureEntry textureEntry = textureRegistry.createSurfaceTexture();
        QrReader reader = new QrReader(targetWidth, targetHeight, activity, barcodeFormats,
            this, this, textureEntry.surfaceTexture(), cameraLensDirection);

        readingInstance = new ReadingInstance(reader, textureEntry, result);
        try {
            reader.start(
                lastHeartbeatTimeout == null ? 0 : lastHeartbeatTimeout
            );
        } catch (IOException e) {
            e.printStackTrace();
            result.error("IOException", "Error starting camera because of IOException: " + e.getLocalizedMessage(), null);
        } catch (QrReader.Exception e) {
            e.printStackTrace();
            result.error(e.reason().name(), "Error starting camera for reason: " + e.reason().name(), null);
        } catch (NoPermissionException e) {
            waitingForPermissionResult = true;
            ActivityCompat.requestPermissions(activity,
                new String[]{Manifest.permission.CAMERA}, REQUEST_PERMISSION);
        }

    }


    void stopListening() {
        methodChannel.setMethodCallHandler(null);
    }

    private void stopReader() {
        readingInstance.reader.stop();
        readingInstance.textureEntry.release();
        readingInstance = null;
        lastHeartbeatTimeout = null;
    }

    @Override
    public void qrRead(String data) {
        methodChannel.invokeMethod("qrRead", data);
    }

    @Override
    public void started() {
        Map<String, Object> response = new HashMap<>();
        response.put("surfaceWidth", readingInstance.reader.qrCamera.getWidth());
        response.put("surfaceHeight", readingInstance.reader.qrCamera.getHeight());
        response.put("surfaceOrientation", readingInstance.reader.qrCamera.getOrientation());
        response.put("textureId", readingInstance.textureEntry.id());
        readingInstance.startResult.success(response);
    }

    private List stackTraceAsString(StackTraceElement[] stackTrace) {
        if (stackTrace == null) {
            return null;
        }

        List<String> stackTraceStrings = new ArrayList<>(stackTrace.length);
        for (StackTraceElement el : stackTrace) {
            stackTraceStrings.add(el.toString());
        }
        return stackTraceStrings;
    }

    @Override
    public void startingFailed(Throwable t) {
        Log.w(TAG, "Starting Flutter Qr Scanner failed", t);
        List<String> stackTraceStrings = stackTraceAsString(t.getStackTrace());

        if (t instanceof QrReader.Exception) {
            QrReader.Exception qrException = (QrReader.Exception) t;
            readingInstance.startResult.error("QR-READER_ERROR", qrException.reason().name(), stackTraceStrings);
        } else {
            readingInstance.startResult.error("UNKNOWN_ERROR", t.getMessage(), stackTraceStrings);
        }
    }


    // We move catching CameraAccessException out of onMethodCall because it causes a crash
    // on plugin registration for sdks incompatible with Camera2 (< 21). We want this plugin to
    // to be able to compile with <21 sdks for apps that want the camera and support earlier version.
    @SuppressWarnings("ConstantConditions")
    private void handleException(Exception exception, Result result) {
        if (exception instanceof CameraAccessException) {
            result.error("CameraAccess", exception.getMessage(), null);
        }

        throw (RuntimeException) exception;
    }
}
