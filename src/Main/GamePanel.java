package Main;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import DarkDog.DarkDogPunch;
import DarkDog.DarkDog;
import DarkDog.Zombie;
import PalaDog.Bear;
import PalaDog.Mouse;
import PalaDog.PalaDog;
import PalaDog.PalaDogPunch;
import javax.swing.Timer;

public class GamePanel extends JFrame {
	private MyPanel m1, m2;
	private Mouse mouse;
	private Bear bear;
	private Zombie zombie;
	private PalaDog paladog;
	private PalaDogPunch punch;
	DarkDogPunch darkDogPunch = new DarkDogPunch();
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
	ArrayList<Mouse> mouselist;
	ArrayList<Bear> bearlist;
	ArrayList<PalaDogPunch> paladogpunchlist;
	ArrayList<DarkDogPunch> darkdogpunchlist;
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

		//
		paladogpunchlist = new ArrayList<>();
		darkdogpunchlist = new ArrayList<>();

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
		팔라독펀치어택(paladogpunchlist,zombielist,darkdog);
		다크독펀치어택(darkdogpunchlist,mouselist,bearlist,paladog);

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
			private boolean isMouseCooldown = false; // Mouse 생성 쿨다운 플래그

			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyChar() == '1') {
					// 쿨다운이 아니고 소환 포인트가 충분한 경우
					if (!isMouseCooldown) {
						if (sohwanhp > 10) {
							new Thread(() -> {
								sohwanhp -= 10;
								mouse = new Mouse();
								mousehplabel = new MouseHpLabel();
								mouselist.add(mouse);
								mousehplabellist.add(mousehplabel);
								panel.add(mousehplabel);
								panel.add(mouse);
								panel.repaint();
							}).start();

							// 쿨다운 활성화
							isMouseCooldown = true;

							// 쿨다운 타이머 설정
							Timer mouseCooldownTimer = new Timer(1000, new ActionListener() {
								@Override
								public void actionPerformed(ActionEvent e) {
									isMouseCooldown = false; // 쿨다운 해제
								}
							});

							mouseCooldownTimer.setRepeats(false); // 반복 실행 방지
							mouseCooldownTimer.start(); // 타이머 시작
						} else {
							System.out.println("소환 포인트가 부족합니다.");
						}
					} else {
						System.out.println("잠시 기다려주세요. (쿨다운 중)");
					}
				}

				if (e.getKeyChar() == '3') {
					new Thread(() -> {
						if (sohwanhp > 30) {
							sohwanhp -= 30;
							bear = new Bear();
							bearlist.add(bear);
							bearhplabel = new BearHpLabel();
							bearhplist.add(bearhplabel);
							panel.add(bear);
							panel.add(bearhplabel);
						}
					}).start();
				}

				if (e.getKeyCode() == KeyEvent.VK_LEFT) {
					paladog.moveLeft();
				} else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
					paladog.moveRight();
				}

				if (e.getKeyChar() == 'j') {
					new Thread(() -> {
						synchronized (this) {
							if (skillmp > 10) {
								punch = new PalaDogPunch();
								paladogpunchlist.add(punch);
								panel.add(punch);
								punch.moveRight();
								punch.Punchx = paladog.x + 50;
								punch.Punchy = paladog.y + 50;
								skillmp -= 10;
							}
						}
					}).start();
				} else if (e.getKeyChar() == 'J') {
					new Thread(() -> {
						synchronized (this) {
							punch = new PalaDogPunch();
							paladogpunchlist.add(punch);
							panel.add(punch);
							punch.moveRight();
							punch.Punchx = paladog.x + 50;
							punch.Punchy = paladog.y + 50;
						}
					}).start();
				}
			}

			@Override
			public void keyReleased(KeyEvent e) {
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

	public int getPaladogX() {
		return this.paladog.x;
	}

	public JPanel getPanel() {
		return panel;
	}

	public ArrayList<Zombie> getZombielist() {
		return zombielist;
	}

	public Object getZombiehplabellist() {
		return Zombiehplabellist;
	}

	public DarkDogHpLabel getDarkdogHpLabel() {
		return darkdoghplabel;
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
					
					for (int i = 0; i < paladogpunchlist.size(); i++) {
						if(paladogpunchlist.get(i).getX() >1000) {
							panel.remove(paladogpunchlist.get(i));
							paladogpunchlist.remove(i);
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
					darkdoghplabel.setLocation(darkdog.x + 65, darkdog.y - 70); /////////////////////// darkdog.x 라고 하면 안됨!다크독의 x,y는 안바뀌므로.
					//darkdoghplabel.setLocation(darkdog.x + 55, darkdog.y - 70);

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
	/////////////////////////////////////////////////////////////////////////////////////////////
//	class ZombieSoHwan extends Thread {
//		@Override
//		public void run() {
//			while (is좀비소환) {
//				zombie = new Zombie();
//				zombie.MoveLeft();
//				zombielist.add(zombie);
//				panel.add(zombie);
//				zombiehplabel = new ZombieHpLabel();
//				panel.add(zombiehplabel);
//				Zombiehplabellist.add(zombiehplabel);
//				System.out.println("좀비 소환" + zombielist.size());
//				try {
//					Thread.sleep(10000);
//				} catch (InterruptedException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}
//		}
//	}

	public class ZombieSoHwan {

		// 좀비를 소환하는 메서드
		public void spawnZombie(JPanel panel, DarkDog darkdog, ArrayList<Zombie> zombielist, ArrayList<ZombieHpLabel> Zombiehplabellist) {
			// 새로운 좀비와 좀비 HP 라벨 생성
			Zombie zombie = new Zombie();
			ZombieHpLabel zombieHpLabel = new ZombieHpLabel();

			// DarkDog 근처에 좀비 소환
			zombie.setLocation(darkdog.getX() - 50, darkdog.getY());
			zombieHpLabel.setLocation(zombie.getX(), zombie.getY() - 20);

			// 좀비와 라벨을 리스트 및 패널에 추가
			zombielist.add(zombie);
			Zombiehplabellist.add(zombieHpLabel);

			panel.add(zombie);
			panel.add(zombieHpLabel);
			panel.repaint();

			System.out.println("좀비 소환: 현재 좀비 개수 = " + zombielist.size());

			// 좀비 이동 로직
			new Thread(() -> {
				try {
					while (zombie.getX() > 0) { // 화면 왼쪽으로 이동
						zombie.MoveLeft();
						Thread.sleep(20); // 이동 속도 조절
						zombie.setBounds(zombie.getX(), zombie.getY(), 50, 50);
						zombieHpLabel.setLocation(zombie.getX(), zombie.getY() - 20);
					}

					// 화면 밖으로 나가면 좀비 제거
					panel.remove(zombie);
					panel.remove(zombieHpLabel);
					zombielist.remove(zombie);
					Zombiehplabellist.remove(zombieHpLabel);
					panel.repaint();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}).start();
		}
	}



//	public void 펀치어택(ArrayList<PalaDogPunch> punchlist, ArrayList<Zombie> zombie, DarkDog darkdog) {
//		new Thread(new Runnable() {
//			@Override
//			public void run() {
//				while (true) {
//					try {
//						Thread.sleep(1);
//					} catch (InterruptedException e1) {
//						// TODO Auto-generated catch block
//						e1.printStackTrace();
//					}
//					try {
//						for (int i = 0; i < punchlist.size(); i++) {
//							for (int j = 0; j < zombie.size(); j++) {
//								try {
//									if (punchlist.get(i).getX() >= zombie.get(j).x - 50) {
//										System.out.println("펀치맞음");
//
//										zombie.get(j).hp = zombie.get(j).hp - punchlist.get(i).attack;
//
//
//										panel.remove(punchlist.get(i));
//										punchlist.remove(i);
//										panel.repaint();
//
//
//									}
//								} catch (Exception e) {
//									// TODO: handle exception
//								}
//
//							}
//
//						}
//					}catch (Exception e) {
//						// TODO: handle exception
//					}
//				}
//			}
//		}).start();
//	}
public void 팔라독펀치어택(ArrayList<PalaDogPunch> punchlist, ArrayList<Zombie> zombie, DarkDog darkdog) {
	new Thread(new Runnable() {
		@Override
		public void run() {
			while (true) {
				try {
					Thread.sleep(1);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				try {
					for (int i = 0; i < punchlist.size(); i++) {
						// 좀비와 충돌 체크
						for (int j = 0; j < zombie.size(); j++) {
							try {
								if (punchlist.get(i).getX() >= zombie.get(j).x - 50) {
									System.out.println("좀비 펀치 맞음");
									zombie.get(j).hp -= punchlist.get(i).attack;

									// 펀치 제거
									panel.remove(punchlist.get(i));
									punchlist.remove(i);
									panel.repaint();
									break; // 이미 펀치를 제거했으므로 더 이상 처리하지 않음
								}
							} catch (Exception e) {
								e.printStackTrace();
							}
						}

						// 다크독과 충돌 체크
						try {
							if (punchlist.get(i).getX() >= darkdog.x - 50 && punchlist.get(i).getX() <= darkdog.x + 50) {
								System.out.println("다크독 펀치 맞음");
								darkdog.hp -= punchlist.get(i).attack;

								// 펀치 제거
								panel.remove(punchlist.get(i));
								punchlist.remove(i);
								panel.repaint();
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}).start();
}

	public void 다크독펀치어택(ArrayList<DarkDogPunch> punchlist, ArrayList<Mouse> mouselist, ArrayList<Bear> bearlist, PalaDog paladog) {
		new Thread(() -> {
			while (true) {
				try {
					Thread.sleep(1); // 공격 딜레이 조정
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				try {
					for (int i = 0; i < punchlist.size(); i++) {
						// 쥐와 충돌 체크
						for (int j = 0; j < mouselist.size(); j++) {
							try {
								if (punchlist.get(i).getX() >= mouselist.get(j).x - 50 && punchlist.get(i).getX() <= mouselist.get(j).x + 50) {
									System.out.println("쥐가 다크독 펀치 맞음");
									mouselist.get(j).hp -= punchlist.get(i).attack;

									// 유닛 체력 0일 시 제거
									if (mouselist.get(j).hp <= 0) {
										panel.remove(mouselist.get(j));
										mouselist.remove(j);
									}

									// 펀치 제거
									panel.remove(punchlist.get(i));
									punchlist.remove(i);
									panel.repaint();
									break; // 이미 펀치를 제거했으므로 루프 종료
								}
							} catch (Exception e) {
								e.printStackTrace();
							}
						}

						// 곰과 충돌 체크
						for (int j = 0; j < bearlist.size(); j++) {
							try {
								if (punchlist.get(i).getX() >= bearlist.get(j).x - 50 && punchlist.get(i).getX() <= bearlist.get(j).x + 50) {
									System.out.println("곰이 다크독 펀치 맞음");
									bearlist.get(j).hp -= punchlist.get(i).attack;

									// 유닛 체력 0일 시 제거
									if (bearlist.get(j).hp <= 0) {
										panel.remove(bearlist.get(j));
										bearlist.remove(j);
									}

									// 펀치 제거
									panel.remove(punchlist.get(i));
									punchlist.remove(i);
									panel.repaint();
									break; // 이미 펀치를 제거했으므로 루프 종료
								}
							} catch (Exception e) {
								e.printStackTrace();
							}
						}

						// 팔라독과 충돌 체크
						try {
							if (punchlist.get(i).getX() >= paladog.x - 50 && punchlist.get(i).getX() <= paladog.x + 50) {
								System.out.println("팔라독이 다크독 펀치 맞음");
								paladog.hp -= punchlist.get(i).attack;

								// 펀치 제거
								panel.remove(punchlist.get(i));
								punchlist.remove(i);
								panel.repaint();

								// 팔라독 체력 0일 시 게임 종료
								if (paladog.hp <= 0) {
									isEnding = false;
									new GameOver(); // 게임 종료 화면 표시
									setVisible(false);
								}
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
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
//	public void spawnDarkDogPunch(){
//		new Thread(() -> {
//			synchronized (this) {
//
//					darkDogPunch = new DarkDogPunch();
//					darkdogpunchlist.add(darkDogPunch);
//
//					panel.setLayout(null);
//					darkDogPunch.Punchx = darkdog.x - 50;
//					darkDogPunch.Punchy = darkdog.y + 50;
//					panel.add(darkDogPunch);
//					darkDogPunch.moveLeft();
//
//
//			}
//		}).start();
//	}

	public void spawnZombieForDarkDog() {
		Zombie zombie = new Zombie();
		ZombieHpLabel zombiehplabel = new ZombieHpLabel();

		// DarkDog 근처에 좀비 소환
		//zombie.setLocation(darkdog.x - 50, darkdog.y);
		//zombie.setLocation(100, darkdog.y);
		//zombieHpLabel.setLocation(zombie.x, zombie.y - 20);

		zombie.MoveLeft();
		zombielist.add(zombie);
		panel.add(zombie);
		panel.add(zombiehplabel);
		Zombiehplabellist.add(zombiehplabel);
		System.out.println("좀비 소환" + zombielist.size());

//		// 좀비 이동 로직
//		new Thread(() -> {
//			try {
//				while (zombie.x > 0) { // 화면 왼쪽으로 이동
//					zombie.MoveLeft();
//					Thread.sleep(1000); // 이동 속도 조절
//					zombie.setBounds(zombie.x, zombie.y, 90, 90);
//					zombieHpLabel.setLocation(zombie.x, zombie.y - 20);
//				}
//
//				// 화면 밖으로 나가면 좀비 제거
//				panel.remove(zombie);
//				panel.remove(zombieHpLabel);
//				zombielist.remove(zombie);
//				Zombiehplabellist.remove(zombieHpLabel);
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//		}).start();
	}

}