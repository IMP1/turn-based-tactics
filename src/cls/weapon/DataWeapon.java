package cls.weapon;

import cls.unit.DataSprite;
import cls.unit.Unit;

public class DataWeapon {
	
	public final String      name;
	public final int         minRange;
	public final int         maxRange;
	public final int         damage;
	public final int         startingAmmo;
	public final int         maxAmmo;
	public final Unit.Kind[] canAttack;
	public final Unit.Kind[] cannotAttack;
	public final DataSprite  icon;

	public DataWeapon(String name, int minRange, int maxRange, int damage, int startingAmmo, int maxAmmo, 
			          Unit.Kind[] canAttack, Unit.Kind[] cannotAttack, DataSprite icon) {
		this.name = name;
		this.minRange = minRange;
		this.maxRange = maxRange;
		this.damage = damage;
		this.startingAmmo = startingAmmo;
		this.maxAmmo = maxAmmo;
		this.canAttack = canAttack;
		this.cannotAttack = cannotAttack;
		this.icon = icon;
	}
	
	public Weapon newWeapon() {
		return new Weapon(this);
	}

}
