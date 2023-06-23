package com.cringe_studios.cringe_authenticator.scanner;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.content.ContextCompat;

import com.cringe_studios.cringe_authenticator.OTPData;
import com.cringe_studios.cringe_authenticator.databinding.ActivityQrScannerBinding;
import com.cringe_studios.cringe_authenticator_library.OTPAlgorithm;
import com.cringe_studios.cringe_authenticator_library.OTPType;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

import java.util.concurrent.ExecutionException;

public class QRScannerActivity extends AppCompatActivity {

    private ActivityQrScannerBinding binding;

    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;

    private BarcodeScanner scanner;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[] {Manifest.permission.CAMERA}, 1234);
        }

        binding = ActivityQrScannerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            Log.i("AMOGUS", "Got something");
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindPreview(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                // No errors need to be handled for this Future.
                // This should never be reached.
            }
        }, ContextCompat.getMainExecutor(this));

        BarcodeScannerOptions opts = new BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                .build();

        scanner = BarcodeScanning.getClient();
    }

    void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {
        Preview preview = new Preview.Builder()
                .build();

        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        preview.setSurfaceProvider(binding.preview.getSurfaceProvider());

        ImageAnalysis analysis = new ImageAnalysis.Builder()
                //.setTargetResolution(new Size(1280, 720))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();
        analysis.setAnalyzer(ContextCompat.getMainExecutor(this), new Amogus());

        Camera camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, analysis);
    }

    private class Amogus implements ImageAnalysis.Analyzer {

        @OptIn(markerClass = ExperimentalGetImage.class)
        @Override
        public void analyze(@NonNull ImageProxy image) {
            Image mediaImage = image.getImage();
            if(mediaImage != null) {
                InputImage input = InputImage.fromMediaImage(mediaImage, image.getImageInfo().getRotationDegrees());
                scanner.process(input).addOnSuccessListener(barcodes -> {
                    //Log.i("AMOGUS", "found " + barcodes.size() + " codes");
                    image.close();

                    if(barcodes.size() >= 1) {
                        Barcode code = null;

                        Log.i("AMOGUS", "TYPE " + barcodes.get(0).getValueType());
                        for(Barcode c : barcodes) {
                            if(c.getValueType() == Barcode.TYPE_TEXT) {
                                code = c;
                                break;
                            }
                        }

                        if(code == null) return;

                        Uri uri = Uri.parse(code.getRawValue());
                        Log.i("AMOGUS", code.getRawValue());
                        Log.i("AMOGUS", uri.getHost());
                        Log.i("AMOGUS", uri.getPath());

                        String type = uri.getHost();
                        String accountName = uri.getPath();
                        String secret = uri.getQueryParameter("secret");
                        String algorithm = uri.getQueryParameter("algorithm");
                        String digits = uri.getQueryParameter("digits");
                        String period = uri.getQueryParameter("period");
                        String counter = uri.getQueryParameter("counter");

                        if(type == null || secret == null) {
                            error("Missing params");
                            return;
                        }

                        OTPType fType;
                        try {
                            fType = OTPType.valueOf(type.toUpperCase());
                        }catch(IllegalArgumentException e) {
                            Log.i("AMOGUS", e.toString());
                            error("Failed to parse OTP parameters");
                            return;
                        }

                        if(fType == OTPType.HOTP && counter == null) {
                            error("Missing params");
                            return;
                        }

                        if(accountName == null || accountName.length() < 2 /* Because path is /accName, so 2 letters for acc with 1 letter name */) {
                            // TODO: error
                            Log.i("AMOGUS", "Missing params");
                            error("Missing OTP parameters");
                            return;
                        }

                        accountName = accountName.substring(1);

                        try {
                            // 0 or null for defaults (handled by Cringe-Authenticator-Library)
                            OTPAlgorithm fAlgorithm = algorithm == null ? null : OTPAlgorithm.valueOf(algorithm.toUpperCase());
                            int fDigits = digits == null ? 0 : Integer.parseInt(digits);
                            int fPeriod = period == null ? 0 : Integer.parseInt(period);
                            int fCounter = counter == null ? 0 : Integer.parseInt(counter);

                            success(new OTPData(accountName, fType, secret, fAlgorithm, fDigits, fPeriod, fCounter));
                        }catch(IllegalArgumentException e) {
                            Log.i("AMOGUS", e.toString());
                            error("Failed to parse OTP parameters");
                            return;
                        }
                    }
                }).addOnFailureListener(e -> {});
            }
        }
    }

    private void success(@NonNull OTPData data) {
        Intent result = new Intent();
        result.putExtra("data", data);
        setResult(Activity.RESULT_OK, result);
        finish();
    }

    private void error(String errorMessage) {
        setResult(Activity.RESULT_CANCELED, null);
        finish();
    }

}
