package org.esipeng.opengl.guangzhao;

import org.esipeng.opengl.base.*;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL33.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class LightingMap extends OGLApplicationGL33 {

    private final static int MVP_BINDING_POINT = 1;
    private final static int COLOR_BINDING_POINT = 2;
    private final static int MATIERIAL_BINDING_POINT = 3;

    private int m_width = 800, m_height = 600;
    private int m_programObject, m_programLight;

    private int m_objectVAO, m_lightVAO;
    private Camera m_camera;
    private UBOManager m_mvpUBOManager, m_colorUBOManager, m_materialUBOManager;
    private Matrix4f m_model, m_projection;
    private Vector3f m_lightPos = new Vector3f(1.2f,1.0f,2.0f), m_lightColor = new Vector3f();
    private Vector4f m_lightPosInViewSpace = new Vector4f(0.0f);

    private int m_textureBox2, m_textureSpecular, m_textureMatrix;

    @Override
    protected boolean applicationCreateContext() {
        long window = glfwCreateWindow(m_width, m_height, "Lighting Map", NULL, NULL);
        if(window == NULL)
            return false;
        glfwMakeContextCurrent(window);
        return true;
    }

    @Override
    protected boolean applicationInitAfterContext() {
        float[] vertices = {
                // positions          // normals           // texture coords
                -0.5f, -0.5f, -0.5f,  0.0f,  0.0f, -1.0f,  0.0f, 0.0f,
                0.5f, -0.5f, -0.5f,  0.0f,  0.0f, -1.0f,  1.0f, 0.0f,
                0.5f,  0.5f, -0.5f,  0.0f,  0.0f, -1.0f,  1.0f, 1.0f,
                0.5f,  0.5f, -0.5f,  0.0f,  0.0f, -1.0f,  1.0f, 1.0f,
                -0.5f,  0.5f, -0.5f,  0.0f,  0.0f, -1.0f,  0.0f, 1.0f,
                -0.5f, -0.5f, -0.5f,  0.0f,  0.0f, -1.0f,  0.0f, 0.0f,

                -0.5f, -0.5f,  0.5f,  0.0f,  0.0f, 1.0f,   0.0f, 0.0f,
                0.5f, -0.5f,  0.5f,  0.0f,  0.0f, 1.0f,   1.0f, 0.0f,
                0.5f,  0.5f,  0.5f,  0.0f,  0.0f, 1.0f,   1.0f, 1.0f,
                0.5f,  0.5f,  0.5f,  0.0f,  0.0f, 1.0f,   1.0f, 1.0f,
                -0.5f,  0.5f,  0.5f,  0.0f,  0.0f, 1.0f,   0.0f, 1.0f,
                -0.5f, -0.5f,  0.5f,  0.0f,  0.0f, 1.0f,   0.0f, 0.0f,

                -0.5f,  0.5f,  0.5f, -1.0f,  0.0f,  0.0f,  1.0f, 0.0f,
                -0.5f,  0.5f, -0.5f, -1.0f,  0.0f,  0.0f,  1.0f, 1.0f,
                -0.5f, -0.5f, -0.5f, -1.0f,  0.0f,  0.0f,  0.0f, 1.0f,
                -0.5f, -0.5f, -0.5f, -1.0f,  0.0f,  0.0f,  0.0f, 1.0f,
                -0.5f, -0.5f,  0.5f, -1.0f,  0.0f,  0.0f,  0.0f, 0.0f,
                -0.5f,  0.5f,  0.5f, -1.0f,  0.0f,  0.0f,  1.0f, 0.0f,

                0.5f,  0.5f,  0.5f,  1.0f,  0.0f,  0.0f,  1.0f, 0.0f,
                0.5f,  0.5f, -0.5f,  1.0f,  0.0f,  0.0f,  1.0f, 1.0f,
                0.5f, -0.5f, -0.5f,  1.0f,  0.0f,  0.0f,  0.0f, 1.0f,
                0.5f, -0.5f, -0.5f,  1.0f,  0.0f,  0.0f,  0.0f, 1.0f,
                0.5f, -0.5f,  0.5f,  1.0f,  0.0f,  0.0f,  0.0f, 0.0f,
                0.5f,  0.5f,  0.5f,  1.0f,  0.0f,  0.0f,  1.0f, 0.0f,

                -0.5f, -0.5f, -0.5f,  0.0f, -1.0f,  0.0f,  0.0f, 1.0f,
                0.5f, -0.5f, -0.5f,  0.0f, -1.0f,  0.0f,  1.0f, 1.0f,
                0.5f, -0.5f,  0.5f,  0.0f, -1.0f,  0.0f,  1.0f, 0.0f,
                0.5f, -0.5f,  0.5f,  0.0f, -1.0f,  0.0f,  1.0f, 0.0f,
                -0.5f, -0.5f,  0.5f,  0.0f, -1.0f,  0.0f,  0.0f, 0.0f,
                -0.5f, -0.5f, -0.5f,  0.0f, -1.0f,  0.0f,  0.0f, 1.0f,

                -0.5f,  0.5f, -0.5f,  0.0f,  1.0f,  0.0f,  0.0f, 1.0f,
                0.5f,  0.5f, -0.5f,  0.0f,  1.0f,  0.0f,  1.0f, 1.0f,
                0.5f,  0.5f,  0.5f,  0.0f,  1.0f,  0.0f,  1.0f, 0.0f,
                0.5f,  0.5f,  0.5f,  0.0f,  1.0f,  0.0f,  1.0f, 0.0f,
                -0.5f,  0.5f,  0.5f,  0.0f,  1.0f,  0.0f,  0.0f, 0.0f,
                -0.5f,  0.5f, -0.5f,  0.0f,  1.0f,  0.0f,  0.0f, 1.0f
        };

        try {
            m_programObject = compileAndLinkProgram(
                    "LightingMap/vertex.glsl",
                    "LightingMap/fragment.glsl"
            );

            m_programLight = compileAndLinkProgram(
                    "LightingMap/vertex.glsl",
                    "LightingMap/fragmentLight.glsl"
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
        glVertexAttribPointer(0,3,GL_FLOAT,false,Float.BYTES * 8, 0L);
        glEnableVertexAttribArray(1);
        glVertexAttribPointer(1, 3,GL_FLOAT,false,Float.BYTES * 8, Float.BYTES * 3);
        glEnableVertexAttribArray(2);
        glVertexAttribPointer(2, 2,GL_FLOAT,false,Float.BYTES * 8, Float.BYTES * 6);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);

        //light VAO
        m_lightVAO = getManagedVAO();
        glBindVertexArray(m_lightVAO);
        glBindBuffer(GL_ARRAY_BUFFER, vboVertices);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(0,3,GL_FLOAT,false,Float.BYTES * 8, 0L);
        glEnableVertexAttribArray(1);
        glVertexAttribPointer(1, 3,GL_FLOAT,false,Float.BYTES * 8, Float.BYTES * 3);
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
        //m_materialUBOManager.setValue("mDiffuse", 5);
        //m_materialUBOManager.setValue("mSpecular", 0.5f, 0.5f, 0.5f);
        m_materialUBOManager.setValue("mShininess", 32.0f);

        //bind texture mDiffuse to texture 5
        int textureLoc = glGetUniformLocation(m_programObject,"mDiffuse");
        if(textureLoc == -1)
            return false;
        glUseProgram(m_programObject);
        glUniform1i(textureLoc, 5);

        //bind texture mSpecular to texture 6
        textureLoc = glGetUniformLocation(m_programObject,"mSpecular");
        if(textureLoc == -1)
            return false;
        glUseProgram(m_programObject);
        glUniform1i(textureLoc, 6);

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
                0.f,0.f,3.f,
                0.f,0.f,-1.f,
                0.f,1.f,0.f,
                m_width/2, m_height/2,
                glfwGetCurrentContext()
        );

        //matrix initializinginvert()
        m_model = new Matrix4f();
        //m_view mat is from m_camera
        m_projection = new Matrix4f();

        //load texture
        try{
            TextureLoader textureLoader = new TextureLoader();
            if(!textureLoader.loadFromResource("LightingMap/container2.png"))
                return false;
            m_textureBox2 = getManagedTexture();
            glBindTexture(GL_TEXTURE_2D,m_textureBox2);
            glTexImage2D(GL_TEXTURE_2D,
                    0,
                    GL_RGBA,
                    textureLoader.getX(),
                    textureLoader.getY(),
                    0,
                    GL_RGBA,
                    GL_UNSIGNED_BYTE,
                    textureLoader.getData());

            glGenerateMipmap(GL_TEXTURE_2D);
            //texture parameters
            glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_WRAP_S,GL_REPEAT);
            glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_WRAP_T,GL_REPEAT);
            glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_MIN_FILTER,GL_LINEAR_MIPMAP_NEAREST);
            glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_MAG_FILTER,GL_LINEAR);
            glBindBuffer(GL_TEXTURE_2D,0);

            if(!textureLoader.loadFromResource("LightingMap/container2_specular.png"))
                return false;

            m_textureSpecular = getManagedTexture();
            glBindTexture(GL_TEXTURE_2D, m_textureSpecular);
            glTexImage2D(GL_TEXTURE_2D,
                    0,
                    GL_RGBA,
                    textureLoader.getX(),
                    textureLoader.getY(),
                    0,
                    GL_RGBA,
                    GL_UNSIGNED_BYTE,
                    textureLoader.getData());

            glGenerateMipmap(GL_TEXTURE_2D);
            //texture parameters
            glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_WRAP_S,GL_REPEAT);
            glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_WRAP_T,GL_REPEAT);
            glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_MIN_FILTER,GL_LINEAR_MIPMAP_NEAREST);
            glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_MAG_FILTER,GL_LINEAR);
            glBindBuffer(GL_TEXTURE_2D,0);

            if(!textureLoader.loadFromResource("LightingMap/matrix.jpg"))
                return false;

            m_textureMatrix = getManagedTexture();
            glBindTexture(GL_TEXTURE_2D, m_textureSpecular);
            glTexImage2D(GL_TEXTURE_2D,
                    0,
                    GL_RGB,
                    textureLoader.getX(),
                    textureLoader.getY(),
                    0,
                    GL_RGB,
                    GL_UNSIGNED_BYTE,
                    textureLoader.getData());

            glGenerateMipmap(GL_TEXTURE_2D);
            //texture parameters
            glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_WRAP_S,GL_REPEAT);
            glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_WRAP_T,GL_REPEAT);
            glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_MIN_FILTER,GL_LINEAR_MIPMAP_LINEAR);
            glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_MAG_FILTER,GL_LINEAR);
            glBindBuffer(GL_TEXTURE_2D,0);

            textureLoader.release();

        } catch (Exception e)   {
            e.printStackTrace();
            return false;
        }


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

        glActiveTexture(GL_TEXTURE5);
        glBindTexture(GL_TEXTURE_2D, m_textureBox2);

        glActiveTexture(GL_TEXTURE6);
        glBindTexture(GL_TEXTURE_2D, m_textureSpecular);

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
        OGLApplicationAbstract application = new LightingMap();
        application.enableFps(true);
        application.run();
    }
}
