package cls.map;

import java.util.ArrayList;

import cls.building.Building;

public class Map {

	protected ArrayList<Building> buildings;
	protected Tile[][] tiles;

	public Building getBuildingAt(int i, int j) {
		for (Building b : buildings) {
			if (b.isAt(i, j)) return b;
		}
		return null;
	}
	
	public Tile getTileAt(int i, int j) {
		if (j < 0 || j >= tiles.length) return null;
		if (i < 0 || i >= tiles[j].length) return null;
		return tiles[j][i];
	}

	public int getWidth() { return tiles[0].length; }
	public int getHeight() { return tiles.length; }
	
	public Map(String filename) {
		buildings = new ArrayList<Building>();
		tiles = new Tile[30][30]; // TODO implement map loading
		for (int j = 0; j < tiles.length; j ++) {
			for (int i = 0; i < tiles[j].length; i ++) {
				tiles[j][i] = Tile.GRASS;
			}
		}
	}

	public void draw() {
		for (int j = 0; j < getHeight(); j ++) {
			for (int i = 0; i < getWidth(); i ++) {
				getTileAt(i, j).draw(i * Tile.TILE_SIZE, j * Tile.TILE_SIZE);
			}
		}
	}
	
}
