package org.esipeng.opengl.base.engine;

import java.util.List;

public class MeshNode {
    private List<Mesh> meshes;
    private List<MeshNode> children;

    public MeshNode(List<Mesh> meshes, List<MeshNode> children) {
        this.meshes = meshes;
        this.children = children;
    }

    public void draw()  {
        for(Mesh mesh : meshes) {
            mesh.draw();
        }

        for(MeshNode child : children)
            child.draw();
    }

    public void drawInstanced(int nrInstances) {
        for(Mesh mesh : meshes) {
            mesh.drawInstanced(nrInstances);
        }

        for(MeshNode child : children)
            child.drawInstanced(nrInstances);
    }

    public List<Mesh> getMeshes() {
        return meshes;
    }

    public List<MeshNode> getChildren() {
        return children;
    }
}
