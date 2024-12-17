package Main;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import DarkDog.DarkDog;
import DarkDog.Zombie;
import PalaDog.Bear;
import PalaDog.Mouse;
import PalaDog.PalaDog;
import PalaDog.PalaDogPunch;
import DarkDog.DarkDogPunch;

public class GamePanel extends JPanel  {
	private Mouse mouse;
	private Bear bear;
	private Zombie zombie;
	private PalaDog paladog;
	private PalaDogPunch palaDogPunch;
	private DarkDogPunch darkDogPunch;
	private DarkDog darkdog;
	public boolean isEnding = true;
	private ZombieHpLabel zombiehplabel;
	private MouseHpLabel mousehplabel;
	private PalaDogHpLabel paladoghplabel;
	private DarkDogHpLabel darkdoghplabel;

	private ArrayList<Zombie> zombielist;
	private ArrayList<Mouse> mouselist;
	private ArrayList<Bear> bearlist;
	private ArrayList<PalaDogPunch> paladogpunchlist;
	public ArrayList<ZombieHpLabel> Zombiehplabellist;
	public ArrayList<MouseHpLabel> mousehplabellist;
	public ArrayList<BearHpLabel> bearhplist;
	private ArrayList<DarkDogPunch> darkdogpunchlist;
	public int sohwanhp = 0;
	public int skillmp = 0;


	//아래의 멤버변수들은 직접 추가한 변수들입니다.
	ImageIcon img;
	JPanel panel;
	JLabel bottom_imgLabel, goldLabel, mpLabel, hpLabel;

	public ImageIcon backicon = new ImageIcon("images/background_img.png");
	public Image backimg = backicon.getImage();

	//채팅을 위한 필드들
	private JTextPane chatArea; // 채팅 로그 표시
	private JTextField chatInput; // 사용자 입력 필드
	private JButton sendButton; // 메시지 전송 버튼
	private JButton sendImageButton; // 메시지 전송 버튼
	private DefaultStyledDocument document;


	// 생성자 안의 내용은 대부분 직접 추가했습니다.
	public GamePanel() {

		init();

		// 채팅을 위한 세팅
		setLayout(null); // 사용자 지정 레이아웃
		// 채팅 영역 구성
		document = new DefaultStyledDocument();
		chatArea = new JTextPane(document);
		chatArea.setEditable(false);

		JScrollPane chatScrollPane = new JScrollPane(chatArea);
		chatScrollPane.setBounds(730, 378, 400, 130); // 오른쪽 아래 위치 조정
		add(chatScrollPane);
		// 채팅 입력 필드
		chatInput = new JTextField();
		chatInput.setBounds(730, 510, 220, 30);
		add(chatInput);
        // 버튼
		sendButton = new JButton("전송");
		sendButton.setBounds(960, 510, 70, 30);
		add(sendButton);
        // 이미지전송 버튼
		sendImageButton = new JButton("이미지 전송");
		sendImageButton.setBounds(1040, 510, 70, 30);
		add(sendImageButton);

		setting();
		batch();
		listener();

		setVisible(true);

	}
	// 원래 채팅 메시지 추가 메서드였습니다. 이것을 printDisplay로 대체하였습니다.
	//	public void appendChatMessage(String message) {
	//		chatArea.append(message + "\n");
	//	}

	//직접 작성하였습니다. 채팅(문자열) 내용을 chatArea에 렌더링하기 위한 메소드입니다.
	public void printDisplay(String msg) {
		int len = chatArea.getDocument().getLength();

		try {
			document.insertString(len, msg + "\n", null);
		} catch (BadLocationException e){
			e.printStackTrace();
		}

		chatArea.setCaretPosition(len);
	}
	//직접 작성하였습니다. 채팅(문자열) 내용을 chatArea에 렌더링하기 위한 메소드입니다. 인자를 다르게 하여 오버로딩을 사용했습니다.
	public void printDisplay(ImageIcon icon) {
		chatArea.setCaretPosition(chatArea.getDocument().getLength());

		if(icon.getIconWidth() > 200){
			Image img = icon.getImage();
			Image changeImg = img.getScaledInstance(200, -1, Image.SCALE_SMOOTH);
			icon = new ImageIcon(changeImg);
		}

		chatArea.insertIcon(icon);
		printDisplay("");
		chatInput.setText("");
	}

	//채팅 기능에 필요하여 직접 추가한 getter 메소드들입니다.
	public String getChat() {
		String input = chatInput.getText();
		chatInput.setText(""); // 입력 필드 초기화
		return input;
	}
	public JTextField getChatInput() {
		return chatInput;
	}
	public JButton getSendButton() {
		return sendButton;
	}
	public JButton getSendImageButton() {
		return sendImageButton;
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
		paladogpunchlist = new ArrayList<>();
		darkdogpunchlist = new ArrayList<>();
		new ZombieSoHwan();

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
		paladog.punchAttack(paladogpunchlist, zombielist, darkdog,this);
		darkdog.punchAttack(darkdogpunchlist, mouselist, paladog,this);

		죽는스레드 죽는스레드 = new 죽는스레드();
		죽는스레드.start();


	}

	public void setting() {
		setPreferredSize(new Dimension(1130, 574)); // JPanel 크기 설정
		setLayout(null); // JPanel의 레이아웃을 null로 설정 (절대 위치)

		panel.setBounds(0, 0, 1500, 375); // panel의 위치와 크기 설정
		panel.setLayout(null);

		bottom_imgLabel.setIcon(img);
		bottom_imgLabel.setBounds(0, 372, 1500, 165);

		goldLabel.setBounds(10, 10, 150, 30);
		goldLabel.setForeground(Color.orange);
		goldLabel.setFont(new Font("", Font.PLAIN, 18));

		mpLabel.setBounds(10, 30, 150, 30);
		mpLabel.setForeground(Color.blue);
		mpLabel.setFont(new Font("", Font.PLAIN, 18));

		hpLabel.setBounds(200, 200, 200, 200);
		hpLabel.setForeground(Color.red);
		hpLabel.setFont(new Font("", Font.PLAIN, 30));
		hpLabel.setVisible(false);

		// 컴포넌트를 JPanel에 추가
		add(panel);
		add(bottom_imgLabel);
		add(goldLabel);
		add(mpLabel);
		add(hpLabel);
	}

	public void batch() {
		add(hpLabel);
		add(goldLabel);
		add(mpLabel);
		add(panel);
		panel.add(paladog);
		panel.add(paladoghplabel);
		panel.add(darkdog);
		panel.add(darkdoghplabel);
		add(bottom_imgLabel);

	}

	public void listener() {

		addKeyListener(new KeyAdapter() {
			private boolean isMouseCooldown = false; // Mouse 생성 쿨다운 플래그
			private boolean isPunchCooldown = false;

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

				// 3번 곰은 게임 운영로직상 제외하였습니다.
//				if (e.getKeyChar() == '3') {
//					new Thread(() -> {
//						if (sohwanhp > 30) {
//							sohwanhp -= 30;
//							bear = new Bear();
//							bearlist.add(bear);
//							bearhplabel = new BearHpLabel();
//							bearhplist.add(bearhplabel);
//							panel.add(bear);
//							panel.add(bearhplabel);
//						}
//					}).start();
//				}

				if (e.getKeyCode() == KeyEvent.VK_LEFT) {
					paladog.moveLeft();
				} else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
					paladog.moveRight();
				}

				//원래 오픈소스 코드에는 대문자 J가 이스터에그로 무한 스킬생성 버튼으로 되어있었습니다.
				//하지만 대문자 J를 입력해도 한번의 스킬생성만 하도록 수정했습니다.
				if (e.getKeyChar() == 'j' || e.getKeyChar() == 'J') {
					if (!isPunchCooldown) {
						if (skillmp > 10) {
							new Thread(() -> {
								synchronized (this) {
									if (skillmp > 10) {
										palaDogPunch = new PalaDogPunch();
										paladogpunchlist.add(palaDogPunch);

										palaDogPunch.Punchx = paladog.x + 50;
										palaDogPunch.Punchy = paladog.y + 50;

										palaDogPunch.setBounds(palaDogPunch.Punchx, palaDogPunch.Punchy, palaDogPunch.getWidth(), palaDogPunch.getHeight());
										panel.add(palaDogPunch);
										panel.repaint();

										palaDogPunch.moveRight();
										skillmp -= 10;
									}
								}
							}).start();

							isPunchCooldown = true;

							// 쿨다운 타이머 설정
							Timer punchCooldownTimer = new Timer(500, new ActionListener() {
								@Override
								public void actionPerformed(ActionEvent e) {
									isPunchCooldown = false; // 쿨다운 해제
								}
							});

							punchCooldownTimer.setRepeats(false); // 반복 실행 방지
							punchCooldownTimer.start(); // 타이머 시작
						} else {
							System.out.println("MP가 부족합니다.");
						}
					}
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

	// getter 메소드가 필요하여 직접 작성하였습니다.
	public PalaDog getPaladog() {
		return this.paladog ;
	}
	public DarkDog getDarkdog() { return this.darkdog ; }

	public int getPaladogX() {
		return this.paladog.x;
	}

//	public JPanel getPanel() {
//		return panel;
//	}
//
//	public ArrayList<Zombie> getZombielist() {
//		return zombielist;
//	}
//
//	public Object getZombiehplabellist() {
//		return Zombiehplabellist;
//	}

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

					for (int i = 0; i < darkdogpunchlist.size(); i++) {
						if(darkdogpunchlist.get(i).getX() < 0) {
							panel.remove(paladogpunchlist.get(i));
							darkdogpunchlist.remove(i);
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
						goldLabel.setText("gold point : "+sohwanhp + "/" + "40");

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
						mpLabel.setText("mana point : " + skillmp + "/" + "40");
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
	//직접 작성하였습니다.
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

		}
	}

	//직접 작성하였습니다.
	public void spawnDarkDogPunch(){
		new Thread(() -> {
			synchronized (this) {
				if (skillmp > 10) {
					darkDogPunch = new DarkDogPunch();
					darkdogpunchlist.add(darkDogPunch);

					darkDogPunch.Punchx = darkdog.x - 50;
					darkDogPunch.Punchy = darkdog.y + 50;

					darkDogPunch.setBounds(darkDogPunch.Punchx, darkDogPunch.Punchy, darkDogPunch.getWidth(), darkDogPunch.getHeight());
					panel.add(darkDogPunch);
					panel.repaint();

					darkDogPunch.moveLeft();

				}
			}
		}).start();
	}

    //직접 작성하였습니다.
	public void spawnZombieForDarkDog() {
		Zombie zombie = new Zombie();
		ZombieHpLabel zombieHpLabel = new ZombieHpLabel();

		// DarkDog 근처에 좀비 소환
		zombie.setLocation(darkdog.x - 50, darkdog.y);
		zombieHpLabel.setLocation(zombie.x, zombie.y - 20);

		zombie = new Zombie();
		zombie.MoveLeft();
		zombielist.add(zombie);
		panel.add(zombie);
		zombiehplabel = new ZombieHpLabel();
		panel.add(zombiehplabel);
		Zombiehplabellist.add(zombiehplabel);
		System.out.println("좀비 소환" + zombielist.size());
	}

}