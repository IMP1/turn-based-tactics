package cls.map;

public class Map {

	private DataMap data;
	private Tile[][] tiles;
	
	public Tile getTileAt(int i, int j) {
		if (j < 0 || j >= tiles.length) return null;
		if (i < 0 || i >= tiles[j].length) return null;
		return tiles[j][i];
	}

	public int getWidth() { return tiles[0].length; }
	public int getHeight() { return tiles.length; }
	
	public Map(DataMap data) {
		this.data = data;
		tiles = new Tile[data.tiles.length][];
		for (int j = 0; j < tiles.length; j ++) {
			tiles[j] = new Tile[data.tiles[j].length];
			for (int i = 0; i < tiles[j].length; i ++) {
				tiles[j][i] = data.tiles[j][i].newTile();
			}
		}
	}
	
	public void draw() {
		for (int j = 0; j < getHeight(); j ++) {
			for (int i = 0; i < getWidth(); i ++) {
				getTileAt(i, j).draw(i * DataTile.TILE_SIZE, j * DataTile.TILE_SIZE);
			}
		}
	}	

}
