package cls;

import java.util.ArrayList;

import cls.unit.Unit;

public class Player {

	private ArrayList<Unit> units;
	private boolean[][] visibility;
	private boolean finishedTurn;
	
	public boolean isTurn() { return !finishedTurn; }
	
	public boolean hasFinishedTurn() { return finishedTurn;	}
	
	public boolean[][] getVisibleTiles() { return visibility; }
	
	public Player(int mapWidth, int mapHeight) {
		units = new ArrayList<Unit>();
		finishedTurn = true;
		visibility = new boolean[mapHeight][mapWidth];
		updateVisibility();
	}
	
	public void beginTurn() {
		finishedTurn = false;
		for (Unit u : units) {
			u.refresh();
		}
	}
	
	public Unit getUnitAt(int i, int j) {
		for (Unit u : units) {
			if (u.isAt(i, j)) return u;
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
			u.draw();
		}
	}
	
	public void updateVisibility() {
		int mapHeight = visibility.length;
		int mapWidth = visibility[0].length;
		visibility = new boolean[mapWidth][mapHeight];
		for (Unit u : units) {
			int dist = u.getVisionDistance();
			int x1 = u.getX() - dist;
			int x2 = u.getX() + dist;
			for (int i = x1; i <= x2; i ++) {
				int d = dist - Math.abs(i - u.getX());
				int y1 = u.getY() - d;
				int y2 = u.getY() + d;
				for (int j = y1; j <= y2; j ++) {
					setVisible(i, j, visibility);
				}
			}
		}
	}
	
	private void setVisible(int i, int j, boolean[][] map) {
		if (j < 0 || j >= map.length) return;
		if (i < 0 || i >= map[j].length) return;
		map[j][i] = true;
	}

	public void addUnit(Unit newUnit) {
		System.out.println("Adding new unit " + newUnit);
		units.add(newUnit);
	}
	
}
