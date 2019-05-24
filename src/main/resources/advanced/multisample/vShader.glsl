#version 330 core
layout(location=0) in vec3 aPos;
uniform mvp  {
    mat4 model;
    mat4 view;
    mat4 projection;
    mat4 normalMatrix; //convert normal from model space to view space, should be (view * model).inverse().transpose()
};

void main() {
    gl_Position = projection * view * model * vec4(aPos, 1.0);

}
