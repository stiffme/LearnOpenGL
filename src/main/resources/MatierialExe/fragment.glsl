#version 330 core

in vec3 oNormal; //normal direction in view space
in vec3 oFragCoord; //frag coord in view space
in vec3 mAmbient;
in vec3 mDiffuse;
in vec3 mSpecular;
in float mShiningness;

uniform LightInfo   {
    vec4 lightPos;      //light pos in view space
    vec3 lightAmbient;
    vec3 lightDiffuse;
    vec3 lightSpecular;
};

out vec4 oColor;

void main() {
    //ambient
    vec3 ambient = lightAmbient * mAmbient;

    //diffuse
    vec3 norm = normalize(oNormal);

    vec3 lightDir = normalize(vec3(lightPos) - oFragCoord);
    float diff = max(dot(lightDir,norm), 0.0f);
    vec3 diffuse = lightDiffuse *  (diff * mDiffuse);

    //specular
    vec3 viewDir = normalize(-oFragCoord);
    vec3 reflectDir = reflect(-lightDir, norm);
    float spec = pow(max(dot(viewDir,reflectDir), 0.0f), mShiningness * 128.0f);
    vec3 specular = lightSpecular * (spec * mSpecular);

    oColor = vec4(ambient + diffuse + specular, 1.0f);
}
