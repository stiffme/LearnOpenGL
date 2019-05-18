package org.esipeng.opengl.base.engine;


import org.esipeng.opengl.base.UBOManager;
import org.esipeng.opengl.base.engine.spi.TextureRepository;
import org.joml.Vector4f;
import org.lwjgl.assimp.AIColor4D;
import org.lwjgl.assimp.AIMaterial;
import org.lwjgl.assimp.AIString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.assimp.Assimp.*;
import static org.lwjgl.opengl.GL33.*;
import static org.esipeng.opengl.base.engine.Const.*;

public class Material {
    private static final Logger logger = LoggerFactory.getLogger(Material.class);

    public static final Map<String,Integer> SAMPLER_MAP = new HashMap<>();
    static {
        //texture uniform allocation
        for(int i = 0; i < STACK_SIZE * MAX_TEXTURE_PER_STACK; ++i)
            SAMPLER_MAP.put(String.format("textures[%d]",i), i);
    }

    private Vector4f[] mBaseColor;
    private Texture[][] textures;
    private int materialVBO;
    private float shininess;
    private float shininessStrength;
    private boolean loaded = true;

    public Material(AIMaterial aiMaterial, TextureRepository textureRepository, int program) {
        mBaseColor = new Vector4f[STACK_SIZE];

        mBaseColor[AMBIENT_STACK] = queryBaseColor(aiMaterial, AI_MATKEY_COLOR_AMBIENT);
        mBaseColor[DIFFUSE_STACK] = queryBaseColor(aiMaterial, AI_MATKEY_COLOR_DIFFUSE);
        mBaseColor[SPECULAR_STACK] = queryBaseColor(aiMaterial, AI_MATKEY_COLOR_SPECULAR);

        shininess = queryMaterialFloat(aiMaterial,AI_MATKEY_SHININESS,0.f);
        shininessStrength = queryMaterialFloat(aiMaterial, AI_MATKEY_SHININESS_STRENGTH, 1.0f);

        //get diffuse stack
        textures = new Texture[STACK_SIZE][];
        textures[AMBIENT_STACK] = queryTextureStack(aiMaterial, aiTextureType_AMBIENT, textureRepository);
        textures[DIFFUSE_STACK] = queryTextureStack(aiMaterial, aiTextureType_DIFFUSE, textureRepository);
        textures[SPECULAR_STACK] = queryTextureStack(aiMaterial, aiTextureType_SPECULAR, textureRepository);
        logger.debug("Material texture ambient {} diffuse {} specular {}",
                textures[AMBIENT_STACK].length,
                textures[DIFFUSE_STACK].length,
                textures[SPECULAR_STACK].length);
        flushMaterialVBO(program);
    }

    private float queryMaterialFloat(AIMaterial aiMaterial, String aiMatKey, float defaultValue)    {
        float[] floatBuf = new float[1];
        int[] bufSize = new int[1];
        bufSize[0] = 1;
        int ret = aiGetMaterialFloatArray(aiMaterial,
                aiMatKey,
                aiTextureType_NONE,
                0,
                floatBuf,
                bufSize);
        if(ret != aiReturn_SUCCESS) {
            logger.warn("Getting {} aiMaterial float failed, returning default value", aiMatKey);
            return defaultValue;
        } else
            return floatBuf[0];
    }

    private Texture[] queryTextureStack(AIMaterial aiMaterial, int textureType, TextureRepository textureRepository) {
        int textureCount = aiGetMaterialTextureCount(aiMaterial,textureType);
        logger.debug("Texture count {}", textureCount);
        if(textureCount > MAX_TEXTURE_PER_STACK)  {
            logger.warn("Texture count {} is bigger than max",textureCount);
            textureCount = MAX_TEXTURE_PER_STACK;
        }

        if(textureCount > 0)    {
            Texture[] ret = new Texture[textureCount];
            for(int i = 0; i < textureCount; ++i)   {
                Texture texture = loadOneTexture(aiMaterial, textureType, i, textureRepository);
                if(texture == null)
                    loaded = false;
                ret[i] = texture;
            }
            return ret;
        } else  {
            return new Texture[0];
        }
    }

    private Vector4f queryBaseColor(AIMaterial aiMaterial, CharSequence aiMatKey)   {
        AIColor4D aiColor4D = AIColor4D.create();
        Vector4f baseColor = new Vector4f(0.0f);
        int ret = aiGetMaterialColor(aiMaterial, aiMatKey,aiTextureType_NONE,0, aiColor4D);
        if(ret == aiReturn_SUCCESS) {
            baseColor.x = aiColor4D.r();
            baseColor.y = aiColor4D.g();
            baseColor.z = aiColor4D.b();
            baseColor.w = aiColor4D.a();
        }
        return baseColor;
    }

    private Texture loadOneTexture(AIMaterial aiMaterial, int textureType, int index,
                                   TextureRepository textureRepository)   {
        AIString texturePath = AIString.create();
        int[] op = new int[1];
        float[] blend = new float[1];
        blend[0] = 1.0f;
        int[] mapMode = new int[2];
        int ret = aiGetMaterialTexture(aiMaterial,textureType,index,texturePath,
                null,null,blend,op,mapMode,null);
        if(ret != aiReturn_SUCCESS) {
            logger.warn("get texture path failed!");
            return null;
        }

        int uMapMode , vMapMode;
        uMapMode = mapAiMapModeToGL(mapMode[0]);
        vMapMode = mapAiMapModeToGL(mapMode[1]);

        int textureVBO = textureRepository.getTexture(texturePath.dataString(), uMapMode, vMapMode);
        if(textureVBO == -1)
            return null;

        Texture texture = new Texture();
        texture.textureVBO = textureVBO;
        texture.texBlend = blend[0];
        texture.textOp = op[0];
        return texture;

    }

    private int mapAiMapModeToGL(int mapMode)   {
        int ret;
        switch (mapMode)    {
            case aiTextureMapMode_Wrap:
                ret = GL_REPEAT;
                break;
            case aiTextureMapMode_Clamp:
                ret = GL_CLAMP;
                break;
            case aiTextureMapMode_Decal:
                ret = GL_CLAMP_TO_EDGE;
                break;
            case aiTextureMapMode_Mirror:
                ret = GL_MIRRORED_REPEAT;
                break;
            default:
                ret = GL_REPEAT;
                break;
        }
        return ret;
    }

    public void bindMaterial()   {
        if(!loaded) {
            logger.warn("Material not loaded successfully!");
            return;
        }
        for(int stackId = 0; stackId < STACK_SIZE; ++stackId)
            bindOneStack(stackId,textures[stackId]);

        //bind the UBO for material
        glBindBufferBase(GL_UNIFORM_BUFFER, MATERIAL_BINDING_POINT, materialVBO);
    }

    private void bindOneStack(int stackId, Texture[] textureStack)  {
        for(int i = 0;  i < textureStack.length; ++i)   {
            glActiveTexture(GL_TEXTURE0 + stackId * MAX_TEXTURE_PER_STACK + i);
            glBindTexture(GL_TEXTURE_2D, textureStack[i].textureVBO);
        }

        for(int i = textureStack.length; i < MAX_TEXTURE_PER_STACK; ++i)  {
            glActiveTexture(GL_TEXTURE0 + stackId * MAX_TEXTURE_PER_STACK + i);
            glBindTexture(GL_TEXTURE_2D, 0);
        }
    }

    /**
     *
     * struct MaterialStruct {
     *     vec3 ambientBaseColor;
     *     int ambientSize;
     *     vec3 diffuseBaseColor;
     *     int diffuseSize;
     *     vec3 specularBaseColor;
     *     int specularSize;
     *     float shininess;
     *     float shininess_strength;
     *     float texBlend[STACK_SIZE * MAX_TEXTURE_SIZE];
     *     int texOp[STACK_SIZE * MAX_TEXTURE_SIZE];
     * };
     */
    private void flushMaterialVBO(int program) {
        materialVBO = glGenBuffers();
        UBOManager uboManager = new UBOManager();
        if(!uboManager.attachUniformBlock(program, "Material", materialVBO))    {
            logger.warn("Not found Material uniform block");
        } else  {
//            uboManager.setValue("material.ambientBaseColor", colorAmbient);
//            uboManager.setValue("material.diffuseBaseColor", colorDiffuse);
//            uboManager.setValue("material.specularBaseColor", colorSpecular);
            for(int stackId = 0; stackId < STACK_SIZE; ++stackId) {
                uboManager.setValue(String.format("material.baseColor[%d]", stackId), mBaseColor[stackId]);
            }

            uboManager.setValue("material.ambientSize", textures[AMBIENT_STACK].length);
            uboManager.setValue("material.diffuseSize", textures[DIFFUSE_STACK].length);
            uboManager.setValue("material.specularSize", textures[SPECULAR_STACK].length);

            uboManager.setValue("material.shininess",shininess);
            uboManager.setValue("material.shininess_strength", shininessStrength);

            //set texBlend

            for(int stackid = 0; stackid < STACK_SIZE; ++stackid)   {
                for(int index = 0; index < MAX_TEXTURE_PER_STACK; ++index)  {
                    if(index <  textures[stackid].length)   {
                        uboManager.setValue(
                                String.format("material.texBlend[%d]", stackid * MAX_TEXTURE_PER_STACK + index),
                                textures[stackid][index].texBlend);
                        uboManager.setValue(
                                String.format("material.texOp[%d]", stackid * MAX_TEXTURE_PER_STACK + index),
                                textures[stackid][index].textOp);
                    } else  {
                        uboManager.setValue(
                                String.format("material.texBlend[%d]", stackid * MAX_TEXTURE_PER_STACK + index),
                                0.f);
                        uboManager.setValue(
                                String.format("material.texOp[%d]", stackid * MAX_TEXTURE_PER_STACK + index),
                                0xff);
                    }
                }
            }

        }
    }
}
