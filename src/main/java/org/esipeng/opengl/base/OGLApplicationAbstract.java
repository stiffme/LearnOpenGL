package org.esipeng.opengl.base;

import org.lwjgl.system.MemoryUtil;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedHashSet;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public abstract class OGLApplicationAbstract {

    // init() --> applicationInit() --> loop() ;
    //            createContext() ----> update/draw

    private boolean m_fpsEnabled = false;
    protected long m_currentWindow;

    public final void enableFps(boolean enable)   {
        m_fpsEnabled = enable;
    }

    public final void run()   {
        if(!init()) {
            System.out.println("init failed");
            return;
        }

        if(!applicationInit())  {
            System.out.println("application init fail");
            glfwTerminate();
            return;
        }
        m_currentWindow = glfwGetCurrentContext();
        //by default set the swap interval to 1
        glfwSwapInterval(1);
        loop();
    }

    protected final boolean init()    {
        return glfwInit();
    }



    protected final void loop ()  {

        double t1 = glfwGetTime();
        double previousTime = t1;
        double t2;

        int frameCount = 0;
        while(!glfwWindowShouldClose(m_currentWindow))   {
            processInput(m_currentWindow);

            t2 = glfwGetTime();
            update((float)(t2 - t1));

            t1 = t2;

            draw();

            glfwSwapBuffers(m_currentWindow);
            glfwPollEvents();

            if(m_fpsEnabled)    {
                ++frameCount;
                double currentTime = t2;
                if ( currentTime - previousTime >= 1.0 )
                {
                    // Display the frame count here any way you want.
                    System.out.println("FPS: " + frameCount);

                    frameCount = 0;
                    previousTime = currentTime;
                }

            }
        }

        destroy();
        glfwDestroyWindow(m_currentWindow);
        glfwTerminate();

    }


    protected String loadFileFromResource(String resource ) throws Exception {
        return new String(Files.readAllBytes(Paths.get(getClass().getClassLoader().getResource(resource).toURI())));
    }

    protected byte[] loadBinaryFileFromResource(String resource ) throws Exception {
        return Files.readAllBytes(Paths.get(getClass().getClassLoader().getResource(resource).toURI()));
    }

    protected String getResourcePath(String resource) throws Exception  {
        return Paths.get(getClass().getClassLoader().getResource(resource).toURI()).toAbsolutePath().toString();
    }

    protected FloatBuffer floatArrayToBuffer(float[] arr)   {
        FloatBuffer ret = MemoryUtil.memAllocFloat(arr.length);
        for(float f : arr)  {
            ret.put(f);
        }

        ret.flip();
        return ret;
    }

    protected IntBuffer intArrayToBuffer(int[] arr)   {
        IntBuffer ret = MemoryUtil.memAllocInt(arr.length);
        for(int f : arr)  {
            ret.put(f);
        }

        ret.flip();
        return ret;
    }

    protected ByteBuffer byteArrayToBuffer(byte[] arr)   {
        ByteBuffer ret = MemoryUtil.memAlloc(arr.length);
        for(byte f : arr)  {
            ret.put(f);
        }

        ret.flip();
        return ret;
    }

    protected void processInput(long window)    {
        if(glfwGetKey(window, GLFW_KEY_ESCAPE) == GLFW_PRESS)
            glfwSetWindowShouldClose(window, true);
    }


    protected abstract boolean applicationInit();
    protected abstract void update(float elapsed);
    protected abstract void draw();
    protected abstract void destroy();



}
