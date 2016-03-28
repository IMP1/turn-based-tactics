package cls.unit;

import lib.Sprite;

public class DataSprite {
	public final String imagePath;
	public final int poses;
	public final int frames;
	public final double frameTime;
	
	public DataSprite(String imagePath, int poses, int frames, double frameTime) {
		this.imagePath = imagePath;
		this.poses     = poses;
		this.frames    = frames;
		this.frameTime = frameTime;
	}
	
	public Sprite newSprite() {
		return new Sprite(new jog.Image(imagePath), poses, frames, frameTime);
	}
	
}