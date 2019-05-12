package org.esipeng.opengl.base;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryUtil;

import java.util.HashMap;

import static org.lwjgl.opengl.GL33.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class UBOManager {

    private class UniformInformation    {
        private int blockOffset, blockSizeInBytes;

        public UniformInformation(int offset, int sizeinBytes)  {
            blockOffset = offset;
            blockSizeInBytes = sizeinBytes;
        }

        public int getBlockOffset() {
            return blockOffset;
        }

        public int getBlockSizeInBytes() {
            return blockSizeInBytes;
        }
    }
    int m_ubo;
    HashMap<String, UniformInformation> m_indices;
    float[] temp1 = new float[1], temp2 = new float[2], temp3 = new float[3], temp4 = new float[4];
    float[] temp16 = new float[16];

    public boolean attachUniformBlock(int program, String uniformBlockName, int ubo) {
        int blockIndex = glGetUniformBlockIndex(program,uniformBlockName);
        if(blockIndex == -1)    {
            return false;
        }
        m_ubo = ubo;

        int dataSize = glGetActiveUniformBlocki(program, blockIndex, GL_UNIFORM_BLOCK_DATA_SIZE);
        System.out.printf("block index %s size is %d\n", uniformBlockName, dataSize);

        //allocate UBO
        glBindBuffer(GL_UNIFORM_BUFFER, m_ubo);
        nglBufferData(GL_UNIFORM_BUFFER, dataSize ,NULL, GL_STATIC_DRAW);

        //get uniform number inside this uniform block
        int nrOfUniforms = glGetActiveUniformBlocki(program, blockIndex, GL_UNIFORM_BLOCK_ACTIVE_UNIFORMS);
        System.out.printf("There are %d uniforms in block %s\n", nrOfUniforms, uniformBlockName);

        //get the uniform index
        int[] blockIndices = new int[nrOfUniforms];
        glGetActiveUniformBlockiv(program, blockIndex, GL_UNIFORM_BLOCK_ACTIVE_UNIFORM_INDICES, blockIndices);

        //get every uniform's name and offset
        m_indices = new HashMap<>();
        for(int indice:blockIndices)   {
            String uniformIndiceName = glGetActiveUniformName(program, indice);
            int uniformIndiceOffset = glGetActiveUniformsi(program, indice, GL_UNIFORM_OFFSET);
            int uniformIndiceSize = glGetActiveUniformsi(program, indice, GL_UNIFORM_SIZE);
            int uniformIndiceType = glGetActiveUniformsi(program,indice, GL_UNIFORM_TYPE);
            int byteSize = mapTypeToByteSize(uniformIndiceType);
            int byteInTotal;
            if(byteSize == Integer.MAX_VALUE)
                byteInTotal = byteSize;
            else
                byteInTotal = byteSize * uniformIndiceSize;
            UniformInformation uniformInformation = new UniformInformation(uniformIndiceOffset, byteInTotal);
            System.out.printf("Uniform %s offset %d size %d\n",uniformIndiceName,uniformIndiceOffset,byteInTotal);
            m_indices.put(uniformIndiceName, uniformInformation);
        }

        return true;
    }

    private int mapTypeToByteSize(int type) {
        int ret = Integer.MAX_VALUE;
        switch (type)   {
            case GL_FLOAT:
                ret = Float.BYTES;
                break;
            case GL_FLOAT_VEC2:
                ret = Float.BYTES * 2;
                break;
            case GL_FLOAT_VEC3:
                ret = Float.BYTES * 3;
                break;
            case GL_FLOAT_VEC4:
                ret = Float.BYTES * 4;
                break;
            case GL_FLOAT_MAT4:
                ret = Float.BYTES * 16;
                break;
                default:
                    System.out.printf("Uniform type %d not recognized\n", type);

        }
        return ret;
    }

    private boolean checkUniformAndSize(String uniformIndiceName , int size)   {
        UniformInformation uniformInformation = m_indices.get(uniformIndiceName);
        if(uniformInformation == null)  {
            System.err.printf("Indice %s not found!\n", uniformIndiceName);
            return false;
        }

        //check size
        if(uniformInformation.blockSizeInBytes < size)   {
            System.err.printf("Indice %s has size %d, too small for %d!\n",
                    uniformIndiceName,
                    uniformInformation.blockSizeInBytes,
                    size);
            return false;
        }

        return true;
    }

    public boolean setValue(String uniformIndiceName, float[] value)    {
        if(!checkUniformAndSize(uniformIndiceName, Float.BYTES * value.length))
            return false;

        UniformInformation uniformInformation = m_indices.get(uniformIndiceName);
        //update
        glBindBuffer(GL_UNIFORM_BUFFER, m_ubo);
        glBufferSubData(GL_UNIFORM_BUFFER, uniformInformation.blockOffset, value);
        glBindBuffer(GL_UNIFORM_BUFFER, 0);
        return true;
    }

    public boolean setValue(String uniformIndiceName, float value)  {
        temp1[0] = value;
        return setValue(uniformIndiceName, temp1);
    }

    public boolean setValue(String uniformIndiceName, float value1, float value2)  {
        temp2[0] = value1;
        temp2[1] = value2;
        return setValue(uniformIndiceName, temp2);
    }

    public boolean setValue(String uniformIndiceName, float value1, float value2, float value3)  {
        temp3[0] = value1;
        temp3[1] = value2;
        temp3[2] = value3;
        return setValue(uniformIndiceName, temp3);
    }

    public boolean setValue(String uniformIndiceName, float value1, float value2, float value3, float value4)  {
        temp4[0] = value1;
        temp4[1] = value2;
        temp4[2] = value3;
        temp4[3] = value4;
        return setValue(uniformIndiceName, temp4);
    }

    public boolean setValue(String uniformIndiceName, Vector2f vec2){
        return setValue(uniformIndiceName, vec2.x, vec2.y);
    }

    public boolean setValue(String uniformIndiceName, Vector3f vec3){
        return setValue(uniformIndiceName, vec3.x, vec3.y, vec3.z);
    }

    public boolean setValue(String uniformIndiceName, Vector4f vec4){
        return setValue(uniformIndiceName, vec4.x, vec4.y, vec4.z, vec4.w);
    }

    public boolean setValue(String uniformIndiceName, Matrix4f mat4){
        mat4.get(temp16);
        return setValue(uniformIndiceName, temp16);
    }


}
