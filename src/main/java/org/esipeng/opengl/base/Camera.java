package org.esipeng.opengl.base;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWScrollCallback;

import static org.lwjgl.glfw.GLFW.*;

public class Camera {
    Vector3f m_cameraPos, m_cameraFront, m_camaraUp, m_vecTemp;
    Matrix4f m_matLookAt = new Matrix4f();
    float[] m_matBuf = new float[16], m_vecBuf = new float[3];
    float speedPerMilisecond = 2.5f;
    float m_lastX = 400, m_lastY = 300;
    long m_window;
    float m_pitch = 0, m_yaw = -90f;
    boolean m_isFirst = true;
    float fov = 45f;

    public float getFov() {
        return fov;
    }

    public float getFovRadians()    {
        return (float)Math.toRadians(fov);
    }

    public float getSpeedPerMilisecond() {
        return speedPerMilisecond;
    }

    public void setSpeedPerMilisecond(float speedPerMilisecond) {
        this.speedPerMilisecond = speedPerMilisecond;
    }

    public Camera(
            float posX, float posY, float posZ,
            float dirX, float dirY, float dirZ,
            float upX,  float upY,  float upZ,
            float lastX, float lastY,
            long window
    )   {
        m_cameraPos = new Vector3f(posX, posY, posZ);
        m_cameraFront = new Vector3f(dirX, dirY, dirZ);
        m_camaraUp  = new Vector3f(upX,  upY,  upZ);
        m_vecTemp = new Vector3f();
        m_lastX = lastX;
        m_lastY = lastY;
        m_window = window;
    }

    public float[] getFront()  {
        m_vecBuf[0] = m_cameraFront.x;
        m_vecBuf[1] = m_cameraFront.y;
        m_vecBuf[2] = m_cameraFront.z;
        return m_vecBuf;
    }

    public Camera(long window) {
        this(
                0.f,0.f,3.0f,
                0.f,0.f,-1.0f,
                0.f,1.f,0.f,
                400, 300,
                window
                );
    }

    public void processInput(float elapsed)   {
        float cameraSpeed = speedPerMilisecond * elapsed;
        m_cameraFront.mul(cameraSpeed, m_vecTemp);

        if(glfwGetKey(m_window, GLFW_KEY_W) == GLFW_PRESS)
            m_cameraPos.add(m_vecTemp);
        if(glfwGetKey(m_window, GLFW_KEY_S) == GLFW_PRESS)
            m_cameraPos.sub(m_vecTemp);
        if(glfwGetKey(m_window, GLFW_KEY_A) == GLFW_PRESS)    {
            m_cameraFront.cross(m_camaraUp, m_vecTemp);
            m_cameraPos.sub(m_vecTemp.mul(cameraSpeed));
        }
        if(glfwGetKey(m_window, GLFW_KEY_D) == GLFW_PRESS)    {
            m_cameraFront.cross(m_camaraUp, m_vecTemp);
            m_cameraPos.add(m_vecTemp.mul(cameraSpeed));
        }
    }

    public void enableMouseFpsView()    {
        glfwSetInputMode(m_window, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
        glfwSetCursorPosCallback(m_window, new GLFWCursorPosCallback() {
            @Override
            public void invoke(long window, double xpos, double ypos) {
                if(m_isFirst)   {
                    m_lastX = (float)xpos;
                    m_lastY = (float)ypos;
                    m_isFirst = false;
                }
                float xoffset = (float)xpos - m_lastX;
                float yoffset = m_lastY - (float)ypos;
                m_lastX = (float)xpos;
                m_lastY = (float)ypos;

                xoffset *= 0.2 ;
                yoffset *= 0.2 ;
                m_yaw   += xoffset;
                m_pitch += yoffset;
                if(m_pitch > 89.f)
                    m_pitch = 89.f;
                if(m_pitch < -89.f)
                    m_pitch = -89.f;

                m_cameraFront.x = (float)(Math.cos( Math.toRadians(m_pitch)) * Math.cos(Math.toRadians(m_yaw)));
                m_cameraFront.y = (float)(Math.sin( Math.toRadians(m_pitch)));
                m_cameraFront.z = (float)(Math.cos( Math.toRadians(m_pitch)) * Math.sin(Math.toRadians(m_yaw)));
                m_cameraFront.normalize();
            }
        });

        glfwSetScrollCallback(m_window, new GLFWScrollCallback() {
            @Override
            public void invoke(long window, double xoff, double yoff) {
                if(fov > 1.f && fov <= 45.f)
                    fov -= yoff;

                if(fov < 1.f)
                    fov = 1.f;
                if(fov > 45.f)
                    fov = 45f;
            }
        });
    }

    public float[] generateView()   {
        m_matLookAt.setLookAt(m_cameraPos, m_cameraPos.add(m_cameraFront, m_vecTemp) , m_camaraUp);
        return m_matLookAt.get(m_matBuf);
    }

    public Matrix4f generateViewMat()   {
        m_matLookAt.setLookAt(m_cameraPos, m_cameraPos.add(m_cameraFront, m_vecTemp) , m_camaraUp);
        return m_matLookAt;
    }

    public float[] getCameraPos()   {
        m_vecBuf[0] = m_cameraPos.x;
        m_vecBuf[1] = m_cameraPos.y;
        m_vecBuf[2] = m_cameraPos.z;
        //System.out.printf("Camera Pos %.2f %.2f %.2f\n", m_vecBuf[0], m_vecBuf[1], m_vecBuf[2]);
        return m_vecBuf;
    }
}
