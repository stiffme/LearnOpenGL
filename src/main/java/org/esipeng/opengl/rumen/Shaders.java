package org.esipeng.opengl.rumen;

import org.esipeng.opengl.base.OGLApplicationAbstract;
import org.esipeng.opengl.base.OGLApplicationGL33;

import static org.lwjgl.opengl.GL33.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Shaders extends OGLApplicationGL33 {
    int m_width = 640, m_height = 480;
    int m_vao, m_program;

    @Override
    protected boolean applicationCreateContext() {
        long window  = glfwCreateWindow(m_width, m_height, "Shaders", NULL, NULL);
        if(window == NULL)
            return false;

        glfwMakeContextCurrent(window);
        return true;

    }

    @Override
    protected boolean applicationInitAfterContext() {
        glViewport(0,0,m_width,m_height);
        try {
            m_program = compileAndLinkProgram("Shaders/vertex.glsl", "Shaders/fragment.glsl");
            glValidateProgram(m_program);
        } catch (Exception e)   {
            e.printStackTrace();
            return false;
        }

        float vertices[] = {
                // 位置              // 颜色
                0.5f, -0.5f, 0.0f,  1.0f, 0.0f, 0.0f,   // 右下
                -0.5f, -0.5f, 0.0f,  0.0f, 1.0f, 0.0f,   // 左下
                0.0f,  0.5f, 0.0f,  0.0f, 0.0f, 1.0f    // 顶部
        };

        int verticesVBO = getManagedVBO();
        glBindBuffer(GL_ARRAY_BUFFER, verticesVBO);
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);

        m_vao = getManagedVAO();
        glBindVertexArray(m_vao);

        glBindBuffer(GL_ARRAY_BUFFER, verticesVBO);
        glVertexAttribPointer(0,3,GL_FLOAT, false, Float.BYTES * 6, 0L);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(1, 3, GL_FLOAT, false, Float.BYTES * 6, Float.BYTES * 3);
        glEnableVertexAttribArray(1);

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);

        glClearColor(0.2f,0.3f,0.3f,1.0f);


        return true;

    }

    @Override
    protected void update(float elapsed) {
        int offsetLoc = glGetUniformLocation(m_program, "offset");
        glUseProgram(m_program);
        glUniform3f(offsetLoc, 0.25f, 0.0f, 0.f);
    }

    @Override
    protected void draw() {
        glClear(GL_COLOR_BUFFER_BIT);
        glUseProgram(m_program);
        glBindVertexArray(m_vao);
        glDrawArrays(GL_TRIANGLES,0,3);
    }

    public static void main(String[] args)  {
        OGLApplicationAbstract application = new Shaders();
        application.run();
    }
}
