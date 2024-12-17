// 해당 클래스는 모두 오픈소스를 참고하였습니다.
// 제가 쥐를 소환하면 상대화면에서 좀비가 나오도록 하기위해 쥐의 스폰 위치만 수정하였습니다.

package PalaDog;

import java.awt.Color;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import DarkDog.DarkDog;
import DarkDog.Zombie;
import Main.GamePanel;

import lombok.Data;

public  class Mouse extends JLabel {
	public ImageIcon mouseIcon, mouse_attackicon, mouse_attackstopicon, ma;
	public Mouse mouse = this;
	public int x = 0;
	public int y = 240;
	public int hp = 30;
	public int attack = 5;
	public final static String TAG = "Mouse:";
	public boolean isMoving = true;

	public Mouse() {

		mouseIcon = new ImageIcon("images/mouse_walk.gif");
		mouse_attackicon = new ImageIcon("images/mouse_attackimg.gif");
		mouse_attackstopicon = new ImageIcon("images/mouse_stopicon.png");
		ma = new ImageIcon("images/ma.png");
		setIcon(mouseIcon);
		setSize(80, 80);
		setLocation(x, y);
		MoveLight();
	}

	public void MoveLight() {
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
					while (isMoving) {
						mouse.x += 10;
						setLocation(x, y);
						try {
							Thread.sleep(200);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}
		}).start();

	}
	
	public static void Mouse_attack(ArrayList<Mouse> mouse, ArrayList<Zombie> zombie, DarkDog darkdog) {

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
						for (int i = 0; i < mouse.size(); i++) {
							for (int j = 0; j < zombie.size(); j++) {
								try {
									if (mouse.get(i).x >= zombie.get(j).x - 100 && zombie.get(j).getX() <= mouse.get(i).getX()+100) {
										System.out.println("false");
										mouse.get(i).isMoving = false;
										mouse.get(i).setIcon(mouse.get(i).mouse_attackstopicon);
										Thread.sleep(300);

										mouse.get(i).setIcon(mouse.get(i).ma);

										Thread.sleep(300);
										// zombie.get(i).hp = zombie.get(i).hp - mouse.get(j).attack;

										try {
											zombie.get(j).hp = zombie.get(j).hp - mouse.get(i).attack;
										} catch (Exception e) {
											// TODO: handle exception
										}

									}
								} catch (Exception e) {
									// TODO: handle exception
								}

							}

						}

						for (int i = 0; i < mouse.size(); i++) {
								if (mouse.get(i).x >= darkdog.getX() - 100 && darkdog.getX() <= mouse.get(i).getX()+100) {
									System.out.println("");
									mouse.get(i).isMoving = false;
									mouse.get(i).setIcon(mouse.get(i).mouse_attackstopicon);
									Thread.sleep(300);
								try	{
									mouse.get(i).setIcon(mouse.get(i).ma);
								}catch (Exception e) {
									// TODO: handle exception
								}
									Thread.sleep(300);
									darkdog.hp = darkdog.hp - mouse.get(i).attack;
								} else {
									try {
										
										mouse.get(i).isMoving = true;
										mouse.get(i).setIcon(mouse.get(i).mouseIcon);
	
									} catch (Exception e) {
										// TODO: handle exception
									}
								
							}
						

						}

					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}

		}).start();
	}
	
	

}
