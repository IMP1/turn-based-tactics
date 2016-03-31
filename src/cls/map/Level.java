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
		int max = u.getMoveDistance();
		boolean [][] tiles = new boolean[map.getHeight()][map.getWidth()];
		
		addAvailableMovement(u, u.getX(), u.getY() - 1, max, tiles);
		addAvailableMovement(u, u.getX() - 1, u.getY(), max, tiles);
		addAvailableMovement(u, u.getX(), u.getY() + 1, max, tiles);
		addAvailableMovement(u, u.getX() + 1, u.getY(), max, tiles);
		
		return tiles;
	}
	
	private void addAvailableMovement(Unit u, int x, int y, int distanceRemaining, boolean[][] moveable) {
		if (distanceRemaining == 0) return;
		if (map.getTileAt(x, y) == null) return;
		setMoveableTo(x, y, moveable);
		
		if (map.getTileAt(x, y - 1) != null) {
			int costUp = map.getTileAt(x, y - 1).getMovementCost(u); 
			if (costUp <= distanceRemaining) {
				addAvailableMovement(u, x, y - 1, distanceRemaining - costUp, moveable);
			}
		}
		if (map.getTileAt(x - 1, y) != null) {
			int costLeft = map.getTileAt(x - 1, y).getMovementCost(u);  
			if (costLeft <= distanceRemaining) {
				addAvailableMovement(u, x - 1, y, distanceRemaining - costLeft, moveable);
			}
		}
		if (map.getTileAt(x, y + 1) != null) {
			int costDown = map.getTileAt(x, y + 1).getMovementCost(u); 
			if (costDown <= distanceRemaining) {
				addAvailableMovement(u, x, y + 1, distanceRemaining - costDown, moveable);
			}
		}
		if (map.getTileAt(x + 1, y) != null) {
			int costRight = map.getTileAt(x + 1, y).getMovementCost(u); 
			if (costRight <= distanceRemaining) {
				addAvailableMovement(u, x + 1, y, distanceRemaining - costRight, moveable);
			}
		}
	}
	
	private void setMoveableTo(int x, int y, boolean[][] moveable) {
		if (y < 0 || y > moveable.length) return;
		if (x < 0 || x > moveable[y].length) return;
		moveable[y][x] = true;
	}
	
	public boolean[][] calculateUnitAttacks(Unit u) {
		return new boolean[0][0];
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
