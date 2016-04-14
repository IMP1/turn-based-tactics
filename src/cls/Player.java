package cls;

import java.util.ArrayList;

import cls.map.Map;
import cls.unit.Unit;

public class Player {
	
	public enum Faction {
		BLUE_SKY_CORPS,
		DELTA_CONFEDERATION,
		NIGHT_WALKERS,
	}

	private static boolean fogOfWarEnabled;

	public static void setFogOfWar(boolean fogOfWar) {
		fogOfWarEnabled = fogOfWar;
	}
		
	public final Faction faction;
	private ArrayList<Unit> units;
	private boolean[][] visibility;
	private boolean finishedTurn;
	
	public boolean isTurn() { return !finishedTurn; }
	
	public boolean hasFinishedTurn() { return finishedTurn;	}
	
	public boolean[][] getVisibleTiles() { return visibility; }
	
	public boolean canSee(int i, int j) {
		if (!fogOfWarEnabled) return true;
		if (j < 0 || j >= visibility.length) return false;
		if (i < 0 || i >= visibility[j].length) return false;
		return visibility[j][i];
	}
	
	public Player(Faction faction, Map map) {
		this.faction = faction;
		units = new ArrayList<Unit>();
		finishedTurn = true;
		visibility = new boolean[map.getHeight()][map.getWidth()];
		updateVisibility(map);
	}
	
	public void beginTurn() {
		finishedTurn = false;
		for (Unit u : units) {
			u.refresh();
		}
	}
	
	public Unit getUnitAt(int i, int j) {
		for (Unit u : units) {
			if (u.isAt(i, j) && !u.isStored()) return u;
		}
		return null;
	}
	
	public void endTurn() {
		finishedTurn = true;
	}
	
	public void updateUnits(double dt) {
		for (Unit u : units) {
			u.update(dt);
		}
	}
	
	public void drawUnits() {
		for (Unit u : units) {
			if (!u.isStored()) u.draw();
		}
	}
	
	public void updateVisibility(Map map) {
		if (!fogOfWarEnabled) return;
		int mapHeight = visibility.length;
		int mapWidth = visibility[0].length;
		visibility = new boolean[mapWidth][mapHeight];
		for (Unit u : units) {
			if (u.isStored()) continue;
			setVisible(u.getX(), u.getY(), visibility);
			updateUnitVisibility(map, u.getX(), u.getY() - 1, u.getVisionDistance());
			updateUnitVisibility(map, u.getX() - 1, u.getY(), u.getVisionDistance());
			updateUnitVisibility(map, u.getX(), u.getY() + 1, u.getVisionDistance());
			updateUnitVisibility(map, u.getX() + 1, u.getY(), u.getVisionDistance());
		}
	}
	
	private void updateUnitVisibility(Map map, int x, int y, int distanceRemaining) {
		if (distanceRemaining == 0) return;
		if (map.getTileAt(x, y) == null) return;
		setVisible(x, y, visibility);
		
		if (map.getTileAt(x, y - 1) != null) {
			int costUp = map.getTileAt(x, y - 1).getVisionCost(); 
			if (costUp <= distanceRemaining) {
				updateUnitVisibility(map, x, y - 1, distanceRemaining - costUp);
			}
		}
		if (map.getTileAt(x - 1, y) != null) {
			int costLeft = map.getTileAt(x - 1, y).getVisionCost(); 
			if (costLeft <= distanceRemaining) {
				updateUnitVisibility(map, x - 1, y, distanceRemaining - costLeft);
			}
		}
		if (map.getTileAt(x, y + 1) != null) {
			int costDown = map.getTileAt(x, y + 1).getVisionCost(); 
			if (costDown <= distanceRemaining) {
				updateUnitVisibility(map, x, y + 1, distanceRemaining - costDown);
			}
		}
		if (map.getTileAt(x + 1, y) != null) {
			int costRight = map.getTileAt(x + 1, y).getVisionCost(); 
			if (costRight <= distanceRemaining) {
				updateUnitVisibility(map, x + 1, y, distanceRemaining - costRight);
			}
		}
		
	}
	
	private void setVisible(int i, int j, boolean[][] map) {
		if (j < 0 || j >= map.length) return;
		if (i < 0 || i >= map[j].length) return;
		map[j][i] = true;
	}

	public void addUnit(Unit newUnit) {
		units.add(newUnit);
	}

}
