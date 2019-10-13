package Map;

import com.flowpowered.noise.module.source.Perlin;
import org.joml.SimplexNoise;

import java.awt.*;

public class Level {
    private RLEColumn[] level;
	public Perlin noise;
	int width; int height;
    
    public Level(int width, int height) {
        this.width = width; this.height = height;
        level = new RLEColumn[this.width * this.height];
        noise = new Perlin();
        
        /*for(int y = 0; y < height; y++) {
            for(int x = 0; x < width; x++) {
                level[getIndex(x,y)] = new RLEColumn();
                //hills

                Color col = new Color(0, 110 + (int)((noise.getValue(x * 0.01f, y * 0.01f, 100) * 50)), 0);
                level[getIndex(x,y)].setSlab(0, (int)(Math.abs(noise.getValue(x * 0.001, y * 0.001, 0) * 50) + 50), col.getRGB(),1);

                col = new Color(col.getGreen() + 50, col.getGreen() + 50, col.getGreen() + 50);
                //clouds
				int cloudVal = (int)(noise.getValue(x * .01, y * 0.01, 50) * 30);
                if(cloudVal > 5) level[getIndex(x,y)].setSlab(150 - cloudVal, 150 + cloudVal, col.getRGB(), 1);
            }
        }*/
        read3DArray(Generator.GenerateStackedPerlin(width / 4, height / 4, 256, 4, 4, 2));
    }

    public void read3DArray(boolean[][][] map) {
        for(int x = 0; x < map.length; x++) {
            for(int y = 0; y < map[0].length; y++) {
                level[getIndex(x,y)] = new RLEColumn();
                boolean last=map[x][y][0];
                int switchHeight = 0;
                
                Color grassColor = new Color(0, 110 + (int)(noise.getValue(x * 0.1f, y * 0.1f, 100.4f) * 10), 0);
                int grassHeight = (int)Math.abs(noise.getValue(x * 0.1f, y * 0.1f, -100.4f) * 4) + 1;

                Color stoneColor = Color.gray;

                switch(grassHeight) {
                    case 2:
                    case 3:
                        stoneColor = stoneColor.brighter();
                        break;
                    case 4:
                    case 5:
                        stoneColor = stoneColor.darker();
                        break;
                }

                for(int z = 1; z < map[0][0].length; z++) {
                    if(last != map[x][y][z]) {
                        if(last == true) {
                            if(z - switchHeight > grassHeight) {
                                level[getIndex(x, y)].setSlab(switchHeight, z-grassHeight, stoneColor.getRGB(),  1);
                                level[getIndex(x, y)].setSlab(z - grassHeight, z, grassColor.getRGB(), 1);
                            }
                        }
                        switchHeight = z;
                    }
                    last = map[x][y][z];
                }
                level[getIndex(x, y)].setSlab(0, 1, Color.black.getRGB(), 1);
            }
        }
    }

    public RLEColumn getColumn(int x, int y) {
        return level[(y * width) + x];
    }

    public int getIndex(int x, int y) { return (y * width) + x; };

    public int getWidth() {return width;     }
    public int getHeight(){return height;  }
    public RLEColumn[] getLevelArray() { return level; }
}
