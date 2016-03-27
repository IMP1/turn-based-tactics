package run;

import lib.Sprite;

import cls.unit.DataUnit;
import cls.unit.Unit;
import cls.unit.Unit.Kind;
import cls.unit.Unit.Movement;
import cls.weapon.DataWeapon;

public final class Data {
	private Data() {}
	
	public static class MissingDataException extends RuntimeException {
		private static final long serialVersionUID = -7046121332447383903L;
		public MissingDataException(String message) {
			super(message);
		}
	}

	private static DataUnit[] units = new DataUnit[0];
	private static DataWeapon[] weapons = new DataWeapon[0];

	public static DataUnit getUnit(String name) {
		for (DataUnit u : units) {
			if (u.name.equals(name)) return u;
		}
		return null;
	}
	
	public static DataWeapon getWeapon(String name) {
		for (DataWeapon w : weapons) {
			if (w.name.equals(name)) return w;
		}
		return null;
	}
	
	public static void load() {
		loadWeapons();
		loadUnits();
	}
	
	private static void loadWeapons() {
		java.util.ArrayList<DataWeapon> dataWeapons = new java.util.ArrayList<DataWeapon>();
		for (String filename : jog.Filesystem.enumerate("dat/weapon")) {
			try {
				DataWeapon u = readWeapon("dat/weapon/" + filename);
				dataWeapons.add(u);
			} catch (Exception e) {
				System.err.printf("Could not read weapon '%s'.\n", filename);
				e.printStackTrace();
			}
		}
		weapons = dataWeapons.toArray(weapons);
	}
	
	private static void loadUnits() {
		java.util.ArrayList<DataUnit> dataActors = new java.util.ArrayList<DataUnit>();
		for (String filename : jog.Filesystem.enumerate("dat/unit")) {
			try {
				DataUnit u = readUnit("dat/unit/" + filename);
				dataActors.add(u);
			} catch (Exception e) {
				System.err.printf("Could not read unit '%s'.\n", filename);
				e.printStackTrace();
			}
		}
		units = dataActors.toArray(units);
	}
	
	private static DataWeapon readWeapon(String filename) {
		String[] weaponData = jog.Filesystem.readFile(filename).split("\n");
		String name = weaponData[0];
		
		String[] rangeData = weaponData[1].split(" ");
		int minRange = Integer.valueOf(rangeData[0]);
		int maxRange = Integer.valueOf(rangeData[1]);
		
		String[] ammoData = weaponData[2].split(" ");
		int startingAmmo = Integer.valueOf(ammoData[0]);
		int maxAmmo = Integer.valueOf(ammoData[1]);
		
		String[] unitData = weaponData[3].split(" ");
		Unit.Kind[] unitsCanAttack;
		Unit.Kind[] unitsCannotAttack;
		if (unitData[0].equals("ALL_BUT")) {
			unitsCanAttack = Unit.Kind.values();
			unitsCannotAttack = new Unit.Kind[unitData.length - 1];
			for (int i = 0; i < unitsCannotAttack.length; i ++) {
				unitsCannotAttack[i] = Unit.Kind.valueOf(unitData[i+1]);
			}
		} else if (unitData[0].equals("ONLY")) {
			unitsCannotAttack = new Unit.Kind[0];
			unitsCanAttack = new Unit.Kind[unitData.length - 1];
			for (int i = 0; i < unitsCanAttack.length; i ++) {
				unitsCanAttack[i] = Unit.Kind.valueOf(unitData[i+1]);
			}
		} else {
			String message = "Incorrect formatting of Weapon Data. Unit types must be preceded with either ALL_BUT or ONLY. " + 
							 "You gave '" + unitData[0] + "'.\n\tIn " + filename + ".";
			throw new RuntimeException(message);
		}
		
		return new DataWeapon(name, minRange, maxRange, startingAmmo, maxAmmo, unitsCanAttack, unitsCannotAttack);
	}
	
	private static DataUnit readUnit(String filename) {
		String[] unitData = jog.Filesystem.readFile(filename).split("\n");
		String name = unitData[0];
		Kind kind = Kind.valueOf(unitData[1]);
		
		String[] movementData = unitData[2].split(" ");
		Movement movement = Movement.valueOf(movementData[0]);
		int movementDistance = Integer.valueOf(movementData[1]);
		
		String[] fuelData = unitData[3].split(" ");
		int startingFuel = Integer.valueOf(fuelData[0]);
		int maxFuel = Integer.valueOf(fuelData[1]);
		int fuelCost = Integer.valueOf(fuelData[2]);
		boolean diesOnFuelDepletion = Boolean.valueOf(fuelData[3]);
		
		String[] miscData = unitData[4].split(" ");
		int vision = Integer.valueOf(miscData[0]);
		int defence = Integer.valueOf(miscData[1]);
		int influence = Integer.valueOf(miscData[2]);
		boolean canMoveAndAttack = Boolean.valueOf(miscData[3]);
		boolean canBuild = Boolean.valueOf(miscData[4]);
		
		String[] spriteData = unitData[5].split(" ");
		String spriteFilename = spriteData[0];
		int spritePoses = Integer.valueOf(spriteData[1]);
		int spriteFrames = Integer.valueOf(spriteData[2]);
		double spriteDelay = Double.valueOf(spriteData[3]);
		Sprite sprite = new Sprite(new jog.Image(spriteFilename), spritePoses, spriteFrames, spriteDelay);
		
		String[] iconData = unitData[6].split(" ");
		String iconFilename = iconData[0];
		int iconPoses = Integer.valueOf(iconData[1]);
		int iconFrames = Integer.valueOf(iconData[2]);
		double iconDelay = Double.valueOf(iconData[3]);
		Sprite icon = new Sprite(new jog.Image(iconFilename), iconPoses, iconFrames, iconDelay);
		
		DataWeapon[] weapons;
		if (unitData.length < 8 || unitData[7].isEmpty()) {
			weapons = new DataWeapon[0];
		} else {
			String[] weaponNames = unitData[7].split(" ");
			weapons = new DataWeapon[weaponNames.length];
			for (int i = 0; i < weapons.length; i ++) {
				weapons[i] = getWeapon(weaponNames[i]);
			}
		}
		
		return new DataUnit(name, kind, movement, movementDistance, fuelCost, startingFuel, maxFuel, diesOnFuelDepletion,
						    vision, defence, influence, canMoveAndAttack, canBuild, sprite, icon, weapons);
	}

}
