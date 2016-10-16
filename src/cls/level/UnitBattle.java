package cls.level;

import jog.Graphics.HorizontalAlign;
import cls.map.Tile;
import cls.unit.Unit;

public class UnitBattle {
	
	private final Tile[] tiles;
	private final Unit[] units;
	
	private final int attacker;
	private final int defender;
	private final int left;
	private final int right;

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
	
	public UnitBattle(Tile attackerTile, Tile defenderTile, Unit attacker, Unit defender) {
		this.units = new Unit[] { attacker, defender };
		this.tiles = new Tile[] { attackerTile, defenderTile };
		this.attacker = 0;
		this.defender = 1;
		if (attacker.getX() <= defender.getX()) {
			left = 0;
			right = 1;
		} else {
			left = 1;
			right = 0;
		}
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
		dealDamage();
		resolved = true;
	}
	
	private void dealDamage() {
		int atk = getAttackerDamage();
		int def = getDefenderDefence();
		int dam = atk - def;
		units[defender].damage(dam);
		// TODO: add the other way round.
	}
	
	private int getAttackerDamage() {
		return units[attacker].getDamage(units[defender]);
	}
	
	private int getDefenderDefence() {
		int baseDefence = units[defender].getDefence();
		baseDefence += tiles[defender].getDefenceBonus(units[defender]);
		return baseDefence;
	}
	
	private boolean isDefenderDefending() {
		int dir = units[defender].getDirectionDefending();
		
		return false; // TODO: calulate direction and whether attacker is in arc.
	}
	
	public void draw() {
		int ox = jog.Graphics.getWidth() / 2;
		jog.Graphics.setColour(255, 255, 255);
		jog.Graphics.rectangle(true, ox - 128, 32, 256, 128);
		jog.Graphics.setColour(0, 0, 0);
		jog.Graphics.print(units[left].name, ox - 16, 64, HorizontalAlign.RIGHT);
		jog.Graphics.print("vs", ox, 64, HorizontalAlign.CENTRE);
		jog.Graphics.print(units[right].name, ox + 16, 64, HorizontalAlign.LEFT);
		
		jog.Graphics.print(tiles[left].name, ox - 16, 96, HorizontalAlign.RIGHT);
		jog.Graphics.print(tiles[right].name, ox + 16, 96, HorizontalAlign.LEFT);
		
		jog.Graphics.arc(true, ox, 128, 16, 0, 2 * Math.PI * timer / duration);
	}
	
}
