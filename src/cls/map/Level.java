package cls.map;

import java.util.ArrayList;
import java.util.Arrays;

import cls.Player;
import cls.building.Building;
import cls.unit.DataUnit;
import cls.unit.Unit;

public class Level {

	private DataLevel data;

	private Player[] players;
	private ArrayList<Building> buildings;
	private int currentPlayer;
	private int turnCount;
	private Map map;
	
	public Building getBuildingAt(int i, int j) {
		for (Building b : buildings) {
			if (b.getX() == i && b.getY() == j) return b;
		}
		return null;
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
		Player.setFogOfWar(data.fogOfWar);
	}
	
	public void begin() {
		createInitialUnits();
		createInitialBuildings();
		for (Player p : players) p.updateVisibility(map);
		currentPlayer = -1;
		turnCount = -1;
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

	public boolean areAllies(Player p1, Player p2) {
		return p1.equals(p2);
	}
	
	public boolean areEnemies(Player p1, Player p2) {
		return !p1.equals(p2);
	}
	
	public boolean hasFogOfWar() {
		return data.fogOfWar;
	}

	public int getTurnCount() {
		return turnCount;
	}
	
	public int getTurn() {
		return 1 + getTurnCount() / players.length;
	}
	
	public Unit getUnitAt(int i, int j) {
		for (Player p : players) {
			Unit u = p.getUnitAt(i, j);
			if (u != null) return u;
		}
		return null;
	}

	public void nextTurn() {
		turnCount ++;
		currentPlayer ++;
		currentPlayer %= players.length;
		players[currentPlayer].beginTurn();
	}

	public boolean[][] getVisibleTiles() {
		return players[currentPlayer].getVisibleTiles();
	}
	
	public int[] getPathTo(final Unit u, final int targetX, final int targetY, final int[] previousPath, final int distanceRemaining) {
		final int currentX = previousPath[previousPath.length-2];
		final int currentY = previousPath[previousPath.length-1];
		if (currentX == targetX && currentY == targetY)
		    return previousPath;
		if (distanceRemaining == 0) 
			return null;
		
		int[] pathUp = null;
		if (map.getTileAt(currentX, currentY - 1) != null) {
			int costUp = map.getTileAt(currentX, currentY - 1).getMovementCost(u);
			if (costUp <= distanceRemaining) {
				int[] newPath = Arrays.copyOf(previousPath, previousPath.length + 2);
				newPath[newPath.length - 2] = currentX;
				newPath[newPath.length - 1] = currentY - 1;
				pathUp = getPathTo(u, targetX, targetY, newPath, distanceRemaining - costUp);
			}
		}
		int[] pathLeft = null;
		if (map.getTileAt(currentX - 1, currentY) != null) {
			int costLeft = map.getTileAt(currentX - 1, currentY).getMovementCost(u);
			if (costLeft <= distanceRemaining) {
				int[] newPath = Arrays.copyOf(previousPath, previousPath.length + 2);
				newPath[newPath.length - 2] = currentX - 1;
				newPath[newPath.length - 1] = currentY;
				pathLeft = getPathTo(u, targetX, targetY, newPath, distanceRemaining - costLeft);
			}
		}
		int[] pathDown = null;
		if (map.getTileAt(currentX, currentY + 1) != null) {
			int costDown = map.getTileAt(currentX, currentY + 1).getMovementCost(u);
			if (costDown <= distanceRemaining) {
				int[] newPath = Arrays.copyOf(previousPath, previousPath.length + 2);
				newPath[newPath.length - 2] = currentX;
				newPath[newPath.length - 1] = currentY + 1;
				pathDown = getPathTo(u, targetX, targetY, newPath, distanceRemaining - costDown);
			}
		}
		int[] pathRight = null;
		if (map.getTileAt(currentX + 1, currentY) != null) {
			int costRight = map.getTileAt(currentX + 1, currentY).getMovementCost(u);
			if (costRight <= distanceRemaining) {
				int[] newPath = Arrays.copyOf(previousPath, previousPath.length + 2);
				newPath[newPath.length - 2] = currentX + 1;
				newPath[newPath.length - 1] = currentY;
				pathRight = getPathTo(u, targetX, targetY, newPath, distanceRemaining - costRight);
			}
		}
		
		return getShortestPath(pathUp, pathLeft, pathRight, pathDown);
	}
	
	private int[] getShortestPath(int[]... paths) {
		int shortestPathLength = -1;
		int[] shortestPath = null;
		for (int[] path : paths) {
			if (path != null && (shortestPathLength == -1 || path.length < shortestPathLength)) {
				shortestPath = path;
				shortestPathLength = path.length;
			}
		}
		return shortestPath;
	}
	
	public boolean[][] calculateUnitMoves(Unit u) {
		if (!u.canMove()) return new boolean[0][0];
		boolean[][] tiles = new boolean[map.getHeight()][map.getWidth()];
		
		int max = u.getMoveDistance();
		addAvailableMovement(u, u.getX(), u.getY() - 1, max, tiles);
		addAvailableMovement(u, u.getX() - 1, u.getY(), max, tiles);
		addAvailableMovement(u, u.getX(), u.getY() + 1, max, tiles);
		addAvailableMovement(u, u.getX() + 1, u.getY(), max, tiles);
		
		return tiles;
	}
	
	private void addAvailableMovement(Unit unit, int x, int y, int distanceRemaining, boolean[][] moveable) {
		if (distanceRemaining == 0) return;
		if (map.getTileAt(x, y) == null) return;
		if (unit.getOwner().canSee(x, y)) {
			Building b = getBuildingAt(x, y); 
			if (b != null && b.getOwner() != unit.getOwner()) return;
			Unit u = getUnitAt(x, y);
			if (u != null && u.getOwner() != unit.getOwner()) return;
		}
		
		setMoveableTo(x, y, moveable);
		if (map.getTileAt(x, y - 1) != null) {
			int costUp = map.getTileAt(x, y - 1).getMovementCost(unit); 
			if (costUp <= distanceRemaining) {
				addAvailableMovement(unit, x, y - 1, distanceRemaining - costUp, moveable);
			}
		}
		if (map.getTileAt(x - 1, y) != null) {
			int costLeft = map.getTileAt(x - 1, y).getMovementCost(unit);  
			if (costLeft <= distanceRemaining) {
				addAvailableMovement(unit, x - 1, y, distanceRemaining - costLeft, moveable);
			}
		}
		if (map.getTileAt(x, y + 1) != null) {
			int costDown = map.getTileAt(x, y + 1).getMovementCost(unit); 
			if (costDown <= distanceRemaining) {
				addAvailableMovement(unit, x, y + 1, distanceRemaining - costDown, moveable);
			}
		}
		if (map.getTileAt(x + 1, y) != null) {
			int costRight = map.getTileAt(x + 1, y).getMovementCost(unit); 
			if (costRight <= distanceRemaining) {
				addAvailableMovement(unit, x + 1, y, distanceRemaining - costRight, moveable);
			}
		}
	}
	
	private void setMoveableTo(int x, int y, boolean[][] moveable) {
		if (y < 0 || y > moveable.length) return;
		if (x < 0 || x > moveable[y].length) return;
		moveable[y][x] = true;
	}
	
	public boolean[][] calculateUnitAttacks(Unit u, boolean[][] movements) {
		boolean[][] tiles = new boolean[map.getHeight()][map.getWidth()];
		if (u.canMoveAndAttack()) {
			int maxRange = u.getMaximumRange();
			for (int j = 0; j < movements.length; j ++) {
				for (int i = 0; i < movements[j].length; i ++) {
					if (movements[j][i]) setBlock(i, j, maxRange, true, tiles);
				}
			}
		} else {
			setBlock(u.getX(), u.getY(), u.getMaximumRange(), true, tiles);
		}
		setBlock(u.getX(), u.getY(), u.getMinimumRange(), false, tiles);
		return tiles;
	}
	
	private void setBlock(int ox, int oy, int size, boolean value, boolean[][] tiles) {
		for (int i = -size; i <= size; i ++) {
			int n = size - Math.abs(i);
			for (int j = 0; j < n; j ++) {
				setAttackable(ox + i, oy + j, tiles, value);
				setAttackable(ox + i, oy - j, tiles, value);
			}
		}
	}
	
	private void setAttackable(int x, int y, boolean[][] attacks, boolean attackable) {
		if (y < 0 || y >= attacks.length) return;
		if (x < 0 || x >= attacks[y].length) return;
		attacks[y][x] = attackable;
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
