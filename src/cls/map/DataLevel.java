package cls.map;

import run.Data;
import cls.building.DataBuilding;
import cls.unit.DataUnit;

public class DataLevel {

	public final String name;
	public final DataMap mapData;
	public final int playerCount;
	public final DataUnit[][] units;
		private String[][] unitNames;
	public final int[][][] unitStartingPositions;
	public final DataBuilding[][] buildings;
	public final int[][][] buildingStartingPositions;
		private String[][] buildingNames;
	
	private boolean finalised;
		
	public DataLevel(String name, DataMap mapData, int playerCount,
			         String[][] unitNames, int[][][] unitStartingPositions, 
			         String[][] buildingNames, int[][][] buildingStartingPositions) {
		this.name = name;
		this.mapData = mapData;
		this.playerCount = playerCount;
		this.units = new DataUnit[playerCount][unitNames.length];
		this.unitNames = unitNames;
		this.unitStartingPositions = unitStartingPositions;
		this.buildings = new DataBuilding[playerCount][buildingNames.length];
		this.buildingNames = buildingNames;
		this.buildingStartingPositions = buildingStartingPositions;
		finalised = false;
	}
	
	private void finalise() {
		// Multidimensional arrays in java are square.
		// Therefore if one player has more starting units than the other,
		// the other will have null values :(
		for (int player = 0; player < units.length; player ++) {
			for (int i = 0; i < units[player].length; i ++) {
				if (units[player][i] != null) units[player][i] = Data.getUnit(unitNames[player][i]);
			}
		}
		for (int player = 0; player < buildings.length; player ++) {
			for (int i = 0; i < buildings[player].length; i ++) {
				if (buildings[player][i] != null) buildings[player][i] = Data.getBuilding(buildingNames[player][i]);
			}
		}
		finalised = true;
	}
	
	public Level newLevel() {
		if (!finalised) finalise();
		return new Level(this);
	}

}
