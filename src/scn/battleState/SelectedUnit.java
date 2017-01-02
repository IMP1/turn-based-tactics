package scn.battleState;

import scn.Battle;
import scn.Battle.State;

public class SelectedUnit extends State {

	public SelectedUnit(Battle scene) {
		super(scene);
	}

	@Override
	public void mouseClick(int x, int y, int i, int j) {
		if (scene.canSelectedUnitGetTo(i, j)) {
			setNextState(new ActionWheel(scene));
			showActionWheel();
		}
	}
	
	@Override
	public void cancel() {
		setSelectedUnit(null);
		resetToIdle();
	}

	@Override
	public void drawMap() {
		scene.drawSelectedUnitOptions();
	}

	@Override
	public void drawScreen() {}
	

	@Override
	public String toString() {
		return "Selected Unit";
	}

}
