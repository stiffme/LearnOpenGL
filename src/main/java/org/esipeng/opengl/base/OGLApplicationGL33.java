package org.esipeng.opengl.base;

import org.lwjgl.glfw.GLFWFramebufferSizeCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL33;

import java.util.LinkedHashSet;
import static org.lwjgl.opengl.GL33.*;
import static org.lwjgl.glfw.GLFW.*;

public abstract class OGLApplicationGL33 extends OGLApplicationAbstract {
    protected LinkedHashSet<Integer> m_vbos, m_vaos, m_shaders, m_programs, m_textures;

    public OGLApplicationGL33() {
        m_vaos = new LinkedHashSet<>();
        m_vbos = new LinkedHashSet<>();
        m_shaders = new LinkedHashSet<>();
        m_programs = new LinkedHashSet<>();
        m_textures = new LinkedHashSet<>();
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

    protected int loadShader(int type, String shaderSrc) throws Exception    {
        int shader = glCreateShader(type);
        glShaderSource(shader,shaderSrc);
        glCompileShader(shader);

        int compiled = glGetShaderi(shader, GL_COMPILE_STATUS);
        if(compiled != GL_TRUE) {
            System.out.println("Failed to compile shader! \n" + shaderSrc );
            System.out.println(glGetShaderInfoLog(shader));
            throw new Exception("Failed to compile shader");
        }
        m_shaders.add(shader);
        return shader;

    }

    protected int compileAndLinkProgram(String vShaderPath, String fShaderPath) throws Exception    {
        String vShaderSrc = loadFileFromResource(vShaderPath);
        String fShaderSrc = loadFileFromResource(fShaderPath);
        int vShader = loadShader(GL_VERTEX_SHADER, vShaderSrc);
        int fShader = loadShader(GL_FRAGMENT_SHADER, fShaderSrc);
        int program = linkProgram(vShader, fShader);
        return program;
    }

    protected void destroy()    {
        m_programs.forEach(GL33::glDeleteProgram);
        m_shaders.forEach(GL33::glDeleteShader);
        m_vbos.forEach(GL33::glDeleteBuffers);
        m_vaos.forEach(GL33::glDeleteVertexArrays);
        m_textures.forEach(GL33::glDeleteTextures);
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

    protected int getManagedTexture()   {
        int texture = glGenTextures();
        m_textures.add(texture);
        return texture;
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
