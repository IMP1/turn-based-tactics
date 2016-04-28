package scn.battleState;

import scn.Battle;
import scn.Battle.State;

public class SelectedLocation extends State {

	public SelectedLocation(Battle scene) {
		super(scene);
	}

	@Override
	public void mouseClick(int x, int y, int i, int j) {
		int action = scene.getClickedAction(x, y);
		if (action >= 0) {
			System.out.println("Selecting Action");
			setAction(action);
		} else {
			setNextState(new Idle(scene));
		}
	}

	@Override
	public void cancel() {
		setNextState(new SelectedUnit(scene));
	}

	@Override
	public void drawMap() {
		scene.drawActionWheel();
	}

	@Override
	public void drawScreen() {
		
	}

}
