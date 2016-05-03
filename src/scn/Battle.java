package scn;

import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.Arrays;

import jog.Graphics.Canvas;
import jog.Graphics.HorizontalAlign;

import lib.Animation;
import lib.Camera;
import cls.building.Building;
import cls.map.UnitBattle;
import cls.map.DataTile;
import cls.map.Level;
import cls.map.Map;
import cls.map.Tile;
import cls.unit.Unit;
import cls.unit.Unit.Action;
import run.Data;
import run.Settings;
import scn.Scene;

public class Battle extends Scene {
	
	public static abstract class State {

		protected final scn.Battle scene;
		protected final cls.map.Level level;
		protected final cls.map.Map map;
		
		public State(scn.Battle scene) {
			this.scene = scene;
			this.level = scene.level;
			this.map = scene.map;
		}
		
		public abstract void mouseClick(int x, int y, int i, int j);
		public abstract void cancel();
		public abstract void drawMap();
		public abstract void drawScreen();
		
		protected void setSelectedUnit(Unit u) {
			scene.selectUnit(u);
		}
		
		protected void setSelectedBuidling(Building b) {
			scene.selectBuilding(b);
		}
		
		protected void setNextState(State s) {
			scene.nextState = s;
		}
		
		protected void unsetAction() {
			scene.selectedAction = null;
		}
		
		protected void setAction(int i) {
			scene.selectedAction = scene.selectedUnitPossibleActions[i];
		}
		
		protected void setChosenUnit(int i) {
			scene.unitToUnload = scene.unloadableUnits[i];
		}
		
		protected void showActionWheel() {
			scene.showSelectedUnitPossibleActions();
		}
		
		protected void hideActionWheel() {
			scene.hideSelectedUnitPossibleActions();
		}
		
		protected void showUnloadableUnits() {
			scene.showUnloadableUnits();
		}
		
		protected void hideUnloadableUnits() {
			scene.hideUnloadableUnits();
		}
		
		protected void moveSelectedUnit() {
			int[] pos = scene.getSelectedPosition();
			int i = pos[0];
			int j = pos[1];
			scene.moveUnit(scene.selectedUnit, i, j, scene.selectedUnitPath);
		}
		
		protected void attack(int i, int j) {
			scene.attackWithUnit(scene.selectedUnit, i, j, scene.selectedUnitPath);
		}
		
		protected void loadSelectedUnit() {
			int[] pos = scene.getSelectedPosition();
			int i = pos[0];
			int j = pos[1];
			scene.loadUnit(scene.selectedUnit, i, j, scene.selectedUnitPath);
		}
		
		protected void unloadUnit(Unit u, int x, int y) {
			scene.unloadUnit(scene.selectedUnit, u, x, y);
		}
		
	}
	
	private static interface DelayedAction {
		public void call();
	}
	
	private final static int GUI_INFO_BOX_WIDTH = 96;
	private final static int GUI_INFO_BOX_HEIGHT = 96;
	private final static int GUI_ORDER_BOX_WIDTH = 128;
	private final static int GUI_ORDER_BOX_HEIGHT = 256;
	
	private static final int ACTION_WHEEL_RADIUS = DataTile.TILE_SIZE;
	private static final int UNLOAD_SELECTION_WIDTH = 128;
	private static final int UNLOAD_SELECTION_MARGIN = DataTile.TILE_SIZE * 2;
	private static final int UNLOAD_SELECTION_PADDING = 4;
	
	private final static Animation[] turnAnimations = new Animation[] {
		new Animation(new jog.Image("gfx/turn_1.png"), 1, 10, 10, false, 0.1),
	};
	
	private boolean fogOfWar;
	private Canvas fogCanvas;
	private Canvas objectCanvas;
	
	// TODO move the following into State, and make them static.
	private Unit selectedUnit;
	private Action selectedAction;
	private boolean[][] selectedUnitMoves;
	private boolean showSelectedUnitActions;
	private Action[] selectedUnitPossibleActions;
	private int[] selectedUnitPath;
	private int selectedPathDistance;
	private boolean[][] selectedUnitAttacks;
	private Building selectedBuilding;
	private boolean showUnloadableUnits;
	private Unit[] unloadableUnits;
	private Unit unitToUnload;
	
	private Unit hoveredUnit;
	private boolean[][] hoveredUnitMoves;
	private boolean[][] hoveredUnitAttacks;
	private Building hoveredBuilding;
	private Tile hoveredTile;

	private Unit movingUnit;
	
	private String levelName;
	private Level level;
	private Map map;
	private Camera camera;
	
	private boolean playingTurnAnimation;
	private boolean playingMovementAnimation;
	private boolean playingAttackAnimation;
	private Animation turnAnimation;
	private UnitBattle attackAnimation;
	private DelayedAction postMoveAction;
	private State inputState;
	private State nextState;
	
	// TODO: remove debugging constructor
	public Battle() { this("Debug"); }
	
	public Battle(String levelName) {
		this.levelName = levelName;
	}
	
	@Override
	public void start() {
		level = Data.getLevel(levelName).newLevel();
		this.fogOfWar = level.hasFogOfWar();
		map = level.getMap();
		hoveredUnit = null;
		hoveredBuilding = null;
		hoveredTile = null;
		selectedUnit = null;
		selectedBuilding = null;
		camera = new Camera(0, 0, map.getWidth() * DataTile.TILE_SIZE, map.getHeight() * DataTile.TILE_SIZE);
		if (fogOfWar) {
			int w = map.getWidth() * DataTile.TILE_SIZE;
			int h = map.getHeight() * DataTile.TILE_SIZE;
			fogCanvas = jog.Graphics.newCanvas(w, h);
			objectCanvas = jog.Graphics.newCanvas(w, h);
		}
		inputState = new scn.battleState.Idle(this);
		nextState = null;
		level.begin();
		playTurnAnimation();
	}
	
	private void playTurnAnimation() {
		if (!Settings.showTurnAnimations) return;
		playingTurnAnimation = true;
		turnAnimation = turnAnimations[level.getCurrentPlayerIndex()];
		turnAnimation.start();
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
		if (level.getCurrentPlayer().hasFinishedTurn()) {
			level.nextTurn();
			playTurnAnimation();
		}
		level.update(dt);
		updateMouseInput();
		if (playingMovementAnimation) {
			movingUnit.getOwner().updateVisibility(map); // TODO move this into unit
			if (!movingUnit.isMoving()) {
				if (movingUnit != selectedUnit) {
					selectUnit(selectedUnit); // reselect the selected unit.
				} else { selectUnit(null); }
				movingUnit = null;
				playingMovementAnimation = false;
				if (postMoveAction != null) {
					postMoveAction.call();
					postMoveAction = null;
				}
			}
		}
		if (playingAttackAnimation && attackAnimation.isPlaying()) {
			attackAnimation.update(dt);
			if (attackAnimation.hasFinished()) {
				playingAttackAnimation = false;
				attackAnimation = null;
			}
		}
		double dx = 0, dy = 0;
		final int CAMERA_SCROLL_SPEED = 512;
		if (jog.Input.isKeyDown(KeyEvent.VK_UP)) {
			dy -= dt * CAMERA_SCROLL_SPEED;
		}
		if (jog.Input.isKeyDown(KeyEvent.VK_LEFT)) {
			dx -= dt * CAMERA_SCROLL_SPEED;
				}
		if (jog.Input.isKeyDown(KeyEvent.VK_DOWN)) {
			dy += dt * CAMERA_SCROLL_SPEED;
		}
		if (jog.Input.isKeyDown(KeyEvent.VK_RIGHT)) {
			dx += dt * CAMERA_SCROLL_SPEED;
		}
		camera.move(dx, dy);
		if (nextState != null) {
			inputState = nextState;
			nextState = null;
		}
	}
	
	private void updateMouseInput() {
		int i = map.tileX(camera.getMouseWorldX());
		int j = map.tileY(camera.getMouseWorldY());
		if (level.getCurrentPlayer().canSee(i, j)) {
			if (hoveredUnit != level.getUnitAt(i, j)) {
				hoveredUnit = level.getUnitAt(i, j);
				if (hoveredUnit != null) {
					hoveredUnitMoves = level.calculateUnitMoves(hoveredUnit);
					hoveredUnitAttacks = level.calculateUnitAttacks(hoveredUnit, hoveredUnitMoves);
				}
			}
			hoveredBuilding = level.getBuildingAt(i, j);
		}
		hoveredTile = map.getTileAt(i, j);
		if (selectedAction == null || selectedAction == Action.MOVE) {
			if (selectedUnit != null && !selectedUnit.isMoving() && 
					selectedUnit.canMove() && !showSelectedUnitActions) {
				updateSelectedUnitPath(i, j);
			}
		}
	}
	
	@Override
	public void keyReleased(int key) {
		if (key == KeyEvent.VK_ESCAPE) {
			selectedUnit = null;
			selectedBuilding = null;
		}
		if (key == KeyEvent.VK_ENTER) {
			level.nextTurn(); 
			// TODO have AI do stuff
			level.nextTurn(); // This just reverts back to our turn for now.
		}
		if (selectedUnit != null) {
			if (key == KeyEvent.VK_M) {
				selectedAction = Action.MOVE;
			}
			if (key == KeyEvent.VK_A) {
				selectedAction = Action.ATTACK;
			}
			if (key == KeyEvent.VK_B) {
				selectedAction = Action.BUILD;
			}
			if (key == KeyEvent.VK_D) {
				selectedAction = Action.DEFEND;
			}
			if (key == KeyEvent.VK_L) {
				selectedAction = Action.LOAD;
			}
		}
	}
	
	@Override
	public void mouseReleased(int mouseX, int mouseY, int mouseKey) {
		int tileX = map.tileX(camera.getWorldX(mouseX));
		int tileY = map.tileY(camera.getWorldY(mouseY));
		if (mouseKey == MouseEvent.BUTTON1) {
			inputState.mouseClick(mouseX, mouseY, tileX, tileY);
//			mouseClick(tileX, tileY);
		} else if (mouseKey == MouseEvent.BUTTON3) {
			inputState.cancel();
//			smartAction(tileX, tileY);
		}
	}
	/*
	private void smartAction(int i, int j) {
		System.out.printf("Mouse smart clicked on (%d, %d).\n", i, j);
		if (selectedUnit != null) {
			if (showSelectedUnitActions) {
				showSelectedUnitActions = false;
				return;
			}
			if (canSelectedUnitAttack(i, j)) {
				if (attackWithUnit(selectedUnit, i, j, selectedUnitPath))
					return;
			}
			if (canSelectedUnitLoad(i, j)) {
				if (loadUnit(selectedUnit, i, j, selectedUnitPath))
					return;
			}
			if (canSelectedUnitMoveTo(i, j)) {
				if (moveUnit(selectedUnit, i, j, selectedUnitPath))
					return;
			}
			selectUnit(null);
		}
	}
	
	private void mouseClick(int i, int j) {
		System.out.printf("Mouse clicked on (%d, %d).\n", i, j);
		System.out.println(selectedUnit);
		System.out.println(selectedAction);
		
		
		if (selectedUnit != null && selectedAction != null) {
			switch (selectedAction) {
			case MOVE:
				if (canSelectedUnitMoveTo(i, j)) {
					moveUnit(selectedUnit, i, j, selectedUnitPath);
				}
				break;
			case ATTACK:
				System.out.println("Attacking?");
				if (canSelectedUnitAttack(i, j)) {
					attackWithUnit(selectedUnit, i, j, selectedUnitPath);
				}
				break;
			case BUILD:
				break;
			case DEFEND:
				break;
			case LOAD:
				break;
			case UNLOAD:
				if (canSelectedUnitUnload(i, j)) {
					
				}
				break;
			default:
				break;
			}
			System.out.println("Performing action.");
			return;
		}
		
		if (selectedUnit != null && showSelectedUnitActions ) {
			int action = getClickedAction();
			if (action >= 0) {
				System.out.println("Selecting Action");
				selectedAction = selectedUnitPossibleActions[action];
			}
			showSelectedUnitActions = false;
			return;
		}
		
		if (selectedUnit != null && canSelectedUnitGetTo(i, j) && !showSelectedUnitActions) {
			showSelectedUnitPossibleActions();
			return;
		}
		
		// if it's a building
		// select building
		Building b = level.getBuildingAt(i, j);
		if (b != null) {
			selectBuilding(b);
			return;
		}
		// if it's an unexhausted unit
		// select unit
		Unit u = level.getUnitAt(i, j);
		if (u != null && !u.isExhausted()) {
			selectUnit(u);
			return;
		}

		if (u == null) {
			selectedUnit = null;
		}
	}
	*/
	public int getClickedAction(int mx, int my) {
		if (!showSelectedUnitActions) return -1;
		if (selectedUnitPossibleActions.length == 0) return -1;
		int x = (int)camera.getWorldX(mx);
		int y = (int)camera.getWorldY(my);
		int[] pos = getSelectedPosition();
		double ox = (pos[0] + 0.5) * DataTile.TILE_SIZE;
		double oy = (pos[1] + 0.5) * DataTile.TILE_SIZE;
		int n = selectedUnitPossibleActions.length;
		
		for (int i = 0; i < n; i ++) {
			final int radiusSqaured = 16 * 16;
			double actionX = ox + ACTION_WHEEL_RADIUS * Math.cos(2 * Math.PI * i / n);
			double actionY = oy + ACTION_WHEEL_RADIUS * Math.sin(2 * Math.PI * i / n);
			double dx = x - actionX; 
			double dy = y - actionY;
			if (dx * dx + dy * dy <= radiusSqaured) return i;
		}
		return -1;
	}
	
	public Action getSelectedAction() {
		return selectedAction;
	}
	
	public int getClickedChoice(int mx, int my) {
		if (!showUnloadableUnits) return -1;
		if (unloadableUnits.length == 0) return -1;
		int x = (int)camera.getWorldX(mx);
		int y = (int)camera.getWorldY(my);
		int[] pos = getSelectedPosition();
		double ox = (pos[0] + 0.5) * DataTile.TILE_SIZE;
		double oy = (pos[1] + 0.5) * DataTile.TILE_SIZE;
		int n = unloadableUnits.length;
		if (camera.getScreenX(ox) > jog.Graphics.getWidth()) {
			ox -= (UNLOAD_SELECTION_MARGIN + UNLOAD_SELECTION_WIDTH);
		} else {
			ox += UNLOAD_SELECTION_MARGIN;
		}
		// TODO offset oy
		for (int i = 0; i < n; i ++) {
			double choiceX = ox + UNLOAD_SELECTION_PADDING;
			double choiceY = oy + (UNLOAD_SELECTION_PADDING + 16) * i;
			System.out.printf("%d, %d in [%f, %f, %d, %d]?\n", x, y, choiceX, choiceY, UNLOAD_SELECTION_WIDTH, 16);
			if (x >= choiceX && x <= choiceX + UNLOAD_SELECTION_WIDTH &&
				y >= choiceY && y <= choiceY + 16) return i;
		}
		return -1;
	}
	
	public Unit getChosenUnit() {
		return unitToUnload;
	}
	
	private void showSelectedUnitPossibleActions() {
		showSelectedUnitActions = true;
		int x = selectedUnitPath[selectedUnitPath.length - 2];
		int y = selectedUnitPath[selectedUnitPath.length - 1];
		selectedUnitPossibleActions = level.getAvailableActions(selectedUnit, x, y);
	}
	
	private void hideSelectedUnitPossibleActions() {
		showSelectedUnitActions = false;
	}
	
	private void showUnloadableUnits() {
		showUnloadableUnits = true;
		unitToUnload = null;
		unloadableUnits = selectedUnit.getStoredUnits();
	}
	
	private void hideUnloadableUnits() {
		unitToUnload = null;
		showUnloadableUnits = false;
	}
	
	private boolean attackWithUnit(Unit unit, int i, int j, int[] unitPath) {
		// TODO add some indirect attackTile option maybe?
		Unit defender = level.getUnitAt(i, j);
		if (defender != null && level.areEnemies(unit.getOwner(), defender.getOwner())) {
			int x, y;
			if (unit.canMoveAndAttack()) {
				x = unitPath[unitPath.length - 2];
				y = unitPath[unitPath.length - 1];
				int distance = Math.abs(i - x) + Math.abs(j - y);
				if (distance > unit.getMaximumRange()) return false;
//				if (distance < unit.getMinimumRange()) return;
				moveUnit(unit, x, y, unitPath);
			} else {
				x = unit.getX();
				y = unit.getY();
			}
			System.out.printf("%s is attacking.\n", unit.name);
			System.out.printf("%s is defending.\n", defender.name);
			unit.attack(defender);
			postMoveAction = new DelayedAction() {
				@Override
				public void call() {
					attackAnimation.start();
					playingAttackAnimation = true;
				}
			};
			attackAnimation = new UnitBattle(map.getTileAt(x, y), map.getTileAt(defender.getX(), defender.getY()), unit, defender);
			return true;
		}
		return false;
	}

	private boolean loadUnit(final Unit unit, final int i, final int j, final int[] unitPath) {
		final Unit transport = level.getUnitAt(i, j);
		if (transport != null) {
			moveUnit(unit, i, j, unitPath);
			postMoveAction = new DelayedAction() {
				@Override
				public void call() {
					transport.tryLoadUnit(unit);
					unit.getOwner().updateVisibility(map);
				}
			};
			return true;
		}
		final Building storage = level.getBuildingAt(i, j);
		if (storage != null) {
			moveUnit(unit, i, j, unitPath);
			postMoveAction = new DelayedAction() {
				@Override
				public void call() {
					storage.tryStoreUnit(unit);
					unit.getOwner().updateVisibility(map);
				}
			};
			return true;
		}
		return false;
	}
	
	private boolean unloadUnit(final Unit transport, final Unit u, final int x, final int y) {
		if (!transport.isCarrying(u)) return false;
		if (level.getUnitAt(x, y) != null) return false;
		if (level.getBuildingAt(x, y) != null) return false;
		moveUnit(transport, selectedUnitPath);
		postMoveAction = new DelayedAction() {
			@Override
			public void call() {
				transport.unloadUnit(u, x, y);
				transport.getOwner().updateVisibility(map);
				System.out.printf("Unloaded %s.\n", u.toString());
			}
		};
		return false;
	}

	private int[] getSelectedPosition() {
		int i, j;
		if (selectedUnitPath.length >= 2) {
			i = selectedUnitPath[selectedUnitPath.length - 2];
			j = selectedUnitPath[selectedUnitPath.length - 1];
		} else {
			i = selectedUnit.getX();
			j = selectedUnit.getY();
		}
		return new int[] { i, j };
	}
	
	public boolean canSelectedUnitMoveTo(int i, int j) {
		if (level.getCurrentPlayer().canSee(i, j)) {
			Unit u = level.getUnitAt(i, j);
			if (u != null && u != selectedUnit) return false;
		}
		return canSelectedUnitGetTo(i, j);
	}
	
	public boolean canSelectedUnitGetTo(int i, int j) {
		if (selectedUnitMoves == null) return false;
		if (j < 0 || j >= selectedUnitMoves.length) return false;
		if (i < 0 || i >= selectedUnitMoves[j].length) return false;
		return selectedUnitMoves[j][i];
	}
	
	public boolean canSelectedUnitAttack(int i, int j) {
		if (selectedUnitAttacks == null) return false;
		if (j < 0 || j >= selectedUnitAttacks.length) return false;
		if (i < 0 || i >= selectedUnitAttacks[j].length) return false;
		return selectedUnitAttacks[j][i];
	}
	
	public boolean canSelectedUnitBuild(Building b, int i, int j) { 
		return false; // TODO implement building checks
	}
	
	public boolean canSelectedUnitLoad(int i, int j) {
		if (!canSelectedUnitGetTo(i, j)) return false;
		Unit transport = level.getUnitAt(i, j);
		if (transport != null) {
			if (transport.canStoreUnit(selectedUnit)) return true;
		}
		Building bunker = level.getBuildingAt(i, j);
		if (bunker != null) {
			if (bunker.canStoreUnit(selectedUnit)) return true;
		}
		return false;
	}
	
	public boolean canSelectedUnitUnload(int i, int j) {
		int x, y;
		if (selectedUnitPath != null && selectedUnitPath.length >= 2) {
			x = selectedUnitPath[selectedUnitPath.length - 2];
			y = selectedUnitPath[selectedUnitPath.length - 1];
		} else {
			x = selectedUnit.getX();
			y = selectedUnit.getY();
		}
		return Math.abs(i - x) + Math.abs(j - y) == 1;
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
			selectedAction = null;
			selectedUnitPossibleActions = null;
			showSelectedUnitActions = false;
		} else if (u.getOwner() == level.getCurrentPlayer()) {
			selectedUnit = u;
			selectedUnitMoves = level.calculateUnitMoves(u);
			selectedUnitAttacks = level.calculateUnitAttacks(u, selectedUnitMoves);
			selectedUnitPath = new int[] { u.getX(), u.getY() };
			selectedPathDistance = 0;
			selectedAction = null;
			selectedUnitPossibleActions = null;
			showSelectedUnitActions = false;
		}
	}

	public Unit getSelectedUnit() {
		return selectedUnit;
	}

	private void updateSelectedUnitPath(final int x, final int y) {
		final int lastX = selectedUnitPath[selectedUnitPath.length - 2];
		final int lastY = selectedUnitPath[selectedUnitPath.length - 1];
		
		final int dx = x - lastX;
		final int dy = y - lastY;
		
		/*
		 * If we haven't moved, don't do anything.
		 */
		if (dx == 0 && dy == 0) return;
		
		/*
		 * If the unit can't move there, don't do anything
		 */
		if (!selectedUnitMoves[y][x]) return;
		
		/*
		 * If this tile is on the path, revert back to it.
		 */
		int tileOnPath = -1;
		for (int i = 0; i < selectedUnitPath.length; i += 2) {
			if (selectedUnitPath[i] == x && selectedUnitPath[i+1] == y) {
				tileOnPath = i / 2;
			}
		}
		if (tileOnPath >= 0) {
			selectedUnitPath = Arrays.copyOf(selectedUnitPath, (tileOnPath+1) * 2);
			updatePathDistance();
			System.out.println("Reverted back to previous tile.");
			return;
		}
		
		final int distanceRemaining = selectedUnit.getMoveDistance() - selectedPathDistance;
		
		/*
		 * If we've added one tile on the end of our path.		
		 */
		if (Math.abs(dx) + Math.abs(dy) == 1) {
			final int moveCost = map.getTileAt(x, y).getMovementCost(selectedUnit);
			if (moveCost > distanceRemaining) {
				// Too far. Try to get there from the unit's position.
				int[] newPath = level.getPathTo(selectedUnit, x, y, new int[] { selectedUnit.getX(), selectedUnit.getY() }, selectedUnit.getMoveDistance());
				if (newPath != null) {
					selectedUnitPath = newPath;
					updatePathDistance();
					System.out.println("Added new path to path.");
				}
			} else {
				// Not too far, add it on the end.
				int[] newPath = Arrays.copyOf(selectedUnitPath, selectedUnitPath.length + 2);
				newPath[newPath.length - 2] = x;
				newPath[newPath.length - 1] = y;
				selectedUnitPath = newPath;
//				selectedPathDistance += moveCost;
				updatePathDistance();
				System.out.println("Added a tile on the end.");
			}
			return;
		}

		/* 
		 * If we've moved the mouse far/diagonally
		 */
		boolean changedPath = false;
		// Get path from last path point
		int[] newPath = level.getPathTo(selectedUnit, x, y, selectedUnitPath, distanceRemaining); 
		if (newPath != null) {
			selectedUnitPath = newPath;
			changedPath = true;
		} else {
			// Get path from unit position
			newPath = level.getPathTo(selectedUnit, x, y, new int[] { selectedUnit.getX(), selectedUnit.getY() }, selectedUnit.getMoveDistance());
			if (newPath != null) {
				selectedUnitPath = newPath;
				changedPath = true;
			}
		}
		// Update path distance
		if (changedPath) {
			updatePathDistance();
			System.out.println("Added new path to path.");
		}
	}
	
	private void updatePathDistance() {
		int newDistance = 0;
		for (int i = 2; i < selectedUnitPath.length; i += 2) {
			newDistance += map.getTileAt(selectedUnitPath[i], selectedUnitPath[i+1]).getMovementCost(selectedUnit);
		}
		selectedPathDistance = newDistance;
	}
	
	
	private boolean moveUnit(Unit unit, int[] path) {
		int[] pos = getSelectedPosition();
		return moveUnit(unit, pos[0], pos[1], path);
	}
	private boolean moveUnit(Unit u, int x, int y, int[] path) {
		if (x != path[path.length-2] || y != path[path.length-1]) return false;
		System.out.printf("Moving Unit %s.\n", u.name);
		for (int i = 0; i < path.length; i += 2) {
			System.out.printf("\t(%d, %d)\n", path[i], path[i+1]);
			Building blockingBuilding = level.getBuildingAt(path[i], path[i+1]);
			if (blockingBuilding != null && blockingBuilding.getOwner() != level.getCurrentPlayer()) {
				System.out.printf("\tBlocked by building!\n", path[i], path[i+1]);
				path = Arrays.copyOf(path, i);
				System.out.printf("\tStopping at (%d, %d).\n", path[i-2], path[i-1]);
				break;
			}
			Unit blockingUnit = level.getUnitAt(path[i], path[i+1]);
			if (blockingUnit != null && blockingUnit.getOwner() != level.getCurrentPlayer()) {
				System.out.printf("\tBlocked by unit!\n", path[i], path[i+1]);
				path = Arrays.copyOf(path, i);
				System.out.printf("\tStopping at (%d, %d).\n", path[i-2], path[i-1]);
				break;
			}
		}
		u.move(path);
		movingUnit = u;
		playingMovementAnimation = true;
		selectedUnitMoves = null;
		return true;
	}
	
	@Override
	public void draw() {
		camera.set();
			drawMap();
			if (fogOfWar) drawFog();
			level.drawBuildings();
			level.drawUnits();
			if (fogOfWar) hideFog();
			inputState.drawMap();
//			if (selectedUnit != null) {
//				if (!playingMovementAnimation || movingUnit != selectedUnit) {
//					drawSelectedUnitOptions();
//				}
//				if (showSelectedUnitActions) {
//					drawActionWheel();
//				}
//			}
			if (hoveredUnit != null && hoveredUnit != selectedUnit) {
				drawHoveredUnitOptions();
			}
		camera.unset();
		drawTileGUI();
		drawUnitGUI();
		inputState.drawScreen();
//		if (selectedBuilding != null) drawBuildingOptions();
		if (playingTurnAnimation) jog.Graphics.draw(turnAnimation, 0, 0);
		if (playingAttackAnimation) drawAttack();
		drawDebug();
		drawCursor();
	}
	
	private void drawCursor() {
		// TODO context-dependent cursor drawing
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
	
	private void hideFog() {
		boolean[][] visible = level.getVisibleTiles();
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
	
	private void drawTileGUI() {
		final int x = 8;
		final int y = jog.Graphics.getHeight() - (GUI_INFO_BOX_HEIGHT + 8);
		jog.Graphics.setColour(255, 255, 255);
		jog.Graphics.rectangle(true, x, y, GUI_INFO_BOX_WIDTH, GUI_INFO_BOX_HEIGHT);
		jog.Graphics.setColour(0, 0, 0);
		jog.Graphics.rectangle(false, x, y, GUI_INFO_BOX_WIDTH, GUI_INFO_BOX_HEIGHT);
		jog.Graphics.setScissor(new Rectangle(x, y, GUI_INFO_BOX_WIDTH, GUI_INFO_BOX_HEIGHT));
		jog.Graphics.translate(x, y);
		if (hoveredBuilding != null) {
			// TODO draw building info
		} else if (hoveredTile != null) {
			jog.Graphics.print(hoveredTile.name, 0, 0, GUI_INFO_BOX_WIDTH, 24, HorizontalAlign.CENTRE);
			hoveredTile.draw((GUI_INFO_BOX_WIDTH - DataTile.TILE_SIZE) / 2, 32);
			// TODO draw tile info
		}
		jog.Graphics.translate(-x, -y);
		jog.Graphics.setScissor();
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

	public void drawActionWheel() {
		drawActionWheel(null);
	}
	public void drawActionWheel(Unit.Action selectedAction) {
		int x = selectedUnitPath[selectedUnitPath.length-2];
		int y = selectedUnitPath[selectedUnitPath.length-1];
		int n = selectedUnitPossibleActions.length;
		double ox = (x + 0.5) * DataTile.TILE_SIZE;
		double oy = (y + 0.5) * DataTile.TILE_SIZE;
		jog.Graphics.setColour(255, 255, 255);
		jog.Graphics.circle(false, ox, oy, ACTION_WHEEL_RADIUS);
		for (int i = 0; i < n; i ++) {
			jog.Graphics.setColour(255, 255, 255);
			double actionX = ox + ACTION_WHEEL_RADIUS * Math.cos(2 * Math.PI * i / n);
			double actionY = oy + ACTION_WHEEL_RADIUS * Math.sin(2 * Math.PI * i / n);
			jog.Graphics.circle(true, actionX, actionY, 16);
			jog.Graphics.setColour(0, 0, 0);
			jog.Graphics.circle(false, actionX, actionY, 16);
			Action a = selectedUnitPossibleActions[i];
			if (a == selectedAction) {
				jog.Graphics.printCentred("✓", actionX, actionY);
			} else {
				jog.Graphics.printCentred(a.toString().substring(0, 1), actionX, actionY);
			}
		}
	}

	public void drawAttackableUnits() {
		drawAttackableUnits(null);
	}
	public void drawAttackableUnits(Unit target) {
		int[] pos = getSelectedPosition();
		int x = pos[0];
		int y = pos[1];
		for (int j = y - selectedUnit.getMaximumRange(); j < y + selectedUnit.getMaximumRange(); j ++) {
			for (int i = x - selectedUnit.getMaximumRange(); i < x + selectedUnit.getMaximumRange(); i ++) {
				Unit u = level.getUnitAt(i, j);
				if (u == null) continue;
				if (u == target) {
					double confirmX = (i + 0.5) * DataTile.TILE_SIZE;
					double confirmY = (j + 0.2) * DataTile.TILE_SIZE;
					jog.Graphics.setColour(255, 255, 255);
					jog.Graphics.circle(true, confirmX, confirmY, 8);
					jog.Graphics.setColour(0, 0, 0);
					jog.Graphics.circle(false, confirmX, confirmY, 8);
					jog.Graphics.printCentred("✓", confirmX, confirmY);
				} else if (level.areEnemies(selectedUnit.getOwner(), u.getOwner())) {
					jog.Graphics.setColour(255, 0, 0);
					jog.Graphics.rectangle(false, i * DataTile.TILE_SIZE, j * DataTile.TILE_SIZE, DataTile.TILE_SIZE, DataTile.TILE_SIZE);
				}
			}
		}
	}
	
	public void drawUnloadableUnits() {
		if (!showUnloadableUnits) return;
		if (unloadableUnits.length == 0) return;
		int[] pos = getSelectedPosition();
		double ox = (pos[0] + 0.5) * DataTile.TILE_SIZE;
		double oy = (pos[1] + 0.5) * DataTile.TILE_SIZE;
		int n = unloadableUnits.length;
		if (camera.getScreenX(ox) > jog.Graphics.getWidth()) {
			ox -= (UNLOAD_SELECTION_MARGIN + UNLOAD_SELECTION_WIDTH);
		} else {
			ox += UNLOAD_SELECTION_MARGIN;
		}
		// TODO offset oy 
		for (int i = 0; i < n; i ++) {
			final String text = unloadableUnits[i].name;
			final int fontHeight = jog.Graphics.getFontHeight(text);
			double choiceX = ox + UNLOAD_SELECTION_PADDING;
			double choiceY = oy + (UNLOAD_SELECTION_PADDING + fontHeight) * i;
			jog.Graphics.setColour(255, 255, 255);
			jog.Graphics.rectangle(true, choiceX, choiceY, UNLOAD_SELECTION_WIDTH, jog.Graphics.getFontHeight(text));
			jog.Graphics.setColour(0, 0, 0);
			jog.Graphics.rectangle(false, choiceX, choiceY, UNLOAD_SELECTION_WIDTH, jog.Graphics.getFontHeight(text));
			jog.Graphics.print(text, choiceX, choiceY - 4);
		}
	}
	
	public void drawAttackInfo(Unit target) {
		jog.Graphics.setColour(255, 255, 255);
		jog.Graphics.print(selectedUnit.name + " vs " + target.name, 32, 32);
		int[] pos = getSelectedPosition();
		int x = pos[0];
		int y = pos[1];
		jog.Graphics.print(map.getTileAt(x, y).name, 32, 64);
		jog.Graphics.print(map.getTileAt(target.getX(), target.getY()).name, 32, 96);
	}
	
	public void drawSelectedUnitOptions() {
		drawMoveAndAttack(selectedUnitMoves, selectedUnitAttacks, 96);
		drawSelectedUnitPath();
		selectedUnit.draw();
	}
	
	public void drawHoveredUnitOptions() {
		drawMoveAndAttack(hoveredUnitMoves, hoveredUnitAttacks, 32);
	}

	public void drawUnloadLocations(int selectedX, int selectedY) {
		int[] pos = getSelectedPosition();
		int x = pos[0];
		int y = pos[1];
		for (int j = y - 1; j <= y + 1; j ++) {
			for (int i = x - 1; i <= x + 1; i ++) {
				if (Math.abs(i - x) + Math.abs(j - y) != 1) continue;
				if (level.getUnitAt(i, j) != null || level.getBuildingAt(i, j) != null) continue;
				if (i == selectedX && j == selectedY) {
					jog.Graphics.setColour(255, 255, 255, 96);
					jog.Graphics.rectangle(true, i * DataTile.TILE_SIZE, j * DataTile.TILE_SIZE, DataTile.TILE_SIZE, DataTile.TILE_SIZE);
					double confirmX = (i + 0.5) * DataTile.TILE_SIZE;
					double confirmY = (j + 0.2) * DataTile.TILE_SIZE;
					jog.Graphics.setColour(255, 255, 255);
					jog.Graphics.circle(true, confirmX, confirmY, 8);
					jog.Graphics.setColour(0, 0, 0);
					jog.Graphics.circle(false, confirmX, confirmY, 8);
					jog.Graphics.printCentred("✓", confirmX, confirmY);
				} else {
					jog.Graphics.setColour(0, 255, 128, 96);
					jog.Graphics.rectangle(true, i * DataTile.TILE_SIZE, j * DataTile.TILE_SIZE, DataTile.TILE_SIZE, DataTile.TILE_SIZE);
				}
			}
		}
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
				boolean isMove = false;
				if (j >= 0 && j < moves.length) {
					if (i >= 0 && i < moves[j].length) {
						isMove = moves[j][i];
					}
				}
				if (attacks[j][i] && !isMove) {
					jog.Graphics.setColour(255, 64, 64, opacity);
					jog.Graphics.rectangle(true, i * DataTile.TILE_SIZE, j * DataTile.TILE_SIZE, DataTile.TILE_SIZE, DataTile.TILE_SIZE);
				}
			}
		}
	}
	
	private void drawSelectedUnitPath() {
		if (selectedUnitPath.length < 2) return;
		// Draw line
		jog.Graphics.setColour(255, 255, 255);
		jog.Graphics.setLineWidth(DataTile.TILE_SIZE / 2);
		for (int n = 0; n < selectedUnitPath.length - 4; n += 2) {
			int x1 = (int)((selectedUnitPath[n] + 0.5) * DataTile.TILE_SIZE); 
			int y1 = (int)((selectedUnitPath[n+1] + 0.5) * DataTile.TILE_SIZE);
			int x2 = (int)((selectedUnitPath[n+2] + 0.5) * DataTile.TILE_SIZE); 
			int y2 = (int)((selectedUnitPath[n+3] + 0.5) * DataTile.TILE_SIZE);
			jog.Graphics.line(x1, y1, x2, y2);
		}
		jog.Graphics.setLineWidth(1);
		
		if (selectedUnitPath.length < 4) return;
		// Draw arrowhead
		final int W = DataTile.TILE_SIZE;
		int ox = (int)((selectedUnitPath[selectedUnitPath.length - 4] + 0.5) * W);
		int oy = (int)((selectedUnitPath[selectedUnitPath.length - 3] + 0.5) * W);
		int dx = selectedUnitPath[selectedUnitPath.length - 2] - selectedUnitPath[selectedUnitPath.length - 4];
		int dy = selectedUnitPath[selectedUnitPath.length - 1] - selectedUnitPath[selectedUnitPath.length - 3];
		jog.Graphics.setLineWidth(DataTile.TILE_SIZE / 2);
		if (dx > 0) {
			jog.Graphics.polygon(true, ox + W, oy, ox + W / 2, oy - W / 2, ox + W / 2, oy + W / 2);
			jog.Graphics.line(ox, oy, ox + W / 2, oy);
		} else if (dx < 0) {
			jog.Graphics.polygon(true, ox - W, oy, ox - W / 2, oy - W / 2, ox - W / 2, oy + W / 2);
			jog.Graphics.line(ox, oy, ox - W / 2, oy);
		} else if (dy > 0) {
			jog.Graphics.polygon(true, ox, oy + W, ox - W / 2, oy + W / 2, ox + W / 2, oy + W / 2);
			jog.Graphics.line(ox, oy, ox, oy + W / 2);
		} else if (dy < 0) {
			jog.Graphics.polygon(true, ox, oy - W, ox - W / 2, oy - W / 2, ox + W / 2, oy - W / 2);
			jog.Graphics.line(ox, oy, ox, oy - W / 2);
		}
		jog.Graphics.setLineWidth(1);
	}

	public void drawBuildingOptions() {
		
	}

	private void drawAttack() {
		if (!Settings.showAttackAnimations) {
			playingAttackAnimation = false;
		}
		if (!playingAttackAnimation) return;
		if (attackAnimation.isPlaying()) attackAnimation.draw();
	}
	
	// TODO remove
	private void drawDebug() {
		jog.Graphics.setColour(0, 0, 0);
		if (selectedAction != null)
			jog.Graphics.print(selectedAction.toString(), 0, 0);
		if (selectedUnitPath != null && selectedUnit != null)
			jog.Graphics.print(String.valueOf(selectedPathDistance) + " / " + selectedUnit.getMoveDistance(), 0, 16);
		jog.Graphics.print("Turn " + String.valueOf(level.getTurn()), 0, 32);
	}

}
