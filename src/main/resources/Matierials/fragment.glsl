#version 330 core

uniform ColorBlock   {
    vec3 lightColor;
    vec3 objectColor;
    vec3 lightPos;  //light pos in view space
    vec3 lAmbient;
    vec3 lDiffuse;
    vec3 lSpecular;
};

uniform Material {
    vec3 mAmbient;
    vec3 mDiffuse;
    vec3 mSpecular;
    float mShininess;
};


out vec4 oColor;

in vec3 oNormal; //oNormal is in view space
in vec3 oFragCoord; //FragCoord in view space
void main() {
    vec3 ambient = mAmbient * lAmbient;

    //diff
    vec3 norm = normalize(oNormal);
    vec3 lightDir = normalize(lightPos - oFragCoord);
    float diff = max(dot(norm, lightDir), 0.0f);
    vec3 diffuse = lDiffuse * (diff * mDiffuse);

    //specular
    //eye coord is 0 because everything is in view space
    vec3 viewDir = normalize(-oFragCoord); //viewDir
    vec3 reflectDir = reflect(-lightDir, norm);
    float spec = pow(max(dot(viewDir, reflectDir), 0.0),mShininess);
    vec3 specular = lSpecular * (spec * mSpecular);

    vec3 result = ambient + diffuse + specular;
    oColor = vec4(result, 1.0f);
}
