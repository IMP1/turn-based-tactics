package cls.map;

import cls.unit.Unit;

public class BattleAnimation extends jog.Graphics.Drawable {
	
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
	
	public BattleAnimation(Tile tileLeft, Tile tileRight, Unit unitLeft, Unit unitRight) {
		super(new jog.Image(1, 1));
		tiles = new Tile[] { tileLeft, tileRight };
		units = new Unit[] { unitLeft, unitRight };
		
	}
	
	public void start() {
		// TODO implement animation
		hasStarted = true;
	}

	public void update(double dt) {
		hasFinished = true;
	}
	
	public void draw() {
		System.out.println("playing beeyootiful animation");
	}
	
}
