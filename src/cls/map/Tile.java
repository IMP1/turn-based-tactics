package cls.map;

import cls.unit.Unit;

public final class Tile {
	
	public final String name;
	private DataTile data;
	
	public int getDefenceBonus(Unit u) {
		if (data.defenceBonuses.containsKey(u.getMovementType())) {
			return data.defenceBonuses.get(u.getMovementType());
		} else {
			return data.defaultDefenceBonus;
		}
	}

	public int getMovementCost(Unit u) {
		if (data.movementCosts.containsKey(u.getMovementType())) {
			return data.movementCosts.get(u.getMovementType());
		} else {
			return data.defaultMovementCost;
		}
	}
	
	public int getVisionCost() { return data.visionCost; }
	public int getVisionBonus() { return data.visionBonus; }
	
	protected Tile(DataTile data) {
		this.data = data;
		this.name = data.name;
	}
	
	public void update(double dt) {
		if (data.isAnimated) data.animation.update(dt);
	}

	public void draw(int x, int y) {
		if (data.isAnimated) {
			jog.Graphics.draw(data.animation, x, y);
		} else {
			jog.Graphics.draw(DataTile.TILE_IMAGE, data.tileQuad, x, y);
		}
		
	}

}
