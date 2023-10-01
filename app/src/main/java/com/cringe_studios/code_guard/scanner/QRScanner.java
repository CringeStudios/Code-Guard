package com.cringe_studios.code_guard.scanner;

import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.core.util.Consumer;

import com.cringe_studios.code_guard.model.OTPMigrationPart;
import com.cringe_studios.code_guard.util.OTPParser;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

public class QRScanner {

    private final BarcodeScanner scanner;

    public QRScanner() {
        scanner = BarcodeScanning.getClient();
    }

    public void scan(InputImage image, Consumer<DetectedCode> onSuccess, Consumer<String> onFailure) {
        scanner.process(image).addOnSuccessListener(barcodes -> {
            if(barcodes.isEmpty()) {
                onSuccess.accept(null);
                return;
            }

            Barcode code = null;

            for(Barcode c : barcodes) {
                if(c.getValueType() == Barcode.TYPE_TEXT) {
                    code = c; // TODO: maybe consider whether this code is actually valid (for cases with multiple text QR codes in one image)
                    break;
                }
            }

            if(code == null) {
                onSuccess.accept(null);
                return;
            }

            Uri uri = Uri.parse(code.getRawValue());
            Result r = parse(uri);
            if(r.code != null) {
                onSuccess.accept(r.code);
            }else {
                onFailure.accept(r.error);
            }
        })
        .addOnFailureListener(e -> onFailure.accept(e.toString()));
    }

    private Result parse(Uri uri) {
        if("otpauth-migration".equalsIgnoreCase(uri.getScheme())) {
            OTPMigrationPart part;

            try {
                part = OTPParser.parseMigration(uri);
            }catch(IllegalArgumentException e) {
                return new Result(e.getMessage());
            }

            return new Result(new DetectedCode(part));
        }

        try {
            return new Result(new DetectedCode(OTPParser.parse(uri)));
        }catch(IllegalArgumentException e) {
            return new Result(e.getMessage());
        }
    }

    public void close() {
        scanner.close();
    }

    private static class Result {

        public final DetectedCode code;
        public final String error;

        private Result(@NonNull DetectedCode code) {
            this.code = code;
            this.error = null;
        }

        private Result(String error) {
            this.error = error;
            this.code = null;
        }

    }

}
