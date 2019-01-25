package cn.tongue.tonguecamera;

import android.net.Uri;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import butterknife.BindView;
import cn.tongue.tonguecamera.util.AppConstant;

public class ShowPicActivity extends BaseActivity {
    @BindView(R.id.img)
    ImageView iv;

    private int picWidth;
    private int picHeight;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_show_pic;
    }

    @Override
    protected void initView() {

    }

    @Override
    protected void initData() {
        picWidth = getIntent().getIntExtra(AppConstant.KEY.PIC_WIDTH, 0);
        picHeight = getIntent().getIntExtra(AppConstant.KEY.PIC_HEIGHT, 0);
        iv.setImageURI(Uri.parse(getIntent().getStringExtra(AppConstant.KEY.IMG_PATH)));
        iv.setLayoutParams(new RelativeLayout.LayoutParams(picWidth, picHeight));
    }
}
