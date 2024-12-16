package Main;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.List;

public class GameServer extends JFrame {
    private static final int PORT = 12345;
    private static final List<ClientHandler> clientHandlers = new ArrayList<>(); // 모든 클라이언트 핸들러 저장
    private ServerSocket serverSocket;
    private volatile boolean isRunning = false;
    private static JTextArea t_display;

    public GameServer() {
        super("Game Server");

        buildGUI();

        this.setBounds(600, 100, 400, 300);
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setVisible(true);
    }

    private void buildGUI() {
        JPanel logPanel = new JPanel(new BorderLayout());
        JPanel buttonPanel = new JPanel(new GridLayout(1,0));

        JButton startButton = new JButton("서버 실행");
        JButton stopButton = new JButton("서버 중지");

        t_display = new JTextArea();
        t_display.setEditable(false);

        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startServer();
            }
        });

        stopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                stopServer();
            }
        });

        logPanel.add(new JScrollPane(t_display), BorderLayout.CENTER);
        buttonPanel.add(startButton);
        buttonPanel.add(stopButton);

        this.add(logPanel, BorderLayout.CENTER);
        this.add(buttonPanel, BorderLayout.SOUTH);
    }

    // 클라이언트와 통신을 처리하는 핸들러
    private static class ClientHandler extends Thread {
        private static int clientCounter = 0; // 클라이언트 고유 ID 생성용 카운터
        private final String clientId; // 클라이언트 고유 ID
        private Socket socket;
        private BufferedReader in;
        private PrintWriter out;

        public ClientHandler(Socket socket) {
            this.socket = socket;
            this.clientId = "CLIENT_" + (++clientCounter); // 고유 ID 생성
        }

        @Override
        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                // 클라이언트에 고유 ID 전송
                out.println("YOUR_ID:" + clientId);

                String message;
                while ((message = in.readLine()) != null) {
                    printDisplay(clientId + "로부터 메시지 수신: " + message);

                    // 모든 클라이언트에게 메시지 브로드캐스트 (ID 포함)
                    broadcastMessage(clientId, message);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                // 클라이언트 연결 종료 처리
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                synchronized (clientHandlers) {
                    clientHandlers.remove(this); // 핸들러 리스트에서 제거
                }
            }
        }

        // 메시지를 모든 클라이언트에게 브로드캐스트
        private void broadcastMessage(String senderId, String message) {
            synchronized (clientHandlers) {
                for (ClientHandler handler : clientHandlers) {
                    if (handler != this) { // 송신자를 제외한 모든 클라이언트에게 메시지 전송
                        //handler.out.println(senderId + ":" + message);
                        handler.out.println(message);
                    }
                }
            }
        }
    }

    private void startServer() {
        printDisplay("서버 실행 중...");
        isRunning = true;

        new Thread(() -> {
            try {
                serverSocket = new ServerSocket(PORT);
                while (isRunning) {
                    Socket clientSocket = serverSocket.accept();
                    printDisplay("클라이언트 연결됨: " + clientSocket.getInetAddress());

                    // 새 클라이언트 핸들러 실행
                    ClientHandler clientHandler = new ClientHandler(clientSocket);
                    synchronized (clientHandlers) {
                        clientHandlers.add(clientHandler); // 핸들러 리스트에 추가
                    }
                    clientHandler.start(); // 스레드 시작
                }
            } catch (IOException e) {
                if (isRunning) {
                    e.printStackTrace();
                }
            } finally {
                stopServer(); // 서버 소켓이 닫힌 경우 서버 종료 처리
            }
        }).start();
    }

    private void stopServer() {
        System.out.println("서버 중지 중...");
        isRunning = false;

        // 클라이언트 핸들러 종료
        synchronized (clientHandlers) {
            for (ClientHandler handler : clientHandlers) {
                try {
                    handler.socket.close(); // 클라이언트 소켓 닫기
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            clientHandlers.clear(); // 핸들러 리스트 비우기
        }

        // 서버 소켓 닫기
        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        printDisplay("서버가 중지되었습니다.");
    }

    private static void printDisplay(String msg) {
        t_display.append(msg + "\n");
        t_display.setCaretPosition(t_display.getDocument().getLength());
    }


    public static void main(String[] args) {
        GameServer gameServer = new GameServer();
    }
}
