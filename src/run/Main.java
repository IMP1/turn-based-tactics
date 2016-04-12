package run;

public class Main extends Game {
	
	public static final int PATCH_NUMBER = 0;
	public static final String VERSION = "0.0.0";

	private static final String[] TITLE_BACKGROUNDS = jog.Filesystem.enumerate("/gfx/title");
	private static final String TITLE = "Battleground";
	private static final int WIDTH = 800;
	private static final int HEIGHT = 600;
	
	public static jog.Image titleBackground;
	
	public Main() {
		super(new scn.Title(titleBackground), TITLE, WIDTH, HEIGHT);
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
		int i = (int)(Math.random() * TITLE_BACKGROUNDS.length);
		titleBackground = new jog.Image("gfx/title/" + TITLE_BACKGROUNDS[i]);
		new Main();
	}
	

}
