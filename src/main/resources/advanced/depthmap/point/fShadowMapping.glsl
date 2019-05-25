#version 330 core

in vec2 oTexCoord;
in vec3 oFragCoord; //frag pos in view space
in vec3 oNormal;   //normalized norm vector in view space

uniform sampler2D floorTexture;
uniform vec3 lightPos;
uniform sampler2D shadowMap;

out vec4 oColor;


void main() {
    vec3 color = texture(floorTexture, oTexCoord).rgb;
    vec3 lightColor = vec3(0.3);
    //ambient
    vec3 ambient = lightColor  * color;

    //diffuse
    vec3 lightDir = normalize(lightPos - oFragCoord);
    float diff = max(dot(lightDir, oNormal), 0.0);
    vec3 diffuse = lightColor *  diff * color;

    //specular
    vec3 viewDir = normalize(-oFragCoord);
    float spec = 0.0;
    vec3 halfwayDir = normalize(viewDir + lightDir);
    spec = pow(max(dot(halfwayDir, oNormal), 0.0), 64.0);

    vec3 specular = lightColor  * spec * color;

    oColor = vec4(ambient + ( diffuse + specular), 1.0);
}
