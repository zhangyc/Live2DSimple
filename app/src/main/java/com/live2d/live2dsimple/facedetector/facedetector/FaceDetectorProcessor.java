/*
 * Copyright 2020 Google LLC. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.live2d.live2dsimple.facedetector.facedetector;

import android.content.Context;
import android.graphics.PointF;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;
import com.google.mlkit.vision.face.FaceLandmark;
import com.live2d.live2dsimple.LAppLive2DManager;
import com.live2d.live2dsimple.facedetector.VisionProcessorBase;
import com.live2d.live2dsimple.utils.GraphicOverlay;

import java.util.List;
import java.util.Locale;

/** Face Detector Demo. */
public class FaceDetectorProcessor extends VisionProcessorBase<List<Face>> {

  private static final String TAG = "FaceDetectorProcessor";

  private final FaceDetector detector;
  private Context context;

  public FaceDetectorProcessor(Context context) {
    this(
        context,
        new FaceDetectorOptions.Builder()
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
            .enableTracking()
            .build(),null);
  }
  private LAppLive2DManager live2DMgr;

  public FaceDetectorProcessor(Context context, FaceDetectorOptions options,LAppLive2DManager lAppLive2DManager) {
    super(context);
    this.live2DMgr=lAppLive2DManager;
    Log.v(MANUAL_TESTING_LOG, "Face detector options: " + options);
    detector = FaceDetection.getClient(options);
    this.context=context;
  }
  public FaceDetectorProcessor(Context context, FaceDetectorOptions options) {
    super(context);
    Log.v(MANUAL_TESTING_LOG, "Face detector options: " + options);
    detector = FaceDetection.getClient(options);
    this.context=context;
  }



  @Override
  public void stop() {
    super.stop();
    detector.close();
  }

  @Override
  protected Task<List<Face>> detectInImage(InputImage image) {
    return detector.process(image);
  }
  private Face face;

  public Face getFace() {
    return face;
  }

  public void setFace(Face face) {
    this.face = face;
  }

  @Override
  protected void onSuccess(@NonNull List<Face> faces, @NonNull GraphicOverlay graphicOverlay) {
    for (Face face : faces) {
      setFace(face);
      graphicOverlay.add(new FaceGraphic(graphicOverlay, face));
      logExtrasForTesting(face);
      //updateLive2dView(face);
    }
  }
  private static void logExtrasForTesting(Face face) {
    if (face != null) {
      Log.v(MANUAL_TESTING_LOG, "face bounding box: " + face.getBoundingBox().flattenToString());
      Log.v(MANUAL_TESTING_LOG, "face Euler Angle X: " + face.getHeadEulerAngleX());  ///欧拉x  低头为负，抬头为正
//      Log.v("人头角度", "face Euler Angle Y: " + face.getHeadEulerAngleY());
//      Log.v("人头角度", "face Euler Angle Z: " + face.getHeadEulerAngleZ());
      Log.v("人头角度", "face Euler Angle X: " + face.getHeadEulerAngleZ()+"XXXX===");
      Log.v(MANUAL_TESTING_LOG, "face Euler Angle Y: " + face.getHeadEulerAngleY());//欧拉Y 左转为正，右转为负
      Log.v(MANUAL_TESTING_LOG, "face Euler Angle Z: " + face.getHeadEulerAngleX()); //

      // All landmarks
      int[] landMarkTypes =
          new int[] {
            FaceLandmark.MOUTH_BOTTOM, //嘴下
            FaceLandmark.MOUTH_RIGHT,  //嘴右
            FaceLandmark.MOUTH_LEFT,  //嘴左
            FaceLandmark.RIGHT_EYE,  //右眼
            FaceLandmark.LEFT_EYE,  //左眼
            FaceLandmark.RIGHT_EAR,   //右耳
            FaceLandmark.LEFT_EAR,   //左耳
            FaceLandmark.RIGHT_CHEEK,  //右脸颊
            FaceLandmark.LEFT_CHEEK,  // 左脸颊
            FaceLandmark.NOSE_BASE   //鼻子
          };
      String[] landMarkTypesStrings =
          new String[] {
            "MOUTH_BOTTOM",
            "MOUTH_RIGHT",
            "MOUTH_LEFT",
            "RIGHT_EYE",
            "LEFT_EYE",
            "RIGHT_EAR",
            "LEFT_EAR",
            "RIGHT_CHEEK",
            "LEFT_CHEEK",
            "NOSE_BASE"
          };
      for (int i = 0; i < landMarkTypes.length; i++) {
        FaceLandmark landmark = face.getLandmark(landMarkTypes[i]);
        if (landmark == null) {
          Log.v(
              MANUAL_TESTING_LOG,
              "No landmark of type: " + landMarkTypesStrings[i] + " has been detected");
        } else {
          PointF landmarkPosition = landmark.getPosition();
          String landmarkPositionStr =
              String.format(Locale.US, "x: %f , y: %f", landmarkPosition.x, landmarkPosition.y);
          Log.v(
              MANUAL_TESTING_LOG,
              "Position for face landmark: "
                  + landMarkTypesStrings[i]
                  + " is :"
                  + landmarkPositionStr);
        }
      }
      Log.v(
          MANUAL_TESTING_LOG,
          "face left eye open probability: " + face.getLeftEyeOpenProbability());
      Log.v(
          MANUAL_TESTING_LOG,
          "face right eye open probability: " + face.getRightEyeOpenProbability());
      Log.v(MANUAL_TESTING_LOG, "face smiling probability: " + face.getSmilingProbability());
      Log.v(MANUAL_TESTING_LOG, "face tracking id: " + face.getTrackingId());
    }
  }

  @Override
  protected void onFailure(@NonNull Exception e) {
    Log.e(TAG, "Face detection failed " + e);
  }
}
