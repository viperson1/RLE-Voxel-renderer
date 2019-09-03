package Engine;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

import Entity.Player;
import Map.Level;
import com.flowpowered.noise.module.source.Perlin;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;


public class Engine {
    private Level level;
    private Player player;
    private long window;
    private final int windowWidth, windowHeight;
    private Renderer renderer;
	private Perlin noiseGen = new Perlin();
	public double frameTime;
	private double lastTime;
    
    public Engine() {
        windowWidth = 640; windowHeight = 360;
        level = new Level(1024, 1024);
        player = new Player(new Vector3f(512, 512, 1), new Vector3i(4, 4, 16), 0, 15);
        renderer = new Renderer(player, level);
        lastTime = System.nanoTime();
        
        initializeWindow();
    }

    public Player getPlayer()   { return player; }
    public Level getLevel()     { return level;  }
    public long getWindow()     { return window; }

    void initializeWindow() {
        if(!glfwInit()) {
            throw new IllegalStateException("Failed to initialize GLFW");
        }

        glfwWindowHint(GLFW_SAMPLES, 4);
        glfwWindowHint(GLFW_DOUBLEBUFFER, GLFW_FALSE);
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        window = glfwCreateWindow(windowWidth, windowHeight, "Window", 0, 0);

        if(window == 0) {
            throw new IllegalStateException("Failed to create window");
        }

        GLFWVidMode videoMode = glfwGetVideoMode(glfwGetPrimaryMonitor());
        glfwSetWindowPos(window, (videoMode.width() - windowWidth) / 2, (videoMode.height() - windowHeight) / 2 );

        glfwShowWindow(window);

        glfwMakeContextCurrent(window);
        GL.createCapabilities();

        glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
    }
    
    public void update() {
        renderer.renderFrame();
        frameTime = (System.nanoTime() - lastTime) / 1000000000f;
        lastTime = System.nanoTime();

        glfwPollEvents();
        
        if(glfwGetKey(window, GLFW_KEY_ESCAPE) == GLFW_PRESS)
            glfwSetWindowShouldClose(window, true);
        if(glfwGetKey(window, GLFW_KEY_W) == GLFW_PRESS)
            player.moveDirection(1, frameTime);
        if(glfwGetKey(window, GLFW_KEY_S) == GLFW_PRESS)
            player.moveDirection(2, frameTime);
        if(glfwGetKey(window, GLFW_KEY_A) == GLFW_PRESS)
            player.moveDirection(3, frameTime);
        if(glfwGetKey(window, GLFW_KEY_D) == GLFW_PRESS)
            player.moveDirection(4, frameTime);
        if(glfwGetKey(window, GLFW_KEY_RIGHT) == GLFW_PRESS)
        	player.direction -= frameTime;
		if(glfwGetKey(window, GLFW_KEY_LEFT) == GLFW_PRESS)
			player.direction += frameTime;
		if(glfwGetKey(window, GLFW_KEY_UP) == GLFW_PRESS) {
			player.horizon += 100 * frameTime;
			if(player.horizon > renderer.rows / 2) player.horizon = renderer.rows / 2;
		}
		if(glfwGetKey(window, GLFW_KEY_DOWN) == GLFW_PRESS) {
			player.horizon -= 100 * frameTime;
			if(player.horizon < renderer.rows / -2) player.horizon = renderer.rows / -2;
		}
		if(glfwGetKey(window, GLFW_KEY_Q) == GLFW_PRESS)
			player.position.z += frameTime * player.moveSpeed;
		if(glfwGetKey(window, GLFW_KEY_E) == GLFW_PRESS)
			player.position.z -= frameTime * player.moveSpeed;
		
        glfwSwapBuffers(window);
    }
}
