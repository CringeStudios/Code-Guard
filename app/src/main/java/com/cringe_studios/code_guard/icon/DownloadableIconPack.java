package com.cringe_studios.code_guard.icon;

import android.util.Log;

import com.cringe_studios.code_guard.util.DownloadUtil;
import com.cringe_studios.code_guard.util.SettingsUtil;
import com.google.gson.JsonObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public enum DownloadableIconPack {

    AEGIS_ICONS("c1018b93-4e8c-490a-b575-30dde62a833e", "aegis-icons", "https://aegis-icons.github.io/", "https://github.com/aegis-icons/aegis-icons/releases/latest/download/aegis-icons.zip"),
    AEGIS_SIMPLE_ICONS("6a371ea0-1178-4677-ae93-cda7a7a5b378", "aegis-simple-icons", "https://github.com/alexbakker/aegis-simple-icons", () -> {
        String apiURL = "https://api.github.com/repos/alexbakker/aegis-simple-icons/releases/latest";
        JsonObject object = SettingsUtil.GSON.fromJson(new String(DownloadUtil.downloadURL(apiURL), StandardCharsets.UTF_8), JsonObject.class);
        return object.get("assets").getAsJsonArray()
                .get(0).getAsJsonObject()
                .get("browser_download_url").getAsString();
    }),
    ;

    private final String id;
    private final String name;
    private final String credit;
    private final URLLoader url;

    DownloadableIconPack(String id, String name, String credit, URLLoader url) {
        this.id = id;
        this.name = name;
        this.credit = credit;
        this.url = url;
    }

    DownloadableIconPack(String id, String name, String credit, String url) {
        this(id, name, credit, () -> url);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCredit() {
        return credit;
    }

    public void download(File destinationFile) throws IOException {
        String downloadURL = url.load();
        Log.d("Download", "Downloading from " + downloadURL);
        try(InputStream in = DownloadUtil.openURL(downloadURL);
            FileOutputStream fOut = new FileOutputStream(destinationFile)) {
            byte[] buf = new byte[1024];
            int len;
            while((len = in.read(buf)) != -1) {
                fOut.write(buf, 0, len);
            }
        }
    }

    private interface URLLoader {

        String load() throws IOException;

    }

}
