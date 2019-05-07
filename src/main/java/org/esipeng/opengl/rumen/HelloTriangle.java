package org.esipeng.opengl.rumen;

import org.esipeng.opengl.base.OGLApplicationAbstract;
import org.esipeng.opengl.base.OGLApplicationGL33;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL33.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class HelloTriangle extends OGLApplicationGL33 {

    int m_width = 800, m_height = 600;
    int m_program, m_vao;
    @Override
    protected boolean applicationCreateContext() {
        long window = glfwCreateWindow(m_width,m_height, "Hello Triangle", NULL, NULL);
        if(window == NULL)
            return false;

        glfwMakeContextCurrent(window);
        return true;
    }

    @Override
    protected boolean applicationInitAfterContext() {
        glViewport(0,0,m_width,m_height);
        glClearColor(0.2f,0.3f,0.3f,1.0f);

        try {
            String vShaderSrc = loadFileFromResource("HelloTriangle/vertex.glsl");
            String fShaderSrc = loadFileFromResource("HelloTriangle/fragment.glsl");

            int vShader = loadShader(GL_VERTEX_SHADER,vShaderSrc);
            int fShader = loadShader(GL_FRAGMENT_SHADER, fShaderSrc);

            m_program = linkProgram(vShader, fShader);
        } catch (Exception e)   {
            e.printStackTrace();
            return false;
        }

        float vertices[] = {
                0.5f, 0.5f, 0.0f,   // 右上角
                0.5f, -0.5f, 0.0f,  // 右下角
                -0.5f, -0.5f, 0.0f, // 左下角
                -0.5f, 0.5f, 0.0f   // 左上角
        };

        int indices[] = { // 注意索引从0开始!
                0, 1, 3, // 第一个三角形
                1, 2, 3  // 第二个三角形
        };

        m_vao = getManagedVAO();
        glBindVertexArray(m_vao);
        int verticesVBO = getManagedVBO();
        glBindBuffer(GL_ARRAY_BUFFER, verticesVBO);
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);
        glVertexAttribPointer(0,3,GL_FLOAT,false,0,0L);
        glEnableVertexAttribArray(0);
        glBindBuffer(GL_ARRAY_BUFFER, 0);

        int indicesVBO = getManagedVBO();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indicesVBO);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW);

        glBindVertexArray(0);

        glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
        return true;
    }

    @Override
    protected void update(float elapsed) {

    }

    @Override
    protected void draw() {
        glClear(GL_COLOR_BUFFER_BIT);
        glUseProgram(m_program);
        glBindVertexArray(m_vao);
        //glDrawArrays(GL_TRIANGLES,0, 3);
        glDrawElements(GL_TRIANGLES,6,GL_UNSIGNED_INT, 0L);
    }

    public static void main(String[] args)  {
        OGLApplicationAbstract application = new HelloTriangle();
        application.enableFps(true);
        application.run();
    }
}
