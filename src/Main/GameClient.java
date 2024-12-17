// GameClient.java
package Main;

import java.awt.*;
import java.io.*;
import java.net.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.Timer;
import javax.swing.filechooser.FileNameExtensionFilter;

public class GameClient extends JFrame {
    private Socket socket; // 서버와의 연결을 위한 소켓
    private ObjectInputStream in; // 서버로부터 객체 메시지를 읽기 위한 입력 스트림
    private ObjectOutputStream out; // 서버로 객체 메시지를 보내기 위한 출력 스트림
    private GamePanel gamePanel; // 게임 화면을 관리하는 GamePanel 객체
    private String clientId; // 클라이언트의 고유 ID

    private JTextField t_userID, t_serverAddress, t_portNumber;
    private JButton b_connect;
    private String serverAddress;
    private int port;

    public GameClient() {
        super("Game Client Login");
        buildLoginGUI(); // 입력 창 GUI 생성
        setSize(400, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    private void buildLoginGUI() {
        JPanel panel = new JPanel(new GridLayout(4, 2, 10, 10));

        panel.add(new JLabel("Client ID:"));
        t_userID = new JTextField();
        panel.add(t_userID);

        panel.add(new JLabel("Server Address:"));
        t_serverAddress = new JTextField("localhost"); // 기본값
        panel.add(t_serverAddress);

        panel.add(new JLabel("Port Number:"));
        t_portNumber = new JTextField("12345"); // 기본 포트
        panel.add(t_portNumber);

        b_connect = new JButton("Connect");
        panel.add(b_connect);

        add(panel, BorderLayout.CENTER);

        b_connect.addActionListener(e -> connectToServer());
    }

    private void connectToServer() {
        try {
            clientId = t_userID.getText();
            serverAddress = t_serverAddress.getText();
            port = Integer.parseInt(t_portNumber.getText());

            socket = new Socket(serverAddress, port);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            System.out.println("Connected to server: " + serverAddress + ":" + port);
            ChatMsg loginMsg = new ChatMsg(clientId, ChatMsg.MODE_LOGIN, null);
            out.writeObject(loginMsg);
            out.flush();

            // 새로운 GamePanel 실행
//            this.dispose(); // 로그인 창 닫기

            new GameClientHandler(socket, gamePanel, clientId, in, out).start();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Connection failed: " + e.getMessage());
        }
    }

    private GamePanel startGamePanel() {
        gamePanel = new GamePanel();
        setupChatListeners();

        JFrame gameFrame = new JFrame("Game Panel");
        gameFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        gameFrame.add(gamePanel); // JPanel 추가
        gameFrame.pack(); // 패널 크기에 맞게 프레임 크기 조정
        gameFrame.setLocationRelativeTo(null); // 프레임을 화면 중앙에 배치
        gameFrame.setVisible(true); // 프레임 표시

        // GamePanel 포커스 설정
        gamePanel.setFocusable(true);
        gamePanel.requestFocusInWindow(); // 포커스를 요청
//            add(gamePanel); // GamePanel을 JFrame에 추가
//            setVisible(true); // 화면 갱신

        // KeyListener 등록
        gamePanel.addKeyListener(new KeyAdapter() {
            private boolean isCooldown = false; // 쿨다운 플래그

            @Override
            public void keyPressed(KeyEvent e) {
                ChatMsg msg;
                if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                    // 팔라독을 왼쪽으로 이동
                    gamePanel.getPaladog().moveLeft();

                    // 팔라독의 현재 좌표를 가져옴
                    int palaX = gamePanel.getPaladogX();
                    int darkdogX = 940 - palaX; // 상대 클라이언트의 다크독 위치 계산
                    int y = 190; // Y값은 고정 또는 필요에 따라 변경

                    // 좌표 데이터를 포함한 ChatMsg 객체 생성
                    msg = new ChatMsg(clientId, ChatMsg.MODE_TX_COORDINATE, darkdogX + "," + y);
                    sendMessage(msg);
                } else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                    // 팔라독을 오른쪽으로 이동
                    gamePanel.getPaladog().moveRight();

                    // 팔라독의 현재 좌표를 가져옴
                    int palaX = gamePanel.getPaladogX();
                    int darkdogX = 940 - palaX; // 상대 클라이언트의 다크독 위치 계산
                    int y = 190; // Y값은 고정 또는 필요에 따라 변경

                    // 좌표 데이터를 포함한 ChatMsg 객체 생성
                    msg = new ChatMsg(clientId, ChatMsg.MODE_TX_COORDINATE, darkdogX + "," + y);
                    sendMessage(msg);
                } else if (e.getKeyChar() == '1') { // 1키 입력 시 좀비 유닛 소환 요청
                    if (!isCooldown) { // 쿨다운이 아닐 때만 실행
                        if (gamePanel.sohwanhp >= 10) {
                            // 좀비 유닛 소환 명령 객체 생성 및 전송
                            msg = new ChatMsg(clientId, ChatMsg.MODE_SPAWN_UNIT, "MOUSE");
                            sendMessage(msg);

                            // 쿨다운 시작
                            isCooldown = true;

                            // Timer를 사용한 쿨다운 설정
                            Timer cooldownTimer = new Timer(1000, new ActionListener() {
                                @Override
                                public void actionPerformed(ActionEvent e) {
                                    isCooldown = false; // 쿨다운 해제
                                }
                            });

                            cooldownTimer.setRepeats(false); // 한 번만 실행
                            cooldownTimer.start(); // Timer 시작
                        } else {
                            System.out.println("돈이 부족합니다");
                        }
                    } else {
                        System.out.println("잠시 기다려 주세요 (쿨다운 중)");
                    }
                } else if (e.getKeyChar() == 'j' || e.getKeyChar() == 'J') { // J키 입력 시 펀치 스킬 소환 요청
                    if (!isCooldown) { // 쿨다운이 아닐 때만 실행
                        if (gamePanel.skillmp >= 10) {
                            // 펀치 스킬 소환 명령 객체 생성 및 전송
                            msg = new ChatMsg(clientId, ChatMsg.MODE_SPAWN_SKILL, "PUNCH");
                            sendMessage(msg);

                            // 쿨다운 시작
                            isCooldown = true;

                            // Timer를 사용한 쿨다운 설정
                            Timer cooldownTimer = new Timer(500, new ActionListener() {
                                @Override
                                public void actionPerformed(ActionEvent e) {
                                    isCooldown = false; // 쿨다운 해제
                                }
                            });

                            cooldownTimer.setRepeats(false); // 한 번만 실행
                            cooldownTimer.start(); // Timer 시작
                        } else {
                            System.out.println("MP가 부족합니다");
                        }
                    } else {
                        System.out.println("잠시 기다려 주세요 (쿨다운 중)");
                    }
                }
            }
        });
        return gamePanel;
    }


    // 채팅 관련 이벤트 리스너 설정
    private void setupChatListeners() {
        // 텍스트 메시지 전송 리스너

        JTextField chatInput = gamePanel.getChatInput();
        gamePanel.getSendButton().addActionListener(e -> {
            String text = gamePanel.getChat();
             // 입력 필드에서 텍스트 가져오기
            if (!text.isEmpty()) {
                ChatMsg chatMsg = new ChatMsg(clientId, ChatMsg.MODE_TX_STRING, text);
                sendMessage(chatMsg); // 서버로 메시지 전송
                gamePanel.printDisplay("Me: " + text); // 로컬에서도 채팅 추가
            }
            gamePanel.requestFocusInWindow(); // 채팅 후 GamePanel에 포커스 다시 설정
        });

        // 이미지 전송 버튼 리스너 (추가 가능)
        JButton sendImageButton = gamePanel.getSendImageButton(); // 이미지 전송 버튼 참조
        sendImageButton.addActionListener(new ActionListener() {

            JFileChooser chooser = new JFileChooser();

            @Override
            public void actionPerformed(ActionEvent e) {
                FileNameExtensionFilter filter = new FileNameExtensionFilter("JPG & GIF & PNG Images", "jpg", "gif", "png");

                chooser.setFileFilter(filter);

                int ret = chooser.showOpenDialog(gamePanel);
                if (ret != JFileChooser.APPROVE_OPTION) {
                    JOptionPane.showMessageDialog(gamePanel, "파일을 선택하지 않았습니다.", "경고", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                chatInput.setText(chooser.getSelectedFile().getAbsolutePath());
                sendImage();
            }
        });

    }

    private void sendImage() {
        JTextField chatInput = gamePanel.getChatInput();
        String filename = chatInput.getText().trim();
        if (filename.isEmpty()) return;

        File file = new File(filename);
        if(!file.exists()) {
            System.out.println(">>파일이 존재하지 않습니다: " + filename);
            return;
        }

        ImageIcon icon = new ImageIcon(filename);
        sendMessage(new ChatMsg(clientId, ChatMsg.MODE_TX_IMAGE, file.getName(), icon));
    }

    // 서버로 메시지를 전송 (ChatMsg 객체 사용)
    public void sendMessage(ChatMsg msg) {
        try {
            synchronized (out) { // 동기화 보장
                out.writeObject(msg);
                out.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    class GameClientHandler extends Thread {
        private Socket socket;
        private GamePanel gamePanel;
        private ObjectInputStream in;
        private ObjectOutputStream out;
        private String clientId;
        private boolean gameStarted = false;

        public GameClientHandler(Socket socket, GamePanel gamePanel, String clientId, ObjectInputStream in, ObjectOutputStream out) {
            this.socket = socket;
            this.gamePanel = gamePanel;
            this.clientId = clientId;
            this.in = in;
            this.out = out;
        }

        @Override
        public void run() {
            try {
                while (true) {
                    ChatMsg msg = (ChatMsg) in.readObject(); // 서버로부터 메시지 수신
                    if (msg.getMode() == ChatMsg.MODE_GAME_START) {
                        // 서버에서 게임 시작 신호를 받음
                        if (!gameStarted) {
                            gameStarted = true;
                            SwingUtilities.invokeLater(() -> {
                                GameClient.this.dispose();
                                this.gamePanel = startGamePanel();
                            });
                        }
                    } else {
                        processMessage(msg);
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                System.out.println("Connection closed for: " + clientId);
            }
        }

        private void processMessage(ChatMsg msg) {

            if (!gameStarted) {
                return; //겜 시작 전에 exception안뜨게 어떤 메시지도 처리 안하게
            }

            SwingUtilities.invokeLater(() -> {
                if (!msg.getUserID().equals(clientId)) { // 상대 클라이언트의 메시지만 처리
                    switch (msg.getMode()) {
                        case ChatMsg.MODE_TX_COORDINATE: // 좌표 메시지 처리
                            if (msg.getMessage().contains(",")) {
                                String[] coords = msg.getMessage().split(",");
                                if (coords.length == 2) {
                                    try {
                                        int x = Integer.parseInt(coords[0].trim());
                                        int y = Integer.parseInt(coords[1].trim());
                                        System.out.println("Updating DarkDog Position to: " + x + ", " + y);
                                        gamePanel.updateDarkDogPosition(x, y);
                                        gamePanel.getDarkdog().x = x;
                                        gamePanel.getDarkdog().y = y;
                                    } catch (NumberFormatException e) {
                                        e.printStackTrace();
                                    }
                                }
                            } else {
                                System.out.println(msg.getUserID() + " says: " + msg.getMessage());
                            }
                            break;

                        case ChatMsg.MODE_SPAWN_UNIT: // 유닛 소환 처리
                            if (msg.getMessage().equals("MOUSE")) {
                                System.out.println("Spawning Zombie for DarkDog.");
                                gamePanel.spawnZombieForDarkDog();
                            }
                            break;

                        case ChatMsg.MODE_SPAWN_SKILL: // 스킬 소환 처리
                            if (msg.getMessage().equals("PUNCH")) {
                                System.out.println("Spawning DarkDog Punch.");
                                gamePanel.spawnDarkDogPunch();
                            }
                            break;

                        case ChatMsg.MODE_TX_STRING: // 텍스트 채팅 메시지 처리
                            // 채팅 메시지를 UI에 추가
                            gamePanel.printDisplay(msg.getUserID() + ": " + msg.getMessage());
                            break;

                        case ChatMsg.MODE_TX_IMAGE: // 이미지 채팅 메시지 처리
                            // 이미지 데이터를 저장하거나 UI에 추가
//                            try {
//                                String fileName = "received_image_" + System.currentTimeMillis() + ".png";
//                                File imageFile = new File(fileName);
//                                try (FileOutputStream fos = new FileOutputStream(imageFile)) {
//                                    fos.write(msg.getImage());
//                                }
//                                System.out.println("Image received and saved as " + fileName);
//                                gamePanel.printDisplay(msg.getUserID() + ": [Image received: " + fileName + "]");
//                            } catch (IOException e) {
//                                System.err.println("Failed to save received image.");
//                                e.printStackTrace();
//                            }
                            gamePanel.printDisplay(msg.getUserID() + ": " + msg.getMessage());
                            gamePanel.printDisplay(msg.getImage());

                            break;

                        default:
                            System.out.println("Unhandled Mode: " + msg.getMode());
                            break;
                    }
                }
            });
        }
    }

    public static void main(String[] args) {

        new GameClient();

    }
}

