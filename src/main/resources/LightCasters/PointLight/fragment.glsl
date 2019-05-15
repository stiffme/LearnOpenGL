#version 330 core

uniform PointLight  {
    vec3 pLightPos; //direction in view space, from light
    vec3 pAmbient;
    vec3 pDiffuse;
    vec3 pSpecular;

    float pConstant;
    float pLinear;
    float pQuadratic;
};

uniform sampler2D mDiffuse;
uniform sampler2D mSpecular;

in vec3 oFragPos; //frag pos in view space
in vec3 oNormal; //normalized norm vector in view space
in vec2 oTexCoord;

out vec4 oColor;

void main() {
    float distance = length(oFragPos - pLightPos);
    float attenuation = 1.0f / (pConstant + pLinear * distance + pQuadratic * (distance * distance));

    vec3 ambient = pAmbient * texture(mDiffuse, oTexCoord).rgb;

    //diffuse
    vec3 lightDir = normalize(-pLightPos); //lightDir from frag to light
    float diff = max(dot(lightDir, oNormal), 0.0f);
    vec3 diffuse = pDiffuse * (diff * texture(mDiffuse, oTexCoord).rgb);

    //specular
    vec3 viewDir = normalize(-oFragPos); //view is at 0
    vec3 reflectDir = reflect(-lightDir, oNormal);
    float spec = pow(max(dot(viewDir, reflectDir),0.0f),32.0f);
    vec3 specular = pSpecular * (spec * texture(mSpecular,oTexCoord).rgb);

    vec3 result = ambient + diffuse + specular;
    oColor = vec4(result, 1.0f) * attenuation;
}
