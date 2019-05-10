package org.esipeng.opengl.rumen;

import org.esipeng.opengl.base.OGLApplicationAbstract;
import org.esipeng.opengl.base.OGLApplicationGL33;
import org.esipeng.opengl.base.TextureLoader;
import org.joml.Matrix4f;
import org.joml.Random;
import org.joml.Vector3f;

import static org.lwjgl.opengl.GL33.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class CoordSystem3DExample extends OGLApplicationGL33 {
    int m_width = 800, m_height = 600;
    int m_program, m_vao, m_texture1, m_texture2;
    int  m_modelLoc = -1;

    Matrix4f m_model = new Matrix4f();
    float[] m_modelBuf = new float[16];
    Vector3f m_rotationVector = new Vector3f(0.5f, 1.0f,0.0f).normalize();

    Vector3f[] m_cubePositions = {
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

    float[] m_initialAngle;
    @Override
    protected boolean applicationCreateContext() {
        long window = glfwCreateWindow(m_width, m_height, "CoordSystemExample 3D", NULL, NULL);
        if(window == NULL)
            return false;
        glfwMakeContextCurrent(window);
        return true;
    }

    @Override
    protected boolean applicationInitAfterContext() {

        float[] vertices = {
                -0.5f, -0.5f, -0.5f,  0.0f, 0.0f,
                0.5f, -0.5f, -0.5f,  1.0f, 0.0f,
                0.5f,  0.5f, -0.5f,  1.0f, 1.0f,
                0.5f,  0.5f, -0.5f,  1.0f, 1.0f,
                -0.5f,  0.5f, -0.5f,  0.0f, 1.0f,
                -0.5f, -0.5f, -0.5f,  0.0f, 0.0f,

                -0.5f, -0.5f,  0.5f,  0.0f, 0.0f,
                0.5f, -0.5f,  0.5f,  1.0f, 0.0f,
                0.5f,  0.5f,  0.5f,  1.0f, 1.0f,
                0.5f,  0.5f,  0.5f,  1.0f, 1.0f,
                -0.5f,  0.5f,  0.5f,  0.0f, 1.0f,
                -0.5f, -0.5f,  0.5f,  0.0f, 0.0f,

                -0.5f,  0.5f,  0.5f,  1.0f, 0.0f,
                -0.5f,  0.5f, -0.5f,  1.0f, 1.0f,
                -0.5f, -0.5f, -0.5f,  0.0f, 1.0f,
                -0.5f, -0.5f, -0.5f,  0.0f, 1.0f,
                -0.5f, -0.5f,  0.5f,  0.0f, 0.0f,
                -0.5f,  0.5f,  0.5f,  1.0f, 0.0f,

                0.5f,  0.5f,  0.5f,  1.0f, 0.0f,
                0.5f,  0.5f, -0.5f,  1.0f, 1.0f,
                0.5f, -0.5f, -0.5f,  0.0f, 1.0f,
                0.5f, -0.5f, -0.5f,  0.0f, 1.0f,
                0.5f, -0.5f,  0.5f,  0.0f, 0.0f,
                0.5f,  0.5f,  0.5f,  1.0f, 0.0f,

                -0.5f, -0.5f, -0.5f,  0.0f, 1.0f,
                0.5f, -0.5f, -0.5f,  1.0f, 1.0f,
                0.5f, -0.5f,  0.5f,  1.0f, 0.0f,
                0.5f, -0.5f,  0.5f,  1.0f, 0.0f,
                -0.5f, -0.5f,  0.5f,  0.0f, 0.0f,
                -0.5f, -0.5f, -0.5f,  0.0f, 1.0f,

                -0.5f,  0.5f, -0.5f,  0.0f, 1.0f,
                0.5f,  0.5f, -0.5f,  1.0f, 1.0f,
                0.5f,  0.5f,  0.5f,  1.0f, 0.0f,
                0.5f,  0.5f,  0.5f,  1.0f, 0.0f,
                -0.5f,  0.5f,  0.5f,  0.0f, 0.0f,
                -0.5f,  0.5f, -0.5f,  0.0f, 1.0f
        };

        //GLSL program
        try{
            m_program = compileAndLinkProgram("CoordSystem3D/vertex.glsl", "CoordSystem3D/fragment.glsl");
        } catch (Exception e)   {
            e.printStackTrace();
            return false;
        }

        //Transfer data to VBO
        int dataVBO = getManagedVBO();
        glBindBuffer(GL_ARRAY_BUFFER, dataVBO);
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);

        //Array Pointers to VAO
        m_vao = getManagedVAO();
        glBindVertexArray(m_vao);
        glBindBuffer(GL_ARRAY_BUFFER, dataVBO);
        glVertexAttribPointer(0,3,GL_FLOAT, false, Float.BYTES * 5, 0L);
        glEnableVertexAttribArray(0);

        glVertexAttribPointer(1, 2, GL_FLOAT, false, Float.BYTES * 5, Float.BYTES * 3);
        glEnableVertexAttribArray(1);
        glBindBuffer(GL_ARRAY_BUFFER,0);
        glBindVertexArray(0);

        //Load Texture
        TextureLoader textureLoader = new TextureLoader();

        //texture 1
        if(!textureLoader.loadFromResource("Textures/awesomeface.png"))
            return false;
        m_texture1 = getManagedTexture();
        glBindTexture(GL_TEXTURE_2D, m_texture1);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, textureLoader.getX(), textureLoader.getY(), 0, GL_RGBA, GL_UNSIGNED_BYTE, textureLoader.getData());
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glBindTexture(GL_TEXTURE_2D,0);

        //texture 2
        if(!textureLoader.loadFromResource("Textures/container.jpg"))
            return false;
        m_texture2 = getManagedTexture();
        glBindTexture(GL_TEXTURE_2D, m_texture2);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, textureLoader.getX(), textureLoader.getY(), 0, GL_RGB, GL_UNSIGNED_BYTE, textureLoader.getData());
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glBindTexture(GL_TEXTURE_2D,0);

        textureLoader.release();

        //static uniform setting
        glUseProgram(m_program);
        //uniform float aSaturate
        int uniformLoc = glGetUniformLocation(m_program, "aSaturate");
        if(uniformLoc != -1)
            glUniform1f(uniformLoc, 0.5f);
        else
            System.out.println("Uniform not found!");

        //uniform sampler2D aTexture1;
        uniformLoc = glGetUniformLocation(m_program, "aTexture1");
        if(uniformLoc != -1)
            glUniform1i(uniformLoc, 1);
        else
            System.out.println("Uniform not found!");

        //uniform sampler2D aTexture2;
        uniformLoc = glGetUniformLocation(m_program, "aTexture2");
        if(uniformLoc != -1)
            glUniform1i(uniformLoc, 2);
        else
            System.out.println("Uniform not found!");


        //uniform mat4 projection;
        int projectionLoc = glGetUniformLocation(m_program, "projection");
        if(projectionLoc == -1)
            return false;
        Matrix4f matProjection = new Matrix4f();
        matProjection.setPerspective((float)Math.toRadians(45.f), (float)(m_width / m_height), 0.1f, 100.f);
        glUniformMatrix4fv(projectionLoc, false, matProjection.get(new float[16]));


        //uniform mat4 view;
        int viewLoc = glGetUniformLocation(m_program,"view");
        if(viewLoc == -1)
            return false;
        Matrix4f matView = new Matrix4f();
        matView.lookAt(
                0.f,0.f,5.f,
                0.f,0.f,0.f,
                0.f,1.f,0.f
        );
        glUniformMatrix4fv(viewLoc, false, matView.get(new float[16]));

        //uniform mat4 model , it will be updated in every draw;
        m_modelLoc = glGetUniformLocation(m_program, "model");
        if(m_modelLoc == -1)
            return false;

        //initial angle
        Random random = new Random(System.currentTimeMillis());
        m_initialAngle = new float[m_cubePositions.length];
        for(int i = 0; i < m_initialAngle.length; ++i)  {
            m_initialAngle[i] = random.nextFloat() * 360.f;
        }

        glClearColor(0.2f,0.3f,0.3f,1.0f);
        glEnable(GL_DEPTH_TEST);
        return true;
    }

    @Override
    protected void update(float elapsed) {


    }

    @Override
    protected void draw() {
        glClear(GL_DEPTH_BUFFER_BIT|GL_COLOR_BUFFER_BIT);
        glUseProgram(m_program);
        glBindVertexArray(m_vao);

        glActiveTexture(GL_TEXTURE1);
        glBindTexture(GL_TEXTURE_2D, m_texture1);

        glActiveTexture(GL_TEXTURE2);
        glBindTexture(GL_TEXTURE_2D, m_texture2);

        for(int i = 0; i < 10; ++i) {
            m_model.identity();
            m_model.translate(m_cubePositions[i]);
            m_model.rotate((float)Math.toRadians(glfwGetTime() * 50.f +  m_initialAngle[i]), m_rotationVector);
            glUniformMatrix4fv(m_modelLoc,false, m_model.get(m_modelBuf));
            glDrawArrays(GL_TRIANGLES,0,36);
        }

    }

    public static void main(String[] args)  {
        OGLApplicationAbstract application = new CoordSystem3DExample();
        application.run();
    }
}
