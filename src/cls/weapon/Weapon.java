package cls.weapon;

import lib.Sprite;
import cls.unit.Unit;

public class Weapon {
	
	private DataWeapon data;
	private int ammo;
	private Sprite icon;
	
	public int getMinimumRange() { return data.minRange; }
	public int getMaximumRange() { return data.maxRange; }
	
	public int getDamage() { return data.damage; }
	public int getAmmo() { return ammo; }

	public Weapon(DataWeapon data) {
		this.data = data;
		this.ammo = data.startingAmmo;
		this.icon = data.icon.newSprite();
	}

	public boolean canAttack(Unit u) {
		boolean canAttack = false;
		for (Unit.Kind k : data.canAttack) {
			if (k == u.getUnitKind()) canAttack = true;
		}
		if (!canAttack) return false;
		for (Unit.Kind k : data.cannotAttack) {
			if (k == u.getUnitKind()) return false;
		}
		return true;
	}
	
	public void drawIcon(int x, int y) {
		jog.Graphics.draw(icon, x, y);
	}
	
}
