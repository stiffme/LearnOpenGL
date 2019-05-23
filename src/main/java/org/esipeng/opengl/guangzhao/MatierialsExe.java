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

public class MatierialsExe extends OGLApplicationGL33 {

    private static final int VIEW_PROJECTION_BINDING_POINT = 1;
    private static final int LIGHT_INFO_BINDING_POINT = 2;
    private int m_width = 800, m_height = 800;
    private int NR_OF_INSTANCES = 25;
    private int m_vao, m_vaoLight;
    private int m_program, m_modelsVBO, m_programLight;
    private Camera m_camera;
    private UBOManager m_viewProjectionUBO, m_lightInfoUBO;
    private Matrix4f m_matProjection = new Matrix4f(), m_matNormal = new Matrix4f();
    private Vector4f m_lightPos = new Vector4f();
    private Vector3f m_rotateAxis = new Vector3f(1.0f,1.0f,0.0f).normalize();
    private float[] temp = new float[16];
    private Matrix4f m_lightModel = new Matrix4f();
    private Vector3f m_lightInitialPos = new Vector3f(0.0f,0.0f,8.0f);
    private int m_lastMouseState = GLFW_RELEASE;

    @Override
    protected boolean applicationCreateContext() {
        glfwWindowHint(GLFW_SAMPLES, 4);
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

        float[] matierials = {
                0.0215f, 0.1745f, 0.0215f, 0.07568f, 0.61424f, 0.07568f, 0.633f, 0.727811f, 0.633f,  	0.6f,
                0.135f, 0.2225f, 0.1575f, 0.54f, 0.89f, 0.63f, 0.316228f, 0.316228f, 0.316228f,  	0.1f,
                0.05375f, 0.05f, 0.06625f, 0.18275f, 0.17f, 0.22525f, 0.332741f, 0.328634f, 0.346435f,  	0.3f,
                0.25f, 0.20725f, 0.20725f, 1f, 0.829f, 0.829f, 0.296648f, 0.296648f, 0.296648f,  	0.088f,
                0.1745f, 0.01175f, 0.01175f, 0.61424f, 0.04136f, 0.04136f, 0.727811f, 0.626959f, 0.626959f,  	0.6f,
                0.1f, 0.18725f, 0.1745f, 0.396f, 0.74151f, 0.69102f, 0.297254f, 0.30829f, 0.306678f,  	0.1f,
                0.329412f, 0.223529f, 0.027451f, 0.780392f, 0.568627f, 0.113725f, 0.992157f, 0.941176f, 0.807843f,  	0.21794872f,
                0.2125f, 0.1275f, 0.054f, 0.714f, 0.4284f, 0.18144f, 0.393548f, 0.271906f, 0.166721f,  	0.2f,
                0.25f, 0.25f, 0.25f, 0.4f, 0.4f, 0.4f, 0.774597f, 0.774597f, 0.774597f,  	0.6f,
                0.19125f, 0.0735f, 0.0225f, 0.7038f, 0.27048f, 0.0828f, 0.256777f, 0.137622f, 0.086014f,  	0.1f,
                0.24725f, 0.1995f, 0.0745f, 0.75164f, 0.60648f, 0.22648f, 0.628281f, 0.555802f, 0.366065f,  	0.4f,
                0.19225f, 0.19225f, 0.19225f, 0.50754f, 0.50754f, 0.50754f, 0.508273f, 0.508273f, 0.508273f,  	0.4f,
                0.0f, 0.0f, 0.0f, 0.01f, 0.01f, 0.01f, 0.50f, 0.50f, 0.50f,  	.25f,
                0.0f, 0.1f, 0.06f, 0.0f, 0.50980392f, 0.50980392f, 0.50196078f, 0.50196078f, 0.50196078f,  	.25f,
                0.0f, 0.0f, 0.0f, 0.1f, 0.35f, 0.1f, 0.45f, 0.55f, 0.45f,  	.25f,
                0.0f, 0.0f, 0.0f, 0.5f, 0.0f, 0.0f, 0.7f, 0.6f, 0.6f,  	.25f,
                0.0f, 0.0f, 0.0f, 0.55f, 0.55f, 0.55f, 0.70f, 0.70f, 0.70f,  	.25f,
                0.0f, 0.0f, 0.0f, 0.5f, 0.5f, 0.0f, 0.60f, 0.60f, 0.50f,  	.25f,
                0.02f, 0.02f, 0.02f, 0.01f, 0.01f, 0.01f, 0.4f, 0.4f, 0.4f,  	.078125f,
                0.0f, 0.05f, 0.05f, 0.4f, 0.5f, 0.5f, 0.04f, 0.7f, 0.7f,  	.078125f,
                0.0f, 0.05f, 0.0f, 0.4f, 0.5f, 0.4f, 0.04f, 0.7f, 0.04f,  	.078125f,
                0.05f, 0.0f, 0.0f, 0.5f, 0.4f, 0.4f, 0.7f, 0.04f, 0.04f,  	.078125f,
                0.05f, 0.05f, 0.05f, 0.5f, 0.5f, 0.5f, 0.7f, 0.7f, 0.7f,  	.078125f,
                0.05f, 0.05f, 0.0f, 0.5f, 0.5f, 0.4f, 0.7f, 0.7f, 0.04f,  	.078125f,
        };

        NR_OF_INSTANCES = matierials.length / 10;

        try {
            m_program = compileAndLinkProgram(
                    "MatierialExe/vertex.glsl",
                    "MatierialExe/fragment.glsl"
            );

            m_programLight = compileAndLinkProgram(
                    "MatierialExe/vertexLight.glsl",
                    "MatierialExe/fragmentLight.glsl"
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
        m_modelsVBO = getManagedVBO();
        glBindBuffer(GL_ARRAY_BUFFER, m_modelsVBO);
        nglBufferData(GL_ARRAY_BUFFER,Float.BYTES * 16 * NR_OF_INSTANCES, 0L, GL_STATIC_DRAW);

        glBindBuffer(GL_ARRAY_BUFFER, 0);

        int materialVBO = getManagedVBO();
        glBindBuffer(GL_ARRAY_BUFFER, materialVBO);
        glBufferData(GL_ARRAY_BUFFER, matierials, GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER,0);


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
        glBindBuffer(GL_ARRAY_BUFFER, m_modelsVBO);
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

        glBindBuffer(GL_ARRAY_BUFFER, materialVBO);
        //aAmbient
        glVertexAttribPointer(6,3,GL_FLOAT,false,Float.BYTES * 10, Float.BYTES * 0);
        glEnableVertexAttribArray(6);
        glVertexAttribDivisor(6,1);
        //aDiffuse
        glVertexAttribPointer(7,3,GL_FLOAT,false,Float.BYTES * 10, Float.BYTES * 3);
        glEnableVertexAttribArray(7);
        glVertexAttribDivisor(7,1);
        //aSpecular
        glVertexAttribPointer(8,4,GL_FLOAT,false,Float.BYTES * 10, Float.BYTES * 6);
        glEnableVertexAttribArray(8);
        glVertexAttribDivisor(8,1);

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);

        //build m_vaoLight
        m_vaoLight = getManagedVAO();
        glBindVertexArray(m_vaoLight);
        glBindBuffer(GL_ARRAY_BUFFER, verticesVBO);
        glVertexAttribPointer(0,3,GL_FLOAT,false, Float.BYTES * 6, 0L );
        glEnableVertexAttribArray(0);

        //bindProgram camera
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

        viewProjectionBlockIndex =
                glGetUniformBlockIndex(m_programLight,"ViewProjection");
        glUniformBlockBinding(m_programLight, viewProjectionBlockIndex, VIEW_PROJECTION_BINDING_POINT);

        //LightInfo Uniform
        int lightInfoUBO = getManagedVBO();
        m_lightInfoUBO = new UBOManager();
        if(!m_lightInfoUBO.attachUniformBlock(m_program,"LightInfo", lightInfoUBO))
            return false;
        //bind UBO to binding point
        glBindBufferBase(GL_UNIFORM_BUFFER, LIGHT_INFO_BINDING_POINT,lightInfoUBO);

        int lightInfoBlockIndex =
                glGetUniformBlockIndex(m_program,"LightInfo");
        //bind the uniform block to the binding point
        glUniformBlockBinding(m_program,lightInfoBlockIndex,LIGHT_INFO_BINDING_POINT);

        //update light data
        m_lightInfoUBO.setValue("lightAmbient", 1.0f,1.0f,1.0f);
        m_lightInfoUBO.setValue("lightDiffuse", 1.0f,1.0f,1.0f);
        m_lightInfoUBO.setValue("lightSpecular", 1.0f,1.0f,1.0f);
        //light POS will be updated in update() when m_camera is changed

        glClearColor(0.2f,0.3f,0.3f,1.0f);
        glEnable(GL_DEPTH_TEST);
        glDepthFunc(GL_LEQUAL);
        glEnable(GL_MULTISAMPLE);
        m_camera.enableMouseFpsView();
        return true;
    }

    @Override
    protected void update(float elapsed) {
        m_camera.processInput(elapsed);
        int currentMouseState = glfwGetMouseButton(glfwGetCurrentContext(), GLFW_MOUSE_BUTTON_1);
        if(m_lastMouseState != currentMouseState && currentMouseState == GLFW_PRESS)
            m_rotateAxis.y = -m_rotateAxis.y;
        m_lastMouseState = currentMouseState;

        //update models
        glBindBuffer(GL_ARRAY_BUFFER,m_modelsVBO);
        Matrix4f matModel = new Matrix4f();
        int ITEM_NUMBER = (int)Math.ceil(Math.sqrt(NR_OF_INSTANCES));
        float DISP = 10.f / (ITEM_NUMBER + 1);
        float scaleFloat = 10.f/ ITEM_NUMBER * 0.5f;
        float[] temp = new float[16];
        //spread the instances
        for(int instance = 0; instance < NR_OF_INSTANCES; ++instance)   {
            int col = instance % ITEM_NUMBER;
            int row = instance / ITEM_NUMBER;
            float dispX = DISP * (col + 1) - 5.f;
            float dispY = -DISP * (row + 1) + 5.f;
            matModel.identity()
                    .translate(dispX, dispY, 0.0f)
                    .scale(scaleFloat);
                    //.rotate((float)glfwGetTime(),m_rotateAxis);
            //update the VBO
            glBufferSubData(GL_ARRAY_BUFFER,Float.BYTES * 16 * instance, matModel.get(temp));
        }
        glBindBuffer(GL_ARRAY_BUFFER, 0);

        //update view
        m_viewProjectionUBO.setValue("view", m_camera.generateView());
        //update projection
        m_matProjection.identity().setPerspective(
                m_camera.getFovRadians(), (float)m_width/m_height, 0.1f, 100.f
        );
        m_viewProjectionUBO.setValue("projection", m_matProjection);
        //normal matrix is calculated in vertex shader

        float time = (float)glfwGetTime();
        //calculate light pos in view
        m_lightPos.set(m_lightInitialPos,1.0f)
                .rotateAbout(time,m_rotateAxis.x, m_rotateAxis.y, m_rotateAxis.z)
                .mul(m_camera.generateViewMat());
        m_lightInfoUBO.setValue("lightPos", m_lightPos);


        m_lightModel.identity()
                .rotate(time,m_rotateAxis.x, m_rotateAxis.y, m_rotateAxis.z)
                .translate(m_lightInitialPos)
                .scale(0.1f);

        int lightModelLoc = glGetUniformLocation(m_programLight,"lightModel");
        glUniformMatrix4fv(lightModelLoc,false, m_lightModel.get(temp));
    }

    @Override
    protected void draw() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glUseProgram(m_program);
        glBindVertexArray(m_vao);
        glDrawArraysInstanced(GL_TRIANGLES,0,36,NR_OF_INSTANCES);
        glBindVertexArray(0);

        glUseProgram(m_programLight);
        glBindVertexArray(m_vaoLight);
        glDrawArrays(GL_TRIANGLES,0,36);
        glBindVertexArray(0);
    }

    @Override
    protected void frameBufferSizeChanged(long window, int width, int height) {
        super.frameBufferSizeChanged(window, width, height);
        m_width = width;
        m_height = height;
    }

    public static void main(String[] args)  {
        OGLApplicationAbstract application = new MatierialsExe();
        application.run();
    }
}
