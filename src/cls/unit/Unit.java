package cls.unit;

import java.util.ArrayList;

import run.Settings;
import scn.Battle;
import lib.Sprite;
import cls.Player;
import cls.Player.Faction;
import cls.map.DataTile;
import cls.weapon.Weapon;

public class Unit extends cls.GameObject {

	private static final int MOVE_SPEED = DataTile.TILE_SIZE * 3; 
	
	public enum Action {
		MOVE,
		ATTACK,
		BUILD,
		DEFEND,
		LOAD,
		UNLOAD,
	}
	
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

	private DataUnit data;

	public Kind     getUnitKind()       { return data.unitClass;        }
	public Movement getMovementType()   { return data.movement;         }
	public int      getMoveDistance()   { return data.moveDistance;     }
	public int      getDefence()        { return data.defence;          }
	public int      getVisionDistance() { return data.visionDistance;   }
	public int      getInfluence()      { return data.influence;        }
	public boolean  canMoveAndAttack()  { return data.canMoveAndAttack; }
	public boolean  canMoveTwice()      { return false;                 } // TODO add this into unit data.
	
	private Player          owner;
	private int             health;
	private Weapon[]        weapons;
	private int             fuel;
	private int             movesPerformed;
	private boolean         isMoving;
	private int[]           movePath;
	private int             pathPosition;
	private double          animationX;
	private double          animationY;
	private boolean         isDestroyed;
	private Sprite          sprite;
	private Sprite          icon;
	private int             spaceForUnits;
	private ArrayList<Unit> storedUnits;
	private boolean         isStored;
	private int             directionDefending;
	
	public int     getFuel()               { return fuel;                    }
	public Player  getOwner()              { return owner;                   }
	public int     getHealth()             { return health;                  }
	public int     getDirectionDefending() { return directionDefending;      }
	public boolean isDefending()           { return directionDefending > -1; }
	public boolean isDestroyed()           { return isDestroyed;             }
	
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
	
	public boolean canAttack() {
		for (Weapon w : weapons) {
			if (w.getAmmo() > 0) return true;
		}
		return false;
	}
	
	public boolean canAttack(Unit target) {
		for (Weapon w : weapons) {
			if (w.canAttack(target) && w.getAmmo() > 0) return true;
		}
		return false;
	}
	
	public int getDamage(Unit target) {
		for (Weapon w : weapons) {
			if (w.canAttack(target) && w.getAmmo() > 0) return w.getDamage();
		}
		return 0;
	}
	
	public boolean canBuild() {
		return data.buildableBuildings.length > 0;
	}

	public boolean canStoreUnit(Unit u) {
		if (spaceForUnits == 0) return false;
		if (!data.unitStorage.containsKey(u.getUnitKind())) return false;
		return data.unitStorage.get(u.getUnitKind()) <= spaceForUnits;
	}
	
	public Unit[] getStoredUnits() {
		return storedUnits.toArray(new Unit[0]);
	}
	
	public boolean isCarringUnits() {
		return storedUnits.size() > 0;
	}

	public boolean tryLoadUnit(Unit u) {
		if (canStoreUnit(u)) {
			storedUnits.add(u);
			u.isStored = true;
			spaceForUnits -= data.unitStorage.get(u.getUnitKind());
			return true;
		} else {
			return false;
		}
	}
	
	public void unloadUnit(Unit u, int x, int y) {
		u.isStored = false;
		u.setPosition(x, y);
		storedUnits.remove(u);
		spaceForUnits += data.unitStorage.get(u.getUnitKind());
	}
	
	public boolean isStored() {
		return isStored;
	}
	
	public boolean isMoving() {
		return isMoving;
	}
	
	public boolean canMove() {
		if (canMoveTwice()) {
			return movesPerformed < 2;
		} else {
			return movesPerformed < 1;
		}
	}
	
	public boolean isCarrying(Unit u) {
		return storedUnits.contains(u);
	}
	
	protected Unit(Player owner, int x, int y, DataUnit data) {
		super(data.name);
		this.owner = owner;
		setPosition(x, y);
		this.data = data;
		// Create Weapon instances
		weapons = new Weapon[data.weapons.length];
		for (int i = 0; i < weapons.length; i ++) {
			weapons [i] = data.weapons[i].newWeapon();
		}
		// Create graphics instances
		sprite = data.sprite.newSprite();
		icon = data.icon.newSprite();
		
		health = 100;
		fuel = data.startingFuel;
		isDestroyed = false;
		isMoving = false;
		movesPerformed = 0;
		spaceForUnits = data.totalSpace;
		storedUnits = new ArrayList<Unit>();
		isStored = false;
	}
	
	protected void setPosition(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	@Override
	public void refresh() {
		fuel -= data.fuelCost;
		movesPerformed = 0;
	}
	
	public void refuel() {
		fuel = data.maxFuel;
	}

	/**
	 * Called on the depletion of fuel.
	 */
	public void depleteFuel() {
		if (data.diesOnFuelDepletion) {
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

	public void move(int[] path) {
		System.out.printf("Moving %s.\n", name);
		isMoving = true;
		directionDefending = -1;
		movePath = path;
		pathPosition = -1;
		nextPathPoint();
		if (!Settings.showMoveAnimations) {
			while (isMoving) nextPathPoint();
		}
	}

	public void attack(Unit defender) {
		
	}

	public void defendTowards(int direction) {
		directionDefending = direction;
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
		if ((dx < 0 && animationX < targetX * DataTile.TILE_SIZE) || (dx > 0 && animationX > targetX * DataTile.TILE_SIZE)) {
			nextPathPoint();
			return;
		}
		if ((dy < 0 && animationY < targetY * DataTile.TILE_SIZE) || (dy > 0 && animationY > targetY * DataTile.TILE_SIZE)) {
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
		animationX = currentX * DataTile.TILE_SIZE;
		animationY = currentY * DataTile.TILE_SIZE;
	}

	private void finishMoving() {
		isMoving = false;
		movesPerformed ++;
		sprite.setPose(0);
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
	
	public void drawConciseInfo() {
		drawUnitInfo(Battle.GUI_INFO_BOX_WIDTH, Battle.GUI_INFO_BOX_HEIGHT);
	}
	
	/**
	 * Draws info about the unit (health, ammo, etc.)
	 */
	public void drawFullInfo() {
		drawCarriedUnits();
		drawUnitInfo(Battle.GUI_ORDER_BOX_WIDTH, Battle.GUI_ORDER_BOX_HEIGHT);
	}

	protected void drawIcon(int x, int y) {
		jog.Graphics.draw(icon, x, y);
	}
	
	protected void drawHealthBar(int x, int y, int w, int h) {
		jog.Graphics.setColour(0, 0, 0);
		jog.Graphics.rectangle(true, x, y, w, h);
		jog.Graphics.setColour(getHealthColour());
		double healthWidth = w * health / 100;
		jog.Graphics.rectangle(true, x + 1, y + 1, healthWidth - 2, h - 2);
	}
	
	private void drawUnitInfo(int w, int h) {
		// draw border
		jog.Graphics.setColour(255, 255, 255);
		jog.Graphics.rectangle(true, 0, 0, w, h);
		jog.Graphics.setColour(0, 0, 0);
		jog.Graphics.rectangle(false, 0, 0, w, h);
		
		jog.Graphics.print(name, 0, 0, w, 16, jog.Graphics.HorizontalAlign.CENTRE);
		drawHealthBar(4, 24, w - 8, 4);
		drawIcon((w - icon.getWidth()) / 2, 40);
		// TODO draw fuel
		// TODO draw ammo
	}
	
	private void drawCarriedUnits() {
		final int size = 32;
		int h = Battle.GUI_ORDER_BOX_HEIGHT;
		int n = h / size - 1;
		for (int i = 0; i < storedUnits.size(); i ++) {
			int x = ((i / n) + 1) * -size + 4;
			int y = (i % n) * size + (size / 2);
			jog.Graphics.setColour(255, 255, 255);
			jog.Graphics.rectangle(true, x, y, size, size);
			jog.Graphics.setColour(0, 0, 0);
			jog.Graphics.rectangle(false, x, y, size, size);
			storedUnits.get(i).drawIcon(x, y);
		}
	}
	
	@Override
	public void draw() {
		if (isMoving) {
			jog.Graphics.draw(sprite, animationX, animationY);
		} else {
			if (owner.faction == Faction.BLUE_SKY_CORPS) {
				jog.Graphics.draw(sprite, x * DataTile.TILE_SIZE, y * DataTile.TILE_SIZE);
			} else {
				jog.Graphics.draw(sprite, x * DataTile.TILE_SIZE, y * DataTile.TILE_SIZE, -1, 1, 0, DataTile.TILE_SIZE / 2, DataTile.TILE_SIZE / 2);
			}
		}
	}
	
}
