package scn.battleState;

import cls.unit.Unit;
import scn.Battle;

public class Idle extends Battle.State {

	public Idle(Battle scene) {
		super(scene);
	}

	@Override
	public void mouseClick(int x, int y, int i, int j) {
		Unit u = level.getUnitAt(i, j);
		if (u != null) {
			setSelectedUnit(u);
			setNextState(new SelectedUnit(scene));
		}
	}

	@Override
	public void cancel() {}

	@Override
	public void drawMap() {}

	@Override
	public void drawScreen() {}

}
