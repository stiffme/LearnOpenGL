#version 330 core

const int MAX_TEXTURE_PER_STACK = 4;
const int STACK_SIZE = 3;
const int AMBIENT_STACK = 0;
const int DIFFUSE_STACK = 1;
const int SPECULAR_STACK = 2;

const int MAX_LIGHT = 4;

const int LIGHT_TYPE_DIR = 0;
const int LIGHT_TYPE_POINT = 1;
const int LIGHT_TYPE_SPOT = 2;
/**
        aiTextureOp_Multiply  = 0x0,
        aiTextureOp_Add       = 0x1,
        aiTextureOp_Subtract  = 0x2,
        aiTextureOp_Divide    = 0x3,
        aiTextureOp_SmoothAdd = 0x4,
        aiTextureOp_SignedAdd = 0x5;
        **/
const int OP_MULTIPLY = 0;
const int OP_ADD = 1;
const int OP_SUBTRACT = 2;
const int OP_DIVIDE = 3;
const int OP_SMOOTHADD = 4;
const int OP_SIGNEDADD = 5;


uniform sampler2D textures[STACK_SIZE * MAX_TEXTURE_PER_STACK];

struct MaterialStruct {
    vec4 baseColor[STACK_SIZE];
    int ambientSize;
    int diffuseSize;
    int specularSize;
    float shininess;
    float shininess_strength;
    float texBlend[STACK_SIZE * MAX_TEXTURE_PER_STACK];
    int texOp[STACK_SIZE * MAX_TEXTURE_PER_STACK];
};

uniform Material {
    MaterialStruct material;
};

struct LightStruct {
    int lightSize;
    vec3 position[MAX_LIGHT]; //position in view space
    vec3 direction[MAX_LIGHT]; //direction in view space, normalized

    int type[MAX_LIGHT];

    float constant[MAX_LIGHT];
    float linear[MAX_LIGHT];
    float quadratic[MAX_LIGHT];

    vec3 ambient[MAX_LIGHT];
    vec3 diffuse[MAX_LIGHT];
    vec3 specular[MAX_LIGHT];

    float cutoff[MAX_LIGHT];
    float outerCutoff[MAX_LIGHT];
};

uniform Lights  {
    LightStruct lights;
};

in vec2 oTexCoord;
in vec3 oFragCoord; //frag pos in view space
in vec3 oNormal;   //normalized norm vector in view space

out vec4 oColor;

//This function is responsible to combine multiple textures
//using op and blend
vec4 evaluateStack(int stackNumberLimit, int stackId) {
    vec4 base = material.baseColor[stackId];
    for(int ind = 0; ind < stackNumberLimit; ++ind)    {
        int index = stackId * MAX_TEXTURE_PER_STACK + ind;
        vec4 color = texture(textures[index], oTexCoord);
        color *= material.texBlend[index];
        int op = material.texOp[index];

        switch(op)  {
            case OP_MULTIPLY:
            base *= color;
            break;

            case OP_ADD:
            base += color;
            break;

            case OP_SUBTRACT:
            base -= color;
            break;

            case OP_DIVIDE:
            base /= color;
            break;

            case OP_SMOOTHADD:
            base = (base + color) - (base * color);
            break;

            case OP_SIGNEDADD:
            base = base + (color - 0.5f);
            break;

        }
    }
    return base;
}

vec3 calculateDirectionalLightContribution(int index, vec3 mAmbient, vec3 mDiffuse, vec3 mSpecular, vec3 viewDir)   {
    vec3 lightDir = -lights.direction[index];

    //ambient
    vec3 ambient = lights.ambient[index] * mAmbient;

    //diffuse
    float diff = max(dot(lightDir, oNormal), 0.0f);
    vec3 diffuse = lights.diffuse[index] * diff * mDiffuse;

    //specular
    vec3 reflectDir = reflect(-lightDir, oNormal);
    float spec = pow(max(dot(reflectDir, viewDir), 0.0f), material.shininess) * material.shininess_strength;
    vec3 specular = lights.specular[index] * spec * mSpecular ;

    return ambient + diffuse + specular;
}

vec3 calculatePointLightContribution(int index, vec3 mAmbient, vec3 mDiffuse, vec3 mSpecular, vec3 viewDir)   {
    vec3 lightDir = normalize(lights.position[index] - oFragCoord);
    float distance = length (lights.position[index] - oFragCoord);
    float attenuation = 1.0f / (lights.constant[index] + distance * lights.linear[index]
                                 + lights.quadratic[index] * distance * distance);

    //ambient
    vec3 ambient = lights.ambient[index] * mAmbient;

    //diffuse
    float diff = max(dot(lightDir, oNormal), 0.0);
    vec3 diffuse = lights.diffuse[index] * diff * mDiffuse;

    //specular
    vec3 reflectDir = reflect(-lightDir, oNormal);
    float spec = pow(max(dot(reflectDir, viewDir), 0.0), material.shininess) * material.shininess_strength;
    vec3 specular = lights.specular[index] * spec * mSpecular;

    return (ambient + diffuse + specular) * attenuation;
}

vec3 calculateSpotLightContribution(int index, vec3 mAmbient, vec3 mDiffuse, vec3 mSpecular, vec3 viewDir) {
    vec3 lightDir = normalize(-oFragCoord);
    float distance = length(oFragCoord);

    float theta = dot(lightDir, -lights.direction[index]);
    float intensity = clamp( (theta - lights.outerCutoff[index]) / (lights.cutoff[index] - lights.outerCutoff[index]),
                    0.0f,
                    1.0f);

    float attenuation = 1.0f / (lights.constant[index] + distance * lights.linear[index]
    + lights.quadratic[index] * distance * distance);

    //amient
    vec3 ambient = lights.ambient[index] * mAmbient;

    //diffuse
    float diff = max(dot(lightDir, oNormal), 0.0);
    vec3 diffuse = lights.diffuse[index] * diff * mDiffuse;

    //specular
    vec3 reflectDir = reflect(-lightDir, oNormal);
    float spec  = pow( max(dot(reflectDir, viewDir),0.0), material.shininess) * material.shininess_strength;
    vec3 specular = lights.specular[index] * spec * mSpecular;

    return (ambient + diffuse + specular) * intensity * attenuation;
}



vec3 calculateLight(vec3 mAmbient, vec3 mDiffuse, vec3 mSpecular)   {
    vec3 color = vec3(0.0f);
    vec3 viewDir = normalize(-oFragCoord); //from frag to view, view is at 0 in view space
    for(int index = 0; index < lights.lightSize; ++index)   {
        int lightType = lights.type[index];
        switch(lightType)   {
            case LIGHT_TYPE_DIR:
            color += calculateDirectionalLightContribution(index, mAmbient, mDiffuse, mSpecular, viewDir);
            break;
            case LIGHT_TYPE_POINT:
            color += calculatePointLightContribution(index, mAmbient, mDiffuse, mSpecular, viewDir);
            break;
            case LIGHT_TYPE_SPOT:
            color += calculateSpotLightContribution(index, mAmbient, mDiffuse, mSpecular, viewDir);
            break;
            default:
            break;
        }
    }
    return color;
}




void main() {
    vec3 mAmbient = vec3(evaluateStack(material.ambientSize, AMBIENT_STACK));
    vec3 mDiffuse = vec3(evaluateStack(material.diffuseSize, DIFFUSE_STACK));
    vec3 mSpecular = vec3(evaluateStack(material.specularSize, SPECULAR_STACK));

    oColor = vec4(calculateLight(mAmbient, mDiffuse, mSpecular),1.0f);
}
