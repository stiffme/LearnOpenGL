package org.esipeng.opengl.base.engine;

import org.esipeng.opengl.base.TextureLoader;
import org.esipeng.opengl.base.engine.spi.MaterialReposibory;
import org.esipeng.opengl.base.engine.spi.TextureRepository;
import org.lwjgl.assimp.AIMaterial;
import org.lwjgl.assimp.AIMesh;
import org.lwjgl.assimp.AINode;
import org.lwjgl.assimp.AIScene;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.lwjgl.opengl.GL33.*;
import static org.lwjgl.assimp.Assimp.*;
public class Scene implements TextureRepository, MaterialReposibory {
    private static final Logger logger = LoggerFactory.getLogger(Scene.class);
    private String mDirectory;

    private Map<String, Integer> mTextures = new HashMap<>(); //material --> texture
    private Material[] mMaterials;
    private Mesh[] mMeshes;
    MeshNode mRoot;


    public boolean loadSceneFromResource(String resourcePath)   {
        try {
            String path = getResourcePath(resourcePath);
            return loadScene(path);
        } catch (Exception e)   {
            e.printStackTrace();
            return false;
        }
    }

    protected String getResourcePath(String resource) throws Exception  {
        return Paths.get(getClass().getClassLoader().getResource(resource).toURI()).toAbsolutePath().toString();
    }

    public boolean loadScene(String path)   {
        AIScene aiScene = aiImportFile(path,
                aiProcess_Triangulate |
                        aiProcess_FlipUVs |
                        aiProcess_GenNormals );

        if(aiScene == null || (aiScene.mFlags() & AI_SCENE_FLAGS_INCOMPLETE) != 0 || aiScene.mRootNode() == null )  {
            logger.error("Loading {} failed.", path);
            return false;
        }

        File sceneFile = new File(path);
        mDirectory = sceneFile.getParent();
        logger.info("directory of scene is {}", mDirectory);
        processScene(aiScene);
        aiFreeScene(aiScene);
        return true;
    }

    private void processScene(AIScene aiScene) {
        //embeded textures are not supported
        if(aiScene.mNumTextures() > 0)
            logger.warn("{} embeded textures are not supported to load", aiScene.mNumTextures());

        //handle materials
        logger.info("Loading {} materials", aiScene.mNumMaterials());
        mMaterials = new Material[aiScene.mNumMaterials()];
        for(int i = 0; i < aiScene.mNumMaterials(); ++i)    {
            AIMaterial aiMaterial = AIMaterial.create(aiScene.mMaterials().get(i));
            logger.debug("handling material {}", i);
            mMaterials[i] = new Material(aiMaterial,this);
        }

        //handle meshes
        logger.info("Loading {} meshes", aiScene.mNumMeshes());
        mMeshes = new Mesh[aiScene.mNumMeshes()];
        for(int i =0; i < aiScene.mNumMeshes(); ++i)    {
            AIMesh aiMesh = AIMesh.create(aiScene.mMeshes().get(i));
            logger.debug("handling mesh {}", i);
            mMeshes[i] = new Mesh(aiMesh,this);
        }
        mRoot = createMeshNode(aiScene.mRootNode());
    }


    @Override
    public int getTexture(String texturePath) {
        if(mTextures.containsKey(texturePath))
            return mTextures.get(texturePath);
        else    {
            logger.debug("Loading texture {}", texturePath);
            TextureLoader loader = new TextureLoader();
            String textureFullPath = mDirectory + File.separator + texturePath;
            logger.debug("Texture full path is {}", textureFullPath);
            if(!loader.loadFromFilePath(textureFullPath))
                return -1;

            int textureVBO = glGenTextures();
            glBindTexture(GL_TEXTURE_2D,textureVBO);
            int format = -1;
            switch (loader.getNrChannel())  {
                case 1:
                    format = GL_RED;
                    break;
                case 3:
                    format = GL_RGB;
                    break;
                case 4:
                    format = GL_RGBA;
                    break;
            }
            if(format == -1)    {
                loader.release();
                return -1;
            }

            glTexImage2D(GL_TEXTURE_2D,0, format,loader.getX(), loader.getY(),0,format,GL_UNSIGNED_BYTE,loader.getData());
            glGenerateMipmap(GL_TEXTURE_2D);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            loader.release();

            //put the texture into the repository
            mTextures.put(texturePath, textureVBO);
            return textureVBO;
        }

    }

    public void draw()    {
        mRoot.draw();
    }


    private MeshNode createMeshNode(AINode aiNode)  {
        List<Mesh> meshes = new LinkedList<>();
        for(int i = 0; i < aiNode.mNumMeshes(); ++i)
            meshes.add(mMeshes[aiNode.mMeshes().get(i)]);

        List<MeshNode> children = new LinkedList<>();

        for(int j = 0; j < aiNode.mNumChildren(); ++j)   {
            AINode child = AINode.create(aiNode.mChildren().get(j));
            children.add(createMeshNode(child));
        }

        return new MeshNode(meshes, children);
    }

    @Override
    public Material getMaterial(int index) {
        return mMaterials[index];
    }

    public void release()   {
        for(Mesh mesh:mMeshes)
            mesh.release();

        for(Map.Entry<String,Integer>entry : mTextures.entrySet())
            glDeleteTextures(entry.getValue());
    }
}
