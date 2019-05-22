package org.esipeng.opengl.advanced;

import org.esipeng.opengl.base.*;
import org.esipeng.opengl.base.engine.LightsManager;
import org.esipeng.opengl.base.engine.Scene;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import static org.lwjgl.opengl.GL33.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.MemoryUtil.NULL;
import static org.lwjgl.stb.STBImage.*;
import static org.esipeng.opengl.base.engine.Const.*;

public class SkyBoxTesting extends OGLApplicationGL33 {
    private static final int MVP_BINDING_POINT = 2;
    private static final int SKYBOX_TEXTURE_ID = 30;
    int mWidth = 800, mHeight = 800;
    long mWindow;
    int mProgramSkybox, mSkyboxVAO, mProgramReflect;
    int mSkyboxTexture;
    Camera mCamera;
    Matrix4f mModel, mProjection;
    UBOManager mvpManager;
    Scene mScene;
    LightsManager lightsManager;

    @Override
    protected boolean applicationCreateContext() {
        mWindow = glfwCreateWindow(mWidth, mHeight, "Sky Box", NULL, NULL);
        if(mWindow == NULL)
            return false;
        glfwMakeContextCurrent(mWindow);
        return true;
    }

    @Override
    protected boolean applicationInitAfterContext() {
        mSkyboxTexture = loadCubeTexture(new String[]{
                "advanced/skybox/right.jpg",
                "advanced/skybox/left.jpg",
                "advanced/skybox/top.jpg",
                "advanced/skybox/bottom.jpg",
                "advanced/skybox/front.jpg",
                "advanced/skybox/back.jpg"
        });

        if(mSkyboxTexture == 0)
            return false;

        try{
            mProgramSkybox = compileAndLinkProgram(
                    "advanced/skybox/vSkybox.glsl",
                    "advanced/skybox/fSkybox.glsl"
            );

            mProgramReflect = compileAndLinkProgram(
                    "advanced/skybox/vEngine.glsl",
                    "advanced/skybox/fReflect.glsl"
            );

            mScene = new Scene(mProgramReflect);
            if(!mScene.loadSceneFromResource("nanosuit/nanosuit.obj"))
                return false;

            if(!mScene.bindProgram(mProgramReflect))
                return false;

        } catch (Exception e)   {
            e.printStackTrace();
            return false;
        }

        float skyboxVertices[] = {
                // positions
                -1.0f,  1.0f, -1.0f,
                -1.0f, -1.0f, -1.0f,
                1.0f, -1.0f, -1.0f,
                1.0f, -1.0f, -1.0f,
                1.0f,  1.0f, -1.0f,
                -1.0f,  1.0f, -1.0f,

                -1.0f, -1.0f,  1.0f,
                -1.0f, -1.0f, -1.0f,
                -1.0f,  1.0f, -1.0f,
                -1.0f,  1.0f, -1.0f,
                -1.0f,  1.0f,  1.0f,
                -1.0f, -1.0f,  1.0f,

                1.0f, -1.0f, -1.0f,
                1.0f, -1.0f,  1.0f,
                1.0f,  1.0f,  1.0f,
                1.0f,  1.0f,  1.0f,
                1.0f,  1.0f, -1.0f,
                1.0f, -1.0f, -1.0f,

                -1.0f, -1.0f,  1.0f,
                -1.0f,  1.0f,  1.0f,
                1.0f,  1.0f,  1.0f,
                1.0f,  1.0f,  1.0f,
                1.0f, -1.0f,  1.0f,
                -1.0f, -1.0f,  1.0f,

                -1.0f,  1.0f, -1.0f,
                1.0f,  1.0f, -1.0f,
                1.0f,  1.0f,  1.0f,
                1.0f,  1.0f,  1.0f,
                -1.0f,  1.0f,  1.0f,
                -1.0f,  1.0f, -1.0f,

                -1.0f, -1.0f, -1.0f,
                -1.0f, -1.0f,  1.0f,
                1.0f, -1.0f, -1.0f,
                1.0f, -1.0f, -1.0f,
                -1.0f, -1.0f,  1.0f,
                1.0f, -1.0f,  1.0f
        };

        mSkyboxVAO = getManagedVAO();
        glBindVertexArray(mSkyboxVAO);
        int vbo = getManagedVBO();
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, skyboxVertices, GL_STATIC_DRAW);
        glVertexAttribPointer(0,3,GL_FLOAT,false,0,0L);
        glEnableVertexAttribArray(0);
        glBindBuffer(GL_ARRAY_BUFFER,0);
        glBindVertexArray(0);

        if(!setUniform1i(mProgramSkybox, "skybox", SKYBOX_TEXTURE_ID))
            return false;

        mCamera = new Camera(mWindow);
        mModel = new Matrix4f();
        mProjection = new Matrix4f();

        mvpManager = new UBOManager();
        int ubo = getManagedVBO();
        if(!mvpManager.attachUniformBlock(mProgramSkybox,"mvp",ubo))
            return false;
        glBindBufferBase(GL_UNIFORM_BUFFER, MVP_BINDING_POINT, ubo);
        glUniformBlockBinding(mProgramSkybox, mvpManager.getBlockIndex(), MVP_BINDING_POINT);

        int mvpLoc = glGetUniformBlockIndex(mProgramReflect,"mvp");
        if(mvpLoc == -1)
            return false;
        glUniformBlockBinding(mProgramReflect,mvpLoc, MVP_BINDING_POINT);

        lightsManager = new LightsManager();
        if(!lightsManager.init(mProgramReflect))
            return false;

        lightsManager.createDirectionalLight(
                new Vector3f(0.4f),
                new Vector3f(1.0f),
                new Vector3f(1.0f),
                new Vector3f(1.f,0.f,0.f));

        lightsManager.createPointLight(
                new Vector3f(0.4f),
                new Vector3f(1.0f),
                new Vector3f(1.0f),
                new Vector3f(0.f,0.f,8.f),
                1.0f, 0.0f,0.0f);

        lightsManager.createSpotLight(
                new Vector3f(0.4f),
                new Vector3f(1.0f),
                new Vector3f(1.0f),
                1.0f,0.0f,0.0f,
                (float)Math.cos(Math.toRadians(12.5f)), (float)Math.cos(Math.toRadians(15.0f)));

        mCamera.enableMouseFpsView();
        glEnable(GL_DEPTH_TEST);
        glDepthFunc(GL_LEQUAL);
        return true;
    }

    @Override
    protected void update(float elapsed) {
        mCamera.processInput(elapsed);

    }

    @Override
    protected void draw() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glDepthMask(false);
        mModel.identity();
        mProjection.setPerspective(mCamera.getFovRadians(),(float)mWidth/mHeight, 0.1f, 100.0f);
        Matrix4f view = mCamera.generateViewMat();
        mvpManager.setValue("model", mModel);
        mvpManager.setValue("view", view);
        lightsManager.updateAllLights(view);
        mvpManager.setValue("projection", mProjection);


        glUseProgram(mProgramSkybox);
        glActiveTexture(GL_TEXTURE0 + SKYBOX_TEXTURE_ID );
        glBindTexture(GL_TEXTURE_CUBE_MAP, mSkyboxTexture);
        glBindVertexArray(mSkyboxVAO);
        glDrawArrays(GL_TRIANGLES, 0, 36);
        glBindVertexArray(0);

        mModel.identity().translate(0.f,-1.75f, 0.f).scale(0.2f);
        mvpManager.setValue("model", mModel);
        mvpManager.setValue("normalMatrix", view.mul(mModel).invert().transpose());
        glUseProgram(mProgramReflect);
        glDepthMask(true);
        mScene.draw();
    }

    private int loadCubeTexture(String[] faces) {
        int cubeTexture = getManagedTexture();
        glBindTexture(GL_TEXTURE_CUBE_MAP, cubeTexture);
        TextureLoader loader = new TextureLoader();
        try {
            for(int i = 0; i < faces.length; ++i)  {
                if(!loader.loadFromResource(faces[i]))
                    return 0;

                int type ;
                switch (loader.getNrChannel())  {
                    case 4:
                        type = GL_RGBA;
                        break;
                    case 3:
                    default:
                        type = GL_RGB;
                        break;
                }
                glTexImage2D(GL_TEXTURE_CUBE_MAP_POSITIVE_X + i,
                        0, type, loader.getX(), loader.getY(), 0,type,
                        GL_UNSIGNED_BYTE, loader.getData());
            }

            glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
            glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
            glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
            glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_R, GL_CLAMP_TO_EDGE);

        } catch (Exception e)   {
            e.printStackTrace();
            return 0;
        }finally {
            glBindTexture(GL_TEXTURE_CUBE_MAP, 0);
            loader.release();
        }
        return cubeTexture;
    }

    public static void main(String[] args)  {
        OGLApplicationAbstract applicationAbstract = new SkyBoxTesting();
        applicationAbstract.run();
    }
}
