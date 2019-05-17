package org.esipeng.opengl.base.engine;

import org.esipeng.opengl.base.engine.spi.MaterialReposibory;
import org.lwjgl.assimp.AIFace;
import org.lwjgl.assimp.AIMesh;
import org.lwjgl.assimp.AIVector3D;
import org.lwjgl.system.MemoryUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL33.*;

public class Mesh {
    private static final Logger logger = LoggerFactory.getLogger(Mesh.class);
    private int mVBO, mVAO, mEBO;
    private int mNumberOfIndices;
    private Material mMaterial;

    public Mesh(AIMesh aiMesh, MaterialReposibory materialReposibory)  {
        mVBO = glGenBuffers();
        mVAO = glGenVertexArrays();
        mEBO = glGenBuffers();

        //vertex 3f normal 3f texcoord 2f
        FloatBuffer verticesBuf = MemoryUtil.memAllocFloat( aiMesh.mNumVertices() * 8);
        logger.debug("Loading {} vertices", aiMesh.mNumVertices());
        for(int i = 0; i < aiMesh.mNumVertices(); ++i)  {
            //vertex x y z
            AIVector3D vertex = aiMesh.mVertices().get(i);
            verticesBuf.put(vertex.x());
            verticesBuf.put(vertex.y());
            verticesBuf.put(vertex.z());

            //normal
            AIVector3D normal = aiMesh.mNormals().get(i);
            verticesBuf.put(normal.x());
            verticesBuf.put(normal.y());
            verticesBuf.put(normal.z());

            //first texcoord
            if(aiMesh.mTextureCoords(0) == null)    {
                verticesBuf.put(0.0f);
                verticesBuf.put(0.0f);
                logger.debug("No texture found. put 0.0");
            } else  {
                AIVector3D texcoord = aiMesh.mTextureCoords(0).get(i);
                if(texcoord != null)    {
                    verticesBuf.put(texcoord.x());
                    verticesBuf.put(texcoord.y());
                } else  {
                    verticesBuf.put(0.0f);
                    verticesBuf.put(0.0f);
                    logger.debug("No texture found. put 0.0");
                }
            }
        }
        verticesBuf.flip();
        glBindBuffer(GL_ARRAY_BUFFER, mVBO);
        glBufferData(GL_ARRAY_BUFFER,verticesBuf,GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        MemoryUtil.memFree(verticesBuf);

        //EBO
        logger.debug("Loading {} faces", aiMesh.mNumFaces());
        mNumberOfIndices = aiMesh.mNumFaces() * 3;
        IntBuffer indicesBuf = MemoryUtil.memAllocInt(mNumberOfIndices);
        for(int i = 0; i < aiMesh.mNumFaces(); ++i) {
            AIFace aiFace = aiMesh.mFaces().get(i);
            if(aiFace.mNumIndices() != 3)
                logger.warn("Not a triangle!");
            for(int j = 0; j < aiFace.mNumIndices(); ++j)
                indicesBuf.put(aiFace.mIndices().get(j));
        }
        indicesBuf.flip();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, mEBO);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indicesBuf, GL_STATIC_DRAW);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
        MemoryUtil.memFree(indicesBuf);

        //build VAO
        glBindVertexArray(mVAO);
        glBindBuffer(GL_ARRAY_BUFFER, mVBO);
        glVertexAttribPointer(0,3,GL_FLOAT,false, Float.BYTES * 8, Float.BYTES * 0);
        glVertexAttribPointer(1,3,GL_FLOAT,false,Float.BYTES * 8,Float.BYTES * 3);
        glVertexAttribPointer(2,2,GL_FLOAT,false,Float.BYTES * 8, Float.BYTES * 6);
        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);
        glEnableVertexAttribArray(2);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, mEBO);
        glBindVertexArray(0);

        mMaterial = materialReposibory.getMaterial(aiMesh.mMaterialIndex());
    }

    public void draw()  {
        //bind material
        mMaterial.bindMaterial();

        //bind VAO
        glBindVertexArray(mVAO);
        glDrawElements(GL_TRIANGLES, mNumberOfIndices, GL_UNSIGNED_INT, 0L);
        glBindVertexArray(0);
    }

    public void release()   {
        glDeleteBuffers(mEBO);
        glDeleteBuffers(mVBO);
        glDeleteVertexArrays(mVAO);
    }

//    public Mesh()   {
//        mVBO = glGenBuffers();
//        mVAO = glGenVertexArrays();
//        mEBO = glGenBuffers();
//
//        float[] vertices = {
//                // positions          // normals           // texture coords
//                -0.5f, -0.5f, -0.5f,  0.0f,  0.0f, -1.0f,  0.0f, 0.0f,
//                0.5f, -0.5f, -0.5f,  0.0f,  0.0f, -1.0f,  1.0f, 0.0f,
//                0.5f,  0.5f, -0.5f,  0.0f,  0.0f, -1.0f,  1.0f, 1.0f,
//                0.5f,  0.5f, -0.5f,  0.0f,  0.0f, -1.0f,  1.0f, 1.0f,
//                -0.5f,  0.5f, -0.5f,  0.0f,  0.0f, -1.0f,  0.0f, 1.0f,
//                -0.5f, -0.5f, -0.5f,  0.0f,  0.0f, -1.0f,  0.0f, 0.0f,
//
//                -0.5f, -0.5f,  0.5f,  0.0f,  0.0f, 1.0f,   0.0f, 0.0f,
//                0.5f, -0.5f,  0.5f,  0.0f,  0.0f, 1.0f,   1.0f, 0.0f,
//                0.5f,  0.5f,  0.5f,  0.0f,  0.0f, 1.0f,   1.0f, 1.0f,
//                0.5f,  0.5f,  0.5f,  0.0f,  0.0f, 1.0f,   1.0f, 1.0f,
//                -0.5f,  0.5f,  0.5f,  0.0f,  0.0f, 1.0f,   0.0f, 1.0f,
//                -0.5f, -0.5f,  0.5f,  0.0f,  0.0f, 1.0f,   0.0f, 0.0f,
//
//                -0.5f,  0.5f,  0.5f, -1.0f,  0.0f,  0.0f,  1.0f, 0.0f,
//                -0.5f,  0.5f, -0.5f, -1.0f,  0.0f,  0.0f,  1.0f, 1.0f,
//                -0.5f, -0.5f, -0.5f, -1.0f,  0.0f,  0.0f,  0.0f, 1.0f,
//                -0.5f, -0.5f, -0.5f, -1.0f,  0.0f,  0.0f,  0.0f, 1.0f,
//                -0.5f, -0.5f,  0.5f, -1.0f,  0.0f,  0.0f,  0.0f, 0.0f,
//                -0.5f,  0.5f,  0.5f, -1.0f,  0.0f,  0.0f,  1.0f, 0.0f,
//
//                0.5f,  0.5f,  0.5f,  1.0f,  0.0f,  0.0f,  1.0f, 0.0f,
//                0.5f,  0.5f, -0.5f,  1.0f,  0.0f,  0.0f,  1.0f, 1.0f,
//                0.5f, -0.5f, -0.5f,  1.0f,  0.0f,  0.0f,  0.0f, 1.0f,
//                0.5f, -0.5f, -0.5f,  1.0f,  0.0f,  0.0f,  0.0f, 1.0f,
//                0.5f, -0.5f,  0.5f,  1.0f,  0.0f,  0.0f,  0.0f, 0.0f,
//                0.5f,  0.5f,  0.5f,  1.0f,  0.0f,  0.0f,  1.0f, 0.0f,
//
//                -0.5f, -0.5f, -0.5f,  0.0f, -1.0f,  0.0f,  0.0f, 1.0f,
//                0.5f, -0.5f, -0.5f,  0.0f, -1.0f,  0.0f,  1.0f, 1.0f,
//                0.5f, -0.5f,  0.5f,  0.0f, -1.0f,  0.0f,  1.0f, 0.0f,
//                0.5f, -0.5f,  0.5f,  0.0f, -1.0f,  0.0f,  1.0f, 0.0f,
//                -0.5f, -0.5f,  0.5f,  0.0f, -1.0f,  0.0f,  0.0f, 0.0f,
//                -0.5f, -0.5f, -0.5f,  0.0f, -1.0f,  0.0f,  0.0f, 1.0f,
//
//                -0.5f,  0.5f, -0.5f,  0.0f,  1.0f,  0.0f,  0.0f, 1.0f,
//                0.5f,  0.5f, -0.5f,  0.0f,  1.0f,  0.0f,  1.0f, 1.0f,
//                0.5f,  0.5f,  0.5f,  0.0f,  1.0f,  0.0f,  1.0f, 0.0f,
//                0.5f,  0.5f,  0.5f,  0.0f,  1.0f,  0.0f,  1.0f, 0.0f,
//                -0.5f,  0.5f,  0.5f,  0.0f,  1.0f,  0.0f,  0.0f, 0.0f,
//                -0.5f,  0.5f, -0.5f,  0.0f,  1.0f,  0.0f,  0.0f, 1.0f
//        };
//
//        //vertex 3f normal 3f texcoord 2f
//        FloatBuffer verticesBuf = MemoryUtil.memAllocFloat( vertices.length );
//        verticesBuf.put(vertices);
//        verticesBuf.flip();
//        glBindBuffer(GL_ARRAY_BUFFER, mVBO);
//        glBufferData(GL_ARRAY_BUFFER,verticesBuf,GL_STATIC_DRAW);
//        glBindBuffer(GL_ARRAY_BUFFER, 0);
//        MemoryUtil.memFree(verticesBuf);
//
//        //EBO
//        IntBuffer indicesBuf = MemoryUtil.memAllocInt(vertices.length / 8);
//        for(int index = 0; index < vertices.length / 8; ++index)
//            indicesBuf.put(index);
//
//        indicesBuf.flip();
//        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, mEBO);
//        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indicesBuf, GL_STATIC_DRAW);
//        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
//        MemoryUtil.memFree(indicesBuf);
//
//        //build VAO
//        glBindVertexArray(mVAO);
//        glBindBuffer(GL_ARRAY_BUFFER, mVBO);
//        glVertexAttribPointer(0,3,GL_FLOAT,false, Float.BYTES * 8, Float.BYTES * 0);
//        glVertexAttribPointer(1,3,GL_FLOAT,false,Float.BYTES * 8,Float.BYTES * 3);
//        glVertexAttribPointer(2,2,GL_FLOAT,false,Float.BYTES * 8, Float.BYTES * 6);
//        glEnableVertexAttribArray(0);
//        glEnableVertexAttribArray(1);
//        glEnableVertexAttribArray(2);
//        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, mEBO);
//        glBindVertexArray(0);
//
//        mNumberOfIndices = vertices.length / 8;
//        mMaterialIndex = 0;
//    }
}
