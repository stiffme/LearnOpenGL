#version 330 core
out vec4 FragColor;

layout (std140) uniform ColorBlock{
    vec3 objectColor;
    vec3 lightColor;
};


void main() {
    FragColor = vec4(lightColor * objectColor, 1.f);
    //FragColor = vec4(1.f);
}
