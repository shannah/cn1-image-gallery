package com.codename1.ext.imagegallery;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.webkit.MimeTypeMap;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class MediaHelper {
    public File copyMediaToTempFile(Context context, Uri imageUri) {
        if (context == null || imageUri == null) {
            // Handle the error - one of the parameters is null.
            return null;
        }

        File tempFile = null;

        try {
            // Create a temporary file in the internal storage
            tempFile = File.createTempFile(
                    "temp_image_",
                    getSuffix(context, imageUri),
                    context.getCacheDir()
            );

            try (InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
                 FileOutputStream fileOutputStream = new FileOutputStream(tempFile);
                 BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream)) {

                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    bufferedOutputStream.write(buffer, 0, bytesRead);
                }
            }
        } catch (IOException e) {
            // Handle the error - something went wrong with file operations
            e.printStackTrace();
            return null;
        }

        return tempFile;
    }

    public String getSuffix(Context context, Uri uri) {
        ContentResolver resolver = context.getContentResolver();
        String mimeType = resolver.getType(uri); // Get the MIME type

        // Use MimeTypeMap to map the MIME type to a file extension
        String extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType);

        // Check if extension is found, otherwise assign a default
        if (extension == null) {
            // Default to .bin for unknown types
            extension = "bin";
        }

        // Add a dot prefix to the extension
        return "." + extension;
    }
}
