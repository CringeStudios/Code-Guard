package com.cringe_studios.cringe_authenticator.scanner;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.FocusMeteringAction;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.MeteringPointFactory;
import androidx.camera.core.Preview;
import androidx.camera.core.SurfaceOrientedMeteringPointFactory;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.content.ContextCompat;

import com.cringe_studios.cringe_authenticator.OTPData;
import com.cringe_studios.cringe_authenticator.databinding.ActivityQrScannerBinding;
import com.cringe_studios.cringe_authenticator.util.OTPParser;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

import java.util.concurrent.ExecutionException;

public class QRScannerActivity extends AppCompatActivity {

    public static final int RESULT_ERROR = -2;

    private ActivityQrScannerBinding binding;

    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;

    private BarcodeScanner scanner;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[] {Manifest.permission.CAMERA}, 1234);
        }

        binding = ActivityQrScannerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindPreview(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                // No errors need to be handled for this Future.
                // This should never be reached.
            }
        }, ContextCompat.getMainExecutor(this));

        scanner = BarcodeScanning.getClient();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

    void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {
        Preview preview = new Preview.Builder()
                .build();

        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        preview.setSurfaceProvider(binding.preview.getSurfaceProvider());

        ImageAnalysis analysis = new ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();
        analysis.setAnalyzer(ContextCompat.getMainExecutor(this), new QRAnalyzer());

        Camera camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, analysis);

        binding.preview.setOnTouchListener((view, event) -> {
            if(event.getAction() != MotionEvent.ACTION_DOWN) return true;
            view.performClick();

            MeteringPointFactory factory = new SurfaceOrientedMeteringPointFactory(binding.preview.getWidth(), binding.preview.getHeight());
            camera.getCameraControl().startFocusAndMetering(new FocusMeteringAction.Builder(factory.createPoint(event.getX(), event.getY())).build());

            return true;
        });
    }

    private class QRAnalyzer implements ImageAnalysis.Analyzer {

        @OptIn(markerClass = ExperimentalGetImage.class)
        @Override
        public void analyze(@NonNull ImageProxy image) {
            Image mediaImage = image.getImage();
            if(mediaImage != null) {
                InputImage input = InputImage.fromMediaImage(mediaImage, image.getImageInfo().getRotationDegrees());
                scanner.process(input).addOnSuccessListener(barcodes -> {
                    image.close();

                    if(barcodes.size() >= 1) {
                        Barcode code = null;

                        for(Barcode c : barcodes) {
                            if(c.getValueType() == Barcode.TYPE_TEXT) {
                                code = c;
                                break;
                            }
                        }

                        if(code == null) return;

                        Uri uri = Uri.parse(code.getRawValue());
                        try {
                            success(OTPParser.parse(uri));
                        }catch(IllegalArgumentException e) {
                            error(e.getMessage());
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
        Intent result = new Intent();
        result.putExtra("error", errorMessage);
        setResult(RESULT_ERROR, result);
        finish();
    }

}
