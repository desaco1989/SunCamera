package cn.ymc.suncamera;

import android.hardware.Camera;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import java.io.IOException;

import butterknife.BindView;
import butterknife.OnClick;
import cn.ymc.suncamera.util.CameraUtil;

/**
 * 拍照界面
 *
 * @author ymc
 */

public class CameraActivity extends BaseActivity implements SurfaceHolder.Callback {
    private static final String TAG = "CameraActivity";
    @BindView(R.id.surfaceView)
    SurfaceView svContent;
    @BindView(R.id.img_camera)
    ImageView ivCamera;
    @BindView(R.id.camera_flash)
    ImageView ivFlash;
    @BindView(R.id.camera_switch)
    ImageView ivSwitch;
    @BindView(R.id.iv_back)
    ImageView ivBack;

    private Camera mCamera;
    private SurfaceHolder mHolder;
    private CameraUtil cameraInstance;
    /**
     * 屏幕宽高
     */
    private int screenWidth;
    private int screenHeight;
    /**
     * 是否有界面
     */
    private boolean isView = true;
    /**
     * 拍照id  1： 前摄像头  0：后摄像头
     */
    private int mCameraId = 0;
    /**
     * 图片高度
     */
    private int picHeight;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_camera;
    }

    @Override
    protected void initView() {
        mHolder = svContent.getHolder();
        mHolder.addCallback(this);
    }

    @Override
    protected void initData() {
        cameraInstance = CameraUtil.getInstance();
        DisplayMetrics dm = getResources().getDisplayMetrics();
        screenWidth = dm.widthPixels;
        screenHeight = dm.heightPixels;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mCamera == null) {
            mCamera = getCamera(mCameraId);
            if (mHolder != null) {
                startPreview(mCamera, mHolder);
            }
        }
    }

    @OnClick({R.id.img_camera, R.id.camera_flash, R.id.camera_switch, R.id.iv_back})
    public void OnClick(View view) {
        switch (view.getId()) {
            // 点击拍照
            case R.id.img_camera:

                break;
            // 切换闪光灯
            case R.id.camera_flash:

                break;
            //切换前后摄像头
            case R.id.camera_switch:

                break;
            // 返回按钮
            case R.id.iv_back:
                finish();
                break;
            default:
                break;
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        startPreview(mCamera, holder);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        mCamera.stopPreview();
        startPreview(mCamera, holder);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        releaseCamera();
    }

    /**
     * 释放相机资源
     */
    private void releaseCamera() {
        if (mCamera != null) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    /**
     * 预览相机
     */
    private void startPreview(Camera camera, SurfaceHolder holder) {
        try {
            setupCamera(camera);
            camera.setPreviewDisplay(holder);
            cameraInstance.setCameraDisplayOrientation(this, mCameraId, camera);
            camera.startPreview();
            isView = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 设置surfaceView的尺寸 因为camera默认是横屏，所以取得支持尺寸也都是横屏的尺寸
     * 我们在startPreview方法里面把它矫正了过来，但是这里我们设置设置surfaceView的尺寸的时候要注意 previewSize.height<previewSize.width
     * previewSize.width才是surfaceView的高度
     * 一般相机都是屏幕的宽度 这里设置为屏幕宽度 高度自适应 你也可以设置自己想要的大小
     */
    private void setupCamera(Camera camera) {
        Camera.Parameters parameters = camera.getParameters();
        if (parameters.getSupportedFocusModes().contains(
                Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        }
        //根据屏幕尺寸获取最佳 大小
        Camera.Size previewSize = cameraInstance.getPropSizeForHeight(parameters.getSupportedPreviewSizes(), screenWidth);
        parameters.setPreviewSize(previewSize.width, previewSize.height);

        Camera.Size pictrueSize = cameraInstance.getPropSizeForHeight(parameters.getSupportedPictureSizes(), screenHeight);
        parameters.setPictureSize(pictrueSize.width, pictrueSize.height);
        camera.setParameters(parameters);
//        picHeight = (screenWidth * pictrueSize.width) / pictrueSize.height;
        picHeight = screenHeight;
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(screenWidth,
                (screenWidth * pictrueSize.width) / pictrueSize.height);
        params.gravity = Gravity.CENTER;
        svContent.setLayoutParams(params);
    }

    /**
     * 获取Camera实例
     *
     * @return Camera
     */
    private Camera getCamera(int id) {
        Camera camera = null;
        try {
            camera = Camera.open(id);
        } catch (Exception e) {
            Log.e(TAG, "getCamera: "+e);
        }
        return camera;
    }

}
