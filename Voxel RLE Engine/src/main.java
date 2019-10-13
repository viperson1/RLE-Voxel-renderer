import Engine.Engine;
import Engine.ComputeShader;
import Engine.GPUCompute.CLRenderManager;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL43.*;

public class main {
    public static void main(String[] args) {
        Engine engine = new Engine();
        double time = System.nanoTime();

        //CLRenderManager test = new CLRenderManager(engine.getLevel(), engine.getPlayer(), engine.getRenderer());

        while(!glfwWindowShouldClose(engine.getWindow())) {
            engine.update();
            glfwSetWindowTitle(engine.getWindow(), "" + engine.getPlayer().position.z);
            glFlush();
        }
        glfwTerminate();
    }
}
