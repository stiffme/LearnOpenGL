#version 330 core

const int MAX_TEXTURE_PER_STACK = 4;
const int STACK_SIZE = 3;
const int AMBIENT_STACK = 0;
const int DIFFUSE_STACK = 1;
const int SPECULAR_STACK = 2;

uniform sampler2D textures[STACK_SIZE * MAX_TEXTURE_PER_STACK];

struct MaterialStruct {
    vec3 baseColor[STACK_SIZE];
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

in vec2 oTexCoord;
in vec3 oFragCoord; //frag pos in view space
in vec3 oNormal;   //normalized norm vector in view space

out vec4 oColor;

//This function is responsible to combine multiple textures
//using op and blend
vec3 evaluateStack(int stackNumberLimit, int indexBase) {
    return vec3(1.0);
}

void main() {
    oColor = texture(textures[DIFFUSE_STACK * MAX_TEXTURE_PER_STACK + 0], oTexCoord);
}
