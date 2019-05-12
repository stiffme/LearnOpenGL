#version 330 core

uniform ColorBlock   {
    vec3 lightColor;
    vec4 objectColor;
};

out vec4 oColor;
void main() {
    //light always is bright!
    oColor = vec4(lightColor, 1.0f);
}
