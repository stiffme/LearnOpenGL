#version 330 core

in vec2 oTexCoord;
in vec3 oFragCoord; //frag pos in view space
in vec3 oNormal;   //normalized norm vector in view space

uniform sampler2D floorTexture;
uniform vec3 lightPos;
uniform vec3 viewPos;
uniform samplerCube depthMap;
uniform float far_plane;
out vec4 oColor;


float ShadowCalculation(vec3 fragPos)
{
    // get vector between fragment position and light position
    vec3 fragToLight = fragPos - lightPos;
    // ise the fragment to light vector to sample from the depth map
    float closestDepth = texture(depthMap, fragToLight).r;
    // it is currently in linear range between [0,1], let's re-transform it back to original depth value
    closestDepth *= far_plane;
    // now get current linear depth as the length between the fragment and light position
    float currentDepth = length(fragToLight);
    // test for shadows
    float bias = 0.05; // we use a much larger bias since depth is now in [near_plane, far_plane] range
    float shadow = currentDepth -  bias > closestDepth ? 1.0 : 0.0;
    // display closestDepth as debug (to visualize depth cubemap)
    //oColor = vec4(vec3(closestDepth / far_plane), 1.0);

    return shadow;
}


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
    vec3 viewDir = normalize(viewPos-oFragCoord);
    float spec = 0.0;
    vec3 halfwayDir = normalize(viewDir + lightDir);
    spec = pow(max(dot(halfwayDir, oNormal), 0.0), 64.0);

    vec3 specular = lightColor  * spec * color;
    float shadow = ShadowCalculation(oFragCoord);
    oColor = vec4(ambient + (1.0 - shadow) * ( diffuse + specular), 1.0);
}
