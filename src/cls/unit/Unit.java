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

	private DataUnit data;

	public Kind     getUnitKind()       { return data.unitClass;        }
	public Movement getMovementType()   { return data.movement;         }
	public int      getMoveDistance()   { return data.moveDistance;     }
	public int      getVisionDistance() { return data.visionDistance;   }
	public int      getDefence()        { return data.defence;          }
	public int      getInfluence()      { return data.influence;        }
	public boolean  canMoveAndAttack()  { return data.canMoveAndAttack; }
	
	private Player   owner;
	private int      health;
	private Weapon[] weapons;
	private int      fuel;
	private boolean  hasMoved;
	private boolean  isMoving;
	private boolean  isExhausted;
	private int[]    movePath;
	private int      pathPosition;
	private double   animationX;
	private double   animationY;
	private boolean  isDestroyed;
	private Sprite   sprite;
	private Sprite   icon;
	
	public int     getFuel()     { return fuel;        }
	public boolean isExhausted() { return isExhausted; }
	public Player  getOwner()    { return owner;       }
	public int     getHealth()   { return health;      }
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
	
	protected Unit(Player owner, int x, int y, DataUnit data) {
		super(data.name);
		this.owner = owner;
		this.x = x;
		this.y = y;
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
		hasMoved = false;
		isExhausted = false;
	}
	
	@Override
	public void refresh() {
		fuel -= data.fuelCost;
		isExhausted = false;
		hasMoved = false;
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
			jog.Graphics.draw(sprite, x * Tile.TILE_SIZE, y * Tile.TILE_SIZE);
		}
	}
	
}
