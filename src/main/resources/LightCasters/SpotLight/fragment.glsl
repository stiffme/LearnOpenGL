#version 330 core

uniform SpotLight  {
    //vec3 pLightPos; //light pos in view space, always 0
    vec3 pLightDirection; //light direction in view space
    float pCutoff;
    vec3 pAmbient;
    float pCutoffOuter;
    vec3 pDiffuse;
    vec3 pSpecular;

};

uniform sampler2D mDiffuse;
uniform sampler2D mSpecular;

in vec3 oFragPos; //frag pos in view space
in vec3 oNormal; //normalized norm vector in view space
in vec2 oTexCoord;

out vec4 oColor;

void main() {
    vec3 lightDir = normalize(-oFragPos); //lightDir from frag to light, in spot light, light is already 0
    float theta = dot(-lightDir, pLightDirection);
    float intensity = clamp( (theta - pCutoffOuter) / (pCutoff - pCutoffOuter), 0.0, 1.0 );
    vec3 ambient = pAmbient * texture(mDiffuse, oTexCoord).rgb;


    float diff = max(dot(lightDir, oNormal), 0.0f);
    vec3 diffuse = pDiffuse * (diff * texture(mDiffuse, oTexCoord).rgb);

    //specular
    vec3 viewDir = normalize(-oFragPos); //view is at 0, viewDir from frag to view
    vec3 reflectDir = reflect(-lightDir, oNormal);
    float spec = pow(max(dot(viewDir, reflectDir),0.0f),32.0f);
    vec3 specular = pSpecular * (spec * texture(mSpecular,oTexCoord).rgb);

    vec3 result = ambient + (diffuse + specular) * intensity;
    oColor = vec4(result, 1.0f) ;

}
