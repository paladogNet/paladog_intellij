//package Main;
//
//import java.awt.Graphics;
//import java.awt.Image;
//import java.awt.event.KeyAdapter;
//import java.awt.event.KeyEvent;
//import java.awt.event.MouseAdapter;
//import java.awt.event.MouseEvent;
//import java.awt.event.MouseListener;
//
//import javax.swing.ImageIcon;
//import javax.swing.JFrame;
//import javax.swing.JLabel;
//import javax.swing.JPanel;
//
//public class IntroImg extends JFrame {
//
//	private IntroImg introimg = this;
//	IntroPanel intropanel = new IntroPanel();
//
//	public IntroImg() {
//
//		setTitle("인트로이미지");
//		setSize(1130, 574);
//		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//		setLayout(null);
//		setLocationRelativeTo(null); // �������� �߾ӹ�ġ
//		setContentPane(intropanel);
//		listener();
//
//		setVisible(true);
//	}
//
//	public void listener() {
//		addMouseListener(new MouseAdapter() {
//			@Override
//			public void mouseClicked(MouseEvent e) {
//				new GamePanel();
//				introimg.setVisible(false);
//
//			}
//		});
//	}
//
//	class IntroPanel extends JPanel {
//		private ImageIcon icon = new ImageIcon("images/main_img3.png");
//		private Image img = icon.getImage();
//
//		public void paintComponent(Graphics g) {
//			super.paintComponent(g);
//
//			// �̹����� �г� ũ��� �����Ͽ� �׸���
//			g.drawImage(img, 0, 0, getWidth(), getHeight(), this);
//
//		}
//
//	}
//
//}