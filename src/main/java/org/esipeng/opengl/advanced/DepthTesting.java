package org.esipeng.opengl.advanced;

import org.esipeng.opengl.base.*;
import org.joml.Matrix4f;

import static org.lwjgl.glfw.GLFW.glfwCreateWindow;
import static org.lwjgl.glfw.GLFW.glfwMakeContextCurrent;
import static org.lwjgl.opengl.GL33.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class DepthTesting extends OGLApplicationGL33 {
    private static final int MVP_BINDING_POINT = 5;
    int mWidth = 800, mHeight = 800;
    int mObjectVAO, mPlaneVAO, mProgramObject, mProgramLining;
    long mWindow;
    UBOManager mvpManager;
    Camera mCamera;
    Matrix4f mModel, mView, mProjection;
    int textureMetal, textureMarble;

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
                    "advanced/stencil/vertex.glsl",
                    "advanced/stencil/fragment.glsl");

            mProgramLining = compileAndLinkProgram(
                    "advanced/stencil/vertex.glsl",
                    "advanced/stencil/fragmentLining.glsl");
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

        //texture uniform
        setUniform1i(mProgramObject,"texuture1", 1);

        //MVP
        mvpManager = new UBOManager();
        int ubo = getManagedVBO();
        if(!mvpManager.attachUniformBlock(mProgramObject,"MVP", ubo))
            return false;
        glBindBufferBase(GL_UNIFORM_BUFFER, MVP_BINDING_POINT, ubo);
        glUniformBlockBinding(mProgramObject, mvpManager.getBlockIndex(), MVP_BINDING_POINT);

        //bind to programLining
        int uBlockLoc = glGetUniformBlockIndex(mProgramLining, "MVP");
        if(uBlockLoc == -1)
            return false;
        glUniformBlockBinding(mProgramLining, uBlockLoc, MVP_BINDING_POINT);

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


            if(!textureLoader.loadFromResource("advanced/stencil/marble.jpg"))
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

    }

    public static void main(String[] args)  {
        OGLApplicationAbstract application = new DepthTesting();
        application.run();
    }
}
