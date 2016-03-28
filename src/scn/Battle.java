package scn;

import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import jog.Graphics.Canvas;

import lib.Animation;
import lib.Camera;
import cls.Player;
import cls.building.Building;
import cls.map.DataTile;
import cls.map.Map;
import cls.map.Tile;
import cls.unit.Unit;
import run.Data;
import run.Settings;
import scn.Scene;

public class Battle extends Scene {
	
	private final static int GUI_INFO_BOX_WIDTH = 96;
	private final static int GUI_INFO_BOX_HEIGHT = 96;
	private final static int GUI_ORDER_BOX_WIDTH = 128;
	private final static int GUI_ORDER_BOX_HEIGHT = 256;
	
	private final static Animation[] turnAnimations = new Animation[] {
		new Animation(new jog.Image("gfx/turn_1.png"), 1, 10, 10, false, 0.1),
	};
	
	private Player[] players;
	private Player currentPlayer;
	private int playerTurn;
	private boolean fogOfWar;
	private Canvas fogCanvas;
	private Canvas objectCanvas;
	
	private Unit selectedUnit;
	private boolean[][] selectedUnitMoves;
	private int[] selectedUnitPath;
	private int selectedUnitPathDistance;
	private boolean[][] selectedUnitAttacks;
	
	private Unit movingUnit;
	
	private Building selectedBuilding;
	
	private Unit hoveredUnit;
	private boolean[][] hoveredUnitMoves;
	private boolean[][] hoveredUnitAttacks;
	
	private Building hoveredBuilding;
	
	private Tile hoveredTile;
	private Map map;
	private Camera camera;
	
	private boolean playingTurnAnimation;
	private boolean playingMovementAnimation;
	private boolean playingAttackAnimation;
	private Animation turnAnimation;
	
	// TODO: remove debugging constructor
	public Battle() { this(2, 30, 30, true); }
	
	public Battle(int playerCount, int mapWidth, int mapHeight, boolean fogOfWar) {
		players = new Player[playerCount];
		this.fogOfWar = fogOfWar;
	}
	
	@Override
	public void start() {
		map = new Map("map_1.map");
		for (int i = 0; i < players.length; i ++) {
			players[i] = new Player(map.getWidth(), map.getHeight());
		}
		// TODO remove debug stuff
		players[0].addUnit(Data.getUnit("Engineer").newUnit(players[0], 4, 3));
		players[0].addUnit(Data.getUnit("Commando").newUnit(players[0], 2, 6));
		players[0].addUnit(Data.getUnit("Tank").newUnit(players[0], 12, 6));
		players[0].addUnit(Data.getUnit("Scout").newUnit(players[0], 12, 8));
		players[0].addUnit(Data.getUnit("Helicopter").newUnit(players[0], 2, 10));
		players[1].addUnit(Data.getUnit("Commando").newUnit(players[1], 8, 3));
		players[0].updateVisibility();
		playerTurn = -1;
		hoveredUnit = null;
		hoveredBuilding = null;
		hoveredTile = null;
		selectedUnit = null;
		movingUnit = null;
		selectedBuilding = null;
		camera = new Camera(0, 0, map.getWidth() * DataTile.TILE_SIZE, map.getHeight() * DataTile.TILE_SIZE);
		if (fogOfWar) {
			int w = map.getWidth() * DataTile.TILE_SIZE;
			int h = map.getHeight() * DataTile.TILE_SIZE;
			fogCanvas = jog.Graphics.newCanvas(w, h);
			objectCanvas = jog.Graphics.newCanvas(w, h);
		}
		nextTurn();
	}
	
	public void nextTurn() {
		playerTurn ++;
		playerTurn %= players.length;
		currentPlayer = players[playerTurn];
		currentPlayer.beginTurn();
		if (Settings.showTurnAnimations) playTurnAnimation();
	}
	
	private void playTurnAnimation() {
		playingTurnAnimation = true;
		turnAnimation = turnAnimations[playerTurn];
		turnAnimation.start();
	}

	private Tile getTileAt(int i, int j) {
		return map.getTileAt(i, j);
	}
	
	private Building getBuildingAt(int i, int j) {
		return map.getBuildingAt(i, j);
	}
	
	private Unit getUnitAt(int i, int j) {
		for (Player p : players) {
			Unit u = p.getUnitAt(i, j);
			if (u != null) return u;
		}
		return null;
	}

	// TODO: move to Map class
	private int tileX(double worldX) {
		return (int)(worldX / DataTile.TILE_SIZE);
	}
	
	// TODO: move to map class
	private int tileY(double worldY) {
		return (int)(worldY / DataTile.TILE_SIZE);
	}
	
	@Override
	public void update(double dt) {
		if (playingTurnAnimation) {
			turnAnimation.update(dt);
			if (turnAnimation.hasFinished()) {
				playingTurnAnimation = false;
			}
			return;
		}
		if (currentPlayer.hasFinishedTurn()) {
			nextTurn();
			return;
		}
		for (Player p : players) {
			p.updateUnits(dt);
		}
		int i = tileX(camera.getMouseWorldX());
		int j = tileY(camera.getMouseWorldY());
		if (hoveredUnit != getUnitAt(i, j)) {
			hoveredUnit = getUnitAt(i, j);
			if (hoveredUnit != null) {
				hoveredUnitMoves = calculateUnitMoves(hoveredUnit);
				hoveredUnitAttacks = calculateUnitAttacks(hoveredUnit);
			}
		}
		hoveredBuilding = getBuildingAt(i, j);
		hoveredTile = getTileAt(i, j);
		if (selectedUnit != null && !selectedUnit.hasMoved()) updateSelectedUnitPath(i, j);
		if (playingMovementAnimation) {
			movingUnit.getOwner().updateVisibility();
			if (!movingUnit.isMoving()) {
				if (movingUnit == selectedUnit) {
					selectUnit(movingUnit); // reselect it.
				}
				movingUnit = null;
				playingMovementAnimation = false;
			}
		}
	}
	
	@Override
	public void keyReleased(int key) {
		if (key == KeyEvent.VK_ESCAPE) {
			selectedUnit = null;
			selectedBuilding = null;
		}
	}
	
	@Override
	public void mouseReleased(int mouseX, int mouseY, int mouseKey) {
		int tileX = tileX(camera.getWorldX(mouseX));
		int tileY = tileY(camera.getWorldY(mouseY));
		if (mouseKey == MouseEvent.BUTTON1) {
			mouseClick(tileX, tileY);
		} else if (mouseKey == MouseEvent.BUTTON3) {
			mouseAction(tileX, tileY);
		}
	}
	
	private void mouseAction(int i, int j) {
		System.out.printf("Mouse smart clicked on (%d, %d).\n", i, j);
		// if selected uunit && valid target
		// attack!
		if (selectedUnit != null && canSelectedUnitAttack(i, j)) {
			System.out.println("Unit Attacking...");
		}
		// if selected unit && valid location
		// move there
		if (selectedUnit != null && canSelectedUnitMoveTo(i, j)) {
			moveUnit(selectedUnit, selectedUnitPath);
		}
	}
	
	private void mouseClick(int i, int j) {
		System.out.printf("Mouse clicked on (%d, %d).\n", i, j);
		// if it's a building
		// select building
		Building b = getBuildingAt(i, j);
		if (b != null) {
			selectBuilding(b);
			return;
		}
		// if it's an unexhausted unit
		// select unit
		Unit u = getUnitAt(i, j);
		if (u != null && !u.isExhausted()) {
			selectUnit(u);
			return;
		} else if (u == null) {
			selectedUnit = null;
		}
		// if selected unit 
		// if selected building && valid option
		// build unit / research upgrade / whatever.
		
	}
	
	private void selectBuilding(Building b) {
		selectedBuilding = b;
	}
	
	private void selectUnit(Unit u) {
		if (u == null) {
			selectedUnit = null;
			selectedUnitAttacks = null;
			selectedUnitMoves = null;
			selectedUnitPath = null;
		} else {
			selectedUnit = u;
			selectedUnitMoves = calculateUnitMoves(u);
			selectedUnitAttacks = calculateUnitAttacks(u);
			selectedUnitPath = new int[] { u.getX(), u.getY() };
			selectedUnitPathDistance = 0;
		}
	}
	
	private boolean canSelectedUnitMoveTo(int i, int j) {
		if (j < 0 || j >= selectedUnitMoves.length) return false;
		if (i < 0 || i >= selectedUnitMoves[j].length) return false;
		return selectedUnitMoves[j][i];
	}
	
	private boolean canSelectedUnitAttack(int i, int j) {
		if (j < 0 || j >= selectedUnitAttacks.length) return false;
		if (i < 0 || i >= selectedUnitAttacks[j].length) return false;
		return selectedUnitAttacks[j][i];
	}
	
	private boolean[][] calculateUnitMoves(Unit u) {
		if (u.hasMoved()) return new boolean[0][0];
		int min = 0;
		int max = u.getMoveDistance();
		// TODO write proper movement method that takes terrain into account.
		return getAllTilesWithinRange(u.getX(), u.getY(), min, max);
	}
	
	@Deprecated
	private boolean[][] getAllTilesWithinRange(int x, int y, int minDistance, int maxDistance) {
		boolean [][] tiles = new boolean[map.getHeight()][map.getWidth()];
		for (int j = 0; j < tiles.length; j ++) {
			for (int i = 0; i < tiles[j].length; i ++) {
				int d = Math.abs(i - x) + Math.abs(j - y);
				if (d >= minDistance && d <= maxDistance) tiles[j][i] = true;
			}
		}
		return tiles;
	}
	
	private boolean[][] calculateUnitAttacks(Unit u) {
		int min = u.getMinimumRange();
		int max = u.getMaximumRange();
		if (u.canMoveAndAttack() && !u.hasMoved()) {
			min += u.getMoveDistance();
			max += u.getMoveDistance();
		}
		// TODO write proper attack range method that take unit's weapon and whether it can move and attack
		return getAllTilesWithinRange(u.getX(), u.getY(), min, max);
	}
	
	private void updateSelectedUnitPath(int i, int j) {
		{ // Don't allow diagonal movement.
			int n = selectedUnitPath.length;
			int dx = i - selectedUnitPath[n-2];
			int dy = j - selectedUnitPath[n-1];
			if (Math.abs(dx) + Math.abs(dy) > 1) {
				return;
			}
		}

		int tileOnPath = -1;
		for (int n = 0; n < selectedUnitPath.length; n += 2) {
			if (selectedUnitPath[n] == i && selectedUnitPath[n+1] == j) {
				tileOnPath = n / 2;
			}
		}
		
		if (tileOnPath == -1 && selectedUnitPathDistance > selectedUnit.getMoveDistance()) {
			return;
		}
		
		int[] newPath;
		int newDistance;
		if (tileOnPath == -1) {
//			System.out.printf("Tile not on path, adding on the end.\n");
			newDistance = selectedUnitPathDistance + map.getTileAt(i, j).getMovementCost(selectedUnit);
			if (newDistance > selectedUnit.getMoveDistance()) return;
			newPath = new int[Math.min(selectedUnitPath.length + 2, (selectedUnit.getMoveDistance() + 1) * 2)];
			for (int n = 0; n < newPath.length - 2; n += 2) {
				newPath[n] = selectedUnitPath[n];
				newPath[n+1] = selectedUnitPath[n+1];
			}
			newPath[newPath.length-2] = i;
			newPath[newPath.length-1] = j;
		} else {
			// TODO interpolate all tiles from last one to this
//			System.out.printf("Tile on path (at %d), reverting back to it.\n", tileOnPath);
			newDistance = 0;
			newPath = new int[(tileOnPath+1) * 2];
			for (int n = 0; n < newPath.length; n += 2) {
				newPath[n] = selectedUnitPath[n];
				newPath[n+1] = selectedUnitPath[n+1];
				if (n > 0) // don't count the square we'r on
					newDistance += map.getTileAt(newPath[n], newPath[n+1]).getMovementCost(selectedUnit);
			}
		}
		selectedUnitPathDistance = newDistance;
		selectedUnitPath = newPath;
//		System.out.printf("New path is %d tiles long.\n", newPath.length / 2);
	}
	
	private void moveUnit(Unit u, int[] path) {
		// TODO work out if path is blocked and alter path so it stops at the interruption.
		System.out.printf("Moving Unit %s.\n", u.name);
		for (int i = 0; i < path.length; i += 2) {
			System.out.printf("\t(%d, %d)\n", i, i+1);
		}
		u.move(path);
		movingUnit = u;
		playingMovementAnimation = true;
		selectedUnitMoves = null;
	}
	
	@Override
	public void draw() {
		camera.set();
			drawMap();
			if (fogOfWar) drawFog();
				drawBuildings();
				drawUnits();
				if (playingMovementAnimation) drawUnitMovement();
			if (fogOfWar) hideFog();
			if (selectedUnit != null && (!playingMovementAnimation || movingUnit != selectedUnit)) {
				drawSelectedUnitOptions();
			}
			if (hoveredUnit != null && hoveredUnit != selectedUnit) {
				drawHoveredUnitOptions();
			}
		camera.unset();
		drawTileGUI();
		drawUnitGUI();
		if (selectedBuilding != null) drawBuildingOptions();
		if (playingTurnAnimation) jog.Graphics.draw(turnAnimation, 0, 0);
		if (playingAttackAnimation) drawAttack();
	}
	
	private void drawMap() {
		jog.Graphics.setColour(255, 255, 255);
		map.draw();
	}
	
	private void drawFog() {
		fogCanvas.clear();
		objectCanvas.clear();
		jog.Graphics.setCanvas(fogCanvas);
		jog.Graphics.setColour(0, 0, 0, 128);
		jog.Graphics.rectangle(true, 0, 0, fogCanvas.getWidth(), fogCanvas.getHeight());
		jog.Graphics.setCanvas(objectCanvas);
		jog.Graphics.setColour(255, 255, 255);
	}
	
	private boolean[][] getVisibleTiles() {
		return currentPlayer.getVisibleTiles();
	}
	
	private void hideFog() {
		boolean[][] visible = getVisibleTiles();
		for (int j = 0; j < visible.length; j ++) {
			for (int i = 0; i < visible[j].length; i ++) {
				if (visible[j][i]) {
					fogCanvas.clear(i * DataTile.TILE_SIZE, j * DataTile.TILE_SIZE, DataTile.TILE_SIZE, DataTile.TILE_SIZE);
				} else {
					objectCanvas.clear(i * DataTile.TILE_SIZE, j * DataTile.TILE_SIZE, DataTile.TILE_SIZE, DataTile.TILE_SIZE);
				}
			}
		}
		jog.Graphics.setCanvas();
		jog.Graphics.setColour(255, 255, 255);
		jog.Graphics.draw(objectCanvas, 0, 0);
		jog.Graphics.draw(fogCanvas, 0, 0);
	}
	
	private void drawBuildings() {
		
	}
	
	private void drawUnits() {
		for (Player p : players) {
			p.drawUnits();
		}
	}
	
	private void drawTileGUI() {
		jog.Graphics.setColour(255, 255, 255);
		jog.Graphics.rectangle(true, 8, jog.Graphics.getHeight() - (GUI_INFO_BOX_HEIGHT + 8), GUI_INFO_BOX_WIDTH, GUI_INFO_BOX_HEIGHT);
		jog.Graphics.setColour(0, 0, 0);
		jog.Graphics.rectangle(false, 8, jog.Graphics.getHeight() - (GUI_INFO_BOX_HEIGHT + 8), GUI_INFO_BOX_WIDTH, GUI_INFO_BOX_HEIGHT);
		if (hoveredBuilding != null) {
			// draw building info
		} else if (hoveredTile != null) {
			// draw tile info
		}
	}
	
	private void drawUnitGUI() {
		if (hoveredUnit != null && hoveredUnit != selectedUnit)  {
			final int x = 8;
			final int y = jog.Graphics.getHeight() - (GUI_INFO_BOX_HEIGHT + 8) * 2;
			jog.Graphics.setColour(255, 255, 255);
			jog.Graphics.rectangle(true, x, y, GUI_INFO_BOX_WIDTH, GUI_INFO_BOX_HEIGHT);
			jog.Graphics.setColour(0, 0, 0);
			jog.Graphics.rectangle(false, x, y, GUI_INFO_BOX_WIDTH, GUI_INFO_BOX_HEIGHT);
			jog.Graphics.setScissor(new Rectangle(x, y, GUI_INFO_BOX_WIDTH, GUI_INFO_BOX_HEIGHT));
				jog.Graphics.translate(x, y);
					hoveredUnit.drawInfo();
				jog.Graphics.translate(-x, -y);
			jog.Graphics.setScissor();
		}
		if (selectedUnit != null) {
			final int x = jog.Graphics.getWidth() - GUI_ORDER_BOX_WIDTH - 8;
			final int y = jog.Graphics.getHeight() - GUI_ORDER_BOX_HEIGHT - 8;
			jog.Graphics.setColour(255, 255, 255);
			jog.Graphics.rectangle(true, x, y, GUI_ORDER_BOX_WIDTH, GUI_ORDER_BOX_HEIGHT);
			jog.Graphics.setColour(0, 0, 0);
			jog.Graphics.rectangle(false, x, y, GUI_ORDER_BOX_WIDTH, GUI_ORDER_BOX_HEIGHT);
			jog.Graphics.setScissor(new Rectangle(x, y, GUI_ORDER_BOX_WIDTH, GUI_ORDER_BOX_HEIGHT / 3));
				jog.Graphics.translate(x, y);
					selectedUnit.drawInfo();
				jog.Graphics.translate(-x, -y);
			jog.Graphics.setScissor();
		}
	}
	
	private void drawSelectedUnitOptions() {
		drawMoveAndAttack(selectedUnitMoves, selectedUnitAttacks, 96);
		drawSelectedUnitPath();
		selectedUnit.draw();
	}
	
	private void drawHoveredUnitOptions() {
		drawMoveAndAttack(hoveredUnitMoves, hoveredUnitAttacks, 32);
	}
	
	private void drawMoveAndAttack(boolean[][] moves, boolean[][] attacks, int opacity) {
		for (int j = 0; j < moves.length; j ++) {
			for (int i = 0; i < moves[j].length; i ++) {
				if (moves[j][i]) {
					jog.Graphics.setColour(0, 128, 255, opacity);
					jog.Graphics.rectangle(true, i * DataTile.TILE_SIZE, j * DataTile.TILE_SIZE, DataTile.TILE_SIZE, DataTile.TILE_SIZE);
				}
			}
		}
		for (int j = 0; j < attacks.length; j ++) {
			for (int i = 0; i < attacks[j].length; i ++) {
				if (attacks[j][i]) {
					jog.Graphics.setColour(255, 64, 64, opacity);
					jog.Graphics.rectangle(true, i * DataTile.TILE_SIZE, j * DataTile.TILE_SIZE, DataTile.TILE_SIZE, DataTile.TILE_SIZE);
				}
			}
		}
	}
	
	private void drawSelectedUnitPath() {
		if (selectedUnitPath.length < 2) return;
		jog.Graphics.setColour(255, 255, 255);
		jog.Graphics.setLineWidth(DataTile.TILE_SIZE / 2);
		for (int n = 0; n < selectedUnitPath.length - 2; n += 2) {
			int x1 = (int)((selectedUnitPath[n] + 0.5) * DataTile.TILE_SIZE); 
			int y1 = (int)((selectedUnitPath[n+1] + 0.5) * DataTile.TILE_SIZE);
			int x2 = (int)((selectedUnitPath[n+2] + 0.5) * DataTile.TILE_SIZE); 
			int y2 = (int)((selectedUnitPath[n+3] + 0.5) * DataTile.TILE_SIZE);
			jog.Graphics.line(x1, y1, x2, y2);
		}
		jog.Graphics.setLineWidth(1);
		// TODO draw an arrow head
	}

	private void drawBuildingOptions() {
		
	}
	
	/**
	 * Draw the unit moving.
	 */
	private void drawUnitMovement() {
		
	}
	
	private void drawAttack() {
		
	}

}
