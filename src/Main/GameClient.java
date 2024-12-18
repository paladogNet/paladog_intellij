// 해당 클래스는 모두 직접 작성하였는데, 키를 입력받는 부분에 '쿨다운' 개념을 웹서칭 하여 도입했습니다.
// 쿨다운은 특정 키 입력(1,J 등)이 실행된 후 일정 시간 동안 다시 입력되지 않도록 막는 기능입니다.(isCooldown 플래그를 사용해 쿨다운 상태를 확인하고, 입력을 막습니다.)
// Timer를 사용해 쿨다운 시간을 설정한 후, 시간이 지나면 isCooldown을 false로 변경해 입력을 다시 허용합니다.
// 쿨다운을 적용한 이유는 빠르게 j를 두번 클릭하게 되면 gold point는 10 차감이 되는데 두번 펀치 스킬이 생성되는 문제가 있었기 때문입니다.

package Main;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.*;
import java.net.*;
import java.util.List;
import javax.swing.*;
import javax.swing.Timer;
import javax.swing.filechooser.FileNameExtensionFilter;

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

        // KeyListener 등록
        gamePanel.addKeyListener(new KeyAdapter() {
            private boolean isCooldown = false; // 쿨다운 플래그

            @Override
            public void keyPressed(KeyEvent e) {
                ChatMsg msg;
                if (e.getKeyCode() == KeyEvent.VK_LEFT) {

                    //1. 내 팔라독 좌표 업데이트 메시지 전송
                    msg = new ChatMsg(clientId, ChatMsg.MODE_PALADOG_MOVE_LEFT,"PALADOG_LEFT");
                    sendMessage(msg);

                    //2. 상대 다크독 좌표 업데이트 메시지 전송
                    // 팔라독의 현재 좌표-1 을 가져옴
                    int palaX = gamePanel.getPaladogX() - 1;
                    int darkdogX = 940 - palaX; // 상대 클라이언트의 다크독 위치 계산
                    int y = 190; // Y값은 고정 또는 필요에 따라 변경

                    // 좌표 데이터를 포함한 ChatMsg 객체 생성
                    msg = new ChatMsg(clientId, ChatMsg.MODE_TX_COORDINATE, darkdogX + "," + y);
                    sendMessage(msg);
                } else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                    // 팔라독을 오른쪽으로 이동
                    //gamePanel.getPaladog().moveRight();

                    //1. 내 팔라독 좌표 업데이트 메시지 전송
                    msg = new ChatMsg(clientId, ChatMsg.MODE_PALADOG_MOVE_RIGHT,"PALADOG_RIGHT");
                    sendMessage(msg);

                    //2. 상대 다크독 좌표 업데이트 메시지 전송
                    // 팔라독의 현재 좌표+1을 가져옴
                    int palaX = gamePanel.getPaladogX() + 1;
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

        chatInput.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String text = gamePanel.getChat();
                // 입력 필드에서 텍스트 가져오기
                if (!text.isEmpty()) {
                    ChatMsg chatMsg = new ChatMsg(clientId, ChatMsg.MODE_TX_STRING, text);
                    sendMessage(chatMsg); // 서버로 메시지 전송
                    gamePanel.printDisplay("Me: " + text); // 로컬에서도 채팅 추가
                }
                gamePanel.requestFocusInWindow(); // 채팅 후 GamePanel에 포커스 다시 설정
            }
        });

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
        chatInput.setText("");
        gamePanel.printDisplay(icon);

        // 추가: 이미지 전송 후 게임 화면에 포커스 요청하도록 했습니다.
        // GamePanel 포커스 설정
        gamePanel.setFocusable(true);
        gamePanel.requestFocusInWindow(); // 포커스를 요청
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

        @Override
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

        private void processMessage(ChatMsg msg) {
            // 수신한 메시지 처리 로직

            //내 메시지 나만수신.
            if(msg.getUserID().equals(clientId)){
                switch (msg.getMode()) {
                    case ChatMsg.MODE_PALADOG_MOVE_LEFT:
                        // 팔라독을 왼쪽으로 이동
                        gamePanel.getPaladog().moveLeft();
                        break;
                    case ChatMsg.MODE_PALADOG_MOVE_RIGHT:
                        // 팔라독을 오른쪽으로 이동
                        gamePanel.getPaladog().moveRight();
                        break;
                }
            }
            else if (!msg.getUserID().equals(clientId)) { //상대 메시지만 수신.
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
                        gamePanel.printDisplay(msg.getUserID() + ": " + msg.getMessage());
                        gamePanel.printDisplay(msg.getImage());

                        break;


                }
            }
            //상대방이 보냈든, 내가 보냈든 상관없이 실행하는 메시지들.
            switch (msg.getMode()) {

                case ChatMsg.MODE_ROOM_LIST:
                    List<String> rooms = (List<String>) msg.getData(); // 서버에서 최신 방 목록 수신
                    roomListModel.clear(); // 기존 방 목록 삭제
                    rooms.forEach(roomListModel::addElement); // 새로운 방 목록 추가
                    //setupHomeScreen(); // 홈 화면으로 돌아가기
                    break;


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

    private void updatePlayerList(DefaultListModel<String> playerListModel, List<String> players) {
        playerListModel.clear();
        for (String player : players) {
            playerListModel.addElement(player); // 사용자 이름과 준비 상태 추가
        }
    }

    public static void main(String[] args) {
        new GameClient();
    }
}
