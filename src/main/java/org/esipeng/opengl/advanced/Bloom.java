package org.esipeng.opengl.advanced;

import org.esipeng.opengl.base.*;
import org.esipeng.opengl.base.engine.MVPManager;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL33.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Bloom extends OGLApplicationGL33 {
    private static final Logger logger = LoggerFactory.getLogger(Bloom.class);
    private final int SCR_WIDTH = 1280, SCR_HEIGHT = 720;
    private long mWindow;

    private int mShader, mShaderLight, mShaderBlur, mShaderBlurFinal;
    private int mWoodTexture, mContainerTexture;

    private Camera mCamera;
    private MVPManager mvpManager;

    private Vector3f[] mLightPositions, mLightColors;

    private Matrix4f tempMat = new Matrix4f();
    private Vector3f tempVec3f = new Vector3f();
    private int mCubeVAO, mQuadVAO;
    private int mhdrFBO;
    private int[] mColorBuffers, mPingpongFBO, mPingPongTexture;
    private float exposure = 1.0f;
    private int bloom = 0;

    @Override
    protected boolean applicationCreateContext() {
        mWindow = glfwCreateWindow(SCR_WIDTH, SCR_HEIGHT,"Bloom", NULL, NULL);
        if(mWindow == NULL)
            return false;

        glfwMakeContextCurrent(mWindow);
        return true;
    }

    @Override
    protected boolean applicationInitAfterContext() {
        this.enableDebug();
        this.enableFps(true);

        mvpManager = new MVPManager();

        glEnable(GL_DEPTH_TEST);
        try {
            mShader = compileAndLinkProgram(
                    "advanced/bloom/bloom.vs.glsl",
                    "advanced/bloom/bloom.fs.glsl"
            );

            mShaderLight = compileAndLinkProgram(
                    "advanced/bloom/bloom.vs.glsl",
                    "advanced/bloom/light_box.fs.glsl"
            );
            if(!mvpManager.bindProgram(mShader))
                return false;

            if(!mvpManager.bindProgram(mShaderLight))
                return false;

            mShaderBlurFinal = compileAndLinkProgram(
                    "advanced/bloom/bloom_final.vs.glsl",
                    "advanced/bloom/bloom_final.fs.glsl"
            );

            setUniform1i(mShaderBlurFinal, "scene", 0);
            setUniform1i(mShaderBlurFinal, "bloomBlur", 1);
            setUniform1i(mShaderBlurFinal,"bloom",bloom);

            mShaderBlur = compileAndLinkProgram(
                    "advanced/bloom/bloom_final.vs.glsl",
                    "advanced/bloom/blur.fs.glsl"
            );
            setUniform1i(mShaderBlur,"image", 0);
        } catch (Exception e)   {
            e.printStackTrace();
            return false;
        }

        // load textures
        mWoodTexture = loadTextureFromResource("advanced/lighting/wood.png",true);
        mContainerTexture = loadTextureFromResource("LightCasters/container2.png");

        mCamera = new Camera(
                0.f,0.f,5.f,
                0.f,0.f,-1.f,
                0.f,1.f,0.f,
                SCR_WIDTH/2, SCR_HEIGHT/2, mWindow
        );

        mLightPositions = new Vector3f[]    {
                new Vector3f(0.0f,0.5f,1.5f),
                new Vector3f(-4.0f, 0.5f, -3.0f),
                new Vector3f(3.0f, 0.5f,  1.0f),
                new Vector3f(-.8f,  2.4f, -1.0f),
        };

        mLightColors = new Vector3f[]   {
                new Vector3f(5.0f,   5.0f,  5.0f),
                new Vector3f(10.0f,  0.0f,  0.0f),
                new Vector3f(0.0f,   0.0f,  15.0f),
                new Vector3f(0.0f,   5.0f,  0.0f),
        };

        UBOManager mLightSettingsManager = new UBOManager();
        int ubo = getManagedVBO();
        if(!mLightSettingsManager.attachUniformBlock(mShader,"LightSettings", ubo))
            return false;
        glBindBufferBase(GL_UNIFORM_BUFFER, 5, ubo);
        glUniformBlockBinding(mShader, mLightSettingsManager.getBlockIndex(), 5);

        for(int i = 0;  i <  mLightPositions.length; ++i)   {
            String current = String.format("lights[%d]", i);
            mLightSettingsManager.setValue(current + ".Position", mLightPositions[i]);
            mLightSettingsManager.setValue(current + ".Color", mLightColors[i]);
        }
        mLightSettingsManager.setValue("lightNum", mLightPositions.length);

        //create hdrFBO with 2 float point buffers
        mhdrFBO = getManagedFramebuffer();
        glBindFramebuffer(GL_FRAMEBUFFER, mhdrFBO);
        mColorBuffers = new int[2];
        mColorBuffers[0] = getManagedTexture();
        mColorBuffers[1] = getManagedTexture();
        for(int i = 0; i < 2; ++i)  {
            glBindTexture(GL_TEXTURE_2D,mColorBuffers[i]);
            glTexImage2D(GL_TEXTURE_2D,0,GL_RGB16F, SCR_WIDTH, SCR_HEIGHT,0,GL_RGB,GL_FLOAT,NULL);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);  // we clamp to the edge as the blur filter would otherwise sample repeated texture values!
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
            glFramebufferTexture2D(GL_FRAMEBUFFER,GL_COLOR_ATTACHMENT0 + i, GL_TEXTURE_2D, mColorBuffers[i],0);
        }
        int rboDepth = getManagedRenderBuffer();
        glBindRenderbuffer(GL_RENDERBUFFER, rboDepth);
        glRenderbufferStorage(GL_RENDERBUFFER,GL_DEPTH_COMPONENT, SCR_WIDTH, SCR_HEIGHT);
        glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER, rboDepth);
        int[] attachments = new int[] {GL_COLOR_ATTACHMENT0, GL_COLOR_ATTACHMENT1};
        glDrawBuffers(attachments);
        if(glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE)
            return false;
        glBindFramebuffer(GL_FRAMEBUFFER, 0);

        mPingpongFBO = new int[2];
        mPingPongTexture = new int[2];

        mPingPongTexture[0] = getManagedTexture();
        mPingPongTexture[1] = getManagedTexture();
        mPingpongFBO[0] = getManagedFramebuffer();
        mPingpongFBO[1] = getManagedFramebuffer();

        for(int i = 0;  i < 2; ++i) {
            glBindFramebuffer(GL_FRAMEBUFFER, mPingpongFBO[i]);
            glBindTexture(GL_TEXTURE_2D, mPingPongTexture[i]);
            glTexImage2D(GL_TEXTURE_2D,0,GL_RGB16F,SCR_WIDTH,SCR_HEIGHT,0,GL_RGB,GL_FLOAT,NULL);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE); // we clamp to the edge as the blur filter would otherwise sample repeated texture values!
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
            glFramebufferTexture2D(GL_FRAMEBUFFER,GL_COLOR_ATTACHMENT0,GL_TEXTURE_2D,mPingPongTexture[i],0);
            if(glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE)
                return false;
        }
        glBindTexture(GL_TEXTURE_2D, 0);
        glBindFramebuffer(GL_FRAMEBUFFER, 0);

        glfwSetKeyCallback(mWindow, new GLFWKeyCallback() {
            @Override
            public void invoke(long window, int key, int scancode, int action, int mods) {
                if(key == GLFW_KEY_SPACE && action == GLFW_RELEASE)  {
                    if(bloom == 1)
                        bloom = 0;
                    else
                        bloom = 1;

                    setUniform1i(mShaderBlurFinal,"bloom", bloom);
                    logger.debug("Bloom {}", bloom);
                }
            }
        });
        mCamera.enableMouseFpsView();
        return true;
    }

    @Override
    protected void update(float elapsed) {
         mCamera.processInput(elapsed);
         mvpManager.updateView(mCamera.generateViewMat());
         mvpManager.updateProjection(
                 tempMat.identity().setPerspective(mCamera.getFovRadians(), (float)SCR_WIDTH/SCR_HEIGHT,
                         0.1f,100.f)
         );

        if(glfwGetKey(mWindow, GLFW_KEY_Q) == GLFW_PRESS) {
            exposure -= 0.001f;
            if(exposure < 0)
                exposure = 0;
        }

        if(glfwGetKey(mWindow, GLFW_KEY_E) == GLFW_PRESS) {
            exposure += 0.001f;
        }

        setUniform1f(mShaderBlurFinal, "exposure", exposure);
    }

    protected void blur(int incomingTexture, int blurCount) {
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, incomingTexture);
        glUseProgram(mShaderBlur);

        for(int i = 0; i < blurCount; ++i)  {
            glBindFramebuffer(GL_FRAMEBUFFER, mPingpongFBO[0]);
            setUniform1i(mShaderBlur,"horizontal", 0);
            renderQuad();

            glBindFramebuffer(GL_FRAMEBUFFER, mPingpongFBO[1]);
            glBindTexture(GL_TEXTURE_2D, mPingPongTexture[0]);
            setUniform1i(mShaderBlur,"horizontal", 1);
            renderQuad();

            glBindTexture(GL_TEXTURE_2D, mPingPongTexture[1]);
        }
    }

    @Override
    protected void draw() {
        glBindFramebuffer(GL_FRAMEBUFFER, mhdrFBO);
        renderScene();
        glFinish();

        blur(mColorBuffers[1], 5);

        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        glClear(GL_DEPTH_BUFFER_BIT | GL_COLOR_BUFFER_BIT);
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D,mColorBuffers[0]);
        glActiveTexture(GL_TEXTURE1);
        glBindTexture(GL_TEXTURE_2D,mPingPongTexture[1]);
        glUseProgram(mShaderBlurFinal);
        renderQuad();
    }

    private void renderScene()  {
        glUseProgram(mShader);
        glClear(GL_DEPTH_BUFFER_BIT | GL_COLOR_BUFFER_BIT);
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D,mWoodTexture);

        //floor
        tempMat.identity().translate(0.0f,-1.f,0.f)
                .scale(12.5f,0.5f,12.5f);
        mvpManager.updateModel(tempMat);
        renderCube();
//
//        //cubes
        glBindTexture(GL_TEXTURE_2D, mContainerTexture);
        tempMat.identity().translate(0.0f, 1.5f, 0.0f)
                .scale(0.5f);
        mvpManager.updateModel(tempMat);
        renderCube();

        tempMat.identity().translate(2.0f, 0.0f, 1.0f)
                .scale(0.5f);
        mvpManager.updateModel(tempMat);
        renderCube();

        tempMat.identity().translate(-1.0f, -1.0f, 2.0f)
                .rotate((float)Math.toRadians(60.f), tempVec3f.set(1.0f,0.0f,1.0f).normalize());
        mvpManager.updateModel(tempMat);
        renderCube();

        tempMat.identity().translate(0.0f, 2.7f, 4.0f)
                .rotate((float)Math.toRadians(23.f), tempVec3f.set(1.0f,0.0f,1.0f).normalize())
                .scale(1.25f);
        mvpManager.updateModel(tempMat);
        renderCube();

        tempMat.identity().translate(-2.0f, 1.0f, -3.0f)
                .rotate((float)Math.toRadians(124.f), tempVec3f.set(1.0f,0.0f,1.0f).normalize());
        mvpManager.updateModel(tempMat);
        renderCube();

        tempMat.identity().translate(-3.0f, 0.0f, 0.0f)
                .scale(0.5f);
        mvpManager.updateModel(tempMat);
        renderCube();

        //draw light
        glUseProgram(mShaderLight);
        for(int i = 0; i < mLightPositions.length; ++i) {
            tempMat.identity().translate(mLightPositions[i]).scale(0.25f);
            mvpManager.updateModel(tempMat);
            setUniform3f(mShaderLight,"lightColor", mLightColors[i].x, mLightColors[i].y, mLightColors[i].z);
            renderCube();
        }
    }

    protected int loadTextureFromResource(String resource)  {
        return loadTextureFromResource(resource, false);
    }

    protected int loadTextureFromResource(String resoure, boolean gammaCorrection) {
        TextureLoader loader = new TextureLoader();
        try {
            if(!loader.loadFromResource(resoure))
                return -1;

            int dataFormat;
            int internalFormat;
            switch (loader.getNrChannel())  {
                case 1:
                    dataFormat = GL_RED;
                    internalFormat = GL_RED;
                    break;
                case 3:
                    internalFormat = gammaCorrection? GL_SRGB : GL_RGB;
                    dataFormat = GL_RGB;
                    break;
                default:
                case 4:
                    internalFormat = gammaCorrection ? GL_SRGB_ALPHA : GL_RGBA;
                    dataFormat =  GL_RGBA;
                    break;
            }
            int tex = getManagedTexture();
            glBindTexture(GL_TEXTURE_2D, tex);
            glTexImage2D(GL_TEXTURE_2D,0,internalFormat,loader.getX(),loader.getY(),0,dataFormat,
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
        if (mQuadVAO == 0)
        {
            float quadVertices[] = {
                    // positions        // texture Coords
                    -1.0f,  1.0f, 0.0f, 0.0f, 1.0f,
                    -1.0f, -1.0f, 0.0f, 0.0f, 0.0f,
                    1.0f,  1.0f, 0.0f, 1.0f, 1.0f,
                    1.0f, -1.0f, 0.0f, 1.0f, 0.0f,
            };
            // setup plane VAO
            mQuadVAO = getManagedVAO();
            int quadVBO = getManagedVBO();
            glBindVertexArray(mQuadVAO);
            glBindBuffer(GL_ARRAY_BUFFER, quadVBO);
            glBufferData(GL_ARRAY_BUFFER, quadVertices, GL_STATIC_DRAW);
            glEnableVertexAttribArray(0);
            glVertexAttribPointer(0, 3, GL_FLOAT, false, 5 * Float.BYTES, 0L);
            glEnableVertexAttribArray(1);
            glVertexAttribPointer(1, 2, GL_FLOAT, false, 5 * Float.BYTES, Float.BYTES * 3);
        }
        glBindVertexArray(mQuadVAO);
        glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);
        glBindVertexArray(0);
    }

    public static void main(String[] args)  {
        OGLApplicationAbstract application = new Bloom();
        application.run();
    }
}
