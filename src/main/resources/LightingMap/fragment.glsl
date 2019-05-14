#version 330 core

uniform ColorBlock   {
    vec3 lightColor;
    vec3 objectColor;
    vec3 lightPos;  //light pos in view space
    vec3 lAmbient;
    vec3 lDiffuse;
    vec3 lSpecular;
};

uniform sampler2D mDiffuse;
uniform sampler2D mSpecular;
uniform sampler2D emmisionMap;

uniform Material {
    //vec3 mSpecular;
    float mShininess;
};


out vec4 oColor;

in vec3 oNormal; //oNormal is in view space
in vec3 oFragCoord; //FragCoord in view space
in vec2 oTexCoord; //texture coord
void main() {
    vec3 ambient = lAmbient * vec3(texture(mDiffuse, oTexCoord));

    //diff
    vec3 norm = normalize(oNormal);
    vec3 lightDir = normalize(lightPos - oFragCoord);
    float diff = max(dot(norm, lightDir), 0.0f);
    vec3 diffuse = lDiffuse * (diff * vec3(texture(mDiffuse, oTexCoord)));

    //specular
    //eye coord is 0 because everything is in view space
    vec3 viewDir = normalize(-oFragCoord); //viewDir
    vec3 reflectDir = reflect(-lightDir, norm);
    float spec = pow(max(dot(viewDir, reflectDir), 0.0),mShininess);
    vec3 specular = lSpecular * (spec * vec3(texture(mSpecular,oTexCoord)));

    vec3 result = ambient + diffuse + specular + texture(emmisionMap, oTexCoord).rgb;
    oColor = vec4(result, 1.0f);
}
