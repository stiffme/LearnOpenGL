package org.esipeng.opengl.base.engine;

import org.esipeng.opengl.base.UBOManager;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import static org.esipeng.opengl.base.engine.Const.LIGHT_TYPE_DIR;
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
public class DirectionalLight extends BasicLight {
    Vector3f directionInWorldSpace;
    public DirectionalLight(int index, UBOManager uboManager,
                            Vector3f ambient, Vector3f diffuse, Vector3f specular,
                            Vector3f directionInWorldSpace) {
        super(index, uboManager,LIGHT_TYPE_DIR, ambient, diffuse, specular);
        this.directionInWorldSpace = directionInWorldSpace.normalize();
    }

    @Override
    public void updateWithViewAndModel(Matrix4f view) {
        //convert direction in world to view space
        tempVec4f.set(directionInWorldSpace, 0.0f).mul(view);
        //update uniform
        uboManager.setValue(String.format("lights.direction[%d]", index),
                tempVec4f.x,
                tempVec4f.y,
                tempVec4f.z);
    }
}
