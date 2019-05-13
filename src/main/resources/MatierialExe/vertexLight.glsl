#version 330 core

layout(location=0) in vec3 aPos; //vertex in model space
uniform mat4 lightModel;
uniform ViewProjection  {
    mat4 view;      //matrix to map world space to view space
    mat4 projection;    // matrix to map view space to projection space
};

void main() {
    gl_Position = projection * view * lightModel * vec4(aPos, 1.0f);
}
