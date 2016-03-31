package cls.map;

import run.Data;
import cls.Player.Faction;
import cls.building.DataBuilding;
import cls.unit.DataUnit;

public class DataLevel {

	public final String         name;
	public final DataMap        mapData;
	public final boolean        fogOfWar;
	public final int            playerCount;
	public final Faction[]      factions;
	public final DataUnit[][]   units;
		private String[][] unitNames;
	public final int[][][]      unitStartingPositions;
	public final DataBuilding[] buildings;
		private String[] buildingNames;
	public final int[][]        buildingStartingPositions;
	public final int[]          buildingStartingOwners;
		
	
	private boolean finalised;
		
	public DataLevel(String name, DataMap mapData, boolean fogOfWar, int playerCount, Faction[] factions,
			         String[][] unitNames, int[][][] unitStartingPositions, 
			         String[] buildingNames, int[][] buildingStartingPositions, int[] buildingStartingOwners) {
		this.name = name;
		this.mapData = mapData;
		this.fogOfWar = fogOfWar;
		this.playerCount = playerCount;
		this.factions = factions;
		this.units = new DataUnit[playerCount][unitNames[0].length];
		this.unitNames = unitNames;
		this.unitStartingPositions = unitStartingPositions;
		this.buildings = new DataBuilding[buildingNames.length];
		this.buildingNames = buildingNames;
		this.buildingStartingPositions = buildingStartingPositions;
		this.buildingStartingOwners = buildingStartingOwners;
		finalised = false;
	}
	
	private void finalise() {
		// Multidimensional arrays in java are square.
		// Therefore if one player has more starting units than the other,
		// the other will have null values :(
		for (int n = 0; n < units.length; n ++) {
			units[n] = new DataUnit[unitNames[n].length];
			for (int i = 0; i < unitNames[n].length; i ++) {
				if (unitNames[n][i] != null) units[n][i] = Data.getUnit(unitNames[n][i]);
			}
		}
		for (int i = 0; i < buildings.length; i ++) {
			if (buildings[i] != null) buildings[i] = Data.getBuilding(buildingNames[i]);
		}
		finalised = true;
	}
	
	public Level newLevel() {
		if (!finalised) finalise();
		return new Level(this);
	}

}
