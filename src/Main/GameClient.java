//// GameClient.java
//package Main;
//
//import java.awt.*;
//import java.io.*;
//import java.net.*;
//import java.awt.event.*;
//import javax.swing.*;
//import javax.swing.Timer;
//
//public class GameClient extends JFrame {
//    private Socket socket; // 서버와의 연결을 위한 소켓
//    private ObjectInputStream in; // 서버로부터 객체 메시지를 읽기 위한 입력 스트림
//    private ObjectOutputStream out; // 서버로 객체 메시지를 보내기 위한 출력 스트림
//    private GamePanel gamePanel; // 게임 화면을 관리하는 GamePanel 객체
//    private String clientId; // 클라이언트의 고유 ID
//
//    private JTextField t_userID, t_serverAddress, t_portNumber;
//    private JButton b_connect;
//    private String serverAddress;
//    private int port;
//
//    public GameClient() {
//        super("Game Client Login");
//        buildLoginGUI(); // 입력 창 GUI 생성
//        setSize(400, 200);
//        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        setVisible(true);
//    }
//
//    private void buildLoginGUI() {
//        JPanel panel = new JPanel(new GridLayout(4, 2, 10, 10));
//
//        panel.add(new JLabel("Client ID:"));
//        t_userID = new JTextField();
//        panel.add(t_userID);
//
//        panel.add(new JLabel("Server Address:"));
//        t_serverAddress = new JTextField("localhost"); // 기본값
//        panel.add(t_serverAddress);
//
//        panel.add(new JLabel("Port Number:"));
//        t_portNumber = new JTextField("12345"); // 기본 포트
//        panel.add(t_portNumber);
//
//        b_connect = new JButton("Connect");
//        panel.add(b_connect);
//
//        add(panel, BorderLayout.CENTER);
//
//        b_connect.addActionListener(e -> connectToServer());
//    }
//
//    private void connectToServer() {
//        try {
//            clientId = t_userID.getText();
//            serverAddress = t_serverAddress.getText();
//            port = Integer.parseInt(t_portNumber.getText());
//
//            socket = new Socket(serverAddress, port);
//            out = new ObjectOutputStream(socket.getOutputStream());
//            in = new ObjectInputStream(socket.getInputStream());
//
//            System.out.println("Connected to server: " + serverAddress + ":" + port);
//            ChatMsg loginMsg = new ChatMsg(clientId, ChatMsg.MODE_LOGIN, null);
//            out.writeObject(loginMsg);
//            out.flush();
//
//            // 새로운 GamePanel 실행
////            this.dispose(); // 로그인 창 닫기
//
//            new GameClientHandler(socket, gamePanel, clientId, in, out).start();
//
//        } catch (Exception e) {
//            JOptionPane.showMessageDialog(this, "Connection failed: " + e.getMessage());
//        }
//    }
//
//    private GamePanel startGamePanel() {
//        gamePanel = new GamePanel();
//        setupChatListeners();
//
//        JFrame gameFrame = new JFrame("Game Panel");
//        gameFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        gameFrame.add(gamePanel); // JPanel 추가
//        gameFrame.pack(); // 패널 크기에 맞게 프레임 크기 조정
//        gameFrame.setLocationRelativeTo(null); // 프레임을 화면 중앙에 배치
//        gameFrame.setVisible(true); // 프레임 표시
//
//        // GamePanel 포커스 설정
//        gamePanel.setFocusable(true);
//        gamePanel.requestFocusInWindow(); // 포커스를 요청
////            add(gamePanel); // GamePanel을 JFrame에 추가
////            setVisible(true); // 화면 갱신
//
//        // KeyListener 등록
//        gamePanel.addKeyListener(new KeyAdapter() {
//            private boolean isCooldown = false; // 쿨다운 플래그
//
//            @Override
//            public void keyPressed(KeyEvent e) {
//                ChatMsg msg;
//                if (e.getKeyCode() == KeyEvent.VK_LEFT) {
//                    // 팔라독을 왼쪽으로 이동
//                    gamePanel.getPaladog().moveLeft();
//
//                    // 팔라독의 현재 좌표를 가져옴
//                    int palaX = gamePanel.getPaladogX();
//                    int darkdogX = 940 - palaX; // 상대 클라이언트의 다크독 위치 계산
//                    int y = 190; // Y값은 고정 또는 필요에 따라 변경
//
//                    // 좌표 데이터를 포함한 ChatMsg 객체 생성
//                    msg = new ChatMsg(clientId, ChatMsg.MODE_TX_COORDINATE, darkdogX + "," + y);
//                    sendMessage(msg);
//                } else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
//                    // 팔라독을 오른쪽으로 이동
//                    gamePanel.getPaladog().moveRight();
//
//                    // 팔라독의 현재 좌표를 가져옴
//                    int palaX = gamePanel.getPaladogX();
//                    int darkdogX = 940 - palaX; // 상대 클라이언트의 다크독 위치 계산
//                    int y = 190; // Y값은 고정 또는 필요에 따라 변경
//
//                    // 좌표 데이터를 포함한 ChatMsg 객체 생성
//                    msg = new ChatMsg(clientId, ChatMsg.MODE_TX_COORDINATE, darkdogX + "," + y);
//                    sendMessage(msg);
//                } else if (e.getKeyChar() == '1') { // 1키 입력 시 좀비 유닛 소환 요청
//                    if (!isCooldown) { // 쿨다운이 아닐 때만 실행
//                        if (gamePanel.sohwanhp >= 10) {
//                            // 좀비 유닛 소환 명령 객체 생성 및 전송
//                            msg = new ChatMsg(clientId, ChatMsg.MODE_SPAWN_UNIT, "MOUSE");
//                            sendMessage(msg);
//
//                            // 쿨다운 시작
//                            isCooldown = true;
//
//                            // Timer를 사용한 쿨다운 설정
//                            Timer cooldownTimer = new Timer(1000, new ActionListener() {
//                                @Override
//                                public void actionPerformed(ActionEvent e) {
//                                    isCooldown = false; // 쿨다운 해제
//                                }
//                            });
//
//                            cooldownTimer.setRepeats(false); // 한 번만 실행
//                            cooldownTimer.start(); // Timer 시작
//                        } else {
//                            System.out.println("돈이 부족합니다");
//                        }
//                    } else {
//                        System.out.println("잠시 기다려 주세요 (쿨다운 중)");
//                    }
//                } else if (e.getKeyChar() == 'j' || e.getKeyChar() == 'J') { // J키 입력 시 펀치 스킬 소환 요청
//                    if (!isCooldown) { // 쿨다운이 아닐 때만 실행
//                        if (gamePanel.skillmp >= 10) {
//                            // 펀치 스킬 소환 명령 객체 생성 및 전송
//                            msg = new ChatMsg(clientId, ChatMsg.MODE_SPAWN_SKILL, "PUNCH");
//                            sendMessage(msg);
//
//                            // 쿨다운 시작
//                            isCooldown = true;
//
//                            // Timer를 사용한 쿨다운 설정
//                            Timer cooldownTimer = new Timer(500, new ActionListener() {
//                                @Override
//                                public void actionPerformed(ActionEvent e) {
//                                    isCooldown = false; // 쿨다운 해제
//                                }
//                            });
//
//                            cooldownTimer.setRepeats(false); // 한 번만 실행
//                            cooldownTimer.start(); // Timer 시작
//                        } else {
//                            System.out.println("MP가 부족합니다");
//                        }
//                    } else {
//                        System.out.println("잠시 기다려 주세요 (쿨다운 중)");
//                    }
//                }
//            }
//        });
//        return gamePanel;
//    }
//
//
//    // 채팅 관련 이벤트 리스너 설정
//    private void setupChatListeners() {
//        // 텍스트 메시지 전송 리스너
//        gamePanel.getSendButton().addActionListener(e -> {
//            String text = gamePanel.getChatInput(); // 입력 필드에서 텍스트 가져오기
//            if (!text.isEmpty()) {
//                ChatMsg chatMsg = new ChatMsg(clientId, ChatMsg.MODE_TX_STRING, text);
//                sendMessage(chatMsg); // 서버로 메시지 전송
//                gamePanel.appendChatMessage("Me: " + text); // 로컬에서도 채팅 추가
//            }
//            gamePanel.requestFocusInWindow(); // 채팅 후 GamePanel에 포커스 다시 설정
//        });
//
//        // 이미지 전송 버튼 리스너 (추가 가능)
////        JButton sendImageButton = gamePanel.getSendImageButton(); // 이미지 전송 버튼 참조
////        if (sendImageButton != null) { // 이미지 전송 기능이 있는 경우
////            sendImageButton.addActionListener(e -> {
////                JFileChooser fileChooser = new JFileChooser();
////                if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
////                    File file = fileChooser.getSelectedFile();
////                    try (FileInputStream fis = new FileInputStream(file)) {
////                        byte[] imageData = fis.readAllBytes();
////                        ChatMsg chatMsg = new ChatMsg(clientId, ChatMsg.MODE_TX_IMAGE, "[Image]", imageData);
////                        sendMessage(chatMsg);
////                        gamePanel.appendChatMessage("Me: [Image Sent]");
////                    } catch (IOException ex) {
////                        System.err.println("Failed to send image.");
////                        ex.printStackTrace();
////                    }
////                }
////            });
////        }
//    }
//
//
//    // 서버로 메시지를 전송 (ChatMsg 객체 사용)
//    public void sendMessage(ChatMsg msg) {
//        try {
//            synchronized (out) { // 동기화 보장
//                out.writeObject(msg);
//                out.flush();
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//
//    class GameClientHandler extends Thread {
//        private Socket socket;
//        private GamePanel gamePanel;
//        private ObjectInputStream in;
//        private ObjectOutputStream out;
//        private String clientId;
//        private boolean gameStarted = false;
//
//        public GameClientHandler(Socket socket, GamePanel gamePanel, String clientId, ObjectInputStream in, ObjectOutputStream out) {
//            this.socket = socket;
//            this.gamePanel = gamePanel;
//            this.clientId = clientId;
//            this.in = in;
//            this.out = out;
//        }
//
//        @Override
//        public void run() {
//            try {
//                while (true) {
//                    ChatMsg msg = (ChatMsg) in.readObject(); // 서버로부터 메시지 수신
//                    if (msg.getMode() == ChatMsg.MODE_GAME_START) {
//                        // 서버에서 게임 시작 신호를 받음
//                        if (!gameStarted) {
//                            gameStarted = true;
//                            SwingUtilities.invokeLater(() -> {
//                                GameClient.this.dispose();
//                                this.gamePanel = startGamePanel();
//                            });
//                        }
//                    } else {
//                        processMessage(msg);
//                    }
//                }
//            } catch (IOException | ClassNotFoundException e) {
//                System.out.println("Connection closed for: " + clientId);
//            }
//        }
//
//        private void processMessage(ChatMsg msg) {
//
//            if (!gameStarted) {
//                return; //겜 시작 전에 exception안뜨게 어떤 메시지도 처리 안하게
//            }
//
//            SwingUtilities.invokeLater(() -> {
//                if (!msg.getUserID().equals(clientId)) { // 상대 클라이언트의 메시지만 처리
//                    switch (msg.getMode()) {
//                        case ChatMsg.MODE_TX_COORDINATE: // 좌표 메시지 처리
//                            if (msg.getMessage().contains(",")) {
//                                String[] coords = msg.getMessage().split(",");
//                                if (coords.length == 2) {
//                                    try {
//                                        int x = Integer.parseInt(coords[0].trim());
//                                        int y = Integer.parseInt(coords[1].trim());
//                                        System.out.println("Updating DarkDog Position to: " + x + ", " + y);
//                                        gamePanel.updateDarkDogPosition(x, y);
//                                        gamePanel.getDarkdog().x = x;
//                                        gamePanel.getDarkdog().y = y;
//                                    } catch (NumberFormatException e) {
//                                        e.printStackTrace();
//                                    }
//                                }
//                            } else {
//                                System.out.println(msg.getUserID() + " says: " + msg.getMessage());
//                            }
//                            break;
//
//                        case ChatMsg.MODE_SPAWN_UNIT: // 유닛 소환 처리
//                            if (msg.getMessage().equals("MOUSE")) {
//                                System.out.println("Spawning Zombie for DarkDog.");
//                                gamePanel.spawnZombieForDarkDog();
//                            }
//                            break;
//
//                        case ChatMsg.MODE_SPAWN_SKILL: // 스킬 소환 처리
//                            if (msg.getMessage().equals("PUNCH")) {
//                                System.out.println("Spawning DarkDog Punch.");
//                                gamePanel.spawnDarkDogPunch();
//                            }
//                            break;
//
//                        case ChatMsg.MODE_TX_STRING: // 텍스트 채팅 메시지 처리
//                            // 채팅 메시지를 UI에 추가
//                            gamePanel.appendChatMessage(msg.getUserID() + ": " + msg.getMessage());
//                            break;
//
//                        case ChatMsg.MODE_TX_IMAGE: // 이미지 채팅 메시지 처리
//                            // 이미지 데이터를 저장하거나 UI에 추가
//                            try {
//                                String fileName = "received_image_" + System.currentTimeMillis() + ".png";
//                                File imageFile = new File(fileName);
//                                try (FileOutputStream fos = new FileOutputStream(imageFile)) {
//                                    fos.write(msg.getImage());
//                                }
//                                System.out.println("Image received and saved as " + fileName);
//                                gamePanel.appendChatMessage(msg.getUserID() + ": [Image received: " + fileName + "]");
//                            } catch (IOException e) {
//                                System.err.println("Failed to save received image.");
//                                e.printStackTrace();
//                            }
//                            break;
//
//                        default:
//                            System.out.println("Unhandled Mode: " + msg.getMode());
//                            break;
//                    }
//                }
//            });
//        }
//    }
//
//    public static void main(String[] args) {
//
//        new GameClient();
//
//    }
//}
//
package Main;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.Timer;

public class GameClient extends JFrame {
    private Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private String clientId;

    private JPanel homePanel, roomPanel;
    private JButton b_createRoom;
    private JList<String> roomList;
    private DefaultListModel<String> roomListModel;
    private GamePanel gamePanel; // 게임 화면을 관리하는 GamePanel 객체


    public GameClient() {
        super("Game Client");
        buildLoginGUI();
        setSize(400, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    // 로그인 화면
    private void buildLoginGUI() {
        JPanel panel = new JPanel(new GridLayout(4, 2, 10, 10));

        panel.add(new JLabel("Client ID:"));
        JTextField t_userID = new JTextField();
        panel.add(t_userID);

        panel.add(new JLabel("Server Address:"));
        JTextField t_serverAddress = new JTextField("localhost");
        panel.add(t_serverAddress);

        panel.add(new JLabel("Port Number:"));
        JTextField t_portNumber = new JTextField("12345");
        panel.add(t_portNumber);

        JButton b_connect = new JButton("Connect");
        panel.add(b_connect);

        add(panel, BorderLayout.CENTER);

        b_connect.addActionListener(e -> {
            try {
                clientId = t_userID.getText();
                String serverAddress = t_serverAddress.getText();
                int port = Integer.parseInt(t_portNumber.getText());

                socket = new Socket(serverAddress, port);
                out = new ObjectOutputStream(socket.getOutputStream());
                in = new ObjectInputStream(socket.getInputStream());

                sendMessage(new ChatMsg(clientId, ChatMsg.MODE_LOGIN, "Login"));
                setupHomeScreen();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Connection failed: " + ex.getMessage());
            }
        });
    }

    // 홈 화면 (방 목록과 방 만들기)
    private void setupHomeScreen() {
        getContentPane().removeAll();
        homePanel = new JPanel(new BorderLayout());

        roomListModel = new DefaultListModel<>();
        roomList = new JList<>(roomListModel);
        JScrollPane scrollPane = new JScrollPane(roomList);

        b_createRoom = new JButton("방 만들기");

        homePanel.add(new JLabel("방 목록", SwingConstants.CENTER), BorderLayout.NORTH);
        homePanel.add(scrollPane, BorderLayout.CENTER);
        homePanel.add(b_createRoom, BorderLayout.SOUTH);

//        b_createRoom.addActionListener(e -> {
//            sendMessage(new ChatMsg(clientId, ChatMsg.MODE_ROOM_CREATE, null));
//        });
        b_createRoom.addActionListener(e -> {
            sendMessage(new ChatMsg(clientId, ChatMsg.MODE_ROOM_CREATE, "create_room", null));
        });


        roomList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    String selectedRoom = roomList.getSelectedValue();
                    sendMessage(new ChatMsg(clientId, ChatMsg.MODE_ROOM_JOIN, selectedRoom));
                }
            }
        });

        add(homePanel);
        setSize(400, 400);
        revalidate();
        repaint();

        new GameClientHandler().start();
    }

    // 대기방 화면
//    private void setupRoomScreen(String roomName, List<String> players) {
//        getContentPane().removeAll();
//        roomPanel = new JPanel(new BorderLayout());
//
//        // 사용자 목록과 준비 상태를 보여주는 JTextArea
//        JTextArea playerArea = new JTextArea();
//        playerArea.setEditable(false);
//        JScrollPane scrollPane = new JScrollPane(playerArea);
//
//        // 버튼 패널: 준비 버튼과 홈으로 나가기 버튼
//        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 10, 10));
//        JButton b_ready = new JButton("준비");
//        JButton b_exit = new JButton("홈으로 나가기");
//
//        buttonPanel.add(b_ready);
//        buttonPanel.add(b_exit);
//
//        // 사용자 목록 갱신
//        updatePlayerArea(playerArea, players);
//
//        // 준비 버튼 클릭 이벤트
//        b_ready.addActionListener(e -> {
//            sendMessage(new ChatMsg(clientId, ChatMsg.MODE_READY, "Ready"));
//        });
//
//        // 홈으로 나가기 버튼 클릭 이벤트
//        b_exit.addActionListener(e -> {
//            sendMessage(new ChatMsg(clientId, ChatMsg.MODE_LOGOUT, "Leave Room"));
//            setupHomeScreen(); // 홈 화면으로 돌아가기
//        });
//
////        // 화면 구성: 방 이름을 상단에 추가
////        JLabel roomLabel = new JLabel("대기방: " + roomName, SwingConstants.CENTER);
////        roomLabel.setFont(new Font("Arial", Font.BOLD, 16));
//
//        // 화면 구성: 방 이름을 상단에 추가
//        JLabel roomLabel = new JLabel("대기방: " + roomName, SwingConstants.CENTER);
//        roomLabel.setFont(new Font("맑은 고딕", Font.BOLD, 16)); // 한글 폰트 설정
//        roomLabel.setForeground(Color.BLACK); // 글자 색상 설정
//
//        roomPanel.add(roomLabel, BorderLayout.NORTH);
//        roomPanel.add(scrollPane, BorderLayout.CENTER);
//        roomPanel.add(buttonPanel, BorderLayout.SOUTH);
//
//        add(roomPanel);
//        setSize(400, 400);
//        revalidate();
//        repaint();
//    }
    private void setupRoomScreen(String roomName, List<String> players) {
        getContentPane().removeAll();
        roomPanel = new JPanel(new BorderLayout());

        // 준비 상태를 나타내기 위해 사용자 목록 표시
        DefaultListModel<String> playerListModel = new DefaultListModel<>();
        JList<String> playerList = new JList<>(playerListModel);
        JScrollPane scrollPane = new JScrollPane(playerList);

        // 버튼 패널: 준비 버튼과 홈으로 나가기 버튼
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 10, 10));
        JButton b_ready = new JButton("준비");
        JButton b_exit = new JButton("홈으로 나가기");

        buttonPanel.add(b_ready);
        buttonPanel.add(b_exit);

        // 사용자 목록과 준비 상태 업데이트
        updatePlayerList(playerListModel, players);

        // 준비 버튼 클릭 이벤트
        b_ready.addActionListener(e -> {
            sendMessage(new ChatMsg(clientId, ChatMsg.MODE_READY, "Ready"));
            b_ready.setEnabled(false); // 준비 버튼 비활성화
        });

//        // 홈으로 나가기 버튼 클릭 이벤트
//        b_exit.addActionListener(e -> {
//            sendMessage(new ChatMsg(clientId, ChatMsg.MODE_LOGOUT, "Leave Room"));
//            setupHomeScreen(); // 홈 화면으로 돌아가기
//        });
        b_exit.addActionListener(e -> {
            sendMessage(new ChatMsg(clientId, ChatMsg.MODE_LOGOUT, "Leave Room"));
            reconnectStreams(); // 스트림 재연결
            setupHomeScreen();  // 홈 화면으로 돌아가기
        });



        // 화면 구성
        JLabel roomLabel = new JLabel("대기방: " + roomName, SwingConstants.CENTER);
        roomLabel.setFont(new Font("맑은 고딕", Font.BOLD, 16));

        roomPanel.add(roomLabel, BorderLayout.NORTH);
        roomPanel.add(scrollPane, BorderLayout.CENTER);
        roomPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(roomPanel);
        setSize(400, 400);
        revalidate();
        repaint();
    }

    private void reconnectStreams() {
        try {
            System.out.println("스트림 재연결 중...");
            if (socket != null && !socket.isClosed()) {
                in.close();
                out.close();

                // 스트림 재생성
                out = new ObjectOutputStream(socket.getOutputStream());
                out.flush();
                in = new ObjectInputStream(socket.getInputStream());

                // 재연결 후 서버에 상태 전송
                sendMessage(new ChatMsg(clientId, ChatMsg.MODE_ROOM_LIST, null));
                System.out.println("스트림 재연결 성공");
            }
        } catch (IOException ex) {
            System.err.println("스트림 재연결 실패: " + ex.getMessage());
        }
    }

//    private void updatePlayerArea(JTextArea playerArea, List<String> players) {
//        if (players == null) {
//            players = new ArrayList<>(); // 빈 리스트로 초기화
//        }
//        StringBuilder sb = new StringBuilder();
//        sb.append("참여자 목록:\n");
//        for (String player : players) {
//            sb.append(player).append("\n");
//        }
//        playerArea.setText(sb.toString());
//    }

    private void updatePlayerList(DefaultListModel<String> playerListModel, List<String> players) {
        playerListModel.clear();
        for (String player : players) {
            playerListModel.addElement(player); // 사용자 이름과 준비 상태 추가
        }
    }



    private void sendMessage(ChatMsg msg) {
        try {
            out.writeObject(msg);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    class GameClientHandler extends Thread {
        public void run() {
            try {
                while (true) {
                    try {
                        ChatMsg msg = (ChatMsg) in.readObject(); // 서버로부터 메시지 수신
                        if (msg == null) break; // 메시지가 null이면 루프 종료
                        processMessage(msg); // 메시지 처리
                    } catch (SocketException se) {
                        System.out.println("소켓 오류: 서버와의 연결이 끊겼습니다.");
                        break; // 소켓 오류 시 루프 종료
                    } catch (EOFException eof) {
                        System.out.println("서버와의 연결이 종료되었습니다.");
                        reconnectStreams(); // 스트림 재연결
                    } catch (IOException | ClassNotFoundException e) {
                        System.out.println("데이터 수신 오류: " + e.getMessage());
                        reconnectStreams(); // 스트림 재연결
                    }
                }
            } finally {
                System.out.println("서버와의 연결이 종료되었습니다. 클라이언트를 종료합니다.");
                closeResources();
            }
        }

//        private void reconnectStreams() {
//            try {
//                System.out.println("스트림 재연결 중...");
//                if (socket != null && !socket.isClosed()) {
//                    in.close();
//                    out.close();
//
//                    // 스트림 재생성
//                    out = new ObjectOutputStream(socket.getOutputStream());
//                    out.flush();
//                    in = new ObjectInputStream(socket.getInputStream());
//
//                    // 재연결 후 서버에 상태 전송
//                    sendMessage(new ChatMsg(clientId, ChatMsg.MODE_ROOM_LIST, null));
//                    System.out.println("스트림 재연결 성공");
//                }
//            } catch (IOException ex) {
//                System.err.println("스트림 재연결 실패: " + ex.getMessage());
//            }
//        }

        private void processMessage(ChatMsg msg) {
            // 수신한 메시지 처리 로직
            if (!msg.getUserID().equals(clientId)) {
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
                        gamePanel.appendChatMessage(msg.getUserID() + ": " + msg.getMessage());
                        break;

                    case ChatMsg.MODE_TX_IMAGE: // 이미지 채팅 메시지 처리
                        // 이미지 데이터를 저장하거나 UI에 추가
                        try {
                            String fileName = "received_image_" + System.currentTimeMillis() + ".png";
                            File imageFile = new File(fileName);
                            try (FileOutputStream fos = new FileOutputStream(imageFile)) {
                                fos.write(msg.getImage());
                            }
                            System.out.println("Image received and saved as " + fileName);
                            gamePanel.appendChatMessage(msg.getUserID() + ": [Image received: " + fileName + "]");
                        } catch (IOException e) {
                            System.err.println("Failed to save received image.");
                            e.printStackTrace();
                        }
                        break;


                }
            }
            switch (msg.getMode()) {

//                case ChatMsg.MODE_ROOM_LIST:
//                    List<String> rooms = (List<String>) msg.getData(); // 서버에서 최신 방 목록 수신
//                    roomListModel.clear(); // 기존 방 목록 삭제
//                    rooms.forEach(roomListModel::addElement); // 새로운 방 목록 추가
//                    break;
                case ChatMsg.MODE_ROOM_LIST:
                    List<String> rooms = (List<String>) msg.getData(); // 서버에서 최신 방 목록 수신
                    roomListModel.clear(); // 기존 방 목록 삭제
                    rooms.forEach(roomListModel::addElement); // 새로운 방 목록 추가
                    //setupHomeScreen(); // 홈 화면으로 돌아가기
                    break;


//                        case ChatMsg.MODE_ROOM_JOIN:
//                            setupRoomScreen();
//                            break;
//                        case ChatMsg.MODE_ROOM_JOIN:
//                            List<String> players = (List<String>) msg.getData(); // 사용자 목록 수신
//                            setupRoomScreen(players); // 대기방 화면으로 전환
//                            break;
//                        case ChatMsg.MODE_ROOM_JOIN:
//                            if (msg.getData() instanceof List) {
//                                List<String> players = (List<String>) msg.getData(); // 사용자 목록 수신
//                                if (players != null) {
//                                    setupRoomScreen(players); // 대기방 화면으로 전환
//                                } else {
//                                    System.out.println("대기방의 사용자 목록이 비어 있습니다.");
//                                }
//                            }
//                            break;
//                        case ChatMsg.MODE_ROOM_JOIN:
//                            if (msg.getData() instanceof List) {
//                                List<String> players = (List<String>) msg.getData(); // 사용자 목록 수신
//                                String roomName = msg.getMessage(); // 방 이름 수신
//                                if (players != null && roomName != null) {
//                                    setupRoomScreen(roomName, players); // 대기방 화면으로 전환
//                                } else {
//                                    System.out.println("대기방의 데이터가 비어 있습니다.");
//                                }
//                            }
//                            break;

                case ChatMsg.MODE_ROOM_JOIN:
                    if (msg.getData() instanceof List) {
                        List<String> players = (List<String>) msg.getData(); // 사용자 목록 수신
                        String roomName = msg.getMessage(); // 방 이름 수신
                        if (players != null && roomName != null) {
                            System.out.println("방에 입장: " + roomName);
                            setupRoomScreen(roomName, players); // 대기방 화면으로 전환
                        }
                    }
                    break;



                case ChatMsg.MODE_GAME_START:
                    SwingUtilities.invokeLater(() -> {
                        System.out.println("게임 시작: GamePanel 화면으로 전환");
                        startGamePanel(); // 게임 화면 실행
                    });
                    break;

                case ChatMsg.MODE_ROOM_FULL:
                    JOptionPane.showMessageDialog(null, "방이 꽉 찼습니다.");
                    break;

//                case ChatMsg.MODE_LOGOUT:
//                    //이때 업데이트하는 로직이 있어야할듯
//                    setupHomeScreen(); // 방에서 나갔을 때 홈 화면으로 돌아가기
//                    break;
                case ChatMsg.MODE_LOGOUT:
                    System.out.println("서버로부터 로그아웃 메시지 수신: 방에서 나감");
                    // 서버에 방 목록 요청
                    sendMessage(new ChatMsg(clientId, ChatMsg.MODE_ROOM_LIST, null));
                    break;

                case ChatMsg.MODE_READY:
                    System.out.println(msg.getUserID() + " 준비 완료!");
                    break;
            }
        }

        private void closeResources() {
            try {
                if (in != null) in.close();
                if (out != null) out.close();
                if (socket != null && !socket.isClosed()) socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

//    class GameClientHandler extends Thread {
//        public void run() {
//            try {
//                while (true) {
//                    ChatMsg msg = (ChatMsg) in.readObject();
//                    if (!msg.getUserID().equals(clientId)) {
//                        switch (msg.getMode()) {
//
//                            case ChatMsg.MODE_TX_COORDINATE: // 좌표 메시지 처리
//                                if (msg.getMessage().contains(",")) {
//                                    String[] coords = msg.getMessage().split(",");
//                                    if (coords.length == 2) {
//                                        try {
//                                            int x = Integer.parseInt(coords[0].trim());
//                                            int y = Integer.parseInt(coords[1].trim());
//                                            System.out.println("Updating DarkDog Position to: " + x + ", " + y);
//                                            gamePanel.updateDarkDogPosition(x, y);
//                                            gamePanel.getDarkdog().x = x;
//                                            gamePanel.getDarkdog().y = y;
//                                        } catch (NumberFormatException e) {
//                                            e.printStackTrace();
//                                        }
//                                    }
//                                } else {
//                                    System.out.println(msg.getUserID() + " says: " + msg.getMessage());
//                                }
//                                break;
//
//                            case ChatMsg.MODE_SPAWN_UNIT: // 유닛 소환 처리
//                                if (msg.getMessage().equals("MOUSE")) {
//                                    System.out.println("Spawning Zombie for DarkDog.");
//                                    gamePanel.spawnZombieForDarkDog();
//                                }
//                                break;
//
//                            case ChatMsg.MODE_SPAWN_SKILL: // 스킬 소환 처리
//                                if (msg.getMessage().equals("PUNCH")) {
//                                    System.out.println("Spawning DarkDog Punch.");
//                                    gamePanel.spawnDarkDogPunch();
//                                }
//                                break;
//
//                            case ChatMsg.MODE_TX_STRING: // 텍스트 채팅 메시지 처리
//                                // 채팅 메시지를 UI에 추가
//                                gamePanel.appendChatMessage(msg.getUserID() + ": " + msg.getMessage());
//                                break;
//
//                            case ChatMsg.MODE_TX_IMAGE: // 이미지 채팅 메시지 처리
//                                // 이미지 데이터를 저장하거나 UI에 추가
//                                try {
//                                    String fileName = "received_image_" + System.currentTimeMillis() + ".png";
//                                    File imageFile = new File(fileName);
//                                    try (FileOutputStream fos = new FileOutputStream(imageFile)) {
//                                        fos.write(msg.getImage());
//                                    }
//                                    System.out.println("Image received and saved as " + fileName);
//                                    gamePanel.appendChatMessage(msg.getUserID() + ": [Image received: " + fileName + "]");
//                                } catch (IOException e) {
//                                    System.err.println("Failed to save received image.");
//                                    e.printStackTrace();
//                                }
//                                break;
//
//
//                        }
//                    }
//                    switch (msg.getMode()) {
////                        case ChatMsg.MODE_ROOM_LIST:
////                            List<String> rooms = (List<String>) msg.getData();
////                            roomListModel.clear();
////                            rooms.forEach(roomListModel::addElement);
////                            break;
//                        case ChatMsg.MODE_ROOM_LIST:
//                            List<String> rooms = (List<String>) msg.getData(); // 서버에서 최신 방 목록 수신
//                            roomListModel.clear(); // 기존 방 목록 삭제
//                            rooms.forEach(roomListModel::addElement); // 새로운 방 목록 추가
//                            break;
//
//
////                        case ChatMsg.MODE_ROOM_JOIN:
////                            setupRoomScreen();
////                            break;
////                        case ChatMsg.MODE_ROOM_JOIN:
////                            List<String> players = (List<String>) msg.getData(); // 사용자 목록 수신
////                            setupRoomScreen(players); // 대기방 화면으로 전환
////                            break;
////                        case ChatMsg.MODE_ROOM_JOIN:
////                            if (msg.getData() instanceof List) {
////                                List<String> players = (List<String>) msg.getData(); // 사용자 목록 수신
////                                if (players != null) {
////                                    setupRoomScreen(players); // 대기방 화면으로 전환
////                                } else {
////                                    System.out.println("대기방의 사용자 목록이 비어 있습니다.");
////                                }
////                            }
////                            break;
////                        case ChatMsg.MODE_ROOM_JOIN:
////                            if (msg.getData() instanceof List) {
////                                List<String> players = (List<String>) msg.getData(); // 사용자 목록 수신
////                                String roomName = msg.getMessage(); // 방 이름 수신
////                                if (players != null && roomName != null) {
////                                    setupRoomScreen(roomName, players); // 대기방 화면으로 전환
////                                } else {
////                                    System.out.println("대기방의 데이터가 비어 있습니다.");
////                                }
////                            }
////                            break;
//
//                        case ChatMsg.MODE_ROOM_JOIN:
//                            if (msg.getData() instanceof List) {
//                                List<String> players = (List<String>) msg.getData(); // 사용자 목록 수신
//                                String roomName = msg.getMessage(); // 방 이름 수신
//                                if (players != null && roomName != null) {
//                                    System.out.println("방에 입장: " + roomName);
//                                    setupRoomScreen(roomName, players); // 대기방 화면으로 전환
//                                }
//                            }
//                            break;
//
//
//
//                        case ChatMsg.MODE_GAME_START:
//                            SwingUtilities.invokeLater(() -> {
//                                System.out.println("게임 시작: GamePanel 화면으로 전환");
//                                startGamePanel(); // 게임 화면 실행
//                            });
//                            break;
//
//                        case ChatMsg.MODE_ROOM_FULL:
//                            JOptionPane.showMessageDialog(null, "방이 꽉 찼습니다.");
//                            break;
//
//                        case ChatMsg.MODE_LOGOUT:
//                            setupHomeScreen(); // 방에서 나갔을 때 홈 화면으로 돌아가기
//                            break;
//
//                        case ChatMsg.MODE_READY:
//                            System.out.println(msg.getUserID() + " 준비 완료!");
//                            break;
//                    }
//                }
//            } catch (IOException | ClassNotFoundException e) {
//                System.out.println("Disconnected from server.");
//            }
//        }
//    }

    // 채팅 관련 이벤트 리스너 설정
    private void setupChatListeners() {
        // 텍스트 메시지 전송 리스너
        gamePanel.getSendButton().addActionListener(e -> {
            String text = gamePanel.getChatInput(); // 입력 필드에서 텍스트 가져오기
            if (!text.isEmpty()) {
                ChatMsg chatMsg = new ChatMsg(clientId, ChatMsg.MODE_TX_STRING, text);
                sendMessage(chatMsg); // 서버로 메시지 전송
                gamePanel.appendChatMessage("Me: " + text); // 로컬에서도 채팅 추가
            }
            gamePanel.requestFocusInWindow(); // 채팅 후 GamePanel에 포커스 다시 설정
        });

        // 이미지 전송 버튼 리스너 (추가 가능)
//        JButton sendImageButton = gamePanel.getSendImageButton(); // 이미지 전송 버튼 참조
//        if (sendImageButton != null) { // 이미지 전송 기능이 있는 경우
//            sendImageButton.addActionListener(e -> {
//                JFileChooser fileChooser = new JFileChooser();
//                if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
//                    File file = fileChooser.getSelectedFile();
//                    try (FileInputStream fis = new FileInputStream(file)) {
//                        byte[] imageData = fis.readAllBytes();
//                        ChatMsg chatMsg = new ChatMsg(clientId, ChatMsg.MODE_TX_IMAGE, "[Image]", imageData);
//                        sendMessage(chatMsg);
//                        gamePanel.appendChatMessage("Me: [Image Sent]");
//                    } catch (IOException ex) {
//                        System.err.println("Failed to send image.");
//                        ex.printStackTrace();
//                    }
//                }
//            });
//        }
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

    public static void main(String[] args) {
        new GameClient();
    }
}
