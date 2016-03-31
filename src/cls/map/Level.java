package cls.map;

import java.util.ArrayList;

import cls.Player;
import cls.building.Building;
import cls.unit.DataUnit;
import cls.unit.Unit;

public class Level {

	private DataLevel data;

	private Player[] players;
	private ArrayList<Building> buildings;
	private int currentPlayer;
	private Map map;
	
	public Building getBuildingAt(int i, int j) {
		return null; // TODO do
	}
	
	public Map getMap() {
		return map;
	}
	
	public Level(DataLevel data) {
		this.data = data;
		map = data.mapData.newMap();
		players = new Player[data.playerCount];
		for (int i = 0; i < players.length; i ++) {
			players[i] = new Player(data.factions[i], map);
		}
	}
	
	public void begin() {
		createInitialUnits();
		createInitialBuildings();
		for (Player p : players) p.updateVisibility(map);
		currentPlayer = -1;
		nextTurn();
	}
	
	private void createInitialUnits() {
		int count = 0;
		for (int player = 0; player < data.playerCount; player ++) {
			for (int i = 0; i < data.units[player].length; i ++) {
				if (data.units[player][i] != null) {
					int x = data.unitStartingPositions[player][i][0];
					int y = data.unitStartingPositions[player][i][1];
					DataUnit u = data.units[player][i];
					players[player].addUnit(u.newUnit(players[player], x, y));
					count ++;
				}
			}
		}
		System.out.printf("[Level] Initialised %d units.\n", count);
	}
	
	private void createInitialBuildings() {
		buildings = new ArrayList<Building>();
		for (int i = 0; i < data.buildings.length; i ++) {
			int playerId = data.buildingStartingOwners[i];
			Player owner = players[playerId];
			int x = data.buildingStartingPositions[i][0];
			int y = data.buildingStartingPositions[i][0];
			buildings.add(data.buildings[i].newBuilding(owner, x, y));
		}
	}
	
	public void setCurrentPlayer(int player) {
		currentPlayer = player % players.length;
	}
	
	public Player getCurrentPlayer() {
		return players[currentPlayer];
	}
	
	public int getCurrentPlayerIndex() {
		return currentPlayer;
	}

	public boolean hasFogOfWar() {
		return data.fogOfWar;
	}

	public Unit getUnitAt(int i, int j) {
		for (Player p : players) {
			Unit u = p.getUnitAt(i, j);
			if (u != null) return u;
		}
		return null;
	}

	public void nextTurn() {
		currentPlayer ++;
		currentPlayer %= players.length;
		players[currentPlayer].beginTurn();
	}

	public boolean[][] getVisibleTiles() {
		return players[currentPlayer].getVisibleTiles();
	}
	
	public boolean[][] calculateUnitMoves(Unit u) {
		if (!u.canMove()) return new boolean[0][0];
		int min = 0;
		int max = u.getMoveDistance();
		return getAllTilesWithinMovementRange(u.getX(), u.getY(), min, max);
	}
	
	private boolean[][] getAllTilesWithinMovementRange(int x, int y, int minDistance, int maxDistance) {
		// TODO make it take terrain into account
		boolean [][] tiles = new boolean[map.getHeight()][map.getWidth()];
		for (int j = 0; j < tiles.length; j ++) {
			for (int i = 0; i < tiles[j].length; i ++) {
				int d = Math.abs(i - x) + Math.abs(j - y);
				if (d >= minDistance && d <= maxDistance) tiles[j][i] = true;
			}
		}
		return tiles;
	}
	
	public boolean[][] calculateUnitAttacks(Unit u) {
		int min = u.getMinimumRange();
		int max = u.getMaximumRange();
		if (u.canMoveAndAttack() && u.canMove()) {
			min += u.getMoveDistance();
			max += u.getMoveDistance();
		}
		// TODO write proper attack range method that take unit's weapon and whether it can move and attack
		return getAllTilesWithinMovementRange(u.getX(), u.getY(), min, max);
	}
	
	public void update(double dt) {
		for (Player p : players) {
			p.updateUnits(dt);
		}
	}
	
	public void drawBuildings() {
		for (Building b : buildings) {
			b.draw();
		}
	}
	
	public void drawUnits() {
		for (Player p : players) {
			p.drawUnits();
		}
	}

}
