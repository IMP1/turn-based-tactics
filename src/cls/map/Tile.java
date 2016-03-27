package cls.map;

import java.awt.Rectangle;
import java.util.HashMap;

import cls.unit.Unit;

public final class Tile {
	
	public final static int TILE_SIZE = 32;
	
	public static Tile GRASS = new Tile("Grass", new Rectangle(0, 0, TILE_SIZE, TILE_SIZE));
	public static Tile ROAD = new Tile("Road", new Rectangle(TILE_SIZE, 0, TILE_SIZE, TILE_SIZE));
	
	private static jog.Image tileImage = new jog.Image("gfx/tiles.png");

	public final String name;
	private HashMap<Unit.Movement, Integer> defenceBonuses = new HashMap<Unit.Movement, Integer>();
	private int visionCost = 1;
	private int visionBonus = 0;
	private HashMap<Unit.Movement, Integer> movementCosts = new HashMap<Unit.Movement, Integer>();
	private boolean animated;
	private lib.Animation animation;
	private Rectangle tileQuad;
	
	public int getDefenceBonus(Unit u) {
		if (defenceBonuses.containsKey(u.getMovementType())) {
			return defenceBonuses.get(u.getMovementType());
		} else {
			return 0;
		}
	}

	public int getMovementCost(Unit u) {
		if (movementCosts.containsKey(u.getMovementType())) {
			return movementCosts.get(u.getMovementType());
		} else {
			return 1;
		}
	}
	
	public int getVisionCost() { return visionCost; }
	public int getVisionBonus() { return visionBonus; }
	
	private Tile(String name) {
		this.name = name;
	}
	
	private Tile(String name, lib.Animation animation) {
		this(name);
		animated = true;
		this.animation = animation;
	}
	
	private Tile(String name, Rectangle rectangle) {
		this(name);
		animated = false;
		this.tileQuad = rectangle;
	}

	public void update(double dt) {
		if (animation != null) animation.update(dt);
	}

	public void draw(int x, int y) {
		if (animated) {
			jog.Graphics.draw(animation, x, y);
		} else {
			jog.Graphics.draw(tileImage, tileQuad, x, y);
		}
		
	}

}
