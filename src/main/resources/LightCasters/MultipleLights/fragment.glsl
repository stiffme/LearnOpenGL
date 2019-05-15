#version 330 core

struct MaterialStruct {
    sampler2D diffuse;
    sampler2D specular;
    float shininess;
};

uniform MaterialStruct material;

in vec3 oFragPos; //frag pos in view space
in vec3 oNormal; //normalized norm vector in view space
in vec2 oTexCoord;

out vec4 oColor;

struct DirLightStruct {
    vec3 direction; //direction in view space
    vec3 ambient;
    vec3 diffuse;
    vec3 specular;
};

uniform DirLight    {
    DirLightStruct dirLight;
};

vec3 CalcDirLight(DirLightStruct light, vec3 normal, vec3 viewDir)  {
    vec3 lightDir = normalize(-light.direction);

    //ambient
    vec3 ambient = light.ambient * vec3(texture(material.diffuse, oTexCoord));

    //diffuse
    float diff = max(dot(lightDir, oNormal),0.0f);
    vec3 diffuse = light.diffuse * vec3(texture(material.diffuse, oTexCoord));

    //specular
    vec3 reflectDir = reflect(-lightDir, oNormal);
    float spec = pow(max(dot(viewDir, reflectDir),0.0f), material.shininess);
    vec3 specular = light.specular * spec * (vec3(texture(material.specular, oTexCoord)));
    return ambient + diffuse + specular;
}

void main() {
    vec3 viewDir = normalize(-oFragPos); //from frag to view, view is at 0,0,0 in view space
    oColor =vec4(CalcDirLight(dirLight, oNormal, viewDir), 1.0f);
}
