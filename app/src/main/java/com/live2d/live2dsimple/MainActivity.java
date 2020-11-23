//package com.live2d.live2dsimple;
//
//import android.app.Activity;
//import android.content.Context;
//import android.content.pm.PackageInfo;
//import android.content.pm.PackageManager;
//import android.opengl.GLSurfaceView;
//import android.os.Bundle;
//import android.speech.tts.TextToSpeech;
//import android.util.Log;
//import android.view.View;
//import android.view.Window;
//import android.widget.CompoundButton;
//import android.widget.FrameLayout;
//import android.widget.ImageButton;
//import android.widget.LinearLayout;
//import android.widget.Toast;
//import android.widget.ToggleButton;
//
//
//import androidx.core.app.ActivityCompat;
//import androidx.core.content.ContextCompat;
//
//import com.google.mlkit.common.model.LocalModel;
//import com.google.mlkit.vision.face.Face;
//import com.google.mlkit.vision.face.FaceDetectorOptions;
//import com.google.mlkit.vision.label.automl.AutoMLImageLabelerLocalModel;
//import com.google.mlkit.vision.label.automl.AutoMLImageLabelerOptions;
//import com.google.mlkit.vision.label.custom.CustomImageLabelerOptions;
//import com.google.mlkit.vision.label.defaults.ImageLabelerOptions;
//import com.google.mlkit.vision.objects.custom.CustomObjectDetectorOptions;
//import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions;
//import com.google.mlkit.vision.pose.PoseDetectorOptionsBase;
//import com.live2d.live2dsimple.barcodescanner.BarcodeScannerProcessor;
//import com.live2d.live2dsimple.facedetector.facedetector.FaceDetectorProcessor;
//import com.live2d.live2dsimple.labeldetector.LabelDetectorProcessor;
//import com.live2d.live2dsimple.objectdetector.ObjectDetectorProcessor;
//import com.live2d.live2dsimple.posedetector.PoseDetectorProcessor;
//import com.live2d.live2dsimple.textdetector.TextRecognitionProcessor;
//import com.live2d.live2dsimple.utils.CameraSource;
//import com.live2d.live2dsimple.utils.CameraSourcePreview;
//import com.live2d.live2dsimple.utils.GraphicOverlay;
//import com.live2d.live2dsimple.utils.preference.PreferenceUtils;
//
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Locale;
//import java.util.Set;
//
//import jp.live2d.framework.L2DStandardID;
//import jp.live2d.framework.L2DTargetPoint;
//import jp.live2d.utils.android.SoundManager;
//
//public final class MainActivity extends Activity implements CompoundButton.OnCheckedChangeListener {
//    // Live2Dの管理
//    private LAppLive2DManager live2DMgr;
//    private CameraSource cameraSource = null;
//    private CameraSourcePreview preview;
//    private GraphicOverlay graphicOverlay;
//    private static final String TAG = "LivePreviewActivity";
//    private static final String FACE_DETECTION = "Face Detection";
//    protected final double[] emotion = new double[10];
//
//    private String selectedModel = FACE_DETECTION;
//    @Override
//    protected final void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        requestWindowFeature(Window.FEATURE_NO_TITLE);
//        // オブジェクトを初期化
//        live2DMgr = new LAppLive2DManager(this.getApplicationContext());
//        // GUIを初期化
//        setupGUI();
//
//    }
//
//    private void initCameraView() {
//
//        preview = findViewById(R.id.preview_view);
//        if (preview == null) {
//            Log.d(TAG, "Preview is null");
//        }
//        graphicOverlay = findViewById(R.id.graphic_overlay);
//        if (graphicOverlay == null) {
//            Log.d(TAG, "graphicOverlay is null");
//        }
//        ToggleButton facingSwitch = findViewById(R.id.facing_switch);
//        facingSwitch.setOnCheckedChangeListener(this);
//    }
//    /**
//     * Starts or restarts the camera source, if it exists. If the camera source doesn't exist yet
//     * (e.g., because onResume was called before the camera source was created), this will be called
//     * again when the camera source is created.
//     */
//    private void startCameraSource() {
//        if (cameraSource != null) {
//            try {
//                if (preview == null) {
//                    Log.d(TAG, "resume: Preview is null");
//                }
//                if (graphicOverlay == null) {
//                    Log.d(TAG, "resume: graphOverlay is null");
//                }
//                preview.start(cameraSource, graphicOverlay);
//            } catch (IOException e) {
//                Log.e(TAG, "Unable to start camera source.", e);
//                cameraSource.release();
//                cameraSource = null;
//            }
//        }
//    }
//    private String[] getRequiredPermissions() {
//        try {
//            PackageInfo info =
//                    this.getPackageManager()
//                            .getPackageInfo(this.getPackageName(), PackageManager.GET_PERMISSIONS);
//            String[] ps = info.requestedPermissions;
//            if (ps != null && ps.length > 0) {
//                return ps;
//            } else {
//                return new String[0];
//            }
//        } catch (Exception e) {
//            return new String[0];
//        }
//    }
//    private boolean allPermissionsGranted() {
//        for (String permission : getRequiredPermissions()) {
//            if (!isPermissionGranted(this, permission)) {
//                return false;
//            }
//        }
//        return true;
//    }
//
//    private void getRuntimePermissions() {
//        List<String> allNeededPermissions = new ArrayList<>();
//        for (String permission : getRequiredPermissions()) {
//            if (!isPermissionGranted(this, permission)) {
//                allNeededPermissions.add(permission);
//            }
//        }
//
//        if (!allNeededPermissions.isEmpty()) {
//            ActivityCompat.requestPermissions(
//                    this, allNeededPermissions.toArray(new String[0]), PERMISSION_REQUESTS);
//        }
//    }
//    private static final int PERMISSION_REQUESTS = 1;
//
//    private static boolean isPermissionGranted(Context context, String permission) {
//        if (ContextCompat.checkSelfPermission(context, permission)
//                == PackageManager.PERMISSION_GRANTED) {
//            Log.i(TAG, "Permission granted: " + permission);
//            return true;
//        }
//        Log.i(TAG, "Permission NOT granted: " + permission);
//        return false;
//    }
//    FaceDetectorProcessor _faceDetectorProcessor;
//    private void createCameraSource(String model) {
//        // If there's no existing cameraSource, create one.
//        if (cameraSource == null) {
//            cameraSource = new CameraSource(this, graphicOverlay);
//        }
//
//        try {
//            switch (model) {
//                case FACE_DETECTION:
//                    Log.i(TAG, "Using Face Detector Processor");
//                    FaceDetectorOptions faceDetectorOptions =
//                            PreferenceUtils.getFaceDetectorOptionsForLivePreview(this);
//                     _faceDetectorProcessor=new FaceDetectorProcessor(this, faceDetectorOptions,live2DMgr);
//                    cameraSource.setMachineLearningFrameProcessor(
//                            _faceDetectorProcessor);
//                    break;
//                default:
//                    Log.e(TAG, "Unknown model: " + model);
//            }
//        } catch (Exception e) {
//            Log.e(TAG, "Can not create image processor: " + model, e);
//            Toast.makeText(
//                    getApplicationContext(),
//                    "Can not create image processor: " + e.getMessage(),
//                    Toast.LENGTH_LONG)
//                    .show();
//        }
//    }
//    @Override
//    protected final void onDestroy() {
//        exit();
//        super.onDestroy();
//    }
//
//    @Override
//    protected final void onPause() {
//        super.onPause();
//        live2DMgr.onPause();
//    }
//
//    @Override
//    protected final void onResume() {
//        super.onResume();
//        live2DMgr.onResume();
//    }
//
//    @Override
//    protected void onStart() {
//        super.onStart();
//        SoundManager.init();
//    }
//
//    /*
//     * GUIの初期化
//     * activity_main.xmlからViewを作成し、そこにLive2Dを配置する
//     */
//    private void setupGUI() {
//        setContentView(R.layout.activity_main);
//        //initview
//        initCameraView();
//        if (allPermissionsGranted()) {
//            createCameraSource(selectedModel);
//            startCameraSource();
//        } else {
//            getRuntimePermissions();
//        }
//        //  Viewの初期化
//        LAppView lAppView = live2DMgr.createView(this);
//
//
//        // activity_main.xmlにLive2DのViewをレイアウトする
//        LinearLayout layout = findViewById(R.id.live2DLayout);
//
//        layout.addView(lAppView, 0, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
//
//
//        // モデル切り替えボタン
//        ImageButton iBtn = findViewById(R.id.imageButton1);
//        ClickListener listener = new ClickListener();
//        iBtn.setOnClickListener(listener);
//        findViewById(R.id.live2dView).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//              ///lAppView.getDragMgr().set(0.5f,0.5f);
//            }
//        });
//    }
//    private TextToSpeech tts;
//
//    private void exit() {
//        SoundManager.release();
//    }
//
//    @Override
//    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//        Log.d(TAG, "Set facing");
//        if (cameraSource != null) {
//            if (isChecked) {
//                cameraSource.setFacing(CameraSource.CAMERA_FACING_FRONT);
//            } else {
//                cameraSource.setFacing(CameraSource.CAMERA_FACING_BACK);
//            }
//        }
//        preview.stop();
//        startCameraSource();
//    }
//
//    // ボタンを押した時のイベント
//    private final class ClickListener implements View.OnClickListener {
//        @Override
//        public final void onClick(View v) {
//            Toast.makeText(MainActivity.this, "change model", Toast.LENGTH_SHORT).show();
//            live2DMgr.changeModel(); // Live2D Event
//        }
//    }
//}