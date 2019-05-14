#version 330 core

layout(location=0) in vec3 aPos;
layout(location=1) in vec3 aNormal; //aNormal is in model space
layout(location=2) in vec2 aTexCoord;
uniform mvp  {
    mat4 model;
    mat4 view;
    mat4 projection;
    mat4 normalMatrix; //convert normal from model space to view space, should be (view * model).inverse().transpose()
};

out vec3 oNormal; //oNormal is in view space
out vec3 oFragCoord; //FragCoord in view space
out vec2 oTexCoord; //texture coord
void main() {
    //output normal in view space
    oNormal = vec3(normalMatrix * vec4(aNormal,1.0f));
    vec4 vertInView = view * model * vec4(aPos, 1.0f);
    //output oFragCoord in view space
    oFragCoord = vec3(vertInView);
    gl_Position = projection * vertInView;
    oTexCoord = aTexCoord;
}
