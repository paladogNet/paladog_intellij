package Main;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import DarkDog.DarkDog;
import DarkDog.Zombie;
import PalaDog.Bear;
import PalaDog.Mouse;
import PalaDog.PalaDog;
import PalaDog.PalaDogPunch;
import DarkDog.DarkDogPunch;

public class GamePanel extends JFrame {
	private MyPanel m1, m2;
	private Mouse mouse;
	private Bear bear;
	private Zombie zombie;
	private PalaDog paladog;
	private PalaDogPunch palaDogPunch;
	private DarkDogPunch darkDogPunch;
	private DarkDog darkdog;
	private boolean count = true;
	public boolean isEnding = true;
	public boolean is좀비소환 = true;
	public boolean is라벨무빙 = false;
	private ZombieHpLabel zombiehplabel;
	private MouseHpLabel mousehplabel;
	private BearHpLabel bearhplabel;
	private PalaDogHpLabel paladoghplabel;
	private DarkDogHpLabel darkdoghplabel;

	public GamePanel gamepanel;
	private ArrayList<Zombie> zombielist;
	private ArrayList<Mouse> mouselist;
	private ArrayList<Bear> bearlist;
	private ArrayList<PalaDogPunch> punchlist;
	private ArrayList<DarkDogPunch> darkpunchlist;
	public ArrayList<ZombieHpLabel> Zombiehplabellist;
	public ArrayList<MouseHpLabel> mousehplabellist;
	public ArrayList<BearHpLabel> bearhplist;
	public int sohwanhp = 0;
	public int skillmp = 0;

	ImageIcon img;
	JPanel panel;
	JLabel bottom_imgLabel, goldLabel, mpLabel, hpLabel;

	public int back1X = 0;
	public ImageIcon backicon = new ImageIcon("images/background_img.png");
	public Image backimg = backicon.getImage();
	public int back2X = backimg.getWidth(null);

	public GamePanel() {

		init();
		setting();
		batch();
		listener();

		setVisible(true);

	}

	public void init() {

		panel = new MyPanel();

		img = new ImageIcon("images/mainbottom4.jpg");

		bottom_imgLabel = new JLabel();
		goldLabel = new JLabel("0/40");
		mpLabel = new JLabel("0/40");
		hpLabel = new JLabel("100");
		mouselist = new ArrayList<Mouse>();
		zombielist = new ArrayList<Zombie>();
		Zombiehplabellist = new ArrayList<>();
		mousehplabellist = new ArrayList<>();
		bearlist = new ArrayList<Bear>();
		bearhplist = new ArrayList<>();
		punchlist = new ArrayList<>();
		darkpunchlist = new ArrayList<>();
		ZombieSoHwan zombiesohwan = new ZombieSoHwan();
		//zombiesohwan.start();

		GoldLabel goldLabel = new GoldLabel();
		goldLabel.start();

		SkillLabel skillLabel = new SkillLabel();
		skillLabel.start();

		paladog = new PalaDog();
		paladoghplabel = new PalaDogHpLabel();
		darkdog = new DarkDog();
		darkdoghplabel = new DarkDogHpLabel();

		HpLabelMoving hpLabelMoving = new HpLabelMoving();
		hpLabelMoving.start();
		
		mouse.Mouse_attack(mouselist, zombielist, darkdog);
		bear.Bear_attack(bearlist, zombielist, darkdog);
		zombie.Zombie_attack(mouselist, zombielist, paladog);
		zombie.Zombie_attack2(bearlist, zombielist);
		punchAttack(punchlist,zombielist);
		darkDogPunchAttack(darkpunchlist, mouselist);
		
		죽는스레드 죽는스레드 = new 죽는스레드();
		죽는스레드.start();

		
	}

	public void setting() {
		setSize(1130, 574);
		// setSize(760, 574);
		setLocationRelativeTo(null); // 프레임을 중앙배치
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		getContentPane().setLayout(null);
		getContentPane().add(panel);
		// panel.setBounds(0, 0, 743, 375);
		panel.setBounds(0, 0, 1500, 375);
		panel.setLayout(null);
		bottom_imgLabel.setIcon(img);
		// bottom_imgLabel.setBounds(0, 372, 743, 165);
		bottom_imgLabel.setBounds(0, 372, 1500, 165);

		goldLabel.setBounds(812, 480, 57, 30);
		getContentPane().add(goldLabel);
		goldLabel.setForeground(Color.orange);
		goldLabel.setFont(new Font("", Font.PLAIN, 18));

		mpLabel.setBounds(995, 480, 57, 30);
		mpLabel.setForeground(Color.blue);
		mpLabel.setFont(new Font("", Font.PLAIN, 18));

		hpLabel.setBounds(200, 200, 200, 200);
		hpLabel.setForeground(Color.red);
		hpLabel.setFont(new Font("", Font.PLAIN, 30));
		hpLabel.setVisible(false);
	}

	public void batch() {
		getContentPane().add(hpLabel);
		getContentPane().add(goldLabel);
		getContentPane().add(mpLabel);
		getContentPane().add(panel);
		panel.add(paladog);
		panel.add(paladoghplabel);
		panel.add(darkdog);
		panel.add(darkdoghplabel);
		getContentPane().add(bottom_imgLabel);

	}

	public void listener() {

		addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyChar() == '1') {
					new Thread(new Runnable() {

						@Override
						public void run() {
							if (sohwanhp > 10) {

								sohwanhp -= 10;
								mouse = new Mouse();
								mousehplabel = new MouseHpLabel();
								mouselist.add(mouse);
								mousehplabellist.add(mousehplabel);
								panel.add(mousehplabel);
								panel.add(mouse);

							}

						}
					}).start();

				}

				if (e.getKeyChar() == '3') {
					new Thread(new Runnable() {

						@Override
						public void run() {
							if (sohwanhp > 30) {
								sohwanhp -= 30;
								bear = new Bear();
								bearlist.add(bear);
								bearhplabel = new BearHpLabel();
								bearhplist.add(bearhplabel);
								panel.add(bear);
								panel.add(bearhplabel);

							}
						}
					}).start();

				}

				if (e.getKeyCode() == KeyEvent.VK_LEFT) {
					paladog.moveLeft();

					// System.out.println("팔라독 x좌표 : " + paladog.x);

				} else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
					paladog.moveRight();

					// System.out.println("팔라독 x좌표 : " + paladog.x);
				}
				if (e.getKeyChar() == 'j') {

					new Thread(new Runnable() {

						@Override
						synchronized public void run() {
							if (skillmp > 10) {

								palaDogPunch = new PalaDogPunch();
								punchlist.add(palaDogPunch);
								panel.add(palaDogPunch);
								palaDogPunch.moveRight();
								palaDogPunch.Punchx = paladog.x+50;
								palaDogPunch.Punchy = paladog.y + 50;
								skillmp = skillmp - 10;
								

							}

						}
					}).start();

				} else if (e.getKeyChar() == 'J') {

					new Thread(new Runnable() {
						@Override
						synchronized public void run() {
							palaDogPunch = new PalaDogPunch();
							punchlist.add(palaDogPunch);
							panel.add(palaDogPunch);
							palaDogPunch.moveRight();
							palaDogPunch.Punchx = paladog.x+50;
							palaDogPunch.Punchy = paladog.y + 50;


						}
					}).start();
				}

			}

			@Override
			public void keyReleased(KeyEvent e) {
				// TODO Auto-generated method stub
				if (e.getKeyCode() == KeyEvent.VK_LEFT) {
					paladog.isLeft = false;
					paladog.Letf();

				}
				if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
					paladog.isRight = false;
					paladog.Right();

				}
			}
		});

	}

	public PalaDog getPaladog() {
		return this.paladog ;
	}
	public DarkDog getDarkdog() { return this.darkdog ; }

	public void punchAttack() {
		// 펀치 생성 및 추가
		PalaDogPunch punch = new PalaDogPunch();
		punchlist.add(punch);
		panel.add(punch);

		// 펀치 초기 위치 설정 (팔라독 위치 기준)
		punch.Punchx = paladog.x + 50; // 팔라독 오른쪽에서 시작
		punch.Punchy = paladog.y + 50;

		// 펀치 이동 스레드 시작
		new Thread(() -> {
			try {
				while (punch.Punchx < 1000) { // 화면 끝까지 이동
					punch.moveRight();
					Thread.sleep(10);
					punch.setBounds(punch.Punchx, punch.Punchy, 50, 50);

					// 적과의 충돌 체크
					for (int i = 0; i < zombielist.size(); i++) {
						if (punch.Punchx >= zombielist.get(i).x - 50 &&
								punch.Punchy >= zombielist.get(i).y - 50 &&
								punch.Punchy <= zombielist.get(i).y + 50) {

							// 좀비 데미지 처리
							zombielist.get(i).hp -= punch.attack;
							if (zombielist.get(i).hp <= 0) {
								panel.remove(zombielist.get(i));
								zombielist.remove(i);
							}

							// 펀치 제거
							panel.remove(punch);
							punchlist.remove(punch);
							return;
						}
					}
				}

				// 화면 밖으로 나가면 펀치 제거
				panel.remove(punch);
				punchlist.remove(punch);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}).start();
	}

	public void spawnUnit(String data) {
		switch (data) {
			case "MOUSE":
				if (sohwanhp >= 10) {
					sohwanhp -= 10;
					Mouse mouse = new Mouse();
					MouseHpLabel mouseHpLabel = new MouseHpLabel();

					// 유닛과 HP 라벨 추가
					mouselist.add(mouse);
					mousehplabellist.add(mouseHpLabel);

					panel.add(mouse);
					panel.add(mouseHpLabel);

					// 유닛 초기 위치 설정
					mouse.setLocation(paladog.x + 50, paladog.y);
					mouseHpLabel.setLocation(mouse.x + 30, mouse.y - 50);

					// 유닛 이동 및 공격 스레드 실행
					new Thread(() -> {
						try {
							while (mouse.x < 1000) {
								mouse.MoveLight();
								Thread.sleep(10);
								mouse.setBounds(mouse.x, mouse.y, 50, 50);
								mouseHpLabel.setLocation(mouse.x + 30, mouse.y - 50);

								// 적과의 충돌 체크
								for (int i = 0; i < zombielist.size(); i++) {
									if (mouse.x >= zombielist.get(i).x - 50) {
										zombielist.get(i).hp -= mouse.attack;
										if (zombielist.get(i).hp <= 0) {
											panel.remove(zombielist.get(i));
											zombielist.remove(i);
										}
										return; // 충돌 시 멈춤
									}
								}
							}

							// 화면 밖으로 나가면 유닛 제거
							panel.remove(mouse);
							mouselist.remove(mouse);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}).start();
				}
				break;

			case "BEAR":
				if (sohwanhp >= 30) {
					sohwanhp -= 30;
					Bear bear = new Bear();
					BearHpLabel bearHpLabel = new BearHpLabel();

					// 유닛과 HP 라벨 추가
					bearlist.add(bear);
					bearhplist.add(bearHpLabel);

					panel.add(bear);
					panel.add(bearHpLabel);

					// 유닛 초기 위치 설정
					bear.setLocation(paladog.x + 50, paladog.y);
					bearHpLabel.setLocation(bear.x + 55, bear.y - 47);

					// 유닛 이동 및 공격 스레드 실행
					new Thread(() -> {
						try {
							while (bear.x < 1000) {
								bear.moveRight();
								Thread.sleep(20);
								bear.setBounds(bear.x, bear.y, 70, 70);
								bearHpLabel.setLocation(bear.x + 55, bear.y - 47);

								// 적과의 충돌 체크
								for (int i = 0; i < zombielist.size(); i++) {
									if (bear.x >= zombielist.get(i).x - 50) {
										zombielist.get(i).hp -= bear.attack;
										if (zombielist.get(i).hp <= 0) {
											panel.remove(zombielist.get(i));
											zombielist.remove(i);
										}
										return; // 충돌 시 멈춤
									}
								}
							}

							// 화면 밖으로 나가면 유닛 제거
							panel.remove(bear);
							bearlist.remove(bear);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}).start();
				}
				break;

			default:
				System.out.println("Unknown unit type: " + data);
		}
	}



	public int getPaladogX() {
		return this.paladog.x;
	}


	class 죽는스레드 extends Thread {
		@Override
		public void run() {
			try {
				while (isEnding) {
					Thread.sleep(1);
					for (int i = 0; i < zombielist.size(); i++) {
						if (zombielist.get(i).hp <= 0 || zombielist.get(i).x < 0) {
							panel.remove(zombielist.get(i));
							panel.remove(Zombiehplabellist.get(i));
							zombielist.remove(i);
							Zombiehplabellist.remove(i);
							panel.repaint();
						}
					}
					for (int i = 0; i < mouselist.size(); i++) {
						if (mouselist.get(i).hp <= 0) {
							panel.remove(mouselist.get(i));
							panel.remove(mousehplabellist.get(i));
							mousehplabellist.remove(i);
							mouselist.remove(i);
							panel.repaint();
						}
					}
					for (int i = 0; i < bearlist.size(); i++) {
						if (bearlist.get(i).hp <= 0) {
							panel.remove(bearlist.get(i));
							panel.remove(bearhplist.get(i));
							bearhplist.remove(i);
							bearlist.remove(i);
							panel.repaint();
						}
					}
					
					for (int i = 0; i < punchlist.size(); i++) {
						if(punchlist.get(i).getX() >1000) {
							panel.remove(punchlist.get(i));
							punchlist.remove(i);
							panel.repaint();
							
						}
					}
					
					if(paladog.hp <=0) {
						isEnding=false;
						new GameOver();
						setVisible(false);
						
						
					}
					
					if(darkdog.hp <=0) {
						isEnding=false;
						new EndImg();
						setVisible(false);
					
						
					}
				}
			} catch (Exception e) {
				// TODO: handle exception
			}
		}
	}

	public class GoldLabel extends Thread {

		@Override
		public void run() {
			while (true) {
				try {
					Thread.sleep(1);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				while (sohwanhp < 40) {
					try {
						sohwanhp++;

//                  System.out.println(hp);
						goldLabel.setText(sohwanhp + "/" + "40");

						Thread.sleep(500);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

			}

		}
	}

	public class HpLabelMoving extends Thread {
		@Override
		public void run() {

			while (true) {

				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				try {
					paladoghplabel.setLocation(paladog.x + 80, paladog.y - 80);
					darkdoghplabel.setLocation(darkdog.x + 55, darkdog.y - 70);
				} catch (Exception e) {
					// TODO: handle exception
				}

				try {
					paladoghplabel.setText(paladog.hp + "");
					darkdoghplabel.setText(darkdog.hp + "");
				} catch (Exception e) {
					// TODO: handle exception
				}

				for (int i = 0; i < mouselist.size(); i++) {
					try {
						Thread.sleep(1);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					try {
						mousehplabellist.get(i).setLocation(mouselist.get(i).x + 30, mouselist.get(i).y - 50);
						mousehplabellist.get(i).setText(mouselist.get(i).hp + "");

					} catch (Exception e) {
						// TODO: handle exception
					}

				}

				for (int i = 0; i < zombielist.size(); i++) {
					try {
						Thread.sleep(1);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					try {
						Zombiehplabellist.get(i).setLocation(zombielist.get(i).x + 30, zombielist.get(i).y - 50);
					} catch (Exception e) {
						// TODO: handle exception
					}

					try {
						Zombiehplabellist.get(i).setText(zombielist.get(i).hp + "");
					} catch (Exception e) {
						// TODO: handle exception
					}

				}
//   
				for (int i = 0; i < bearlist.size(); i++) {
					try {
						Thread.sleep(1);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					try {
						bearhplist.get(i).setLocation(bearlist.get(i).x + 55, bearlist.get(i).y - 47);
					} catch (Exception e) {
						// TODO: handle exception
					}

					try {
						bearhplist.get(i).setText(bearlist.get(i).hp + "");
					} catch (Exception e) {
						// TODO: handle exception
					}

				}
			}
		}

	}

	public class SkillLabel extends Thread {

		@Override
		public void run() {
			while (true) {
				try {
					Thread.sleep(1);
				} catch (InterruptedException e1) {

					e1.printStackTrace();
				}
				while (skillmp < 40) {
					try {
						skillmp++;
						mpLabel.setText(skillmp + "/" + "40");
						Thread.sleep(300);
					} catch (InterruptedException e) {

						e.printStackTrace();
					}
				}
			}

		}
	}

	public class PalaDogHpLabel extends JLabel {
		public int x;
		public int y;

		public PalaDogHpLabel() {

			setForeground(Color.red);
			setSize(200, 200);
			setLocation(x, y);

		}

	}

	public class DarkDogHpLabel extends JLabel {
		public int x;
		public int y;

		public DarkDogHpLabel() {

			setForeground(Color.red);
			setSize(200, 200);
			setLocation(x, y);

		}

	}

	public class MouseHpLabel extends JLabel {
		public int x;
		public int y;

		public MouseHpLabel() {

			setForeground(Color.orange);
			setSize(80, 80);
			setLocation(x, y);

		}

	}
	public class ZombieHpLabel extends JLabel {
		public int x;
		public int y;

		public ZombieHpLabel() {

			setForeground(Color.cyan);
			setSize(80, 80);
			setLocation(x, y);

		}
	}
	public class BearHpLabel extends JLabel {
		public int x;
		public int y;

		public BearHpLabel() {

			setForeground(Color.orange);
			setSize(80, 80);
			setLocation(x, y);

		}
	}
	class ZombieSoHwan extends Thread {
		@Override
		public void run() {
			while (is좀비소환) {
				zombie = new Zombie();
				zombie.MoveLeft();
				zombielist.add(zombie);
				panel.add(zombie);
				zombiehplabel = new ZombieHpLabel();
				panel.add(zombiehplabel);
				Zombiehplabellist.add(zombiehplabel);
				System.out.println("좀비 소환" + zombielist.size());
				try {
					Thread.sleep(10000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	public void punchAttack(ArrayList<PalaDogPunch> punchlist, ArrayList<Zombie> zombie) {
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
							for (int j = 0; j < zombie.size(); j++) {
								try {
									if (punchlist.get(i).getX() >= zombie.get(j).x - 50) {
										System.out.println("펀치맞음");
										
										zombie.get(j).hp = zombie.get(j).hp - punchlist.get(i).attack;
										
										
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

	public void darkDogPunchAttack(ArrayList<DarkDogPunch> punchlist, ArrayList<Mouse> mouse) {
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
									if (punchlist.get(i).getX() <= mouse.get(j).x + 50) {
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

	/**
	 * DarkDog의 위치를 업데이트하는 메서드.
	 * 서버에서 받은 메시지를 바탕으로 x, y 좌표를 업데이트합니다.
	 *
	 * @param x DarkDog의 새로운 x 좌표
	 * @param y DarkDog의 새로운 y 좌표
	 */
	public void updateDarkDogPosition(int x, int y) {
		darkdog.setLocation(x, y); // DarkDog의 위치 업데이트
		repaint(); // UI 갱신
	}
	// DarkDog의 현재 x, y 좌표를 반환
	public int getDarkDogX() {
		return darkdog.getX();
	}
	public int getDarkDogY() {
		return darkdog.getY();
	}

	class MyPanel extends JPanel {
		public boolean isBackMoving = false;

		public MyPanel() {

		}

		@Override
		public void paintComponent(Graphics g) {

			super.paintComponent(g);

			g.drawImage(backimg, 0, 0, 1115, 375, this);
			// g.drawImage(backimg, back2X, 0, this);
		}
	}

	public void spawnZombieForDarkDog() {
		Zombie zombie = new Zombie();
		ZombieHpLabel zombieHpLabel = new ZombieHpLabel();

		// DarkDog 근처에 좀비 소환
		zombie.setLocation(darkdog.x - 50, darkdog.y);
		zombieHpLabel.setLocation(zombie.x, zombie.y - 20);

		zombielist.add(zombie);
		Zombiehplabellist.add(zombieHpLabel);

		panel.add(zombie);
		panel.add(zombieHpLabel);
		panel.repaint();

		// 좀비 이동 로직
		new Thread(() -> {
			try {
				while (zombie.x > 0) { // 화면 왼쪽으로 이동
					zombie.MoveLeft();
					Thread.sleep(20); // 이동 속도 조절
					zombie.setBounds(zombie.x, zombie.y, 50, 50);
					zombieHpLabel.setLocation(zombie.x, zombie.y - 20);
				}

				// 화면 밖으로 나가면 좀비 제거
				panel.remove(zombie);
				panel.remove(zombieHpLabel);
				zombielist.remove(zombie);
				Zombiehplabellist.remove(zombieHpLabel);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}).start();
	}

}