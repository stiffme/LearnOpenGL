#version 330 core
precision highp float;
out vec4 FragColor;
in vec3 Normal;
in vec3 FragPos;

layout(std140) uniform ColorBlock{
    float ambientStrength;
    vec3 objectColor;
    vec3 lightColor;
    vec3 lightPos;
};

void main() {
    vec3 ambient = ambientStrength * lightColor;

    vec3 norm = normalize(Normal);
    vec3 lightDir = normalize(lightPos - FragPos);
    float diff = max(dot(norm, lightDir), 0.0f);
    vec3 diffuse = diff * lightColor;

    vec3 result = (ambient + diffuse) * objectColor;

    FragColor = vec4(result, 1.0f);
}
