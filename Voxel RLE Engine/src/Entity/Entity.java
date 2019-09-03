package Entity;

import org.joml.*;

public class Entity {
    public Vector3f position;
    public Vector3f motion;
    public Vector3i boundingBox;
    public double direction = 0;
    public float moveSpeed;

    public Entity() {
        this.position = new Vector3f(0f, 0f, 0f);
        this.motion = new Vector3f(0f,0f,0f);
        this.boundingBox = new Vector3i(4,4,4);
        this.direction = 0;
        this.moveSpeed = 30;
    }

    public Entity(Vector3f position, Vector3i boundingBox, double direction) {
        this.position = position;
        this.motion = new Vector3f(0f,0f,0f);
        this.boundingBox = boundingBox;
        this.direction = 0;
        this.moveSpeed = 30;
    }

    void applyMotion() {
        position.add(motion.x, motion.y, motion.z);
    }

    void setMotion(float x, float y, float z) {
        motion.set(x,y,z);
    }
}
