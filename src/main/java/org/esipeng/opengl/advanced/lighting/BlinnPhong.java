package org.esipeng.opengl.advanced.lighting;

import org.esipeng.opengl.base.Camera;
import org.esipeng.opengl.base.OGLApplicationAbstract;
import org.esipeng.opengl.base.OGLApplicationGL33;
import org.esipeng.opengl.base.TextureLoader;
import org.esipeng.opengl.base.engine.MVPManager;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.GLFW_REFRESH_RATE;
import static org.lwjgl.opengl.GL33.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class BlinnPhong extends OGLApplicationGL33 {
    private static final Logger logger = LoggerFactory.getLogger(BlinnPhong.class);
    int mWidth, mHeight, mVAO, textureFloor, mProgram;
    long mWindow;
    MVPManager mvpManager;
    Camera mCamera;
    Matrix4f tempMat;
    Vector4f lightPos;
    int blinn = 0;

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
                    "advanced/lighting/vShader.glsl",
                    "advanced/lighting/fShader.glsl");

        } catch (Exception e)   {
            e.printStackTrace();
            return false;
        }

        mVAO = getManagedVAO();
        glBindVertexArray(mVAO);
        int vbo  = getManagedVBO();
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, planeVertices, GL_STATIC_DRAW);

        glVertexAttribPointer(0,3,GL_FLOAT,false,Float.BYTES * 8, 0);
        glVertexAttribPointer(1,3,GL_FLOAT,false,Float.BYTES * 8, Float.BYTES * 3);
        glVertexAttribPointer(2,2,GL_FLOAT,false,Float.BYTES * 8, Float.BYTES * 6);
        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);
        glEnableVertexAttribArray(2);

        glBindBuffer(GL_ARRAY_BUFFER,0);
        glBindVertexArray(0);

        TextureLoader textureLoader = new TextureLoader();
        try {
            if(!textureLoader.loadFromResource("advanced/lighting/wood.png"))
                return false;
            textureFloor = getManagedTexture();
            glBindTexture(GL_TEXTURE_2D, textureFloor);
            glTexImage2D(GL_TEXTURE_2D,0,GL_RGB,textureLoader.getX(),textureLoader.getY(),0,GL_RGB,GL_UNSIGNED_BYTE,textureLoader.getData());
            glGenerateMipmap(GL_TEXTURE_2D);
            glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_WRAP_S,GL_REPEAT);
            glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_WRAP_T,GL_REPEAT);
            glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_MAG_FILTER,GL_LINEAR);
            glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_MIN_FILTER,GL_LINEAR_MIPMAP_LINEAR);
            glBindTexture(GL_TEXTURE_2D, 0);
        } catch (Exception e)   {
            e.printStackTrace();
            return false;
        } finally {
            textureLoader.release();
        }

        mCamera = new Camera(
                0.f,0.f,3.f,
                0.f,0.f,-1.f,
                0.f,1.f,0.f,
                mWidth/2, mHeight/2,mWindow
        );
        mCamera.enableMouseFpsView();

        mvpManager = new MVPManager();
        if(!mvpManager.bindProgram(mProgram))
            return false;

        if(!setUniform1i(mProgram,"floorTexture",5))
            return false;

        tempMat = new Matrix4f();
        lightPos = new Vector4f();

        glfwSetKeyCallback(mWindow, new GLFWKeyCallback() {
            @Override
            public void invoke(long window, int key, int scancode, int action, int mods) {
                if(key == GLFW_KEY_B && action == GLFW_RELEASE) {
                    if(blinn == 0)
                        blinn = 1;
                    else
                        blinn = 0;

                    if(blinn == 0)
                        logger.debug("Phuong render!");
                    else
                        logger.debug("Blinn render!");
                }
            }
        });

        glEnable(GL_DEPTH_TEST);
        return true;
    }

    @Override
    protected void update(float elapsed) {
        mCamera.processInput(elapsed);
        mvpManager.updateModel(tempMat.identity());

        mvpManager.updateView(mCamera.generateViewMat());
        mvpManager.updateProjection(tempMat.setPerspective(mCamera.getFovRadians(),
                (float)mWidth/mHeight,0.1f,100.f));
        mvpManager.updateNormalInView();

        lightPos.set(0.f,0.f,0.f,1.f);
        lightPos.mul(mCamera.generateViewMat());


        setUniform1i(mProgram,"blinn", blinn);
        setUniform3f(mProgram,"lightPos", lightPos.x, lightPos.y, lightPos.z );
    }

    @Override
    protected void draw() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glUseProgram(mProgram);
        glBindVertexArray(mVAO);
        glActiveTexture(GL_TEXTURE5);
        glBindTexture(GL_TEXTURE_2D, textureFloor);
        glDrawArrays(GL_TRIANGLES,0,6);
        glFinish();
    }

    public static void main(String[] args)  {
        OGLApplicationAbstract application = new BlinnPhong();
        application.run();
    }
}
