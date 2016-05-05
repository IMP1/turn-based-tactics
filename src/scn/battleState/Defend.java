package scn.battleState;

import cls.unit.Unit;
import run.Settings;
import scn.Battle;
import scn.Battle.State;

public class Defend extends State {
	
	private int targetX;
	private int targetY;

	public Defend(Battle scene) {
		super(scene);
		unit = null;
		targetX = -1;
		targetY = -1;
	}

	@Override
	public void mouseClick(int x, int y, int i, int j) {
		if ((targetX == i && targetY == j) || !Settings.askConfirmationOnDefend) {
			// TODO make unit defend in that direction
			hideDefendableDirections();
			setNextState(new Idle(scene));
		} else if (false /* TODO  */) {
				targetX = i;
				targetY = j;
			}
		}
	}

	@Override
	public void cancel() {
		unsetAction();
		hideDefendableDirections();
		setNextState(new ActionWheel(scene));
	}

	@Override
	public void drawMap() {
		scene.drawDefendDirections();
	}

	@Override
	public void drawScreen() {}

}
