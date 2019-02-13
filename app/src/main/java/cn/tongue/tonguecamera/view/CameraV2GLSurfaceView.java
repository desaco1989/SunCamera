package cn.tongue.tonguecamera.view;

import android.content.Context;
import android.opengl.GLSurfaceView;

import cn.tongue.tonguecamera.render.CameraV2Renderer;
import cn.tongue.tonguecamera.util.CameraV2;

/**
 * CameraV2 GLSurfaceView
 * @date 2019年2月12日 13:41:16
 * @author ymc
 */

public class CameraV2GLSurfaceView extends GLSurfaceView {
    public static final String TAG = "CameraV2GLSurfaceView";

    public void init(CameraV2 camera, boolean isPreviewStarted, Context context) {
        setEGLContextClientVersion(2);
        CameraV2Renderer mCameraV2Renderer = new CameraV2Renderer();
        mCameraV2Renderer.init(this, camera, isPreviewStarted, context);
        setRenderer(mCameraV2Renderer);
    }

    public CameraV2GLSurfaceView(Context context) {
        super(context);
    }
}