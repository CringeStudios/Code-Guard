package com.cringe_studios.cringe_authenticator.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class IOUtil {

    public static byte[] readBytes(File file) throws IOException {
        try(FileInputStream fIn = new FileInputStream(file)) {
            ByteBuffer fileBuffer = ByteBuffer.allocate((int) file.length());
            byte[] buffer = new byte[1024];
            int len;
            while ((len = fIn.read(buffer)) > 0) {
                fileBuffer.put(buffer, 0, len);
            }

            return fileBuffer.array();
        }
    }

    public static byte[] readBytes(InputStream in) throws IOException {
        ByteArrayOutputStream bOut = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len;
        while ((len = in.read(buffer)) > 0) {
            bOut.write(buffer, 0, len);
        }

        return bOut.toByteArray();
    }

    public static void writeBytes(File file, byte[] bytes) throws IOException {
        try(FileOutputStream fOut = new FileOutputStream(file)) {
            fOut.write(bytes);
        }
    }

}
