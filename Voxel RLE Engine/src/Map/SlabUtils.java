package Map;

public class SlabUtils {
	public static char createSlab(int type, int length) {
		if(type > 255) type=0;
		if(length == 0) return 0;
		if(length > 255) length = 255;
		
		return (char)(0 | length | (type << 8));
	}
	
	public static int getType(char slab) {
		return (slab >> 8) & 255;
	}
	
	public static int getLength(char slab) {
		return slab & 255;
	}
}
