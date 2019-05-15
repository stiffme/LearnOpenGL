package org.esipeng.opengl.guangzhao;

import org.esipeng.opengl.base.*;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import static org.lwjgl.glfw.GLFW.glfwCreateWindow;
import static org.lwjgl.glfw.GLFW.glfwMakeContextCurrent;
import static org.lwjgl.opengl.GL33.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class MultipleLights extends OGLApplicationGL33 {
    private static final int MVP_BINDING_POINT = 1;
    private static final int DIR_LIGHT_BINDING_POINT = 2;

    private int m_width = 800, m_height = 800;
    private long m_window ;

    private int m_vao, m_program, m_programLight;
    private UBOManager m_modelViewProjectionUBO, m_dirLightUBO;
    private Matrix4f m_model, m_projection;
    private Camera m_camera;
    private Vector3f m_rotateAxis = new Vector3f(1.0f,0.3f,0.5f).normalize();
    private Vector4f m_dirLightDirection = new Vector4f();

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

    private Vector3f[] m_pointLightPositions = {
            new Vector3f( 0.7f,  0.2f,  2.0f),
            new Vector3f( 2.3f, -3.3f, -4.0f),
            new Vector3f(-4.0f,  2.0f, -12.0f),
            new Vector3f( 0.0f,  0.0f, -3.0f)
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
                    "LightCasters/MultipleLights/vertex.glsl",
                    "LightCasters/MultipleLights/fragment.glsl");

            m_programLight = compileAndLinkProgram(
                    "LightCasters/MultipleLights/vertex.glsl",
                    "LightCasters/MultipleLights/fragmentLight.glsl");

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

            //set material
            glUseProgram(m_program);
            int sampler2d = glGetUniformLocation(m_program,"material.diffuse");
            if(sampler2d == -1)
                return false;
            glUniform1i(sampler2d,0);
            sampler2d = glGetUniformLocation(m_program,"material.specular");
            if(sampler2d == -1)
                return false;
            glUniform1i(sampler2d,1);

            int materialShiningness = glGetUniformLocation(m_program,"material.shininess");
            if(materialShiningness == -1)
                return false;
            glUniform1f(materialShiningness, 32.0f);

            //create UBO for DirLight
            int dirLightVBO = getManagedVBO();
            m_dirLightUBO = new UBOManager();
            if(!m_dirLightUBO.attachUniformBlock(m_program,"DirLight", dirLightVBO))
                return false;
            //bind the VBO to the DIR binding point
            glBindBufferBase(GL_UNIFORM_BUFFER,DIR_LIGHT_BINDING_POINT, dirLightVBO);
            //link the program to the DIR binding point
            glUniformBlockBinding(m_program, m_dirLightUBO.getBlockIndex(),DIR_LIGHT_BINDING_POINT);
            //dir direction will be calculated in update()
            m_dirLightUBO.setValue("dirLight.ambient", 0.05f, 0.05f, 0.05f);
            m_dirLightUBO.setValue("dirLight.diffuse", 0.4f, 0.4f, 0.4f);
            m_dirLightUBO.setValue("dirLight.specular", 0.5f, 0.5f, 0.5f);


        } catch (Exception e)   {
            e.printStackTrace();
            return false;
        }

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
        //calculate dir light direction
        m_dirLightDirection.set(-0.2f, -1.0f, -0.3f,0.f).mul(m_camera.generateViewMat());
        m_dirLightUBO.setValue("dirLight.direction", -0.2f, -1.0f, -0.3f);
    }

    @Override
    protected void draw() {
        glClear(GL_COLOR_BUFFER_BIT|GL_DEPTH_BUFFER_BIT);
        m_projection.identity().setPerspective(
                m_camera.getFovRadians(), (float)m_width/m_height,0.1f,100.0f
        );
        m_modelViewProjectionUBO.setValue("projection", m_projection);
        m_modelViewProjectionUBO.setValue("view", m_camera.generateViewMat());


        glUseProgram(m_program);
        glBindVertexArray(m_vao);
        for(int i = 0; i < m_cubePositions.length; ++i) {
            m_model.identity()
                    .translate(m_cubePositions[i])
                    .rotate((float)Math.toRadians(20.0f * i), m_rotateAxis);
            m_modelViewProjectionUBO.setValue("model",m_model);
            glDrawArrays(GL_TRIANGLES,0,36);
        }
        glFinish();
    }

    public static void main(String[] args)  {
        OGLApplicationAbstract application = new MultipleLights();
        application.run();
    }
}
