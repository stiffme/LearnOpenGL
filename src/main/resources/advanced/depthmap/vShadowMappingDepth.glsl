#version 330 core

//simple depth shader
layout(location=0) in vec3 position;

uniform mvp  {
    mat4 model;
    mat4 view;
    mat4 projection;
    mat4 normalMatrix; //convert normal from model space to view space, should be (view * model).inverse().transpose()
    mat4 lightSpaceMatrix;
};

void main() {
    gl_Position = lightSpaceMatrix * model * vec4(position, 1.0);
}
