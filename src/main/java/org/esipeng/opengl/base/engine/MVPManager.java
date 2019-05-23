package org.esipeng.opengl.base.engine;

import org.esipeng.opengl.base.UBOManager;
import org.joml.Matrix4f;

import static org.lwjgl.opengl.GL33.*;
import static org.esipeng.opengl.base.engine.Const.*;

public class MVPManager {

    private static String MODEL = "model";
    private static String VIEW = "view";
    private static String PROJECTION = "projection";
    private static String NORMALMATRIX = "normalMatrix";

    private UBOManager uboManager;
    private Matrix4f model = new Matrix4f(),
            view = new Matrix4f(),
            projection = new Matrix4f(),
            normalMatrix = new Matrix4f();

    public boolean bindProgram(int program) {
        if(uboManager == null)  {
            uboManager = new UBOManager();
            int vbo = glGenBuffers();
            if(!uboManager.attachUniformBlock(program, "mvp", vbo))
                return false;

            glBindBufferBase(GL_UNIFORM_BUFFER, MVP_BINDING_POINT, vbo);

            if(!uboManager.setValue(MODEL, model))
                return false;

            if(!uboManager.setValue(VIEW, view))
                return false;

            if(!uboManager.setValue(PROJECTION, projection))
                return false;

            if(!uboManager.setValue(NORMALMATRIX, projection))
                return false;
        }

        int uniformBlockLoc = glGetUniformBlockIndex(program, "mvp");
        if(uniformBlockLoc == -1)
            return false;

        glUniformBlockBinding(program, uniformBlockLoc, MVP_BINDING_POINT);
        return true;
    }

    public void updateModel(Matrix4f model) {
        this.model.set(model);
        uboManager.setValue(MODEL, model);
    }

    public void updateView(Matrix4f view)   {
        this.view.set(view);
        uboManager.setValue(VIEW, view);
    }

    public void updateProjection(Matrix4f projection)   {
        this.projection.set(projection);
        uboManager.setValue(PROJECTION, projection);
    }

    public void updateNormalInWorld()   {
        normalMatrix.set(model);
        normalMatrix.invert().transpose();
        uboManager.setValue(NORMALMATRIX, normalMatrix);
    }

    public void updateNormalInView()    {
        view.mul(model, normalMatrix);
        normalMatrix.invert().transpose();
        uboManager.setValue(NORMALMATRIX, normalMatrix);
    }
}
