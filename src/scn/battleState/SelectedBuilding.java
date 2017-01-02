package scn.battleState;

import scn.Battle;
import scn.Battle.State;

public class SelectedBuilding extends State {

	public SelectedBuilding(Battle scene) {
		super(scene);
	}

	@Override
	public void mouseClick(int x, int y, int i, int j) {
		
	}

	@Override
	public void cancel() {
		setNextState(new Idle(scene));
	}

	@Override
	public void drawMap() {}

	@Override
	public void drawScreen() {
		// scene.drawBuildingOptions();
	}

	@Override
	public String toString() {
		return "Selected Building";
	}
	
}
