//package com.live2d.live2dsimple.facedetector.view;
//
//import android.app.Activity;
//import android.content.Context;
//import android.opengl.GLSurfaceView;
//import android.util.Log;
//
//import com.google.mlkit.vision.face.Face;
//import com.google.mlkit.vision.face.FaceContour;
//import com.live2d.live2dsimple.facedetector.facedetector.FaceDetectorProcessor;
//
//import java.io.InputStream;
//
//import javax.microedition.khronos.egl.EGLConfig;
//import javax.microedition.khronos.opengles.GL10;
//
//import jp.live2d.android.Live2DModelAndroid;
//import jp.live2d.android.UtOpenGL;
//import jp.live2d.framework.L2DEyeBlink;
//import jp.live2d.framework.L2DStandardID;
//
//class Live2dRenderer implements GLSurfaceView.Renderer {
//    private Context mActivity;
//
//    private Live2DModelAndroid live2DModel;
//    private L2DEyeBlink mEyeBlink;
//    FaceDetectorProcessor faceDetectorProcessor;
//
//    private String MODEL_PATH;
//    private String[] TEXTURE_PATHS;
//
//    private float wRatio, hRatio;
//
//    public void setUpModel(Context activity, String MODEL_PATH, String[] TEXTURE_PATHS,
//                           float wRatio, float hRatio, FaceDetectorProcessor faceDetectorProcessor) {
//        this.mActivity = activity;
//        this.MODEL_PATH = MODEL_PATH;
//        this.TEXTURE_PATHS = TEXTURE_PATHS;
//        this.wRatio = wRatio;
//        this.hRatio = hRatio;
//        this.mEyeBlink = new L2DEyeBlink();
//        this.faceDetectorProcessor=faceDetectorProcessor;
//    }
//    public void setUpModel2(Context activity, String MODEL_PATH, String[] TEXTURE_PATHS,
//                           float wRatio, float hRatio) {
//        this.mActivity = activity;
//        this.MODEL_PATH = MODEL_PATH;
//        this.TEXTURE_PATHS = TEXTURE_PATHS;
//        this.wRatio = wRatio;
//        this.hRatio = hRatio;
//        this.mEyeBlink = new L2DEyeBlink();
//    }
//
//    private void loadLive2dModel(GL10 gl, String modelPath, String[] texturePath) {
//        try {
//            InputStream in = this.mActivity.getAssets().open(modelPath);
//            live2DModel = Live2DModelAndroid.loadModel(in);
//            in.close();
//
//            for (int i = 0; i < texturePath.length; i++) {
//                InputStream tin = this.mActivity.getAssets().open(texturePath[i]);
//                int texNo = UtOpenGL.loadTexture(gl, tin, true);
//                live2DModel.setTexture(i, texNo);
//            }
//        } catch(Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//
//    @Override
//    public void onDrawFrame(GL10 gl)
//    {
//        gl.glMatrixMode(GL10.GL_MODELVIEW);
//        gl.glLoadIdentity();
//        gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
//
//        //live2DModel.loadParam();
//        //boolean update = mMotionManager.updateParam(live2DModel);
//        mEyeBlink.updateParam(live2DModel);
//        //live2DModel.saveParam();
//        ///通过activity的值来更新\
////        if (faceDetectorProcessor!=null&&faceDetectorProcessor.getFace()!=null){
////            Face face = faceDetectorProcessor.getFace();
////            if (face==null){
////                return;
////            }
////            FaceContour upperLipBottom = faceDetectorProcessor.getFace().getContour(FaceContour.UPPER_LIP_BOTTOM);
////            FaceContour lowerLipTop = faceDetectorProcessor.getFace().getContour(FaceContour.LOWER_LIP_TOP);
////            FaceContour rightEye = faceDetectorProcessor.getFace().getContour(FaceContour.RIGHT_EYE);
////            FaceContour leftEye = faceDetectorProcessor.getFace().getContour(FaceContour.LEFT_EYE);
////            if (lowerLipTop!=null&&upperLipBottom!=null){
////                float mouthHeght=lowerLipTop.getPoints().get(7).y-upperLipBottom.getPoints().get(7).y;
////                live2DModel.setParamFloat(L2DStandardID.PARAM_MOUTH_OPEN_Y, mouthHeght/10f, 0.75f);
////                live2DModel.setParamFloat(L2DStandardID.PARAM_MOUTH_FORM, mouthHeght/10f, 0.75f);
////            }
//////            if (rightEye!=null){
//////                float rightHeight=rightEye.getPoints().get(12).y-rightEye.getPoints().get(5).y;
//////                live2DModel.setParamFloat(L2DStandardID.PARAM_EYE_R_OPEN, rightHeight/20f, 0.75f);
//////
//////
//////            }
//////            if (leftEye!=null){
//////                float leftHeght=leftEye.getPoints().get(12).y-leftEye.getPoints().get(5).y;
//////                live2DModel.setParamFloat(L2DStandardID.PARAM_EYE_L_OPEN, leftHeght/20f, 0.75f);
//////            }
////            if (face.getRightEyeOpenProbability()!=null&&face.getLeftEyeOpenProbability()!=null){
////                if (face.getRightEyeOpenProbability()>0.98f){
////                    live2DModel.setParamFloat(L2DStandardID.PARAM_EYE_R_OPEN,1.0f, 0.75f);
////                }else if (face.getRightEyeOpenProbability()<0.1f){
////                    live2DModel.setParamFloat(L2DStandardID.PARAM_EYE_R_OPEN,0.0f, 0.75f);
////                }
////                if (face.getLeftEyeOpenProbability()>0.98f){
////                    live2DModel.setParamFloat(L2DStandardID.PARAM_EYE_L_OPEN,1.0f, 0.75f);
////                }else if (face.getRightEyeOpenProbability()<0.1f){
////                    live2DModel.setParamFloat(L2DStandardID.PARAM_EYE_L_OPEN,0.0f, 0.75f);
////                }
////            }
////
////            live2DModel.setParamFloat(L2DStandardID.PARAM_ANGLE_Z, faceDetectorProcessor.getFace().getHeadEulerAngleZ(), 0.75f);
////            live2DModel.setParamFloat(L2DStandardID.PARAM_ANGLE_X , faceDetectorProcessor.getFace().getHeadEulerAngleY(), 0.75f);
////            live2DModel.setParamFloat(L2DStandardID.PARAM_ANGLE_Y , faceDetectorProcessor.getFace().getHeadEulerAngleX(), 0.75f);
////
////
////        }
//
//
//
//        live2DModel.setGL(gl);
//        live2DModel.update();
//        live2DModel.draw();
//    }
//
//
//    @Override
//    public void onSurfaceChanged(GL10 gl, int width, int height)
//    {
//        gl.glViewport(0 , 0 , width , height);
//
//        gl.glMatrixMode(GL10.GL_PROJECTION);
//        gl.glLoadIdentity();
//
//        float modelWidth = live2DModel.getCanvasWidth();
//        float aspect = (float)width/height;
//
//        gl.glOrthof(0, wRatio*modelWidth, hRatio*modelWidth / aspect, 0, 0.5f, -0.5f);
//    }
//
//
//    @Override
//    public void onSurfaceCreated(GL10 gl, EGLConfig config)
//    {
//        Log.d("Render", "onSurfaceCreated");
//        loadLive2dModel(gl, MODEL_PATH, TEXTURE_PATHS);
//    }
//}