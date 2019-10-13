package Entity.Visuals;

import Entity.Entity;
import org.joml.Vector3f;
import org.joml.Vector3i;

public class Particle {
    private Vector3f position;
    private Vector3f motion;
    private Vector3i color;

    public Particle(Vector3f position, Vector3i color) {
        this.position = position;
        this.motion = new Vector3f(0, 0,  0);
        this.color = color;
    }

    public Particle(Vector3f position, Vector3f motion, Vector3i color) {
        this.position = position;
        this.motion = motion;
        this.color = color;
    }
}
