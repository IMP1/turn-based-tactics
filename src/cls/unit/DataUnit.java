package cls.unit;

import lib.Sprite;
import cls.Player;
import cls.unit.Unit.Kind;
import cls.unit.Unit.Movement;
import cls.weapon.DataWeapon;
import cls.weapon.Weapon;

public class DataUnit {
	
	public final String          name;
	public final Kind            unitClass;
	public final Movement        movement;
	public final int             moveDistance;
	public final int             visionDistance;
	public final int             defence;
	public final int             influence;
	public final int             fuelCost;
	public final boolean         diesOnFuelDepletion;
	public final boolean         canMoveAndAttack;
	public final boolean         canBuild;
	// TODO handle ability to build somewhere
	public final int             startingFuel;
	public final int             maxFuel;
	public final Sprite          sprite;
	public final Sprite          icon;
	public final DataWeapon[]    weapons;
	
	public DataUnit(String name, Kind unitClass, Movement movement, int moveDistance,  
			        int fuelCost, int startingFuel, int maxFuel, boolean diesOnFuelDepletion,
			        int visionDistance, int defence, int influence, 
			        boolean canMoveAndAttack, boolean canBuild, 
			        Sprite sprite, Sprite icon, DataWeapon[] weapons) {
		this.name = name;
		this.weapons = weapons;
		this.unitClass = unitClass;
		this.movement = movement;
		this.moveDistance = moveDistance;
		this.visionDistance = visionDistance;
		this.defence = defence;
		this.influence = influence;
		this.fuelCost = fuelCost;
		this.diesOnFuelDepletion = diesOnFuelDepletion;
		this.canMoveAndAttack = canMoveAndAttack;
		this.canBuild = canBuild;
		this.startingFuel = startingFuel;
		this.maxFuel = maxFuel;
		this.sprite = sprite;
		this.icon = icon;
	}
	
	public Unit newUnit(Player owner, int x, int y) {
		Weapon[] startingWeapons = new Weapon[weapons.length];
		for (int i = 0; i < startingWeapons.length; i ++) {
			startingWeapons[i] = weapons[i].newWeapon();
		}
		Unit u = new Unit(name, owner, x, y);
		u.initialise(unitClass, movement, moveDistance, startingFuel, maxFuel, fuelCost, diesOnFuelDepletion, 
				     visionDistance, defence, influence, canMoveAndAttack, canBuild, startingWeapons, sprite, icon);
		return u;
	}

}
