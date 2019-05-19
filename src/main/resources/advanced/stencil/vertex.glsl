#version 330 core

layout(location = 0) in vec3 aPos;
layout(location = 1) in vec2 aTexCoord;

uniform MVP {
    mat4 model;
    mat4 view;
    mat4 projection;
};
out vec2 texCoord;

void main() {
    gl_Position = projection * view * model * vec4(aPos, 1.0f);
    texCoord = aTexCoord;
}
