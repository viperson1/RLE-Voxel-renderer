package Map;

import com.flowpowered.noise.module.source.Perlin;
import org.joml.SimplexNoise;

public class Generator {
	public static boolean[][][] GenerateStackedPerlin(int width, int length, int height) {
		Perlin noise = new Perlin();
		
		boolean[][][] map = new boolean[width][length][height];
		for(int x = 0; x < width; x++)
			for(int y = 0; y < length; y++)
				for(int z = 0; z < length; z++) {
					map[x][y][z] = (SimplexNoise.noise(x * .01f, y * .01f, z * .01f)) > 0;
				}
		return map;
	}
	
	static float lerp(float v0, float v1, float t) {
		return v0 + t * (v1 - v0);
	}
}
