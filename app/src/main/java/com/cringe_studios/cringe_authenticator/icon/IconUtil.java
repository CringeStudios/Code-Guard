package com.cringe_studios.cringe_authenticator.icon;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.net.Uri;
import android.util.Base64;

import androidx.core.util.Consumer;

import com.caverock.androidsvg.SVG;
import com.caverock.androidsvg.SVGImageView;
import com.caverock.androidsvg.SVGParseException;
import com.cringe_studios.cringe_authenticator.model.OTPData;
import com.cringe_studios.cringe_authenticator.util.DialogUtil;
import com.cringe_studios.cringe_authenticator.util.IOUtil;
import com.cringe_studios.cringe_authenticator.util.SettingsUtil;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class IconUtil {

    private static final int ICON_SIZE = 128;

    // Source: https://sashamaps.net/docs/resources/20-colors/
    private static final List<Integer> DISTINCT_COLORS = Collections.unmodifiableList(Arrays.asList(
            Color.parseColor("#e6194B"), // Red
            Color.parseColor("#f58231"), // Orange
//          Color.parseColor("#ffe119"), // Yellow
//          Color.parseColor("#bfef45"), // Lime
            Color.parseColor("#3cb44b"), // Green
//          Color.parseColor("#42d4f4"), // Cyan
            Color.parseColor("#4363d8"), // Blue
            Color.parseColor("#911eb4"), // Purple
            Color.parseColor("#f032e6"), // Magenta
//          Color.parseColor("#a9a9a9"), // Grey
            Color.parseColor("#800000"), // Maroon
            Color.parseColor("#9A6324"), // Brown
            Color.parseColor("#808000"), // Olive
            Color.parseColor("#469990"), // Teal
            Color.parseColor("#000075") // Navy
//          Color.parseColor("#000000"), // Black
//          Color.parseColor("#fabed4"), // Pink
//          Color.parseColor("#ffd8b1"), // Apricot
//          Color.parseColor("#fffac8"), // Beige
//          Color.parseColor("#aaffc3"), // Mint
//          Color.parseColor("#dcbeff"), // Lavender
//          Color.parseColor("#ffffff")  // White
    ));

    private static Map<String, IconPack> loadedPacks = new HashMap<>();

    private static File getIconPacksDir(Context context) {
        File iconPacksDir = new File(context.getFilesDir(), "iconpacks");
        if(!iconPacksDir.exists()) {
            iconPacksDir.mkdirs();
        }
        return iconPacksDir;
    }

    public static IconPackMetadata importIconPack(Context context, Uri uri) throws IconPackException {
        IconPackMetadata meta = loadPackMetadata(context, uri);

        // TODO: check for existing icon pack
        File iconPackFile = new File(getIconPacksDir(context), meta.getUuid());

        try {
            if (!iconPackFile.exists()) {
                iconPackFile.createNewFile();
            }

            try (OutputStream out = new FileOutputStream(iconPackFile);
                 InputStream in = context.getContentResolver().openInputStream(uri)) {
                if(in == null) throw new IconPackException("Failed to read icon pack");
                byte[] bytes = IOUtil.readBytes(in);
                out.write(bytes);
            }
        }catch(IOException e) {
            throw new IconPackException("Failed to import icon pack", e);
        }

        return meta;
    }

    private static IconPackMetadata loadPackMetadata(Context context, Uri uri) throws IconPackException {
        try(InputStream in = context.getContentResolver().openInputStream(uri)) {
            if(in == null) throw new IconPackException("Failed to read icon pack");
            try(ZipInputStream zIn = new ZipInputStream(in)) {
                ZipEntry en;
                while((en = zIn.getNextEntry()) != null) {
                    if(en.getName().equals("pack.json")) {
                        byte[] entryBytes = readEntry(zIn, en);
                        return SettingsUtil.GSON.fromJson(new String(entryBytes, StandardCharsets.UTF_8), IconPackMetadata.class); // TODO: validate metadata
                    }
                }
            }
        }catch(IOException e) {
            throw new IconPackException("Failed to read icon pack", e);
        }

        throw new IconPackException("No pack.json");
    }

    public static List<IconPack> loadAllIconPacks(Context context) {
        File iconPacksDir = getIconPacksDir(context);

        String[] packIDs = iconPacksDir.list();
        if(packIDs == null) return Collections.emptyList();

        List<IconPack> packs = new ArrayList<>();
        for(String pack : packIDs) {
            try {
                packs.add(loadIconPack(context, pack));
            }catch(IconPackException e) {
                DialogUtil.showErrorDialog(context, "An icon pack failed to load", e);
            }
        }

        return packs;
    }

    public static IconPack loadIconPack(Context context, String uuid) throws IconPackException {
        if(loadedPacks.containsKey(uuid)) return loadedPacks.get(uuid);

        IconPack p = loadIconPack(new File(getIconPacksDir(context), uuid));
        if(p == null) return null;

        loadedPacks.put(uuid, p);
        return p;
    }

    private static IconPack loadIconPack(File file) throws IconPackException {
        if(!file.exists()) return null;

        try(ZipInputStream in = new ZipInputStream(new FileInputStream(file))) {
            IconPackMetadata metadata = null;
            Map<String, byte[]> files = new HashMap<>();

            ZipEntry en;
            while((en = in.getNextEntry()) != null) {
                byte[] entryBytes = readEntry(in, en);

                if(en.getName().equals("pack.json")) {
                    metadata = SettingsUtil.GSON.fromJson(new String(entryBytes, StandardCharsets.UTF_8), IconPackMetadata.class); // TODO: validate metadata
                }else {
                    files.put(en.getName(), entryBytes);
                }
            }

            if(metadata == null) throw new IconPackException("Missing icon pack metadata");

            Icon[] icons = new Icon[metadata.getIcons().length];
            int iconCount = 0;
            for(IconMetadata m : metadata.getIcons()) {
                byte[] bytes = files.get(m.getFilename());
                if(bytes == null) continue;
                icons[iconCount++] = new Icon(m, bytes);
            }

            Icon[] workingIcons = new Icon[iconCount];
            System.arraycopy(icons, 0, workingIcons, 0, iconCount);

            return new IconPack(metadata, workingIcons);
        }catch(IOException e) {
            throw new IconPackException("Failed to read icon pack", e);
        }
    }

    private static byte[] readEntry(ZipInputStream in, ZipEntry en) throws IOException {
        if (en.getSize() < 0 || en.getSize() > Integer.MAX_VALUE) {
            throw new IOException("Invalid ZIP entry");
        }

        byte[] entryBytes = new byte[(int) en.getSize()];

        int totalRead = 0;
        while (totalRead < entryBytes.length) {
            totalRead += in.read(entryBytes, totalRead, entryBytes.length - totalRead);
        }

        return entryBytes;
    }

    public static Bitmap generateCodeImage(String issuer, String name) {
        if(issuer == null || issuer.isEmpty()) issuer = name;
        if(issuer == null || issuer.isEmpty()) issuer = "?";

        Bitmap b = Bitmap.createBitmap(ICON_SIZE, ICON_SIZE, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);

        Paint p = new Paint();
        p.setColor(DISTINCT_COLORS.get(Math.abs(issuer.hashCode()) % DISTINCT_COLORS.size()));
        p.setStyle(Paint.Style.FILL);
        c.drawCircle(ICON_SIZE / 2, ICON_SIZE / 2, ICON_SIZE / 2, p);

        p.setColor(Color.WHITE);
        p.setAntiAlias(true);
        p.setTextSize(64);

        String text = issuer.substring(0, 1);
        Rect r = new Rect();
        p.getTextBounds(text, 0, text.length(), r);
        c.drawText(text, ICON_SIZE / 2 - r.exactCenterX(), ICON_SIZE / 2 - r.exactCenterY(), p);

        return b;
    }

    public static void loadEffectiveImage(Context context, String imageData, String issuer, String name, SVGImageView view, Consumer<String> setOTPImage) {
        List<IconPack> packs = IconUtil.loadAllIconPacks(context);

        byte[] imageBytes = null;
        if(!OTPData.IMAGE_DATA_NONE.equals(imageData)) {
            if (imageData == null) {
                for (IconPack pack : packs) {
                    Icon ic = pack.findIconForIssuer(issuer);
                    if (ic != null) {
                        imageBytes = ic.getBytes();
                        if(setOTPImage != null) {
                            setOTPImage.accept(Base64.encodeToString(imageBytes, Base64.DEFAULT));
                        }
                        break;
                    }
                }
            } else {
                try {
                    imageBytes = Base64.decode(imageData, Base64.DEFAULT);
                }catch(IllegalArgumentException ignored) {
                    // Just use default icon
                }
            }
        }

        if(imageBytes == null) {
            view.setImageBitmap(IconUtil.generateCodeImage(issuer, name));
        }else {
            Bitmap bm = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
            if(bm != null) {
                view.setImageBitmap(bm);
            }else {
                try {
                    SVG svg = SVG.getFromInputStream(new ByteArrayInputStream(imageBytes));
                    view.setSVG(svg);
                }catch(SVGParseException e) {
                    view.setImageBitmap(IconUtil.generateCodeImage(issuer, name));
                }
            }
        }
    }

    public static void loadEffectiveImage(Context context, OTPData data, SVGImageView view, Runnable saveOTP) {
        loadEffectiveImage(context, data.getImageData(), data.getIssuer(), data.getName(), view, saveOTP == null ? null : newData -> {
            data.setImageData(newData);
            saveOTP.run();
        });
    }

    public static Bitmap cutToIcon(Bitmap bitmap) {
        Bitmap b = Bitmap.createBitmap(ICON_SIZE, ICON_SIZE, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);

        double sourceRatio = bitmap.getWidth() / (double) bitmap.getHeight();
        int newWidth, newHeight, offsetX, offsetY;
        if(sourceRatio < 1) {
            newWidth = ICON_SIZE;
            newHeight = (int) (newWidth / sourceRatio);
            offsetX = 0;
            offsetY = (ICON_SIZE - newHeight) / 2;
        }else {
            newHeight = ICON_SIZE;
            newWidth = (int) (newHeight * sourceRatio);
            offsetX = (ICON_SIZE - newWidth) / 2;
            offsetY = 0;
        }

        Paint p = new Paint();
        Path path = new Path();
        path.addCircle(ICON_SIZE / 2, ICON_SIZE / 2, ICON_SIZE / 2, Path.Direction.CW);
        c.clipPath(path);
        c.drawBitmap(bitmap, null, new Rect(offsetX, offsetY, offsetX + newWidth, offsetY + newHeight), p);

        return b;
    }

}
