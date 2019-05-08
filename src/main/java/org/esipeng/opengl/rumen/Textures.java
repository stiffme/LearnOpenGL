package org.esipeng.opengl.rumen;

import org.esipeng.opengl.base.OGLApplicationAbstract;
import org.esipeng.opengl.base.OGLApplicationGL33;

import java.nio.ByteBuffer;

import static org.lwjgl.stb.STBImage.*;
import static org.lwjgl.opengl.GL33.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Textures extends OGLApplicationGL33 {
    int m_width = 640, m_height = 480;

    int m_program, m_vao, m_containerTexture;
    @Override
    protected boolean applicationCreateContext() {
        long window = glfwCreateWindow(m_width, m_height, "Textures", NULL, NULL);
        if(window == NULL)
            return false;
        glfwMakeContextCurrent(window);
        return true;
    }

    @Override
    protected boolean applicationInitAfterContext() {
        try {
            m_program = compileAndLinkProgram("Textures/vertex.glsl", "Textures/fragment.glsl");
        } catch (Exception e)   {
            e.printStackTrace();
            return false;
        }

        float[] vertices = {
//     ---- 位置 ----       ---- 颜色 ----     - 纹理坐标 -
                0.5f,  0.5f, 0.0f,   1.0f, 0.0f, 0.0f,   1.0f, 1.0f,   // 右上
                0.5f, -0.5f, 0.0f,   0.0f, 1.0f, 0.0f,   1.0f, 0.0f,   // 右下
                -0.5f, -0.5f, 0.0f,   0.0f, 0.0f, 1.0f,   0.0f, 0.0f,   // 左下
                -0.5f,  0.5f, 0.0f,   1.0f, 1.0f, 0.0f,   0.0f, 1.0f    // 左上
        };

        int verticesVBO = getManagedVBO();
        glBindBuffer(GL_ARRAY_BUFFER, verticesVBO);
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);

        //load container.jpg
        ByteBuffer containerJpgData;
        try {
            String containerJpg = getResourcePath("Textures/container.jpg");
            int[] x = new int[1];
            int[] y = new int[1];
            int[] nrChannel = new int[1];
            containerJpgData = stbi_load(containerJpg,x,y,nrChannel, 0);
            if(containerJpgData == null)
                return false;

            System.out.printf("Width %d Height %d nrChannel %d", x[0], y[0], nrChannel[0]);

            m_containerTexture = getManagedTexture();
            glBindTexture(GL_TEXTURE_2D, m_containerTexture);
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, x[0], y[0], 0, GL_RGB, GL_UNSIGNED_BYTE,containerJpgData);
            glGenerateMipmap(GL_TEXTURE_2D);

            stbi_image_free(containerJpgData);


        } catch (Exception e)   {
            e.printStackTrace();
            return false;
        }


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

        glBindBuffer(GL_ARRAY_BUFFER,0);
        glBindVertexArray(0);

        glClearColor(0.2f,0.3f,0.3f,1.0f);

        return true;
    }

    @Override
    protected void update(float elapsed) {

    }

    @Override
    protected void draw() {
        glClear(GL_COLOR_BUFFER_BIT);
        glUseProgram(m_program);
        glBindTexture(GL_TEXTURE_2D, m_containerTexture);
        glBindVertexArray(m_vao);
        glDrawArrays(GL_TRIANGLE_FAN, 0 ,4);
        glBindVertexArray(0);
    }

    public static void main(String[] args)  {
        OGLApplicationAbstract application = new Textures();
        application.run();
    }
}
