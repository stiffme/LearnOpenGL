#version 330 core

in VS_OUT  {
    vec3 FragPos;           //in world space
    vec2 TexCoords;         //tex coord in tex space
    vec3 TangentLightPos;   //lights pos in tangent space
    vec3 TangentViewPos;    // view pos in tangent space
    vec3 TangentFragPos;    // FragPos in tangent space
    vec3 Normal;
} fs_in;

uniform sampler2D diffuseMap;
uniform sampler2D normalMap;
out vec4 FragColor;
uniform int enableNormalMap;

void main() {
    //Obtain the normal vec in tangent space from normal map
    vec3 normal;
    if(enableNormalMap == 0)  {
        normal = texture(normalMap, fs_in.TexCoords).rgb;
    } else  {
        normal = fs_in.Normal;
    }
    //transform the normal from (0, 1) to (-1, 1)
    normal = normalize(normal * 2.0 - 1.0);

    //Get diffuse color
    vec3 color = texture(diffuseMap, fs_in.TexCoords).rgb;
    //Ambient
    vec3 ambient = 0.1 * color;

    //Diffuse
    vec3 lightDir = normalize(fs_in.TangentLightPos - fs_in.TangentFragPos);
    float diff = max(dot(lightDir, normal), 0.0);
    vec3 diffuse = diff * color;

    //Specular
    vec3 viewDir = normalize(fs_in.TangentViewPos - fs_in.TangentFragPos);
    vec3 halfwayDir = normalize(lightDir + viewDir);
    float spec = pow(max(dot(normal, halfwayDir), 0.0), 32.0);
    vec3 specular = vec3(0.2) * spec;

    FragColor = vec4(ambient + diffuse + specular, 1.0);
}
