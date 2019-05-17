#version 330 core

layout(location=0) in vec3 aPos;
layout(location=1) in vec3 aNormal; //aNormal is in model space
layout(location=2) in vec2 aTexCoord;

uniform mvp  {
    mat4 model;
    mat4 view;
    mat4 projection;
    mat4 normalMatrix; //convert normal from model space to view space, should be (view * model).inverse().transpose()
};

out vec2 oTexCoord;

void main() {
    gl_Position = projection * view * model * vec4(aPos, 1.0f);
    oTexCoord = aTexCoord;
}
