package org.esipeng.opengl.rumen;

import org.esipeng.opengl.base.OGLApplicationAbstract;
import org.esipeng.opengl.base.OGLApplicationGL33;
import org.joml.Matrix4f;

import java.nio.ByteBuffer;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL33.*;
import static org.lwjgl.stb.STBImage.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class CoordSystem extends OGLApplicationGL33 {
    int m_width = 800, m_height = 600;

    int m_program, m_vao, m_texture1, m_texture2;
    int  m_saturateLoc = -1, m_transformLoc = -1;
    Matrix4f m_mvp = new Matrix4f();

    @Override
    protected boolean applicationCreateContext() {
        long window = glfwCreateWindow(m_width, m_height, "Transform", NULL, NULL);
        if(window == NULL)
            return false;
        glfwMakeContextCurrent(window);
        return true;
    }

    @Override
    protected boolean applicationInitAfterContext() {
        this.enableFps(true);
        try {
            m_program = compileAndLinkProgram("Transform/vertex.glsl", "Transform/fragment.glsl");
        } catch (Exception e)   {
            e.printStackTrace();
            return false;
        }

        float[] vertices = {
                // positions          // colors           // texture coords
                0.5f,  0.5f, 0.0f,   1.0f, 0.0f, 0.0f,   1.0f, 1.0f, // top right
                0.5f, -0.5f, 0.0f,   0.0f, 1.0f, 0.0f,   1.0f, 0.0f, // bottom right
                -0.5f, -0.5f, 0.0f,   0.0f, 0.0f, 1.0f,   0.0f, 0.0f, // bottom left
                -0.5f,  0.5f, 0.0f,   1.0f, 1.0f, 0.0f,   0.0f, 1.0f  // top left
        };
        int[] indices = {
                0, 1, 3, // first triangle
                1, 2, 3  // second triangle
        };

        int verticesVBO = getManagedVBO();
        glBindBuffer(GL_ARRAY_BUFFER, verticesVBO);
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        int indicesVBO = getManagedVBO();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indicesVBO);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER,indices,GL_STATIC_DRAW);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);


        m_vao = getManagedVAO();
        glBindVertexArray(m_vao);
        glBindBuffer(GL_ARRAY_BUFFER, verticesVBO);
        //aPos
        glVertexAttribPointer(0,3, GL_FLOAT, false, Float.BYTES * 8, 0L);
        glEnableVertexAttribArray(0);
        //aColor
        glVertexAttribPointer(1, 3, GL_FLOAT, false, Float.BYTES * 8, Float.BYTES * 3);
        glEnableVertexAttribArray(1);
        //aTexCoord
        glVertexAttribPointer(2, 2, GL_FLOAT, false, Float.BYTES * 8, Float.BYTES * 6);
        glEnableVertexAttribArray(2);

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indicesVBO);

        glBindBuffer(GL_ARRAY_BUFFER,0);
        glBindVertexArray(0);

        //load container.jpg
        ByteBuffer textureBufData;
        try {

            int[] x = new int[1];
            int[] y = new int[1];
            int[] nrChannel = new int[1];
            stbi_set_flip_vertically_on_load(true);

            m_texture1 = getManagedTexture();
            glBindTexture(GL_TEXTURE_2D, m_texture1);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);

            String containerJpg = getResourcePath("Textures/container.jpg");
            textureBufData = stbi_load(containerJpg,x,y,nrChannel, 0);
            if(textureBufData == null)
                return false;

            System.out.printf("Width %d Height %d nrChannel %d\n", x[0], y[0], nrChannel[0]);

            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, x[0], y[0], 0, GL_RGB, GL_UNSIGNED_BYTE,textureBufData);
            glGenerateMipmap(GL_TEXTURE_2D);

            stbi_image_free(textureBufData);

            m_texture2 = getManagedTexture();
            glBindTexture(GL_TEXTURE_2D, m_texture2);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);

            String faceJpg = getResourcePath("Textures/awesomeface.png");
            textureBufData = stbi_load(faceJpg,x,y,nrChannel,0);
            if(textureBufData == null)
                return false;
            System.out.printf("Width %d Height %d nrChannel %d\n", x[0], y[0], nrChannel[0]);

            glTexImage2D(GL_TEXTURE_2D,0,GL_RGBA, x[0],y[0],0,GL_RGBA,GL_UNSIGNED_BYTE,textureBufData);
            glGenerateMipmap(GL_TEXTURE_2D);

            glBindTexture(GL_TEXTURE_2D,0);
            stbi_image_free(textureBufData);

        } catch (Exception e)   {
            e.printStackTrace();
            return false;
        }


        //set texture location
        int textureLoc;
        glUseProgram(m_program);
        textureLoc = glGetUniformLocation(m_program, "texture1");
        if(textureLoc == -1)
            return false;
        glUniform1i(textureLoc, 1);

        textureLoc = glGetUniformLocation(m_program, "texture2");
        if(textureLoc == -1)
            return false;
        glUniform1i(textureLoc, 2);

        glClearColor(0.2f,0.3f,0.3f,1.0f);

        m_saturateLoc = glGetUniformLocation(m_program,"saturate");
        if(m_saturateLoc != -1) {
            glUseProgram(m_program);
            glUniform1f(m_saturateLoc, 0.3f);
        }
        m_transformLoc = glGetUniformLocation(m_program,"transform");

        m_mvp.identity();
        m_mvp.setRotationXYZ(0.0f,0.0f,(float)Math.toRadians(90.0f));
        m_mvp.scale(0.5f);

        if(m_transformLoc != -1)    {
            glUseProgram(m_program);
            glUniformMatrix4fv(m_transformLoc,false,m_mvp.get(new float[16]));
        }

        return true;
    }

    @Override
    protected void update(float elapsed) {


    }

    @Override
    protected void draw() {
        glClear(GL_COLOR_BUFFER_BIT);
        glActiveTexture(GL_TEXTURE1);
        glBindTexture(GL_TEXTURE_2D, m_texture1);
        glActiveTexture(GL_TEXTURE2);
        glBindTexture(GL_TEXTURE_2D, m_texture2);
        glUseProgram(m_program);
        glBindVertexArray(m_vao);
        if(m_transformLoc != -1)    {
            m_mvp.identity();

            m_mvp.setPerspective((float)Math.toRadians(45.0f), (float)(m_width / m_height), 0.1f, 100.0f);
            m_mvp.translate(0.0f,0.0f,-3.0f);
            m_mvp.rotate((float)Math.toRadians(-55.0f),1.0f, 0.0f,0.0f);

            glUseProgram(m_program);
            glUniformMatrix4fv(m_transformLoc,false,m_mvp.get(new float[16]));
            glDrawElements(GL_TRIANGLES,6, GL_UNSIGNED_INT, 0L);
        }

    }

    public static void main(String[] args)  {
        OGLApplicationAbstract application = new CoordSystem();
        application.run();
    }
}
