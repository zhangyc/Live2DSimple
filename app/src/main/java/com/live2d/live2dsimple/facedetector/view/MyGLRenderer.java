package com.live2d.live2dsimple.facedetector.view;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.Random;

import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.GLU;

import com.live2d.live2dsimple.GLBitmap;

/**
 * @date 2014年10月20日 下午2:49:17
 * @author Zheng Haibo
 * @Description: TODO
 */
public class MyGLRenderer implements Renderer {
	Context context; // Application's context
	Random r = new Random();
	//private Square square;
	private GLBitmap glBitmap;
	private int width = 0;
	private int height = 0;
	private long frameSeq = 0;
	private int viewportOffset = 0;
	private int maxOffset = 400;

	public MyGLRenderer(Context context) {
		this.context = context;
		//square = new Square();
		glBitmap = new GLBitmap();
		ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4 * 2 * 4);
		byteBuffer.order(ByteOrder.nativeOrder());
		vertices = byteBuffer.asFloatBuffer();
		//定义四个点
		vertices.put( new float[] {  -80f,-120f,
				80f,-120f,
				-80f,120f,
				80f,120f});
		ByteBuffer indicesBuffer = ByteBuffer.allocateDirect(6 * 2);
		indicesBuffer.order(ByteOrder.nativeOrder());
		indices = indicesBuffer.asShortBuffer();
		indices.put(new short[] { 0, 1, 2, 1, 2, 3 });
		//indices.flip() == indices.position(0)
		indices.flip();
		vertices.flip();
	}

	FloatBuffer vertices;
	ShortBuffer indices;


	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		if (height == 0) { // Prevent A Divide By Zero By
			height = 1; // Making Height Equal One
		}
		this.width = width;
		this.height = height;
		gl.glViewport(0, 0, width, height); // Reset The
											// Current
		// Viewport
		gl.glMatrixMode(GL10.GL_PROJECTION); // Select The Projection Matrix选择投影矩阵
		gl.glLoadIdentity(); // Reset The Projection Matrix重置投影矩阵

		// Calculate The Aspect Ratio Of The Window  计算窗口的宽高比
		GLU.gluPerspective(gl, 45.0f, (float) width / (float) height, 0.1f,
				100.0f);

		gl.glMatrixMode(GL10.GL_MODELVIEW); // Select The Modelview Matrix选择模型视图矩阵
		gl.glLoadIdentity();
	}

	/**
	 * 每隔16ms调用一次
	 */
	@Override
	public void onDrawFrame(GL10 gl) {
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
		// Reset the Modelview Matrix重置模型视图矩阵
		gl.glLoadIdentity();
		//z的数值越小，画布距离相机的距离越远，图像越小。
		gl.glTranslatef(0.0f, 0.0f, -5.0f); // move 5 units INTO the screen is
		glBitmap.draw(gl);
		// the same as moving the camera 5

		gl.glTranslatef(1.0f, 0.0f, 2f); // move 5 units INTO the screen is
		// square.draw(gl);
		glBitmap.draw(gl);
		//changeGLViewport(gl);
	}

	/**
	 * 通过改变gl的视角获取
	 * 
	 * @param gl
	 */
	private void changeGLViewport(GL10 gl) {
		System.out.println("time=" + System.currentTimeMillis());
		frameSeq++;
		viewportOffset++;
		// The
		// Current
		if (frameSeq % 100 == 0) {// 每隔100帧，重置
			gl.glViewport(0, 0, width, height);
			viewportOffset = 0;
		} else {
			int k = 4;
			gl.glViewport(-maxOffset + viewportOffset * k, -maxOffset
					+ viewportOffset * k, this.width - viewportOffset * 2 * k
					+ maxOffset * 2, this.height - viewportOffset * 2 * k
					+ maxOffset * 2);
		}
	}

	@Override
	public void onSurfaceCreated(GL10 gl,
			javax.microedition.khronos.egl.EGLConfig arg1) {
		glBitmap.loadGLTexture(gl, this.context);

		gl.glEnable(GL10.GL_TEXTURE_2D); // Enable Texture Mapping ( NEW )启用纹理映射（新）
		gl.glShadeModel(GL10.GL_SMOOTH); // Enable Smooth Shading 启用平滑着色
		gl.glClearColor(0.0f, 0.0f, 0.0f, 0.5f); // Black Background黑色背景
		gl.glClearDepthf(1.0f); // Depth Buffer Setup深度缓冲区设置
		gl.glEnable(GL10.GL_DEPTH_TEST); // Enables Depth Testing启用深度测试
		gl.glDepthFunc(GL10.GL_LEQUAL); // The Type Of Depth Testing To Do深度测试的类型

		gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST);
	}
}