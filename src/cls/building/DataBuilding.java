package cls.building;

import java.util.HashMap;

import run.Data;

import cls.Player;
import cls.map.DataTile;
import cls.unit.DataSprite;
import cls.unit.DataUnit;
import cls.unit.Unit;

public class DataBuilding {

	public final String                      name;
	public final DataTile[]                      buildingConditions;
		private String[] validTileNames;
	public final DataBuilding[]              buildingPrerequisistes;
		private String[] requiredBuildingNames;
	public final int                         turnsToBuild;
	public final int                         costToBuild;
	// Maybe other resource costs here?
	public final int                         influenceToCapture;
	public final int                         turnsToCapture;
	public final int                         moneyProvided;
	public final Unit.Kind[]                 resupplies;
	public final HashMap<Unit.Kind, Integer> healthRestoration;
	public final int                         totalSpace;
	public final HashMap<Unit.Kind, Integer> unitStorage;
	public final DataSprite                  sprite;
	public final DataUnit[]                  makableUnits;
		private String[] makableUnitNames;
	
	private boolean finalised;
	
	public DataBuilding(String name, String[] validTileNames, String[] requiredBuildingNames, 
						int turnsToBuild, int costToBuild,
						int influenceToCapture, int turnsToCapture,
						int moneyProvided, Unit.Kind[] resupplies,
						HashMap<Unit.Kind, Integer> healthRestoration, 
						int totalSpace, HashMap<Unit.Kind, Integer> unitStorage,
						DataSprite sprite,
						String[] makableUnitNames) {
		this.name = name;
		this.buildingConditions = new DataTile[validTileNames.length];
		this.validTileNames = validTileNames;
		this.buildingPrerequisistes = new DataBuilding[requiredBuildingNames.length];
		this.requiredBuildingNames = requiredBuildingNames;
		this.turnsToBuild = turnsToBuild;
		this.costToBuild = costToBuild;
		this.turnsToCapture = turnsToCapture;
		this.influenceToCapture = influenceToCapture;
		this.moneyProvided = moneyProvided;
		this.resupplies = resupplies;
		this.healthRestoration = healthRestoration;
		this.unitStorage = unitStorage;
		this.totalSpace = totalSpace;
		this.sprite = sprite;
		this.makableUnits = new DataUnit[makableUnitNames.length];
		this.makableUnitNames = makableUnitNames;
		finalised = false;
	}
	
	private void finalise() {
		for (int i = 0; i < makableUnits.length; i ++) {
			makableUnits[i] = Data.getUnit(makableUnitNames[i]);
		}
		for (int i = 0; i < buildingPrerequisistes.length; i ++) {
			buildingPrerequisistes[i] = Data.getBuilding(requiredBuildingNames[i]);
		}
		for (int i = 0; i < buildingConditions.length; i ++) {
			buildingConditions[i] = Data.getTile(validTileNames[i]);
		}
		finalised = true;
	}
	
	public Building newBuilding(Player owner, int x, int y) {
		if (!finalised) finalise();
		return new Building(owner, x, y, this);
	}

}
