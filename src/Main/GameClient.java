package Main;

import java.io.*;
import java.net.*;
//
//public class GameClient {
//    private Socket socket;
//    private BufferedReader in;
//    private PrintWriter out;
//
//    public GameClient(String serverAddress, int port) {
//        try {
//            socket = new Socket(serverAddress, port);
//            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//            out = new PrintWriter(socket.getOutputStream(), true);
//
//            // 수신 스레드 시작
//            new Thread(new IncomingMessagesHandler()).start();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    public void sendMessage(String message) {
//        out.println(message);
//    }
//
//    private class IncomingMessagesHandler implements Runnable {
//        @Override
//        public void run() {
//            try {
//                String message;
//                while ((message = in.readLine()) != null) {
//                    System.out.println("서버로부터 메시지 수신: " + message);
//                    // 게임 상태 업데이트
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//    }
//
//    public static void main(String[] args) {
//        GameClient client = new GameClient("localhost", 12345);
//        // 예: 키보드 입력으로 메시지 전송
//        client.sendMessage("팔라독 움직임 좌표: x=100, y=200");
//    }
//}

import java.io.*;
import java.net.*;
import java.awt.event.*;
import javax.swing.*;

public class GameClient {
    private Socket socket; // 서버와의 연결을 위한 소켓
    private BufferedReader in; // 서버로부터 메시지를 읽기 위한 입력 스트림
    private PrintWriter out; // 서버로 메시지를 보내기 위한 출력 스트림
    private GamePanel gamePanel; // 게임 화면을 관리하는 GamePanel 객체

    // 생성자: 서버에 연결하고 입력/출력 스트림 초기화
    public GameClient(String serverAddress, int port, GamePanel gamePanel) {
        this.gamePanel = gamePanel; // GamePanel 객체를 받아옴
        try {
            // 서버에 연결
            socket = new Socket(serverAddress, port);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            // 서버로부터 메시지를 처리하는 스레드를 시작
            new Thread(new IncomingMessagesHandler()).start();
        } catch (IOException e) {
            e.printStackTrace(); // 연결 실패 시 예외 처리
        }
    }

    // 서버로 메시지를 전송
    public void sendMessage(String message) {
        out.println(message); // 메시지를 서버로 보냄
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

    // 서버로부터 받은 메시지를 처리하는 메서드
    private void processServerMessage(String message) {
        // 메시지 형식: "ACTION:DATA"
        String[] parts = message.split(":"); // ":"를 기준으로 메시지를 분리
        if (parts.length < 2) return; // 형식이 잘못된 메시지는 무시

        String action = parts[0]; // 메시지의 동작 (예: MOVE_LEFT)
        String data = parts[1]; // 메시지의 데이터 (예: 유닛 정보)

        // 메시지에 따른 동작 수행 (Swing 스레드에서 실행)
        SwingUtilities.invokeLater(() -> {
            switch (action) {
                case "MOVE_LEFT": // 왼쪽으로 이동
                    gamePanel.getPaladog().moveLeft();
                    break;
                case "MOVE_RIGHT": // 오른쪽으로 이동
                    gamePanel.getPaladog().moveRight();
                    break;
                case "ATTACK": // 펀치 공격
                    gamePanel.punchAttack();
                    break;
                case "SPAWN_UNIT": // 유닛 생성      SPAWN_UNIT : mouse
                    gamePanel.spawnUnit(data);
                    break;
                case "START_GAME":
                    new GamePanel();
                    break;
                default: // 알 수 없는 명령은 무시
                    break;
            }
        });
    }

    public static void main(String[] args) {


        // GamePanel 생성 (게임 화면 초기화)
        GamePanel gamePanel = new GamePanel();

        // GameClient 생성 및 서버 연결
        GameClient client = new GameClient("localhost", 12345, gamePanel);

        // 키보드 입력 처리: 사용자가 키를 눌렀을 때 동작
        gamePanel.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                // 왼쪽 화살표 키 입력 시 "MOVE_LEFT" 메시지 전송
                if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                    client.sendMessage("MOVE_LEFT");
                }
                // 오른쪽 화살표 키 입력 시 "MOVE_RIGHT" 메시지 전송
                else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                    client.sendMessage("MOVE_RIGHT");
                }
                // 'j' 키 입력 시 "ATTACK" 메시지 전송
                else if (e.getKeyChar() == 'j') {
                    client.sendMessage("ATTACK");
                }
                // '1' 키 입력 시 "SPAWN_UNIT:MOUSE" 메시지 전송 (마우스 유닛 생성)
                else if (e.getKeyChar() == '1') {
                    client.sendMessage("SPAWN_UNIT:MOUSE");
                }
                // '3' 키 입력 시 "SPAWN_UNIT:BEAR" 메시지 전송 (곰 유닛 생성)
                else if (e.getKeyChar() == '3') {
                    client.sendMessage("SPAWN_UNIT:BEAR");
                }
            }
        });
    }
}



