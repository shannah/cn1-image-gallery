package com.codename1.ext.imagegallery;

import android.net.Uri;
import com.codename1.impl.android.AndroidNativeUtil;
import com.codename1.plugin.Plugin;
import com.codename1.plugin.event.OpenGalleryEvent;
import com.codename1.plugin.event.PluginEvent;
import com.codename1.ui.CN;
import com.codename1.ui.Display;
import com.codename1.ui.events.ActionEvent;
import com.codename1.ui.events.ActionListener;
import android.util.Log;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import java.io.File;
import java.util.ArrayList;


public class ImageGalleryPlugin implements Plugin {

    private static final int MAX_IMAGES_ALLOWED_IN_MULTI_SELECT = 5;
    private static ActivityResultLauncher<PickVisualMediaRequest> pickMedia;
    private static ActivityResultLauncher<PickVisualMediaRequest> pickMultipleMedia;
    private ActionListener resultCallback;

    private final MediaHelper mediaHelper;

    public ImageGalleryPlugin() {
        mediaHelper = new MediaHelper();
    }

    public static void onCreate() {

        if (!isPluginSupported()) {
            return;
        }

        registerPickMediaActivityResultListener();
        registerPickMultipleMediaActivityResultListener();
    }

    @Override
    public void actionPerformed(PluginEvent evt) {
        if (evt instanceof PickVisualMediaEvent) {
            evt.consume();
            handlePickMedia(((PickVisualMediaEvent)evt).uri);
            return;
        }
        if (evt.getEventType() != ActionEvent.Type.OpenGallery) {
            evt.consume();
            handlePickMultiMedia(((PickMultipleVisualMediaEvent)evt).uris);
            return;
        }

        OpenGalleryEvent openGalleryEvent = (OpenGalleryEvent) evt;
        switch (openGalleryEvent.getType()) {
            case Display.GALLERY_IMAGE:
            case Display.GALLERY_VIDEO:
            case Display.GALLERY_ALL:
                evt.consume();
                AndroidNativeUtil.getActivity().runOnUiThread(() -> {
                    openImageGallery(openGalleryEvent.getResponse());
                });
                break;
            case Display.GALLERY_IMAGE_MULTI:
            case Display.GALLERY_VIDEO_MULTI:
                case Display.GALLERY_ALL_MULTI:
                evt.consume();
                AndroidNativeUtil.getActivity().runOnUiThread(() -> {
                    openImageGalleryMulti(
                            openGalleryEvent.getResponse(),
                            openGalleryEvent.getType()
                    );
                });
                break;
        }
    }

    private static void registerPickMediaActivityResultListener() {
        pickMedia = getActivity().registerForActivityResult(
                new ActivityResultContracts.PickVisualMedia(),
                PickVisualMediaEvent::createAndFire
        );
    }

    private static void registerPickMultipleMediaActivityResultListener() {
        pickMultipleMedia =
                getActivity().registerForActivityResult(
                        new ActivityResultContracts.PickMultipleVisualMedia(
                                MAX_IMAGES_ALLOWED_IN_MULTI_SELECT
                        ),
                        PickMultipleVisualMediaEvent::createAndFire
                );
    }

    private void handlePickMedia(Uri uri) {
        // Callback is invoked after the user selects a media item or closes the
        // photo picker.
        Log.d("ImageGallery", "In handlePickMedia " + uri);
        if (uri != null) {
            Log.d("PhotoPicker", "Selected URI: " + uri);
            File tempFile = mediaHelper.copyMediaToTempFile(AndroidNativeUtil.getActivity(), uri);
            CN.callSerially(new Runnable() {
                public void run() {
                    resultCallback.actionPerformed(new ActionEvent(tempFile.getAbsolutePath()));
                }
            });
        } else {
            Log.d("PhotoPicker", "No media selected");
            CN.callSerially(new Runnable() {
                public void run() {
                    resultCallback.actionPerformed(new ActionEvent(null));
                }
            });
        }
    }

    private void handlePickMultiMedia(java.util.List<Uri> uris) {
        // Callback is invoked after the user selects a media item or closes the
        // photo picker.
        Log.d("ImageGallery", "In handlePickMultiMedia " + uris);
        if (uris != null) {
            Log.d("PhotoPicker", "Selected URI: " + uris);
            java.util.List<String> tempFiles = new ArrayList<String>();
            for (Uri uri : uris) {
                tempFiles.add( mediaHelper.copyMediaToTempFile(AndroidNativeUtil.getActivity(), uri).getAbsolutePath());
            }

            CN.callSerially(new Runnable() {
                public void run() {
                    resultCallback.actionPerformed(new ActionEvent(tempFiles.toArray(new String[tempFiles.size()])));
                }
            });
        } else {
            Log.d("PhotoPicker", "No media selected");
            CN.callSerially(new Runnable() {
                public void run() {
                    resultCallback.actionPerformed(new ActionEvent(new String[0]));
                }
            });
        }
    }

    private void openImageGallery(final ActionListener resultCallback) {
        this.resultCallback = resultCallback;
        pickMedia.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build());
    }

    private void openImageGalleryMulti(ActionListener resultCallback, int mediaType) {
        this.resultCallback = resultCallback;
        PickVisualMediaRequest.Builder requestBuilder = new PickVisualMediaRequest.Builder();
        switch (mediaType) {
            case Display.GALLERY_IMAGE:
            case Display.GALLERY_IMAGE_MULTI:
                requestBuilder.setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE);
                break;
            case Display.GALLERY_VIDEO:
            case Display.GALLERY_VIDEO_MULTI:
                requestBuilder.setMediaType(ActivityResultContracts.PickVisualMedia.VideoOnly.INSTANCE);
                break;

        }
        pickMultipleMedia.launch(requestBuilder.build());

    }

    private static boolean isPluginSupported() {
        if (!((AndroidNativeUtil.getActivity()) instanceof AppCompatActivity)) {
            Log.w(
                    "ImageGallery",
                    "ImageGallerPlugin requires the android.extendAppCompatActivity=true build hint"
            );

            return false;
        }

        return true;
    }

    private static AppCompatActivity getActivity() {
        return (AppCompatActivity) AndroidNativeUtil.getActivity();
    }

    private static void firePluginEvent(PluginEvent e) {
        Display.getInstance().getPluginSupport().firePluginEvent(e);
    }

    private static class PickVisualMediaEvent extends PluginEvent<Void> {
        private final Uri uri;

        PickVisualMediaEvent(Uri uri) {
            super(null, Type.Other);
            this.uri = uri;
        }

        static void createAndFire(Uri uri) {
            firePluginEvent(new PickVisualMediaEvent(uri));
        }
    }

    private static class PickMultipleVisualMediaEvent extends PluginEvent<Void> {
        private final java.util.List<Uri> uris;

        PickMultipleVisualMediaEvent(java.util.List<Uri> uris) {
            super(null, Type.Other);
            this.uris = uris;
        }

        static void createAndFire(java.util.List<Uri> uris) {
            firePluginEvent(new PickMultipleVisualMediaEvent(uris));
        }
    }
}
