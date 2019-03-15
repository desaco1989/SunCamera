#extension GL_OES_EGL_image_external : require
uniform samplerExternalOES uTextureSampler;
precision mediump float;
uniform int vColorType;
varying vec2 vTextureCoord;

uniform vec3 vChangeColor;
uniform vec3 vChangeColorB;
uniform vec3 vChangeColorC;
void main()
{
    vec4 vCameraColor = texture2D(uTextureSampler, vTextureCoord);
    gl_FragColor = vec4(vCameraColor.r,  vCameraColor.g, vCameraColor.b, 1.0);
    for(int i = 0;i<vChangeColor.length();++i){
        if(vChangeColor[i] ==1.0){
            gl_FragColor = vec4(1.0-vCameraColor.r,  1.0-vCameraColor.g,1.0- vCameraColor.b, 1.0);
        }

    }

    if(vChangeColor[0] == 9.0 && vChangeColor[1] ==1.0 && vChangeColor[2] == 1.0 ){
            gl_FragColor = vec4(1.0-vCameraColor.r,  1.0-vCameraColor.g,1.0- vCameraColor.b, 1.0);
        }else{

        }

}