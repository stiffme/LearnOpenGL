package org.esipeng.opengl.base.engine;

import org.esipeng.opengl.base.UBOManager;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import static org.esipeng.opengl.base.engine.Const.LIGHT_TYPE_POINT;

public class PointLight extends BasicLight {
    protected Vector3f posInWorldSpace;

    public PointLight(int index, UBOManager uboManager, Vector3f ambient, Vector3f diffuse, Vector3f specular, Vector3f posInWorldSpace, float constant, float linear, float quadratic) {
        super(index, uboManager, LIGHT_TYPE_POINT, ambient, diffuse, specular);
        this.posInWorldSpace = posInWorldSpace;

        //update attenuation constant
        uboManager.setValue(String.format("lights.constant[%d]", index), constant);
        uboManager.setValue(String.format("lights.linear[%d]", index), linear);
        uboManager.setValue(String.format("lights.quadratic[%d]", index), quadratic);
    }

    @Override
    public void updateWithViewAndModel(Matrix4f view) {
        tempVec4f.set(posInWorldSpace, 1.0f).mul(view);
        uboManager.setValue(String.format("lights.position[%d]", index),
                tempVec4f.x,
                tempVec4f.y,
                tempVec4f.z);
    }
}
