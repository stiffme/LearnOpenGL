#version 330 core
precision highp float;
out vec4 FragColor;
in vec3 Normal;
in vec3 FragPos;

//uniform mat4 model;
layout (std140) uniform view_projection {
    mat4 model;
    mat4 view;
    mat4 projection;
    mat4 normalMatrix;
};

uniform vec3 viewPos;

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

    float specularStrength = 0.5;
    vec3 viewDir = normalize(viewPos - FragPos);
    vec3 reflectDir = reflect(-lightDir, norm);
    float spec = pow(max(dot(viewDir, reflectDir), 0.0), 256);
    vec3 specular = specularStrength * spec * lightColor;

    vec3 result = (ambient + diffuse + specular) * objectColor;

    FragColor = vec4(result, 1.0f);
}
