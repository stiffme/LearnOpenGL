package org.esipeng.opengl.base.engine;


import org.esipeng.opengl.base.engine.spi.TextureRepository;
import org.joml.Vector3f;
import org.lwjgl.assimp.AIMaterial;
import org.lwjgl.assimp.AIString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.assimp.Assimp.*;
import static org.lwjgl.opengl.GL33.*;

public class Material {
    private static final Logger logger = LoggerFactory.getLogger(Material.class);
    public static final Map<String,Integer> SAMPLER_MAP = new HashMap<>();
    static {
        SAMPLER_MAP.put("diffuse0", 0);
        SAMPLER_MAP.put("diffuse1", 1);
    }

    private Vector3f colorDiffuse;
    private Texture[] diffuses;
    private boolean loaded = true;

    public Material(AIMaterial aiMaterial, TextureRepository textureRepository) {
        colorDiffuse = new Vector3f(0.0f);

        //get color diffuse
        //aiGetMaterialColor(aiMaterial,AI_MATKEY_COLOR_DIFFUSE)

        //get diffuse count
        int diffuseCount = aiGetMaterialTextureCount(aiMaterial,aiTextureType_DIFFUSE);
        logger.debug("Diffuse texture count {}", diffuseCount);
        if(diffuseCount > 0)    {
            diffuses = new Texture[diffuseCount];
            for(int i = 0; i < diffuseCount; ++i)   {
                Texture texture = loadOneTexture(aiMaterial, aiTextureType_DIFFUSE, i, textureRepository);
                if(texture == null)
                    loaded = false;
                diffuses[i] = texture;
            }
        }


    }

    private Texture loadOneTexture(AIMaterial aiMaterial, int textureType, int index,
                                   TextureRepository textureRepository)   {
        AIString texturePath = AIString.create();
        int[] op = new int[1];
        int ret = aiGetMaterialTexture(aiMaterial,textureType,index,texturePath,
                null,null,null,op,null,null);
        if(ret != aiReturn_SUCCESS) {
            logger.warn("get texture path failed!");
            return null;
        }

        int textureVBO = textureRepository.getTexture(texturePath.dataString());
        if(textureVBO == -1)
            return null;

        Texture texture = new Texture();
        texture.textureVBO = textureVBO;
        return texture;

    }

    public void bindMaterial()   {
        for(int i = 0; i < diffuses.length; ++i)    {
            glActiveTexture(GL_TEXTURE0 + i);
            glBindTexture(GL_TEXTURE_2D, diffuses[i].textureVBO);
        }
    }
}
