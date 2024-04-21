package com.cringe_studios.code_guard.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class DownloadUtil {

    public static InputStream openURL(String url) throws IOException {
        return new URL(url).openStream();
    }

    public static byte[] downloadURL(String url) throws IOException {
        try(InputStream in = openURL(url)) {
            ByteArrayOutputStream bOut = new ByteArrayOutputStream();
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) != -1) {
                bOut.write(buf, 0, len);
            }
            return bOut.toByteArray();
        }
    }

}
