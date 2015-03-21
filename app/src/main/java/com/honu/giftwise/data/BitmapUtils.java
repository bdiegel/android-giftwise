package com.honu.giftwise.data;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;

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

    public static Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);

        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false);
        return resizedBitmap;
    }

    public static Bitmap createScaledBitmap(Bitmap bm) {
        float aspectRatio = bm.getWidth() / (float) bm.getHeight();
        //int width = 480;
        int width = 480;
        int height = Math.round(width / aspectRatio);

//        // To use height as base intead of width change to:
//        int height = 480;
//        int width = Math.round(height * aspectRatio);

        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bm, width, height, false);
        return resizedBitmap;
    }
}
