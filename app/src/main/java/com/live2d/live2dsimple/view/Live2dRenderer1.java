package com.live2d.live2dsimple.view;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.util.Log;

import java.io.InputStream;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import jp.live2d.android.Live2DModelAndroid;
import jp.live2d.android.UtOpenGL;
import jp.live2d.framework.L2DEyeBlink;

public class Live2dRenderer1 implements GLSurfaceView.Renderer {
    private Activity mActivity;

    private Live2DModelAndroid live2DModel;

    private L2DEyeBlink mEyeBlink;

    private String MODEL_PATH;
    private String[] TEXTURE_PATHS;

    private float wRatio, hRatio;


    public Live2dRenderer1(Activity activity, String MODEL_PATH, String[] TEXTURE_PATHS,
                           float wRatio, float hRatio) {
        this.mActivity = activity;
        this.MODEL_PATH = MODEL_PATH;
        this.TEXTURE_PATHS = TEXTURE_PATHS;
        this.wRatio = wRatio;
        this.hRatio = hRatio;
        this.mEyeBlink = new L2DEyeBlink();
    }

    private void loadLive2dModel(GL10 gl, String modelPath, String[] texturePath) {
        try {
            InputStream in = this.mActivity.getAssets().open(modelPath);
            live2DModel = Live2DModelAndroid.loadModel(in);
            in.close();

            for (int i = 0; i < texturePath.length; i++) {
                InputStream tin = this.mActivity.getAssets().open(texturePath[i]);
                int texNo = UtOpenGL.loadTexture(gl, tin, true);
                live2DModel.setTexture(i, texNo);
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public boolean can;

    @Override
    public void onDrawFrame(GL10 gl)
    {

        //live2DModel.saveParam();
        ///通过activity的值来更新

        if (can){
            gl.glMatrixMode(GL10.GL_MODELVIEW);
            gl.glLoadIdentity();
            gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
            mEyeBlink.updateParam(live2DModel);
            live2DModel.setGL(gl);
            live2DModel.update();
            live2DModel.draw();

        }

    }


    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height)
    {
        gl.glViewport(0 , 0 , width , height);

        gl.glMatrixMode(GL10.GL_PROJECTION);
        gl.glLoadIdentity();

        float modelWidth = live2DModel.getCanvasWidth();
        float aspect = (float)width/height;

        gl.glOrthof(0, wRatio*modelWidth, hRatio*modelWidth / aspect, 0, 0.5f, -0.5f);
    }


    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config)
    {
        Log.d("Render", "onSurfaceCreated");
        loadLive2dModel(gl, MODEL_PATH, TEXTURE_PATHS);
    }
}