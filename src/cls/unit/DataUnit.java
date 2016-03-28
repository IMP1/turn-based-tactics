package cls.unit;

import java.util.HashMap;

import run.Data;

import cls.Player;
import cls.building.DataBuilding;
import cls.unit.Unit.Kind;
import cls.unit.Unit.Movement;
import cls.weapon.DataWeapon;

public class DataUnit {
	
	public final String                      name;
	public final Kind                        unitClass;
	public final Movement                    movement;
	public final int                         moveDistance;
	public final int                         turnsToMake;
	public final int                         costToMake;
	public final int                         visionDistance;
	public final int                         defence;
	public final int                         influence;
	public final int                         fuelCost;
	public final boolean                     diesOnFuelDepletion;
	public final boolean                     usesFuelIfNotMoved;
	public final boolean                     canMoveAndAttack;
	public final boolean                     canBuild;
	// TODO handle ability to build somewhere
	public final int                         startingFuel;
	public final int                         maxFuel;
	public final DataSprite                  sprite;
	public final DataSprite                  icon;
	public final DataWeapon[]                weapons;
		private String[] weaponNames;
	public final HashMap<Unit.Kind, Integer> unitStorage;
	public final int                         totalSpace; 
	public final DataBuilding[]              buildableBuildings;
		private String[] buildableBuildingNames;
	
	private boolean finalised;
		
	public DataUnit(String name, Kind unitClass, Movement movement, int moveDistance,
					int turnsToMake, int costToMake,
			        int fuelCost, int startingFuel, int maxFuel, boolean usesFuelRegardless, boolean diesOnFuelDepletion, 
			        int visionDistance, int defence, int influence, 
			        boolean canMoveAndAttack, boolean canBuild, 
			        DataSprite sprite, DataSprite icon, String[] weaponNames,
			        int totalSpace, HashMap<Unit.Kind, Integer> unitsCanStore, 
			        String[] buildableBuildingNames) {
		this.name = name;
		this.weapons = new DataWeapon[weaponNames.length];
		this.weaponNames = weaponNames;
		this.unitClass = unitClass;
		this.movement = movement;
		this.moveDistance = moveDistance;
		this.turnsToMake = turnsToMake;
		this.costToMake = costToMake;
		this.visionDistance = visionDistance;
		this.defence = defence;
		this.influence = influence;
		this.fuelCost = fuelCost;
		this.diesOnFuelDepletion = diesOnFuelDepletion;
		this.usesFuelIfNotMoved = usesFuelRegardless;
		this.canMoveAndAttack = canMoveAndAttack;
		this.canBuild = canBuild;
		this.startingFuel = startingFuel;
		this.maxFuel = maxFuel;
		this.sprite = sprite;
		this.icon = icon;
		this.unitStorage = unitsCanStore;
		this.totalSpace = totalSpace;
		this.buildableBuildings = new DataBuilding[buildableBuildingNames.length];
		this.buildableBuildingNames = buildableBuildingNames;
		finalised = false;
	}
	
	private void finalise() {
		for (int i = 0; i < buildableBuildings.length; i ++) {
			buildableBuildings[i] = Data.getBuilding(buildableBuildingNames[i]);
		}
		for (int i = 0; i < weapons.length; i ++) {
			weapons[i] = Data.getWeapon(weaponNames[i]);
		}
		finalised = true;
	}
	
	public Unit newUnit(Player owner, int x, int y) {
		if (!finalised) finalise();
		Unit u = new Unit(owner, x, y, this);
		return u;
	}

}
