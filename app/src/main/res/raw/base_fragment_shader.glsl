#extension GL_OES_EGL_image_external : require
uniform samplerExternalOES uTextureSampler;
precision mediump float;
uniform int vColorType;
varying vec2 vTextureCoord;
uniform vec3 vChangeColor;
void main()
{
    vec4 vCameraColor = texture2D(uTextureSampler, vTextureCoord);
    if(vChangeColor[0] == 1.0 ){
        gl_FragColor = vec4(1.0-vCameraColor.r,  1.0-vCameraColor.g,1.0- vCameraColor.b, 1.0);
    }else{
        gl_FragColor = vec4(vCameraColor.r,  vCameraColor.g, vCameraColor.b, 1.0);
    }

}