package cn.tongue.tonguecamera.ui;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.yanzhenjie.permission.Action;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.Rationale;
import com.yanzhenjie.permission.RequestExecutor;

import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import cn.tongue.tonguecamera.R;
import cn.tongue.tonguecamera.base.BaseActivity;
import cn.tongue.tonguecamera.bean.Lab;
import cn.tongue.tonguecamera.util.AppConstant;
import cn.tongue.tonguecamera.util.LabUtil;

/**
 * 首页
 *
 * @author ymc
 */

public class MainActivity extends BaseActivity {
    private final static String TAG = "MainActivity";
    @BindView(R.id.btn_camera)
    public Button btn;

    double[][] labmap = {{37.986,13.555,12.059},{65.711,18.13,15.81},{49.927,-4.88,-23.925},
            {43.139,-13.095,19.905} ,{55.112,8.844,-27.399},{70.719,-33.397,-2.199},
            {62.661,36.067,55.096},{40.02,10.41,-47.964},{51.124,48.239,14.248},
            {30.325,22.976,-23.587},{72.532,-23.709,55.255},{71.941,19.363,65.857},
            {28.778,14.179,-52.297},{55.261,-38.342,29.37},{42.101,53.378,26.19},
            {81.733,4.039,77.819} ,{51.935,49.986,-16.574},{51.038,-28.631,-30.638},
            {96.539,-0.425,-1.186},{81.257,-0.638,-2.335},{66.766,-0.734,-2.504},
            {50.867,-0.153,-2.27} ,{35.656,-0.421,-3.231},{20.461,-0.079,-2.973}};

    double [][] rgbmap = {{115,82,68},{194,150,130},{88,123,160},{89,107,67},{128,128,179},
            {92,191,176},{225,125,52},{63,92,174},{197,82,100},{91,57,109},{159,190,70},
            {230,162,45},{30,64,150},{65,148,78},{179,49,60},{239,199,32},{192,84,155},
            {0,136,174},{246,246,249},{199,201,205},{161,163,168},{120,122,125},
            {83,85,89},{47,48,53}};

    static {
        System.loadLibrary("native-lib");
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    protected void initView() {

    }

    @Override
    protected void initData() {
        requestPermission();
        btn.setText(stringFromJNI());

    }

    @OnClick({R.id.btn_camera, R.id.btn_camera2, R.id.btn_filter_camera2})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_camera:
//                Intent intent = new Intent(this, CameraActivity.class);
//                startActivityForResult(intent, 0);
                double[] xyz = LabUtil.sRGB2XYZ(rgbmap[0]);
                double[] lab = LabUtil.XYZ2Lab(xyz);

                double[] xyz2 = LabUtil.Lab2XYZ(labmap[0]);
                double[] rgb = LabUtil.XYZ2sRGB(xyz2);

                Lab testLab = LabUtil.rgbToLabD50(rgbmap[0]);
                double[] testRgb = LabUtil.labToRgbD50(testLab);

                Log.e(TAG, Arrays.toString(lab));
                break;
            case R.id.btn_camera2:
                Intent intent2 = new Intent(this, GoogleCameraActivity.class);
                startActivityForResult(intent2, 0);
                break;
            case R.id.btn_filter_camera2:
                Intent intentFilter2 = new Intent(this, CameraSurfaceViewActivity.class);
                startActivityForResult(intentFilter2, 0);
                break;
            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != AppConstant.RESULT_CODE.RESULT_OK) {
            return;
        }

        if (requestCode == 0) {
            String imgPath = data.getStringExtra(AppConstant.KEY.IMG_PATH);
            int picWidth = data.getIntExtra(AppConstant.KEY.PIC_WIDTH, 0);
            int picHeight = data.getIntExtra(AppConstant.KEY.PIC_HEIGHT, 0);
            Intent intent = new Intent(activity, ShowPicActivity.class);
            intent.putExtra(AppConstant.KEY.PIC_WIDTH, picWidth);
            intent.putExtra(AppConstant.KEY.PIC_HEIGHT, picHeight);
            intent.putExtra(AppConstant.KEY.IMG_PATH, imgPath);
            startActivity(intent);
        }
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();

    /**
     * 动态申请  (电话/位置/存储)
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private void requestPermission() {
        AndPermission.with(this)
                .permission(Manifest.permission.CAMERA,
                        Manifest.permission.READ_EXTERNAL_STORAGE)
                .rationale(new Rationale() {
                    @Override
                    public void showRationale(Context context, List<String> permissions, RequestExecutor executor) {
                        executor.execute();
                    }
                })
                .onGranted(new Action() {
                    @Override
                    public void onAction(List<String> permissions) {
                        Log.e(TAG, "用户给权限");
                    }
                })
                .onDenied(new Action() {
                    @Override
                    public void onAction(List<String> permissions) {
                        if (AndPermission.hasAlwaysDeniedPermission(MainActivity.this, permissions)) {
                            // 打开权限设置页
                            AndPermission.permissionSetting(MainActivity.this).execute();
                            return;
                        }
                        Log.e(TAG, "用户拒绝权限");
                    }
                })
                .start();
    }
}
