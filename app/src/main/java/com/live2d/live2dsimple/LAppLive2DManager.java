/*
   You can modify and use this source freely
   only for the development of application related Live2D.

   (c) Live2D Inc. All rights reserved.
 */
package com.live2d.live2dsimple;

import android.app.Activity;
import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.Log;
import jp.live2d.Live2D;
import jp.live2d.framework.L2DViewMatrix;
import jp.live2d.framework.Live2DFramework;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import javax.microedition.khronos.opengles.GL10;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/*
 *  LAppLive2DManagerは、Live2D関連の司令塔としてモデル、ビュー、イベント等を管理するクラス（のサンプル実装）になります。
 *
 *  外部（ゲーム等アプリケーション本体）とLive2D関連クラスとの連携をこのクラスでラップして独立性を高めています。
 *
 *  ビュー（LAppView）で発生したイベントは、このクラスのイベント処理用メソッド（tapEvent()等）を呼び出します。
 *  イベント処理用メソッドには、イベント発生時のキャラクターの反応（特定アニメーション開始等）を記述します。
 *
 *  このサンプルで取得しているイベントは、タップ、ダブルタップ、シェイク、ドラッグ、フリック、加速度、キャラ最大化・最小化です。
 * LAppLive2DManager是一个类（示例实现），作为与Live2D相关的命令塔来管理模型，视图，事件等。 * *
 * 该类包装了外部（如游戏的应用程序主体）与Live2D相关类之间的合作，以增强独立性。
 *  * *视图中发生的事件（LAppView）调用此类的事件处理方法（tapEvent（）等）。
 *  *在事件处理方法中，描述事件发生时角色的反应（特定动画的开始等）。
 *  * *在此示例中获得的事件是敲击，双击，摇动，拖动，轻拂，加速和字符最大化/最小化
 */
public final class LAppLive2DManager {
    // ログ用タグ
    private static final String TAG = "SampleLive2DManager";
    // アプリケーションコンテキスト
    private Context applicationContext;
    // モデル表示用View  ///模型显示
    private LAppView view;
    // モデルデータ  ///模型数据
    private ArrayList<LAppModel> models;
    // ボタンから実行できるサンプル機能  可以通过按钮执行的示例功能
    private int modelCount = -1;
    private boolean reloadFlg; // モデル再読み込みのフラグ        模型重载标志

    List<LAppView> views=new ArrayList();  ///把所有创建的view添加到map中进行管理
    ///
    public LAppLive2DManager(@NotNull Context applicationContext) {
        this.applicationContext = applicationContext;
        Live2D.init();
        Live2DFramework.setPlatformManager(new PlatformManager());
        models = new ArrayList<>();
    }

    @Contract(pure = true)
    public final Context getApplicationContext() {
        return applicationContext;
    }

    private void releaseModel() {
        for (LAppModel model : models) {
            model.release(); // テクスチャなどを解放
        }
        models.clear();
    }

    /*
     * モデルの管理状態などの更新。
     *
     * レンダラ（LAppRenderer）のonDrawFrame()からモデル描画の前に毎回呼ばれます。
     * モデルの切り替えなどが必要な場合はここで行なって下さい。
     *
     * モデルのパラメータ（モーション）などの更新はdrawで行って下さい。
     *
     * @param gl
     * *更新，例如模型管理状态。 * *每次在绘制模型之前都从渲染器（LAppRenderer）的onDrawFrame（）调用。
     *  *如果您需要切换型号，请在此处进行。 * *请使用绘图更新模型参数（运动）。
     */
    public final void update(GL10 gl) {
        view.update();
        if (reloadFlg) {
            // モデル切り替えボタンが押された時、モデルを再読み込みする     //按下模型切换按钮时重新加载模型
            reloadFlg = false;

            int no = modelCount % 4;

            try {
                switch (no) {
                    case 0: // ハル
                        releaseModel();

                        models.add(new LAppModel());
                        models.get(0).load(applicationContext, gl, LAppDefine.MODEL_HARU1);
                        models.get(0).feedIn();
                        break;
                    case 1: // しずく
                        releaseModel();

                        models.add(new LAppModel());
                        models.get(0).load(applicationContext, gl, LAppDefine.MODEL_SHIZUKU);
                        models.get(0).feedIn();
                        break;
                    case 2: // わんこ
                        releaseModel();

                        models.add(new LAppModel());
                        models.get(0).load(applicationContext, gl, LAppDefine.MODEL_WANKO);
                        models.get(0).feedIn();
                        break;
                    case 3: // 複数モデル
                        releaseModel();

                        models.add(new LAppModel());
                        models.get(0).load(applicationContext, gl, LAppDefine.MODEL_HARU_A);
                        models.get(0).feedIn();

                        models.add(new LAppModel());
                        models.get(1).load(applicationContext, gl, LAppDefine.MODEL_HARU_B);
                        models.get(1).feedIn();
                        break;
                    default:
                        break;
                }
            } catch (Exception e) {
                // ファイルの指定ミスかメモリ不足が考えられる。復帰か中断が必要          //文件指定不正确或内存不足。需要返回或中断
                Log.e(TAG, "Failed to load." + Arrays.toString(e.getStackTrace()));
                // SampleApplication.exit();
            }
        }
    }

    /*
     * noを指定してモデルを取得
     *           通过指定否获取模型
     * @param no
     * @return
     */
    public final LAppModel getModel(int no) {
        if (no >= models.size())
            return null;
        return models.get(no);
    }

    public final int getModelNum() {
        return models.size();
    }

    //=========================================================
    // 	アプリケーション側（SampleApplication）から呼ばれる処理
    //=========================================================
    /*
     * LAppView(Live2Dを表示するためのView)を生成します。          L生成应用程序视图（用于显示Live 2 D的视图）。
     *
     * @param activity
     * @return view
     * 测试，改为了Context类型
     *
     */
    public final LAppView createView(Activity activity) {
        // Viewの初期化
        view = new LAppView(activity);
        ///为当前的view设置工具类
        view.setLive2DManager(this);
        view.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
        view.startAcceleration(activity);
        ///把创建的view加入一个集合中。因为需要遍历渲染。
        views.add(view);
        return view;
    }


    /*
     * Activityが再開された時のイベント
     */
    public final void onResume() {
        if (LAppDefine.DEBUG_LOG)
            Log.d(TAG, "onResume");
        view.onResume();
    }

    /*
     * Activityがポーズされた時のイベント
     */
    public final void onPause() {
        if (LAppDefine.DEBUG_LOG)
            Log.d(TAG, "onPause");
        view.onPause();
    }

    /*
     * GLSurfaceViewの画面変更時のイベント                   GL Surface View的屏幕更改时发生的事件
     * @param gl
     * @param width
     * @param height
     */
    public final void onSurfaceChanged(GL10 gl, int width, int height) {
        if (LAppDefine.DEBUG_LOG)
            Log.d(TAG, "onSurfaceChanged " + width + " " + height);
        view.setupView(width, height);

        if (getModelNum() == 0)
            changeModel();
    }

    //=========================================================
    // 	アプリケーションからのサンプルイベント
    //=========================================================
    /*
     * モデルの切り替え
     */
    public final void changeModel() {
        reloadFlg = true; // フラグだけ立てて次回update時に切り替え
        modelCount++;
    }


    //=========================================================
    // 	LAppViewから呼ばれる各種イベント        //从LAppView调用的各种事件
    //=========================================================
    /*
     * タップしたときのイベント             单击事件
     * @param x	タップの座標 x
     * @param y	タップの座標 y
     * @return
     */
    public final boolean tapEvent(float x, float y) {
        if (LAppDefine.DEBUG_LOG)
            Log.d(TAG, "tapEvent view x: " + x + " y: " + y);

        for (LAppModel model : models) {
            if (model.hitTest(LAppDefine.HIT_AREA_HEAD, x, y)) {
                // 顔をタップしたら表情切り替え            点按脸部以切换表情
                if (LAppDefine.DEBUG_LOG)
                    Log.d(TAG, "Tap face.");
                model.setRandomExpression();
            } else if (model.hitTest(LAppDefine.HIT_AREA_BODY, x, y)) {
                if (LAppDefine.DEBUG_LOG)
                    Log.d(TAG, "Tap body.");
                model.startRandomMotion(applicationContext, LAppDefine.MOTION_GROUP_TAP_BODY, LAppDefine.PRIORITY_NORMAL);
            }
        }
        return true;
    }


    /*
     * フリックした時のイベント
     *
     * LAppView側でフリックイベントを感知した時に呼ばれ
     * フリック時のモデルの動きを開始します。              轻拂时开始模型的移动
     */
    public final void flickEvent(float x, float y) {
        if (LAppDefine.DEBUG_LOG)
            Log.d(TAG, "flick x: " + x + " y: " + y);

        for (LAppModel model : models) {
            if (model.hitTest(LAppDefine.HIT_AREA_HEAD, x, y)) {
                if (LAppDefine.DEBUG_LOG)
                    Log.d(TAG, "Flick head.");
                model.startRandomMotion(applicationContext, LAppDefine.MOTION_GROUP_FLICK_HEAD, LAppDefine.PRIORITY_NORMAL);
            }
        }
    }

    /*
     * 画面が最大になったときのイベント
     * 屏幕最大化时的事件
     */
    public final void maxScaleEvent() {
        if (LAppDefine.DEBUG_LOG)
            Log.d(TAG, "Max scale event.");

        for (LAppModel model : models) {
            model.startRandomMotion(applicationContext, LAppDefine.MOTION_GROUP_PINCH_IN, LAppDefine.PRIORITY_NORMAL);
        }
    }

    /*
     * 画面が最小になったときのイベント
     */
    public final void minScaleEvent() {
        if (LAppDefine.DEBUG_LOG)
            Log.d(TAG, "Min scale event.");

        for (LAppModel model : models) {
            model.startRandomMotion(applicationContext, LAppDefine.MOTION_GROUP_PINCH_OUT, LAppDefine.PRIORITY_NORMAL);
        }
    }

    /*
     * シェイクイベント
     *
     * LAppView側でシェイクイベントを感知した時に呼ばれ、
     * シェイク時のモデルの動きを開始します。
     * 摇动事件
     */
    public final void shakeEvent() {
        if (LAppDefine.DEBUG_LOG)
            Log.d(TAG, "Shake event.");

        for (LAppModel model : models) {
            model.startRandomMotion(applicationContext, LAppDefine.MOTION_GROUP_SHAKE, LAppDefine.PRIORITY_FORCE);
        }
    }

    public final void setAcceleration(float x, float y, float z) {
        for (LAppModel model : models) {
            model.setAcceleration(x, y, z);
        }
    }

    public final void setDrag(float x, float y) {
        for (LAppModel model : models) {
            model.setDrag(x, y);
        }
    }

    @Contract(pure = true)
    public final L2DViewMatrix getViewMatrix() {
        return view.getViewMatrix();
    }
}