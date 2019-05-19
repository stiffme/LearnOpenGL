package org.esipeng.opengl.base.engine;

import org.esipeng.opengl.base.UBOManager;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.lwjgl.opengl.GL33.*;
import static org.esipeng.opengl.base.engine.Const.*;

public class LightsManager {
    private Logger logger = LoggerFactory.getLogger(LightsManager.class);
    private UBOManager uboManager;
    private int lightIndex = 0;
    private BasicLight[] lights = new BasicLight[MAX_LIGHT];
    public boolean init(int program)    {
        int vbo = glGenBuffers();
        uboManager = new UBOManager();
        if(! uboManager.attachUniformBlock(program, "Lights", vbo))
            return false;

        //bind buffer
        glBindBufferBase(GL_UNIFORM_BUFFER, LIGHT_BINDING_POINT, vbo);
        glUniformBlockBinding(program, uboManager.getBlockIndex(), LIGHT_BINDING_POINT);

        updateLightSize();
        return true;
    }

    public boolean createDirectionalLight(Vector3f ambient, Vector3f diffuse, Vector3f specular,
                                          Vector3f directionInWorldSpace)    {
        if(lightIndex + 1 >= MAX_LIGHT) {
            logger.error("Max light reached!");
            return false;
        }

        BasicLight light = new DirectionalLight(lightIndex, uboManager,
                ambient,diffuse,specular,directionInWorldSpace);

        lights[lightIndex] = light;
        logger.debug("Adding directional light in index {}", lightIndex);
        ++lightIndex;
        updateLightSize();
        return true;
    }

    public boolean createPointLight(Vector3f ambient, Vector3f diffuse, Vector3f specular,
                                    Vector3f posInWorldSpace,
                                    float constant,
                                    float linear,
                                    float quadratic)   {
        if(lightIndex + 1 >= MAX_LIGHT) {
            logger.error("Max light reached!");
            return false;
        }

        BasicLight light = new PointLight(lightIndex, uboManager,
                ambient,diffuse,specular,
                posInWorldSpace,
                constant, linear,quadratic);
        lights[lightIndex] = light;
        logger.debug("Adding point light in index {}", lightIndex);
        ++lightIndex;
        updateLightSize();
        return true;
    }

    public boolean createSpotLight(Vector3f ambient, Vector3f diffuse, Vector3f specular,
                                    float constant,
                                    float linear,
                                    float quadratic,
                                    float cutoff,
                                    float outerCutoff)   {
        if(lightIndex + 1 >= MAX_LIGHT) {
            logger.error("Max light reached!");
            return false;
        }

        BasicLight light = new SpotLight(lightIndex, uboManager,
                ambient,diffuse,specular,
                constant, linear,quadratic,
                cutoff, outerCutoff);
        lights[lightIndex] = light;
        logger.debug("Adding spot light in index {}", lightIndex);
        ++lightIndex;
        updateLightSize();
        return true;
    }

    private void updateLightSize()  {
        logger.debug("Updating lightSize {}", lightIndex);
        uboManager.setValue("lights.lightSize",lightIndex);
    }

    public void updateAllLights(Matrix4f view)  {
        for(int i = 0; i <  lightIndex; ++i)
            lights[i].updateWithViewAndModel(view);
    }
}
