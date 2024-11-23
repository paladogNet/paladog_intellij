package Main;

import java.io.*;
import java.net.*;

public class GameClient {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    public GameClient(String serverAddress, int port) {
        try {
            socket = new Socket(serverAddress, port);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            // 수신 스레드 시작
            new Thread(new IncomingMessagesHandler()).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(String message) {
        out.println(message);
    }

    private class IncomingMessagesHandler implements Runnable {
        @Override
        public void run() {
            try {
                String message;
                while ((message = in.readLine()) != null) {
                    System.out.println("서버로부터 메시지 수신: " + message);
                    // 게임 상태 업데이트
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        GameClient client = new GameClient("localhost", 12345);
        // 예: 키보드 입력으로 메시지 전송
        client.sendMessage("팔라독 움직임 좌표: x=100, y=200");
    }
}

//import Main.GamePanel;
//
//import java.io.*;
//import java.net.*;
//import java.awt.event.*;
//import javax.swing.*;
//
//public class GameClient {
//    private Socket socket;
//    private BufferedReader in;
//    private PrintWriter out;
//    private GamePanel gamePanel;
//
//    public GameClient(String serverAddress, int port, GamePanel gamePanel) {
//        this.gamePanel = gamePanel;
//        try {
//            // 서버에 연결
//            socket = new Socket(serverAddress, port);
//            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//            out = new PrintWriter(socket.getOutputStream(), true);
//
//            // 서버로부터 메시지를 처리하는 스레드 시작
//            new Thread(new IncomingMessagesHandler()).start();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    // 서버에 메시지 전송
//    public void sendMessage(String message) {
//        out.println(message);
//    }
//
//    // 서버로부터 메시지 수신
//    private class IncomingMessagesHandler implements Runnable {
//        @Override
//        public void run() {
//            try {
//                String message;
//                while ((message = in.readLine()) != null) {
//                    // 메시지 처리
//                    processServerMessage(message);
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//    }
//
//    // 서버로부터 받은 메시지를 처리
//    private void processServerMessage(String message) {
//        // 메시지 형식: "ACTION:DATA"
//        String[] parts = message.split(":");
//        if (parts.length < 2) return;
//
//        String action = parts[0];
//        String data = parts[1];
//
//        SwingUtilities.invokeLater(() -> {
//            switch (action) {
//                case "MOVE_LEFT":
//                    gamePanel.getPaladog().moveLeft();
//                    break;
//                case "MOVE_RIGHT":
//                    gamePanel.getPaladog().moveRight();
//                    break;
//                case "ATTACK":
//                    gamePanel.punchAttack(); // 펀치 공격 처리
//                    break;
//                case "SPAWN_UNIT":
//                    gamePanel.spawnUnit(data); // 유닛 생성
//                    break;
//                default:
//                    break;
//            }
//        });
//    }
//
//    public static void main(String[] args) {
//        // 게임 패널 생성
//        GamePanel gamePanel = new GamePanel();
//
//        // 클라이언트 생성
//        GameClient client = new GameClient("localhost", 12345, gamePanel);
//
//        // 키보드 입력 처리
//        gamePanel.addKeyListener(new KeyAdapter() {
//            @Override
//            public void keyPressed(KeyEvent e) {
//                if (e.getKeyCode() == KeyEvent.VK_LEFT) {
//                    client.sendMessage("MOVE_LEFT");
//                } else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
//                    client.sendMessage("MOVE_RIGHT");
//                } else if (e.getKeyChar() == 'j') {
//                    client.sendMessage("ATTACK");
//                } else if (e.getKeyChar() == '1') {
//                    client.sendMessage("SPAWN_UNIT:MOUSE");
//                } else if (e.getKeyChar() == '3') {
//                    client.sendMessage("SPAWN_UNIT:BEAR");
//                }
//            }
//        });
//    }
//}
