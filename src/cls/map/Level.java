package cls.map;

import cls.building.Building;

public class Level {

	private DataLevel data;
	private Map map;
	
	public Building getBuildingAt(int i, int j) {
		return null; // TODO do
	}
	
	public Map getMap() {
		return map;
	}
	
	public Level(DataLevel data) {
		this.data = data;
		this.map = data.mapData.newMap();
	}

}
