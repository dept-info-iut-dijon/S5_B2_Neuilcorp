package com.example.spotthedifference;

import android.graphics.Bitmap;

public interface IImageConverter {
    Bitmap convertBytesToBitmap(byte[] imageBytes);
}
