package cls;

public abstract class GameObject {

	public final String name;
	protected int x;
	protected int y;
	
	public int getX() { return x; }
	public int getY() { return y; }
	public boolean isAt(int x, int y) {
		return x == this.x && y == this.y;
	}
	
	public GameObject(String name) {
		this.name = name;
	}
	
	/**
	 * This is run at the beginning of the owner player's turn.
	 * It prepares the game object for another turn.
	 */
	public abstract void refresh();
	
	/**
	 * This is run each frame of the game.
	 * @param dt the time since the last update.
	 */
	public abstract void update(double dt);
	
	/**
	 * This renders the object.
	 */
	public abstract void draw();

}
