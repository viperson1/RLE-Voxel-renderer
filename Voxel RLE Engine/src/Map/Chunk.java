package Map;

public class Chunk {
    //A data structure dedicated to holding a 16 by 16 array of RLE Columns
    //used to allow 3D rendering to have a detached memory location, and still be updated easily
    //such as in a GPU rendering structure.

    private int maxHeight;
    public RLEColumn[] columns;

    public Chunk() {
        maxHeight = 0;
        columns = new RLEColumn[16 * 16];
    }

    public RLEColumn getColumn(int x, int y) {
        return columns[(y * 16) + x];
    }
}
