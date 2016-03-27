package cls.weapon;

import lib.Sprite;
import cls.unit.Unit;

public class Weapon {
	
	public final String name;
	
	protected int rangeMin;
	protected int rangeMax;
	protected int attack;
	protected int maxAmmo;
	protected Unit.Kind[] unitsCanAttack;
	protected Unit.Kind[] unitsCannotAttack;
	protected Sprite icon;
	
	protected int ammo;
	
	private boolean isInitialised;
	
	public int getMinimumRange() { return rangeMin; }
	public int getMaximumRange() { return rangeMax; }
	public int getAmmo() { return ammo; }
	public int getDamage() { return attack; }

	public Weapon(String name) {
		this.name = name;
		this.isInitialised = false;
	}

	public void initialise(int minRange, int maxRange, int startingAmmo, int maxAmmo, Unit.Kind[] canAttack, Unit.Kind[] cannotAttack) {
		if (isInitialised) return;
		this.rangeMin = minRange;
		this.rangeMax = maxRange;
		this.ammo = startingAmmo;
		this.maxAmmo = maxAmmo;
		this.unitsCanAttack = canAttack;
		this.unitsCannotAttack = cannotAttack;
		isInitialised = true;
	}

	public boolean canAttack(Unit u) {
		boolean canAttack = false;
		for (Unit.Kind k : unitsCanAttack) {
			if (k == u.getUnitKind()) canAttack = true;
		}
		if (!canAttack) return false;
		for (Unit.Kind k : unitsCannotAttack) {
			if (k == u.getUnitKind()) return false;
		}
		return true;
	}
	
	public void drawIcon(int x, int y) {
		jog.Graphics.draw(icon, x, y);
	}
	
}
