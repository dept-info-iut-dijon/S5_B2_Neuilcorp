package com.example.spotthedifference.ui.util;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.spotthedifference.R;

public class ImagePairSpinnerAdapter extends BaseAdapter {

    private final Context context;
    private final int[] imageIds;
    private int selectedImageId = -1;

    public ImagePairSpinnerAdapter(Context context, int[] imageIds) {
        this.context = context;
        this.imageIds = imageIds;
    }

    @Override
    public int getCount() {
        return (int) Math.ceil(imageIds.length / 2.0);
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        return null;
    }

    /**@Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.spinner_item, parent, false);
        }

        ImageView image1 = convertView.findViewById(R.id.image1);
        ImageView image2 = convertView.findViewById(R.id.image2);

        image1.setImageResource(imageIds[position * 2]);
        if (position * 2 + 1 < imageIds.length) {
            image2.setImageResource(imageIds[position * 2 + 1]);
            image2.setVisibility(View.VISIBLE);
        } else {
            image2.setVisibility(View.GONE);
        }

        image1.setOnClickListener(v -> {
            selectedImageId = imageIds[position * 2];
            notifyDataSetChanged();
            Toast.makeText(context, "Image 1 sélectionnée", Toast.LENGTH_SHORT).show();
        });

        image2.setOnClickListener(v -> {
            selectedImageId = imageIds[position * 2 + 1];
            notifyDataSetChanged();
            Toast.makeText(context, "Image 2 sélectionnée", Toast.LENGTH_SHORT).show();
        });

        image1.setBackgroundColor(selectedImageId == imageIds[position * 2] ? Color.LTGRAY : Color.TRANSPARENT);
        image2.setBackgroundColor(selectedImageId == imageIds[position * 2 + 1] ? Color.LTGRAY : Color.TRANSPARENT);

        return convertView;
    }

    public int getSelectedImageId() {
        return selectedImageId;
    }
    */
}