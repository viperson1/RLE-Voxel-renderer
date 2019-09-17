package Entity;

import Engine.Engine;
import org.joml.*;
import java.lang.Math;

public class Player extends Entity {
    public float horizon;
    public int eyeHeight;
    Engine engine;

    public Player(Engine engine) {
        super(new Vector3f(), new Vector3i(2, 2, 8), 0);
        this.horizon = 0f;
        this.eyeHeight = 7;
    }

    public Player(Vector3f position, Vector3i boundingBox, double direction, int eyeHeight, Engine engine) {
        super(position, boundingBox, direction);
        this.horizon = 0f;
        this.eyeHeight = eyeHeight;
    }

    public void moveDirection(int dirID, double frameTime) {
        switch(dirID) {
            case 1: //forward
                motion.set((float)-Math.sin(direction), (float)-Math.cos(direction), (((1) - horizon) / 360f));
                break;
            case 2: //back
                motion.set((float)Math.sin(direction), (float)Math.cos(direction), -(((1) - horizon) / 360f));
                break;
            case 3: //left
                motion.set((float)-Math.cos(direction), (float)Math.sin(direction), 0);
                break;
            case 4: //right
                motion.set((float)Math.cos(direction), (float)-Math.sin(direction),0);
                break;
        }
        motion.mul(moveSpeed * (float)frameTime);
        applyMotion();
    }
}
