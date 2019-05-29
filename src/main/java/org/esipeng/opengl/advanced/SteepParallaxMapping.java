package org.esipeng.opengl.advanced;

import org.esipeng.opengl.base.Camera;
import org.esipeng.opengl.base.OGLApplicationAbstract;
import org.esipeng.opengl.base.OGLApplicationGL33;
import org.esipeng.opengl.base.TextureLoader;
import org.esipeng.opengl.base.engine.MVPManager;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL33.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class SteepParallaxMapping extends OGLApplicationGL33 {
    private static final Logger logger = LoggerFactory.getLogger(SteepParallaxMapping.class);
    private static final int DIFFUSE_MAP = 0;
    private static final int NORMAL_MAP = 1;
    private static final int DEPTH_MAP = 2;

    private int mWidth = 800, mHeight = 600;
    private long mWindow;

    private int mShader;

    private int mDiffuseMap, mNormalMap, mDepthMap;

    private Vector3f mLightPos;
    private Camera mCamera;
    private MVPManager mMVPManager = new MVPManager();
    private Matrix4f mMatTemp = new Matrix4f();
    private Vector3f mRotateAxis = new Vector3f(1.0f,0.0f,1.0f).normalize();
    private int mQuadVAO = 0;

    private boolean enableNormalMap = false;

    @Override
    protected boolean applicationCreateContext() {
        mWindow = glfwCreateWindow(mWidth,mHeight,"Normal Mapping", NULL, NULL);
        if(mWindow == NULL)
            return false;

        glfwMakeContextCurrent(mWindow);
        return true;
    }

    @Override
    protected boolean applicationInitAfterContext() {
        //set view port
        glViewport(0,0,mWidth,mHeight);
        //enable depth test
        glEnable(GL_DEPTH_TEST);

        //load shader
        try {
            mShader = compileAndLinkProgram(
                    "advanced/ParallaxMapping/SteepParallaxMapping/vertex.glsl",
                    "advanced/ParallaxMapping/SteepParallaxMapping/fragment.glsl"
            );

            mDiffuseMap =
                    loadTextureFromResource("advanced/ParallaxMapping/SteepParallaxMapping/toy_box_diffuse.png");
            mNormalMap =
                    loadTextureFromResource("advanced/ParallaxMapping/SteepParallaxMapping/toy_box_normal.png");
            mDepthMap =
                    loadTextureFromResource("advanced/ParallaxMapping/SteepParallaxMapping/toy_box_disp.png");
        } catch (Exception e)   {
            e.printStackTrace();
            return false;
        }

        if(!setUniform1i(mShader,"diffuseMap", DIFFUSE_MAP))
            logger.warn("diffuseMap uniform is not set");

        if(!setUniform1i(mShader,"normalMap", NORMAL_MAP))
            logger.warn("normalMap uniform is not set");

        if(!setUniform1i(mShader,"depthMap", DEPTH_MAP))
            logger.warn("depthMap uniform is not set");

        mLightPos = new Vector3f(0.5f,1.0f,0.3f);
        if(!mMVPManager.bindProgram(mShader))
            return false;

        glClearColor(0.1f,0.1f,0.1f,1.0f);

        mCamera = new Camera(
                0.0f,0.0f,3.0f,
                0.f,0.f,-1.f,
                0.f,1.f,0.f,
                mWidth/2, mHeight/2,
                mWindow
        );
        mCamera.enableMouseFpsView();
        setUniform1f(mShader,"height_scale",0.1f);

        glfwSetKeyCallback(mWindow, new GLFWKeyCallback() {
            @Override
            public void invoke(long window, int key, int scancode, int action, int mods) {
                if(key == GLFW_KEY_B && action == GLFW_RELEASE) {
                    SteepParallaxMapping.this.enableNormalMap = !SteepParallaxMapping.this.enableNormalMap;
                }

                if(enableNormalMap)
                    setUniform1i(mShader,"parallax", 1);
                else
                    setUniform1i(mShader,"parallax", 0);
            }
        });
        return true;
    }

    @Override
    protected void update(float elapsed) {
        mCamera.processInput(elapsed);
    }

    @Override
    protected void draw() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        glUseProgram(mShader);

        //update view and projection matrix
        mMVPManager.updateView(mCamera.generateViewMat());
        mMVPManager.updateProjection(
                mMatTemp.setPerspective(mCamera.getFovRadians(),
                        (float)mWidth/mHeight,
                        0.1f, 100.f));

        //render normal-mapped quad
        mMatTemp.identity().rotate((float)50.f,mRotateAxis);
        mMVPManager.updateModel(mMatTemp);

        mMVPManager.getUboManager().setValue("lightPos", mLightPos);
        mMVPManager.getUboManager().setValue("viewPos", mCamera.getCameraPos());

        glActiveTexture(GL_TEXTURE0 + DIFFUSE_MAP);
        glBindTexture(GL_TEXTURE_2D, mDiffuseMap);

        glActiveTexture(GL_TEXTURE0 + NORMAL_MAP);
        glBindTexture(GL_TEXTURE_2D, mNormalMap);

        glActiveTexture(GL_TEXTURE0 + DEPTH_MAP);
        glBindTexture(GL_TEXTURE_2D, mDepthMap);
        renderQuad();

        glFinish();
    }

    protected int loadTextureFromResource(String resoure) {
        TextureLoader loader = new TextureLoader();
        try {
            if(!loader.loadFromResource(resoure))
                return -1;

            int textureType;
            switch (loader.getNrChannel())  {
                case 1:
                    textureType = GL_RED;
                    break;
                case 3:
                    textureType = GL_RGB;
                    break;
                    default:
                case 4:
                    textureType = GL_RGBA;
                    break;
            }
            int tex = getManagedTexture();
            glBindTexture(GL_TEXTURE_2D, tex);
            glTexImage2D(GL_TEXTURE_2D,0,textureType,loader.getX(),loader.getY(),0,textureType,
                    GL_UNSIGNED_BYTE,loader.getData());
            glGenerateMipmap(GL_TEXTURE_2D);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            glBindTexture(GL_TEXTURE_2D, 0);
            return tex;
        } catch (Exception e)   {
            e.printStackTrace();
            return -1;
        } finally {
            loader.release();
        }
    }

    private void renderQuad()   {
        if(mQuadVAO == 0)   {
            //positions
            Vector3f pos1 = new Vector3f(-1.0f,1.0f,0.0f);
            Vector3f pos2 = new Vector3f(-1.0f,-1.0f,0.0f);
            Vector3f pos3 = new Vector3f(1.0f,-1.0f,0.0f);
            Vector3f pos4 = new Vector3f(1.0f,1.0f,0.0f);

            Vector2f uv1 = new Vector2f(0.0f, 1.0f);
            Vector2f uv2 = new Vector2f(0.0f, 0.0f);
            Vector2f uv3 = new Vector2f(1.0f, 0.0f);
            Vector2f uv4 = new Vector2f(1.0f, 1.0f);

            Vector3f nm = new Vector3f(0.0f,0.0f,1.0f);

            Vector3f tangent1 = new Vector3f(), tangent2 = new Vector3f(),
                    bitangent1 = new Vector3f(), bitangent2 = new Vector3f();

            Vector3f edge1 = new Vector3f(), edge2 = new Vector3f();
            Vector2f deltaUV1 = new Vector2f(), deltaUV2 = new Vector2f();

            //triangle 1
            pos2.sub(pos1, edge1);
            pos3.sub(pos1, edge2);

            uv2.sub(uv1, deltaUV1);
            uv3.sub(uv1, deltaUV2);

            float f = 1.0f / (deltaUV1.x * deltaUV2.y - deltaUV2.x * deltaUV1.y);
            tangent1.x = f * (deltaUV2.y * edge1.x - deltaUV1.y * edge2.x);
            tangent1.y = f * (deltaUV2.y * edge1.y - deltaUV1.y * edge2.y);
            tangent1.z = f * (deltaUV2.y * edge1.z - deltaUV1.y * edge2.z);
            tangent1.normalize();

            bitangent1.x = f * (-deltaUV2.x * edge1.x + deltaUV1.x * edge2.x);
            bitangent1.y = f * (-deltaUV2.x * edge1.y + deltaUV1.x * edge2.y);
            bitangent1.z = f * (-deltaUV2.x * edge1.z + deltaUV1.x * edge2.z);
            bitangent1.normalize();

            //triangle 2
            pos3.sub(pos1, edge1);
            pos4.sub(pos1, edge2);

            uv3.sub(uv1, deltaUV1);
            uv4.sub(uv1, deltaUV2);

            f = 1.0f / (deltaUV1.x * deltaUV2.y - deltaUV2.x * deltaUV1.y);
            tangent2.x = f * (deltaUV2.y * edge1.x - deltaUV1.y * edge2.x);
            tangent2.y = f * (deltaUV2.y * edge1.y - deltaUV1.y * edge2.y);
            tangent2.z = f * (deltaUV2.y * edge1.z - deltaUV1.y * edge2.z);
            tangent2.normalize();

            bitangent2.x = f * (-deltaUV2.x * edge1.x + deltaUV1.x * edge2.x);
            bitangent2.y = f * (-deltaUV2.x * edge1.y + deltaUV1.x * edge2.y);
            bitangent2.z = f * (-deltaUV2.x * edge1.z + deltaUV1.x * edge2.z);
            bitangent2.normalize();

            float[] quadVertices = {
                    // Positions            // normal         // TexCoords  // Tangent                          // Bitangent
                    pos1.x, pos1.y, pos1.z, nm.x, nm.y, nm.z, uv1.x, uv1.y, tangent1.x, tangent1.y, tangent1.z, bitangent1.x, bitangent1.y, bitangent1.z,
                    pos2.x, pos2.y, pos2.z, nm.x, nm.y, nm.z, uv2.x, uv2.y, tangent1.x, tangent1.y, tangent1.z, bitangent1.x, bitangent1.y, bitangent1.z,
                    pos3.x, pos3.y, pos3.z, nm.x, nm.y, nm.z, uv3.x, uv3.y, tangent1.x, tangent1.y, tangent1.z, bitangent1.x, bitangent1.y, bitangent1.z,

                    pos1.x, pos1.y, pos1.z, nm.x, nm.y, nm.z, uv1.x, uv1.y, tangent2.x, tangent2.y, tangent2.z, bitangent2.x, bitangent2.y, bitangent2.z,
                    pos3.x, pos3.y, pos3.z, nm.x, nm.y, nm.z, uv3.x, uv3.y, tangent2.x, tangent2.y, tangent2.z, bitangent2.x, bitangent2.y, bitangent2.z,
                    pos4.x, pos4.y, pos4.z, nm.x, nm.y, nm.z, uv4.x, uv4.y, tangent2.x, tangent2.y, tangent2.z, bitangent2.x, bitangent2.y, bitangent2.z
            };

            mQuadVAO = getManagedVAO();
            int vbo = getManagedVBO();
            glBindVertexArray(mQuadVAO);
            glBindBuffer(GL_ARRAY_BUFFER, vbo);
            glBufferData(GL_ARRAY_BUFFER, quadVertices, GL_STATIC_DRAW);
            glVertexAttribPointer(0,3,GL_FLOAT,false,Float.BYTES * 14, Float.BYTES * 0);
            glVertexAttribPointer(1,3,GL_FLOAT,false,Float.BYTES * 14, Float.BYTES * 3);
            glVertexAttribPointer(2,2,GL_FLOAT,false,Float.BYTES * 14, Float.BYTES * 6);
            glVertexAttribPointer(3,3,GL_FLOAT,false,Float.BYTES * 14, Float.BYTES * 8);
            glVertexAttribPointer(4,3,GL_FLOAT,false,Float.BYTES * 14, Float.BYTES * 11);
            glEnableVertexAttribArray(0);
            glEnableVertexAttribArray(1);
            glEnableVertexAttribArray(2);
            glEnableVertexAttribArray(3);
            glEnableVertexAttribArray(4);
        }
        glBindVertexArray(mQuadVAO);
        glDrawArrays(GL_TRIANGLES,0,6);
        glBindVertexArray(0);
    }

    public static void main(String[] args)  {
        OGLApplicationAbstract application = new SteepParallaxMapping();
        application.run();
    }
}
