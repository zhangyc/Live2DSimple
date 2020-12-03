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
    private GLSurfaceView glView2;
    final String MODEL_PATH = "live2d/shizuku/shizuku.moc";
    final String[] TEXTURE_PATHS = {
            "live2d/fill/fill.2048/texture_00.png",
    };
    final String MODEL_PATH2 = "live2d/model01/model01.moc";
    final String[] TEXTURE_PATHS2 = {
            "live2d/model01/model01.2048/texture_00.png",

    };
    private GLSurfaceView glView3;
    Random random=new Random();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        glView=findViewById(R.id.gl1);
//        glView.setEGLContextClientVersion(2);

        glView2=findViewById(R.id.gl2);
        glView3=findViewById(R.id.gl3);
        glView.setPreserveEGLContextOnPause(true);
        glView2.setPreserveEGLContextOnPause(true);
        glView3.setPreserveEGLContextOnPause(true);


//        glView2.setEGLContextClientVersion(2);
        Live2dRenderer1 render1 = new Live2dRenderer1(this, MODEL_PATH2, TEXTURE_PATHS2, 1, 1);
        Live2dRenderer1 render2 = new Live2dRenderer1(this, MODEL_PATH2, TEXTURE_PATHS2, 1, 1);
        Live2dRenderer1 render3 = new Live2dRenderer1(this, MODEL_PATH2, TEXTURE_PATHS2, 1, 1);

        glView.setRenderer(render1); // Use a custom renderer
        glView2.setRenderer(render2); // Use a custom renderer
        glView3.setRenderer(render3);
        glView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
        glView2.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
        glView3.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
//        List<GLSurfaceView> list=new ArrayList<>();
        List<Live2dRenderer1> rend=new ArrayList<>();
        rend.add(render1);
        rend.add(render2);
        rend.add(render3);
//
//        list.add(glView);
//        list.add(glView2);
//        list.add(glView3);
//        glView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
//        glView2.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
//        glView3.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true){
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    int index=random.nextInt(3);
                    for (int i=0;i<rend.size();i++){
                        if (i==index){
                            rend.get(i).can=true;
                        }else {
                            rend.get(i).can=false;
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
        glView3.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        glView.onPause();
        glView2.onPause();
        glView3.onPause();
    }
}