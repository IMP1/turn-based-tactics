package cls.building;

import java.util.ArrayList;

import lib.Sprite;

import cls.Player;
import cls.map.Tile;
import cls.unit.Unit;

public class Building extends cls.GameObject {

	public static final Player NEUTRAL_OWNER = null;
	
	private Player owner;
	private Player capturer;
	private int turnsLeftForCapture;
	private DataBuilding data;
	private Sprite sprite;
	private int spaceForUnits;
	private ArrayList<Unit> storedUnits;

	public int getTurnsToBuild()      { return data.turnsToBuild; }
	public int getMoneyProvided()     { return data.moneyProvided; }
	
	public boolean canBeBuildOn(Tile tile) {
		for (Tile t : data.buildingConditions) {
			if (t == tile) return true;
		}
		return false;
	}
	
	public boolean canStoreUnit(Unit u) {
		if (spaceForUnits == 0) return false;
		if (!data.unitStorage.containsKey(u)) return false;
		return data.unitStorage.get(u) <= spaceForUnits;
	}
	
	public boolean tryStoreUnit(Unit u) {
		if (canStoreUnit(u)) {
			storedUnits.add(u);
			return true;
		} else {
			return false;
		}
	}

	public int getHealthRestoration(Unit u) {
		if (data.healthRestoration.containsKey(u.getUnitKind())) {
			return data.healthRestoration.get(u.getUnitKind());
		} else { 
			return 0;
		}
	}
	
	public Player getOwner()          { return owner; }
	public int getTurnsUntilCapture() { return turnsLeftForCapture; }
	
	public Building(Player owner, int x, int y, DataBuilding data) {
		super(data.name);
		this.owner = owner;
		this.x = x;
		this.y = y;
		this.data = data;
		this.sprite = data.sprite.newSprite();
		this.spaceForUnits = data.totalSpace;
		this.storedUnits = new ArrayList<Unit>();
	}
	
	public boolean tryCapture(Unit unit) {
		if (unit.getOwner() != capturer) {
			capturer = unit.getOwner();
			turnsLeftForCapture = data.turnsToCapture;
		}
		if (unit.getInfluence() >= data.influenceToCapture) {
			turnsLeftForCapture --;
			if (turnsLeftForCapture == 0) {
				owner = capturer;
			}
			return true;
		} else {
			return false;
		}
	}
	@Override
	public void refresh() {
		
	}
	
	@Override
	public void update(double dt) {
		
	}
	@Override
	public void draw() {
		// TODO draw with bottom middle as the origin
		jog.Graphics.draw(sprite, x * Tile.TILE_SIZE, y * Tile.TILE_SIZE);
	}
	
}
