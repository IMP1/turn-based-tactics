package cls.weapon;

import cls.unit.Unit;

public class DataWeapon {
	
	public final String name;
	public final int minRange;
	public final int maxRange;
	public final int startingAmmo;
	public final int maxAmmo;
	public final Unit.Kind[] canAttack;
	public final Unit.Kind[] cannotAttack;

	public DataWeapon(String name, int minRange, int maxRange, int startingAmmo, int maxAmmo, Unit.Kind[] canAttack, Unit.Kind[] cannotAttack) {
		this.name = name;
		this.minRange = minRange;
		this.maxRange = maxRange;
		this.startingAmmo = startingAmmo;
		this.maxAmmo = maxAmmo;
		this.canAttack = canAttack;
		this.cannotAttack = cannotAttack;
	}
	
	public Weapon newWeapon() {
		Weapon w = new Weapon(name);
		w.initialise(minRange, maxRange, startingAmmo, maxAmmo, canAttack, cannotAttack);
		return w;
	}

}
