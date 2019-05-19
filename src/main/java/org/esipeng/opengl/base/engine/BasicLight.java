package org.esipeng.opengl.base.engine;

import org.esipeng.opengl.base.UBOManager;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * struct LightStruct {
 *     int lightSize;
 *     vec3 position[MAX_LIGHT]; //position in view space
 *     vec3 direction[MAX_LIGHT]; //direction in view space
 *
 *     int type[MAX_LIGHT];
 *
 *     float constant[MAX_LIGHT];
 *     float linear[MAX_LIGHT];
 *     float quadratic[MAX_LIGHT];
 *
 *     vec3 ambient[MAX_LIGHT];
 *     vec3 diffuse[MAX_LIGHT];
 *     vec3 specular[MAX_LIGHT];
 *
 *     float cutoff[MAX_LIGHT];
 *     float outerCutoff[MAX_LIGHT];
 * };
 *
 * uniform Lights  {
 *     LightStruct lights;
 * };
 */
public abstract class BasicLight {
    private static final Logger logger = LoggerFactory.getLogger(BasicLight.class);

    protected int index;
    protected int type = -1;
    protected UBOManager uboManager;
    protected Vector3f ambient, diffuse, specular;
    protected Vector4f tempVec4f;
    public BasicLight(int index, UBOManager uboManager,int type, Vector3f ambient, Vector3f diffuse, Vector3f specular) {
        this.index = index;
        this.uboManager = uboManager;
        this.type = type;

        logger.debug("Updating {} to {}", String.format("lights.diffuse[%d]", index), diffuse);
        //update ambient, diffuse, and specular to uniform
        uboManager.setValue(String.format("lights.ambient[%d]", index), ambient);
        uboManager.setValue(String.format("lights.diffuse[%d]", index), diffuse);
        uboManager.setValue(String.format("lights.specular[%d]", index), specular);
        uboManager.setValue(String.format("lights.type[%d]", index), type);
        tempVec4f = new Vector4f();
    }

    public abstract void updateWithViewAndModel(Matrix4f view);
}
