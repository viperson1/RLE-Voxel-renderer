package Map;

public class RLEColumn {
	private String column;
	public int maxHeight;
	public int[] palette;

	public RLEColumn() {
		column = "";
		palette = new int[255];
		//setSlab(0, 1, 1);
	}
	
	public int length() { 			 return column.length(); 	  }
	
	public char getSlab(int index) { return column.charAt(index); }
	
	public String getColumnString() {return column; 			  }
	
	public void setSlab(int botHeight, int topHeight, int color) {
		if(botHeight > topHeight) return;
		int type = 0;
		if(color != 0) {
			type = 1;
			for (int i = 0; i < 255; i++) {
				if (palette[i] == color) type = i + 1;
				if (palette[i] == 0) {
					type = i + 1;
					palette[i] = color;
					break;
				}
			}
		}
		int currHeight = 0;
		if(topHeight > maxHeight) maxHeight = topHeight;
		
		char insertSlab = SlabUtils.createSlab(type, topHeight - botHeight);
		
		for(int slabIndex = 0; slabIndex < column.length(); slabIndex++) {
			char currSlab = column.charAt(slabIndex);
			
			int currSlabBotHeight = currHeight, currSlabTopHeight = currHeight + SlabUtils.getLength(currSlab);
			
			currHeight = currSlabTopHeight;

			if(botHeight > currSlabTopHeight) continue; //my new slab is above the current one, so skip all other calculations
			//now I know that at least part of my new slab is going to overwrite part of this one. 
			if(topHeight <= currSlabTopHeight) { //my new slab fits entirely within this current slab
				char splitSlabBot = SlabUtils.createSlab(SlabUtils.getType(currSlab), botHeight - currSlabBotHeight);
				char splitSlabTop = SlabUtils.createSlab(SlabUtils.getType(currSlab), currSlabTopHeight - topHeight);
				
				column =  column.substring(0, slabIndex) 
						+ splitSlabBot + insertSlab + ((splitSlabTop != 0) ? splitSlabTop : "") 
						+ column.substring(slabIndex + 1);
				return;
			}
			else { 
				if(slabIndex < column.length() - 1) { //my new slab is on the border of this slab and the one above
					char currSlabNew = SlabUtils.createSlab(SlabUtils.getType(currSlab), botHeight - currSlabBotHeight);
					
					char nextSlab = column.charAt(slabIndex + 1);
					char nextSlabNew = SlabUtils.createSlab(SlabUtils.getType(nextSlab), SlabUtils.getLength(nextSlab) - (topHeight - currSlabTopHeight));
					
					column =  column.substring(0, slabIndex)
							+ currSlabNew + insertSlab + nextSlabNew
							+ column.substring(slabIndex + 2);
				}
				else { //my new slab is on the top, but overlapping with the current top slab
					char currSlabNew = SlabUtils.createSlab(SlabUtils.getType(currSlab), botHeight - currSlabBotHeight);
					
					column = column.substring(0, column.length() - 1) + currSlabNew + insertSlab;
				}
				return;
			}
		}
		//my new slab is on the top, not overlapping
		while(currHeight + 255 < botHeight) {
			column += SlabUtils.createSlab(0, 255);
			currHeight += 255;
		}
		if(currHeight < botHeight) column += SlabUtils.createSlab(0, botHeight - currHeight);
		column += insertSlab;
	}
	
	public static void main(String[] args) {
		RLEColumn column = new RLEColumn();
		column.setSlab(3, 10, 1);
		column.setSlab(250, 254, 2);
		
		for(int i = 0; i < column.length(); i++) {
			System.out.println(i + " Length: " + SlabUtils.getLength(column.getSlab(i)) + " Type: " + SlabUtils.getType(column.getSlab(i)));
			System.out.println(Integer.toBinaryString(column.getSlab(i)));
		}
		System.out.println(column.maxHeight);
	}
}
