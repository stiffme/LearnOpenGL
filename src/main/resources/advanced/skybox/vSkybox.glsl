#version 330 core

layout (location = 0) in vec3 aPos;

uniform mvp  {
    mat4 model;
    mat4 view;
    mat4 projection;
    mat4 normalMatrix; //convert normal from model space to view space, should be (view * model).inverse().transpose()
};

out vec3 TexCoords;

void main() {
    mat4 viewNoDisp = mat4(mat3(view));
    vec4 pos = projection * viewNoDisp * vec4(aPos, 1.0);
    gl_Position = pos.xyww;
    TexCoords = aPos;
}
