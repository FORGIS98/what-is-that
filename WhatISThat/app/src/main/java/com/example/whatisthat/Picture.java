package com.example.whatisthat;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.annotation.NonNull;

import java.nio.ByteBuffer;

public class Picture {
    private final Bitmap content;

    public Picture(ByteBuffer buffer) {
        byte[] bytes = new byte[buffer.capacity()];
        buffer.get(bytes);
        content = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    @NonNull
    public String toString() {
        return content.toString();
    }

    public Bitmap getBitmap() {
        return content;
    }
}
