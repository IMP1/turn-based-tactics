package cls.level;

import java.util.ArrayList;

import cls.unit.Unit;

public class BattleLog {

	public enum ActionType {
		UNIT_MOVE,
		UNIT_LOAD,
		UNIT_BATTLE,
		TURN_END,
	}
	
	private class Action {
		public final ActionType actionType;
		public final Object[]   params;
		
		private Action(ActionType type, Object... args) {
			actionType = type;
			params = args;
		}
	}
	
	private int startingPlayer;
	private ArrayList<Action> log;
	
	public BattleLog(int startingPlayer) {
		this.startingPlayer = startingPlayer;
		this.log = new ArrayList<Action>();
	}
	
	public void unitMove(Unit u, int oldX, int oldY, int newX, int newY) {
		Action a = new Action(ActionType.UNIT_MOVE, oldX, oldY, u.name, newX, newY);
		log.add(a);
	}

	public void unitLoad(Unit u, int oldX, int oldY, Unit transport, int tx, int ty) {
		Action a = new Action(ActionType.UNIT_LOAD, oldX, oldY, u.name, tx, ty, transport.name);
	}
	
	public void unloadUnit(Unit u, int oldX, int oldY, int newX, int newY) {
		
	}
	
	public void buildUnit(Unit u, int x, int y) {
		
	}
	
}
