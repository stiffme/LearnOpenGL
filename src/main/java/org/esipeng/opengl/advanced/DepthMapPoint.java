package org.esipeng.opengl.advanced;

import org.esipeng.opengl.base.Camera;
import org.esipeng.opengl.base.OGLApplicationAbstract;
import org.esipeng.opengl.base.OGLApplicationGL33;
import org.esipeng.opengl.base.TextureLoader;
import org.esipeng.opengl.base.engine.MVPManager;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.lwjgl.glfw.GLFW.glfwCreateWindow;
import static org.lwjgl.glfw.GLFW.glfwMakeContextCurrent;
import static org.lwjgl.opengl.GL33.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class DepthMapPoint extends OGLApplicationGL33 {
    private static final Logger logger = LoggerFactory.getLogger(DepthMapPoint.class);
    private static final int SHADOW_WIDTH = 1024, SHADOW_HEIGHT = 1024;
    int mWidth = 800, mHeight = 600, mPlaneVAO, mQuardVAO, mCubeVAO;
    long mWindow;
    int mSimpleDepthShader, mDebugDepthQuad, mShader;
    Vector3f mLightPos;
    int mWoodTexture, mDepthMapFBO, mTextureDepthMap;
    Camera mCamera;
    MVPManager mvpManager;
    Matrix4f tempMat = new Matrix4f(), lightProjection = new Matrix4f();
    Matrix4f lightView = new Matrix4f(), lightSpaceMatrix = new Matrix4f();
    Vector4f tempVec4 = new Vector4f();
    Vector3f tempVec3 = new Vector3f();

    @Override
    protected boolean applicationCreateContext() {
//        long monitor = glfwGetPrimaryMonitor();
//        GLFWVidMode mode = glfwGetVideoMode(monitor);
//
//        glfwWindowHint(GLFW_RED_BITS, mode.redBits());
//        glfwWindowHint(GLFW_GREEN_BITS, mode.greenBits());
//        glfwWindowHint(GLFW_BLUE_BITS, mode.blueBits());
//        glfwWindowHint(GLFW_REFRESH_RATE, mode.refreshRate());
//
//        mWidth = mode.width();
//        mHeight = mode.height();

        mWindow = glfwCreateWindow(mWidth, mHeight, "DepthMap",NULL, NULL);
        glfwMakeContextCurrent(mWindow);
        return true;
    }

    @Override
    protected boolean applicationInitAfterContext() {
        try {
            mSimpleDepthShader = compileAndLinkProgram(
                    "advanced/depthmap/vShadowMappingDepth.glsl",
                    "advanced/depthmap/fShadowMappingDepth.glsl"
            );
            mvpManager = new MVPManager();
            if(!mvpManager.bindProgram(mSimpleDepthShader))
                return false;

            mDebugDepthQuad = compileAndLinkProgram(
                    "advanced/depthmap/vDebugQuard.glsl",
                    "advanced/depthmap/fDebugQuard.glsl"
            );

            mShader = compileAndLinkProgram(
                    "advanced/depthmap/point/vShadowMapping.glsl",
                    "advanced/depthmap/point/fShadowMapping.glsl"
            );

            if(!mvpManager.bindProgram(mShader))
                return false;

            if(!setUniform1i(mShader,"floorTexture", 2))
                return false;

//            if(!setUniform1i(mShader,"shadowMap", 1))
//                return false;

        } catch (Exception e)   {
            e.printStackTrace();
            return false;
        }

        float[] planeVertices = {
                // Positions          // Normals         // Texture Coords
                25.0f, -0.5f, 25.0f, 0.0f, 1.0f, 0.0f, 25.0f, 0.0f,
                -25.0f, -0.5f, -25.0f, 0.0f, 1.0f, 0.0f, 0.0f, 25.0f,
                -25.0f, -0.5f, 25.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f,

                25.0f, -0.5f, 25.0f, 0.0f, 1.0f, 0.0f, 25.0f, 0.0f,
                25.0f, -0.5f, -25.0f, 0.0f, 1.0f, 0.0f, 25.0f, 25.0f,
                - 25.0f, -0.5f, -25.0f, 0.0f, 1.0f, 0.0f, 0.0f, 25.0f
        };

        //Setup plane VAO
        mPlaneVAO = getManagedVAO();
        int vbo = getManagedVBO();
        glBindVertexArray(mPlaneVAO);
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER,planeVertices,GL_STATIC_DRAW);
        glVertexAttribPointer(0,3,GL_FLOAT,false,Float.BYTES*8,0L);
        glVertexAttribPointer(1,3,GL_FLOAT,false,Float.BYTES*8,Float.BYTES *3);
        glVertexAttribPointer(2,2,GL_FLOAT,false,Float.BYTES*8,Float.BYTES *6);
        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);
        glEnableVertexAttribArray(2);
        glBindBuffer(GL_ARRAY_BUFFER,0);
        glBindVertexArray(0);

        //quard VAO
        float[] quadVertices = {
                // Positions        // Texture Coords
                -1.0f,  1.0f, 0.0f,  0.0f, 1.0f,
                -1.0f, -1.0f, 0.0f,  0.0f, 0.0f,
                1.0f,  1.0f, 0.0f,  1.0f, 1.0f,
                1.0f, -1.0f, 0.0f,  1.0f, 0.0f,
        };
        mQuardVAO = getManagedVAO();
        glBindVertexArray(mQuardVAO);
        vbo = getManagedVBO();
        glBindBuffer(GL_ARRAY_BUFFER,vbo);
        glBufferData(GL_ARRAY_BUFFER,quadVertices,GL_STATIC_DRAW);
        glVertexAttribPointer(0,3,GL_FLOAT,false,Float.BYTES * 5, 0L);
        glVertexAttribPointer(1,2,GL_FLOAT,false,Float.BYTES * 5, Float.BYTES * 3);
        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);

        //cube VAO
        float[] cubeVertices = {
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
        vbo = getManagedVBO();
        glBindVertexArray(mCubeVAO);
        glBindBuffer(GL_ARRAY_BUFFER,vbo);
        glBufferData(GL_ARRAY_BUFFER,cubeVertices,GL_STATIC_DRAW);
        glVertexAttribPointer(0,3,GL_FLOAT,false,Float.BYTES*8, 0L);
        glVertexAttribPointer(1,3,GL_FLOAT,false, Float.BYTES*8, Float.BYTES * 3);
        glVertexAttribPointer(2,2,GL_FLOAT,false,Float.BYTES*8,Float.BYTES * 6);
        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);
        glEnableVertexAttribArray(2);
        glBindBuffer(GL_ARRAY_BUFFER,0);
        glBindVertexArray(0);

        mLightPos = new Vector3f(0f,0f,0f);
        mWoodTexture = loadTextureFromResource("advanced/lighting/wood.png");
        if(mWoodTexture == -1)
            return false;

        //create FBO

        glClearColor(0.1f, 0.1f, 0.1f, 1.0f);

        mCamera = new Camera(
                0.f,0.f,2.f,
                0.f,0.f,-1.f,
                0.f,1.f,0.f,
                mWidth/2, mHeight/2, mWindow
        );

        glEnable(GL_DEPTH_TEST);
        glClearColor(0.1f, 0.1f, 0.1f, 1.0f);
        mCamera.enableMouseFpsView();
        return true;
    }

    protected int loadTextureFromResource(String resoure) {
        TextureLoader loader = new TextureLoader();
        try {
            if(!loader.loadFromResource(resoure))
                return -1;

            int tex = getManagedTexture();
            glBindTexture(GL_TEXTURE_2D, tex);
            glTexImage2D(GL_TEXTURE_2D,0,GL_RGB,loader.getX(),loader.getY(),0,GL_RGB,
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

    @Override
    protected void update(float elapsed) {
        mCamera.processInput(elapsed);

        mvpManager.updateProjection(tempMat.setPerspective(
                mCamera.getFovRadians(),(float)mWidth/mHeight,0.1f,100.f
        ));

        Matrix4f view = mCamera.generateViewMat();
        mvpManager.updateView(view);

        //update light pos
        tempVec4.set(mLightPos,1.0f).mul(view);
        setUniform3f(mShader,"lightPos", tempVec4.x, tempVec4.y, tempVec4.z);
    }

    @Override
    protected void draw() {
        glUseProgram(mShader);
        glActiveTexture(GL_TEXTURE2);
        glBindTexture(GL_TEXTURE_2D,this.mWoodTexture);
        glActiveTexture(GL_TEXTURE1);
        glBindTexture(GL_TEXTURE_2D, this.mTextureDepthMap);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        renderScene(mShader);

    }

    private void renderScene(int program)  {
        //outer
        glDisable(GL_CULL_FACE);
        mvpManager.updateModel(tempMat.identity().scale(7.f));
        setUniform1i(program,"reverseNormal", 1);
        renderCube();

        setUniform1i(program,"reverseNormal", 0);
        glEnable(GL_CULL_FACE);

        mvpManager.updateModel(tempMat.identity().scale(0.5f).translate(4.f,-3.f,0.f));
        renderCube();

        mvpManager.updateModel(tempMat.identity().scale(0.75f).translate(2.0f,3f,1.f));
        renderCube();

        mvpManager.updateModel(tempMat.identity().scale(0.5f).translate(-3.0f,-1f,0.f));
        renderCube();

        mvpManager.updateModel(tempMat.identity().scale(0.5f).translate(-1.5f,1f,1.5f));
        renderCube();

        mvpManager.updateModel(tempMat.identity().scale(0.75f)
                .rotate((float)Math.toRadians(60.f), tempVec3.set(1.0f,0.f,1.f).normalize())
                .translate(-1.5f,2.f,-3.f));
        renderCube();
    }

    private void renderQuard()  {
        glBindVertexArray(mQuardVAO);
        glDrawArrays(GL_TRIANGLE_STRIP,0,4);
        glBindVertexArray(0);
    }

    private void renderCube()   {
        glBindVertexArray(mCubeVAO);
        glDrawArrays(GL_TRIANGLES,0,36);
        glBindVertexArray(0);
    }

    public static void main(String[] args)  {
        OGLApplicationAbstract application = new DepthMapPoint();
        application.run();
    }
}
