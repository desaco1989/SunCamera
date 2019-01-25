package cn.tongue.tonguecamera.util;

/**
 * 应用 常量类
 *
 * @packageName: cn.ymc.suncamera.util
 * @fileName: AppConstant
 * @date: 2019/1/24  16:54
 * @author: ymc
 * @QQ:745612618
 */

public class AppConstant {

    public interface KEY{
        String IMG_PATH = "IMG_PATH";
        String VIDEO_PATH = "VIDEO_PATH";
        String PIC_WIDTH = "PIC_WIDTH";
        String PIC_HEIGHT = "PIC_HEIGHT";
    }

    public interface RESULT_CODE {
        int RESULT_OK = -1;
        int RESULT_CANCELED = 0;
        int RESULT_ERROR = 1;
    }

}
