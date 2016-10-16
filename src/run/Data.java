package run;

import java.awt.Rectangle;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import cls.Player.Faction;
import cls.building.DataBuilding;
import cls.level.DataLevel;
import cls.map.DataMap;
import cls.map.DataTile;
import cls.unit.DataUnit;
import cls.unit.DataSprite;
import cls.unit.Unit;
import cls.unit.Unit.Kind;
import cls.unit.Unit.Movement;
import cls.weapon.DataWeapon;

public final class Data {
	private Data() {}
	
	private static Object finishedMonitor = new Object();
	private static Object messageMonitor = new Object();
	private static String message = "";
	private static boolean finished = false;
	
	private static void setFinished(boolean finished) {
		synchronized (finishedMonitor) {
			Data.finished = finished;
		}
	}
	
	public static boolean isFinished() {
		synchronized (finishedMonitor) {
			return finished;
		}
	}
	
	private static void setMessage(String message) {
		synchronized (messageMonitor) {
			Data.message = message;
		}
	}
	
	public static String getMessage() {
		synchronized (messageMonitor) {
			return message;
		}
	}
	
	public static class MissingDataException extends RuntimeException {
		private static final long serialVersionUID = -7046121332447383903L;
		public MissingDataException(String message) {
			super(message);
		}
	}

	private static String[] modFolders = new String[0]; 
			
	private static DataTile[] tiles = new DataTile[0];
	private static DataBuilding[] buildings = new DataBuilding[0];
	private static DataUnit[] units = new DataUnit[0];
	private static DataWeapon[] weapons = new DataWeapon[0];
	private static DataMap[] maps = new DataMap[0];
	private static DataLevel[] levels = new DataLevel[0];

	public static DataTile getTile(String name) {
		for (DataTile t : tiles) {
			if (t.name.equals(name)) return t;
		}
		System.out.printf("[Data] No tile with the name '%s'.\n", name);
		return null;
	}
	
	public static DataBuilding getBuilding(String name) {
		for (DataBuilding b : buildings) {
			if (b.name.equals(name)) return b;
		}
		System.out.printf("[Data] No building with the name '%s'.\n", name);
		return null;
	}
	
	public static DataUnit getUnit(String name) {
		for (DataUnit u : units) {
			if (u.name.equals(name)) return u;
		}
		System.out.printf("[Data] No unit with the name '%s'.\n", name);
		new Exception().printStackTrace();
		return null;
	}
	
	public static DataWeapon getWeapon(String name) {
		for (DataWeapon w : weapons) {
			if (w.name.equals(name)) return w;
		}
		System.out.printf("[Data] No weapon with the name '%s'.\n", name);
		return null;
	}
	
	public static DataMap getMap(String name) {
		for (DataMap m : maps) {
			if (m.name.equals(name)) return m;
		}
		System.out.printf("[Data] No map with the name '%s'.\n", name);
		return null;
	}
	
	public static DataLevel getLevel(String name) {
		for (DataLevel l : levels) {
			if (l.name.equals(name)) return l;
		}
		System.out.printf("[Data] No level with the name '%s'.\n", name);
		return null;
	}
	
	public static void load() {
		Thread dataLoader = new Thread() {
			@Override
			public void run() {
				setFinished(false);
				findModFolders();
				loadTiles();
				loadBuildings();
				loadWeapons();
				loadUnits();
				loadMaps();
				loadLevels();
				setMessage("");
				setFinished(true);
			}
		};
		dataLoader.start();
	}

	public static String[] getResources(String folderName) {
		ArrayList<String> resourceList = new ArrayList<String>();
		for (String modFolder : modFolders) {
			String[] folder = jog.Filesystem.enumerate(modFolder + "/" + folderName);
			resourceList.addAll(Arrays.asList(folder));
		}
		return resourceList.toArray(new String[0]);
	}
	
	public static String getResource(String modPath) {
		String resourcePath = null;
		for (String modFolder : modFolders) {
			File resource = jog.Filesystem.getFile("mods/" + modFolder + "/resources/" + modPath); 
			if (resource.exists()) resourcePath = "mods/" + modFolder + "/resources/" + modPath;
		}
		if (resourcePath == null) throw new RuntimeException("No path to file: " + modPath);
		return resourcePath;
	}
	
	private static void findModFolders() {
		modFolders = jog.Filesystem.readFile("mods/.loadorder").split("\n");
		ArrayList<String> modNames = new ArrayList<String>(Arrays.asList(modFolders));
		for (String mod : modFolders) {
			if (!jog.Filesystem.getFile("mods/" + mod).exists()) {
				System.err.printf("[Data] WARNING: Mod '%s' wasn't found.\n", mod);
				modNames.remove(mod);
			}
		}
		modFolders = modNames.toArray(modFolders);
	}
	
	private static void loadTiles() {
		setMessage("Loading Tiles...");
		ArrayList<DataTile> dataTiles = new ArrayList<DataTile>();
		for (String mod : modFolders) {
			final String path = "mods/" + mod + "/data/tile";
			for (String filename : jog.Filesystem.enumerate(path)) {
				try {
					DataTile t = readTile(path + "/" + filename);
					dataTiles.add(t);
					System.out.println("[Data] Loaded " + filename);
				} catch (Exception e) {
					System.err.printf("Could not read tile '%s'.\n", filename);
					e.printStackTrace();
				}
			}
		}
		tiles = dataTiles.toArray(tiles);
	}
	
	private static void loadBuildings() {
		setMessage("Loading Buildings...");
		ArrayList<DataBuilding> dataBuildings = new ArrayList<DataBuilding>();
		for (String mod : modFolders) {
			final String path = "mods/" + mod + "/data/building";
			for (String filename : jog.Filesystem.enumerate(path)) {
				try {
					DataBuilding t = readBuilding(path + "/" + filename);
					dataBuildings.add(t);
					System.out.println("[Data] Loaded " + filename);
				} catch (Exception e) {
					System.err.printf("Could not read building '%s'.\n", filename);
					e.printStackTrace();
				}
			}
		}
		buildings = dataBuildings.toArray(buildings);
	}
	
	private static void loadWeapons() {
		setMessage("Loading Weapons...");
		ArrayList<DataWeapon> dataWeapons = new ArrayList<DataWeapon>();
		for (String mod : modFolders) {
			final String path = "mods/" + mod + "/data/weapon";
			for (String filename : jog.Filesystem.enumerate(path)) {
				try {
					DataWeapon u = readWeapon(path + "/" + filename);
					dataWeapons.add(u);
					System.out.println("[Data] Loaded " + filename);
				} catch (Exception e) {
					System.err.printf("Could not read weapon '%s'.\n", filename);
					e.printStackTrace();
				}
			}
		}
		weapons = dataWeapons.toArray(weapons);
	}
	
	private static void loadUnits() {
		setMessage("Loading Units...");
		ArrayList<DataUnit> dataActors = new ArrayList<DataUnit>();
		for (String mod : modFolders) {
			final String path = "mods/" + mod + "/data/unit";
			for (String filename : jog.Filesystem.enumerate(path)) {
				try {
					DataUnit u = readUnit(path + "/" + filename);
					dataActors.add(u);
					System.out.println("[Data] Loaded " + filename);
				} catch (Exception e) {
					System.err.printf("Could not read unit '%s'.\n", filename);
					e.printStackTrace();
				}
			}
		}
		units = dataActors.toArray(units);
	}
	
	private static void loadMaps() {
		setMessage("Loading Maps...");
		ArrayList<DataMap> dataMaps = new ArrayList<DataMap>();
		for (String mod : modFolders) {
			final String path = "mods/" + mod + "/data/map";
			for (String filename : jog.Filesystem.enumerate(path)) {
				try {
					DataMap m = readMap(path + "/" + filename);
					dataMaps.add(m);
					System.out.println("[Data] Loaded " + filename);
				} catch (Exception e) {
					System.err.printf("Could not read map '%s'.\n", filename);
					e.printStackTrace();
				}
			}
		}
		maps = dataMaps.toArray(maps);
	}
	
	private static void loadLevels() {
		setMessage("Loading Levels...");
		ArrayList<DataLevel> dataLevels = new ArrayList<DataLevel>();
		for (String mod : modFolders) {
			final String path = "mods/" + mod + "/data/level";
			for (String filename : jog.Filesystem.enumerate(path)) {
				try {
					DataLevel l = readLevel(path + "/" + filename);
					dataLevels.add(l);
					System.out.println("[Data] Loaded " + filename);
				} catch (Exception e) {
					System.err.printf("Could not read level '%s'.\n", filename);
					e.printStackTrace();
				}
			}
		}
		levels = dataLevels.toArray(levels);
	}
	
	private static DataTile readTile(String filename) {
		String[] tileData = jog.Filesystem.readFile(filename).split("\n");
		String name = tileData[0];
		
		String[] visionData = tileData[1].split(" ");
		int visionCost = Integer.valueOf(visionData[0]);
		int visionBonus = Integer.valueOf(visionData[1]);
		
		String[] defenceData = tileData[2].split(" ");
		int defaultDefenceBonus = Integer.valueOf(defenceData[0]);
		HashMap<Unit.Movement, Integer> defenceBonuses = new HashMap<Unit.Movement, Integer>();
		for (int i = 1; i < defenceData.length - 1; i += 2) {
			Unit.Movement move = Unit.Movement.valueOf(defenceData[i]);
			int bonus = Integer.valueOf(defenceData[i + 1]);
			defenceBonuses.put(move, bonus);
		}
		
		String[] movementData = tileData[3].split(" ");
		int defaultMovementCost = Integer.valueOf(movementData[0]);
		HashMap<Unit.Movement, Integer> movementCosts = new HashMap<Unit.Movement, Integer>();
		for (int i = 1; i < movementData.length - 1; i += 2) {
			Unit.Movement move = Unit.Movement.valueOf(movementData[i]);
			Integer cost = Integer.valueOf(movementData[i + 1]);
			movementCosts.put(move, cost);
		}
		
		boolean isAnimated = !tileData[4].matches("\\d \\d \\d \\d");
		lib.Animation animation = null;
		java.awt.Rectangle tileQuad = null;
		if (isAnimated) {
			// TODO load animation
		} else {
			String[] rectangleData = tileData[4].split(" ");
			int x = Integer.valueOf(rectangleData[0]) * DataTile.TILE_SIZE;
			int y = Integer.valueOf(rectangleData[1]) * DataTile.TILE_SIZE;
			int w = Integer.valueOf(rectangleData[2]) * DataTile.TILE_SIZE;
			int h = Integer.valueOf(rectangleData[3]) * DataTile.TILE_SIZE;
			tileQuad = new Rectangle(x, y, w, h);
		}
		
		return new DataTile(name, visionCost, visionBonus, 
				            defaultDefenceBonus, defenceBonuses, 
				            defaultMovementCost, movementCosts,
				            isAnimated, animation, tileQuad);
	}
	
	private static DataBuilding readBuilding(String filename) {
		String[] buildingData = jog.Filesystem.readFile(filename).split("\n");
		String name = buildingData[0];
		
		String[] validTiles = buildingData[1].split(" ");
		String[] requiredBuildingNames = buildingData[2].split(" ");
		
		String[] costData = buildingData[3].split(" ");
		int turnsToBuild = Integer.valueOf(costData[0]);
		int costToBuild = Integer.valueOf(costData[1]);
		
		String[] captureData = buildingData[4].split(" ");
		int influenceToCapture = Integer.valueOf(captureData[0]);
		int turnsToCapture = Integer.valueOf(captureData[1]);
		
		String[] supplyData = buildingData[5].split(" ");
		int moneyProvided = Integer.valueOf(supplyData[0]);
		Unit.Kind[] resupplies = new Unit.Kind[supplyData.length - 1];
		for (int i = 0; i < resupplies.length; i ++) {
			resupplies[i] = Unit.Kind.valueOf(supplyData[i+1]);
		}
		
		String[] healingData = buildingData[6].split(" ");
		HashMap<Unit.Kind, Integer> healthRestoration = new HashMap<Unit.Kind, Integer>();
		for (int i = 0; i < healingData.length - 1; i += 2) {
			Unit.Kind unit = Unit.Kind.valueOf(healingData[i]);
			int healing = Integer.valueOf(healingData[i+1]);
			healthRestoration.put(unit, healing);
		}
		
		String[] storageData = buildingData[7].split(" ");
		int totalSpace = Integer.valueOf(storageData[0]);
		HashMap<Unit.Kind, Integer> unitStorage = new HashMap<Unit.Kind, Integer>();
		for (int i = 1; i < storageData.length - 1; i += 2) {
			Unit.Kind unit = Unit.Kind.valueOf(storageData[i]);
			int size = Integer.valueOf(storageData[i + 1]);
			unitStorage.put(unit, size);
		}
		
		String[] spriteData = buildingData[8].split(" ");
		String spriteFilename = spriteData[0];
		int spritePoses = Integer.valueOf(spriteData[1]);
		int spriteFrames = Integer.valueOf(spriteData[2]);
		double spriteDelay = Double.valueOf(spriteData[3]);
		DataSprite sprite = new DataSprite(spriteFilename, spritePoses, spriteFrames, spriteDelay);
		
		String[] makableUnitNames = buildingData[9].split(" ");
		
		return new DataBuilding(name, validTiles, requiredBuildingNames, 
				                turnsToBuild, costToBuild, 
				                influenceToCapture, turnsToCapture, 
				                moneyProvided, resupplies, healthRestoration, 
				                totalSpace, unitStorage, 
				                sprite, makableUnitNames);
	}
	
	
	private static DataWeapon readWeapon(String filename) {
		String[] weaponData = jog.Filesystem.readFile(filename).split("\n");
		String name = weaponData[0];
		
		String[] rangeData = weaponData[1].split(" ");
		int minRange = Integer.valueOf(rangeData[0]);
		int maxRange = Integer.valueOf(rangeData[1]);
		
		int damage = Integer.valueOf(weaponData[2]);
		
		String[] ammoData = weaponData[3].split(" ");
		int startingAmmo = Integer.valueOf(ammoData[0]);
		int maxAmmo = Integer.valueOf(ammoData[1]);
		
		String[] unitData = weaponData[4].split(" ");
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
		
		String[] spriteData = weaponData[5].split(" ");
		String spriteFilename = spriteData[0];
		int spritePoses = Integer.valueOf(spriteData[1]);
		int spriteFrames = Integer.valueOf(spriteData[2]);
		double spriteDelay = Double.valueOf(spriteData[3]);
		DataSprite sprite = new DataSprite(spriteFilename, spritePoses, spriteFrames, spriteDelay);
		
		return new DataWeapon(name, minRange, maxRange, damage, startingAmmo, maxAmmo, unitsCanAttack, unitsCannotAttack, sprite);
	}
	
	private static DataUnit readUnit(String filename) {
		String[] unitData = jog.Filesystem.readFile(filename).split("\n");
		String name = unitData[0];
		Kind kind = Kind.valueOf(unitData[1]);
		
		String[] movementData = unitData[2].split(" ");
		Movement movement = Movement.valueOf(movementData[0]);
		int movementDistance = Integer.valueOf(movementData[1]);
		
		String[] costData = unitData[3].split(" ");
		int populationCost = Integer.valueOf(costData[0]);
		int turnsToMake = Integer.valueOf(costData[1]);
		int costToMake = Integer.valueOf(costData[2]);
		
		String[] fuelData = unitData[4].split(" ");
		int startingFuel = Integer.valueOf(fuelData[0]);
		int maxFuel = Integer.valueOf(fuelData[1]);
		int fuelCost = Integer.valueOf(fuelData[2]);
		boolean usesFuelIfNotMoved = Boolean.valueOf(fuelData[3]);
		boolean diesOnFuelDepletion = Boolean.valueOf(fuelData[4]);
		
		String[] miscData = unitData[5].split(" ");
		int vision = Integer.valueOf(miscData[0]);
		int defence = Integer.valueOf(miscData[1]);
		int influence = Integer.valueOf(miscData[2]);
		boolean canMoveAndAttack = Boolean.valueOf(miscData[3]);
		boolean canBuild = Boolean.valueOf(miscData[4]);
		
		String[] spriteData = unitData[6].split(" ");
		String spriteFilename = spriteData[0];
		int spritePoses = Integer.valueOf(spriteData[1]);
		int spriteFrames = Integer.valueOf(spriteData[2]);
		double spriteDelay = Double.valueOf(spriteData[3]);
		DataSprite sprite = new DataSprite(spriteFilename, spritePoses, spriteFrames, spriteDelay);
		
		String[] iconData = unitData[7].split(" ");
		String iconFilename = iconData[0];
		int iconPoses = Integer.valueOf(iconData[1]);
		int iconFrames = Integer.valueOf(iconData[2]);
		double iconDelay = Double.valueOf(iconData[3]);
		DataSprite icon = new DataSprite(iconFilename, iconPoses, iconFrames, iconDelay);
		
		String[] weaponNames = new String[0];
		if (!unitData[8].isEmpty()) {
			weaponNames = unitData[8].split(" ");
		}
		
		String storageData[] = unitData[9].split(" ");
		int totalStorage = Integer.valueOf(storageData[0]);
		HashMap<Unit.Kind, Integer> unitStorageSizes = new HashMap<Unit.Kind, Integer>();
		for (int i = 1; i < storageData.length - 1; i += 2) {
			Unit.Kind unit = Unit.Kind.valueOf(storageData[i]);
			int size = Integer.valueOf(storageData[i + 1]);
			unitStorageSizes.put(unit, size);
		}
		
		String[] buildingNames = new String[0];
		if (unitData.length > 10 && !unitData[10].isEmpty()) {
			buildingNames = unitData[9].split(" ");
		}
		
		return new DataUnit(name, kind, movement, movementDistance, 
							populationCost, turnsToMake, costToMake,
							fuelCost, startingFuel, maxFuel, usesFuelIfNotMoved, diesOnFuelDepletion,
						    vision, defence, influence, canMoveAndAttack, canBuild, 
						    sprite, icon, weaponNames,
						    totalStorage, unitStorageSizes, buildingNames);
	}
	
	private static DataMap readMap(String filename) {
		String[] mapData = jog.Filesystem.readFile(filename).split("\n");
		String name = mapData[0];
		
		String[] keyData = mapData[1].split(" "); 
		HashMap<Character, DataTile> key = new HashMap<Character, DataTile>();
		for (int i = 0; i < keyData.length - 1; i += 2) {
			char character = Character.valueOf(keyData[i].charAt(0));
			DataTile tile = Data.getTile(keyData[i+1]);
			key.put(character, tile);
		}
		
		DataTile[][] tiles = new DataTile[mapData.length - 2][];
		for (int j = 0; j < tiles.length; j ++) {
			char[] mapRow = mapData[j+2].toCharArray();
			tiles[j] = new DataTile[mapRow.length];
			for (int i = 0; i < tiles[j].length; i ++) {
				tiles[j][i] = key.get(mapRow[i]);
			}
		}
		
		return new DataMap(name, tiles);
	}
	
	private static DataLevel readLevel(String filename) {
		String[] levelData = jog.Filesystem.readFile(filename).split("\n");
		String name = levelData[0];
		
		String[] mapData = levelData[1].split(" ");
		DataMap dataMap = Data.getMap(mapData[0]);
		boolean fogOfWar = Boolean.valueOf(mapData[1]);
		int populationCount = Integer.valueOf(mapData[2]);
		
		String[] playerData = levelData[2].split(" ");
		int playerCount = Integer.valueOf(playerData[0]);
		Faction[] factions = new Faction[playerData.length - 1];
		for (int i = 1; i < playerData.length; i ++) {
			factions[i-1] = Faction.valueOf(playerData[i]);
		}
		
		String[][] startingUnits;
		int[][][] unitStartingPositions;
		if (levelData.length > 3) {
			startingUnits = new String[playerCount][];
			unitStartingPositions = new int[playerCount][][];
			for (int n = 0; n < playerCount; n ++) {
				String[] unitData = levelData[3 + n].split(" ");
				startingUnits[n] = new String[unitData.length / 3];
				unitStartingPositions[n] = new int[unitData.length / 3][2];
				for (int i = 0; i < unitData.length - 2; i += 3) {
					startingUnits[n][i / 3] = unitData[i];
					unitStartingPositions[n][i / 3][0] = Integer.valueOf(unitData[i+1]);
					unitStartingPositions[n][i / 3][1] = Integer.valueOf(unitData[i+2]);
				}
			}
		} else {
			startingUnits = new String[0][0];
			unitStartingPositions = new int[0][0][0];
		}
		
		String[] startingBuildings;
		int[][] buildingStartingPositions;
		int[] buildingStartingOwners; 
		if (levelData.length > 3 + playerCount) {
			String[] buildingData = levelData[3 + playerCount].split(" ");
			startingBuildings = new String[buildingData.length];
			buildingStartingPositions = new int[buildingData.length][2];
			buildingStartingOwners = new int[buildingData.length];
			for (int i = 0; i < buildingData.length - 3; i += 4) {
				startingBuildings[i] = buildingData[i];
				buildingStartingOwners[i] = Integer.valueOf(buildingData[i+1]);
				buildingStartingPositions[i][0] = Integer.valueOf(buildingData[i+2]);
				buildingStartingPositions[i][1] = Integer.valueOf(buildingData[i+3]);
			}
		} else {
			startingBuildings = new String[0];
			buildingStartingPositions = new int[0][0];
			buildingStartingOwners = new int[0];
		}
		
		return new DataLevel(name, dataMap, fogOfWar, playerCount, factions, populationCount, startingUnits, unitStartingPositions, startingBuildings, buildingStartingPositions, buildingStartingOwners);
	}

}
