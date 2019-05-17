package org.esipeng.opengl.moxin;

import org.esipeng.opengl.base.Camera;
import org.esipeng.opengl.base.OGLApplicationAbstract;
import org.esipeng.opengl.base.OGLApplicationGL33;
import org.esipeng.opengl.base.UBOManager;
import org.esipeng.opengl.base.engine.Material;
import org.esipeng.opengl.base.engine.Scene;
import org.joml.Matrix4f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import static org.lwjgl.opengl.GL33.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Moxin extends OGLApplicationGL33 {
    private static final Logger logger = LoggerFactory.getLogger(Moxin.class);
    private static final int MVP_BINDING_POINT = 1;

    private int m_width = 800, m_height = 800;
    private long m_window;
    private Scene m_scene;

    private UBOManager m_mvpUBO;
    private int m_program;
    private Camera m_camera;

    private Matrix4f m_model, m_projection;

    @Override
    protected boolean applicationCreateContext() {
        m_window = glfwCreateWindow(m_width, m_height, "MoXin", NULL, NULL);
        if(m_window == NULL)
            return false;
        glfwMakeContextCurrent(m_window);
        return true;
    }

    @Override
    protected boolean applicationInitAfterContext() {
        m_scene = new Scene();
        if(!m_scene.loadSceneFromResource("nanosuit/nanosuit.obj"))
            return false;

        try {
            m_program = compileAndLinkProgram(
                    "engine/vertex.glsl",
                    "engine/fragment.glsl"
            );
        } catch (Exception e)   {
            e.printStackTrace();
            return false;
        }

        //mvp UBO
        m_mvpUBO = new UBOManager();
        int mvpVBO = getManagedVBO();
        if(!m_mvpUBO.attachUniformBlock(m_program,"mvp", mvpVBO))
            return false;
        //bind the buffer to the binding point
        glBindBufferBase(GL_UNIFORM_BUFFER, MVP_BINDING_POINT, mvpVBO);
        //bind the block index to the binding point
        glUniformBlockBinding(m_program, m_mvpUBO.getBlockIndex(), MVP_BINDING_POINT);

        //set texture uniform
        for(Map.Entry<String,Integer> entry : Material.SAMPLER_MAP.entrySet()) {
            logger.debug("Setting sampler uniform {} -> {}", entry.getKey(), entry.getValue());
            setUniform1i(m_program, entry.getKey(), entry.getValue());
        }

        m_camera = new Camera(m_window);
        m_camera.enableMouseFpsView();

        m_model = new Matrix4f();
        m_projection = new Matrix4f();

        glEnable(GL_DEPTH_TEST);
        glDepthFunc(GL_LEQUAL);
        glClearColor(0.05f, 0.05f, 0.05f, 1.0f);

        enableFps(true);
        return true;
    }

    @Override
    protected void update(float elapsed) {
        m_camera.processInput(elapsed);

        //update mvp
        m_model.identity().translate(0.f,-1.75f, 0.f).scale(0.2f);
        m_mvpUBO.setValue("model", m_model);
        m_mvpUBO.setValue("view", m_camera.generateViewMat());

        //calculate perspective
        m_projection.identity().setPerspective(m_camera.getFovRadians(),
                (float)(m_width / m_height), 0.1f, 100.f);

        m_mvpUBO.setValue("projection", m_projection);
    }

    @Override
    protected void draw() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glUseProgram(m_program);
        m_scene.draw();
    }

    @Override
    protected void destroy() {
        super.destroy();
        m_scene.release();
    }

    public static void main(String[] args)  {
        OGLApplicationAbstract application = new Moxin();
        application.run();
    }
}
