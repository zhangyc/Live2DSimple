/*
   You can modify and use this source freely
   only for the development of application related Live2D.

   (c) Live2D Inc. All rights reserved.
 */
package com.live2d.live2dsimple;

import android.content.Context;
import android.util.Log;
import jp.live2d.android.Live2DModelAndroid;
import jp.live2d.framework.L2DBaseModel;
import jp.live2d.framework.L2DEyeBlink;
import jp.live2d.framework.L2DStandardID;
import jp.live2d.framework.Live2DFramework;
import jp.live2d.motion.AMotion;
import jp.live2d.util.UtSystem;
import jp.live2d.utils.android.*;
import org.jetbrains.annotations.NotNull;

import javax.microedition.khronos.opengles.GL10;
import java.io.IOException;
import java.io.InputStream;
import java.nio.FloatBuffer;
import java.util.HashMap;

/*
 * LAppModel は低レベルのLive2Dモデル定義クラス Live2DModelAndroid をラップし       * LAppModel包装了底层Live2D模型定义类Live2DModelAndroid
 * 簡便に扱うためのユーティリティクラスです。
 *
 *
 * 機能一覧
 *  アイドリングモーション             空转运动
 *  表情
 *  音声
 *  物理演算によるアニメーション                物理计算动画
 *  モーションが無いときに自動で目パチ                    没有运动时自动眨眼
 *  パーツ切り替えによるポーズの変更                  通过切换零件来改变姿势
 *  当たり判定                   碰撞检测
 *  呼吸のアニメーション               呼吸动画
 *  ドラッグによるアニメーション              拖动动画
 *  デバイスの傾きによるアニメーション                    通过倾斜设备进行动画
 *
 */
public final class LAppModel extends L2DBaseModel {
    //  ログ用タグ
    private String TAG = "LAppModel";
    //  デバッグ用の当たり判定表示のためのバッファ
    private FloatBuffer debugBufferVer = null;
    private FloatBuffer debugBufferColor = null;
    //  モデル関連
    private ModelSetting modelSetting = null;    // モデルファイルやモーションの定義
    private String modelHomeDir;            // モデルデータのあるディレクトリ

    LAppModel() {
        super();
        if (LAppDefine.DEBUG_LOG) {
            debugMode = true;
        }
    }

    public final void release() {
        if (live2DModel == null)
            return;
        live2DModel.deleteTextures();
    }

    /*
     * モデルを初期化する
     * @param gl
     * @throws Exception
     */
    public final void load(@NotNull Context applicationContext, GL10 gl, @NotNull String modelSettingPath) {
        updating = true;
        initialized = false;

        modelHomeDir = modelSettingPath.substring(0, modelSettingPath.lastIndexOf("/") + 1); //live2d/model/xxx/
        PlatformManager pm = (PlatformManager) Live2DFramework.getPlatformManager();
        pm.setGL(gl);

        if (LAppDefine.DEBUG_LOG)
            Log.d(TAG, "json: " + modelSettingPath);

        try {
            InputStream in = FileManager.open(applicationContext, modelSettingPath);
            modelSetting = new ModelSettingJson(in);
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (modelSetting.getModelName() != null) {
            TAG += "LAppModel " + modelSetting.getModelName(); // ログ用
        }

        if (LAppDefine.DEBUG_LOG)
            Log.d(TAG, "Load model.");

        loadModelData(applicationContext, modelHomeDir + modelSetting.getModelFile());
        String[] texPaths = modelSetting.getTextureFiles();
        for (int i = 0; i < texPaths.length; i++) {
            loadTexture(applicationContext, i, modelHomeDir + texPaths[i]);
        }
        // 表情
        String[] expressionNames = modelSetting.getExpressionNames();
        String[] expressionPaths = modelSetting.getExpressionFiles();
        for (int i = 0; i < expressionPaths.length; i++) {
            loadExpression(applicationContext, expressionNames[i], modelHomeDir + expressionPaths[i]);
        }
        // 物理演算
        loadPhysics(applicationContext, modelHomeDir + modelSetting.getPhysicsFile());
        // パーツ切り替え
        loadPose(applicationContext, modelHomeDir + modelSetting.getPoseFile());
        // レイアウト
        HashMap<String, Float> layout = new HashMap<>();
        if (modelSetting.getLayout(layout)) {
            if (layout.get("width") != null)
                modelMatrix.setWidth(layout.get("width"));
            if (layout.get("height") != null)
                modelMatrix.setHeight(layout.get("height"));
            if (layout.get("x") != null)
                modelMatrix.setX(layout.get("x"));
            if (layout.get("y") != null)
                modelMatrix.setY(layout.get("y"));
            if (layout.get("center_x") != null)
                modelMatrix.centerX(layout.get("center_x"));
            if (layout.get("center_y") != null)
                modelMatrix.centerY(layout.get("center_y"));
            if (layout.get("top") != null)
                modelMatrix.top(layout.get("top"));
            if (layout.get("bottom") != null)
                modelMatrix.bottom(layout.get("bottom"));
            if (layout.get("left") != null)
                modelMatrix.left(layout.get("left"));
            if (layout.get("right") != null)
                modelMatrix.right(layout.get("right"));
        }

        // Sound
        String[] soundPaths = modelSetting.getSoundPaths();
        for (String path : soundPaths) {
            ///这个需要处理
            SoundManager.load(applicationContext, modelHomeDir + path);
        }

        // 初期パラメータ
        for (int i = 0; i < modelSetting.getInitParamNum(); i++) {
            String id = modelSetting.getInitParamID(i);
            float value = modelSetting.getInitParamValue(i);
            live2DModel.setParamFloat(id, value);
        }

        for (int i = 0; i < modelSetting.getInitPartsVisibleNum(); i++) {
            String id = modelSetting.getInitPartsVisibleID(i);
            float value = modelSetting.getInitPartsVisibleValue(i);
            live2DModel.setPartsOpacity(id, value);
        }

        // 自動目パチ
        eyeBlink = new L2DEyeBlink();

        updating = false;// 更新状態の完了
        initialized = true;// 初期化完了
    }
    //预加载运动组
    public final void preloadMotionGroup(@NotNull Context applicationContext, String name) {
        int len = modelSetting.getMotionNum(name);
        for (int i = 0; i < len; i++) {
            String fileName = modelSetting.getMotionFile(name, i);
            AMotion motion = loadMotion(applicationContext, fileName, modelHomeDir + fileName);
            motion.setFadeIn(modelSetting.getMotionFadeIn(name, i));
            motion.setFadeOut(modelSetting.getMotionFadeOut(name, i));
        }
    }

    public final void update(@NotNull Context applicationContext) {
        if (live2DModel == null) {
            if (LAppDefine.DEBUG_LOG)
                Log.d(TAG, "Failed to update.");
            return;
        }

        long timeMSec = UtSystem.getUserTimeMSec() - startTimeMSec;
        double timeSec = timeMSec / 1000.0;
        double t = timeSec * 2 * Math.PI;

        // 待機モーション判定        待机动作判断
        if (mainMotionManager.isFinished()) {
            // モーションの再生がない場合、待機モーションの中からランダムで再生する
            startRandomMotion(applicationContext, LAppDefine.MOTION_GROUP_IDLE, LAppDefine.PRIORITY_IDLE);
        }
        live2DModel.loadParam(); // 前回セーブされた状態をロード         加载先前保存的状态
        boolean update = mainMotionManager.updateParam(live2DModel); // モーションを更新       更新动作
        if (!update) {
            // メインモーションの更新がないとき     没有主要动作更新时
            eyeBlink.updateParam(live2DModel); // 目パチ 眨眼
        }
        live2DModel.saveParam();// 状態を保存

        if (expressionManager != null)
            expressionManager.updateParam(live2DModel); // 表情でパラメータ更新（相対変化）

        // ドラッグによる変化 因拖曳而改变
        // ドラッグによる顔の向きの調整 通过拖动来调整脸部方向
        live2DModel.addToParamFloat(L2DStandardID.PARAM_ANGLE_X, dragX * 30, 1); // -30から30の値を加える
        live2DModel.addToParamFloat(L2DStandardID.PARAM_ANGLE_Y, dragY * 30, 1);
        live2DModel.addToParamFloat(L2DStandardID.PARAM_ANGLE_Z, (dragX * dragY) * -30, 1);

        // ドラッグによる体の向きの調整
        live2DModel.addToParamFloat(L2DStandardID.PARAM_BODY_ANGLE_X, dragX * 10, 1); // -10から10の値を加える

        // ドラッグによる目の向きの調整
        live2DModel.addToParamFloat(L2DStandardID.PARAM_EYE_BALL_X, dragX, 1); // -1から1の値を加える
        live2DModel.addToParamFloat(L2DStandardID.PARAM_EYE_BALL_Y, dragY, 1);  //通过拖动来调整眼睛方向

        // 呼吸など
        live2DModel.addToParamFloat(L2DStandardID.PARAM_ANGLE_X, (float) (15 * Math.sin(t / 6.5345)), 0.5f);
        live2DModel.addToParamFloat(L2DStandardID.PARAM_ANGLE_Y, (float) (8 * Math.sin(t / 3.5345)), 0.5f);
        live2DModel.addToParamFloat(L2DStandardID.PARAM_ANGLE_Z, (float) (10 * Math.sin(t / 5.5345)), 0.5f);
        live2DModel.addToParamFloat(L2DStandardID.PARAM_BODY_ANGLE_X, (float) (4 * Math.sin(t / 15.5345)), 0.5f);
        live2DModel.setParamFloat(L2DStandardID.PARAM_BREATH, (float) (0.5f + 0.5f * Math.sin(t / 3.2345)), 1);

        // 加速度による変化
        live2DModel.addToParamFloat(L2DStandardID.PARAM_ANGLE_Z, 90 * accelerationX, 0.5f);

        if (physics != null)
            physics.updateParam(live2DModel); // 物理演算でパラメータ更新

        // リップシンクの設定  嘴唇同步设置
        if (lipSync) {
            live2DModel.setParamFloat(L2DStandardID.PARAM_MOUTH_OPEN_Y, lipSyncValue, 0.8f);
        }

        // ポーズの設定
        if (pose != null)
            pose.updateParam(live2DModel);

        live2DModel.update();
    }

    /*
     * デバッグ用当たり判定の表示
     * @param gl
     */
    private void drawHitArea(@NotNull GL10 gl) {
        gl.glDisable(GL10.GL_TEXTURE_2D);
        gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glEnableClientState(GL10.GL_COLOR_ARRAY);
        gl.glPushMatrix();
        {
            gl.glMultMatrixf(modelMatrix.getArray(), 0);
            int len = modelSetting.getHitAreasNum();
            for (int i = 0; i < len; i++) {
                String drawID = modelSetting.getHitAreaID(i);
                int drawIndex = live2DModel.getDrawDataIndex(drawID);
                if (drawIndex < 0)
                    continue;
                float[] points = live2DModel.getTransformedPoints(drawIndex);
                float left = live2DModel.getCanvasWidth();
                float right = 0;
                float top = live2DModel.getCanvasHeight();
                float bottom = 0;

                for (int j = 0; j < points.length; j += 2) {
                    float x = points[j];
                    float y = points[j + 1];
                    if (x < left)
                        left = x; // 最小のx
                    if (x > right)
                        right = x; // 最大のx
                    if (y < top)
                        top = y; // 最小のy
                    if (y > bottom)
                        bottom = y; // 最大のy
                }

                float[] vertex = {left, top, right, top, right, bottom, left, bottom, left, top};
                float r = 1;
                float g = 0;
                float b = 0;
                float a = 0.5f;
                int size = 5;
                float color[] = {r, g, b, a, r, g, b, a, r, g, b, a, r, g, b, a, r, g, b, a};

                gl.glLineWidth(size); // 描画サイズをsizeにする
                gl.glVertexPointer(2, GL10.GL_FLOAT, 0, BufferUtil.setupFloatBuffer(debugBufferVer, vertex)); // 表示座標のセット
                gl.glColorPointer(4, GL10.GL_FLOAT, 0, BufferUtil.setupFloatBuffer(debugBufferColor, color)); // カラーのセット
                gl.glDrawArrays(GL10.GL_LINE_STRIP, 0, 5); // pointNumだけ描画する
            }
        }
        gl.glPopMatrix();
        gl.glEnable(GL10.GL_TEXTURE_2D);
        gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glDisableClientState(GL10.GL_COLOR_ARRAY);
    }

    public final void startRandomMotion(@NotNull Context applicationContext, String name, int priority) {
        int max = modelSetting.getMotionNum(name);
        int no = (int) (Math.random() * max);
        startMotion(applicationContext, name, no, priority);
    }

    /*
     * モーションの開始。运动开始。
     * 再生できる状態かチェックして、できなければ何もしない。检查它是否可以播放，否则，什么也不做。
     * 再生出来る場合は自動でファイルを読み込んで再生。如果可以播放，将自动读取并播放文件。
     * 音声付きならそれも再生。 播放音频
     * フェードイン、フェードアウトの情報があればここで設定。なければ初期値。如果您具有淡入和淡出信息，请在此处进行设置。如果不是，则为初始值。
     */
    private void startMotion(@NotNull Context applicationContext, String name, int no, int priority) {
        ///根据参数获取动作名称
        String motionName = modelSetting.getMotionFile(name, no);
        //为空，
        if (motionName == null || motionName.equals("")) {
            if (LAppDefine.DEBUG_LOG)
                Log.d(TAG, "Failed to motion.");
            return;
        }

        AMotion motion;

        // 新しいモーションのpriorityと、再生中のモーション、予約済みモーションのpriorityと比較して    将新动作的优先级与正在播放的动作的优先级和保留的动作进行比较
        // 予約可能であれば（優先度が高ければ）再生を予約します。           如果可以预约（优先级更高），请预约播放。
        //
        // 予約した新モーションは、このフレームで即時再生されるか、もしくは音声のロード等が必要な場合は  保留的新动作将立即在此帧中播放，或者如果您需要加载音频等。
        // 以降のフレームで再生開始されます。                  播放将从随后的帧开始
        if (priority == LAppDefine.PRIORITY_FORCE) {
            mainMotionManager.setReservePriority(priority);
        } else if (!mainMotionManager.reserveMotion(priority)) {
            if (LAppDefine.DEBUG_LOG)
                Log.d(TAG, "Failed to motion.");
            return;
        }

        String motionPath = modelHomeDir + motionName;
        ///动作载入
        motion = loadMotion(applicationContext, null, motionPath);

        if (motion == null) {
            Log.w(TAG, "Failed to load motion.");
            mainMotionManager.setReservePriority(0);
            return;
        }

        // フェードイン、フェードアウトの設定  淡入和淡出设置
        motion.setFadeIn(modelSetting.getMotionFadeIn(name, no));
        motion.setFadeOut(modelSetting.getMotionFadeOut(name, no));

        if (LAppDefine.DEBUG_LOG)
            Log.d(TAG, "Start motion: " + motionName);

        // 音声が無いモーションは即時再生を開始します。没有声音的动作将立即开始播放。
        if (modelSetting.getMotionSound(name, no) == null) {
            mainMotionManager.startMotionPrio(motion, priority);
        } else { // 音声があるモーションは音声のロードを待って次のフレーム以降に再生を開始します。带有音频的运动会等待音频加载并在下一帧之后开始播放。
            String soundName = modelSetting.getMotionSound(name, no);
            String soundPath = modelHomeDir + soundName;

            if (LAppDefine.DEBUG_LOG)
                Log.d(TAG, "sound : " + soundName);

            SoundManager.play(soundPath);
            mainMotionManager.startMotionPrio(motion, priority);
        }
    }

    /*
     * 表情を設定する      //表情设置
     * @param motion
     */
    public final void setExpression(String name) {
        if (!expressions.containsKey(name))
            return; // 無効な指定ならなにもしない
        if (LAppDefine.DEBUG_LOG)
            Log.d(TAG, "Expression: " + name);
        AMotion motion = expressions.get(name);
        expressionManager.startMotion(motion, false);
    }
    /*
     * 左眼打开
     */
    public final void openLeftEye() {

    }
    /*
     * 右眼打开
     */
    public final void openRightEye() {
    }
    /*
     * 表情をランダムに切り替える      随机表情
     */
    public final void setRandomExpression() {
        int no = (int) (Math.random() * expressions.size());
        String[] keys = expressions.keySet().toArray(new String[expressions.size()]);
        setExpression(keys[no]);
    }

    public final void draw(GL10 gl) {
        ((Live2DModelAndroid) live2DModel).setGL(gl); // OpenGLのコンテキストをLive2Dモデルに設定  将Open GL上下文设置为Live 2D模型

        alpha += accAlpha;

        if (alpha < 0) {
            alpha = 0;
            accAlpha = 0;
        } else if (alpha > 1) {
            alpha = 1;
            accAlpha = 0;
        }

        if (alpha < 0.001)
            return;

        if (alpha < 0.999) {
            // 半透明
            // オフスクリーンにモデルを描画
            OffscreenImage.setOffscreen(gl);
            gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
            gl.glPushMatrix();
            {
                gl.glMultMatrixf(modelMatrix.getArray(), 0);
                live2DModel.draw();
            }
            gl.glPopMatrix();

            // 実際のウィンドウに半透明で描画
            OffscreenImage.setOnscreen(gl);
            gl.glPushMatrix();
            {
                gl.glLoadIdentity();
                OffscreenImage.drawDisplay(gl, alpha);
            }
            gl.glPopMatrix();
        } else {
            // 通常表示
            gl.glPushMatrix();
            {
                gl.glMultMatrixf(modelMatrix.getArray(), 0);
                live2DModel.draw();
            }
            gl.glPopMatrix();

            if (LAppDefine.DEBUG_DRAW_HIT_AREA) {
                // デバッグ用当たり判定の描画
                drawHitArea(gl);
            }
        }
    }

    /*
     * 当たり判定との簡易テスト。
     * 指定IDの頂点リストからそれらを含む最大の矩形を計算し、点がそこに含まれるか判定
     *
     * @param id
     * @param testX
     * @param testY
     * @return
     */
    public final boolean hitTest(String id, float testX, float testY) {
        if (alpha < 1)
            return false; // 透明時は当たり判定なし。
        if (modelSetting == null)
            return false;
        int len = modelSetting.getHitAreasNum();
        for (int i = 0; i < len; i++) {
            if (id.equals(modelSetting.getHitAreaName(i))) {
                return hitTestSimple(modelSetting.getHitAreaID(i), testX, testY);
            }
        }
        return false; // 存在しない場合はfalse
    }

    public final void feedIn() {
        alpha = 0;
        accAlpha = 0.1f;
    }
}