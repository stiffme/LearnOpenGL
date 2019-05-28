#version 330 core
layout(location=0) in vec3 position;
layout(location=1) in vec3 normal;
layout(location=2) in vec2 texCoords;
layout(location=3) in vec3 tangent;
layout(location=4) in vec3 bitangent;

out VS_OUT  {
    vec3 FragPos;           //in world space
    vec2 TexCoords;         //tex coord in tex space
    vec3 TangentLightPos;   //lights pos in tangent space
    vec3 TangentViewPos;    // view pos in tangent space
    vec3 TangentFragPos;    // FragPos in tangent space
    vec3 Normal;
} vs_out;

uniform mvp {
    mat4 model;
    mat4 view;
    mat4 projection;
    mat4 normalMatrix;
    vec3 lightPos;  //light pos in world space
    vec3 viewPos;   //view pos in world space
};

void main() {
    gl_Position = projection * view * model * vec4(position, 1.0);
    vs_out.FragPos = vec3(model * vec4(position,1.0));
    vs_out.TexCoords = texCoords;

    mat3 normalMatrix = transpose(inverse(mat3(model)));
    vec3 T = normalize(normalMatrix * tangent);
    vec3 B = normalize(normalMatrix * bitangent);
    vec3 N = normalize(normalMatrix * normal);

    mat3 TBN = transpose(mat3(T, B, N)); // matrix map from world space to tangent space
    vs_out.TangentLightPos = TBN * lightPos;
    vs_out.TangentViewPos = TBN * viewPos;
    vs_out.TangentFragPos = TBN * vs_out.FragPos;
    vs_out.Normal = normal;
}
