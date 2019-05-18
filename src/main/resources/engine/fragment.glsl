#version 330 core

const int MAX_TEXTURE_PER_STACK = 4;
const int STACK_SIZE = 3;
const int AMBIENT_STACK = 0;
const int DIFFUSE_STACK = 1;
const int SPECULAR_STACK = 2;

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
    vec3 position; //position in view space
    vec3 direction; //direction in view space

    int type;

    float constant;
    float linear;
    float quadratic;

    vec3 ambient;
    vec3 diffuse;
    vec3 specular;
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

/**
lightDir: from frag to light, in view space
**/
vec4 CalculateAmbient()    {
    vec4 matAmbient = evaluateStack(material.ambientSize, AMBIENT_STACK);
    return vec4(1.0f);
}

void main() {
    //oColor = texture(textures[DIFFUSE_STACK * MAX_TEXTURE_PER_STACK + 0], oTexCoord);
    oColor = evaluateStack(material.diffuseSize, DIFFUSE_STACK);
}
