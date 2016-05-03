package scn.battleState;

import cls.unit.Unit;
import run.Settings;
import scn.Battle;
import scn.Battle.State;

public class Attack extends State {
	
	private Unit target;

	public Attack(Battle scene) {
		super(scene);
		target = null;
	}

	@Override
	public void mouseClick(int x, int y, int i, int j) {
		Unit enemy = level.getUnitAt(i, j);
		if (target == enemy  || !Settings.askConfirmationOnAttack) {
			attack(i, j);
			setNextState(new Idle(scene));
		} else {
			if (level.areEnemies(enemy.getOwner(), scene.getSelectedUnit().getOwner())) {
				target = enemy;
			}
		}
	}

	@Override
	public void cancel() {
		unsetAction();
		setNextState(new ActionWheel(scene));
	}

	@Override
	public void drawMap() {
		scene.drawAttackableUnits(target);
	}

	@Override
	public void drawScreen() {
		if (target != null)	scene.drawAttackInfo(target);
	}

}
