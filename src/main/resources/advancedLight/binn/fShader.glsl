#version 330 core

in VS_OUT  {
    vec3 oNormal; //in view space
    vec2 oTexCoord; // in texture space
    vec3 oFragPos; // in view space
} fs_in;

uniform LightSetting    {
    vec3 lightPos;
    int binn;
};

out vec4 FragColor;
uniform sampler2D floorTexture;

void main() {
    vec3 color = texture(floorTexture, fs_in.oTexCoord).rgb;

    vec3 ambient = 0.5 * color;

    //diffuse
    vec3 lightDir = (lightPos - fs_in.oFragPos);
    float diff = max(dot(lightDir, fs_in.oNormal), 0.0);
    vec3 diffuse = diff * color;

    //ambient
    vec3 reflectDir = reflect(-lightDir, fs_in.oNormal);
    vec3 viewDir = normalize(-fs_in.oFragPos);
    float spec = 0.0;
    if(binn == 0)   {
        spec = pow(max(dot(reflectDir, viewDir), 0.0), 8.0);
    } else  {
        //binn
        vec3 halfway = normalize(lightDir + viewDir);
        spec = pow(max(dot(halfway, fs_in.oNormal), 0.0), 32.0);
    }
    vec3 specular = vec3(0.3) * spec ;
    FragColor = vec4(ambient + diffuse + specular, 1.0);
}
