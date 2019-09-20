package Entity;

import Map.Level;
import org.joml.*;

import java.lang.Math;

public class Entity {
    public Vector3f position;
    public Vector3f motion;
    public Vector3i boundingBox;
    public double direction = 0;
    public float moveSpeed;
    public float kneeHeight;

    public Entity() {
        this.position = new Vector3f(0f, 0f, 0f);
        this.motion = new Vector3f(0f,0f,0f);
        this.boundingBox = new Vector3i(4,4,4);
        this.direction = 0;
        this.moveSpeed = 30;
        this.kneeHeight = 1;
    }

    public Entity(Vector3f position, Vector3i boundingBox, int kneeHeight, double direction) {
        this.position = position;
        this.motion = new Vector3f(0f,0f,0f);
        this.boundingBox = boundingBox;
        this.direction = direction;
        this.moveSpeed = 30;
        this.kneeHeight = kneeHeight;
    }

    public void applyMotion(Level level) {
        Vector3f newPos = new Vector3f(); motion.add(position, newPos);

        for(int x = -boundingBox.x / 2; x <= boundingBox.x / 2; x++)
            for(int y = -boundingBox.y / 2; y <= boundingBox.y / 2; y++) {
                if (newPos.x + x >= 0 && newPos.x + x < level.getWidth() && newPos.y + y >= 0 && newPos.y + y < level.getHeight()) {
                    byte collType = level.getLevelArray()[level.getIndex((int) newPos.x + x, (int) newPos.y + y)].getIntersections(newPos.z, newPos.z + boundingBox.z, kneeHeight);


                    if ((collType & 3) == 2) {
                        return;
                    }
                    if ((collType & 3) == 1) {
                        motion.z = (collType >> 2) - (newPos.z - (int)newPos.z);
                        newPos.z = position.z + motion.z;
                        //motion.z = collType >> 2;
                        //newPos.z = position.z + motion.z;
                    }
                }
                else return;
            }

        position.add(motion.x, motion.y, motion.z);
        motion.set(0, 0, 0);
    }

    void setMotion(float x, float y, float z) {
        motion.set(x,y,z);
    }
}
