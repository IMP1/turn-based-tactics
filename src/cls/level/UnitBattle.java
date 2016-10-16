package cls.map;

import jog.Graphics.HorizontalAlign;
import cls.unit.Unit;

public class UnitBattle {
	
	private final Tile[] tiles;
	private final Unit[] units;

	private boolean hasStarted;
	private boolean hasFinished;
	
	public boolean hasFinished() {
		return hasFinished;
	}
	
	public boolean isPlaying() {
		return hasStarted && !hasFinished;
	}
	
	private double duration = 2;
	private double timer;
	private boolean resolved;
	
	public UnitBattle(Tile tileLeft, Tile tileRight, Unit unitLeft, Unit unitRight) {
		tiles = new Tile[] { tileLeft, tileRight };
		units = new Unit[] { unitLeft, unitRight };
		
	}
	
	public void start() {
		// TODO implement animation
		timer = 0;
		hasStarted = true;
		resolved = false;
	}

	public void update(double dt) {
		timer += dt;
		if (timer >= duration) hasFinished = true;
	}
	
	public void resolve() {
		if (resolved) return;
		// TODO deal damage.
		resolved = true;
	}
	
	public void draw() {
		int ox = jog.Graphics.getWidth() / 2;
		jog.Graphics.setColour(255, 255, 255);
		jog.Graphics.rectangle(true, ox - 128, 32, 256, 128);
		jog.Graphics.setColour(0, 0, 0);
		jog.Graphics.print(units[0].name, ox - 16, 64, HorizontalAlign.RIGHT);
		jog.Graphics.print("vs", ox, 64, HorizontalAlign.CENTRE);
		jog.Graphics.print(units[1].name, ox + 16, 64, HorizontalAlign.LEFT);
		
		jog.Graphics.print(tiles[0].name, ox - 16, 96, HorizontalAlign.RIGHT);
		jog.Graphics.print(tiles[1].name, ox + 16, 96, HorizontalAlign.LEFT);
		
		jog.Graphics.arc(true, ox, 128, 16, 0, 2 * Math.PI * timer / duration);
	}
	
}
