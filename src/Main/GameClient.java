// GameClient.java
package Main;

import java.io.*;
import java.net.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.Timer;

public class GameClient {
    private Socket socket; // 서버와의 연결을 위한 소켓
    private ObjectInputStream in; // 서버로부터 객체 메시지를 읽기 위한 입력 스트림
    private ObjectOutputStream out; // 서버로 객체 메시지를 보내기 위한 출력 스트림
    private GamePanel gamePanel; // 게임 화면을 관리하는 GamePanel 객체
    private String clientId; // 클라이언트의 고유 ID

    public GameClient(String serverAddress, int port, GamePanel gamePanel) {
        this.gamePanel = gamePanel; // GamePanel 객체를 받아옴

        try {
            // 서버에 연결
            socket = new Socket(serverAddress, port);
            System.out.println("Connected to server at " + serverAddress + ":" + port);

            // 입력/출력 스트림 초기화
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            // 서버로부터 고유 ID 수신
            ChatMsg initialMsg = (ChatMsg) in.readObject(); // 서버에서 ChatMsg 객체 수신
            if (initialMsg.getMode() == ChatMsg.MODE_LOGIN) {
                clientId = initialMsg.getUserID();
                System.out.println("Assigned Client ID: " + clientId);
            } else {
                System.err.println("Unexpected initial message mode: " + initialMsg.getMode());
            }

            // 서버로부터 메시지를 처리하는 스레드 시작
            new Thread(new IncomingMessagesHandler()).start();

            // 게임 패널에 초기 포커스 설정
            gamePanel.setFocusable(true);
            gamePanel.requestFocusInWindow();

            // 채팅 메시지 전송 리스너 설정
            setupChatListeners();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Failed to connect to server or initialize streams.");
            e.printStackTrace();
        }
    }

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


    // 서버로 메시지를 전송 (ChatMsg 객체 사용)
    public void sendMessage(ChatMsg msg) {
        try {
            out.writeObject(msg);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 서버로부터 메시지를 처리하는 내부 클래스
    private class IncomingMessagesHandler implements Runnable {
        @Override
        public void run() {
            try {
                while (true) {
                    // 서버로부터 ChatMsg 객체를 읽어들임
                    ChatMsg msg = (ChatMsg) in.readObject();
                    processServerMessage(msg);
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace(); // 수신 중 오류 발생 시 예외 처리
            }
        }
    }

    private void processServerMessage(ChatMsg msg) {
        // 디버깅용 로그 추가
        System.out.println("Received Message - UserID: " + msg.getUserID() + ", Mode: " + msg.getMode() + ", Message: " + msg.getMessage());

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

                    default:
                        System.out.println("Unhandled Mode: " + msg.getMode());
                        break;
                }
            }
        });
    }

    public static void main(String[] args) {
        GamePanel gamePanel = new GamePanel();
        GameClient client = new GameClient("localhost", 12345, gamePanel);

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
                    msg = new ChatMsg(client.clientId, ChatMsg.MODE_TX_COORDINATE, darkdogX + "," + y);
                    client.sendMessage(msg);
                } else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                    // 팔라독을 오른쪽으로 이동
                    gamePanel.getPaladog().moveRight();

                    // 팔라독의 현재 좌표를 가져옴
                    int palaX = gamePanel.getPaladogX();
                    int darkdogX = 940 - palaX; // 상대 클라이언트의 다크독 위치 계산
                    int y = 190; // Y값은 고정 또는 필요에 따라 변경

                    // 좌표 데이터를 포함한 ChatMsg 객체 생성
                    msg = new ChatMsg(client.clientId, ChatMsg.MODE_TX_COORDINATE, darkdogX + "," + y);
                    client.sendMessage(msg);
                } else if (e.getKeyChar() == '1') { // 1키 입력 시 좀비 유닛 소환 요청
                    if (!isCooldown) { // 쿨다운이 아닐 때만 실행
                        if (gamePanel.sohwanhp >= 10) {
                            // 좀비 유닛 소환 명령 객체 생성 및 전송
                            msg = new ChatMsg(client.clientId, ChatMsg.MODE_SPAWN_UNIT, "MOUSE");
                            client.sendMessage(msg);

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
                            msg = new ChatMsg(client.clientId, ChatMsg.MODE_SPAWN_SKILL, "PUNCH");
                            client.sendMessage(msg);

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

    }
}

