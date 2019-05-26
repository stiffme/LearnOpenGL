#version 330 core
layout (location = 0) in vec3 aPos;

uniform mvp  {
    mat4 model;
    mat4 view;
    mat4 projection;
    mat4 normalMatrix;
};

void main()
{
    gl_Position = model * vec4(aPos, 1.0);
}