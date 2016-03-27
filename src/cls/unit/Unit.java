package cls.unit;

import lib.Sprite;
import cls.Player;
import cls.map.Tile;
import cls.weapon.Weapon;

public class Unit extends cls.GameObject {

	private static final int MOVE_SPEED = Tile.TILE_SIZE * 2; 
	
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

	private Kind unitClass;
	private Movement movementType;
	private int moveDistance;
	private int visionDistance;
	private int defence;
	private int influence;
	private int fuelCost;
	private int maxFuel;
	private boolean diesOnFuelDepletion;
	private boolean canAttackAndMove;
	private boolean canBuild;
	
	private Sprite sprite;
	private Sprite icon;
	
	private Player owner;
	private int health;
	private Weapon[] weapons;
	private int fuel;
	private boolean hasMoved;
	private boolean isExhausted;
	private boolean isMoving;
	private int[] movePath;
	private int pathPosition;
	private double animationX;
	private double animationY;
	
	private boolean isDestroyed;
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
	
	public boolean isDestroyed() { return isDestroyed; }
	
	public int getMinimumRange() {
		if (weapons.length == 0) return -1;
		int min = weapons[0].getMinimumRange();
		for (Weapon w : weapons) {
			if (w.getMinimumRange() < min) min = w.getMinimumRange();
		}
		return min;
	}
	public int getMaximumRange() {
		if (weapons.length == 0) return -1;
		int max = weapons[0].getMaximumRange();
		for (Weapon w : weapons) {
			if (w.getMaximumRange() < max) max = w.getMaximumRange();
		}
		return max;
	}
	
	public boolean isMoving() {
		return isMoving;
	}
	
	public boolean hasMoved() {
		return hasMoved;
	}
	
	protected Unit(String name, Player owner, int x, int y) {
		super(name);
		this.owner = owner;
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
		
		health = 100;
		isDestroyed = false;
		isMoving = false;
		
		
		isInitialised = true;
	}
	
	@Override
	public void refresh() {
		fuel -= fuelCost;
		isExhausted = false;
		hasMoved = false;
	}
	
	public void refuel() {
		fuel = maxFuel;
	}

	/**
	 * Called on the depletion of fuel.
	 */
	public void depleteFuel() {
		if (diesOnFuelDepletion) {
			destroy();
		}
	}
	
	/**
	 * Called on the destruction of the unit
	 */
	public void destroy() {
		isDestroyed = true;
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
		sprite.update(dt);
		icon.update(dt);
		if (isMoving) {
			updateMovement(dt);
		}
	}
	
	private void updateMovement(double dt) {
		int targetX = movePath[(pathPosition+1)*2];
		int targetY = movePath[(pathPosition+1)*2+1];
		int currentX = movePath[(pathPosition)*2];
		int currentY = movePath[(pathPosition)*2+1];
		double dx = Math.signum(targetX - currentX);
		double dy = Math.signum(targetY - currentY);
		animationX += dx * dt * MOVE_SPEED;
		animationY += dy * dt * MOVE_SPEED;
		if ((dx < 0 && animationX < targetX * Tile.TILE_SIZE) || (dx > 0 && animationX > targetX * Tile.TILE_SIZE)) {
			nextPathPoint();
			return;
		}
		if ((dy < 0 && animationY < targetY * Tile.TILE_SIZE) || (dy > 0 && animationY > targetY * Tile.TILE_SIZE)) {
			nextPathPoint();
			return;
		}
			
	}
	
	private void nextPathPoint() {
		pathPosition ++;
		int currentX = movePath[(pathPosition)*2];
		int currentY = movePath[(pathPosition)*2+1];
		x = currentX;
		y = currentY;
		if ((pathPosition+1) * 2 >= movePath.length) {
			finishMoving();
			return;
		}
		int targetX = movePath[(pathPosition+1)*2];
		int targetY = movePath[(pathPosition+1)*2+1];
		if (targetX > currentX) {
			sprite.setPose(2);
		}
		if (targetX < currentX) {
			sprite.setPose(3);
		}
		if (targetY > currentY) {
			sprite.setPose(1);
		}
		if (targetY < currentY) {
			sprite.setPose(4);
		}
		animationX = currentX * Tile.TILE_SIZE;
		animationY = currentY * Tile.TILE_SIZE;
	}
	
	public void move(int[] path) {
		System.out.printf("Moving %s.\n", name);
		isMoving = true;
		movePath = path;
		pathPosition = -1;
		nextPathPoint();
	}
	
	private void finishMoving() {
		isMoving = false;
		hasMoved = true;
		sprite.setPose(0);
	}
	
	/**
	 * Draws info about the unit (health, ammo, etc.)
	 */
	public void drawInfo() {
		int w = jog.Graphics.getScissor().width;
//		int h = jog.Graphics.getScissor().height;
		jog.Graphics.print(name, 0, 0, w, 16, jog.Graphics.HorizontalAlign.CENTRE);
		jog.Graphics.setColour(0, 0, 0);
		jog.Graphics.rectangle(true, 4, 24, w - 8, 4);
		jog.Graphics.setColour(getHealthColour());
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

	@Override
	public void draw() {
		if (isMoving) {
			jog.Graphics.draw(sprite, animationX, animationY);
		} else {
			jog.Graphics.scale(-1, 1);
			jog.Graphics.draw(sprite, x * Tile.TILE_SIZE, y * Tile.TILE_SIZE);
		}
	}
	
}
