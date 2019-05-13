#version 330 core

layout(location=0) in vec3 aPos; //vertex in model space
layout(location=1) in vec3 aNormal; //normal direction in model space
layout(location=2) in mat4 aModel; //matrix for model to map model space to world space

uniform ViewProjection  {
    mat4 view;      //matrix to map world space to view space
    mat4 projection;    // matrix to map view space to projection space
};

out vec3 oNormal; //normal direction in view space
out vec3 oFragCoord; //frag coord in view space

void main()  {
    vec4 vertexInView = view * aModel * vec4(aPos, 1.0f);
    oFragCoord = vec3(vertexInView);
    gl_Position = projection * vertexInView;
    mat4 normalMatrix = transpose(inverse((view * aModel)));
    oNormal = vec3(normalMatrix * vec4(aNormal,1.0f));
}
