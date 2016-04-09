package scn;

import java.awt.event.KeyEvent;

import jog.Graphics;

import run.Data;

public class Title extends Scene {

	jog.Image background;
	double scaleX;
	double scaleY;
	
	public Title(jog.Image background) {
		this.background = background;
	}
	
	@Override
	public void start() {
		scaleX = ((double)jog.Graphics.getWidth()) / background.getWidth();
		scaleY = ((double)jog.Graphics.getHeight()) / background.getHeight();
	}
	
	@Override
	public void update(double dt) {
		
	}
	
	@Override
	public void keyReleased(int key) {
		if (!Data.isFinished()) return;
		if (key == KeyEvent.VK_SPACE) {
			SceneManager.changeScene(new Battle());
		}
	}
	
	@Override
	public void draw() {
		Graphics.draw(background, 0, 0, scaleX, scaleY);
		Graphics.printCentred(Data.getMessage(), Graphics.getWidth() / 2, Graphics.getHeight() / 2);
	}

}
