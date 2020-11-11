/*
   You can modify and use this source freely
   only for the development of application related Live2D.

   (c) Live2D Inc. All rights reserved.
 */
package com.live2d.live2dsimple;

import android.content.Context;
import android.opengl.GLSurfaceView;
import jp.live2d.framework.L2DViewMatrix;
import jp.live2d.utils.android.FileManager;
import jp.live2d.utils.android.OffscreenImage;
import jp.live2d.utils.android.SimpleImage;
import org.jetbrains.annotations.NotNull;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

/*
 * LAppRendererはモデル描画と、そのためのOpenGL命令を集約したクラスです。
 *
 */
public final class LAppRenderer implements GLSurfaceView.Renderer {
    private LAppLive2DManager delegate;
    private SimpleImage bg; // 背景の描画
    private float accelerationX = 0;
    private float accelerationY = 0;

    LAppRenderer(LAppLive2DManager live2DMgr) {
        this.delegate = live2DMgr;
    }

    /*
     * OpenGL画面の作成時に呼ばれるイベント。
     */
    @Override
    public final void onSurfaceCreated(GL10 context, EGLConfig arg1) {
        // 背景の作成
        setupBackground(delegate.getApplicationContext(), context);
    }

    /*
     * OpenGL画面の変更時に呼ばれるイベント。
     * 初期化時とActivity再開時に呼ばれる。
     */
    @Override
    public final void onSurfaceChanged(GL10 gl, int width, int height) {
        delegate.onSurfaceChanged(gl, width, height); // Live2D Event

        // OpenGL 初期化処理
        gl.glViewport(0, 0, width, height);
        gl.glMatrixMode(GL10.GL_PROJECTION);
        gl.glLoadIdentity();

        L2DViewMatrix viewMatrix = delegate.getViewMatrix();
        // glOrthof( Xの左端, Xの右端, Yの下端, Yの上端, Zの手前, Zの奥);
        gl.glOrthof(
                viewMatrix.getScreenLeft(),
                viewMatrix.getScreenRight(),
                viewMatrix.getScreenBottom(),
                viewMatrix.getScreenTop(),
                0.5f, -0.5f);
        gl.glMatrixMode(GL10.GL_MODELVIEW);
        gl.glLoadIdentity();
        gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f); // 背景色
        OffscreenImage.createFrameBuffer(gl, width, height, 0);
    }

    /*
     * 描画イベント。
     */
    @Override
    public final void onDrawFrame(@NotNull GL10 gl) {
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT);

        // ポリゴン等を描画します
        delegate.update(gl);

        // OpenGL 設定
        // 画面への変換行列を適用   将转换矩阵应用于屏幕
        gl.glMatrixMode(GL10.GL_MODELVIEW);
        gl.glLoadIdentity();

        // OpenGLをLive2D用の設定にする
        gl.glDisable(GL10.GL_DEPTH_TEST); // デプステストを行わない
        gl.glDisable(GL10.GL_CULL_FACE); // カリングを行わない
        gl.glEnable(GL10.GL_BLEND); // ブレンドを行う
        gl.glBlendFunc(GL10.GL_ONE, GL10.GL_ONE_MINUS_SRC_ALPHA); // ブレンド方法の指定
        gl.glEnable(GL10.GL_TEXTURE_2D);
        gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);

        // テクスチャのクランプ 指定纹理夹规格
        gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
        gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);

        gl.glColor4f(1, 1, 1, 1);

        // 背景とモデルの描画
        gl.glPushMatrix();
        {
            // 画面の拡大縮小、移動を設定
            L2DViewMatrix viewMatrix = delegate.getViewMatrix();
            gl.glMultMatrixf(viewMatrix.getArray(), 0);

            // 背景の描画
            if (bg != null) {
                gl.glPushMatrix();
                {
                    float SCALE_X = 0.25f; // デバイスの回転による揺れ幅
                    float SCALE_Y = 0.1f;
                    gl.glTranslatef(-SCALE_X * accelerationX, SCALE_Y * accelerationY, 0); // 揺れ
                    bg.draw(gl);
                }
                gl.glPopMatrix();
            }
            // キャラの描画 人物画
            for (int i = 0; i < delegate.getModelNum(); i++) {
                LAppModel model = delegate.getModel(i);
                if (Objects.requireNonNull(model).isInitialized() && !Objects.requireNonNull(model).isUpdating()) {
                    model.update(delegate.getApplicationContext());
                    //模型绘制
                    model.draw(gl);
                }
            }
        }
        gl.glPopMatrix();
    }

    public final void setAcceleration(float x, float y, float z) {
        accelerationX = x;
        accelerationY = y;
    }

    /*
     * 背景の設定
     * @param context
     */
    private void setupBackground(@NotNull Context context, GL10 gl) {
        try {
            InputStream in = FileManager.open(context, LAppDefine.BACK_IMAGE_NAME);
            bg = new SimpleImage(gl, in);
            // 描画範囲。画面の最大表示範囲に合わせる
            bg.setDrawRect(
                    LAppDefine.VIEW_LOGICAL_MAX_LEFT,
                    LAppDefine.VIEW_LOGICAL_MAX_RIGHT,
                    LAppDefine.VIEW_LOGICAL_MAX_BOTTOM,
                    LAppDefine.VIEW_LOGICAL_MAX_TOP);

            // 画像を使用する範囲(uv)
            bg.setUVRect(0.0f, 1.0f, 0.0f, 1.0f);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}