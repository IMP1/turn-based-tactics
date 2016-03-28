package run;

public class Main extends Game {
	
	public static final int PATCH_NUMBER = 0;
	public static final String VERSION = "0.0.0";

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
