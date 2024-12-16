//package Main;
//
//import java.io.*;
//import java.net.*;
//import java.awt.event.*;
//import java.util.concurrent.atomic.AtomicInteger;
//import javax.swing.*;
//
//public class GameClient {
//    private Socket socket; // 서버와의 연결을 위한 소켓
//    private BufferedReader in; // 서버로부터 메시지를 읽기 위한 입력 스트림
//    private PrintWriter out; // 서버로 메시지를 보내기 위한 출력 스트림
//    private GamePanel gamePanel; // 게임 화면을 관리하는 GamePanel 객체
//    private String clientId; // 클라이언트의 고유 ID
//
//    // 생성자: 서버에 연결하고 입력/출력 스트림 초기화
//    public GameClient(String serverAddress, int port, GamePanel gamePanel) {
//        this.gamePanel = gamePanel; // GamePanel 객체를 받아옴
//        try {
//            // 서버에 연결
//            socket = new Socket(serverAddress, port);
//            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//            out = new PrintWriter(socket.getOutputStream(), true);
//
//            // 서버로부터 고유 ID를 수신
//            clientId = in.readLine().split(":")[1]; // 서버에서 "YOUR_ID:CLIENT_X" 형식으로 전달
//            System.out.println("Assigned Client ID: " + clientId);
//
//            // 서버로부터 메시지를 처리하는 스레드를 시작
//            new Thread(new IncomingMessagesHandler()).start();
//        } catch (IOException e) {
//            e.printStackTrace(); // 연결 실패 시 예외 처리
//        }
//    }
//
//    // 서버로 메시지를 전송 (ID 포함)
//    public void sendMessage(String action) {
//        out.println(clientId + ":" + action); // 메시지에 ID 포함
//    }
//
//    // 서버로부터 메시지를 처리하는 내부 클래스
//    private class IncomingMessagesHandler implements Runnable {
//        @Override
//        public void run() {
//            try {
//                String message;
//                // 서버로부터 메시지를 읽어들임
//                while ((message = in.readLine()) != null) {
//                    // 읽은 메시지를 처리
//                    processServerMessage(message);
//                    System.out.println("Message received: " + message); // 디버그용 메시지 출력
//                }
//            } catch (IOException e) {
//                e.printStackTrace(); // 수신 중 오류 발생 시 예외 처리
//            }
//        }
//    }
//
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
//
//public static void main(String[] args) {
//    GamePanel gamePanel = new GamePanel();
//    GameClient client = new GameClient("localhost", 12345, gamePanel);
//
//    gamePanel.addKeyListener(new KeyAdapter() {
//        private boolean isLeftPressed = false;  // 왼쪽 이동 상태
//        private boolean isRightPressed = false; // 오른쪽 이동 상태
//
//        @Override
//        public void keyPressed(KeyEvent e) {
//            AtomicInteger darkdogX = new AtomicInteger(940-gamePanel.getPalaDogX());
//            int darkdogY = 190;
//
//            if (e.getKeyCode() == KeyEvent.VK_RIGHT && !isLeftPressed) {
//                isLeftPressed = true;  // 왼쪽 이동 활성화
//                new Thread(() -> {
//                    while (isLeftPressed) {
//                        if (darkdogX.get() > 0) { // x 좌표가 0보다 클 때만 감소
//                            darkdogX.getAndDecrement();
//                            client.sendMessage("UPDATE_POSITION:" + darkdogX + "," + darkdogY); // 서버에 위치 전송
//                        }
//                        try {
//                            Thread.sleep(10); // 움직임 속도 조절
//                        } catch (InterruptedException ex) {
//                            ex.printStackTrace();
//                        }
//                    }
//                }).start();
//            } else if (e.getKeyCode() == KeyEvent.VK_LEFT && !isRightPressed) {
//                isRightPressed = true;  // 오른쪽 이동 활성화
//                new Thread(() -> {
//                    while (isRightPressed) {
//                        if (darkdogX.get() < 1000) { // x 좌표가 1000보다 작을 때만 증가
//                            darkdogX.getAndIncrement();
//                            client.sendMessage("UPDATE_POSITION:" + darkdogX + "," + darkdogY); // 서버에 위치 전송
//                        }
//                        try {
//                            Thread.sleep(10); // 움직임 속도 조절
//                        } catch (InterruptedException ex) {
//                            ex.printStackTrace();
//                        }
//                    }
//                }).start();
//            }
//        }
//
//        @Override
//        public void keyReleased(KeyEvent e) {
//            if (e.getKeyCode() == KeyEvent.VK_LEFT) {
//                isLeftPressed = false;  // 왼쪽 이동 중지
//            } else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
//                isRightPressed = false;  // 오른쪽 이동 중지
//            }
//        }
//    });
//}
//
//}
//package Main;
//
//import java.io.*;
//import java.net.*;
//import java.awt.event.*;
//import javax.swing.*;
//
//public class GameClient {
//    private Socket socket; // 서버와의 연결을 위한 소켓
//    private BufferedReader in; // 서버로부터 메시지를 읽기 위한 입력 스트림
//    private PrintWriter out; // 서버로 메시지를 보내기 위한 출력 스트림
//    private GamePanel gamePanel; // 게임 화면을 관리하는 GamePanel 객체
//    private String clientId; // 클라이언트의 고유 ID
//
//    // 생성자: 서버에 연결하고 입력/출력 스트림 초기화
//    public GameClient(String serverAddress, int port, GamePanel gamePanel) {
//        this.gamePanel = gamePanel; // GamePanel 객체를 받아옴
//        try {
//            // 서버에 연결
//            socket = new Socket(serverAddress, port);
//            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//            out = new PrintWriter(socket.getOutputStream(), true);
//
//            // 서버로부터 고유 ID를 수신
//            clientId = in.readLine().split(":")[1]; // 서버에서 "YOUR_ID:CLIENT_X" 형식으로 전달
//            System.out.println("Assigned Client ID: " + clientId);
//
//            // 서버로부터 메시지를 처리하는 스레드를 시작
//            new Thread(new IncomingMessagesHandler()).start();
//        } catch (IOException e) {
//            e.printStackTrace(); // 연결 실패 시 예외 처리
//        }
//    }
//
//    // 서버로 메시지를 전송 (ID 포함)
//    public void sendMessage(String action) {
//        out.println(clientId + ":" + action); // 메시지에 ID 포함
//    }
//
//    // 서버로부터 메시지를 처리하는 내부 클래스
//    private class IncomingMessagesHandler implements Runnable {
//        @Override
//        public void run() {
//            try {
//                String message;
//                // 서버로부터 메시지를 읽어들임
//                while ((message = in.readLine()) != null) {
//                    // 읽은 메시지를 처리
//                    processServerMessage(message);
//                    System.out.println("Message received: " + message); // 디버그용 메시지 출력
//                }
//            } catch (IOException e) {
//                e.printStackTrace(); // 수신 중 오류 발생 시 예외 처리
//            }
//        }
//    }
//
//    // 읽은 메시지를 처리하는 메소드
//    private void processServerMessage(String message) {
//        // 메시지 형식: "CLIENT_ID:ACTION:DATA"
//        String[] parts = message.split(":");
//        if (parts.length < 3) return; // 형식이 잘못된 메시지는 무시
//
//        String senderId = parts[0]; // 메시지를 보낸 클라이언트 ID
//        String action = parts[1]; // 메시지의 동작
//        String data = parts[2]; // 추가 데이터
//
//        SwingUtilities.invokeLater(() -> {
//            if (!senderId.equals(clientId)) { // 상대 클라이언트로부터 받은 메시지
//                switch (action) {
//                    case "UPDATE_POSITION":
//                        // "x,y" 형식의 좌표 데이터 처리
//                        String[] coords = data.split(",");
//                        if (coords.length == 2) {
//                            try {
//                                int x = Integer.parseInt(coords[0].trim());
//                                int y = Integer.parseInt(coords[1].trim());
//                                gamePanel.updateDarkDogPosition(x, y); // 다크독 위치 업데이트
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
//
//    public static void main(String[] args) {
//        GamePanel gamePanel = new GamePanel();
//        GameClient client = new GameClient("localhost", 12345, gamePanel);
//
//        gamePanel.addKeyListener(new KeyAdapter() {
//            @Override
//            public void keyPressed(KeyEvent e) {
//                if (e.getKeyCode() == KeyEvent.VK_LEFT) {
//                    gamePanel.getPaladog().moveLeft();
//                    //gamePanel.spawnZombie();
//
//                    // 팔라독의 위치를 서버로 전송
//                    int palaX = gamePanel.getPaladogX();
//                    int darkdogX = 940 - palaX; // 상대 클라이언트의 다크독 위치 계산
//                    client.sendMessage("UPDATE_POSITION:" + darkdogX + ",190");
//                } else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
//                    gamePanel.getPaladog().moveRight();
//                    // 팔라독의 위치를 서버로 전송
//                    int palaX = gamePanel.getPaladogX();
//                    int darkdogX = 940 - palaX; // 상대 클라이언트의 다크독 위치 계산
//                    client.sendMessage("UPDATE_POSITION:" + darkdogX + ",190");
//                }
//            }
//        });
//    }
//}

//package Main;
//
//import java.io.*;
//import java.net.*;
//import java.awt.event.*;
//import javax.swing.*;
//import javax.swing.Timer;
//
//public class GameClient {
//    private Socket socket; // 서버와의 연결을 위한 소켓
//    private BufferedReader in; // 서버로부터 메시지를 읽기 위한 입력 스트림
//    private PrintWriter out; // 서버로 메시지를 보내기 위한 출력 스트림
//    private GamePanel gamePanel; // 게임 화면을 관리하는 GamePanel 객체
//    private String clientId; // 클라이언트의 고유 ID
//
//    // 생성자: 서버에 연결하고 입력/출력 스트림 초기화
//    public GameClient(String serverAddress, int port, GamePanel gamePanel) {
//        this.gamePanel = gamePanel; // GamePanel 객체를 받아옴
//        try {
//            // 서버에 연결
//            socket = new Socket(serverAddress, port);
//            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//            out = new PrintWriter(socket.getOutputStream(), true);
//
//            // 서버로부터 고유 ID를 수신
//            clientId = in.readLine().split(":")[1]; // 서버에서 "YOUR_ID:CLIENT_X" 형식으로 전달
//            System.out.println("Assigned Client ID: " + clientId);
//
//            // 서버로부터 메시지를 처리하는 스레드를 시작
//            new Thread(new IncomingMessagesHandler()).start();
//        } catch (IOException e) {
//            e.printStackTrace(); // 연결 실패 시 예외 처리
//        }
//    }
//
//    // 서버로 메시지를 전송 (ID 포함)
//    public void sendMessage(String action) {
//        out.println(clientId + ":" + action); // 메시지에 ID 포함
//    }
//
//    // 서버로부터 메시지를 처리하는 내부 클래스
//    private class IncomingMessagesHandler implements Runnable {
//        @Override
//        public void run() {
//            try {
//                String message;
//                // 서버로부터 메시지를 읽어들임
//                while ((message = in.readLine()) != null) {
//                    // 읽은 메시지를 처리
//                    processServerMessage(message);
//                    System.out.println("Message received: " + message); // 디버그용 메시지 출력
//                }
//            } catch (IOException e) {
//                e.printStackTrace(); // 수신 중 오류 발생 시 예외 처리
//            }
//        }
//    }
//
//    // 읽은 메시지를 처리하는 메소드
//    private void processServerMessage(String message) {
//        // 메시지 형식: "CLIENT_ID:ACTION:DATA"
//        String[] parts = message.split(":");
//        if (parts.length < 3) return; // 형식이 잘못된 메시지는 무시
//
//        String senderId = parts[0]; // 메시지를 보낸 클라이언트 ID
//        String action = parts[1]; // 메시지의 동작
//        String data = parts[2]; // 추가 데이터
//
//        SwingUtilities.invokeLater(() -> {
//            if (!senderId.equals(clientId)) { // 상대 클라이언트로부터 받은 메시지
//                switch (action) {
//                    case "UPDATE_POSITION":
//                        // "x,y" 형식의 좌표 데이터 처리
//                        String[] coords = data.split(",");
//                        if (coords.length == 2) {
//                            try {
//                                int x = Integer.parseInt(coords[0].trim());
//                                int y = Integer.parseInt(coords[1].trim());
//                                gamePanel.updateDarkDogPosition(x, y); // 다크독 위치 업데이트
//                                gamePanel.getDarkdog().x = x;
//                                gamePanel.getDarkdog().y = y;
//                            } catch (NumberFormatException e) {
//                                e.printStackTrace(); // 좌표 파싱 실패 시 예외 처리
//                            }
//                        }
//                        break;
//                    case "SPAWN_UNIT":
//                        if (data.equals("MOUSE")) {
//                            gamePanel.spawnZombieForDarkDog();
//                        }
//                        break;
//                    case "SPAWN_SKILL":
//                        if (data.equals("PUNCH")) {
//                            gamePanel.spawnDarkDogPunch();
//                        }
//                        break;
//                    default:
//                        break;
//                }
//            }
//        });
//    }
//
//    public static void main(String[] args) {
//        GamePanel gamePanel = new GamePanel();
//        GameClient client = new GameClient("localhost", 12345, gamePanel);
//        gamePanel.addKeyListener(new KeyAdapter() {
//            private boolean isCooldown = false; // 쿨다운 플래그
//
//            @Override
//            public void keyPressed(KeyEvent e) {
//                if (e.getKeyCode() == KeyEvent.VK_LEFT) {
//                    gamePanel.getPaladog().moveLeft();
//
//                    // 팔라독의 위치를 서버로 전송
//                    int palaX = gamePanel.getPaladogX();
//                    int darkdogX = 940 - palaX; // 상대 클라이언트의 다크독 위치 계산
//                    client.sendMessage("UPDATE_POSITION:" + darkdogX + ",190");
//                } else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
//                    gamePanel.getPaladog().moveRight();
//
//                    // 팔라독의 위치를 서버로 전송
//                    int palaX = gamePanel.getPaladogX();
//                    int darkdogX = 940 - palaX; // 상대 클라이언트의 다크독 위치 계산
//                    client.sendMessage("UPDATE_POSITION:" + darkdogX + ",190");
//                } else if (e.getKeyChar() == '1') { // 1키 입력 시 좀비 유닛 소환 요청
//                    if (!isCooldown) { // 쿨다운이 아닐 때만 실행
//                        if (gamePanel.sohwanhp >= 10) {
//                            client.sendMessage("SPAWN_UNIT:MOUSE");
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
//
//                } else if (e.getKeyChar() == 'j' || e.getKeyChar() == 'J') {
//                    if (!isCooldown) { // 쿨다운이 아닐 때만 실행
//                        if (gamePanel.skillmp >= 10) {
//                            client.sendMessage("SPAWN_SKILL:PUNCH");
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
//    }
//}



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

//    // 생성자: 서버에 연결하고 입력/출력 스트림 초기화
//    public GameClient(String serverAddress, int port, GamePanel gamePanel) {
//        this.gamePanel = gamePanel; // GamePanel 객체를 받아옴
//        try {
//            // 서버에 연결
//            socket = new Socket(serverAddress, port);
//            out = new ObjectOutputStream(socket.getOutputStream());
//            in = new ObjectInputStream(socket.getInputStream());
//
//            // 서버로부터 고유 ID를 수신
//            ChatMsg initialMsg = (ChatMsg) in.readObject(); // 서버에서 ChatMsg 객체 수신
//            if (initialMsg.getMode() == ChatMsg.MODE_LOGIN) {
//                clientId = initialMsg.getUserID();
//                System.out.println("Assigned Client ID: " + clientId);
//            }
//
//            // 서버로부터 메시지를 처리하는 스레드를 시작
//            new Thread(new IncomingMessagesHandler()).start();
//        } catch (IOException | ClassNotFoundException e) {
//            e.printStackTrace(); // 연결 실패 시 예외 처리
//        }
//    }
// GameClient.java
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
        JButton sendImageButton = gamePanel.getSendImageButton(); // 이미지 전송 버튼 참조
        if (sendImageButton != null) { // 이미지 전송 기능이 있는 경우
            sendImageButton.addActionListener(e -> {
                JFileChooser fileChooser = new JFileChooser();
                if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                    File file = fileChooser.getSelectedFile();
                    try (FileInputStream fis = new FileInputStream(file)) {
                        byte[] imageData = fis.readAllBytes();
                        ChatMsg chatMsg = new ChatMsg(clientId, ChatMsg.MODE_TX_IMAGE, "[Image]", imageData);
                        sendMessage(chatMsg);
                        gamePanel.appendChatMessage("Me: [Image Sent]");
                    } catch (IOException ex) {
                        System.err.println("Failed to send image.");
                        ex.printStackTrace();
                    }
                }
            });
        }
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




    //    public static void main(String[] args) {
//        GamePanel gamePanel = new GamePanel();
//        GameClient client = new GameClient("localhost", 12345, gamePanel);
//
//        gamePanel.addKeyListener(new KeyAdapter() {
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
//                    msg = new ChatMsg(client.clientId, ChatMsg.MODE_TX_STRING, darkdogX + "," + y);
//                    client.sendMessage(msg);
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
//                    msg = new ChatMsg(client.clientId, ChatMsg.MODE_TX_STRING, darkdogX + "," + y);
//                    client.sendMessage(msg);
//                }
//            }
//        });
//
//    }
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

