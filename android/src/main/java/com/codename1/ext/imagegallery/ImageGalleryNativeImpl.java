package com.codename1.ext.imagegallery;

import com.codename1.ui.Display;

public class ImageGalleryNativeImpl {

    private final ImageGalleryPlugin imageGalleryPlugin = new ImageGalleryPlugin();

    public void deregisterPlugin() {
        Display.getInstance().getPluginSupport().deregisterPlugin(imageGalleryPlugin);
    }

    public void registerPlugin() {
        Display.getInstance().getPluginSupport().registerPlugin(imageGalleryPlugin);
    }

    public boolean isSupported() {
        return true;
    }
}
