package DarkDog;

import Main.GamePanel;

import javax.swing.*;
import java.util.ArrayList;

public class DarkDogPunch extends JLabel {
	public GamePanel gamepanel;
	public DarkDogPunch punch = this;
	public ImageIcon icPunch;
	public int Punchx;
	public int Punchy;
	public boolean isPunch = true;
	public int attack=10;
	public DarkDogPunch() {
		icPunch = new ImageIcon("images/DarkdogPunch.jpg");
		setSize(80, 80);
		setIcon(icPunch);
	}

	public void moveLeft() {
		ArrayList<DarkDogPunch> PList = new ArrayList<DarkDogPunch>();
		PList.add(punch);
		new Thread(new Runnable() {
			@Override
			public void run() {

				while (isPunch) {
					Punchx = Punchx - 10;
					try {
						
						Thread.sleep(20);
						setLocation(Punchx, Punchy);
						for (int i = 0; i < PList.size(); i++) {
//							System.out.println("��ġ��  i����ǥ"+PList.get(i).getX());
						}

					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

			}
		}).start();
	}
	
	
	
	
}
