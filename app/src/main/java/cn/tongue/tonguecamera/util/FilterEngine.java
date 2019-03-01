package cn.tongue.tonguecamera.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import cn.tongue.tonguecamera.R;

import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_FRAGMENT_SHADER;
import static android.opengl.GLES20.GL_TRIANGLES;
import static android.opengl.GLES20.GL_VERTEX_SHADER;
import static android.opengl.GLES20.glActiveTexture;
import static android.opengl.GLES20.glAttachShader;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glCompileShader;
import static android.opengl.GLES20.glCreateProgram;
import static android.opengl.GLES20.glCreateShader;
import static android.opengl.GLES20.glDrawArrays;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetError;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glLinkProgram;
import static android.opengl.GLES20.glShaderSource;
import static android.opengl.GLES20.glUniform1i;
import static android.opengl.GLES20.glUniformMatrix4fv;
import static android.opengl.GLES20.glUseProgram;
import static android.opengl.GLES20.glVertexAttribPointer;

/**
 * 滤镜 工具
 * 参考url ： [https://blog.csdn.net/lb377463323/article/details/78054892]
 *
 * @date 2019年2月12日 14:10:07
 * @author ymc
 */

public class FilterEngine {

    @SuppressLint("StaticFieldLeak")
    private static FilterEngine filterEngine = null;

    private Context mContext;
    /**
     * 存放顶点的Color数组
     */
    private FloatBuffer mBuffer;
    private int mOESTextureId = -1;
    private int vertexShader = -1;
    private int fragmentShader = -1;

    private int mShaderProgram = -1;

    private int aPositionLocation = -1;
    private int aTextureCoordLocation = -1;
    private int uTextureMatrixLocation = -1;
    private int uTextureSamplerLocation = -1;
    /**
     * 每行前两个值为顶点坐标，后两个为纹理坐标
     */
    private static final float[] VERTEX_DATA = {
            1f, 1f, 1f, 1f,
            -1f, 1f, 0f, 1f,
            -1f, -1f, 0f, 0f,
            1f, 1f, 1f, 1f,
            -1f, -1f, 0f, 0f,
            1f, -1f, 1f, 0f
    };

    /**
     * 与资源文件 raw fragment_shader 相同
     */
    private static final String VERTEX_SHADER = "" +
            //顶点坐标
            "attribute vec4 aPosition;\n" +
            //纹理矩阵
            "uniform mat4 uTextureMatrix;\n" +
            //自己定义的纹理坐标
            "attribute vec4 aTextureCoordinate;\n" +
            //传给片段着色器的纹理坐标
            "varying vec2 vTextureCoord;\n" +
            "void main()\n" +
            "{\n" +
            //根据自己定义的纹理坐标和纹理矩阵求取传给片段着色器的纹理坐标
            "  vTextureCoord = (uTextureMatrix * aTextureCoordinate).xy;\n" +
            "  gl_Position = aPosition;\n" +
            "}\n";

    /**
     * 与资源文件 raw vertex_shader 相同
     */
    private static final String FRAGMENT_SHADER = "" +
            //使用外部纹理必须支持此扩展
            "#extension GL_OES_EGL_image_external : require\n" +
            "precision mediump float;\n" +
            //外部纹理采样器
            "uniform samplerExternalOES uTextureSampler;\n" +
            "varying vec2 vTextureCoord;\n" +
            "void main() \n" +
            "{\n" +
            //获取此纹理（预览图像）对应坐标的颜色值
            "  vec4 vCameraColor = texture2D(uTextureSampler, vTextureCoord);\n" +
            //求此颜色的灰度值
            "  float fGrayColor = (0.3*vCameraColor.r + 0.59*vCameraColor.g + 0.11*vCameraColor.b);\n" +
            //将此灰度值作为输出颜色的RGB值，这样就会变成黑白滤镜
            "  gl_FragColor = vec4(fGrayColor, fGrayColor, fGrayColor, 1.0);\n" +
            "}\n";

    public static final String POSITION_ATTRIBUTE = "aPosition";
    public static final String TEXTURE_COORD_ATTRIBUTE = "aTextureCoordinate";
    public static final String TEXTURE_MATRIX_UNIFORM = "uTextureMatrix";
    public static final String TEXTURE_SAMPLER_UNIFORM = "uTextureSampler";

    /**
     * 构造方法
     * @param oestextureid oes id
     * @param context 上下文
     */
    public FilterEngine(int oestextureid, Context context) {
        mContext = context;
        mOESTextureId = oestextureid;
        mBuffer = createBuffer(VERTEX_DATA);
        fragmentShader = loadShader(GL_FRAGMENT_SHADER, Utils.readShaderFromResource(mContext, R.raw.base_fragment_shader));
        vertexShader = loadShader(GL_VERTEX_SHADER, Utils.readShaderFromResource(mContext, R.raw.base_vertex_shader));
        mShaderProgram = linkProgram(vertexShader, fragmentShader);
    }

    /**
     * 创建 FloatBuffer 数组 (防止内存回收)
     * @param vertexData float 数组
     * @return FloatBuffer
     */
    private FloatBuffer createBuffer(float[] vertexData) {
        FloatBuffer buffer = ByteBuffer.allocateDirect(vertexData.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        buffer.put(vertexData, 0, vertexData.length).position(0);
        return buffer;
    }

    /**
     * 加载着色器
     * GL_VERTEX_SHADER 代表生成顶点着色器
     * GL_FRAGMENT_SHADER 代表生成片段着色器
     *
     * @param type 类型
     * @param shaderSource shader string
     * @return shader
     */
    private int loadShader(int type, String shaderSource) {
        int shader = glCreateShader(type);
        if (shader == 0) {
            throw new RuntimeException("Create Shader Failed!" + glGetError());
        }
        glShaderSource(shader, shaderSource);
        glCompileShader(shader);
        return shader;
    }

    /**
     * 将两个Shader链接至program中
     * @param verShader verShader
     * @param fragShader fragShader
     * @return program
     */
    private int linkProgram(int verShader, int fragShader) {
        int program = glCreateProgram();
        if (program == 0) {
            throw new RuntimeException("Create Program Failed!" + glGetError());
        }
        //附着顶点和片段着色器
        glAttachShader(program, verShader);
        glAttachShader(program, fragShader);
        // 绑定 program
        glLinkProgram(program);
        //告诉OpenGL ES使用此program
        glUseProgram(program);
        return program;
    }

    public void drawTexture(float[] transformMatrix) {
        aPositionLocation = glGetAttribLocation(mShaderProgram, FilterEngine.POSITION_ATTRIBUTE);
        aTextureCoordLocation = glGetAttribLocation(mShaderProgram, FilterEngine.TEXTURE_COORD_ATTRIBUTE);
        uTextureMatrixLocation = glGetUniformLocation(mShaderProgram, FilterEngine.TEXTURE_MATRIX_UNIFORM);
        uTextureSamplerLocation = glGetUniformLocation(mShaderProgram, FilterEngine.TEXTURE_SAMPLER_UNIFORM);

        glActiveTexture(GLES20.GL_TEXTURE0);
        glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, mOESTextureId);
        glUniform1i(uTextureSamplerLocation, 0);
        glUniformMatrix4fv(uTextureMatrixLocation, 1, false, transformMatrix, 0);

        if (mBuffer != null) {
            mBuffer.position(0);
            glEnableVertexAttribArray(aPositionLocation);
            glVertexAttribPointer(aPositionLocation, 2, GL_FLOAT, false, 16, mBuffer);

            mBuffer.position(2);
            glEnableVertexAttribArray(aTextureCoordLocation);
            glVertexAttribPointer(aTextureCoordLocation, 2, GL_FLOAT, false, 16, mBuffer);

            glDrawArrays(GL_TRIANGLES, 0, 6);
        }
    }

    public int getShaderProgram() {
        return mShaderProgram;
    }

    public FloatBuffer getBuffer() {
        return mBuffer;
    }

    public int getOESTextureId() {
        return mOESTextureId;
    }

    public void setOESTextureId(int OESTextureId) {
        mOESTextureId = OESTextureId;
    }
}

