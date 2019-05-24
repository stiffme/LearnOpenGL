#version 330 core
layout(location=0) in vec3 aPos;
layout(location=1) in vec3 aNormal;
layout(location=2) in vec2 aTexCoord;

uniform mvp  {
    mat4 model;
    mat4 view;
    mat4 projection;
    mat4 normalMatrix; //convert normal from model space to view space, should be (view * model).inverse().transpose()
};

out VS_OUT  {
    vec3 oNormal; //in view space
    vec2 oTexCoord; // in texture space
    vec3 oFragPos; // in view space
} vs_out;

void main() {
    vec4 posInView = view * model * vec4(aPos, 1.0f);
    vs_out.oFragPos = vec3(posInView);
    vs_out.oNormal = normalize(vec3((normalMatrix * vec4(aNormal,0.0))));
    vs_out.oTexCoord = aTexCoord;
    gl_Position = projection * posInView;
}
