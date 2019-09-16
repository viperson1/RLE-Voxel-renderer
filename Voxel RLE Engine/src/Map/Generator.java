package Map;

import com.flowpowered.noise.module.source.Perlin;
import org.joml.SimplexNoise;
import org.joml.Vector3f;
import org.joml.Vector3i;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class Generator {

	public static boolean[][][] GenerateStackedPerlin(int width, int length, int height) {
		final Perlin noise = new Perlin();
		noise.setSeed((int)(Math.random() * 100000));

		float vScale = 10;

		Vector3f[][][] vPointArr = new Vector3f[(int)Math.ceil(width / vScale)][(int)Math.ceil(length / vScale)][(int)Math.ceil(length / vScale)];

		boolean[][][] map = new boolean[width][length][height];

		Random rand = new Random();

		float time = System.currentTimeMillis() / 1000f;
		for(int x = 0; x < width; x++)
			for(int y = 0; y < length; y++)
				for(int z = 0; z < height; z++) {
					double noiseVal = Math.abs(noise.getValue(x * 0.001f, y * 0.001f, 100.65)) * 96;
					boolean voxOn = ((z - 64) + noise.getValue(x * 0.01f, y * 0.01f, z * 0.005f) * 64)  - (getVoronoi(new Vector3f(x, y, z), 8, rand, vPointArr) * 4f * z / height) < noiseVal;
					map[x][y][z] = voxOn;
				}
		System.out.println(time - (System.currentTimeMillis() / 1000f));
		return map;
	}

	static float getVoronoi(Vector3f coord, float scale, Random rand, Vector3f[][][] vPointArr) {
		Vector3f randPoint = new Vector3f();
		Vector3i scaledPoint = new Vector3i((int)(coord.x / scale), (int)(coord.y / scale), (int)(coord.z / scale));
		float minDist = Float.MAX_VALUE;

		for(int x = scaledPoint.x - 1; x < scaledPoint.x + 2; x++)
			for(int y = scaledPoint.y - 1; y < scaledPoint.y + 2; y++)
				for(int z = scaledPoint.z - 1; z < scaledPoint.z + 2; z++) {
						if   (x >= 0 && x < vPointArr.length
							&& y >= 0 && y < vPointArr[0].length
							&& z >= 0 && z < vPointArr[0][0].length) {
							if(vPointArr[x][y][z] == null) {
								randPoint.set(getRandomPoint(coord)); //get a random point from 0 to 1 seeded with the coordinate
								vPointArr[x][y][z] = new Vector3f(randPoint);
							}
							else randPoint.set(vPointArr[x][y][z]);

							randPoint.x *= scale; randPoint.y *= scale; randPoint.z *= scale; //multiply by scale to make the range 0 to scale
							randPoint.x += x * scale; randPoint.y += y * scale; randPoint.z += z * scale; //make it fit into same box as our coordinate.

							minDist = Math.min(coord.distance(randPoint), minDist);
						}
					}
		return minDist;
	}

	static Vector3f getRandomPoint(Vector3f coord) {
		ThreadLocalRandom rand = ThreadLocalRandom.current();
		return new Vector3f(rand.nextFloat(), rand.nextFloat(), rand.nextFloat());
	}

	static float lerp(float start, float end, float transition) {
		return start + transition * (end - start);
	}

	public static void main(String[] args) {
	}
}
