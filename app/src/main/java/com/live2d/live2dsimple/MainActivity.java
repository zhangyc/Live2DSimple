package com.live2d.live2dsimple;

import android.app.Activity;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import jp.live2d.utils.android.SoundManager;

public final class MainActivity extends Activity {
    // Live2Dの管理
    private LAppLive2DManager live2DMgr;
    private static final String TAG = "LivePreviewActivity";
    private static final String FACE_DETECTION = "Face Detection";
    protected final double[] emotion = new double[10];

    private String selectedModel = FACE_DETECTION;
    @Override
    protected final void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        // オブジェクトを初期化
        live2DMgr = new LAppLive2DManager(this.getApplicationContext());
        // GUIを初期化


    }

    /**
     * Starts or restarts the camera source, if it exists. If the camera source doesn't exist yet
     * (e.g., because onResume was called before the camera source was created), this will be called
     * again when the camera source is created.
     */
    private void setupGUI() {
        setContentView(R.layout.activity_main);
        //  Viewの初期化
        LAppView lAppView = live2DMgr.createView(this);
        // activity_main.xmlにLive2DのViewをレイアウトする
        LinearLayout layout = findViewById(R.id.live2DLayout);

        layout.addView(lAppView, 0, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        // モデル切り替えボタン
        ImageButton iBtn = findViewById(R.id.imageButton1);
        ClickListener listener = new ClickListener();
        iBtn.setOnClickListener(listener);
        findViewById(R.id.live2dView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
              ///lAppView.getDragMgr().set(0.5f,0.5f);
            }
        });
    }

    private static final int PERMISSION_REQUESTS = 1;

    @Override
    protected final void onDestroy() {
        exit();
        super.onDestroy();
    }

    @Override
    protected final void onPause() {
        super.onPause();
        live2DMgr.onPause();
    }

    @Override
    protected final void onResume() {
        super.onResume();
        live2DMgr.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        SoundManager.init();
    }

    /*
     * GUIの初期化
     * activity_main.xmlからViewを作成し、そこにLive2Dを配置する
     */
    private TextToSpeech tts;

    private void exit() {
        SoundManager.release();
    }


    // ボタンを押した時のイベント
    private final class ClickListener implements View.OnClickListener {
        @Override
        public final void onClick(View v) {
            Toast.makeText(MainActivity.this, "change model", Toast.LENGTH_SHORT).show();
            live2DMgr.changeModel(); // Live2D Event
        }
    }
}