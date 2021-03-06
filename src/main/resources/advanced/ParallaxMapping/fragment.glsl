#version 330 core

in VS_OUT  {
    vec3 FragPos;           //in world space
    vec2 TexCoords;         //tex coord in tex space
    vec3 TangentLightPos;   //lights pos in tangent space
    vec3 TangentViewPos;    // view pos in tangent space
    vec3 TangentFragPos;    // FragPos in tangent space
} fs_in;

uniform sampler2D diffuseMap;
uniform sampler2D normalMap;
uniform sampler2D depthMap;
uniform int parallax;
uniform float height_scale;

out vec4 FragColor;


vec2 parallaxMapping(vec2 texCoords, vec3 viewDir){
    float height = texture(depthMap, texCoords).r;
    vec2 p = viewDir.xy  * (height * height_scale);
    return texCoords - p;
}

void main() {
    vec3 viewDir = normalize(fs_in.TangentViewPos - fs_in.TangentFragPos);
    vec2 texCoords = fs_in.TexCoords;
    if(parallax == 1)
        texCoords = parallaxMapping(fs_in.TexCoords, viewDir);

    if(texCoords.x > 1.0 || texCoords.y > 1.0 || texCoords.x < 0.0 || texCoords.y < 0.0)
        discard;

    //Obtain the normal vec in tangent space from normal map
    vec3 normal = texture(normalMap, texCoords).rgb;
    //transform the normal from (0, 1) to (-1, 1)
    normal = normalize(normal * 2.0 - 1.0);

    //Get diffuse color
    vec3 color = texture(diffuseMap, texCoords).rgb;
    //Ambient
    vec3 ambient = 0.1 * color;
    //Diffuse
    vec3 lightDir = normalize(fs_in.TangentLightPos - fs_in.TangentFragPos);
    float diff = max(dot(lightDir, normal), 0.0);
    vec3 diffuse = diff * color;

    //Specular
    vec3 halfwayDir = normalize(lightDir + viewDir);
    float spec = pow(max(dot(normal, halfwayDir), 0.0), 32.0);

    vec3 specular = vec3(0.2) * spec;
    FragColor = vec4(ambient + diffuse + specular, 1.0);
}
