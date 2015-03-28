package com.honu.giftwise.data;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayOutputStream;

/**
 * Created by bdiegel on 3/21/15.
 */
public class BitmapUtils {

    // convert from bitmap to byte array
    public static byte[] getBytes(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, stream);
        return stream.toByteArray();
    }

    // convert from byte array to bitmap
    public static Bitmap getImage(byte[] image) {
        return BitmapFactory.decodeByteArray(image, 0, image.length);
    }


    public static Bitmap resizeBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();
        float aspectRatio = width / (float) height;

        if (aspectRatio > 1) {
            width = maxSize;
            height = Math.round(width / aspectRatio);
        } else {
            height = maxSize;
            width = Math.round(height * aspectRatio);
        }

        return Bitmap.createScaledBitmap(image, width, height, true);
    }
}
