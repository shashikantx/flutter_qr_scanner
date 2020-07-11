package com.github.contactlutforrahman.flutter_qr_scanner;

public interface QrCamera {
    void start() throws QrReader.Exception;
    void stop();
    int getOrientation();
    int getWidth();
    int getHeight();
}
