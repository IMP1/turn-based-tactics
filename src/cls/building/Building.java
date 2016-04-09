package cls.building;

import java.util.ArrayList;

import lib.Sprite;

import cls.Player;
import cls.map.DataTile;
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
	
	/**
	 * This tells you whether or not the building has had its prerequisites met.
	 * These currently are whether the player has standing other buildings.
	 * @param buildings the player's currently standing and owned buildings.
	 * @return whether this building can be built.
	 */
	public boolean requirementsAreMet(Building[] buildings) {
		for (DataBuilding requirement : data.buildingPrerequisistes) {
			boolean isMet = false;
			for (Building b : buildings) {
				if (b.data == requirement) isMet = true;
			}
			if (!isMet) return false;
		}
		return true;
	}
	
	public boolean canBeBuildOn(Tile tile) {
		
		return false;
	}
	
	public boolean canStoreUnit(Unit u) {
		if (spaceForUnits == 0) return false;
		if (!data.unitStorage.containsKey(u.getUnitKind())) return false;
		return data.unitStorage.get(u.getUnitKind()) <= spaceForUnits;
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
	
	protected Building(Player owner, int x, int y, DataBuilding data) {
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
		jog.Graphics.draw(sprite, x * DataTile.TILE_SIZE, y * DataTile.TILE_SIZE);
	}
	
}
