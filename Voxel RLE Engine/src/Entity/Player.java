package Entity;

import Engine.Engine;
import Map.Level;
import org.joml.*;
import java.lang.Math;

public class Player extends Entity {
    public float horizon;
    public int eyeHeight;

    public Player(Engine engine) {
        super(new Vector3f(), new Vector3i(2, 2, 8), 3, 0, engine, engine.getLevel());
        this.horizon = 0f;
        this.eyeHeight = 7;
    }

    public Player(Vector3f position, Vector3i boundingBox, int kneeHeight, double direction, int eyeHeight, Engine engine, Level level) {
        super(position, boundingBox, kneeHeight, direction, engine, level);
        this.horizon = 0f;
        this.eyeHeight = eyeHeight;
    }

    public void moveDirection(int dirID, double frameTime) {
        if (true) {
            switch (dirID) {
                case 1: //forward
                    motion.set((float) -Math.sin(direction), (float) -Math.cos(direction), /*((1 - horizon) / engine.getRenderer().heightScale)*/ 0);
                    break;
                case 2: //back
                    motion.set((float) Math.sin(direction), (float) Math.cos(direction), /*-((1 - horizon) / engine.getRenderer().heightScale)*/ 0);
                    break;
                case 3: //left
                    motion.set((float) -Math.cos(direction), (float) Math.sin(direction), motion.z);
                    break;
                case 4: //right
                    motion.set((float) Math.cos(direction), (float) -Math.sin(direction), motion.z);
                    break;
            }

            motion.mul(moveSpeed * (float) frameTime);
            applyMotion();
        }
    }
}
