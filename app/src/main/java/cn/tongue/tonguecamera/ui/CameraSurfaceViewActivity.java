package cn.tongue.tonguecamera.ui;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.constraint.ConstraintLayout;
import android.util.DisplayMetrics;
import android.util.Size;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import butterknife.BindView;
import butterknife.OnClick;
import cn.tongue.tonguecamera.R;
import cn.tongue.tonguecamera.base.BaseActivity;
import cn.tongue.tonguecamera.util.CameraV2;
import cn.tongue.tonguecamera.view.CameraV2GLSurfaceView;

/**
 * 基于 camera2 surfaceview 过滤界面
 *
 * @author ymc
 * @date 2019年2月12日 14:32:37
 */

public class CameraSurfaceViewActivity extends BaseActivity {
    @BindView(R.id.frame_layout)
    FrameLayout frameLayout;
    private CameraV2 mCamera;
    private CameraV2GLSurfaceView mCameraV2GLSurfaceView;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_camera_sv;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void initView() {
        mCameraV2GLSurfaceView = new CameraV2GLSurfaceView(this);
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        mCamera = new CameraV2(this);
        Size size = mCamera.setUpCameraOutputs(dm.widthPixels, dm.heightPixels);
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

//        mCamera.setGetBitmapListerent(new CameraV2.GetBitmapListerent() {
//            @Override
//            public void onGetBitmap() {
//                Drawable bit = mCameraV2GLSurfaceView.getBackground();
//                BitmapDrawable bitmap = (BitmapDrawable) bit;
//                Bitmap bitm = bitmap.getBitmap();
//                if(bit!=null){
//                    Toast.makeText(CameraSurfaceViewActivity.this,"获取bitmap 成功",Toast.LENGTH_SHORT).show();
//                    try {
//                        File mFile = new File(getExternalFilesDir(null), "pic1.jpg");
//                        FileOutputStream fos = new FileOutputStream(mFile);
//                        bitm.compress(Bitmap.CompressFormat.JPEG, 100, fos);
//                        fos.flush();
//                        fos.close();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }
//
//            }
//        });
    }

    @Override
    protected void initData() {

    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @OnClick({R.id.iv_back, R.id.img_camera})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_back:
                finish();
                break;
            case R.id.img_camera:
                mCamera.lockFocus();
                break;
            default:
                break;
        }
    }



    @Override
    protected void onResume() {
        mCamera.startBackgroundThread();
        super.onResume();
    }

    @Override
    protected void onPause() {
        mCamera.closeCamera();
        mCamera.stopBackgroundThread();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}
