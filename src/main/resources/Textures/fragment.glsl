#version 330 core

in vec2 TexCoord;
in vec3 OurColor;

out vec4 FragColor;

uniform sampler2D ourTexture;

void main() {
    FragColor = texture(ourTexture, TexCoord) * vec4(OurColor, 1.0f);
}
