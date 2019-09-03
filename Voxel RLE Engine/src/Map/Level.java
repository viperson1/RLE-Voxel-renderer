package Map;

import com.flowpowered.noise.module.source.Perlin;
import org.joml.SimplexNoise;

public class Level {
    private RLEColumn[][] level;
	public Perlin noise;
    
    public Level(int width, int height) {
        level = new RLEColumn[width][height];
        noise = new Perlin();
        
        for(int y = 0; y < height; y++) {
            for(int x = 0; x < width; x++) {
                level[x][y] = new RLEColumn();
                //hills
                level[x][y].setSlab(0, (int)(Math.abs(noise.getValue(x * 0.001, y * 0.001, 0) * 50)), 1);
                
                //clouds
				int cloudVal = (int)(noise.getValue(x * .01, y * 0.01, 50) * 30);
                if(cloudVal > 0) level[x][y].setSlab(150 - cloudVal, 150 + cloudVal, 1);
            }
        }
    }

    public int getWidth() {return level.length;     }
    public int getHeight(){return level[0].length;  }
    public RLEColumn[][] getLevelArray() {return level;}
}
