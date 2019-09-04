import Engine.Engine;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.glFlush;

public class main {
    public static void main(String[] args) {
        Engine engine = new Engine();
        double time = System.nanoTime();
        while(!glfwWindowShouldClose(engine.getWindow())) {
            engine.update();
            glfwSetWindowTitle(engine.getWindow(), "" + 1 / engine.frameTime);
            glFlush();
        }
        glfwTerminate();
    }
}
