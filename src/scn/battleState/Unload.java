package scn.battleState;

import cls.unit.Unit;
import run.Settings;
import scn.Battle;
import scn.Battle.State;

public class Unload extends State {
	
	private Unit unit;
	private int targetX;
	private int targetY;

	public Unload(Battle scene) {
		super(scene);
		unit = null;
		targetX = -1;
		targetY = -1;
	}

	@Override
	public void mouseClick(int x, int y, int i, int j) {
		if (unit == null) {
			int choice = scene.getClickedChoice(x, y);
			System.out.println(choice);
			if (choice >= 0) {
				setChosenUnit(choice);
				unit = scene.getChosenUnit();
			}
		} else {
			if ((targetX == i && targetY == j) || !Settings.askConfirmationOnAttack) {
				unloadUnit(unit, i, j);
				System.out.printf("Unloading %s at (%d, %d).\n", unit.name, targetX, targetY);
				hideUnloadableUnits();
				setNextState(new Idle(scene));
			} else {
				if (scene.canSelectedUnitUnload(i, j)) {
					targetX = i;
					targetY = j;
				}
			}
		}
	}

	@Override
	public void cancel() {
		unsetAction();
		hideUnloadableUnits();
		setNextState(new ActionWheel(scene));
	}

	@Override
	public void drawMap() {
		if (unit != null) {
			scene.drawUnloadLocations(targetX, targetY);
		}
	}

	@Override
	public void drawScreen() {
		scene.drawUnloadableUnits();
	}

}
