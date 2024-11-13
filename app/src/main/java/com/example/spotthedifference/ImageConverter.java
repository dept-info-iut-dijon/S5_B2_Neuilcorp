package com.example.spotthedifference;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class ImageConverter implements IImageConverter {
    public static IImageConverter Companion;

    @Override
    public Bitmap convertBytesToBitmap(byte[] imageBytes) {
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
    }
}
