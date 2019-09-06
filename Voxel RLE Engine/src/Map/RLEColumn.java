package Map;

import java.awt.*;
import java.util.ArrayList;

public class RLEColumn {
	private ArrayList<Long> column;

	public RLEColumn() {
		column = new ArrayList<Long>();
		//setSlab(0, 1, 1);
	}
	
	public int length() { 			 return column.size(); 	  }
	
	public long getSlab(int index) { return column.get(index); }
	
	public ArrayList<Long> getColumnString() { return column; }
	
	public void setSlab(int botHeight, int topHeight, int color, int type) {
		setSlab(botHeight, topHeight, color, type, 0, column.size());
	}
	
	public void setSlab(int botHeight, int topHeight, int color, int type, int bot, int top) {
		if(column.size() == 0) { column.add(createSlab(color, topHeight, botHeight, type)); return; }
		
		if(botHeight > topHeight) return;
		
		long insertSlab = createSlab(color, topHeight, botHeight, type);
		
		int mid = (top + bot) / 2;
		int step = 0;
		
		while(mid < top && mid > bot) {
			if(botHeight > getTopHeight(column.get(mid))) {		bot = mid; }
			else if(topHeight < getBotHeight(column.get(mid))) { top = mid;}
			if(topHeight < getBotHeight(column.get(mid))
			|| botHeight > getTopHeight(column.get(mid))) {
				mid = (top + bot) / 2;
				continue;
			}
			long currSlab = column.get(mid);
			
			//due to the binary search, I know that I have found a slab that will overlap with my new slab.
			int currSlabTopHeight = getTopHeight(currSlab);
			int currSlabBotHeight = getBotHeight(currSlab);
			
			if(botHeight >= currSlabBotHeight) { //my new slab will overlap on the top of this slab, or fit entirely within.
				long splitSlabBot = createSlab(getColor(currSlab), botHeight, currSlabBotHeight, getType(currSlab));
				if(topHeight <= currSlabTopHeight) { //my new slab will fit entirely within this slab.
					long splitSlabTop = createSlab(getColor(currSlab), currSlabTopHeight, topHeight, getType(currSlab));
					
					//set the current slab to be the bottom, add in the next after it.
					if(splitSlabBot != 0) column.set(mid, splitSlabBot); else column.remove(mid);
					column.add(mid + 1, insertSlab);
					if(splitSlabTop != 0) column.add(mid + 2, splitSlabTop);
					return;
				}
				else { //I want to change my current slab to be cut off by my new one, and search to find the one it overlaps with.
					if(splitSlabBot != 0) column.set(mid, splitSlabBot); else column.remove(mid);
					setSlab(botHeight, topHeight, color, type, mid, top);
					return;
				}
			}
			else { //My new slab overlaps with this slab on the bottom.
				long splitSlabTop = createSlab(getColor(currSlab), currSlabTopHeight, topHeight, getType(currSlab));
				if(splitSlabTop != 0) column.set(mid, splitSlabTop); else column.remove(mid);
				setSlab(botHeight, topHeight, color, type, bot, mid);
				return;
			}
		}
		//We did not find a position with that overlaps our slab, putting it on the top or the bottom.
		if(getTopHeight(column.get(mid)) <= botHeight) column.add(mid + 1, insertSlab);
		else column.add(mid, insertSlab);
	}
	
	public long getSlab(float z) {
		int top = column.size(), bot = 0;
		int mid = (top + bot) / 2;
		while(mid < top) {
			if(z >= getBotHeight(column.get(mid))) {
				if(z <= getTopHeight(column.get(mid))) return column.get(mid);
				else bot = mid;
			}
			else top = mid;
			mid = bot + top / 2;
		}
		return -1;
	}
	
	public int getSlabIndex(float z) {
		int top = column.size(), bot = 0;
		if(top < 2) return top;
		int mid = (top + bot) / 2;
		while(mid < top && mid > bot) {
			if(z >= getBotHeight(column.get(mid))) {
				if(z <= getTopHeight(column.get(mid))) return mid;
				else bot = mid;
			}
			else top = mid;
			mid = bot + top / 2;
		}
		if(mid >= 0 && mid < column.size() && (z < getBotHeight(column.get(mid)) || z > getTopHeight(column.get(mid)))) return mid;
		return -1;
	}
	public static long createSlab(int color, int topHeight, int botHeight, int type) {
		//A slab is a 64 bit value, the first 24 bits being a color, followed by a material type,
		//followed by two 16 bit values being the bottom and top heights of the slab.
		
		if(topHeight - botHeight == 0) return 0;
		
		if(type > 255) type=0;
		
		return 	(long)(color & 16777215) | //isolates the first 24 bits of the int, representing 3 8 bit BGR values
				((long)(type & 255) << 24) | //isolates first 8 bits of type, then moves it into position after color
				((long)(botHeight & 65535) << 32) |
				((long)(topHeight & 65535) << 48);
	}
	
	public static int getType(long slab) { return (int)((slab >> 24) & 255); }
	public static int getColor(long slab) { return (int)(slab & 16777215); }
	
	public static int getTopHeight(long slab) { return (int)((slab >> 48) & 65535); }
	public static int getBotHeight(long slab) { return (int)((slab >> 32) & 65535); }
	
	public static void main(String[] args) {
		RLEColumn column = new RLEColumn();
		
		column.setSlab(0, 1, Color.white.getRGB(), 255);
		column.setSlab(20, 30, Color.white.getRGB(), 255);
		column.setSlab(5, 15, Color.white.getRGB(), 255);
		
		System.out.println(column.getSlabIndex(32));
		
		for(int i = 0; i < column.getColumnString().size(); i++) {
			long slab = column.getColumnString().get(i);
			System.out.println(getBotHeight(slab) + ", " + getTopHeight(slab) + ", " + Long.toBinaryString(slab));
		}
	}
}
