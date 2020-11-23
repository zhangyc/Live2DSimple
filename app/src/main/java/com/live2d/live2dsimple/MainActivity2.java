package com.live2d.live2dsimple;

import androidx.appcompat.app.AppCompatActivity;

import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.View;


import com.live2d.live2dsimple.view.Live2dRenderer2;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity2 extends AppCompatActivity {
    private GLSurfaceView glView; // Use GLSurfaceView
    private GLSurfaceView glView2;
    final String MODEL_PATH = "live2d/haru/haru.moc";
    final String[] TEXTURE_PATHS = {
            "live2d/haru/haru.1024/texture_00.png",
            "live2d/haru/haru.1024/texture_01.png",
            "live2d/haru/haru.1024/texture_02.png"
    };
    final String MODEL_PATH2 = "live2d/shizuku/shizuku.moc";
    final String[] TEXTURE_PATHS2 = {
            "live2d/shizuku/shizuku.1024/texture_00.png",
            "live2d/shizuku/shizuku.1024/texture_01.png",
            "live2d/shizuku/shizuku.1024/texture_02.png",
            "live2d/shizuku/shizuku.1024/texture_03.png",
            "live2d/shizuku/shizuku.1024/texture_04.png",

    };
    private GLSurfaceView glView3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        glView=findViewById(R.id.gl1);
//        glView.setEGLContextClientVersion(2);

        glView2=findViewById(R.id.gl2);
        glView3=findViewById(R.id.gl3);

//        glView2.setEGLContextClientVersion(2);
        Live2dRenderer2 render1 = new Live2dRenderer2(this, MODEL_PATH, TEXTURE_PATHS, 1, 1);
        Live2dRenderer2 render2 = new Live2dRenderer2(this, MODEL_PATH, TEXTURE_PATHS, 1, 1);
        Live2dRenderer2 render3 = new Live2dRenderer2(this, MODEL_PATH, TEXTURE_PATHS, 1, 1);

        glView.setRenderer(render1); // Use a custom renderer
        glView2.setRenderer(render2); // Use a custom renderer
        glView3.setRenderer(render3);
        glView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
        glView2.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
        glView3.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
        List<GLSurfaceView> list=new ArrayList<>();
        list.add(glView);
        list.add(glView2);
        list.add(glView3);
        glView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
        glView2.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
        glView3.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true){

                    Random random=new Random();
                    int index=random.nextInt(3);
                    for (GLSurfaceView view:
                         list) {
                        view.onPause();
                    }
                    for (int i=0;i<list.size();i++){
                        if (i==index){
                            list.get(i).onResume();
                        }else {
                            list.get(i).onPause();
                        }
                    }

                }
            }
        }).start();

        findViewById(R.id.btn_change).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                glView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
                glView2.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
            }
        });
        findViewById(R.id.btn_change2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                glView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
                glView2.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        glView.onResume();
        glView2.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        glView.onPause();
        glView2.onPause();
    }
}