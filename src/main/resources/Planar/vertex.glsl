#version 330 core

layout(location=0) in vec3 aPos;
layout(location=1) in vec3 aNormal; //aNormal is in model space
layout(location=2) in vec2 aTexCoord;
layout(location=3) in mat4 aModel;
uniform mvp  {
    mat4 model;
    mat4 view;
    mat4 projection;
    mat4 normalMatrix; //convert normal from model space to view space, should be (view * model).inverse().transpose()
};

uniform mat4 timeBasedRot;

out vec2 oTexCoord;
out vec3 oFragCoord; //frag pos in view space
out vec3 oNormal;   //normalized norm vector in view space

void main() {
    vec4 posInView = view * timeBasedRot * aModel * vec4(aPos, 1.0f);
    mat4 normMat = transpose(inverse((view * aModel)));
    oFragCoord = posInView.xyz;
    oTexCoord = aTexCoord;
    oNormal = normalize(vec3(normMat * vec4(aNormal, 0.0f)));
    gl_Position = projection * posInView;
}
