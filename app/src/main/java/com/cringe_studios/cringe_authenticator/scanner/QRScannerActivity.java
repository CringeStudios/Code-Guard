package com.cringe_studios.cringe_authenticator.scanner;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.view.MotionEvent;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
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

import com.cringe_studios.cringe_authenticator.BaseActivity;
import com.cringe_studios.cringe_authenticator.R;
import com.cringe_studios.cringe_authenticator.databinding.ActivityQrScannerBinding;
import com.cringe_studios.cringe_authenticator.model.OTPData;
import com.cringe_studios.cringe_authenticator.model.OTPMigrationPart;
import com.cringe_studios.cringe_authenticator.util.DialogUtil;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class QRScannerActivity extends BaseActivity {

    public static final int RESULT_ERROR = -2;

    private ActivityQrScannerBinding binding;

    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;

    private QRScanner scanner;

    private boolean process = true;

    private List<OTPData> currentCodes;

    private OTPMigrationPart lastPart;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[] {Manifest.permission.CAMERA}, 1234); // TODO: handle denied
        }

        binding = ActivityQrScannerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        currentCodes = new ArrayList<>();
        lastPart = null;

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

        scanner = new QRScanner();
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
            if(!process) {
                image.close();
                return;
            }

            Image mediaImage = image.getImage();
            if(mediaImage != null) {
                InputImage input = InputImage.fromMediaImage(mediaImage, image.getImageInfo().getRotationDegrees());
                scanner.scan(input, code -> {
                    image.close();

                    if(code != null) {
                        process = false;
                        importCode(code);
                    }
                }, error -> {
                    image.close();
                    error(error);
                });
                /*.addOnSuccessListener(barcodes -> {

                }).addOnFailureListener(e -> error("Failed to scan code"));*/
            }
        }
    }

    private void importCode(DetectedCode code) {
        if(code.isMigrationPart()) {
            OTPMigrationPart part = code.getMigrationPart();

            if((lastPart != null && part.getBatchIndex() != lastPart.getBatchIndex() + 1) || (lastPart == null && part.getBatchIndex() > 0)) {
                // Not next batch, or first batch (if nothing was scanned yet), keep looking
                process = true;
                return;
            }

            if(part.getBatchIndex() == 0) {
                DialogUtil.showYesNo(this, R.string.qr_scanner_migration_title, R.string.qr_scanner_migration_message, () -> {
                    if(part.getBatchSize() == 1) {
                        success(part.getOTPs());
                    }else {
                        currentCodes.addAll(Arrays.asList(part.getOTPs()));
                        lastPart = part;
                        Toast.makeText(this, getString(R.string.qr_scanner_migration_part, part.getBatchIndex()+ 1, part.getBatchSize()), Toast.LENGTH_LONG).show();
                        process = true;
                    }
                }, null);
            }else {
                currentCodes.addAll(Arrays.asList(part.getOTPs()));
                Toast.makeText(this, getString(R.string.qr_scanner_migration_part, part.getBatchIndex()+ 1, part.getBatchSize()), Toast.LENGTH_LONG).show();

                if(part.getBatchIndex() == part.getBatchSize() - 1) {
                    success(currentCodes.toArray(new OTPData[0]));
                }else {
                    lastPart = part;
                    process = true;
                }
            }

            return;
        }

        if(lastPart != null) {
            // Migration is being imported
            process = true;
            return;
        }

        try {
            success(code.getData());
        }catch(IllegalArgumentException e) {
            error(e.getMessage());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        scanner.close();
    }

    private void success(@NonNull OTPData... data) {
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

    private void cancel() {
        setResult(RESULT_CANCELED);
        finish();
    }

}
