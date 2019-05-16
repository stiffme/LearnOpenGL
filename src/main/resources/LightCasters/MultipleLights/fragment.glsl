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

struct PointLightStruct {
    vec3 position;
    float constant;
    float linear;
    vec3 ambient;
    float quadratic;
    vec3 diffuse;
    vec3 specular;
};

#define NR_POINT_LIGHTS 4
uniform PointLight{
    PointLightStruct pointLights[NR_POINT_LIGHTS];
};

struct SpotLightStruct  {
    //light position in view space is always at 0,0
    vec3 direction;
    float outerCutoff;
    vec3 ambient;
    float cutoff;
    vec3 diffuse;
    vec3 specular;

    float constant;
    float linear;
    float quadratic;
};

uniform SpotLight {
    SpotLightStruct spotLight;
};

vec3 CalcPointLight(PointLightStruct light, vec3 normal, vec3 viewDir)  {
    vec3 lightDir = normalize(light.position - oFragPos);
    float distance = length(light.position - oFragPos);
    float attenuation = 1.0f / ( light.constant + distance * light.linear + distance * light.quadratic * light.quadratic);

    //ambient
    vec3 ambient = light.ambient * vec3(texture(material.diffuse, oTexCoord));

    //diffuse
    float diff = max(dot(lightDir, normal), 0.0f);
    vec3 diffuse = light.diffuse * diff * vec3(texture(material.diffuse, oTexCoord));

    //specular
    vec3 reflectDir = reflect(-lightDir, normal);
    float spec = pow(max(dot(reflectDir, viewDir), 0.0f), material.shininess);
    vec3 specular = light.specular * spec *  vec3(texture(material.specular, oTexCoord));
    return (ambient + diffuse + specular) * attenuation ;
}


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

vec3 CalcSpotLight(SpotLightStruct light, vec3 normal, vec3 viewDir)    {
    vec3 lightDir = normalize(-oFragPos);
    float distance = length(oFragPos);

    float theta = dot(lightDir, normalize(-light.direction));
    float intensity = clamp( (theta - light.outerCutoff) / (light.cutoff - light.outerCutoff), 0.0,1.0f);

    float attenuation = 1.0 / (light.constant + distance * light.linear + distance * light.quadratic * light.quadratic);

    vec3 ambient = light.ambient * vec3(texture(material.diffuse,oTexCoord));

    //diffuse
    float diff = max(dot(lightDir,normal), 0.0);
    vec3 diffuse = light.diffuse * diff * vec3(texture(material.diffuse, oTexCoord));

    //specular
    vec3 reflectDir = reflect(-lightDir, normal);
    float spec = max(dot(reflectDir,normal), 0.0);
    vec3 specular = light.specular * spec * vec3(texture(material.specular, oTexCoord));

    return (ambient + diffuse + specular) * intensity * attenuation;
}


void main() {
    vec3 viewDir = normalize(-oFragPos); //from frag to view, view is at 0,0,0 in view space
    //directional light
    vec3 result =CalcDirLight(dirLight, oNormal, viewDir);

    for(int i = 0; i < NR_POINT_LIGHTS; ++i)
        result += CalcPointLight(pointLights[i], oNormal, viewDir);

    result += CalcSpotLight(spotLight, oNormal, viewDir);
    oColor = vec4(result, 1.0f);
}
