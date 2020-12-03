package com.live2d.live2dsimple;

import androidx.appcompat.app.AppCompatActivity;

import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.View;


import com.live2d.live2dsimple.view.Live2dRenderer1;
import com.live2d.live2dsimple.view.Live2dRenderer2;
import com.live2d.live2dsimple.view.Live2dRenderer3;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity2 extends AppCompatActivity {
    private GLSurfaceView glView; // Use GLSurfaceView
    final String MODEL_PATH = "live2d/shizuku/shizuku.moc";
    final String[] TEXTURE_PATHS = {
            "live2d/fill/fill.2048/texture_00.png",
    };
    final String MODEL_PATH2 = "live2d/model01/model01.moc";
    final String[] TEXTURE_PATHS2 = {
            "live2d/model01/model01.2048/texture_00.png",

    };
    Random random=new Random();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        glView=findViewById(R.id.gl1);
        Live2dRenderer1 render1 = new Live2dRenderer1(this, MODEL_PATH2, TEXTURE_PATHS2, 1, 1);

        glView.setRenderer(render1); // Use a custom renderer

        findViewById(R.id.btn_change).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                glView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
            }
        });
        findViewById(R.id.btn_change2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                glView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        glView.onResume();

    }

    @Override
    protected void onPause() {
        super.onPause();
        glView.onPause();

    }
}