package cls.building;

import java.util.HashMap;

import cls.Player;
import cls.map.Tile;
import cls.unit.DataSprite;
import cls.unit.Unit;

public class DataBuilding {

	public final String                      name;
	public final Tile[]                      buildingConditions; 
	public final int                         turnsToBuild;
	public final int                         influenceToCapture;
	public final int                         turnsToCapture;
	public final int                         moneyProvided;
	public final Unit.Kind[]                 resupplies;
	public final HashMap<Unit.Kind, Integer> healthRestoration;
	public final HashMap<Unit.Kind, Integer> unitStorage;
	public final int                         totalSpace;
	public final DataSprite                  sprite;
	
	
	public DataBuilding(String name, Tile[] buildingConditions, int turnsToBuild,
						int influenceToCapture, int turnsToCapture,
						int moneyProvided, Unit.Kind[] resupplies,
						HashMap<Unit.Kind, Integer> healthRestoration, 
						HashMap<Unit.Kind, Integer> unitStorage, int totalSpace,
						DataSprite sprite) {
		this.name = name;
		this.buildingConditions = buildingConditions;
		this.turnsToBuild = turnsToBuild;
		this.turnsToCapture = turnsToCapture;
		this.influenceToCapture = influenceToCapture;
		this.moneyProvided = moneyProvided;
		this.resupplies = resupplies;
		this.healthRestoration = healthRestoration;
		this.unitStorage = unitStorage;
		this.totalSpace = totalSpace;
		this.sprite = sprite;
	}
	
	public Building newBuilding(Player owner, int x, int y) {
		return new Building(owner, x, y, this);
	}

}
