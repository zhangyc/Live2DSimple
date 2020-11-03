package com.live2d.live2dsimple.facedetector.view;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.mlkit.vision.face.FaceDetectorOptions;
import com.live2d.live2dsimple.R;
import com.live2d.live2dsimple.facedetector.facedetector.FaceDetectorProcessor;
import com.live2d.live2dsimple.utils.CameraSource;
import com.live2d.live2dsimple.utils.CameraSourcePreview;
import com.live2d.live2dsimple.utils.GraphicOverlay;
import com.live2d.live2dsimple.utils.preference.PreferenceUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Live2dByFaceActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener{
    LinearLayout container;
    private Live2dGLSurfaceView mGLSurfaceView;
    final String MODEL_PATH = "live2d/haru/haru.moc";
    final String[] TEXTURE_PATHS = {
            "live2d/haru/haru.1024/texture_00.png",
            "live2d/haru/haru.1024/texture_01.png",
            "live2d/haru/haru.1024/texture_02.png"
    };
    private CameraSource cameraSource = null;
    private CameraSourcePreview preview;
    private GraphicOverlay graphicOverlay;
    private static final String TAG = "Live2dByFaceActivity";
    private static final String FACE_DETECTION = "Face Detection";
    protected final double[] emotion = new double[10];

    private String selectedModel = FACE_DETECTION;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live2d_by_face);
        container = (LinearLayout) findViewById(R.id.ll_container);
        mGLSurfaceView = new Live2dGLSurfaceView(Live2dByFaceActivity.this);
        container.addView(mGLSurfaceView);
        initCameraView();

    }
    private void initCameraView() {

        preview = findViewById(R.id.preview_view);
        if (preview == null) {
            Log.d(TAG, "Preview is null");
        }
        graphicOverlay = findViewById(R.id.graphic_overlay);
        if (graphicOverlay == null) {
            Log.d(TAG, "graphicOverlay is null");
        }
        ToggleButton facingSwitch = findViewById(R.id.facing_switch);
        facingSwitch.setOnCheckedChangeListener(this);
        if (allPermissionsGranted()) {
            createCameraSource(selectedModel);
            startCameraSource();
        } else {
            getRuntimePermissions();
        }
    }
    private void startCameraSource() {
        if (cameraSource != null) {
            try {
                if (preview == null) {
                    Log.d(TAG, "resume: Preview is null");
                }
                if (graphicOverlay == null) {
                    Log.d(TAG, "resume: graphOverlay is null");
                }
                preview.start(cameraSource, graphicOverlay);
            } catch (IOException e) {
                Log.e(TAG, "Unable to start camera source.", e);
                cameraSource.release();
                cameraSource = null;
            }
        }
    }
    private String[] getRequiredPermissions() {
        try {
            PackageInfo info =
                    this.getPackageManager()
                            .getPackageInfo(this.getPackageName(), PackageManager.GET_PERMISSIONS);
            String[] ps = info.requestedPermissions;
            if (ps != null && ps.length > 0) {
                return ps;
            } else {
                return new String[0];
            }
        } catch (Exception e) {
            return new String[0];
        }
    }
    private boolean allPermissionsGranted() {
        for (String permission : getRequiredPermissions()) {
            if (!isPermissionGranted(this, permission)) {
                return false;
            }
        }
        return true;
    }

    private void getRuntimePermissions() {
        List<String> allNeededPermissions = new ArrayList<>();
        for (String permission : getRequiredPermissions()) {
            if (!isPermissionGranted(this, permission)) {
                allNeededPermissions.add(permission);
            }
        }

        if (!allNeededPermissions.isEmpty()) {
            ActivityCompat.requestPermissions(
                    this, allNeededPermissions.toArray(new String[0]), PERMISSION_REQUESTS);
        }
    }
    private static final int PERMISSION_REQUESTS = 1;

    private static boolean isPermissionGranted(Context context, String permission) {
        if (ContextCompat.checkSelfPermission(context, permission)
                == PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "Permission granted: " + permission);
            return true;
        }
        Log.i(TAG, "Permission NOT granted: " + permission);
        return false;
    }
    FaceDetectorProcessor _faceDetectorProcessor;
    private void createCameraSource(String model) {
        // If there's no existing cameraSource, create one.
        if (cameraSource == null) {
            cameraSource = new CameraSource(this, graphicOverlay);
        }

        try {
            switch (model) {
                case FACE_DETECTION:
                    Log.i(TAG, "Using Face Detector Processor");
                    FaceDetectorOptions faceDetectorOptions =
                            PreferenceUtils.getFaceDetectorOptionsForLivePreview(this);
                    _faceDetectorProcessor=new FaceDetectorProcessor(this, faceDetectorOptions);
                    mGLSurfaceView.init(Live2dByFaceActivity.this, MODEL_PATH, TEXTURE_PATHS, 1, 1,_faceDetectorProcessor);

                    cameraSource.setMachineLearningFrameProcessor(
                            _faceDetectorProcessor);
                    break;
                default:
                    Log.e(TAG, "Unknown model: " + model);
            }
        } catch (Exception e) {
            Log.e(TAG, "Can not create image processor: " + model, e);
            Toast.makeText(
                    getApplicationContext(),
                    "Can not create image processor: " + e.getMessage(),
                    Toast.LENGTH_LONG)
                    .show();
        }
    }
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        Log.d(TAG, "Set facing");
        if (cameraSource != null) {
            if (isChecked) {
                cameraSource.setFacing(CameraSource.CAMERA_FACING_FRONT);
            } else {
                cameraSource.setFacing(CameraSource.CAMERA_FACING_BACK);
            }
        }
        preview.stop();
        startCameraSource();
    }
}