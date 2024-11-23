package DarkDog;

import javax.swing.ImageIcon;
import javax.swing.JLabel;

import Main.GamePanel;
import lombok.Data;

@Data
public class DarkDog extends JLabel {
	public ImageIcon darkIcon, dark_attackicon;
	public DarkDog darkdog = this;
	public GamePanel gamepanel;
	public int x = 940;
	public int y = 190;
	public int hp = 100;
	public final static String TAG = "DarkDog:";
	public boolean isRight = false;
	public boolean isLeft = false;
	public boolean isMoving = true;

	public DarkDog() {

		darkIcon = new ImageIcon("images/darkdog.png");
		dark_attackicon = new ImageIcon("images/mouse_attackimg.gif");
		setIcon(darkIcon);
		setSize(190, 190);
		setLocation(x, y);
	
	}
	public void Right() {
		setIcon(darkIcon);
	}

	public void Letf() {
		setIcon(darkIcon);
	}

	public void moveRight() {
		if (true) {
			new Thread(new Runnable() {

				@Override
				public void run() {
					setIcon(darkIcon);
					isRight = true;
					while (isRight) {
						x++;
						setLocation(x, y); // 내부에 repaint() 존재
						try {

							Thread.sleep(10);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}).start();
		}
	}

	public void moveLeft() {
		if (!isLeft) {
			new Thread(new Runnable() {

				@Override
				public void run() {
					setIcon(darkIcon);
					isLeft = true;
					while (true) {
						x--;
						setLocation(x, y); // 내부에 repaint() 존재
						try {
							if (x < 0) {
								break;
							}
							Thread.sleep(10);

						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}).start();
		}
	}

}