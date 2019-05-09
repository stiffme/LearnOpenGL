#version 330 core
in vec2 outTextureCoord;
uniform float aSaturate;
uniform sampler2D aTexture1;
uniform sampler2D aTexture2;

out vec4 outColor;
void main() {
    outColor = mix(texture(aTexture1, outTextureCoord), texture(aTexture2, outTextureCoord), aSaturate);
}
