package Main;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class EndImg extends JFrame {

   private EndImg endimg = this;
   EndPanel endpanel = new EndPanel();

   public EndImg() {

      setTitle("�ȶ󵶿���ȭ��");
      setSize(1130, 574);
      setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      setLayout(null);
      setLocationRelativeTo(null); // �������� �߾ӹ�ġ
      setContentPane(endpanel);
      

      setVisible(true);
   }

   class EndPanel extends JPanel {
      private ImageIcon icon = new ImageIcon("images/gameclear.jpg");
      private Image img = icon.getImage();

      public void paintComponent(Graphics g) {
         super.paintComponent(g);

         // �̹����� �г� ũ��� �����Ͽ� �׸���
         g.drawImage(img, 0, 0, getWidth(), getHeight(), this);

      }

   }

}


//    private void processServerMessage(String message) {
//        // 메시지 형식: "CLIENT_ID:ACTION:DATA" 또는 "CLIENT_ID:UPDATE_POSITION:x,y"
//
//
//        String[] parts = message.split(":");
//        if (parts.length < 3) return; // 형식이 잘못된 메시지는 무시
//
//        String senderId = parts[0]; // 메시지를 보낸 클라이언트 ID
//        String action = parts[1]; // 메시지의 동작 (예: MOVE_LEFT, UPDATE_POSITION)
//        String data = parts[2]; // 추가 데이터 (예: 유닛 정보 또는 좌표)
//
//        SwingUtilities.invokeLater(() -> {
//            if (senderId.equals(clientId)) {
//                // 자신의 캐릭터인 PalaDog에 대한 동작
//                switch (action) {
//                    case "MOVE_LEFT":
//                        gamePanel.getPaladog().moveLeft();
//                        break;
//                    case "MOVE_RIGHT":
//                        gamePanel.getPaladog().moveRight();
//                        break;
//                    case "ATTACK":
//                        gamePanel.punchAttack();
//                        break;
//                    case "SPAWN_UNIT":
//                        gamePanel.spawnUnit(data);
//                        break;
//                    default:
//                        break;
//                }
//            } else {
//                // 상대방 캐릭터인 DarkDog에 대한 동작
//                switch (action) {
//                    case "MOVE_LEFT":
//                        gamePanel.getDarkdog().moveLeft();
//                        break;
//                    case "MOVE_RIGHT":
//                        gamePanel.getDarkdog().moveRight();
//                        break;
//                    case "UPDATE_POSITION":
//                        // "x,y" 형식의 좌표 데이터 처리
//                        String[] coords = data.split(",");
//                        if (coords.length == 2) {
//                            try {
//                                int x = Integer.parseInt(coords[0].trim());
//                                int y = Integer.parseInt(coords[1].trim());
//                                gamePanel.updateDarkDogPosition(x, y); // DarkDog 위치 업데이트
//                            } catch (NumberFormatException e) {
//                                e.printStackTrace(); // 좌표 파싱 실패 시 예외 처리
//                            }
//                        }
//                        break;
//                    default:
//                        break;
//                }
//            }
//        });
//    }