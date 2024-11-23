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