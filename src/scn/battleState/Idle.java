package scn.battleState;

import cls.building.Building;
import cls.unit.Unit;
import scn.Battle;

public class Idle extends Battle.State {

	public Idle(Battle scene) {
		super(scene);
	}

	@Override
	public void mouseClick(int x, int y, int i, int j) {
		Unit u = level.getUnitAt(i, j);
		if (u != null && u.canMove() && u.getOwner() == level.getCurrentPlayer()) {
			setSelectedUnit(u);
			setNextState(new SelectedUnit(scene));
			return;
		}
		Building b = level.getBuildingAt(i, j);
		if (b != null && b.getOwner() == level.getCurrentPlayer()) {
			setSelectedBuidling(b);
			setNextState(new SelectedBuilding(scene));
			return;
		}
	}

	@Override
	public void cancel() {}

	@Override
	public void drawMap() {}

	@Override
	public void drawScreen() {}
	

	@Override
	public String toString() {
		return "Idle";
	}

}
