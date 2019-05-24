package org.esipeng.opengl.advanced;

import org.esipeng.opengl.base.Camera;
import org.esipeng.opengl.base.OGLApplicationAbstract;
import org.esipeng.opengl.base.OGLApplicationGL33;
import org.esipeng.opengl.base.engine.MVPManager;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFWVidMode;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL33.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class MSAA extends OGLApplicationGL33 {
    int mWidth, mHeight;
    int mVAO, mProgram, mProgramQuard, mQuadVAO;
    long mWindow;
    Matrix4f mModel, mProjection;
    MVPManager mvpManager;
    Camera mCamera;
    Matrix4f tempMat;
    int mFramebuffer, fbTexture,mFrameBufferInter, fbTextureInter;

    @Override
    protected boolean applicationCreateContext() {
        long monitor = glfwGetPrimaryMonitor();
        GLFWVidMode mode = glfwGetVideoMode(monitor);

        glfwWindowHint(GLFW_RED_BITS, mode.redBits());
        glfwWindowHint(GLFW_GREEN_BITS, mode.greenBits());
        glfwWindowHint(GLFW_BLUE_BITS, mode.blueBits());
        glfwWindowHint(GLFW_REFRESH_RATE, mode.refreshRate());

        //glfwWindowHint(GLFW_SAMPLES, 4);

        mWidth = mode.width();
        mHeight = mode.height();

        mWindow = glfwCreateWindow(mWidth, mHeight, "Planet",monitor, NULL);
        glfwMakeContextCurrent(mWindow);
        return true;
    }

    @Override
    protected boolean applicationInitAfterContext() {
        try {
            mProgram = compileAndLinkProgram(
                    "advanced/multisample/vShader.glsl",
                    "advanced/multisample/fShader.glsl");

            mProgramQuard = compileAndLinkProgram(
                    "advanced/framebuffer/vertexQuard.glsl",
                    "advanced/framebuffer/fragmentQuardKernel.glsl");
        } catch (Exception e)   {
            e.printStackTrace();
            return false;
        }

        float cubeVertices[] = {
                // Positions
                -0.5f, -0.5f, -0.5f,
                0.5f, -0.5f, -0.5f,
                0.5f,  0.5f, -0.5f,
                0.5f,  0.5f, -0.5f,
                -0.5f,  0.5f, -0.5f,
                -0.5f, -0.5f, -0.5f,

                -0.5f, -0.5f,  0.5f,
                0.5f, -0.5f,  0.5f,
                0.5f,  0.5f,  0.5f,
                0.5f,  0.5f,  0.5f,
                -0.5f,  0.5f,  0.5f,
                -0.5f, -0.5f,  0.5f,

                -0.5f,  0.5f,  0.5f,
                -0.5f,  0.5f, -0.5f,
                -0.5f, -0.5f, -0.5f,
                -0.5f, -0.5f, -0.5f,
                -0.5f, -0.5f,  0.5f,
                -0.5f,  0.5f,  0.5f,

                0.5f,  0.5f,  0.5f,
                0.5f,  0.5f, -0.5f,
                0.5f, -0.5f, -0.5f,
                0.5f, -0.5f, -0.5f,
                0.5f, -0.5f,  0.5f,
                0.5f,  0.5f,  0.5f,

                -0.5f, -0.5f, -0.5f,
                0.5f, -0.5f, -0.5f,
                0.5f, -0.5f,  0.5f,
                0.5f, -0.5f,  0.5f,
                -0.5f, -0.5f,  0.5f,
                -0.5f, -0.5f, -0.5f,

                -0.5f,  0.5f, -0.5f,
                0.5f,  0.5f, -0.5f,
                0.5f,  0.5f,  0.5f,
                0.5f,  0.5f,  0.5f,
                -0.5f,  0.5f,  0.5f,
                -0.5f,  0.5f, -0.5f
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

        mVAO = getManagedVAO();
        glBindVertexArray(mVAO);
        int vbo = getManagedVBO();
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, cubeVertices, GL_STATIC_DRAW);
        glVertexAttribPointer(0,3,GL_FLOAT,false,0,0L);
        glEnableVertexAttribArray(0);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);

        mQuadVAO = getManagedVAO();
        glBindVertexArray(mQuadVAO);
        vbo = getManagedVBO();
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER,quadVertices, GL_STATIC_DRAW);
        glVertexAttribPointer(0,2,GL_FLOAT,false,Float.BYTES * 4, 0L);
        glVertexAttribPointer(1,2,GL_FLOAT,false,Float.BYTES * 4, Float.BYTES * 2);
        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);

        if(!setUniform1i(mProgramQuard, "screenTexture", 2))
            return false;

        mvpManager = new MVPManager();
        if(!mvpManager.bindProgram(mProgram))
            return false;
        tempMat = new Matrix4f();
        mCamera = new Camera(mWindow);
        mCamera.enableMouseFpsView();
        //glEnable(GL_MULTISAMPLE);

        mFramebuffer = getManagedFramebuffer();
        glBindFramebuffer(GL_FRAMEBUFFER, mFramebuffer);
        //texture
        fbTexture = getManagedTexture();
        glBindTexture(GL_TEXTURE_2D_MULTISAMPLE, fbTexture);
        glTexImage2DMultisample(GL_TEXTURE_2D_MULTISAMPLE,4,GL_RGB,mWidth,mHeight,true);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0,GL_TEXTURE_2D_MULTISAMPLE,fbTexture,0);
        glBindTexture(GL_TEXTURE_2D, 0);

        int rbo = getManagedRenderBuffer();
        glBindRenderbuffer(GL_RENDERBUFFER, rbo);
        glRenderbufferStorageMultisample(GL_RENDERBUFFER, 4,GL_DEPTH24_STENCIL8,mWidth,mHeight);
        glFramebufferRenderbuffer(GL_FRAMEBUFFER,GL_DEPTH_STENCIL_ATTACHMENT,GL_RENDERBUFFER,rbo);
        glBindRenderbuffer(GL_RENDERBUFFER, 0);

        if(glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE)
            return false;

        //create inter framebuffer
        mFrameBufferInter = getManagedFramebuffer();
        glBindFramebuffer(GL_FRAMEBUFFER, mFrameBufferInter);
        fbTextureInter = getManagedTexture();
        glBindTexture(GL_TEXTURE_2D, fbTextureInter);
        glTexImage2D(GL_TEXTURE_2D,0,GL_RGB,mWidth,mHeight,0,GL_RGB,GL_UNSIGNED_BYTE,0L);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glFramebufferTexture2D(GL_FRAMEBUFFER,GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, fbTextureInter,0);
        glBindTexture(GL_TEXTURE_2D, 0);

        rbo = getManagedRenderBuffer();
        glBindRenderbuffer(GL_RENDERBUFFER, rbo);
        glRenderbufferStorage(GL_RENDERBUFFER,GL_DEPTH24_STENCIL8,mWidth,mHeight);
        glFramebufferRenderbuffer(GL_FRAMEBUFFER,GL_DEPTH_STENCIL_ATTACHMENT, GL_RENDERBUFFER, rbo);
        glBindRenderbuffer(GL_RENDERBUFFER, 0);

        if(glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE)
            return false;

        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        mCamera.enableMouseFpsView();
        return true;
    }

    @Override
    protected void update(float elapsed) {
        mCamera.processInput(elapsed);
        mvpManager.updateModel(tempMat.identity());
        mvpManager.updateModel(mCamera.generateViewMat());
        tempMat.setPerspective(mCamera.getFovRadians(), (float)mWidth/mHeight, 0.1f,100.f);
        mvpManager.updateProjection(tempMat);
    }

    @Override
    protected void draw() {
        glEnable(GL_DEPTH_TEST);
        glBindFramebuffer(GL_FRAMEBUFFER, this.mFramebuffer);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glUseProgram(mProgram);
        glBindVertexArray(mVAO);
        glDrawArrays(GL_TRIANGLES, 0, 36);
        glFinish();

        glBindFramebuffer(GL_READ_FRAMEBUFFER, mFramebuffer);
        glBindFramebuffer(GL_DRAW_FRAMEBUFFER, mFrameBufferInter);
        glBlitFramebuffer(0,0,mWidth,mHeight, 0,0,mWidth,mHeight,GL_COLOR_BUFFER_BIT,GL_NEAREST);

        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        glClear(GL_COLOR_BUFFER_BIT);
        glDisable(GL_DEPTH_TEST);

        glUseProgram(mProgramQuard);
        glBindVertexArray(mQuadVAO);
        glActiveTexture(GL_TEXTURE2);
        glBindTexture(GL_TEXTURE_2D, fbTextureInter);
        glDrawArrays(GL_TRIANGLES,0,6);

        glFinish();
    }

    public static void main(String[] args)  {
        OGLApplicationAbstract application = new MSAA();
        application.run();
    }
}
