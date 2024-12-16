package Main;

//2091273 홍해담

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.io.*;
import java.net.*;


public class WithTalk extends JFrame{
    private static final long serialVersionUID = 1L;
    private JTextField t_input, t_userID, t_hostAddr, t_portNum; //사용자 입력 텍스트필드
    private JTextPane t_display;                              //메시지를 표시하는 텍스트 영역
    private DefaultStyledDocument document;
    private JButton b_connect, b_disconnect, b_send, b_exit, b_select; //기능 버튼들

    private String serverAddress; //서버 주소는 내 컴퓨터이고
    private int serverPort;       //서버포트는 임의로 동적포트 49152~65535 중 하나로 설정
    private String uid;           //사용자 아이디

    private Socket socket;        //서버와의 연결을 위한 소켓
    private ObjectOutputStream out;//서버로 데이터를 보내는 스트림
    private ObjectInputStream in; //서버로부터 데이터를 받는 스트림

    private Thread receiveThread = null;   //메시지 수신을 처리할 스레드


    public WithTalk(String serverAddress, int serverPort){
        super("2091273 클라이언트 : With Talk");
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;

        //프레임 구성
        buildGUI();

        setSize(500, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }


    private void buildGUI() {
        //JPanel DisplayPanel = createDisplayPanel();
        //JPanel InputPanel = createInputPanel();
        JPanel ControlPanel = createControlPanel();
        JPanel InfoPanel = createInfoPanel();

        JPanel belowPanel = new JPanel();
        belowPanel.setLayout(new GridLayout(3, 0));
        //belowPanel.add(InputPanel);
        belowPanel.add(InfoPanel);
        belowPanel.add(ControlPanel);

        //this.add(DisplayPanel, BorderLayout.CENTER);
        this.add(belowPanel, BorderLayout.SOUTH);
    }

    //InfoPanel 생성
    private JPanel createInfoPanel(){
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        t_userID = new JTextField(7); //사용자 아이디 텍스트 필드
        t_hostAddr = new JTextField(12); //서버 주소 입력 텍스트 필드
        t_portNum = new JTextField(5); //서버 포트 입력하는 텍스트 필드

        t_userID.setText("guest"); // 기본 아이디 설정
        t_hostAddr.setText(this.serverAddress); // 기본 서버 주소 설정
        t_portNum.setText(String.valueOf(this.serverPort)); // 기본 서버 포트 설정

        panel.add(new JLabel("아이디: "));
        panel.add(t_userID);
        panel.add(new JLabel("서버주소: "));
        panel.add(t_hostAddr);
        panel.add(new JLabel("포트번호: "));
        panel.add(t_portNum);

        return panel;
    }

    //ControlPanel 생성
    private JPanel createControlPanel(){
        JPanel panel = new JPanel(new GridLayout(0, 3));
        b_connect = new JButton("접속하기"); //서버 접속 버튼
        b_disconnect = new JButton("접속끊기"); //서버 연결 해제 버튼
        b_exit = new JButton("종료하기"); //프로그램 종료 버튼

        b_disconnect.setEnabled(false); //초기에는 비활성화 상태

        b_connect.addActionListener(e -> connectToServer()); //접속하기 버튼 클릭 시 서버 연결
        b_disconnect.addActionListener(e -> disconnect()); //접속끊기 버튼 클릭 시 연결 해제
        b_exit.addActionListener(e -> System.exit(0)); //종료하기 버튼 클릭 시 프로그램 종료

        panel.add(b_connect);
        panel.add(b_disconnect);
        panel.add(b_exit);

        return panel;
    }




    private void printDisplay(ImageIcon icon){
        if (icon.getIconWidth() > 400) { //이미지가 너무 클 경우 크기를 조정함
            Image img = icon.getImage().getScaledInstance(400, -1, Image.SCALE_SMOOTH);
            icon = new ImageIcon(img);
        }
        t_display.insertIcon(icon);
    }



    //서버와 연결하는 메소드
    private void connectToServer(){
        try {
            socket = new Socket(serverAddress, serverPort); //서버에 연결
            out = new ObjectOutputStream(new BufferedOutputStream(socket.getOutputStream())); //출력 스트림 초기화
            in = new ObjectInputStream(new BufferedInputStream(socket.getInputStream())); //입력 스트림 초기화

            uid = t_userID.getText(); //사용자 아이디 설정

            ChatMsg loginMsg = new ChatMsg(uid, ChatMsg.MODE_LOGIN, null); //로그인 메시지 생성
            out.writeObject(loginMsg); //서버로 메시지 전송
            out.flush();

//            receiveThread = new Thread(() -> { //메시지 수신용 스레드 시작
//                try {
//                    while (receiveThread != null) {
//                        receiveMessage();
//                    }
//                } catch (IOException | ClassNotFoundException e) {
//                    //printDisplay("수신 오류: " + e.getMessage());
//                }
//            });
//            receiveThread.start();



            //버튼들 활성화&비활성화 세팅
            b_connect.setEnabled(false);
            b_disconnect.setEnabled(true);
            b_send.setEnabled(true);
            t_input.setEnabled(true);
        } catch (IOException e) {
            printDisplay("연결 실패: " + e.getMessage());
        }
    }

    private void disconnect(){
        try {
            if (socket != null) {
                ChatMsg logoutMsg = new ChatMsg(uid, ChatMsg.MODE_LOGOUT, null); //로그아웃 메시지 생성
                out.writeObject(logoutMsg);
                out.flush();

                socket.close(); //소켓 닫기
                receiveThread = null;
            }

            //버튼들 활성화&비활성화 세팅
            b_connect.setEnabled(true);
            b_disconnect.setEnabled(false);
            b_send.setEnabled(false);
        } catch (IOException e) {
            printDisplay("연결 해제 실패: " + e.getMessage());
        }
    }


    private void sendMessage(){
        try {
            String msg = t_input.getText(); //입력된 메시지 가져오기
            if (!msg.isEmpty()) {
                ChatMsg chatMsg = new ChatMsg(uid, ChatMsg.MODE_TX_STRING, msg); //텍스트 메시지 생성
                out.writeObject(chatMsg); //서버로 메시지 전송
                out.flush();
                t_input.setText(""); //입력 필드 초기화
            }
        } catch (IOException e) {
            printDisplay("메시지 전송 실패: " + e.getMessage());
        }
    }


    //JFileChooser 사용해서 이미지 파일 찾는로직 구현된 함수
    private void selectFile(){
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("Image Files", "jpg", "png", "gif"));

        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile(); //선택한 파일 file변수로 가져오기
//            sendFile(file); //파일 전송
        }
    }



//    private void sendFile(File file) {
//
//        try (FileInputStream fis = new FileInputStream(file)) {
//
//            byte[] imageBytes = fis.readAllBytes(); //파일 내용을 바이트 '배열'로 읽음
//            ChatMsg fileMsg = new ChatMsg(uid, ChatMsg.MODE_TX_IMAGE, file.getName(), imageBytes); //ChatMsg 객체로 파일 메시지를 생성
//
//            out.writeObject(fileMsg); //서버로 ChatMsg 객체인 파일 메시지 writeObject 해버림. ObjectOutputStream 라 writeObject() 사용.
//            out.flush();			  //flush() 해버리기
//
//            printDisplay("파일 전송 완료: " + file.getName());
//        } catch (IOException e) {
//            printDisplay("파일 전송 실패: " + e.getMessage());
//        }
//    }

    private void receiveMessage() throws IOException, ClassNotFoundException {
        Object obj = in.readObject(); //서버로부터 객체 수신
        if (obj instanceof ChatMsg) {
            ChatMsg msg = (ChatMsg) obj;
            switch (msg.getMode()) {
                case ChatMsg.MODE_TX_STRING: //텍스트 메시지 수신일 경우
                    printDisplay(msg.toString());
                    break;
                case ChatMsg.MODE_TX_IMAGE: //이미지 메시지 수신일 경우
                    printDisplay(msg.getUserID() + ": " + msg.getMessage());
                    printDisplay(new ImageIcon(msg.getImage()));
                    break;
            }
        }
    }

    private void printDisplay(String msg) {
        try {
            document.insertString(document.getLength(), msg + "\n", null); //scrollpane 때문에 append 대신 document.insertString으로 메시지 추가
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
        t_display.setCaretPosition(document.getLength()); //화면 스크롤을 최신 상태로 유지하는 로직
    }

    public static void main(String[] args) {
        new WithTalk("localhost", 54321); //프로그램 실행
    }
}

