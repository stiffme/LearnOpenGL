package org.esipeng.opengl.moxin;

import org.esipeng.opengl.base.Camera;
import org.esipeng.opengl.base.OGLApplicationAbstract;
import org.esipeng.opengl.base.OGLApplicationGL33;
import org.esipeng.opengl.base.UBOManager;
import org.esipeng.opengl.base.engine.LightsManager;
import org.esipeng.opengl.base.engine.Material;
import org.esipeng.opengl.base.engine.Scene;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import static org.lwjgl.opengl.GL33.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.MemoryUtil.NULL;
import static org.esipeng.opengl.base.engine.Const.MATERIAL_BINDING_POINT;

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
    private LightsManager lightsManager;

    public String modelPath = "";

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

        try {
            m_program = compileAndLinkProgram(
                    "engine/vertex.glsl",
                    "engine/fragment.glsl"
            );
        } catch (Exception e)   {
            e.printStackTrace();
            return false;
        }

        m_scene = new Scene(m_program);

        if(modelPath.length() != 0) {
            if(!m_scene.loadScene(modelPath))
                return false;
        } else  {
            if(!m_scene.loadSceneFromResource("nanosuit/nanosuit.obj"))
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

//        //set texture uniform
//        for(Map.Entry<String,Integer> entry : Material.SAMPLER_MAP.entrySet()) {
//            logger.debug("Setting sampler uniform {} -> {}", entry.getKey(), entry.getValue());
//            setUniform1i(m_program, entry.getKey(), entry.getValue());
//        }
//
//        //bind material UBO to the binding point
//        int materialLocation = glGetUniformBlockIndex(m_program, "Material");
//        if(materialLocation == -1)  {
//            logger.error("Material uniform block not found!");
//        } else {
//            glUniformBlockBinding(m_program,materialLocation, MATERIAL_BINDING_POINT);
//        }
        m_scene.bindProgram(m_program);

        m_camera = new Camera(m_window);
        m_camera.enableMouseFpsView();

        m_model = new Matrix4f();
        m_projection = new Matrix4f();

        glEnable(GL_DEPTH_TEST);
        glDepthFunc(GL_LEQUAL);
        glClearColor(0.05f, 0.05f, 0.05f, 1.0f);

        lightsManager = new LightsManager();
        if(!lightsManager.init(m_program))
            return false;

        lightsManager.createDirectionalLight(
                new Vector3f(0.4f),
                new Vector3f(1.0f),
                new Vector3f(1.0f),
                new Vector3f(1.f,0.f,0.f));

        lightsManager.createPointLight(
                new Vector3f(0.4f),
                new Vector3f(1.0f),
                new Vector3f(1.0f),
                new Vector3f(0.f,0.f,8.f),
                1.0f, 0.0f,0.0f);

        lightsManager.createSpotLight(
                new Vector3f(0.4f),
                new Vector3f(1.0f),
                new Vector3f(1.0f),
                1.0f,0.0f,0.0f,
                (float)Math.cos(Math.toRadians(12.5f)), (float)Math.cos(Math.toRadians(15.0f)));


        //enableFps(true);
        return true;
    }

    @Override
    protected void update(float elapsed) {
        m_camera.processInput(elapsed);

        //update mvp
        m_model.identity().translate(0.f,-1.75f, 0.f).scale(0.3f);
        m_mvpUBO.setValue("model", m_model);
        Matrix4f viewMat = m_camera.generateViewMat();
        m_mvpUBO.setValue("view", viewMat);

        lightsManager.updateAllLights(viewMat);

        //calculate perspective
        m_projection.identity().setPerspective(m_camera.getFovRadians(),
                (float)(m_width / m_height), 0.1f, 100.f);

        m_mvpUBO.setValue("projection", m_projection);

        //update normal matrix
        m_mvpUBO.setValue("normalMatrix",
                m_camera.generateViewMat().mul(m_model).invert().transpose());

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
        Moxin application = new Moxin();
        if(args.length > 0)
            application.modelPath = args[0];

        application.run();
    }
}
