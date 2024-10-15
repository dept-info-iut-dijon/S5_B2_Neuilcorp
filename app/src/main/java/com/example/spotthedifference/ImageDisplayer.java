package com.example.spotthedifference;

import android.graphics.Bitmap;
import android.util.Log;
import android.widget.ImageView;

public class ImageDisplayer
{
    public static void displayImage(ImageView imageView, Bitmap bitmap)
    {
        if (bitmap != null)
        {
            imageView.setImageBitmap(bitmap);
        } else {
            Log.e("ImageDisplayer", "bitmap is null");
        }
    }
}
