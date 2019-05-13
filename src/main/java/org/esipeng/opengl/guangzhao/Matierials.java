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

public class Matierials extends OGLApplicationGL33 {

    private final static int MVP_BINDING_POINT = 1;
    private final static int COLOR_BINDING_POINT = 2;
    private final static int MATIERIAL_BINDING_POINT = 3;

    private int m_width = 800, m_height = 600;
    private int m_programObject, m_programLight;

    private int m_objectVAO, m_lightVAO;
    private Camera m_camera;
    private UBOManager m_mvpUBOManager, m_colorUBOManager, m_materialUBOManager;
    private Matrix4f m_model, m_projection;
    private Vector3f m_lightPos = new Vector3f(0.6f,0.0f,5.0f), m_lightColor = new Vector3f();
    private Vector4f m_lightPosInViewSpace = new Vector4f(0.0f);

    @Override
    protected boolean applicationCreateContext() {
        long window = glfwCreateWindow(m_width, m_height, "Matierials", NULL, NULL);
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
                    "Matierials/vertex.glsl",
                    "Matierials/fragment.glsl"
            );

            m_programLight = compileAndLinkProgram(
                    "Matierials/vertex.glsl",
                    "Matierials/fragmentLight.glsl"
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

        int materialUBO = getManagedVBO();
        m_materialUBOManager = new UBOManager();
        if(!m_materialUBOManager.attachUniformBlock(m_programObject, "Material", materialUBO))
            return false;
        glBindBufferBase(GL_UNIFORM_BUFFER, MATIERIAL_BINDING_POINT, materialUBO);

        //ColorBlock data
        m_colorUBOManager.setValue("lightColor", 1.0f,1.0f,1.0f);
        m_colorUBOManager.setValue("objectColor", 1.0f,0.5f,0.31f);
        m_colorUBOManager.setValue("lAmbient", 0.2f,0.2f,0.2f);
        m_colorUBOManager.setValue("lDiffuse", 0.5f,0.5f,0.5f);
        m_colorUBOManager.setValue("lSpecular", 1.0f,1.0f,1.0f);

        //Matierial Block data
        m_materialUBOManager.setValue("mAmbient", 1.0f, 0.5f, 0.31f);
        m_materialUBOManager.setValue("mDiffuse", 1.0f, 0.5f, 0.31f);
        m_materialUBOManager.setValue("mSpecular", 0.5f, 0.5f, 0.5f);
        m_materialUBOManager.setValue("mShininess", 32.0f);

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

        //bind Matierial block to binding point
        int matirialBlockIndex = glGetUniformBlockIndex(m_programObject,"Material");
        glUniformBlockBinding(m_programObject,matirialBlockIndex, MATIERIAL_BINDING_POINT);

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

        //update light color based on time
        m_lightColor.x = (float)Math.sin(glfwGetTime()) * 2.0f;
        m_lightColor.y = (float)Math.sin(glfwGetTime()) * 0.7f;
        m_lightColor.z = (float)Math.sin(glfwGetTime()) * 0.3f;

        //diffuse -> scale to 0.5
        m_lightColor.mul(0.5f);
        m_colorUBOManager.setValue("lDiffuse", m_lightColor);
        //ambient -> scale to 0.2 (0.5 * 0.4)
        m_lightColor.mul(0.4f);
        m_colorUBOManager.setValue("lAmbient", m_lightColor);
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
        OGLApplicationAbstract application = new Matierials();
        application.enableFps(true);
        application.run();
    }
}
