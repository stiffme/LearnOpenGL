package org.esipeng.opengl.guangzhao;

import org.esipeng.opengl.base.Camera;
import org.esipeng.opengl.base.OGLApplicationAbstract;
import org.esipeng.opengl.base.OGLApplicationGL33;
import org.esipeng.opengl.base.UBOManager;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL33.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class BasicLightExe3 extends OGLApplicationGL33 {

    private final static int MVP_BINDING_POINT = 1;
    private final static int COLOR_BINDING_POINT = 2;

    private int m_width = 800, m_height = 600;
    private int m_programObject, m_programLight;

    private int m_objectVAO, m_lightVAO;
    private Camera m_camera;
    private UBOManager m_mvpUBOManager, m_colorUBOManager;
    private Matrix4f m_model, m_projection;
    private Vector3f m_lightPos = new Vector3f(0.6f,0.0f,5.0f);
    private Vector4f m_lightPosInViewSpace = new Vector4f(0.0f);

    @Override
    protected boolean applicationCreateContext() {
        long window = glfwCreateWindow(m_width, m_height, "Basic Light Exe3", NULL, NULL);
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
            m_programObject = compileAndLinkProgram(
                    "BasicLightExe/vertex.glsl",
                    "BasicLightExe/fragment.glsl"
            );

            m_programLight = compileAndLinkProgram(
                    "BasicLightExe/vertex.glsl",
                    "BasicLightExe/fragmentLight.glsl"
            );
        } catch ( Exception e)  {
            e.printStackTrace();
            return false;
        }

        //upload vertices data to VBO
        int vboVertices = getManagedVBO();
        glBindBuffer(GL_ARRAY_BUFFER, vboVertices);
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);

        //object VAO
        m_objectVAO = getManagedVAO();
        glBindVertexArray(m_objectVAO);
        glBindBuffer(GL_ARRAY_BUFFER, vboVertices);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(0,3,GL_FLOAT,false,Float.BYTES * 6, 0L);
        glEnableVertexAttribArray(1);
        glVertexAttribPointer(1, 3,GL_FLOAT,false,Float.BYTES * 6, Float.BYTES * 3);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);

        //light VAO
        m_lightVAO = getManagedVAO();
        glBindVertexArray(m_lightVAO);
        glBindBuffer(GL_ARRAY_BUFFER, vboVertices);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(0,3,GL_FLOAT,false,Float.BYTES * 6, 0L);
        glEnableVertexAttribArray(1);
        glVertexAttribPointer(1, 3,GL_FLOAT,false,Float.BYTES * 6, Float.BYTES * 3);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);

        //MVP UBO
        int mvpVBO = getManagedVBO();
        m_mvpUBOManager = new UBOManager();
        if(!m_mvpUBOManager.attachUniformBlock(m_programObject, "mvp", mvpVBO))
            return false;
        //bind UBO to MVP binding point
        glBindBufferBase(GL_UNIFORM_BUFFER, MVP_BINDING_POINT, mvpVBO);

        //ColorBlock UBO
        int colorBlockUBO = getManagedVBO();
        m_colorUBOManager = new UBOManager();
        if(!m_colorUBOManager.attachUniformBlock(m_programObject, "ColorBlock", colorBlockUBO))
            return false;
        //bind UBO to Color binding point
        glBindBufferBase(GL_UNIFORM_BUFFER, COLOR_BINDING_POINT, colorBlockUBO);

        //ColorBlock data
        m_colorUBOManager.setValue("lightColor", 1.0f,1.0f,1.0f);
        m_colorUBOManager.setValue("objectColor", 1.0f,0.5f,0.31f);

        //bind object MVP to binding point
        int mvpBlockIndex = glGetUniformBlockIndex(m_programObject,"mvp");
        glUniformBlockBinding(m_programObject, mvpBlockIndex, MVP_BINDING_POINT);

        //bind light MVP to binding point
        mvpBlockIndex = glGetUniformBlockIndex(m_programLight, "mvp");
        glUniformBlockBinding(m_programLight, mvpBlockIndex, MVP_BINDING_POINT);

        //bind object ColorBlock to binding point
        int colorBlockIndex = glGetUniformBlockIndex(m_programObject,"ColorBlock");
        glUniformBlockBinding(m_programObject, colorBlockIndex, COLOR_BINDING_POINT);

        //bind light ColorBlock to binding point
        colorBlockIndex = glGetUniformBlockIndex(m_programLight,"ColorBlock");
        glUniformBlockBinding(m_programLight, colorBlockIndex, COLOR_BINDING_POINT);

        //initialize camera
        m_camera = new Camera(
                0.f,0.f,10.f,
                0.f,0.f,-1.f,
                0.f,1.f,0.f,
                m_width/2, m_height/2,
                glfwGetCurrentContext()
        );

        //matrix initializinginvert()
        m_model = new Matrix4f();
        //m_view mat is from m_camera
        m_projection = new Matrix4f();

        glClearColor(0.2f,0.3f,0.3f,1.0f);
        glEnable(GL_DEPTH_TEST);
        glDepthFunc(GL_LEQUAL);
        m_camera.enableMouseFpsView();
        return true;
    }

    @Override
    protected void update(float elapsed) {
        m_camera.processInput(elapsed);
        m_projection.setPerspective(m_camera.getFovRadians(), (float)m_width/m_height, 0.1f, 100.f);

        //update view
        m_mvpUBOManager.setValue("view", m_camera.generateView());
        //update projection
        m_mvpUBOManager.setValue("projection", m_projection);

    }

    @Override
    protected void draw() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        //update light model
        m_model.identity().translate(m_lightPos).scale(0.2f);
        glUseProgram(m_programLight);
        glBindVertexArray(m_lightVAO);
        m_mvpUBOManager.setValue("model", m_model);
        glDrawArrays(GL_TRIANGLES,0,36);
        glBindVertexArray(0);

        //update light pos in view space
        m_lightPosInViewSpace.set(m_lightPos,1.0f).mul(m_camera.generateViewMat());
        m_colorUBOManager.setValue("lightPos",
                m_lightPosInViewSpace.x,
                m_lightPosInViewSpace.y,
                m_lightPosInViewSpace.z);

        //draw object
        glUseProgram(m_programObject);
        glBindVertexArray(m_objectVAO);
        //update model
        m_mvpUBOManager.setValue("model", m_model.identity());

        //calculate normalMatrix in view space, should be
        // (view * model).inverse().transpose();
        m_mvpUBOManager.setValue("normalMatrix",
                m_camera.generateViewMat().mul(m_model).invert().transpose());

        glDrawArrays(GL_TRIANGLES,0,36);
        glBindVertexArray(0);
    }

    public static void main(String[] args)  {
        OGLApplicationAbstract application = new BasicLightExe3();
        application.enableFps(true);
        application.run();
    }
}
