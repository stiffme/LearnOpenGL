package org.esipeng.opengl.advanced;

import org.esipeng.opengl.base.Camera;
import org.esipeng.opengl.base.OGLApplicationAbstract;
import org.esipeng.opengl.base.OGLApplicationGL33;
import org.esipeng.opengl.base.engine.LightsManager;
import org.esipeng.opengl.base.engine.MVPManager;
import org.esipeng.opengl.base.engine.Scene;
import org.joml.Matrix4f;
import org.joml.Random;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFWVidMode;

import static org.lwjgl.opengl.GL33.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Planet extends OGLApplicationGL33 {
    int mWidth = 1024, mHeight = 1024, mProgram;
    Scene mPlanet, mRock;
    long mWindow;

    MVPManager mvpManager;
    LightsManager lightsManager;

    Matrix4f mProj = new Matrix4f(), mModel = new Matrix4f();
    Camera mCamera;
    Random random = new Random(System.currentTimeMillis());
    Matrix4f[] mRocksModels;
    final int NUM_ROCKS = 100000;

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
        try {
            mProgram = compileAndLinkProgram(
                    "engine/vertex.glsl",
                    "engine/fragment.glsl"
            );

            mPlanet = new Scene(mProgram);
            if(!mPlanet.loadSceneFromResource("Planar/planet/planet.obj"))
                return false;

            if(!mPlanet.bindProgram(mProgram))
                return false;

            mRock = new Scene(mProgram);
            if(!mRock.loadSceneFromResource("Planar/rock/rock.obj"))
                return false;

        } catch (Exception e)   {
            e.printStackTrace();
            return false;
        }

        mvpManager = new MVPManager();
        if(!mvpManager.bindProgram(mProgram))
            return false;

        lightsManager = new LightsManager();
        if(!lightsManager.bindProgram(mProgram))
            return false;

        lightsManager.createDirectionalLight(
                new Vector3f(1.0f),
                new Vector3f(1.0f),
                new Vector3f(1.0f),
                new Vector3f(-1.0f)
        );

        lightsManager.createPointLight(
                new Vector3f(1.0f),
                new Vector3f(1.0f),
                new Vector3f(1.0f),
                new Vector3f(0.f),
                1.f,0.f,0.f
        );

        mCamera = new Camera(0.0f,0.0f,55.f,
                0.0f,0.0f,-1.f,
                0.0f,1.0f,0.0f,
                mWidth/2, mHeight/2,
                mWindow);
        mCamera.enableMouseFpsView();
        glEnable(GL_DEPTH_TEST);
        glDepthFunc(GL_LEQUAL);

        float offset = 5f;
        float radius = 35.f;
        mRocksModels = new Matrix4f[NUM_ROCKS];

        for(int i = 0; i < NUM_ROCKS; ++i)   {
            float angle = (float)Math.PI * 2 * i / NUM_ROCKS;
            float displament = random.nextFloat() * offset;

            float x = (float)Math.sin(angle) * radius + displament;
            displament = random.nextFloat() * offset;

            float y = displament * 0.4f;

            displament = random.nextFloat() * offset;
            float z = (float)Math.cos(angle) * radius + displament;

            Matrix4f m = new Matrix4f();
            float rotAngle = (float) Math.PI * 2 * random.nextFloat();
            float scale = (float)random.nextFloat() * 0.05f + 0.05f;

            m.translate(x ,y ,z).scale(scale).rotate(rotAngle, 0.4f,0.6f, 0.8f);
            mRocksModels[i] = m;
        }

        this.enableFps(true);
        glfwSwapInterval(0);
        return true;
    }

    @Override
    protected void update(float elapsed) {
        mCamera.processInput(elapsed);
        //mvp update
        mvpManager.updateModel(mModel.identity());
        Matrix4f view = mCamera.generateViewMat();
        mvpManager.updateView(view);
        lightsManager.updateAllLights(view);
        mProj.setPerspective(mCamera.getFovRadians(), (float)mWidth/mHeight, 0.1f, 100.f);
        mvpManager.updateProjection(mProj);
        mvpManager.updateNormalInView();
    }

    @Override
    protected void draw() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        mvpManager.updateModel(mModel.identity());
        glUseProgram(mProgram);
        mPlanet.draw();

        for(int i = 0; i < NUM_ROCKS; ++i)  {
            mvpManager.updateModel(mRocksModels[i]);
            mvpManager.updateNormalInView();
            mRock.draw();
        }
        glFinish();
    }

    public static void main(String[] args)  {
        OGLApplicationAbstract application = new Planet();
        application.run();
    }
}
