//해당 클래스는 모두 오픈소스를 참고하였습니다.

package Main;

import java.awt.Graphics;
import java.awt.Image;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class EndImg extends JFrame {

   EndPanel endpanel = new EndPanel();

   public EndImg() {

      setTitle("게임승리");
      setSize(1130, 574);
      setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      setLayout(null);
      setLocationRelativeTo(null);
      setContentPane(endpanel);
      

      setVisible(true);
   }

   class EndPanel extends JPanel {
      private ImageIcon icon = new ImageIcon("images/gameclear.jpg");
      private Image img = icon.getImage();

      public void paintComponent(Graphics g) {
         super.paintComponent(g);
         g.drawImage(img, 0, 0, getWidth(), getHeight(), this);

      }

   }

}
