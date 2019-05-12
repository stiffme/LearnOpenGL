#version 330 core
precision highp float;
layout (location=0) in vec3 aPos;
layout (location=1) in vec3 aNormal;

//uniform mat4 model;
layout (std140) uniform view_projection {
    mat4 model;
    mat4 view;
    mat4 projection;
};

out vec3 Normal;
out vec3 FragPos;
void main() {
    gl_Position = projection * view * model * vec4(aPos, 1.0f);
    Normal = aNormal;
    FragPos = vec3( model * vec4(aPos, 1.0f));
}
