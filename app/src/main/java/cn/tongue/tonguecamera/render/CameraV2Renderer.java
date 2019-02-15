package cn.tongue.tonguecamera.render;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.RequiresApi;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import cn.tongue.tonguecamera.util.BitmapRotating;
import cn.tongue.tonguecamera.util.CameraV2;
import cn.tongue.tonguecamera.util.FilterEngine;
import cn.tongue.tonguecamera.util.Utils;
import cn.tongue.tonguecamera.view.CameraV2GLSurfaceView;

import static android.opengl.GLES11Ext.GL_TEXTURE_EXTERNAL_OES;
import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_FRAMEBUFFER;
import static android.opengl.GLES20.GL_TRIANGLES;
import static android.opengl.GLES20.glActiveTexture;
import static android.opengl.GLES20.glBindFramebuffer;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glDrawArrays;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glGenFramebuffers;
import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glUniform1i;
import static android.opengl.GLES20.glUniformMatrix4fv;
import static android.opengl.GLES20.glVertexAttribPointer;
import static android.opengl.GLES20.glViewport;

/**
 * camera render
 * 参考url ： [https://blog.csdn.net/lb377463323/article/details/78054892]
 *
 * @date 2019年2月12日 13:39:55
 * @author ymc
 */

public class CameraV2Renderer implements GLSurfaceView.Renderer {
    private static final String TAG = "CameraV2Renderer";
    private int surfaceWidth;
    private int surfaceHeight;
    private Context mContext;
    private CameraV2GLSurfaceView mCameraV2GLSurfaceView;
    private CameraV2 mCamera;
    private boolean bIsPreviewStarted;
    private int mOESTextureId = -1;
    private SurfaceTexture mSurfaceTexture;
    private float[] transformMatrix = new float[16];
    private FilterEngine mFilterEngine;
    private FloatBuffer mDataBuffer;
    private int mShaderProgram = -1;
    private int aPositionLocation = -1;
    private int aTextureCoordLocation = -1;
    private int uTextureMatrixLocation = -1;
    private int uTextureSamplerLocation = -1;
    private int[] mFBOIds = new int[1];
    private int[] fFrame = new int[1];
    private int[] fTexture = new int[1];

    public void init(CameraV2GLSurfaceView surfaceView, CameraV2 camera,
                     boolean isPreviewStarted, Context context) {
        mContext = context;
        mCameraV2GLSurfaceView = surfaceView;
        mCamera = camera;
        bIsPreviewStarted = isPreviewStarted;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        mOESTextureId = Utils.createOESTextureObject();
        mFilterEngine = new FilterEngine(mOESTextureId, mContext);
        mDataBuffer = mFilterEngine.getBuffer();
        mShaderProgram = mFilterEngine.getShaderProgram();
        glGenFramebuffers(1, mFBOIds, 0);
        glBindFramebuffer(GL_FRAMEBUFFER, mFBOIds[0]);
        Log.i(TAG, "onSurfaceCreated: mFBOId: " + mFBOIds[0]);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        glViewport(0, 0, width, height);
        surfaceWidth = width;
        surfaceHeight = height;
        Log.i(TAG, "onSurfaceChanged: " + width + ", " + height);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onDrawFrame(GL10 gl) {
        Long t1 = System.currentTimeMillis();
        if (mSurfaceTexture != null) {
            mSurfaceTexture.updateTexImage();
            mSurfaceTexture.getTransformMatrix(transformMatrix);
        }

        if (!bIsPreviewStarted) {
            bIsPreviewStarted = initSurfaceTexture();
            bIsPreviewStarted = true;
            return;
        }
        //glClear(GL_COLOR_BUFFER_BIT);
        glClearColor(1.0f, 0.0f, 0.0f, 0.0f);

        aPositionLocation = glGetAttribLocation(mShaderProgram, FilterEngine.POSITION_ATTRIBUTE);
        aTextureCoordLocation = glGetAttribLocation(mShaderProgram, FilterEngine.TEXTURE_COORD_ATTRIBUTE);
        uTextureMatrixLocation = glGetUniformLocation(mShaderProgram, FilterEngine.TEXTURE_MATRIX_UNIFORM);
        uTextureSamplerLocation = glGetUniformLocation(mShaderProgram, FilterEngine.TEXTURE_SAMPLER_UNIFORM);

        glActiveTexture(GL_TEXTURE_EXTERNAL_OES);
        glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, mOESTextureId);
        glUniform1i(uTextureSamplerLocation, 0);
        glUniformMatrix4fv(uTextureMatrixLocation, 1, false, transformMatrix, 0);

        // 根据 标识，如果需要截图，则进行截图操作
        if (mDataBuffer != null) {
            mDataBuffer.position(0);
            glEnableVertexAttribArray(aPositionLocation);
            glVertexAttribPointer(aPositionLocation, 2, GL_FLOAT, false, 16, mDataBuffer);
            mDataBuffer.position(2);
            glEnableVertexAttribArray(aTextureCoordLocation);
            glVertexAttribPointer(aTextureCoordLocation, 2, GL_FLOAT, false, 16, mDataBuffer);
        }

        //glDrawElements(GL_TRIANGLE_FAN, 6,GL_UNSIGNED_INT, 0);
        //glDrawArrays(GL_TRIANGLE_FAN, 0 , 6);
        glDrawArrays(GL_TRIANGLES, 0, 6);
        //glDrawArrays(GL_TRIANGLES, 3, 3);
        glBindFramebuffer(GL_FRAMEBUFFER, 0);

        /**
         * 根据标识 是否截图
         * 参考url： [http://hounychang.github.io/2015/05/13/%E5%AF%B9GLSurfaceView%E6%88%AA%E5%9B%BE/]
         */
        if (CameraV2GLSurfaceView.shouldTakePic) {
            CameraV2GLSurfaceView.shouldTakePic = false;
//            bindfbo();
            int w = surfaceWidth;
            int h = surfaceHeight;
            int b[] = new int[w * h];
            int bt[] = new int[w * h];
            IntBuffer buffer = IntBuffer.wrap(b);
            buffer.position(0);
            GLES20.glReadPixels(0, 0, w, h, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, buffer);
            for (int i = 0; i < h; i++) {
                for (int j = 0; j < w; j++) {
                    int pix = b[i * w + j];
                    int pb = (pix >> 16) & 0xff;
                    int pr = (pix << 16) & 0x00ff0000;
                    int pix1 = (pix & 0xff00ff00) | pr | pb;
                    bt[(h - i - 1) * w + j] = pix1;
                }
            }
            Bitmap inBitmap = null;
            inBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.RGB_565);
            //为了图像能小一点，使用了RGB_565而不是ARGB_8888
            inBitmap.copyPixelsFromBuffer(buffer);
            inBitmap = Bitmap.createBitmap(bt, w, h, Bitmap.Config.RGB_565);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            inBitmap.compress(Bitmap.CompressFormat.JPEG, 90, bos);
            byte[] bitmapData = bos.toByteArray();
            ByteArrayInputStream fis = new ByteArrayInputStream(bitmapData);
            File mFile = new File(mContext.getExternalFilesDir(null), "pic1.jpg");
            try {
                FileOutputStream fos = new FileOutputStream(mFile);
                byte[] buf = new byte[1024];
                int len;
                while ((len = fis.read(buf)) > 0) {
                    fos.write(buf, 0, len);
                }
                fis.close();
                fos.close();

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                //旋转角度
//                int rotate = BitmapRotating.readPictureDegree(mFile.getPath());
//                BitmapRotating.rotaingImageView(rotate,inBitmap);
                inBitmap.recycle();
//                unbindfbo();
            }
        }
        long t2 = System.currentTimeMillis();
        long t = t2 - t1;
        Log.i(TAG, "onDrawFrame: time: " + t);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public boolean initSurfaceTexture() {
        if (mCamera == null || mCameraV2GLSurfaceView == null) {
            Log.i(TAG, "mCamera or mGLSurfaceView is null!");
            return false;
        }
        mSurfaceTexture = new SurfaceTexture(mOESTextureId);
        mSurfaceTexture.setOnFrameAvailableListener(new SurfaceTexture.OnFrameAvailableListener() {
            @Override
            public void onFrameAvailable(SurfaceTexture surfaceTexture) {
                mCameraV2GLSurfaceView.requestRender();
            }
        });
        mCamera.setPreviewTexture(mSurfaceTexture);
        mCamera.createCameraPreviewSession();
        return true;
    }

    /**
     * 改变截图时候界面卡顿的现象
     * 参考url ： [https://blog.csdn.net/SXH_Android/article/details/78835966]
     *
     * 问题：加上后确实没有卡顿现象 但是截图会是 黑屏
     */
    private void bindfbo() {
        GLES20.glGenFramebuffers(1, fFrame, 0);

        GLES20.glGenTextures(1, fTexture, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, fTexture[0]);
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA,
                surfaceWidth, surfaceHeight,0,
                GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);

        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

        GLES20.glBindFramebuffer(GL_FRAMEBUFFER, fFrame[0]);
        GLES20.glFramebufferTexture2D(GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0,GLES20.GL_TEXTURE_2D, fTexture[0], 0);

        int status= GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER);
        if(status !=GLES20.GL_FRAMEBUFFER_COMPLETE) {
            throw new RuntimeException("status:"+status+", hex:"+Integer.toHexString(status));
        }
    }

    private void unbindfbo() {
        GLES20.glBindFramebuffer(GL_FRAMEBUFFER, 0);
        GLES20.glDeleteTextures(1, fTexture, 0);
        GLES20.glDeleteFramebuffers(1, fFrame, 0);
    }
}
