package scn.battleState;

import run.Settings;
import scn.Battle;
import scn.Battle.State;

public class Defend extends State {
	
	private int direction;

	public Defend(Battle scene) {
		super(scene);
		direction = -1;
	}

	@Override
	public void mouseClick(int x, int y, int i, int j) {
		if ((direction == getDirection(i, j)) || !Settings.askConfirmationOnDefend) {
			// TODO make unit defend in that direction
			System.out.println("Defending!");
			hideDefendableDirections();
			setNextState(new Idle(scene));
		} else if (validClick(i, j)) {
			direction = getDirection(i, j);
		} else if (direction > -1) {
			direction = -1;
		} else {
			cancel();
		}
	}
	
	private boolean validClick(int i, int j) {
		return true;
	}
	
	private int getDirection(int i, int j) {
		int[] pos = getSelectedPosition();
		int x = pos[0]; 
		int y = pos[1];
		double r = Math.atan2(j - y, i - x);
		if (r < 0) r += 2 * Math.PI;
		return (int)(r * 4 / Math.PI);
	}

	@Override
	public void cancel() {
		if (direction != -1) {
			direction = -1;
			return;
		}
		unsetAction();
		hideDefendableDirections();
		setNextState(new ActionWheel(scene));
	}

	@Override
	public void drawMap() {
		scene.drawDefendDirections(direction);
	}

	@Override
	public void drawScreen() {}

}
