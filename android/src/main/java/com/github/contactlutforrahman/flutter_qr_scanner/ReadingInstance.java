package com.github.contactlutforrahman.flutter_qr_scanner;

import io.flutter.view.TextureRegistry;
import io.flutter.plugin.common.MethodChannel.Result;

public class ReadingInstance {
    final public QrReader reader;
    final public TextureRegistry.SurfaceTextureEntry textureEntry;
    final public Result startResult;

    public ReadingInstance(QrReader reader, TextureRegistry.SurfaceTextureEntry textureEntry, Result startResult) {
        this.reader = reader;
        this.textureEntry = textureEntry;
        this.startResult = startResult;
    }
}
