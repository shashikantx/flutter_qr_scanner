package com.github.flutter_qr_scannerexample;

import android.os.Bundle;
import io.flutter.app.FlutterActivity;
import com.github.rmtmckenzie.nativedeviceorientation.NativeDeviceOrientationPlugin;
import com.github.contactlutforrahman.flutter_qr_scanner.FlutterQrScannerPlugin;

public class EmbeddingV1Activity extends FlutterActivity {
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    NativeDeviceOrientationPlugin.registerWith(registrarFor("com.github.rmtmckenzie.nativedeviceorientation.NativeDeviceOrientationPlugin"));
    FlutterQrScannerPlugin.registerWith(registrarFor("com.github.contactlutforrahman.flutter_qr_scanner.FlutterQrScannerPlugin"));
  }
}
