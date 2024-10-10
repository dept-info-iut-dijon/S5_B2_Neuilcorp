package com.example.spotthedifference;

import android.graphics.Bitmap;
import android.widget.ImageView;

public class ImageDisplayer
{
    public static void displayImage(ImageView imageView, Bitmap bitmap)
    {
        imageView.setImageBitmap(bitmap);
    }
}
