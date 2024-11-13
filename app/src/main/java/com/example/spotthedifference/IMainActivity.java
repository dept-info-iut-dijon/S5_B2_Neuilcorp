package com.example.spotthedifference;

import android.graphics.Bitmap;
import android.widget.ImageView;

public interface IMainActivity {

    void loadImage(int imageId);

    void displayImage(ImageView imageView, Bitmap bitmap);

    void sendCoordinatesToServer(Coordonnees coordonnees);

    void showWaitingDialog();

    void hideWaitingDialog();

    void showResultDialog(boolean isSuccess);
}
