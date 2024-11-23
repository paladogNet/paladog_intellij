package Main;

import java.util.List;
import javax.swing.JPanel;
import javax.swing.JLabel;


import DarkDog.DarkDog;
import DarkDog.Zombie;

public class ZombieSoHwan {

    // 좀비를 소환하는 메서드
    public void spawnZombie(JPanel panel, DarkDog darkdog, List<Zombie> zombielist, List<JLabel> Zombiehplabellist) {
        Zombie zombie = new Zombie();
        JLabel zombieHpLabel = new JLabel();

        // DarkDog 근처에 좀비 소환
        zombie.setLocation(darkdog.getX() - 50, darkdog.getY());
        zombieHpLabel.setLocation(zombie.getX(), zombie.getY() - 20);

        // 리스트에 추가
        zombielist.add(zombie);
        Zombiehplabellist.add(zombieHpLabel);

        // 패널에 추가
        panel.add(zombie);
        panel.add(zombieHpLabel);
        panel.repaint();

        // 좀비 이동 로직
        new Thread(() -> {
            try {
                while (zombie.getX() > 0) { // 화면 왼쪽으로 이동
                    zombie.MoveLeft();
                    Thread.sleep(1000); // 이동 속도 조절
                    zombie.setBounds(zombie.getX(), zombie.getY(), 50, 50);
                    zombieHpLabel.setLocation(zombie.getX(), zombie.getY() - 20);
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

