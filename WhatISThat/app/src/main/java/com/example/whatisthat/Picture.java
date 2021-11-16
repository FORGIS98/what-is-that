package com.example.whatisthat;

import java.nio.ByteBuffer;

public class Picture {
    private byte[] content;

    public Picture(ByteBuffer buffer) {
        content = new byte[buffer.capacity()];
        buffer.get(content);
    }
}
