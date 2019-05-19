package org.esipeng.opengl.base.engine;

import org.esipeng.opengl.base.UBOManager;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import static org.esipeng.opengl.base.engine.Const.LIGHT_TYPE_SPOT;

public class SpotLight extends BasicLight {

    public SpotLight(int index, UBOManager uboManager, Vector3f ambient, Vector3f diffuse, Vector3f specular,
                     float constant, float linear, float quadratic,
                     float cutoff, float outerCutoff) {
        super(index, uboManager, LIGHT_TYPE_SPOT, ambient, diffuse, specular);

        //update attenuation constant
        uboManager.setValue(String.format("lights.constant[%d]", index), constant);
        uboManager.setValue(String.format("lights.linear[%d]", index), linear);
        uboManager.setValue(String.format("lights.quadratic[%d]", index), quadratic);

        uboManager.setValue(String.format("lights.cutoff[%d]", index), cutoff);
        uboManager.setValue(String.format("lights.outerCutoff[%d]", index), outerCutoff);

        //directional in view space is always 0,0,-1
        uboManager.setValue(String.format("lights.direction[%d]", index), 0.0f,0.0f, -1.0f);
    }

    @Override
    public void updateWithViewAndModel(Matrix4f view) {

    }
}
