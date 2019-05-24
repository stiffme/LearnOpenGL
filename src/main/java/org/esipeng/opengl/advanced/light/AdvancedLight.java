package org.esipeng.opengl.advanced.light;

import org.esipeng.opengl.base.*;
import org.esipeng.opengl.base.engine.MVPManager;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.glfw.GLFWVidMode;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL33.*;
import static org.lwjgl.system.MemoryUtil.NULL;
import static org.esipeng.opengl.base.engine.Const.*;

public class AdvancedLight extends OGLApplicationGL33 {

    private int mWidth, mHeight, mVAO, mProgram;

    private long mWindow;
    private MVPManager mvpManager;
    private Camera mCamera;
    private UBOManager lightSettingManager;
    private Vector4f lightPos;
    private Matrix4f tempMat;
    private int textureWood;

    @Override
    protected boolean applicationCreateContext() {

        long monitor = glfwGetPrimaryMonitor();
        GLFWVidMode mode = glfwGetVideoMode(monitor);

        glfwWindowHint(GLFW_RED_BITS, mode.redBits());
        glfwWindowHint(GLFW_GREEN_BITS, mode.greenBits());
        glfwWindowHint(GLFW_BLUE_BITS, mode.blueBits());
        glfwWindowHint(GLFW_REFRESH_RATE, mode.refreshRate());

        mWidth = mode.width();
        mHeight = mode.height();

        mWindow = glfwCreateWindow(mWidth, mHeight, "Planet",monitor, NULL);
        glfwMakeContextCurrent(mWindow);
        return true;
    }

    @Override
    protected boolean applicationInitAfterContext() {
        float[] planeVertices = {
                // positions            // normals         // texcoords
                10.0f, -0.5f,  10.0f,  0.0f, 1.0f, 0.0f,  10.0f,  0.0f,
                -10.0f, -0.5f,  10.0f,  0.0f, 1.0f, 0.0f,   0.0f,  0.0f,
                -10.0f, -0.5f, -10.0f,  0.0f, 1.0f, 0.0f,   0.0f, 10.0f,

                10.0f, -0.5f,  10.0f,  0.0f, 1.0f, 0.0f,  10.0f,  0.0f,
                -10.0f, -0.5f, -10.0f,  0.0f, 1.0f, 0.0f,   0.0f, 10.0f,
                10.0f, -0.5f, -10.0f,  0.0f, 1.0f, 0.0f,  10.0f, 10.0f
        };

        try {
            mProgram = compileAndLinkProgram(
                    "advancedLight/binn/vShader.glsl",
                    "advancedLight/binn/fShader.glsl"
            );
        } catch (Exception  e)  {
            e.printStackTrace();
            return false;
        }

        mVAO = getManagedVAO();
        glBindVertexArray(mVAO);
        int vbo = getManagedVBO();
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, planeVertices, GL_STATIC_DRAW);
        glVertexAttribPointer(0,3,GL_FLOAT,false, Float.BYTES * 8, 0L);
        glVertexAttribPointer(1, 3,GL_FLOAT,false,Float.BYTES * 8, Float.BYTES * 3);
        glVertexAttribPointer(2,2,GL_FLOAT,false,Float.BYTES * 8,Float.BYTES * 6);
        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);
        glEnableVertexAttribArray(2);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);

        mvpManager = new MVPManager();
        if(!mvpManager.bindProgram(mProgram))
            return false;

        if(!setUniform1i(mProgram,"floorTexture", 1))
            return false;

        int ubo = getManagedVBO();
        lightSettingManager = new UBOManager();
        if(!lightSettingManager.attachUniformBlock(mProgram,"LightSetting", ubo))
            return false;

        if(!lightSettingManager.setValue("binn", 0))
            return false;

        tempMat = new Matrix4f();
        mCamera = new Camera(0.0f,0.0f,3.0f,
                0.f,0.f,-1.f,
                0.f,1.f,0.f,
                mWidth/2, mHeight/2,
                mWindow);
        mCamera.enableMouseFpsView();
        lightPos = new Vector4f(0.f);

        //load texture
        try {
            TextureLoader loader = new TextureLoader();
            if(!loader.loadFromResource("advancedLight/binn/wood.png"))
                return false;

            textureWood = getManagedTexture();
            glBindTexture(GL_TEXTURE_2D, textureWood);
            glTexImage2D(GL_TEXTURE_2D,0,GL_RGB, loader.getX(), loader.getY(), 0, GL_RGB, GL_UNSIGNED_BYTE, loader.getData());
            glGenerateMipmap(GL_TEXTURE_2D);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
            glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            glBindTexture(GL_TEXTURE_2D, 0);

        } catch (Exception e)   {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    protected void update(float elapsed) {
        mCamera.processInput(elapsed);
        mvpManager.updateModel(tempMat.identity());
        Matrix4f view = mCamera.generateViewMat();
        mvpManager.updateView(view);
        mvpManager.updateProjection(tempMat.setPerspective(mCamera.getFovRadians(), (float)mWidth/mHeight, 0.1f, 100.f));
        mvpManager.updateNormalInView();

        lightPos.set(0.0f,0.f,0.f,1.f).mul(view);
        lightSettingManager.setValue("lightPos", lightPos.x, lightPos.y, lightPos.z);
    }

    @Override
    protected void draw() {
        glClear(GL_COLOR_BUFFER_BIT);
        glUseProgram(mProgram);
        glBindVertexArray(mVAO);
        glActiveTexture(GL_TEXTURE1);
        glBindTexture(GL_TEXTURE_2D, textureWood);
        glDrawArrays(GL_TRIANGLES, 0 , 6);
    }

    public static void main(String[] args)  {
        OGLApplicationAbstract application = new AdvancedLight();
        application.run();
    }
}
