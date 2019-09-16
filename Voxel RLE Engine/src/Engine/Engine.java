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

import java.awt.*;


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
        windowWidth = 960; windowHeight = 540;
        level = new Level(512, 512);
        player = new Player(new Vector3f(128, 128, 100), new Vector3i(1, 1, 2), 0, 2, this);
        renderer = new Renderer(player, level);
        lastTime = System.nanoTime();
        
        initializeWindow();
    }

    public Player getPlayer()   { return player; }
    public Level getLevel()     { return level;  }
    public long getWindow()     { return window; }
    public Renderer getRenderer(){return renderer; }

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
        glEnable(GL_DEPTH_TEST);
        glDepthFunc(GL_LESS);

        glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
    }

    int frame = 0;

    public void update() {
        renderer.renderFrame(++frame);
        frameTime = (System.nanoTime() - lastTime) / 1000000000f;
        lastTime = System.nanoTime();

        glFlush();

        glfwPollEvents();

        double[][] cursorPos = new double[2][1];
        glfwGetCursorPos(window, cursorPos[0], cursorPos[1]);

        player.direction -= .0625 * (cursorPos[0][0] - (windowWidth / 2)) * frameTime;
        player.horizon +=  9 * (cursorPos[1][0] - (windowHeight / 2)) * frameTime;
        if(player.horizon > renderer.rows / 2) player.horizon = renderer.rows / 2;
        if(player.horizon < renderer.rows / -2) player.horizon = renderer.rows / -2;


        glfwSetCursorPos(window, windowWidth/2, windowHeight/2);

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
		if(glfwGetMouseButton(window, GLFW_MOUSE_BUTTON_LEFT) == GLFW_PRESS || glfwGetKey(window, GLFW_KEY_RIGHT_SHIFT) == GLFW_PRESS) {
		    Vector3i rayHit = renderer.screenRayCast(64, player.direction, 0);
		    if(rayHit != null) {
                for (int x = rayHit.x - 2; x < rayHit.x + 3; x++) {
                    for (int y = rayHit.y - 2; y < rayHit.y + 3; y++) {
                        if(x < level.getWidth() && x >= 0 && y < level.getHeight() && y >= 0)
                            level.getLevelArray()[level.getIndex(x, y)].setSlab(rayHit.z - 2, rayHit.z + 3, Color.green.getRGB(), 1);
                    }
                }
            }
        }
        if(glfwGetKey(window, GLFW_KEY_RIGHT_CONTROL) == GLFW_PRESS || glfwGetMouseButton(window, GLFW_MOUSE_BUTTON_RIGHT) == GLFW_PRESS) {
            Vector3i rayHit = renderer.screenRayCast(64, player.direction, 0);
            if(rayHit != null) {
                for (int x = rayHit.x - 2; x < rayHit.x + 3; x++) {
                    for (int y = rayHit.y - 2; y < rayHit.y + 3; y++) {
                        if(x < level.getWidth() && x >= 0 && y < level.getHeight() && y >= 0)
                            level.getLevelArray()[level.getIndex(x, y)].removeArea(rayHit.z - 2, rayHit.z + 3);
                    }
                }
            }
        }
    }
}