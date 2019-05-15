package org.esipeng.opengl.guangzhao.lightcasters;

import org.esipeng.opengl.base.*;
import org.joml.Matrix3d;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import static org.lwjgl.system.MemoryUtil.NULL;
import static org.lwjgl.opengl.GL33.*;
import static org.lwjgl.glfw.GLFW.*;

public class ParallelLight extends OGLApplicationGL33 {
    private static final int MVP_BINDING_POINT = 1;
    private static final int PARAL_LIGHT_BINDING_POINT = 2;

    private int m_width = 800, m_height = 800;
    private long m_window ;

    private int m_vao, m_program;
    private UBOManager m_paralLightUBO, m_modelViewProjectionUBO;
    private Matrix4f m_model, m_projection;
    private Camera m_camera;
    private Vector3f m_lightDir = new Vector3f();
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
        m_window = glfwCreateWindow(m_width, m_height, "Parallel Light", NULL, NULL);
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
                    "LightCasters/vertex.glsl",
                    "LightCasters/fragment.glsl");
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
        glBindBufferBase(GL_UNIFORM_BUFFER,uniformBlockLoc,mvpUBO);


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

        //ParalLight setting
        m_paralLightUBO = new UBOManager();
        int paralLightVBO = getManagedVBO();
        if(!m_paralLightUBO.attachUniformBlock(m_program,"ParalLight", paralLightVBO))
            return false;
        m_paralLightUBO.setValue("pDirection",-0.2f,-1.0f,-0.3f);
        m_paralLightUBO.setValue("pAmbient",0.2f,0.2f,0.2f);
        m_paralLightUBO.setValue("pDiffuse",0.5f,0.5f,0.5f);
        //m_paralLightUBO.setValue("pSpecular", 1.0f,1.0f,1.0f);
        //lightDir should be updated in update, since it is in view space
        glBindBufferBase(GL_UNIFORM_BUFFER,PARAL_LIGHT_BINDING_POINT,paralLightVBO);

        int paralLightBlockLoc = glGetUniformBlockIndex(m_program,"ParalLight");
        glUniformBlockBinding(m_program,paralLightBlockLoc,PARAL_LIGHT_BINDING_POINT);


        m_model = new Matrix4f();
        m_projection = new Matrix4f();
        glEnable(GL_DEPTH_TEST);
        glDepthFunc(GL_LEQUAL);
        m_camera = new Camera(m_window);
        m_camera.enableMouseFpsView();
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
        m_lightDir.set(-0.2f,-1.0f,-0.3f).mul(m_camera.generateViewMat().get3x3(new Matrix3d()));
        m_paralLightUBO.setValue("pDirection",
                m_lightDir.x,
                m_lightDir.y,
                m_lightDir.z);

        glUseProgram(m_program);
        glBindVertexArray(m_vao);
        for(int i = 0; i < m_cubePositions.length; ++i) {
            m_model.identity()
                    .translate(m_cubePositions[i])
                    .rotate((float)Math.toRadians(20.0f * i), m_rotateAxis);
            m_modelViewProjectionUBO.setValue("model",m_model);
            glDrawArrays(GL_TRIANGLES,0,36);
        }
    }

    public static void main(String[] args)  {
        OGLApplicationAbstract application = new ParallelLight();
        application.run();
    }
}
