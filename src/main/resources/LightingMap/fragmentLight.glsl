#version 330 core

uniform ColorBlock   {
    vec3 lightColor;
    vec3 objectColor;
    vec3 lightPos;  //light pos in view space
    vec3 lAmbient;
    vec3 lDiffuse;
    vec3 lSpecular;
};

out vec4 oColor;
void main() {
    //light always is bright!
    oColor = vec4(lightColor, 1.0f);
}
