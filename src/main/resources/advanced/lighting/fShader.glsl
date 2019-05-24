#version 330 core

in vec2 oTexCoord;
in vec3 oFragCoord; //frag pos in view space
in vec3 oNormal;   //normalized norm vector in view space
uniform sampler2D floorTexture;
uniform vec3 lightPos;
uniform int blinn;

out vec4 oColor;

void main() {
    vec3 color = texture(floorTexture, oTexCoord).rgb;

    //ambient
    vec3 ambient = 0.05 * color;

    //diffuse
    vec3 lightDir = normalize(lightPos - oFragCoord);
    float diff = max(dot(lightDir, oNormal), 0.0);
    vec3 diffuse = diff * color;

    //ambient
    vec3 viewDir = normalize(-oFragCoord);
    float spec = 0.0;
    if(blinn == 0)  {
        vec3 reflectDir = reflect(-lightDir, oNormal);
        spec = pow(max(dot(reflectDir, viewDir), 0.0), 8.0);
    } else  {
        vec3 halfwayDir = normalize(viewDir + lightDir);
        spec = pow(max(dot(halfwayDir, oNormal), 0.0), 32.0);
    }
    vec3 specular = vec3(0.3) * spec;
    oColor = vec4(ambient + diffuse + specular, 1.0);

}
