#version 330 core
layout(location = 0) in vec3 aPos;
layout(location = 1) in vec3 aNormal;
layout(location = 2) in vec2 aTexCoord;

uniform ModelViewProjection {
    mat4 model;
    mat4 view;
    mat4 projection;
};

out vec3 oFragPos; //frag pos in view space
out vec3 oNormal; //normalized norm vector in view space
out vec2 oTexCoord;

void main() {
    oTexCoord = aTexCoord;
    oNormal = normalize(mat3(transpose(inverse(view *model))) * aNormal);
    vec4 positionInView =  view * model * vec4(aPos, 1.0f);
    oFragPos = vec3(positionInView);
    gl_Position = projection * positionInView;
}
