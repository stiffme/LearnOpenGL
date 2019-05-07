package org.esipeng.opengl.base;

import org.lwjgl.glfw.GLFWFramebufferSizeCallback;
import org.lwjgl.glfw.GLFWFramebufferSizeCallbackI;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL33;

import java.util.LinkedHashSet;
import static org.lwjgl.opengl.GL33.*;
import static org.lwjgl.glfw.GLFW.*;

public abstract class OGLApplicationGL33 extends OGLApplicationAbstract {
    protected LinkedHashSet<Integer> m_vbos, m_vaos, m_shaders, m_programs;

    public OGLApplicationGL33() {
        m_vaos = new LinkedHashSet<>();
        m_vbos = new LinkedHashSet<>();
        m_shaders = new LinkedHashSet<>();
        m_programs = new LinkedHashSet<>();
    }

    protected int linkProgram(int vShader, int fShader)    {
        int program = glCreateProgram();
        if(program == 0)
            return 0;

        glAttachShader(program, vShader);
        glAttachShader(program, fShader);
        glLinkProgram(program);

        int linkStatus = glGetProgrami(program, GL_LINK_STATUS);
        if(linkStatus != GL_TRUE)   {
            System.out.println("Link failed " +
                    glGetProgramInfoLog(program));
            return 0;
        }
        m_programs.add(program);
        return program;
    }

    protected void destroy()    {
        m_programs.forEach(GL33::glDeleteProgram);
        m_shaders.forEach(GL33::glDeleteShader);
        m_vbos.forEach(GL33::glDeleteBuffers);
        m_vaos.forEach(GL33::glDeleteVertexArrays);
    }


    protected int getManagedVBO()  {
        int vbo = glGenBuffers();
        m_vbos.add(vbo);
        return vbo;
    }

    protected int getManagedVAO()  {
        int vao = glGenVertexArrays();
        m_vaos.add(vao);
        return vao;
    }

    @Override
    protected final boolean applicationInit() {

        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);

        if(!applicationCreateContext())
            return false;

        GL.createCapabilities();
        long window = glfwGetCurrentContext();
        glfwSetFramebufferSizeCallback(window, new GLFWFramebufferSizeCallback() {
            @Override
            public void invoke(long window, int width, int height) {
                OGLApplicationGL33.this.frameBufferSizeChanged(window, width, height);
            }
        });

        return applicationInitAfterContext();
    }


    protected void frameBufferSizeChanged(long window, int width, int height)   {
        glViewport(0,0,width,height);
    }
    protected abstract boolean applicationCreateContext();
    protected abstract boolean applicationInitAfterContext();
}
