#version 330 core

in vec2 oTexCoord;
in vec3 oFragCoord; //frag pos in view space
in vec3 oNormal;   //normalized norm vector in view space
in vec4 FragPosLightSpace;

uniform sampler2D floorTexture;
uniform vec3 lightPos;
uniform int blinn;
uniform sampler2D shadowMap;

out vec4 oColor;

float calculateShadow(vec4 fragPosLightSpace)  {
    vec3 projCoords = fragPosLightSpace.xyz / fragPosLightSpace.w;
    projCoords = projCoords * 0.5 + 0.5;
    float closestDepth = texture(shadowMap, projCoords.xy).r;
    float currentDepth = projCoords.z;

    vec3 lightDir = normalize(lightPos - oFragCoord);
    float bias = max(0.05 * (1.0 - dot(oNormal, lightDir)), 0.005);
    float shadow = 0.0;
    vec2 texelSize = 1.0 / textureSize(shadowMap, 0);
    for(int x = -1; x <= 1; ++x)
    {
        for(int y = -1; y <= 1; ++y)
        {
            float pcfDepth = texture(shadowMap, projCoords.xy + vec2(x, y) * texelSize).r;
            shadow += currentDepth - bias > pcfDepth ? 1.0 : 0.0;
        }
    }
    shadow /= 9.0;
    if(projCoords.z > 1.0)
        shadow = 0.0;
    return shadow;
}

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
    float shadow = calculateShadow(FragPosLightSpace);


    oColor = vec4(ambient + (1 - shadow) * ( diffuse + specular), 1.0);

}
