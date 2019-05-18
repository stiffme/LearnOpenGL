package org.esipeng.opengl.base.engine;

public class Const {
    public static final int MAX_TEXTURE_PER_STACK = 4;
    public static final int STACK_SIZE = 3;
    public static final int AMBIENT_STACK = 0;
    public static final int DIFFUSE_STACK = 1;
    public static final int SPECULAR_STACK = 2;

    //uniform block of Material
    /**
     * truct MaterialStruct {
     *     vec3 ambientBaseColor;
     *     int ambientSize;
     *     vec3 diffuseBaseColor;
     *     int diffuseSize;
     *     vec3 specularBaseColor;
     *     int specularSize;
     *     float shininess;
     *     float shininess_strength;
     *     float texBlend[STACK_SIZE * MAX_TEXTURE_PER_STACK];
     *     int texOp[STACK_SIZE * MAX_TEXTURE_PER_STACK];
     * };
     *
     * uniform Material {
     *     MaterialStruct material;
     * };
     */

    public static final int MATERIAL_BINDING_POINT = 9;
}
