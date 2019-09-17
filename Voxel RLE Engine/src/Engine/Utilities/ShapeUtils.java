package Engine.Utilities;

import Map.Level;
import Map.RLEColumn;
import org.joml.Vector3f;

public class ShapeUtils {
	public int[][][] sphere;
	
	public void initSphere(float radius) {
		int sideLength = (int)(radius * 2);
		Vector3f center = new Vector3f(radius, radius, radius);
		sphere = new int[sideLength][sideLength][2];
		for(int x = 0; x < sideLength; x++) {
			for(int y = 0; y < sideLength; y++) {
				boolean last = false;
				for(int z = 0; z < sideLength; z++) {
					if((center.distance(x, y, z) - radius < 0) != last) {
						if(last == false) sphere[x][y][0] = z;
						else sphere[x][y][1] = z;
						last = (center.distance(x, y, z) - radius < 0);
					}
				}
			}
		}
	}
	
	public void removeSphereArea(int x, int y, int z, Level level) {
		if(sphere == null) initSphere(3);
		RLEColumn[] levelArray = level.getLevelArray();
		for(int xRel = 0; xRel < sphere.length; xRel++)
			for(int yRel = 0; yRel < sphere.length; yRel++) {
				levelArray[level.getIndex(x + (xRel - sphere.length / 2), y + (yRel - sphere.length / 2))]
						.removeArea(z + (sphere[xRel][yRel][0] - 3), z + (sphere[xRel][yRel][1] - 3));
			}
	}
}
