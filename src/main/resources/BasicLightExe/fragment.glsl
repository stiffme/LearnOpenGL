#version 330 core

uniform ColorBlock   {
    vec3 lightColor;
    vec3 objectColor;
    vec3 lightPos;  //light pos in view space
};
out vec4 oColor;

in vec3 oNormal; //oNormal is in view space
in vec3 oFragCoord; //FragCoord in view space
void main() {
    float ambientStrength = 0.2;
    vec3 ambient = ambientStrength * lightColor;

    //diff
    vec3 norm = normalize(oNormal);
    vec3 lightDir = normalize(lightPos - oFragCoord);
    float diff = max(dot(norm, lightDir), 0.0f);
    vec3 diffuse = diff * lightColor;

    //specular
    //eye coord is 0 because everything is in view space
    float specularStrength = 0.5f;
    vec3 viewDir = normalize(-oFragCoord); //viewDir
    vec3 reflectDir = reflect(-lightDir, norm);
    float spec = pow(max(dot(viewDir, reflectDir), 0.0),256);
    vec3 specular = specularStrength * spec * lightColor;

    vec3 result = (ambient + diffuse + specular) * objectColor;
    oColor = vec4(result, 1.0f);
}
