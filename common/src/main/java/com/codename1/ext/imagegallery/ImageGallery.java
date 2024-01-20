package com.codename1.ext.imagegallery;


import com.codename1.system.NativeLookup;

public class ImageGallery {

    private static ImageGalleryNative impl;

    public static void register() {
        getImpl().registerPlugin();
    }

    public static void deregister() {
        getImpl().deregisterPlugin();
    }

    public static boolean isSupported() {
        return getImpl() != null && getImpl().isSupported();
    }

    private static ImageGalleryNative getImpl() {
        if (impl == null) {
            synchronized(ImageGallery.class) {
                if (impl == null) {
                    impl = NativeLookup.create(ImageGalleryNative.class);
                }
            }
        }

        return impl;
    }
}
