package org.esipeng.opengl.guangzhao;

import org.esipeng.opengl.base.Camera;
import org.esipeng.opengl.base.OGLApplicationAbstract;
import org.esipeng.opengl.base.OGLApplicationGL33;
import org.joml.Matrix4f;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL33.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class ColorsExample extends OGLApplicationGL33 {
    final static int MVP_BLOCK_BINDING_POINT = 1;
    final static int COLOR_BLOCK_BINDING_POINT = 2;

    int m_width = 800, m_height = 600;
    long m_window = -1;

    int m_programObject, m_programLight;
    int m_objectVAO, m_lightVAO;
    int m_mvpVBO, m_colorVBO;

    float[] m_matBuffer = new float[16];
    Camera m_camera;

    Matrix4f m_model, m_view, m_projection;

    @Override
    protected boolean applicationCreateContext() {
        m_window = glfwCreateWindow(m_width, m_height, "Colors Example", NULL, NULL);
        glfwMakeContextCurrent(m_window);
        return m_window != NULL;
    }

    @Override
    protected boolean applicationInitAfterContext() {
        try{
            m_programObject = compileAndLinkProgram(
                    "ColorsExample/vertex.glsl",
                    "ColorsExample/fragment.glsl"
            );
            m_programLight = compileAndLinkProgram(
                    "ColorsExample/vertex.glsl",
                    "ColorsExample/fragmentLight.glsl"
            );
        } catch (Exception e)   {
            e.printStackTrace();
            return false;
        }

        float[] vertices= {
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

        int verticesVBO = getManagedVBO();
        glBindBuffer(GL_ARRAY_BUFFER, verticesVBO);
        glBufferData(GL_ARRAY_BUFFER,  vertices, GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);

        //uniform  MVP block VBO
        m_mvpVBO = getManagedVBO();
        glBindBuffer(GL_UNIFORM_BUFFER, m_mvpVBO);
        nglBufferData(GL_UNIFORM_BUFFER, Float.BYTES * 16 * 3, 0L, GL_STATIC_DRAW);
        glBindBuffer(GL_UNIFORM_BUFFER, 0);
        //bind the block VBO to binding point
        glBindBufferBase(GL_UNIFORM_BUFFER, MVP_BLOCK_BINDING_POINT, m_mvpVBO);

        //uniform Color block VBO
        m_colorVBO = getManagedVBO();
        glBindBuffer(GL_UNIFORM_BUFFER, m_colorVBO);
        float[] colorData = new float[]{
          1.f, 0.5f,0.31f,
          1.f,1.f,1.f
        };
        glBufferData(GL_UNIFORM_BUFFER,colorData,GL_STATIC_DRAW);
        glBindBuffer(GL_UNIFORM_BUFFER, 0);
        glBindBufferBase(GL_UNIFORM_BUFFER, COLOR_BLOCK_BINDING_POINT, m_colorVBO);


        //object VAO
        m_objectVAO = getManagedVAO();
        glBindVertexArray(m_objectVAO);
        glBindBuffer(GL_ARRAY_BUFFER, verticesVBO);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(0, 3, GL_FLOAT,false, Float.BYTES * 5, 0L);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(m_objectVAO);

        //light VAO
        m_lightVAO = getManagedVAO();
        glBindVertexArray(m_lightVAO);
        glBindBuffer(GL_ARRAY_BUFFER, verticesVBO);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(0,3,GL_FLOAT,false, Float.BYTES * 5, 0L);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);

        //bind view_projection in program
        int viewProjectionLoc = glGetUniformBlockIndex(m_programObject, "view_projection");
        glUniformBlockBinding(m_programObject, viewProjectionLoc, MVP_BLOCK_BINDING_POINT);

        viewProjectionLoc = glGetUniformBlockIndex(m_programLight, "view_projection");
        glUniformBlockBinding(m_programLight,viewProjectionLoc, MVP_BLOCK_BINDING_POINT);

        //bind ColorBlock in object program
        viewProjectionLoc = glGetUniformBlockIndex(m_programObject, "ColorBlock");
        glUniformBlockBinding(m_programObject, viewProjectionLoc, COLOR_BLOCK_BINDING_POINT);

        //initialize camera
        m_camera = new Camera(
                1.f,0.6f,6.f,
                0.f,0.f,-1.f,
                0.f,1.f,0.f,
                m_width/2, m_height/2,
                m_window
        );

        m_model = new Matrix4f();
        m_view = new Matrix4f();
        m_projection = new Matrix4f();
        glClearColor(0.2f,0.3f,0.3f,1.0f);

        m_camera.enableMouseFpsView();
        return true;
    }

    @Override
    protected void update(float elapsed) {
        m_camera.processInput(elapsed);
    }

    @Override
    protected void draw() {
        glClear(GL_COLOR_BUFFER_BIT);
        //view projection VBO updating
        m_model.identity();
        glBindBuffer(GL_UNIFORM_BUFFER, m_mvpVBO);

        //view mat4
        glBufferSubData(GL_UNIFORM_BUFFER, Float.BYTES * 16, m_camera.generateView());

        //projection mat4
        m_projection.setPerspective((float)Math.toRadians(m_camera.getFov()), (float)(m_width/m_height), 0.1f, 100.f);
        glBufferSubData(GL_UNIFORM_BUFFER, Float.BYTES * 32, m_projection.get(m_matBuffer));

        //object model mat4
        glBufferSubData(GL_UNIFORM_BUFFER, Float.BYTES * 0, m_model.get(m_matBuffer));
        glUseProgram(m_programObject);
        glBindVertexArray(m_objectVAO);
        glDrawArrays(GL_TRIANGLES,0,36);

        //light
        m_model.identity();

        m_model.translate(1.2f,1.0f,2.0f);
        m_model.scale(0.2f);

        glBufferSubData(GL_UNIFORM_BUFFER, Float.BYTES * 0, m_model.get(m_matBuffer));
        glUseProgram(m_programLight);
        glBindVertexArray(m_lightVAO);
        glDrawArrays(GL_TRIANGLES,0,36);

        glBindVertexArray(0);
        glBindBuffer(GL_UNIFORM_BUFFER, 0);
    }

    public static void main(String[] args)  {
        OGLApplicationAbstract application = new ColorsExample();
        application.run();
    }
}
