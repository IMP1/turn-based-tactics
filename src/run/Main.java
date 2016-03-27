package run;

public class Main extends Game {

	public Main() {
		super(new scn.Battle());
	}
	
	@Override
	protected void setup() {
		jog.Filesystem.addLocation("gfx");
		jog.Filesystem.addLocation("dat");
		Data.load();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new Main();
	}
	

}
