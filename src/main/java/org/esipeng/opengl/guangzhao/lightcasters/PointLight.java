package org.esipeng.opengl.guangzhao.lightcasters;

import org.esipeng.opengl.base.*;
import org.joml.Matrix3d;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import static org.lwjgl.glfw.GLFW.glfwCreateWindow;
import static org.lwjgl.glfw.GLFW.glfwMakeContextCurrent;
import static org.lwjgl.opengl.GL33.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class PointLight extends OGLApplicationGL33 {
    private static final int MVP_BINDING_POINT = 1;
    private static final int POINT_LIGHT_BINDING_POINT = 2;

    private int m_width = 800, m_height = 800;
    private long m_window ;

    private int m_vao, m_program, m_programLight;
    private UBOManager m_pointLightUBO, m_modelViewProjectionUBO;
    private Matrix4f m_model, m_projection;
    private Camera m_camera;
    private Vector4f m_lightPos = new Vector4f();
    private Vector3f m_rotateAxis = new Vector3f(1.0f,0.3f,0.5f).normalize();

    private Vector3f[] m_cubePositions = {
            new Vector3f( 0.0f,  0.0f,  0.0f),
            new Vector3f( 2.0f,  5.0f, -15.0f),
            new Vector3f(-1.5f, -2.2f, -2.5f),
            new Vector3f(-3.8f, -2.0f, -12.3f),
            new Vector3f( 2.4f, -0.4f, -3.5f),
            new Vector3f(-1.7f,  3.0f, -7.5f),
            new Vector3f( 1.3f, -2.0f, -2.5f),
            new Vector3f( 1.5f,  2.0f, -2.5f),
            new Vector3f( 1.5f,  0.2f, -1.5f),
            new Vector3f(-1.3f,  1.0f, -1.5f)
    };

    @Override
    protected boolean applicationCreateContext() {
        m_window = glfwCreateWindow(m_width, m_height, "Point Light", NULL, NULL);
        if(m_window == NULL)
            return false;
        glfwMakeContextCurrent(m_window);
        return true;
    }

    @Override
    protected boolean applicationInitAfterContext() {

        float[] vertices = {
                // positions          // normals           // texture coords
                -0.5f, -0.5f, -0.5f,  0.0f,  0.0f, -1.0f,  0.0f, 0.0f,
                0.5f, -0.5f, -0.5f,  0.0f,  0.0f, -1.0f,  1.0f, 0.0f,
                0.5f,  0.5f, -0.5f,  0.0f,  0.0f, -1.0f,  1.0f, 1.0f,
                0.5f,  0.5f, -0.5f,  0.0f,  0.0f, -1.0f,  1.0f, 1.0f,
                -0.5f,  0.5f, -0.5f,  0.0f,  0.0f, -1.0f,  0.0f, 1.0f,
                -0.5f, -0.5f, -0.5f,  0.0f,  0.0f, -1.0f,  0.0f, 0.0f,

                -0.5f, -0.5f,  0.5f,  0.0f,  0.0f, 1.0f,   0.0f, 0.0f,
                0.5f, -0.5f,  0.5f,  0.0f,  0.0f, 1.0f,   1.0f, 0.0f,
                0.5f,  0.5f,  0.5f,  0.0f,  0.0f, 1.0f,   1.0f, 1.0f,
                0.5f,  0.5f,  0.5f,  0.0f,  0.0f, 1.0f,   1.0f, 1.0f,
                -0.5f,  0.5f,  0.5f,  0.0f,  0.0f, 1.0f,   0.0f, 1.0f,
                -0.5f, -0.5f,  0.5f,  0.0f,  0.0f, 1.0f,   0.0f, 0.0f,

                -0.5f,  0.5f,  0.5f, -1.0f,  0.0f,  0.0f,  1.0f, 0.0f,
                -0.5f,  0.5f, -0.5f, -1.0f,  0.0f,  0.0f,  1.0f, 1.0f,
                -0.5f, -0.5f, -0.5f, -1.0f,  0.0f,  0.0f,  0.0f, 1.0f,
                -0.5f, -0.5f, -0.5f, -1.0f,  0.0f,  0.0f,  0.0f, 1.0f,
                -0.5f, -0.5f,  0.5f, -1.0f,  0.0f,  0.0f,  0.0f, 0.0f,
                -0.5f,  0.5f,  0.5f, -1.0f,  0.0f,  0.0f,  1.0f, 0.0f,

                0.5f,  0.5f,  0.5f,  1.0f,  0.0f,  0.0f,  1.0f, 0.0f,
                0.5f,  0.5f, -0.5f,  1.0f,  0.0f,  0.0f,  1.0f, 1.0f,
                0.5f, -0.5f, -0.5f,  1.0f,  0.0f,  0.0f,  0.0f, 1.0f,
                0.5f, -0.5f, -0.5f,  1.0f,  0.0f,  0.0f,  0.0f, 1.0f,
                0.5f, -0.5f,  0.5f,  1.0f,  0.0f,  0.0f,  0.0f, 0.0f,
                0.5f,  0.5f,  0.5f,  1.0f,  0.0f,  0.0f,  1.0f, 0.0f,

                -0.5f, -0.5f, -0.5f,  0.0f, -1.0f,  0.0f,  0.0f, 1.0f,
                0.5f, -0.5f, -0.5f,  0.0f, -1.0f,  0.0f,  1.0f, 1.0f,
                0.5f, -0.5f,  0.5f,  0.0f, -1.0f,  0.0f,  1.0f, 0.0f,
                0.5f, -0.5f,  0.5f,  0.0f, -1.0f,  0.0f,  1.0f, 0.0f,
                -0.5f, -0.5f,  0.5f,  0.0f, -1.0f,  0.0f,  0.0f, 0.0f,
                -0.5f, -0.5f, -0.5f,  0.0f, -1.0f,  0.0f,  0.0f, 1.0f,

                -0.5f,  0.5f, -0.5f,  0.0f,  1.0f,  0.0f,  0.0f, 1.0f,
                0.5f,  0.5f, -0.5f,  0.0f,  1.0f,  0.0f,  1.0f, 1.0f,
                0.5f,  0.5f,  0.5f,  0.0f,  1.0f,  0.0f,  1.0f, 0.0f,
                0.5f,  0.5f,  0.5f,  0.0f,  1.0f,  0.0f,  1.0f, 0.0f,
                -0.5f,  0.5f,  0.5f,  0.0f,  1.0f,  0.0f,  0.0f, 0.0f,
                -0.5f,  0.5f, -0.5f,  0.0f,  1.0f,  0.0f,  0.0f, 1.0f
        };

        try {
            m_program = compileAndLinkProgram(
                    "LightCasters/PointLight/vertex.glsl",
                    "LightCasters/PointLight/fragment.glsl");

            m_programLight = compileAndLinkProgram(
                    "LightCasters/PointLight/vertex.glsl",
                    "LightCasters/PointLight/fragmentLight.glsl");

        } catch (Exception e)   {
            e.printStackTrace();
            return false;
        }

        int vertexVBO = getManagedVBO();
        glBindBuffer(GL_ARRAY_BUFFER, vertexVBO);
        glBufferData(GL_ARRAY_BUFFER,vertices,GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);

        m_vao = getManagedVAO();
        glBindVertexArray(m_vao);
        glBindBuffer(GL_ARRAY_BUFFER, vertexVBO);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(0,3,GL_FLOAT,false,Float.BYTES * 8, Float.BYTES * 0);
        glEnableVertexAttribArray(1);
        glVertexAttribPointer(1,3,GL_FLOAT,false,Float.BYTES * 8,Float.BYTES * 3);
        glEnableVertexAttribArray(2);
        glVertexAttribPointer(2,2,GL_FLOAT,false,Float.BYTES * 8,Float.BYTES * 6);
        glBindBuffer(GL_ARRAY_BUFFER,0);
        glBindVertexArray(0);

        //MVP uniform
        int mvpUBO = getManagedVBO();
        m_modelViewProjectionUBO = new UBOManager();
        if(!m_modelViewProjectionUBO.attachUniformBlock(
                m_program,
                "ModelViewProjection",
                mvpUBO))
            return false;
        //bind the UBO to the binding point
        int uniformBlockLoc = glGetUniformBlockIndex(m_program,"ModelViewProjection");
        if(uniformBlockLoc == -1)
            return false;
        glUniformBlockBinding(m_program,uniformBlockLoc, MVP_BINDING_POINT);
        glBindBufferBase(GL_UNIFORM_BUFFER,MVP_BINDING_POINT, mvpUBO);

        //bind the UBO to the light program
        uniformBlockLoc = glGetUniformBlockIndex(m_programLight, "ModelViewProjection");
        if(uniformBlockLoc == -1)
            return false;
        glUniformBlockBinding(m_programLight,uniformBlockLoc, MVP_BINDING_POINT);


        //load texture
        try{
            TextureLoader textureLoader = new TextureLoader();
            if(!textureLoader.loadFromResource("LightCasters/container2.png"))
                return false;

            int textureDiffuse = getManagedTexture();
            glBindTexture(GL_TEXTURE_2D, textureDiffuse);
            glTexImage2D(GL_TEXTURE_2D,
                    0,
                    GL_RGBA,
                    textureLoader.getX(),
                    textureLoader.getY(),
                    0,
                    GL_RGBA,
                    GL_UNSIGNED_BYTE,
                    textureLoader.getData());
            glGenerateMipmap(GL_TEXTURE_2D);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);


            if(!textureLoader.loadFromResource("LightCasters/container2_specular.png"))
                return false;
            int textureSpecular = getManagedTexture();
            glBindTexture(GL_TEXTURE_2D, textureSpecular);
            glTexImage2D(GL_TEXTURE_2D,
                    0,
                    GL_RGBA,
                    textureLoader.getX(),
                    textureLoader.getY(),
                    0,
                    GL_RGBA,
                    GL_UNSIGNED_BYTE,
                    textureLoader.getData());
            glGenerateMipmap(GL_TEXTURE_2D);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
            glBindTexture(GL_TEXTURE_2D,0);
            textureLoader.release();

            //bind textures
            glActiveTexture(GL_TEXTURE0);
            glBindTexture(GL_TEXTURE_2D,textureDiffuse);
            glActiveTexture(GL_TEXTURE1);
            glBindTexture(GL_TEXTURE_2D,textureSpecular);

            glUseProgram(m_program);
            int sampler2d = glGetUniformLocation(m_program,"mDiffuse");
            if(sampler2d == -1)
                return false;
            glUniform1i(sampler2d,0);
            sampler2d = glGetUniformLocation(m_program,"mSpecular");
            if(sampler2d == -1)
                return false;
            glUniform1i(sampler2d,1);

        } catch (Exception e)   {
            e.printStackTrace();
            return false;
        }

        //Point Light setting
        m_pointLightUBO = new UBOManager();
        int paralLightVBO = getManagedVBO();
        if(!m_pointLightUBO.attachUniformBlock(m_program,"PointLight", paralLightVBO))
            return false;
        m_pointLightUBO.setValue("pLightPos",-0.2f,-1.0f,-0.3f);
        m_pointLightUBO.setValue("pAmbient",0.2f,0.2f,0.2f);
        m_pointLightUBO.setValue("pDiffuse",0.5f,0.5f,0.5f);
        m_pointLightUBO.setValue("pSpecular", 1.0f,1.0f,1.0f);
        m_pointLightUBO.setValue("pConstant", 1.0f);
        m_pointLightUBO.setValue("pLinear", 0.045f);
        m_pointLightUBO.setValue("pQuadratic", 0.0075f);

        //lightDir should be updated in update, since it is in view space
        glBindBufferBase(GL_UNIFORM_BUFFER, POINT_LIGHT_BINDING_POINT,paralLightVBO);

        int pointLightLoc = glGetUniformBlockIndex(m_program,"PointLight");
        if(pointLightLoc == -1)
            return false;
        glUniformBlockBinding(m_program,pointLightLoc, POINT_LIGHT_BINDING_POINT);


        m_model = new Matrix4f();
        m_projection = new Matrix4f();
        glEnable(GL_DEPTH_TEST);
        glDepthFunc(GL_LEQUAL);
        m_camera = new Camera(m_window);
        m_camera.enableMouseFpsView();
        glClearColor(0.1f, 0.1f, 0.1f, 1.0f);
        return true;
    }

    @Override
    protected void update(float elapsed) {
        m_camera.processInput(elapsed);
    }

    @Override
    protected void draw() {
        glClear(GL_COLOR_BUFFER_BIT|GL_DEPTH_BUFFER_BIT);
        m_projection.identity().setPerspective(
                m_camera.getFovRadians(), (float)m_width/m_height,0.1f,100.0f
        );
        m_modelViewProjectionUBO.setValue("projection", m_projection);
        m_modelViewProjectionUBO.setValue("view", m_camera.generateViewMat());

        //calculate pDirection
        //m_lightPos.set(0.f,0.f,0.f,1.f)(-0.2f,-1.0f,-0.3f).mul(m_camera.generateViewMat().get3x3(new Matrix3d()));
        Matrix4f moveLight = new Matrix4f().translate(1.2f, 1.0f,2.0f);
        m_lightPos.set(0.f,0.f,0.f,1.f)
                .mul(moveLight)
                .mul(m_camera.generateViewMat());
        m_pointLightUBO.setValue("pLightPos", //light pos in view space
                m_lightPos.x,
                m_lightPos.y,
                m_lightPos.z);

        glUseProgram(m_program);
        glBindVertexArray(m_vao);
        for(int i = 0; i < m_cubePositions.length; ++i) {
            m_model.identity()
                    .translate(m_cubePositions[i])
                    .rotate((float)Math.toRadians(20.0f * i), m_rotateAxis);
            m_modelViewProjectionUBO.setValue("model",m_model);
            glDrawArrays(GL_TRIANGLES,0,36);
        }

        //draw light
        moveLight.identity().translate(1.2f, 1.0f,2.0f).scale(0.2f);
        m_modelViewProjectionUBO.setValue("model", moveLight);
        glUseProgram(m_programLight);
        glDrawArrays(GL_TRIANGLES,0,36);

        glFinish();
    }

    public static void main(String[] args)  {
        OGLApplicationAbstract application = new PointLight();
        application.run();
    }
}
