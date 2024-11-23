package DarkDog;

import javax.swing.ImageIcon;
import javax.swing.JLabel;

import Main.GamePanel;
import PalaDog.Mouse;
import lombok.Data;

import java.util.ArrayList;

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

	public static void punchAttack(ArrayList<DarkDogPunch> punchlist, ArrayList<Mouse> mouse, GamePanel panel) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {
					try {
						Thread.sleep(1);
					} catch (InterruptedException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					try {
						for (int i = 0; i < punchlist.size(); i++) {
							for (int j = 0; j < mouse.size(); j++) {
								try {
									if (punchlist.get(i).getX() <= mouse.get(j).x + 100) {
										System.out.println("펀치맞음");

										mouse.get(j).hp = mouse.get(j).hp - punchlist.get(i).attack;

										panel.remove(punchlist.get(i));
										punchlist.remove(i);
										panel.repaint();


									}
								} catch (Exception e) {
									// TODO: handle exception
								}

							}

						}
					}catch (Exception e) {
						// TODO: handle exception
					}
				}
			}
		}).start();
	}

}