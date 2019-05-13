#version 330 core

layout(location=0) in vec3 aPos; //vertex in model space
layout(location=1) in vec3 aNormal; //normal direction in model space
layout(location=2) in mat4 aModel; //matrix for model to map model space to world space
//location = 3,4,5

layout(location=6) in vec3 aAmbient;
layout(location=7) in vec3 aDiffuse;
layout(location=8) in vec4 aSpecular; //aShiningness is in 4th

uniform ViewProjection  {
    mat4 view;      //matrix to map world space to view space
    mat4 projection;    // matrix to map view space to projection space
};


out vec3 oNormal; //normal direction in view space
out vec3 oFragCoord; //frag coord in view space
out vec3 mAmbient;
out vec3 mDiffuse;
out vec3 mSpecular;
out float mShiningness;
void main()  {
    vec4 vertexInView = view * aModel * vec4(aPos, 1.0f);
    oFragCoord = vec3(vertexInView);

    gl_Position = projection * vertexInView;

    oNormal = mat3(transpose(inverse((view * aModel)))) * aNormal;

    mAmbient = aAmbient;
    mDiffuse = aDiffuse;
    mSpecular = vec3(aSpecular);
    mShiningness = aSpecular.w;
}
