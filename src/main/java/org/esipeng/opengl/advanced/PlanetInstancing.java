package org.esipeng.opengl.advanced;

import org.esipeng.opengl.base.Camera;
import org.esipeng.opengl.base.OGLApplicationAbstract;
import org.esipeng.opengl.base.OGLApplicationGL33;
import org.esipeng.opengl.base.engine.*;
import org.joml.Matrix4f;
import org.joml.Random;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.glfw.GLFWVidMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL33.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class PlanetInstancing extends OGLApplicationGL33 {
    private static final Logger logger = LoggerFactory.getLogger(PlanetInstancing.class);
    int mWidth = 1024, mHeight = 1024, mProgram, mProgramRock;
    Scene mPlanet, mRock;
    long mWindow;

    MVPManager mvpManager;
    LightsManager lightsManager;

    Matrix4f mProj = new Matrix4f(), mModel = new Matrix4f();
    Camera mCamera;
    Random random = new Random(System.currentTimeMillis());
    int timeBasedLoc;
    Matrix4f timeBasedRot = new Matrix4f();

    final int NUM_ROCKS = 1000;
    float[] temp = new float[16];

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

            mProgramRock = compileAndLinkProgram(
                    "Planar/vertex.glsl",
                    "Planar/fragment.glsl"
            );


            mPlanet = new Scene(mProgram);
            if(!mPlanet.loadSceneFromResource("Planar/planet/planet.obj"))
                return false;

            if(!mPlanet.bindProgram(mProgram))
                return false;

            if(!mPlanet.bindProgram(mProgramRock))
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

        if(!mvpManager.bindProgram(mProgramRock))
            return false;

        lightsManager = new LightsManager();
        if(!lightsManager.bindProgram(mProgram))
            return false;

        if(!lightsManager.bindProgram(mProgramRock))
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
                new Vector3f(15f,15f,0.f),
                1.f,0.f,0.f
        );

        lightsManager.createSpotLight(
                new Vector3f(0.4f),
                new Vector3f(0.5f),
                new Vector3f(0.5f),
                1.0f,0.00f,0.032f,
                (float)Math.cos(Math.toRadians(12.5f)), (float)Math.cos(Math.toRadians(15.0f)));


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

        int mvbo = getManagedVBO();
        glBindBuffer(GL_ARRAY_BUFFER, mvbo);
        glBufferData(GL_ARRAY_BUFFER,Float.BYTES * 16 * NUM_ROCKS, GL_STATIC_DRAW);

        Vector3f rotateAxis = new Vector3f(0.4f,0.6f, 0.8f).normalize();
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
            float scale = (float)random.nextFloat() * 0.25f + 0.05f;

            m.translate(x ,y ,z).scale(scale).rotate(rotAngle, rotateAxis);
            m.get(temp);
            glBufferSubData(GL_ARRAY_BUFFER, Float.BYTES * 16 * i, temp);
        }
        bindMeshNode(mRock.getRoot());
        glBindVertexArray(0);

        timeBasedLoc = glGetUniformLocation(mProgramRock,"timeBasedRot");
        if(timeBasedLoc == -1)
            return false;

        this.enableFps(true);
        return true;
    }

    private void bindMeshNode(MeshNode meshNode)    {
        for(Mesh mesh : meshNode.getMeshes())   {
            glBindVertexArray(mesh.getmVAO());
            bindNormal();
        }

        for(MeshNode child : meshNode.getChildren())
            bindMeshNode(child);
    }

    private void bindNormal()   {
        glVertexAttribPointer(3,4, GL_FLOAT,false, Float.BYTES * 16, Float.BYTES * 0);
        glVertexAttribPointer(4,4, GL_FLOAT,false, Float.BYTES * 16, Float.BYTES * 4);
        glVertexAttribPointer(5,4, GL_FLOAT,false, Float.BYTES * 16, Float.BYTES * 8);
        glVertexAttribPointer(6,4, GL_FLOAT,false, Float.BYTES * 16, Float.BYTES * 12);
        glEnableVertexAttribArray(3);
        glEnableVertexAttribArray(4);
        glEnableVertexAttribArray(5);
        glEnableVertexAttribArray(6);
        glVertexAttribDivisor(3,1);
        glVertexAttribDivisor(4,1);
        glVertexAttribDivisor(5,1);
        glVertexAttribDivisor(6,1);

    }

    @Override
    protected void update(float elapsed) {
        mCamera.processInput(elapsed);
        //mvp update
        Matrix4f view = mCamera.generateViewMat();
        mvpManager.updateView(view);
        lightsManager.updateAllLights(view);
        mProj.setPerspective(mCamera.getFovRadians(), (float)mWidth/mHeight, 0.1f, 100.f);
        mvpManager.updateProjection(mProj);
        mvpManager.updateNormalInView();

        float angle = (float)Math.toRadians( elapsed * 6.);
        timeBasedRot.rotate(angle,0.f,1.f,0.f);
    }

    @Override
    protected void draw() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        mvpManager.updateModel(mModel.identity());
        glUseProgram(mProgram);
        mPlanet.draw();

        glUseProgram(mProgramRock);
        glUniformMatrix4fv(timeBasedLoc,false, timeBasedRot.get(temp));
        mRock.drawInstanced(NUM_ROCKS);
        glFinish();
    }

    public static void main(String[] args)  {
        OGLApplicationAbstract application = new PlanetInstancing();
        application.run();
    }
}
