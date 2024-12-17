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

        startButton.addActionListener(e -> startServer());

        stopButton.addActionListener(e -> stopServer());

        logPanel.add(new JScrollPane(t_display), BorderLayout.CENTER);
        buttonPanel.add(startButton);
        buttonPanel.add(stopButton);

        this.add(logPanel, BorderLayout.CENTER);
        this.add(buttonPanel, BorderLayout.SOUTH);
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
                    // 방 생성
                    Room room = new Room(this); // 현재 ClientHandler 객체(this)를 방에 추가
                    rooms.put(room.getRoomName(), room); // 방 목록에 추가
                    printDisplay("방 생성됨: " + room.getRoomName());

                    // 클라이언트를 자동으로 방에 입장시키기
                    joinRoom(room);

                    // 방 입장 메시지 전송
                    sendMessage(new ChatMsg("SERVER", ChatMsg.MODE_ROOM_JOIN, room.getRoomName(), room.getPlayerList()));

                    // 모든 클라이언트에게 방 목록 갱신
                    broadcastRoomList();
                    break;
                case ChatMsg.MODE_ROOM_JOIN:
                    Room roomToJoin = rooms.get(msg.getMessage());
                    if (roomToJoin != null && roomToJoin.addPlayer(this)) {
                        joinRoom(roomToJoin);
                        // 방 이름과 사용자 목록을 함께 전송
                        sendMessage(new ChatMsg("SERVER", ChatMsg.MODE_ROOM_JOIN, roomToJoin.getRoomName(), roomToJoin.getPlayerList()));
                        broadcastRoomList();
                    } else {
                        sendMessage(new ChatMsg("SERVER", ChatMsg.MODE_ROOM_FULL, "방이 꽉 찼습니다."));
                    }
                    break;

                case ChatMsg.MODE_READY:
                    currentRoom.setReady(this);
                    if (currentRoom.isGameReady()) {
                        currentRoom.startGame();
                    }
                    break;

                case ChatMsg.MODE_ROOM_LIST:
                    broadcastRoomListToClient(this);
                    break;

                case ChatMsg.MODE_LOGOUT:
                    if (currentRoom != null) {
                        currentRoom.removePlayer(this);
                        //currentRoom = null;
                    }
                    broadcastRoomList();
                    break;
            }
        }

        // 클라이언트에게만 방 목록 전송
        private void broadcastRoomListToClient(ClientHandler client) throws IOException {
            List<String> roomNames = new ArrayList<>(rooms.keySet());
            client.sendMessage(new ChatMsg("SERVER", ChatMsg.MODE_ROOM_LIST, "방 목록", roomNames));
        }

        private void joinRoom(Room room) throws IOException {
            currentRoom = room;
            currentRoom.addPlayer(this); //?????????????????????????????????????
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

    private String getLocalAddr(){
        try{
            return InetAddress.getLocalHost().getHostAddress(); //로컬 IP 주소 반환
        } catch (UnknownHostException e){
            return "Unknown"; //IP 주소를 얻지 못했을 때 기본값 반환
        }
    }

    class Room {
        private String roomName;
        private Map<ClientHandler, Boolean> players = new HashMap<>();

        public Room(ClientHandler clientHandler) {
            this.roomName = clientHandler.userID + "_Room";
            players.put(clientHandler, false);
        }
        public boolean isEmpty() {
            return players.isEmpty();
        }

        public String getRoomName() { return roomName; }


        public synchronized boolean addPlayer(ClientHandler handler) {
            if (players.size() < 2) {
                players.put(handler, false);
                broadcastPlayerList(); // 사용자 추가 시 사용자 목록 브로드캐스트
                return true;
            }
            return false;
        }


        public synchronized void setReady(ClientHandler clientHandler) {
            players.put(clientHandler, true); // 준비 상태 설정
            broadcastPlayerList(); // 준비 상태 변경 시 사용자 목록 브로드캐스트
        }

        public synchronized void broadcastPlayerList() {
            List<String> playerStatus = getPlayerList(); // 사용자 목록과 준비 상태 가져오기
            for (ClientHandler player : players.keySet()) {
                try {
                    player.sendMessage(new ChatMsg("SERVER", ChatMsg.MODE_ROOM_JOIN, roomName, playerStatus));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        public synchronized void removePlayer(ClientHandler handler) {
            players.remove(handler); // 사용자 제거
            broadcastPlayerList(); // 남은 사용자에게 목록 브로드캐스트
        }


        public synchronized boolean isGameReady() {
            return players.size() == 2 && players.values().stream().allMatch(ready -> ready);
        }

        public synchronized List<String> getPlayerList() {
            List<String> playerList = new ArrayList<>();
            for (ClientHandler player : players.keySet()) {
                String readyStatus = players.get(player) ? " (준비 완료)" : " (대기 중)";
                playerList.add(player.userID + readyStatus);
            }
            return playerList;
        }

        public void startGame() throws IOException {
            ChatMsg startMsg = new ChatMsg("SERVER", ChatMsg.MODE_GAME_START, "Game Start!");
            for (Map.Entry<ClientHandler, Boolean> entry : players.entrySet()) {
                ClientHandler player = entry.getKey(); // ClientHandler 객체 //???????????????????????????????????????????????????
                player.sendMessage(startMsg);
                System.out.println(player.userID + " 에게 게임 시작 메시지 전송");
            }
        }

        public List<String> getPlayers() {
            List<String> playerList = new ArrayList<>();
            for (ClientHandler handler : players.keySet()) {
                String status = players.get(handler) ? "(준비 완료)" : "(대기 중)";
                playerList.add(handler.userID + " " + status);
            }
            return playerList;
        }

    }

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
}

