package org.esipeng.opengl.advanced;

import org.esipeng.opengl.base.*;
import org.joml.Matrix4f;

import static org.lwjgl.glfw.GLFW.glfwCreateWindow;
import static org.lwjgl.glfw.GLFW.glfwMakeContextCurrent;
import static org.lwjgl.opengl.GL33.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class FramebufferTest extends OGLApplicationGL33 {
    private static final int MVP_BINDING_POINT = 5;
    int mWidth = 800, mHeight = 800;
    int mObjectVAO, mPlaneVAO, mProgramObject, mQuadVAO, mProgramQuard;
    long mWindow;
    UBOManager mvpManager;
    Camera mCamera;
    Matrix4f mModel, mView, mProjection;
    int textureMetal, textureMarble;
    int mFramebuffer;
    int fbTexture;

    @Override
    protected boolean applicationCreateContext() {
        mWindow = glfwCreateWindow(mWidth, mHeight, "Stencil", NULL, NULL);
        if(mWindow == NULL)
            return false;
        glfwMakeContextCurrent(mWindow);
        return true;
    }

    @Override
    protected boolean applicationInitAfterContext() {
        try {
            mProgramObject = compileAndLinkProgram(
                    "advanced/framebuffer/vertex.glsl",
                    "advanced/framebuffer/fragment.glsl");

            mProgramQuard = compileAndLinkProgram(
                    "advanced/framebuffer/vertexQuard.glsl",
                    "advanced/framebuffer/fragmentQuardKernel.glsl");
        } catch (Exception e)   {
            e.printStackTrace();
            return false;
        }

        float cubeVertices[] = {
                // positions          // texture Coords
                -0.5f, -0.5f, -0.5f,  0.0f, 0.0f,
                0.5f, -0.5f, -0.5f,  1.0f, 0.0f,
                0.5f,  0.5f, -0.5f,  1.0f, 1.0f,
                0.5f,  0.5f, -0.5f,  1.0f, 1.0f,
                -0.5f,  0.5f, -0.5f,  0.0f, 1.0f,
                -0.5f, -0.5f, -0.5f,  0.0f, 0.0f,

                -0.5f, -0.5f,  0.5f,  0.0f, 0.0f,
                0.5f, -0.5f,  0.5f,  1.0f, 0.0f,
                0.5f,  0.5f,  0.5f,  1.0f, 1.0f,
                0.5f,  0.5f,  0.5f,  1.0f, 1.0f,
                -0.5f,  0.5f,  0.5f,  0.0f, 1.0f,
                -0.5f, -0.5f,  0.5f,  0.0f, 0.0f,

                -0.5f,  0.5f,  0.5f,  1.0f, 0.0f,
                -0.5f,  0.5f, -0.5f,  1.0f, 1.0f,
                -0.5f, -0.5f, -0.5f,  0.0f, 1.0f,
                -0.5f, -0.5f, -0.5f,  0.0f, 1.0f,
                -0.5f, -0.5f,  0.5f,  0.0f, 0.0f,
                -0.5f,  0.5f,  0.5f,  1.0f, 0.0f,

                0.5f,  0.5f,  0.5f,  1.0f, 0.0f,
                0.5f,  0.5f, -0.5f,  1.0f, 1.0f,
                0.5f, -0.5f, -0.5f,  0.0f, 1.0f,
                0.5f, -0.5f, -0.5f,  0.0f, 1.0f,
                0.5f, -0.5f,  0.5f,  0.0f, 0.0f,
                0.5f,  0.5f,  0.5f,  1.0f, 0.0f,

                -0.5f, -0.5f, -0.5f,  0.0f, 1.0f,
                0.5f, -0.5f, -0.5f,  1.0f, 1.0f,
                0.5f, -0.5f,  0.5f,  1.0f, 0.0f,
                0.5f, -0.5f,  0.5f,  1.0f, 0.0f,
                -0.5f, -0.5f,  0.5f,  0.0f, 0.0f,
                -0.5f, -0.5f, -0.5f,  0.0f, 1.0f,

                -0.5f,  0.5f, -0.5f,  0.0f, 1.0f,
                0.5f,  0.5f, -0.5f,  1.0f, 1.0f,
                0.5f,  0.5f,  0.5f,  1.0f, 0.0f,
                0.5f,  0.5f,  0.5f,  1.0f, 0.0f,
                -0.5f,  0.5f,  0.5f,  0.0f, 0.0f,
                -0.5f,  0.5f, -0.5f,  0.0f, 1.0f
        };
        float planeVertices[] = {
                // positions          // texture Coords (note we set these higher than 1 (together with GL_REPEAT as texture wrapping mode). this will cause the floor texture to repeat)
                5.0f, -0.5f,  5.0f,  2.0f, 0.0f,
                -5.0f, -0.5f,  5.0f,  0.0f, 0.0f,
                -5.0f, -0.5f, -5.0f,  0.0f, 2.0f,

                5.0f, -0.5f,  5.0f,  2.0f, 0.0f,
                -5.0f, -0.5f, -5.0f,  0.0f, 2.0f,
                5.0f, -0.5f, -5.0f,  2.0f, 2.0f
        };

        float quadVertices[] = { // vertex attributes for a quad that fills the entire screen in Normalized Device Coordinates.
                // positions   // texCoords
                -1.0f,  1.0f,  0.0f, 1.0f,
                -1.0f, -1.0f,  0.0f, 0.0f,
                1.0f, -1.0f,  1.0f, 0.0f,

                -1.0f,  1.0f,  0.0f, 1.0f,
                1.0f, -1.0f,  1.0f, 0.0f,
                1.0f,  1.0f,  1.0f, 1.0f
        };

        int cubeVBO = getManagedVBO();
        glBindBuffer(GL_ARRAY_BUFFER, cubeVBO);
        glBufferData(GL_ARRAY_BUFFER, cubeVertices, GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);

        int planeVBO = getManagedVBO();
        glBindBuffer(GL_ARRAY_BUFFER, planeVBO);
        glBufferData(GL_ARRAY_BUFFER, planeVertices, GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);

        mObjectVAO = getManagedVAO();
        glBindVertexArray(mObjectVAO);
        glBindBuffer(GL_ARRAY_BUFFER, cubeVBO);
        glVertexAttribPointer(0,3,GL_FLOAT,false, Float.BYTES * 5, 0L);
        glVertexAttribPointer(1,2,GL_FLOAT,false,Float.BYTES * 5, Float.BYTES * 3);
        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);

        mPlaneVAO = getManagedVAO();
        glBindVertexArray(mPlaneVAO);
        glBindBuffer(GL_ARRAY_BUFFER,planeVBO);
        glVertexAttribPointer(0,3,GL_FLOAT,false, Float.BYTES * 5, 0L);
        glVertexAttribPointer(1,2,GL_FLOAT,false,Float.BYTES * 5, Float.BYTES * 3);
        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);

        mQuadVAO = getManagedVAO();
        glBindVertexArray(mQuadVAO);
        int quadVBO = getManagedVBO();
        glBindBuffer(GL_ARRAY_BUFFER, quadVBO);
        glBufferData(GL_ARRAY_BUFFER, quadVertices,GL_STATIC_DRAW);
        glVertexAttribPointer(0,2,GL_FLOAT,false,Float.BYTES * 4, 0L);
        glVertexAttribPointer(1,2,GL_FLOAT,false,Float.BYTES * 4, Float.BYTES * 2);
        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);


        //texture uniform
        setUniform1i(mProgramObject,"texuture1", 1);

        setUniform1i(mProgramQuard, "screenTexture", 5);

        //MVP
        mvpManager = new UBOManager();
        int ubo = getManagedVBO();
        if(!mvpManager.attachUniformBlock(mProgramObject,"MVP", ubo))
            return false;
        glBindBufferBase(GL_UNIFORM_BUFFER, MVP_BINDING_POINT, ubo);
        glUniformBlockBinding(mProgramObject, mvpManager.getBlockIndex(), MVP_BINDING_POINT);

        //load texture
        try{
            TextureLoader textureLoader = new TextureLoader();
            if(!textureLoader.loadFromResource("advanced/stencil/metal.png"))
                return false;

            textureMetal = getManagedTexture();
            glBindTexture(GL_TEXTURE_2D, textureMetal);
            glTexImage2D(GL_TEXTURE_2D,
                    0,
                    GL_RGB,
                    textureLoader.getX(),
                    textureLoader.getY(),
                    0,
                    GL_RGB,
                    GL_UNSIGNED_BYTE,
                    textureLoader.getData());
            glGenerateMipmap(GL_TEXTURE_2D);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);


            if(!textureLoader.loadFromResource("advanced/framebuffer/container.jpg"))
                return false;
            textureMarble = getManagedTexture();
            glBindTexture(GL_TEXTURE_2D, textureMarble);
            glTexImage2D(GL_TEXTURE_2D,
                    0,
                    GL_RGB,
                    textureLoader.getX(),
                    textureLoader.getY(),
                    0,
                    GL_RGB,
                    GL_UNSIGNED_BYTE,
                    textureLoader.getData());
            glGenerateMipmap(GL_TEXTURE_2D);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);

            textureLoader.release();
        } catch (Exception e)   {
            e.printStackTrace();
            return false;
        }

        mCamera = new Camera(
                0.f,0.f,3.f,
                0.f,0.f,-1f,
                0f,1f,0f,
                mWidth/2, mHeight/2,
                mWindow
        );
        mCamera.enableMouseFpsView();

        mModel = new Matrix4f();
        mView = new Matrix4f();
        mProjection = new Matrix4f();

        glEnable(GL_DEPTH_TEST);
        glDepthFunc(GL_LEQUAL);

        mFramebuffer = getManagedFramebuffer();
        glBindFramebuffer(GL_FRAMEBUFFER, mFramebuffer);
        //texture attachment
        fbTexture = getManagedTexture();
        glBindTexture(GL_TEXTURE_2D, fbTexture);
        glTexImage2D(GL_TEXTURE_2D,0,GL_RGB,mWidth,mHeight,0,GL_RGB,GL_UNSIGNED_BYTE,NULL);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, fbTexture, 0);
        glBindTexture(GL_TEXTURE_2D, 0);

        int rbo = getManagedRenderBuffer();
        glBindRenderbuffer(GL_RENDERBUFFER,rbo);
        glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH24_STENCIL8, mWidth, mHeight);
        glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_STENCIL_ATTACHMENT, GL_RENDERBUFFER, rbo);
        glBindRenderbuffer(GL_RENDERBUFFER, 0);

        if(glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE)
            return false;

        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        return true;
    }

    @Override
    protected void update(float elapsed) {
        mCamera.processInput(elapsed);

        mProjection.identity().setPerspective(mCamera.getFovRadians(), (float)mWidth / mHeight,0.1f, 100.f);
        mvpManager.setValue("projection", mProjection);
        mvpManager.setValue("view", mCamera.generateViewMat());
    }

    @Override
    protected void draw() {
        glBindFramebuffer(GL_FRAMEBUFFER, mFramebuffer);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT);
        glUseProgram(mProgramObject);
        //draw plane
        mModel.identity();
        mvpManager.setValue("model", mModel);
        glBindVertexArray(mPlaneVAO);
        glActiveTexture(GL_TEXTURE1);
        glBindTexture(GL_TEXTURE_2D, textureMetal);
        glDrawArrays(GL_TRIANGLES, 0, 6);
        glBindVertexArray(0);


        //draw cubs
        glUseProgram(mProgramObject);
        glBindVertexArray(mObjectVAO);
        glActiveTexture(GL_TEXTURE1);
        glBindTexture(GL_TEXTURE_2D, textureMarble);

        mModel.identity().translate(-1.0f, 0.0f, -1.0f);
        mvpManager.setValue("model", mModel);
        glDrawArrays(GL_TRIANGLES, 0, 36);
        mModel.identity().translate(2.0f, 0.0f, 0.0f);
        mvpManager.setValue("model", mModel);
        glDrawArrays(GL_TRIANGLES, 0, 36);

        glFinish();

        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        glUseProgram(mProgramQuard);
        glActiveTexture(GL_TEXTURE5);
        glBindTexture(GL_TEXTURE_2D,fbTexture);
        glBindVertexArray(mQuadVAO);
        glDrawArrays(GL_TRIANGLES, 0, 6);
        glBindVertexArray(0);

        glFinish();
    }

    public static void main(String[] args)  {
        OGLApplicationAbstract application = new FramebufferTest();
        application.run();
    }
}
