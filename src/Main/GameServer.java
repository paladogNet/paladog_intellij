package Main;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.List;

public class GameServer extends JFrame{
    private static final int PORT = 12345;
    private ServerSocket serverSocket;
    private final List<ClientHandler> clientHandlers = new ArrayList<>();
    private final Map<String, Room> rooms = new HashMap<>(); // 방 관리
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

    private void stopServer() {
        System.out.println("서버 중지 중...");
        //isRunning = false;

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
        new GameServer();
    }

//    public void start() {
//        System.out.println("서버 시작...");
//        try {
//            serverSocket = new ServerSocket(PORT);
//            while (true) {
//                Socket socket = serverSocket.accept();
//                ClientHandler handler = new ClientHandler(socket);
//                clientHandlers.add(handler);
//                handler.start();
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

    private void startServer() {
        printDisplay("서버 실행 중...");
        //isRunning = true;

        new Thread(() -> {
            try {
                serverSocket = new ServerSocket(PORT);
                printDisplay("서버가 시작되었습니다: " + getLocalAddr()); //서버 시작 메시지 출력

                while (true) {
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
                    e.printStackTrace();

            } finally {
                stopServer(); // 서버 소켓이 닫힌 경우 서버 종료 처리
            }
        }).start();
    }
    private String getLocalAddr(){
        try{
            return InetAddress.getLocalHost().getHostAddress(); //로컬 IP 주소 반환
        } catch (UnknownHostException e){
            return "Unknown"; //IP 주소를 얻지 못했을 때 기본값 반환
        }
    }

    class ClientHandler extends Thread {
        private Socket socket;
        private ObjectInputStream in;
        private ObjectOutputStream out;
        String userID;
        private Room currentRoom;

        public ClientHandler(Socket socket) {
            this.socket = socket;
            try {
                out = new ObjectOutputStream(socket.getOutputStream());
                in = new ObjectInputStream(socket.getInputStream());
            } catch (IOException e) { e.printStackTrace(); }
        }

        public void run() {
            try {
                while (true) {
                    ChatMsg msg = (ChatMsg) in.readObject();
                    handleMessage(msg);
                }
            } catch (IOException | ClassNotFoundException e) {
                System.out.println(userID + " 연결 종료");
            }
        }

        private void handleMessage(ChatMsg msg) throws IOException {
            userID = msg.getUserID();
            switch (msg.getMode()) {

                case ChatMsg.MODE_TX_COORDINATE:


                case ChatMsg.MODE_SPAWN_SKILL:


                case ChatMsg.MODE_SPAWN_UNIT:
                    broadcastMessage(msg);
                    break;

                case ChatMsg.MODE_TX_STRING: // 텍스트 채팅 메시지 처리
                    broadcastMessage(msg);
                    break;

                case ChatMsg.MODE_TX_IMAGE: // 이미지 채팅 메시지 처리
                    broadcastMessage(msg);
                    break;


                case ChatMsg.MODE_ROOM_CREATE:
                    Room room = new Room(this); // this는 현재 ClientHandler 객체
                    rooms.put(room.getRoomName(), room); // 방 목록에 추가
                    joinRoom(room);
                    broadcastRoomList();
                    break;

                case ChatMsg.MODE_ROOM_JOIN:
                    Room roomToJoin = rooms.get(msg.getMessage());
                    if (roomToJoin != null && roomToJoin.addPlayer(this)) {
                        joinRoom(roomToJoin);
                        broadcastRoomList();
                    } else {
                        sendMessage(new ChatMsg("SERVER", ChatMsg.MODE_ROOM_FULL, "방이 꽉 찼습니다."));
                    }
                    break;


                case ChatMsg.MODE_READY:
                    currentRoom.setReady(this); // this는 현재 ClientHandler 객체
                    if (currentRoom.isGameReady()) {
                        currentRoom.startGame();
                    }
                    break;

                case ChatMsg.MODE_ROOM_LIST:
                    broadcastRoomList();
                    break;
            }
        }

        private void joinRoom(Room room) throws IOException {
            currentRoom = room;
            currentRoom.addPlayer(this);
            sendMessage(new ChatMsg("SERVER", ChatMsg.MODE_ROOM_JOIN, "방에 입장했습니다."));
        }

        private void sendMessage(ChatMsg msg) throws IOException {
            out.writeObject(msg);
            out.flush();
        }

        private void broadcastRoomList() throws IOException {
            List<String> roomNames = new ArrayList<>(rooms.keySet());
            for (ClientHandler client : clientHandlers) {
                client.sendMessage(new ChatMsg("SERVER", ChatMsg.MODE_ROOM_LIST, "방 목록", roomNames));
            }
        }

        // 메시지를 모든 클라이언트에게 브로드캐스트
        private void broadcastMessage(ChatMsg message) {
            synchronized (clientHandlers) {
                for (ClientHandler handler : clientHandlers) {
                    try {
                        synchronized (handler.out) { // 스트림 동기화
                            handler.out.writeObject(message);
                            handler.out.flush();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    class Room {
        private String roomName;
        private Map<ClientHandler, Boolean> players = new HashMap<>();

        public Room(ClientHandler clientHandler) {
            this.roomName = clientHandler.userID + "_Room";
            players.put(clientHandler, false);
        }

        public String getRoomName() { return roomName; }

        public synchronized boolean addPlayer(GameServer.ClientHandler handler) {
            if (players.size() < 2) {
                players.put(handler, false);
                return true;
            }
            return false;
        }

        public synchronized void setReady(ClientHandler clientHandler) {
            players.put(clientHandler, true);
        }

        public synchronized boolean isGameReady() {
            return players.size() == 2 && players.values().stream().allMatch(ready -> ready);
        }

        public void startGame() throws IOException {
            ChatMsg startMsg = new ChatMsg("SERVER", ChatMsg.MODE_GAME_START, "Game Start!");
            for (Map.Entry<ClientHandler, Boolean> entry : players.entrySet()) {
                ClientHandler player = entry.getKey(); // ClientHandler 객체
                player.sendMessage(startMsg);
                System.out.println(player.userID + " 에게 게임 시작 메시지 전송");
            }
        }


    }
}






//package Main;
//
//import javax.swing.*;
//import java.awt.*;
//import java.awt.event.ActionEvent;
//import java.awt.event.ActionListener;
//import java.io.*;
//import java.net.*;
//import java.util.*;
//import java.util.List;
//
//public class GameServer extends JFrame {
//    private static final int PORT = 12345;
//    private static final List<ClientHandler> clientHandlers = new ArrayList<>(); // 모든 클라이언트 핸들러 저장
//    private ServerSocket serverSocket;
//    private volatile boolean isRunning = false;
//    private static JTextArea t_display;
//
//    public GameServer() {
//        super("Game Server");
//
//        buildGUI();
//
//        this.setBounds(600, 100, 400, 300);
//        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
//        this.setVisible(true);
//    }
//
//    private void buildGUI() {
//        JPanel logPanel = new JPanel(new BorderLayout());
//        JPanel buttonPanel = new JPanel(new GridLayout(1,0));
//
//        JButton startButton = new JButton("서버 실행");
//        JButton stopButton = new JButton("서버 중지");
//
//        t_display = new JTextArea();
//        t_display.setEditable(false);
//
//        startButton.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                startServer();
//            }
//        });
//
//        stopButton.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                stopServer();
//            }
//        });
//
//        logPanel.add(new JScrollPane(t_display), BorderLayout.CENTER);
//        buttonPanel.add(startButton);
//        buttonPanel.add(stopButton);
//
//        this.add(logPanel, BorderLayout.CENTER);
//        this.add(buttonPanel, BorderLayout.SOUTH);
//    }
//
//    // 클라이언트와 통신을 처리하는 핸들러
//    private static class ClientHandler extends Thread {
//        private static int clientCounter = 0; // 클라이언트 고유 ID 생성용 카운터
//        private final String clientId; // 클라이언트 고유 ID
//        private Socket socket;
//        private ObjectInputStream in;
//        private ObjectOutputStream out;
//
//        public ClientHandler(Socket socket) {
//            this.socket = socket;
//            this.clientId = "CLIENT_" + (++clientCounter); // 고유 ID 생성
//            try {
//                out = new ObjectOutputStream(socket.getOutputStream());
//                out.flush();
//                in = new ObjectInputStream(socket.getInputStream());
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//
//        private void sendMessage(ChatMsg message) {
//            try {
//                synchronized (out) {
//                    out.writeObject(message);
//                    out.flush();
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//
//        @Override
//        public void run() {
//            try {
//                System.out.println("클라이언트 핸들러 시작: " + clientId);
//
//                ChatMsg loginMsg = new ChatMsg(clientId, ChatMsg.MODE_LOGIN, "Welcome!");
//                sendMessage(loginMsg);
//
//                // 메시지 처리 루프
//                ChatMsg message;
//                while ((message = (ChatMsg) in.readObject()) != null) {
//                    printDisplay(clientId + "로부터 메시지 수신: " + message);
//                    broadcastMessage(message);
//                }
//            } catch (IOException | ClassNotFoundException e) {
//                e.printStackTrace();
//            } finally {
//                closeResources();
//            }
//        }
//
//        private void closeResources() {
//            try {
//                if (in != null) in.close();
//                if (out != null) out.close();
//                if (socket != null) socket.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//
//
//        // 메시지를 모든 클라이언트에게 브로드캐스트
//        private void broadcastMessage(ChatMsg message) {
//            synchronized (clientHandlers) {
//                for (ClientHandler handler : clientHandlers) {
//                    try {
//                        synchronized (handler.out) { // 스트림 동기화
//                            handler.out.writeObject(message);
//                            handler.out.flush();
//                        }
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//        }
//
//    }
//
//    private void startGameIfReady() {
//        synchronized (clientHandlers) {
//            if (clientHandlers.size() == 2) {
//                // 클라이언트 2명 연결됨 -> 시작 신호 전송
//                ChatMsg startMsg = new ChatMsg("SERVER", ChatMsg.MODE_GAME_START, null);
//                for (ClientHandler handler : clientHandlers) {
//                    handler.sendMessage(startMsg);
//                }
//            }
//        }
//    }
//
//    private void startServer() {
//        printDisplay("서버 실행 중...");
//        isRunning = true;
//
//        new Thread(() -> {
//            try {
//                serverSocket = new ServerSocket(PORT);
//                printDisplay("서버가 시작되었습니다: " + getLocalAddr()); //서버 시작 메시지 출력
//
//                while (isRunning) {
//                    Socket clientSocket = serverSocket.accept();
//                    printDisplay("클라이언트 연결됨: " + clientSocket.getInetAddress());
//
//                    // 새 클라이언트 핸들러 실행
//                    ClientHandler clientHandler = new ClientHandler(clientSocket);
//                    synchronized (clientHandlers) {
//                        clientHandlers.add(clientHandler); // 핸들러 리스트에 추가
//                    }
//                    clientHandler.start(); // 스레드 시작
//
//                    startGameIfReady();
//                }
//            } catch (IOException e) {
//                if (isRunning) {
//                    e.printStackTrace();
//                }
//            } /*finally {
//                stopServer(); // 서버 소켓이 닫힌 경우 서버 종료 처리
//            }*/
//        }).start();
//    }
//
//    private void stopServer() {
//        System.out.println("서버 중지 중...");
//        isRunning = false;
//
//        // 클라이언트 핸들러 종료
//        synchronized (clientHandlers) {
//            for (ClientHandler handler : clientHandlers) {
//                try {
//                    handler.socket.close(); // 클라이언트 소켓 닫기
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//            clientHandlers.clear(); // 핸들러 리스트 비우기
//        }
//
//        // 서버 소켓 닫기
//        if (serverSocket != null && !serverSocket.isClosed()) {
//            try {
//                serverSocket.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//
//        printDisplay("서버가 중지되었습니다.");
//    }
//
//    private static void printDisplay(String msg) {
//        t_display.append(msg + "\n");
//        t_display.setCaretPosition(t_display.getDocument().getLength());
//    }
//    private String getLocalAddr(){
//        try{
//            return InetAddress.getLocalHost().getHostAddress(); //로컬 IP 주소 반환
//        } catch (UnknownHostException e){
//            return "Unknown"; //IP 주소를 얻지 못했을 때 기본값 반환
//        }
//    }
//
//    public static void main(String[] args) {
//        GameServer gameServer = new GameServer();
//    }
//}
