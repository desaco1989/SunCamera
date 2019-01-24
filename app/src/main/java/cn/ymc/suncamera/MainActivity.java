package cn.ymc.suncamera;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.yanzhenjie.permission.Action;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.Rationale;
import com.yanzhenjie.permission.RequestExecutor;

import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * 首页
 *
 * @author ymc
 */

public class MainActivity extends BaseActivity {
    private final static String TAG = "MainActivity";
    @BindView(R.id.btn_camera)
    public Button btn;

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

    @OnClick({R.id.btn_camera})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_camera:
                Intent intent = new Intent(this, CameraActivity.class);
                startActivityForResult(intent, 0);
                break;
            default:
                break;
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
