package Map;

import com.flowpowered.noise.module.source.Perlin;
import org.joml.SimplexNoise;

public class Generator {
	public static boolean[][][] GenerateStackedPerlin(int width, int length, int height) {
		Perlin noise = new Perlin();
		
		boolean[][][] map = new boolean[width][length][height];
		for(int x = 0; x < width; x++)
			for(int y = 0; y < length; y++)
				for(int z = 0; z < height; z++) {
					double noiseVal = Math.abs(noise.getValue(x * 0.001f, y * 0.001f, 100.65)) * 96;
					boolean voxOn = (z - 64) + noise.getValue(x * 0.01f, y * 0.01f, z * 0.005f) * 64 < noiseVal;
					map[x][y][z] = voxOn;
				}
		return map;
	}
	
	static float lerp(float v0, float v1, float t) {
		return v0 + t * (v1 - v0);
	}
}
