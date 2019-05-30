#version 330 core
layout (location = 0) in vec3 aPos;
layout (location = 1) in vec3 aNormal;
layout (location = 2) in vec2 aTexCoords;

out VS_OUT {
    vec3 FragPos;
    vec3 Normal;
    vec2 TexCoords;
} vs_out;

uniform mvp {
    mat4 model;
    mat4 view;
    mat4 projection;
    mat4 normalMatrix;

};
uniform int inverse_normals;

void main() {
    vs_out.FragPos = vec3(model * vec4(aPos, 1.0));
    vs_out.TexCoords = aTexCoords;

    vec3 n;
    if(inverse_normals == 0)
        n = aNormal;
    else
        n = -aNormal;

    mat3 normalMatrix = transpose(inverse(mat3(model)));
    vs_out.Normal = normalize(normalMatrix * n);

    gl_Position = projection * view * model * vec4(aPos, 1.0);
}

