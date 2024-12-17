//이 DarkDog 클래스는 punchAttack() 메소드 제외하고 전체 오픈소스를 사용했습니다.
package DarkDog;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import PalaDog.Mouse;
import Main.GamePanel;
import lombok.Data;
import java.util.ArrayList;
import PalaDog.PalaDog;

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

	// 팔라독의 punchAttack()을 참고하여 작성했습니다.
	public static void punchAttack(ArrayList<DarkDogPunch> punchlist, ArrayList<Mouse> mouse, PalaDog paladog, GamePanel panel) {
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
							try {
								if (punchlist.get(i).getX() <= paladog.x + 50) {
									System.out.println("팔라독 펀치 맞음");
									paladog.hp -= punchlist.get(i).attack;

									// 펀치 제거
									panel.remove(punchlist.get(i));
									punchlist.remove(i);
									panel.repaint();
								}
							} catch (Exception e) {
								e.printStackTrace();
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