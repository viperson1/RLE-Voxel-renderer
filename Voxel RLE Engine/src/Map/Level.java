package Map;

import com.flowpowered.noise.module.source.Perlin;
import org.joml.SimplexNoise;

import java.awt.*;

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
                level[x][y].setSlab(0, (int)(Math.abs(noise.getValue(x * 0.001, y * 0.001, 0) * 50) + 50), Color.gray.getRGB(),1);
                
                //clouds
				int cloudVal = (int)(noise.getValue(x * .01, y * 0.01, 50) * 30);
                if(cloudVal > 5) level[x][y].setSlab(150 - cloudVal, 150 + cloudVal, Color.BLACK.getRGB(), 1);
            }
        }
        //read3DArray(Generator.GenerateStackedPerlin(width, height, 256));
    }

    public void read3DArray(boolean[][][] map) {
        for(int x = 0; x < map.length; x++) {
            for(int y = 0; y < map[0].length; y++) {
                level[x][y] = new RLEColumn();
                boolean last=false;
                int switchHeight = 0;
                
                Color grassColor = new Color(0, 110 + (int)(noise.getValue(x * 0.1f, y * 0.1f, 100.4f) * 10), 0);
                int grassHeight = (int)Math.abs(noise.getValue(x * 0.1f, y * 0.1f, -100.4f) * 4 + 1);
                
                for(int z = 0; z < map[0][0].length; z++) {
                    if(last != map[x][y][z]) {
                        if(last == true) {
                            level[x][y].setSlab(switchHeight, z - grassHeight, Color.gray.getRGB(), 1);
                            level[x][y].setSlab(z - grassHeight, z, grassColor.getRGB(), 1);
                        }
                        switchHeight = z;
                    }
                    last = map[x][y][z];
                }
            
            }
        }
    }
    
    public int getWidth() {return level.length;     }
    public int getHeight(){return level[0].length;  }
    public RLEColumn[][] getLevelArray() {return level;}
}
