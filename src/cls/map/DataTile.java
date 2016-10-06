package cls.map;

import java.awt.Rectangle;
import java.util.HashMap;

import cls.unit.Unit;

public class DataTile {
	
	public final static int TILE_SIZE = 32;
	public final static jog.Image TILE_IMAGE = new jog.Image("mods/core/resources/tiles.png");
	
	public final String name;
	public final int visionCost;
	public final int visionBonus;
	public final int defaultDefenceBonus;
	public final HashMap<Unit.Movement, Integer> defenceBonuses;
	public final int defaultMovementCost;
	public final HashMap<Unit.Movement, Integer> movementCosts;
	public final boolean isAnimated;
	public final lib.Animation animation;
	public final Rectangle tileQuad;

	public DataTile(String name, int visionCost, int visionBonus,
					int defaultDefenceBonus, HashMap<Unit.Movement, Integer> defenceBonuses,
					int defaultMovementCost, HashMap<Unit.Movement, Integer> movementCosts,
					boolean isAnimated, lib.Animation animation, Rectangle tileQuad) {
		this.name = name;
		this.visionCost = visionCost;
		this.visionBonus = visionBonus;
		this.defaultDefenceBonus = defaultDefenceBonus;
		this.defenceBonuses = defenceBonuses;
		this.defaultMovementCost = defaultMovementCost;
		this.movementCosts = movementCosts;
		this.isAnimated = isAnimated;
		this.animation = animation;
		this.tileQuad = tileQuad;
	}
	
	public Tile newTile() {
		return new Tile(this);
	}

}
