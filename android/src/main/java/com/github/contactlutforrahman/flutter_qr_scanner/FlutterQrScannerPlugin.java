package com.github.contactlutforrahman.flutter_qr_scanner;

import android.app.Activity;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.PluginRegistry.Registrar;
import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.PluginRegistry.Registrar;
import com.github.contactlutforrahman.flutter_qr_scanner.CameraPermissions.PermissionsRegistry;
import com.github.contactlutforrahman.flutter_qr_scanner.MethodCallHandlerImpl;
import io.flutter.view.TextureRegistry;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * FlutterQrScannerPlugin
 */
public final class FlutterQrScannerPlugin implements FlutterPlugin, ActivityAware {

    private static final String TAG = "FlutterQrScannerPlugin";
    private @Nullable FlutterPluginBinding flutterPluginBinding;
    private @Nullable MethodCallHandlerImpl methodCallHandler;

    public FlutterQrScannerPlugin() {}


    /**
     * Plugin registration.
     */
    public static void registerWith(Registrar registrar) {
        FlutterQrScannerPlugin plugin = new FlutterQrScannerPlugin();
        plugin.maybeStartListening(
            registrar.activity(),
            registrar.messenger(),
            registrar::addRequestPermissionsResultListener,
            registrar.view());

    }


    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding binding) {
        this.flutterPluginBinding = binding;
    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
        this.flutterPluginBinding = null;
    }

    @Override
    public void onAttachedToActivity(@NonNull ActivityPluginBinding binding) {
        maybeStartListening(
            binding.getActivity(),
            flutterPluginBinding.getBinaryMessenger(),
            binding::addRequestPermissionsResultListener,
            flutterPluginBinding.getFlutterEngine().getRenderer());
    }

    @Override
    public void onDetachedFromActivity() {
        if (methodCallHandler == null) {
            // Could be on too low of an SDK to have started listening originally.
            return;
        }

        methodCallHandler.stopListening();
        methodCallHandler = null;
    }


    @Override
    public void onReattachedToActivityForConfigChanges(@NonNull ActivityPluginBinding binding) {
        onAttachedToActivity(binding);
    }

    @Override
    public void onDetachedFromActivityForConfigChanges() {
        onDetachedFromActivity();
    }

    private void maybeStartListening(
        Activity activity,
        BinaryMessenger messenger,
        PermissionsRegistry permissionsRegistry,
        TextureRegistry textureRegistry) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            return;
        }

        methodCallHandler =
            new MethodCallHandlerImpl(
                activity, messenger, new CameraPermissions(), permissionsRegistry, textureRegistry);
    }

}