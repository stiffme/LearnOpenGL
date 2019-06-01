package org.esipeng.opengl.advanced;

import org.esipeng.opengl.base.*;
import org.esipeng.opengl.base.engine.MVPManager;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFWKeyCallback;

import static org.lwjgl.opengl.GL33.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class HDR extends OGLApplicationGL33 {
    int mWidth = 800, mHeight = 600;
    Camera mCamera;
    Matrix4f tempMat = new Matrix4f();
    long mWindow;
    int mShader, mHdrShader;
    int mWoodTexture, mHdrFBO, mColorBuffer;

    Vector3f[] mLightPositions, mLightColors;
    MVPManager mvpManager = new MVPManager();
    UBOManager lightSettingManager = new UBOManager();

    int hdr = 1;
    float exposure = 1.0f;

    int mQuadVAO, mCubeVAO;
    @Override
    protected boolean applicationCreateContext() {
        mWindow = glfwCreateWindow(mWidth, mHeight, "HDR", NULL, NULL);

        glfwMakeContextCurrent(mWindow);
        return true;
    }

    @Override
    protected boolean applicationInitAfterContext() {
        this.enableDebug();
        glEnable(GL_DEPTH_TEST);
        glViewport(0,0,mWidth,mHeight);

        try {
            mShader = compileAndLinkProgram(
                    "advanced/hdr/vLighting.glsl",
                    "advanced/hdr/fLighting.glsl"
            );

            mHdrShader = compileAndLinkProgram(
                    "advanced/hdr/vHdr.glsl",
                    "advanced/hdr/fHdr.glsl"
            );

            mWoodTexture = loadTextureFromResource("advanced/lighting/wood.png");

        } catch (Exception e)   {
            e.printStackTrace();
            return false;
        }

        mLightPositions = new Vector3f[]    {
                new Vector3f(0.0f, 0.0f, 49.5f),
                new Vector3f(-1.4f, -1.9f, 9.0f),
                new Vector3f(0.0f, -1.8f, 4.0f),
                new Vector3f(0.8f, -1.7f, 6.0f)
        };

        mLightColors = new Vector3f[]   {
                new Vector3f(200, 200, 200),
                new Vector3f(0.1f, 0.0f, 0.0f),
                new Vector3f(0.0f, 0.0f, 0.2f),
                new Vector3f(0.0f, 0.1f, 0.0f),
        };

        mColorBuffer = getManagedTexture();
        //Create floating point color buffer
        glBindTexture(GL_TEXTURE_2D, mColorBuffer);
        glTexImage2D(GL_TEXTURE_2D,0,GL_RGBA16F,mWidth,mHeight,0,GL_RGBA,GL_FLOAT,NULL);
        glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_MAG_FILTER,GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_MIN_FILTER,GL_LINEAR);
        glBindTexture(GL_TEXTURE_2D, 0);

        //Create depth buffer renderbuffer
        int rboDepth = getManagedRenderBuffer();
        glBindRenderbuffer(GL_RENDERBUFFER, rboDepth);
        glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH_COMPONENT, mWidth, mHeight);
        glBindRenderbuffer(GL_RENDERBUFFER, 0);

        //Framebuffer
        mHdrFBO = getManagedFramebuffer();
        glBindFramebuffer(GL_FRAMEBUFFER, mHdrFBO);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, mColorBuffer, 0);
        glFramebufferRenderbuffer(GL_FRAMEBUFFER,GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER,rboDepth);
        if(glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE)
            return false;
        glBindFramebuffer(GL_FRAMEBUFFER, 0);


        glClearColor(0.1f,0.1f,0.1f,1.0f);
        mCamera = new Camera(0.f,0.f,5.f,
                0.f,0.f,-1.f,
                0.f,1.f,0.f,
                mWidth/2, mHeight/2, mWindow);

        if(!mvpManager.bindProgram(mShader))
            return false;

        int ubo = getManagedVBO();
        if(!lightSettingManager.attachUniformBlock(mShader,"lightSetting", ubo))
            return false;

        glBindBufferBase(GL_UNIFORM_BUFFER, 5, ubo);
        glUniformBlockBinding(mShader, lightSettingManager.getBlockIndex(), 5);

        for(int i = 0; i < mLightPositions.length; ++ i)    {
            lightSettingManager.setValue(
                    String.format("lights[%d].Position", i),
                    mLightPositions[i]
            );

            lightSettingManager.setValue(
                    String.format("lights[%d].Color", i),
                    mLightColors[i]
            );
        }
        lightSettingManager.setValue("lightNum",mLightPositions.length);

        //update model
        tempMat.identity().translate(0.f,0.f,25.f).scale(5.f,5.f,55.f);
        mvpManager.updateModel(tempMat);

        glfwSetKeyCallback(mWindow, new GLFWKeyCallback() {
            @Override
            public void invoke(long window, int key, int scancode, int action, int mods) {
                if(key == GLFW_KEY_SPACE && action == GLFW_RELEASE)   {
                    if(hdr == 0)
                        hdr = 1;
                    else
                        hdr = 0;


                }
            }
        });
        return true;
    }

    @Override
    protected void update(float elapsed) {
        mCamera.processInput(elapsed);

        mvpManager.updateView(mCamera.generateViewMat());

        mvpManager.updateProjection(
                tempMat.setPerspective(mCamera.getFovRadians(),(float)mWidth/mHeight, 0.1f, 100.f)
        );
    }

    @Override
    protected void draw() {
        glBindFramebuffer(GL_FRAMEBUFFER, mHdrFBO);
        {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            glUseProgram(mShader);
            glActiveTexture(GL_TEXTURE0);
            glBindTexture(GL_TEXTURE_2D, mWoodTexture);
            setUniform1i(mShader,"inverse_normals", 1);
            renderCube();
        }
        glBindFramebuffer(GL_FRAMEBUFFER, 0);

        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glUseProgram(mHdrShader);
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D,mColorBuffer);
        setUniform1i(mHdrShader,"hdr", hdr);
        setUniform1f(mHdrShader,"exposure", exposure);
        renderQuad();
    }

    private void renderCube()   {
        if(mCubeVAO == 0)   {
            float[] vertices = {
                    // Back face
                    -0.5f, -0.5f, -0.5f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f, // Bottom-left
                    0.5f, 0.5f, -0.5f, 0.0f, 0.0f, -1.0f, 1.0f, 1.0f, // top-right
                    0.5f, -0.5f, -0.5f, 0.0f, 0.0f, -1.0f, 1.0f, 0.0f, // bottom-right
                    0.5f, 0.5f, -0.5f, 0.0f, 0.0f, -1.0f, 1.0f, 1.0f,  // top-right
                    -0.5f, -0.5f, -0.5f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f,  // bottom-left
                    -0.5f, 0.5f, -0.5f, 0.0f, 0.0f, -1.0f, 0.0f, 1.0f,// top-left
                    // Front face
                    -0.5f, -0.5f, 0.5f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, // bottom-left
                    0.5f, -0.5f, 0.5f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f,  // bottom-right
                    0.5f, 0.5f, 0.5f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f,  // top-right
                    0.5f, 0.5f, 0.5f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, // top-right
                    -0.5f, 0.5f, 0.5f, 0.0f, 0.0f, 1.0f, 0.0f, 1.0f,  // top-left
                    -0.5f, -0.5f, 0.5f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f,  // bottom-left
                    // Left face
                    -0.5f, 0.5f, 0.5f, -1.0f, 0.0f, 0.0f, 1.0f, 0.0f, // top-right
                    -0.5f, 0.5f, -0.5f, -1.0f, 0.0f, 0.0f, 1.0f, 1.0f, // top-left
                    -0.5f, -0.5f, -0.5f, -1.0f, 0.0f, 0.0f, 0.0f, 1.0f,  // bottom-left
                    -0.5f, -0.5f, -0.5f, -1.0f, 0.0f, 0.0f, 0.0f, 1.0f, // bottom-left
                    -0.5f, -0.5f, 0.5f, -1.0f, 0.0f, 0.0f, 0.0f, 0.0f,  // bottom-right
                    -0.5f, 0.5f, 0.5f, -1.0f, 0.0f, 0.0f, 1.0f, 0.0f, // top-right
                    // Right face
                    0.5f, 0.5f, 0.5f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, // top-left
                    0.5f, -0.5f, -0.5f, 1.0f, 0.0f, 0.0f, 0.0f, 1.0f, // bottom-right
                    0.5f, 0.5f, -0.5f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, // top-right
                    0.5f, -0.5f, -0.5f, 1.0f, 0.0f, 0.0f, 0.0f, 1.0f,  // bottom-right
                    0.5f, 0.5f, 0.5f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f,  // top-left
                    0.5f, -0.5f, 0.5f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, // bottom-left
                    // Bottom face
                    -0.5f, -0.5f, -0.5f, 0.0f, -1.0f, 0.0f, 0.0f, 1.0f, // top-right
                    0.5f, -0.5f, -0.5f, 0.0f, -1.0f, 0.0f, 1.0f, 1.0f, // top-left
                    0.5f, -0.5f, 0.5f, 0.0f, -1.0f, 0.0f, 1.0f, 0.0f,// bottom-left
                    0.5f, -0.5f, 0.5f, 0.0f, -1.0f, 0.0f, 1.0f, 0.0f, // bottom-left
                    -0.5f, -0.5f, 0.5f, 0.0f, -1.0f, 0.0f, 0.0f, 0.0f, // bottom-right
                    -0.5f, -0.5f, -0.5f, 0.0f, -1.0f, 0.0f, 0.0f, 1.0f, // top-right
                    // Top face
                    -0.5f, 0.5f, -0.5f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f,// top-left
                    0.5f, 0.5f, 0.5f, 0.0f, 1.0f, 0.0f, 1.0f, 0.0f, // bottom-right
                    0.5f, 0.5f, -0.5f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f, // top-right
                    0.5f, 0.5f, 0.5f, 0.0f, 1.0f, 0.0f, 1.0f, 0.0f, // bottom-right
                    -0.5f, 0.5f, -0.5f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f,// top-left
                    -0.5f, 0.5f, 0.5f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f // bottom-left
            };

            mCubeVAO = getManagedVAO();
            glBindVertexArray(mCubeVAO);
            int vbo = getManagedVBO();
            glBindBuffer(GL_ARRAY_BUFFER, vbo);
            glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);
            glEnableVertexAttribArray(0);
            glVertexAttribPointer(0, 3, GL_FLOAT, false, 8 * Float.BYTES, 0L);
            glEnableVertexAttribArray(1);
            glVertexAttribPointer(1, 3, GL_FLOAT, false, 8 * Float.BYTES, Float.BYTES * 3);
            glEnableVertexAttribArray(2);
            glVertexAttribPointer(2, 2, GL_FLOAT, false, 8 * Float.BYTES, Float.BYTES * 6);
            glBindBuffer(GL_ARRAY_BUFFER, 0);
            glBindVertexArray(0);
        }
        // Render Cube
        glBindVertexArray(mCubeVAO);
        glDrawArrays(GL_TRIANGLES, 0, 36);
        glBindVertexArray(0);
    }

    private void renderQuad()   {
        if(mQuadVAO == 0)   {
            float[] quadVertices = {
                    // Positions        // Texture Coords
                    -1.0f, 1.0f, 0.0f, 0.0f, 1.0f,
                    -1.0f, -1.0f, 0.0f, 0.0f, 0.0f,
                    1.0f, 1.0f, 0.0f, 1.0f, 1.0f,
                    1.0f, -1.0f, 0.0f, 1.0f, 0.0f,
            };
            mQuadVAO = getManagedVAO();
            glBindVertexArray(mQuadVAO);
            int vbo = getManagedVBO();
            glBindBuffer(GL_ARRAY_BUFFER, vbo);
            glBufferData(GL_ARRAY_BUFFER,quadVertices, GL_STATIC_DRAW);
            glVertexAttribPointer(0,3,GL_FLOAT,false,Float.BYTES * 5, 0);
            glVertexAttribPointer(1,2,GL_FLOAT,false,Float.BYTES * 5, Float.BYTES * 3);
            glEnableVertexAttribArray(0);
            glEnableVertexAttribArray(1);
        }

        glBindVertexArray(mQuadVAO);
        glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);
        glBindVertexArray(0);
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

    public static void main(String[] args)  {
        OGLApplicationAbstract application =  new HDR();
        application.run();
    }
}
