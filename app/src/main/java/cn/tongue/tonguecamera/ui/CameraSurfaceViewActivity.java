package cn.tongue.tonguecamera.ui;

import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.constraint.ConstraintLayout;
import android.util.DisplayMetrics;
import android.util.Size;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import butterknife.BindView;
import cn.tongue.tonguecamera.R;
import cn.tongue.tonguecamera.base.BaseActivity;
import cn.tongue.tonguecamera.util.CameraV2;
import cn.tongue.tonguecamera.view.CameraV2GLSurfaceView;

/**
 * 基于 camera2 surfaceview 过滤界面
 *
 * @date 2019年2月12日 14:32:37
 * @author ymc
 */

public class CameraSurfaceViewActivity extends BaseActivity {
    @BindView(R.id.frame_layout)
    FrameLayout frameLayout;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_camera_sv;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void initView() {
        CameraV2GLSurfaceView mCameraV2GLSurfaceView = new CameraV2GLSurfaceView(this);
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        CameraV2 mCamera = new CameraV2(this);
        Size size = mCamera.setupCamera(dm.widthPixels, dm.heightPixels);
        if (!mCamera.openCamera()) {
            return;
        }
        mCameraV2GLSurfaceView.init(mCamera, false,
                CameraSurfaceViewActivity.this);
        ConstraintLayout.LayoutParams lp = new ConstraintLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.width = size.getHeight();
        lp.height = size.getWidth();
        mCameraV2GLSurfaceView.setLayoutParams(lp);
        frameLayout.addView(mCameraV2GLSurfaceView);
    }

    @Override
    protected void initData() {

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}
