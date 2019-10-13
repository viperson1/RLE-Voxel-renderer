package Map;

import com.flowpowered.noise.module.source.Perlin;
import org.joml.SimplexNoise;
import org.joml.Vector3f;
import org.joml.Vector3i;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class Generator {

	public static boolean[][][] GenerateStackedPerlin(int width, int length, int height, int xScale, int yScale, int zScale) {
		final Perlin noise = new Perlin();
		noise.setSeed((int)(Math.random() * 100000));

		float vScale = 32;

		Vector3f[][][] vPointArr = new Vector3f[(int)(width / vScale)][(int)(length / vScale)][(int)(height / vScale)];

		boolean[][][] map = new boolean[width][length][height];
		boolean[][][] interpMap = new boolean[width * xScale][length * yScale][height * zScale];

		float time = System.nanoTime() / 1000000000f;
		for(int x = 0; x < width; x++)
			for(int y = 0; y < length; y++)
				for(int z = 0; z < height; z++) {
					double noiseVal = Math.abs(noise.getValue(x * 0.001f, y * 0.001f, 100.65)) * 64;
					boolean voxOn = ((z - 64) + noise.getValue(x * 0.04f, y * 0.04f, z * 0.02f) * 32)  /*- ((getVoronoi(new Vector3f(x, y, z), vScale, vPointArr) * 8 * .5f) * z / height)*/ < noiseVal;
					map[x][y][z] = voxOn;
				}
		System.out.println((System.nanoTime() / 1000000000f) - time);

		for(int x = 0; x < width * (xScale - 1); x++)
			for(int y = 0; y < length * (yScale - 1); y++)
				for(int z = 0; z < height * (zScale - 1); z++) {
					interpMap[x][y][z] = trilerp(map[x / xScale]		[y / yScale]		[z / zScale]		? 1 : 0,
												 map[x / xScale + 1] 	[y / yScale]		[z / zScale]		? 1 : 0,
												 map[x / xScale]		[y / yScale + 1]	[z / zScale]		? 1 : 0,
												 map[x / xScale + 1]	[y / yScale + 1]	[z / zScale]		? 1 : 0,
												 map[x / xScale]		[y / yScale]		[z / zScale + 1]  	? 1 : 0,
												 map[x / xScale + 1]	[y / yScale]		[z / zScale + 1]	? 1 : 0,
												 map[x / xScale]		[y / yScale + 1]	[z / zScale + 1]  	? 1 : 0,
												 map[x / xScale + 1]	[y / yScale + 1]	[z / zScale + 1]  	? 1 : 0,
							((x % xScale) / (float)xScale), ((y % yScale) / (float)yScale), ((z % zScale) / (float)zScale)) > .5;
				}
		return interpMap;
	}

	static float getVoronoi(Vector3f coord, float scale, Vector3f[][][] vPointArr) {
		Vector3f randPoint = new Vector3f();
		Vector3i scaledPoint = new Vector3i((int)(coord.x / scale), (int)(coord.y / scale), (int)(coord.z / scale));
		float minDist = Float.MAX_VALUE;

		for(int x = scaledPoint.x - 1; x < scaledPoint.x + 2; x++)
			for(int y = scaledPoint.y - 1; y < scaledPoint.y + 2; y++)
				for(int z = scaledPoint.z - 1; z < scaledPoint.z + 2; z++) {
						int testX = (x + vPointArr.length) % vPointArr.length;
						int testY = (y + vPointArr[0].length) % vPointArr[0].length;
						int testZ = (z + vPointArr[0][0].length) % vPointArr[0][0].length;
						if(vPointArr[testX][testY][testZ] == null) {
							randPoint.set(getRandomPoint(coord)); //get a random point from 0 to 1 seeded with the coordinate
							vPointArr[testX][testY][testZ] = new Vector3f(randPoint);
						}
						else randPoint.set(vPointArr[testX][testY][testZ]);

						randPoint.x *= scale; randPoint.y *= scale; randPoint.z *= scale; //multiply by scale to make the range 0 to scale
						randPoint.x += x * scale; randPoint.y += y * scale; randPoint.z += z * scale; //make it fit into same box as our coordinate.

						minDist = Math.min(coord.distance(randPoint), minDist);
					}
		return minDist / scale;
	}

	static Vector3f getRandomPoint(Vector3f coord) {
		ThreadLocalRandom rand = ThreadLocalRandom.current();
		return new Vector3f(rand.nextFloat(), rand.nextFloat(), rand.nextFloat());
	}

	static float lerp(float start, float end, float transition) {
		return start + transition * (end - start);
	}

	static float bilerp(float v1, float v2, float v3, float v4, float x, float y) {
		//v1   v2
		//
		//v3   v4
		float smooth1 = lerp(v1, v2, x);
		float smooth2 = lerp(v3, v4, x);
		return lerp(smooth1, smooth2, y);
	}

	static float trilerp(float v1, float v2, float v3, float v4, float v5, float v6, float v7, float v8, float x, float y, float z) {
		//v1--v2
		//  v5  v6
		//v3  v4
		//  v7  v8

		float smooth1 = bilerp(v1, v2, v3, v4, x, y);
		float smooth2 = bilerp(v5, v6, v7, v8, x, y);

		return lerp(smooth1, smooth2, z);
	}

	public static void main(String[] args) {
	}
}
