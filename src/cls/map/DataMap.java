package cls.map;

public class DataMap {

	public final String name;
	public final DataTile[][] tiles;
	
	public DataMap(String name, DataTile[][] tiles) {
		this.name = name;
		this.tiles = tiles;
	}
	
	public Map newMap() {
		return new Map(this);
	}

}
