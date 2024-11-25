package DarkDog;

import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JLabel;

import Main.GamePanel;

public class DarkDogPunch extends JLabel {
    public GamePanel gamePanel;
    public DarkDogPunch punch = this;
    public ImageIcon icPunch;
    public int Punchx;
    public int Punchy;
    public boolean isPunch = true;
    public int attack = 15; // 다크독 펀치의 공격력

    // 생성자: 다크독 펀치 초기화
    public DarkDogPunch() {
        icPunch = new ImageIcon("images/PalaDogPunch.jpg"); // 다크독 펀치 이미지 경로
        setSize(80, 80); // 펀치 크기 설정
        setIcon(icPunch); // 아이콘 설정
    }

    // 펀치를 왼쪽으로 이동
    public void moveLeft() {
        // 펀치 리스트 생성
        ArrayList<DarkDogPunch> punchList = new ArrayList<>();
        punchList.add(punch);

        // 펀치 이동을 위한 스레드 실행
        new Thread(() -> {
            while (isPunch) {
                Punchx = Punchx - 10; // x 좌표 감소 (왼쪽으로 이동)
                try {
                    Thread.sleep(20); // 이동 속도 조정
                    setLocation(Punchx, Punchy); // 펀치의 위치 갱신
                    for (int i = 0; i < punchList.size(); i++) {
                        // 디버깅용 출력
                        // System.out.println("펀치의 위치: x = " + punchList.get(i).getX());
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
