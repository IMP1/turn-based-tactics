package scn.battleState;

import cls.unit.Unit.Action;
import run.Settings;
import scn.Battle;
import scn.Battle.State;

public class ActionWheel extends State {

	private Action selectedAction;
	
	public ActionWheel(Battle scene) {
		super(scene);
		selectedAction = null;
	}

	@Override
	public void mouseClick(int x, int y, int i, int j) {
		int action = scene.getClickedAction(x, y);
		if (action >= 0) {
			setAction(action);
			Action a = scene.getSelectedAction(); 
			switch (a) {
			case ATTACK:
				setNextState(new Attack(scene));
				return;
			case BUILD:
				break;
			case DEFEND:
				break;
			case LOAD:
				if (selectedAction == Action.LOAD || !Settings.askConfirmationOnLoad) {
					loadSelectedUnit();
					setNextState(new Idle(scene));
				} else {
					selectedAction = Action.LOAD;
				}
				return;
			case MOVE:
				if (selectedAction == Action.MOVE || !Settings.askConfirmationOnMove) {
					moveSelectedUnit();
					setNextState(new Idle(scene));
				} else {
					selectedAction = Action.MOVE;
				}
				return;
			case UNLOAD:
				showUnloadableUnits();
				setNextState(new Unload(scene));
				return;
			default:
				break;
			}
		} else {
			setNextState(new Idle(scene));
		}
	}

	@Override
	public void cancel() {
		unsetAction();
		hideActionWheel();
		setNextState(new SelectedUnit(scene));
	}

	@Override
	public void drawMap() {
		scene.drawActionWheel(selectedAction);
	}

	@Override
	public void drawScreen() {
		
	}

}
