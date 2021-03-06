#version 330 core

layout(location=0) in vec3 aPos;
layout(location=1) in vec3 aNormal; //aNormal is in model space
layout(location=2) in vec2 aTexCoord;


uniform mvp  {
    mat4 model;
    mat4 view;
    mat4 projection;
    mat4 normalMatrix; //convert normal from model space to view space, should be (view * model).inverse().transpose()
    mat4 lightSpaceMatrix;
};
out vec2 oTexCoord;
out vec3 oFragCoord; //frag pos in view space
out vec3 oNormal;   //normalized norm vector in view space
out vec4 FragPosLightSpace;

void main() {
    vec4 posInView = view * model * vec4(aPos, 1.0f);
    mat4 normalMat = transpose(inverse(view * model));
    oFragCoord = posInView.xyz;
    oTexCoord = aTexCoord;
    oNormal = normalize(vec3(normalMat * vec4(aNormal, 0.0f)));
    gl_Position = projection * posInView;
    FragPosLightSpace = lightSpaceMatrix * model * vec4(aPos, 1.0f);
}
