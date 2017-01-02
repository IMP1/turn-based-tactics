package run;

import java.util.ArrayList;
import java.util.Arrays;

public class Main extends Game {
	
	public  static final int      PATCH_NUMBER = 0;
	public  static final String   VERSION = "0.0.0";

	private static final String[] TITLE_BACKGROUNDS = getTitleBackgrounds();
	private static final String   TITLE = "Battleground";
	private static final int      WIDTH = 800;
	private static final int      HEIGHT = 600;
	
	public static jog.Image titleBackground;
	
	static {
		// DEBUGGING CODE TODO: have actual settings menu.
		Settings.askConfirmationOnMove = false;
		Settings.askConfirmationOnLoad = false;
	}
	
	public Main() {
		super(new scn.Title(titleBackground), TITLE, WIDTH, HEIGHT);
	}
	
	private static String[] getTitleBackgrounds() {
		ArrayList<String> titleBackgrounds = new ArrayList<String>();
		String[] modFolders = jog.Filesystem.readFile("mods/.loadorder").split("\n");
		for (String mod : modFolders) {
			if (jog.Filesystem.getFile("mods/" + mod).exists()) {
				String[] modTitleBackgrounds = jog.Filesystem.enumerate("mods/" + mod + "/resources/title");
				for (int i = 0; i < modTitleBackgrounds.length; i ++) {
					modTitleBackgrounds[i] = "mods/" + mod + "/resources/title/" + modTitleBackgrounds[i]; 
				}
				titleBackgrounds.addAll(Arrays.asList(modTitleBackgrounds));
			}
		}
		return titleBackgrounds.toArray(new String[0]);
	}
	
	@Override
	protected void setup() {
		Data.load();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (TITLE_BACKGROUNDS.length >= 1) {
			int i = (int)(Math.random() * TITLE_BACKGROUNDS.length);
			titleBackground = new jog.Image(TITLE_BACKGROUNDS[i]);
		}
		new Main();
	}

}
