#version 330 core

uniform sampler2D texuture1;

out vec4 oColor;
in vec2 texCoord;
void main() {
    vec4 texColor = texture(texuture1, texCoord);
    if(texColor.a < 0.1)
        discard;
    oColor = texColor;
}
