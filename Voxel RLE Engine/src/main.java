import Engine.Engine;
import Engine.ComputeShader;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL43.*;

public class main {
    public static void main(String[] args) {
        Engine engine = new Engine();
        double time = System.nanoTime();
        while(!glfwWindowShouldClose(engine.getWindow())) {
            engine.update();
            glfwSetWindowTitle(engine.getWindow(), "" + engine.getPlayer().position.z);
            glFlush();
        }
        glfwTerminate();
    }
}
