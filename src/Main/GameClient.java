package Main;

import java.io.*;
import java.net.*;
import java.awt.event.*;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.*;

public class GameClient {
    private Socket socket; // 서버와의 연결을 위한 소켓
    private BufferedReader in; // 서버로부터 메시지를 읽기 위한 입력 스트림
    private PrintWriter out; // 서버로 메시지를 보내기 위한 출력 스트림
    private GamePanel gamePanel; // 게임 화면을 관리하는 GamePanel 객체
    private String clientId; // 클라이언트의 고유 ID

    // 생성자: 서버에 연결하고 입력/출력 스트림 초기화
    public GameClient(String serverAddress, int port, GamePanel gamePanel) {
        this.gamePanel = gamePanel; // GamePanel 객체를 받아옴
        try {
            // 서버에 연결
            socket = new Socket(serverAddress, port);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            // 서버로부터 고유 ID를 수신
            clientId = in.readLine().split(":")[1]; // 서버에서 "YOUR_ID:CLIENT_X" 형식으로 전달
            System.out.println("Assigned Client ID: " + clientId);

            // 서버로부터 메시지를 처리하는 스레드를 시작
            new Thread(new IncomingMessagesHandler()).start();
        } catch (IOException e) {
            e.printStackTrace(); // 연결 실패 시 예외 처리
        }
    }

    // 서버로 메시지를 전송 (ID 포함)
    public void sendMessage(String action) {
        out.println(clientId + ":" + action); // 메시지에 ID 포함
    }

    // 서버로부터 메시지를 처리하는 내부 클래스
    private class IncomingMessagesHandler implements Runnable {
        @Override
        public void run() {
            try {
                String message;
                // 서버로부터 메시지를 읽어들임
                while ((message = in.readLine()) != null) {
                    // 읽은 메시지를 처리
                    processServerMessage(message);
                    System.out.println("Message received: " + message); // 디버그용 메시지 출력
                }
            } catch (IOException e) {
                e.printStackTrace(); // 수신 중 오류 발생 시 예외 처리
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
private void processServerMessage(String message) {
    // 메시지 형식: "CLIENT_ID:ACTION:DATA" 또는 "CLIENT_ID:UPDATE_POSITION:x,y"
    String[] parts = message.split(":");
    if (parts.length < 3) return;

    String senderId = parts[0]; // 메시지를 보낸 클라이언트 ID
    String action = parts[1];  // 동작
    String data = parts[2];    // 좌표 데이터

    SwingUtilities.invokeLater(() -> {
        if (senderId.equals(clientId)) {
            // 자신의 캐릭터 동작
            switch (action) {
                case "MOVE_LEFT":
                    gamePanel.getPaladog().moveLeft();
                    break;
                case "MOVE_RIGHT":
                    gamePanel.getPaladog().moveRight();
                    break;
                case "ATTACK":
                    gamePanel.punchAttack();
                    break;
                case "SPAWN_UNIT":
                    gamePanel.spawnUnit(data);
                    break;
            }
        } else {
            // DarkDog의 좌표 업데이트
            switch (action) {
                case "UPDATE_POSITION":
                    String[] coords = data.split(",");
                    if (coords.length == 2) {
                        try {
                            int x = Integer.parseInt(coords[0].trim());
                            int y = Integer.parseInt(coords[1].trim());
                            gamePanel.updateDarkDogPosition(x, y); // DarkDog 위치 업데이트
                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
            }
        }
    });
}

public static void main(String[] args) {
    GamePanel gamePanel = new GamePanel();
    GameClient client = new GameClient("localhost", 12345, gamePanel);

    gamePanel.addKeyListener(new KeyAdapter() {
        private boolean isLeftPressed = false;  // 왼쪽 이동 상태
        private boolean isRightPressed = false; // 오른쪽 이동 상태

        @Override
        public void keyPressed(KeyEvent e) {
            AtomicInteger darkdogX = new AtomicInteger(gamePanel.getDarkDogX());
            int darkdogY = gamePanel.getDarkDogY();

            if (e.getKeyCode() == KeyEvent.VK_LEFT && !isLeftPressed) {
                isLeftPressed = true;  // 왼쪽 이동 활성화
                new Thread(() -> {
                    while (isLeftPressed) {
                        if (darkdogX.get() > 0) { // x 좌표가 0보다 클 때만 감소
                            darkdogX.getAndDecrement();
                            client.sendMessage("UPDATE_POSITION:" + darkdogX + "," + darkdogY); // 서버에 위치 전송
                        }
                        try {
                            Thread.sleep(10); // 움직임 속도 조절
                        } catch (InterruptedException ex) {
                            ex.printStackTrace();
                        }
                    }
                }).start();
            } else if (e.getKeyCode() == KeyEvent.VK_RIGHT && !isRightPressed) {
                isRightPressed = true;  // 오른쪽 이동 활성화
                new Thread(() -> {
                    while (isRightPressed) {
                        if (darkdogX.get() < 1000) { // x 좌표가 1000보다 작을 때만 증가
                            darkdogX.getAndIncrement();
                            client.sendMessage("UPDATE_POSITION:" + darkdogX + "," + darkdogY); // 서버에 위치 전송
                        }
                        try {
                            Thread.sleep(10); // 움직임 속도 조절
                        } catch (InterruptedException ex) {
                            ex.printStackTrace();
                        }
                    }
                }).start();
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                isLeftPressed = false;  // 왼쪽 이동 중지
            } else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                isRightPressed = false;  // 오른쪽 이동 중지
            }
        }
    });
}

}
