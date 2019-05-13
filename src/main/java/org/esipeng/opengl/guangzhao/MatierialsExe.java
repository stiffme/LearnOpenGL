package org.esipeng.opengl.guangzhao;

import org.esipeng.opengl.base.Camera;
import org.esipeng.opengl.base.OGLApplicationAbstract;
import org.esipeng.opengl.base.OGLApplicationGL33;
import org.esipeng.opengl.base.UBOManager;
import org.joml.Matrix4f;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL33.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class MatierialsExe extends OGLApplicationGL33 {
    private static final int NR_OF_INSTANCES = 25;
    private static final int VIEW_PROJECTION_BINDING_POINT = 1;
    private int m_width = 800, m_height = 800;
    private int m_vao;
    private int m_program;
    private Camera m_camera;
    private UBOManager m_viewProjectionUBO;
    private Matrix4f m_matProjection = new Matrix4f(), m_matNormal = new Matrix4f();

    @Override
    protected boolean applicationCreateContext() {
        long window = glfwCreateWindow(m_width, m_height, "Matierial Exe", NULL, NULL);
        if(window == NULL)
            return false;
        glfwMakeContextCurrent(window);
        return true;
    }

    @Override
    protected boolean applicationInitAfterContext() {
        float[] vertices = {
                -0.5f, -0.5f, -0.5f,  0.0f,  0.0f, -1.0f,
                0.5f, -0.5f, -0.5f,  0.0f,  0.0f, -1.0f,
                0.5f,  0.5f, -0.5f,  0.0f,  0.0f, -1.0f,
                0.5f,  0.5f, -0.5f,  0.0f,  0.0f, -1.0f,
                -0.5f,  0.5f, -0.5f,  0.0f,  0.0f, -1.0f,
                -0.5f, -0.5f, -0.5f,  0.0f,  0.0f, -1.0f,

                -0.5f, -0.5f,  0.5f,  0.0f,  0.0f, 1.0f,
                0.5f, -0.5f,  0.5f,  0.0f,  0.0f, 1.0f,
                0.5f,  0.5f,  0.5f,  0.0f,  0.0f, 1.0f,
                0.5f,  0.5f,  0.5f,  0.0f,  0.0f, 1.0f,
                -0.5f,  0.5f,  0.5f,  0.0f,  0.0f, 1.0f,
                -0.5f, -0.5f,  0.5f,  0.0f,  0.0f, 1.0f,

                -0.5f,  0.5f,  0.5f, -1.0f,  0.0f,  0.0f,
                -0.5f,  0.5f, -0.5f, -1.0f,  0.0f,  0.0f,
                -0.5f, -0.5f, -0.5f, -1.0f,  0.0f,  0.0f,
                -0.5f, -0.5f, -0.5f, -1.0f,  0.0f,  0.0f,
                -0.5f, -0.5f,  0.5f, -1.0f,  0.0f,  0.0f,
                -0.5f,  0.5f,  0.5f, -1.0f,  0.0f,  0.0f,

                0.5f,  0.5f,  0.5f,  1.0f,  0.0f,  0.0f,
                0.5f,  0.5f, -0.5f,  1.0f,  0.0f,  0.0f,
                0.5f, -0.5f, -0.5f,  1.0f,  0.0f,  0.0f,
                0.5f, -0.5f, -0.5f,  1.0f,  0.0f,  0.0f,
                0.5f, -0.5f,  0.5f,  1.0f,  0.0f,  0.0f,
                0.5f,  0.5f,  0.5f,  1.0f,  0.0f,  0.0f,

                -0.5f, -0.5f, -0.5f,  0.0f, -1.0f,  0.0f,
                0.5f, -0.5f, -0.5f,  0.0f, -1.0f,  0.0f,
                0.5f, -0.5f,  0.5f,  0.0f, -1.0f,  0.0f,
                0.5f, -0.5f,  0.5f,  0.0f, -1.0f,  0.0f,
                -0.5f, -0.5f,  0.5f,  0.0f, -1.0f,  0.0f,
                -0.5f, -0.5f, -0.5f,  0.0f, -1.0f,  0.0f,

                -0.5f,  0.5f, -0.5f,  0.0f,  1.0f,  0.0f,
                0.5f,  0.5f, -0.5f,  0.0f,  1.0f,  0.0f,
                0.5f,  0.5f,  0.5f,  0.0f,  1.0f,  0.0f,
                0.5f,  0.5f,  0.5f,  0.0f,  1.0f,  0.0f,
                -0.5f,  0.5f,  0.5f,  0.0f,  1.0f,  0.0f,
                -0.5f,  0.5f, -0.5f,  0.0f,  1.0f,  0.0f
        };

        try {
            m_program = compileAndLinkProgram(
                    "MatierialExe/vertex.glsl",
                    "MatierialExe/fragment.glsl"
            );
        } catch (Exception e)   {
            e.printStackTrace();
            return false;
        }

        int verticesVBO = getManagedVBO();
        glBindBuffer(GL_ARRAY_BUFFER, verticesVBO);
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);

        //update aModel for each instance
        int modelsVBO = getManagedVBO();
        glBindBuffer(GL_ARRAY_BUFFER, modelsVBO);
        nglBufferData(GL_ARRAY_BUFFER,Float.BYTES * 16 * NR_OF_INSTANCES, 0L, GL_STATIC_DRAW);
        Matrix4f matModel = new Matrix4f();
        int ITEM_NUMBER = (int)Math.sqrt(NR_OF_INSTANCES);
        float DISP = 10.f / (ITEM_NUMBER + 1);
        float scaleFloat = 10.f/ ITEM_NUMBER * 0.5f;
        System.out.printf("DISP %.2f, scaleFloat %.2f ITEM_NUMBER %d\n", DISP, scaleFloat, ITEM_NUMBER);
        float[] temp = new float[16];
        //spread the instances
        for(int instance = 0; instance < NR_OF_INSTANCES; ++instance)   {
            int col = instance % ITEM_NUMBER;
            int row = instance / ITEM_NUMBER;
            System.out.printf("ROW: %d, COL %d\n", row, col);
            float dispX = DISP * (col + 1) - 5.f;
            float dispY = -DISP * (row + 1) + 5.f;
            matModel.identity().translate(dispX, dispY, 0.0f).scale(scaleFloat);
            //update the VBO
            glBufferSubData(GL_ARRAY_BUFFER,Float.BYTES * 16 * instance, matModel.get(temp));
        }
        glBindBuffer(GL_ARRAY_BUFFER, 0);

        //build VAO
        m_vao = glGenVertexArrays();
        glBindVertexArray(m_vao);
        //aPos and aNormal
        glBindBuffer(GL_ARRAY_BUFFER, verticesVBO);
        glVertexAttribPointer(0,3, GL_FLOAT,false, Float.BYTES * 6, 0L);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(1,3, GL_FLOAT,false, Float.BYTES * 6, Float.BYTES * 3);
        glEnableVertexAttribArray(1);
        glBindBuffer(GL_ARRAY_BUFFER, 0);

        //aModel
        glBindBuffer(GL_ARRAY_BUFFER, modelsVBO);
        glVertexAttribPointer(2, 4, GL_FLOAT,false, Float.BYTES * 16, Float.BYTES * 0);
        glVertexAttribPointer(3, 4, GL_FLOAT,false, Float.BYTES * 16, Float.BYTES * 4);
        glVertexAttribPointer(4, 4, GL_FLOAT,false, Float.BYTES * 16, Float.BYTES * 8);
        glVertexAttribPointer(5, 4, GL_FLOAT,false, Float.BYTES * 16, Float.BYTES * 12);

        glEnableVertexAttribArray(2);
        glEnableVertexAttribArray(3);
        glEnableVertexAttribArray(4);
        glEnableVertexAttribArray(5);

        glVertexAttribDivisor(2, 1);
        glVertexAttribDivisor(3, 1);
        glVertexAttribDivisor(4, 1);
        glVertexAttribDivisor(5, 1);


        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);

        //init camera
        m_camera = new Camera(
                0.0f,0.0f,13.0f,
                0.0f,0.0f,-1.0f,
                0.0f,1.0f,0.0f,
                m_width/2, m_height/2,
                glfwGetCurrentContext()
        );

        //ViewProjection Uniform
        int viewPrjectionUBO = getManagedVBO();
        m_viewProjectionUBO = new UBOManager();
        if(!m_viewProjectionUBO.attachUniformBlock(m_program,"ViewProjection",viewPrjectionUBO))
            return false;
        //bind UBO to binding point
        glBindBufferBase(GL_UNIFORM_BUFFER, VIEW_PROJECTION_BINDING_POINT, viewPrjectionUBO);

        int viewProjectionBlockIndex =
                glGetUniformBlockIndex(m_program,"ViewProjection");
        //bind the uniform block to the binding point
        glUniformBlockBinding(m_program, viewProjectionBlockIndex, VIEW_PROJECTION_BINDING_POINT);

        glClearColor(0.2f,0.3f,0.3f,1.0f);
        glEnable(GL_DEPTH_TEST);
        glDepthFunc(GL_LEQUAL);

        return true;
    }

    @Override
    protected void update(float elapsed) {
        m_camera.processInput(elapsed);
        //update view
        m_viewProjectionUBO.setValue("view", m_camera.generateView());
        //update projection
        m_matProjection.identity().setPerspective(
                m_camera.getFovRadians(), (float)m_width/m_height, 0.1f, 100.f
        );
        m_viewProjectionUBO.setValue("projection", m_matProjection);
        //normal matrix is calculated in vertex shader
    }

    @Override
    protected void draw() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glUseProgram(m_program);
        glBindVertexArray(m_vao);
        glDrawArraysInstanced(GL_TRIANGLES,0,36,NR_OF_INSTANCES);
        glBindVertexArray(0);
    }

    public static void main(String[] args)  {
        OGLApplicationAbstract application = new MatierialsExe();
        application.run();
    }
}
