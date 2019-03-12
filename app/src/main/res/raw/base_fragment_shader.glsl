#extension GL_OES_EGL_image_external : require
uniform samplerExternalOES uTextureSampler;
precision mediump float;
varying vec2 vTextureCoord;

void main()
{
  vec4 vCameraColor = texture2D(uTextureSampler, vTextureCoord);
    if(vCameraColor.r>0.5){
    gl_FragColor = vec4(vCameraColor.r *0.5,  vCameraColor.g, vCameraColor.b, 1.0);
    }else{
    gl_FragColor = vec4(1.0-vCameraColor.r,  1.0-vCameraColor.g,1.0- vCameraColor.b, 1.0);
    }

}