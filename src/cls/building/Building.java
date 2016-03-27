package cls.building;

import java.util.HashMap;

import cls.Player;
import cls.map.Tile;
import cls.unit.Unit;

public abstract class Building extends cls.GameObject {

	public static final Player NEUTRAL_OWNER = null;
	
	protected Tile[] buildingConditions; 
	protected int turnsToBuild;
	protected int influenceToCapture;
	protected int turnsToCapture;
	
	protected int moneyProvided;
	protected Unit.Kind resupplies;
	protected HashMap<Unit.Kind, Integer> healthRestoration;
	protected HashMap<Unit.Kind, Integer> spaceForUnits; 
	
	protected Player owner;
	protected Player capturer;
	protected int turnsLeftForCapture;
	
	public int getTurnsToBuild() { return turnsToBuild; }
	public int getTurnsUntilCapture() { return turnsLeftForCapture; }
	public int getMoneyProvided() { return moneyProvided; }
	public Player getOwner() { return owner; }
	public int getHealthRestoration(Unit u) {
		if (healthRestoration.containsKey(u.getUnitKind())) {
			return healthRestoration.get(u.getUnitKind());
		} else { 
			return 0;
		}
	}
	public boolean canBeBuildOn(Tile tile) {
		for (Tile t : buildingConditions) {
			if (t == tile) return true;
		}
		return false;
	}

	protected abstract void initialise();
	
	public Building(String name, Player owner) {
		super(name);
		this.owner = owner;
		initialise();
	}
	
	public void tryCapture(Unit unit) {
		if (unit.getOwner() != capturer) {
			capturer = unit.getOwner();
			turnsLeftForCapture = turnsToCapture;
		}
		if (unit.getInfluence() >= influenceToCapture) {
			turnsLeftForCapture --;
		}
	}
	
}
