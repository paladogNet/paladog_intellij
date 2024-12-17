// 해당 java파일의 내용은 전부 직접 작성하였습니다.

package Main;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.List;

public class GameServer extends JFrame{
    private static final int PORT = 12345; // 서버 포트 번호(우선 고정하였습니다. 동적 포트번호 범위로 PORT 멤버변수를 바꾸면 해당 포트를 사용하여 통신을 하게 됩니다.)
    private ServerSocket serverSocket; // 서버소켓
    private final List<ClientHandler> clientHandlers = new ArrayList<>(); // 클라이언트 핸들러 목록(클라이언트 한명당 1요소씩.)
    private final Map<String, Room> rooms = new HashMap<>(); // 방 목록(맵 사용)
    private static JTextArea t_display; // 서버로그를 출력하는 텍스트영역

    public GameServer() {
        super("Game Server");
        buildGUI();//생성자에 buildGUI() 추가하여 new GameServer(); 시 바로 서버프레임 보이게함.
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

    // 1대 1로 클라이언트 요청을 처리하는 ClientHandler 클래스 입니다.
    class ClientHandler extends Thread {
        private Socket socket;
        private ObjectInputStream in;
        private ObjectOutputStream out;
        String userID;
        private Room currentRoom;

        //생성자에서 ObjectOutputStream,ObjectInputStream을 초기화하고, 후에 계속 사용하도록 했습니다.
        public ClientHandler(Socket socket) {
            this.socket = socket;
            try {
                out = new ObjectOutputStream(socket.getOutputStream());
                in = new ObjectInputStream(socket.getInputStream());
            } catch (IOException e) { e.printStackTrace(); }
        }

        //무한으로 ChatMsg 객체를 읽어 handleMessage() 함수로 메세지를 처리하도록 했습니다.
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
        // 클라이언트로부터 읽어들인 메시지(ChatMsg객체)를 처리하는 메소드입니다.
        private void handleMessage(ChatMsg msg) throws IOException {
            userID = msg.getUserID();
            switch (msg.getMode()) { // switch문으로 모드를 조회하여 각 모드에 맞는 차별적인 동작을 수행하도록 했습니다.
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

                    // 모든 클라이언트에게 방 목록 갱신하기
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

                case ChatMsg.MODE_LOGOUT:// 로그아웃 처리 -> 방에서 퇴장할 때(홈으로 나가기) 사용하는 모드 채널입니다.
                    if (currentRoom != null) {
                        currentRoom.removePlayer(this);
                    }
                    broadcastRoomList();
                    break;

                // 내 팔라독을 움직이려 방향키를 누르면 서버 주도 렌더링을 하기 위해서 바로 클라이언트에서
                case ChatMsg.MODE_PALADOG_MOVE_LEFT:
                    broadcastMessage(msg);
                    break;

                case ChatMsg.MODE_PALADOG_MOVE_RIGHT:
                    broadcastMessage(msg);
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

        // 메시지를 모든 클라이언트에게 브로드캐스트. 가장 일반적인 브로드캐스팅입니다.
        // 저희는 이 메소드를 사용하여 먼저 모든 클라이언트에게 브로드캐스팅을 한 후
        // 클라이언트 측에서 비즈니스 로직에 따라 본인이 처리할 메시지인지 여부를 판별하여 처리를 수행하도록 로직을 구성했습니다.(서버측 판별로 할걸 후회도 있습니다..!)
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

    // withTalk 을 참고하여 적은 메소드입니다. 서버 시작을 하면 디스플레이에 서버의 IP 주소가 뜨도록 했습니다.
    private String getLocalAddr(){
        try{
            return InetAddress.getLocalHost().getHostAddress(); //로컬 IP 주소 반환
        } catch (UnknownHostException e){
            return "Unknown"; //IP 주소를 얻지 못했을 때 기본값 반환
        }
    }

    class Room {
        private String roomName;
        // 방에 참여 중인 클라이언트와 해당 클라이언트 들의 준비 상태를 저장하는 맵
        private Map<ClientHandler, Boolean> players = new HashMap<>();

        public Room(ClientHandler clientHandler) {
            this.roomName = clientHandler.userID + "_Room";
            players.put(clientHandler, false);
        }

        public String getRoomName() { return roomName; }


        public synchronized boolean addPlayer(ClientHandler handler) {
            if (players.size() < 2) { // 최대 2명까지 참여 가능
                players.put(handler, false); // 새로운 플레이어 추가 (대기 중 상태 false)
                broadcastPlayerList(); // 사용자 추가 시 사용자 목록 브로드캐스트
                return true; // 추가 성공
            }
            return false; // 방이 가득 찬 경우 추가 실패
        }

        // 클라이언트가 대기방에서 준비 버튼을 누르면 해당 메소드가 실행됩니다.
        // 준비 상태 변경 시 사용자 목록을 브로드캐스트하여 상대 플레이어의 준비상태도 실시간 확인가능하도록 했습니다.
        public synchronized void setReady(ClientHandler clientHandler) {
            players.put(clientHandler, true); // 준비 상태 설정
            broadcastPlayerList(); // 준비 상태 변경 시 사용자 목록 브로드캐스트
        }

        //모든 플레이어에게 현재 사용자 목록을 브로드캐스트하는 커스텀 브로드캐스트 메소드입니다.
        public synchronized void broadcastPlayerList() {
            List<String> playerStatus = getPlayerList(); // 사용자 목록과 준비 상태 가져오기
            for (ClientHandler player : players.keySet()) {
                try {
                    player.sendMessage(new ChatMsg("SERVER", ChatMsg.MODE_ROOM_JOIN, roomName, playerStatus)); // 유저아이디에는 적절한 말을 고민하다 웹에서 보고 "SERVER" 사용했습니다.
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        // 사용자가 홈으로 나가기 버튼을 누르면 해당 메소드가 실행됩니다.
        public synchronized void removePlayer(ClientHandler handler) {
            players.remove(handler); // 사용자 제거
            broadcastPlayerList(); // 남은 사용자에게 목록 브로드캐스트
        }

        // 두명의 플레이어가 준비버튼을 누르게 되면 해당 메소드를 호출 할때 true를 반환합니다.
        public synchronized boolean isGameReady() {
            return players.size() == 2 && players.values().stream().allMatch(ready -> ready);
        }

        // 사용자 목록과 준비 상태를
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
                ClientHandler player = entry.getKey(); // ClientHandler 객체
                player.sendMessage(startMsg);
                System.out.println(player.userID + " 에게 게임 시작 메시지 전송");
            }
        }

    }

    private void startServer() {
        printDisplay("서버 실행 중...");

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

