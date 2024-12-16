//package Main;
//
//import java.io.*;
//import java.net.*;
//import java.util.*;
//
//public class GameServer {
//    private static final int PORT = 12345;
//    private static final List<ClientHandler> clientHandlers = new ArrayList<>(); // 모든 클라이언트 핸들러 저장
//
//    public static void main(String[] args) {
//        System.out.println("서버 실행 중...");
//        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
//            while (true) {
//                Socket clientSocket = serverSocket.accept();
//                System.out.println("클라이언트 연결됨: " + clientSocket.getInetAddress());
//
//                // 새 클라이언트 핸들러 실행
//                ClientHandler clientHandler = new ClientHandler(clientSocket);
//                synchronized (clientHandlers) {
//                    clientHandlers.add(clientHandler); // 핸들러 리스트에 추가
//                }
//                clientHandler.start(); // 스레드 시작
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    // 클라이언트와 통신을 처리하는 핸들러
//    private static class ClientHandler extends Thread {
//        private static int clientCounter = 0; // 클라이언트 고유 ID 생성용 카운터
//        private final String clientId; // 클라이언트 고유 ID
//        private Socket socket;
//        private BufferedReader in;
//        private PrintWriter out;
//
//        public ClientHandler(Socket socket) {
//            this.socket = socket;
//            this.clientId = "CLIENT_" + (++clientCounter); // 고유 ID 생성
//        }
//
//        @Override
//        public void run() {
//            try {
//                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//                out = new PrintWriter(socket.getOutputStream(), true);
//
//                // 클라이언트에 고유 ID 전송
//                out.println("YOUR_ID:" + clientId);
//
//                String message;
//                while ((message = in.readLine()) != null) {
//                    System.out.println(clientId + "로부터 메시지 수신: " + message);
//
//                    // 모든 클라이언트에게 메시지 브로드캐스트 (ID 포함)
//                    broadcastMessage(clientId, message);
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            } finally {
//                // 클라이언트 연결 종료 처리
//                try {
//                    socket.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//                synchronized (clientHandlers) {
//                    clientHandlers.remove(this); // 핸들러 리스트에서 제거
//                }
//            }
//        }
//
//        // 메시지를 모든 클라이언트에게 브로드캐스트
//        private void broadcastMessage(String senderId, String message) {
//            synchronized (clientHandlers) {
//                for (ClientHandler handler : clientHandlers) {
//                    if (handler != this) { // 송신자를 제외한 모든 클라이언트에게 메시지 전송
//                        //handler.out.println(senderId + ":" + message);
//                        handler.out.println(message);
//                    }
//                }
//            }
//        }
//    }
//}


// GameServer.java
package Main;

import java.io.*;
import java.net.*;
import java.util.*;

public class GameServer {
    private static final int PORT = 12345;
    private static final List<ClientHandler> clientHandlers = new ArrayList<>(); // 모든 클라이언트 핸들러 저장

    public static void main(String[] args) {
        System.out.println("서버 실행 중...");
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("클라이언트 연결됨: " + clientSocket.getInetAddress());

                // 새 클라이언트 핸들러 실행
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                synchronized (clientHandlers) {
                    clientHandlers.add(clientHandler); // 핸들러 리스트에 추가
                }
                clientHandler.start(); // 스레드 시작
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 클라이언트와 통신을 처리하는 핸들러
    private static class ClientHandler extends Thread {
        private static int clientCounter = 0; // 클라이언트 고유 ID 생성용 카운터
        private final String clientId; // 클라이언트 고유 ID
        private Socket socket;
        private ObjectInputStream in;
        private ObjectOutputStream out;

        public ClientHandler(Socket socket) {
            this.socket = socket;
            this.clientId = "CLIENT_" + (++clientCounter); // 고유 ID 생성
        }

        @Override
        public void run() {
            try {
                out = new ObjectOutputStream(socket.getOutputStream());
                in = new ObjectInputStream(socket.getInputStream());

                // 클라이언트에 고유 ID 전송
                ChatMsg loginMsg = new ChatMsg(clientId, ChatMsg.MODE_LOGIN, "Welcome!");
                out.writeObject(loginMsg);
                out.flush();

                ChatMsg message;
                while ((message = (ChatMsg) in.readObject()) != null) {
                    System.out.println(clientId + "로부터 메시지 수신: " + message);

                    // 모든 클라이언트에게 메시지 브로드캐스트
                    broadcastMessage(message);
                }
            } catch (IOException | ClassNotFoundException e) {
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
        private void broadcastMessage(ChatMsg message) {
            synchronized (clientHandlers) {
                for (ClientHandler handler : clientHandlers) {
                    try {
                        handler.out.writeObject(message);
                        handler.out.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}