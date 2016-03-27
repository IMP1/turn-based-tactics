package cls.unit;

import lib.Sprite;
import cls.Player;
import cls.map.Tile;
import cls.weapon.Weapon;

public class Unit extends cls.GameObject {
	
	public enum Kind {
		INFANTRY,
		VEHICLE,
		SHIP,
		AIRCRAFT
	}
	
	public enum Movement {
		FOOT, 
		WHEELS, 
		TRACKS,
		ROTOR_BLADES,
		JET_ENGINE,
	}

	protected Kind unitClass;
	protected Movement movementType;
	protected int moveDistance;
	protected int visionDistance;
	protected int defence;
	protected int influence;
	protected int fuelCost;
	protected int maxFuel;
	protected boolean diesOnFuelDepletion;
	protected boolean canAttackAndMove;
	protected boolean canBuild;
	
	protected Sprite sprite;
	protected Sprite icon;
	
	protected Player owner;
	protected int health;
	protected Weapon[] weapons;
	protected int fuel;
	protected boolean isExhausted;
	
	private boolean isInitialised;
	
	public Kind getUnitKind() { return unitClass; }
	public Movement getMovementType() { return movementType; }
	public int getMoveDistance() { return moveDistance; }
	public int getVisionDistance() { return visionDistance; }
	public int getDefence() { return defence; }
	public int getInfluence() { return influence; }
	public Player getOwner() { return owner; }
	public int getHealth() { return health; }
	public boolean canAttackAndMove() { return canAttackAndMove; }
	public int getFuel() { return fuel; }
	public boolean isExhausted() { return isExhausted; }
	public int getMinimumRange() {
		if (weapons.length == 0) return 0;
		int min = weapons[0].getMinimumRange();
		for (Weapon w : weapons) {
			if (w.getMinimumRange() < min) min = w.getMinimumRange();
		}
		return min;
	}
	public int getMaximumRange() {
		if (weapons.length == 0) return 0;
		int max = weapons[0].getMaximumRange();
		for (Weapon w : weapons) {
			if (w.getMaximumRange() < max) max = w.getMaximumRange();
		}
		return max;
		
	}
	
	protected Unit(String name, Player owner, int x, int y) {
		super(name);
		this.owner = owner;
		this.health = 100;
		this.x = x;
		this.y = y;
		isInitialised = false;
	}
	
	public void initialise(Kind unitKind, Movement movement, int moveDistance,
			               int startFuel, int maxFuel, int fuelCost, boolean diesOnFuelDepletion,
			               int vision, int defence, int influence, boolean canAttackAndMove, boolean canBuild,
			               Weapon[] weapons, Sprite sprite, Sprite icon) {
		if (isInitialised) return;
		this.unitClass = unitKind;
		this.movementType = movement;
		this.moveDistance = moveDistance;
		this.fuel = startFuel;
		this.maxFuel = maxFuel;
		this.fuelCost = fuelCost;
		this.diesOnFuelDepletion = diesOnFuelDepletion;
		this.visionDistance = vision;
		this.defence = defence;
		this.influence = influence;
		this.canAttackAndMove = canAttackAndMove;
		this.canBuild = canBuild;
		this.weapons = weapons;
		this.sprite = sprite;
		this.icon = icon;
		isInitialised = true;
	}
	
	@Override
	public void refresh() {
		isExhausted = false;
	}
	
	public void refuel() {
		fuel = maxFuel;
	}

	/**
	 * Called on the depletion of fuel.
	 */
	public void depleteFuel() {
		
	}
	
	/**
	 * Called on the destruction of the unit
	 */
	public void destroy() {}
	
	/**
	 * Draws info about the unit (health, ammo, etc.)
	 */
	public void drawInfo() {
		int w = jog.Graphics.getScissor().width;
//		int h = jog.Graphics.getScissor().height;
		jog.Graphics.print(name, 0, 0, w, 16, jog.Graphics.HorizontalAlign.CENTRE);
		jog.Graphics.setColour(0, 0, 0);
		jog.Graphics.rectangle(true, 4, 24, w - 8, 4);
		jog.Graphics.setColour(getHealthColour()); // TODO change to getHealthColour, which will adapt to the health (lower health = more red)
		jog.Graphics.rectangle(true, 5, 25, (w - 8) * health / 100 - 2, 2);
		
		int x = (w - icon.getWidth()) / 2;
		jog.Graphics.draw(icon, x, 40);
		// TODO draw fuel
		// TODO draw ammo
	}
	
	protected java.awt.Color getHealthColour() {
		double h = health / 100.0;
		int r, g;
		if (h < 0.5) {
			r = 255;
			g = (int)lib.maths.Easing.linear(h, 255, 0.5); 
		} else {
			g = 255;
			r = 255 - (int)lib.maths.Easing.linear(h - 0.5, 255, 0.5);
		}
		return new java.awt.Color(r, g, 0);
	}
	
	/**
	 * Draw the GUI of the actions that this unit can perform (eg moving, attacking).
	 */
	public void drawActions() {
		
	}
	public void damage(int dam) {
		health -= dam;
		if (health < 0) health = 0;
		if (health == 0) {
			destroy();
		}
	}
	@Override
	public void update(double dt) {
		icon.update(dt);
	}
	
	@Override
	public void draw() {
		jog.Graphics.draw(sprite, x * Tile.TILE_SIZE, y * Tile.TILE_SIZE);
	}
	
}
