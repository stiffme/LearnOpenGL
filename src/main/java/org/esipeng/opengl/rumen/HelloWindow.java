package org.esipeng.opengl.rumen;

import org.esipeng.opengl.base.OGLApplicationAbstract;
import org.esipeng.opengl.base.OGLApplicationGL33;
import static org.lwjgl.opengl.GL33.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class HelloWindow extends OGLApplicationGL33 {
    int m_width = 800, m_height = 600;
    @Override
    protected boolean applicationCreateContext() {
        long window = glfwCreateWindow(m_width,m_height, "LearnOpenGL", NULL, NULL);
        if(window == NULL)
            return false;

        glfwMakeContextCurrent(window);
        return true;
    }

    @Override
    protected boolean applicationInitAfterContext() {
        glViewport(0,0,m_width,m_height);
        glClearColor(0.2f,0.3f,0.3f,1.0f);


        return true;
    }

    @Override
    protected void update(float elapsed) {
        glClear(GL_COLOR_BUFFER_BIT);
    }

    @Override
    protected void draw() {

    }

    public static void main(String[] args)  {
        OGLApplicationAbstract application = new HelloWindow();
        application.enableFps(true);
        application.run();
    }
}
