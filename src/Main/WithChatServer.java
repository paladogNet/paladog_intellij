package Main;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.Vector;

public class WithChatServer extends JFrame {

    private static final long serialVersionUID = 1L; //'The serializable class WithChatServer does not declare a static final serialVersionUID field of type long'라는 노란 경고 게속 뜨길래 추가했음.
    private int port; //서버가 실행될 포트를 저장
    private ServerSocket serverSocket = null; //클라이언트 연결을 받기 위한 ServerSocket 객체
    private Thread acceptThread = null; //클라이언트 연결 요청을 처리할 스레드
    private Vector<ClientHandler> users = new Vector<>(); //서버에 연결된 클라이언트를 관리하기 위한 벡터

    private JTextArea t_display; //서버 로그를 출력할 텍스트 영역
    private JButton b_connect, b_disconnect, b_exit; //서버 제어 버튼

    public WithChatServer(int port){
        super("2091273 서버 : With Chat Server"); //JFrame 생성자 호출, 서버 이름 설정
        this.port = port; //사용자가 설정한 포트를 저장
        buildGUI(); //GUI 구성
        setSize(400, 300); //서버 창 크기 설정
        setLocation(500, 0); //창 위치 설정
        setDefaultCloseOperation(EXIT_ON_CLOSE); //창 종료 시 프로그램 종료
        setVisible(true); //창 보이게 설정
    }

    private void buildGUI(){
        add(createDisplayPanel(), BorderLayout.CENTER); //로그 표시 패널을 중앙에 추가
        add(createControlPanel(), BorderLayout.SOUTH); //버튼 패널을 아래쪽에 추가
    }

    private JPanel createDisplayPanel(){
        JPanel p = new JPanel(new BorderLayout()); //로그 표시용 패널 생성
        t_display = new JTextArea(); //텍스트 영역 생성
        t_display.setEditable(false); //로그를 수정하지 못하게 설정
        p.add(new JScrollPane(t_display), BorderLayout.CENTER); //텍스트 영역을 스크롤 가능한 패널에 추가
        return p; //패널 반환
    }

    private JPanel createControlPanel(){
        JPanel p = new JPanel(new GridLayout(0, 3)); //버튼을 3개로 나눌 패널 생성
        b_connect = new JButton("서버 시작"); //서버 시작 버튼 생성
        b_disconnect = new JButton("서버 종료"); //서버 종료 버튼 생성
        b_exit = new JButton("종료"); //프로그램 종료 버튼 생성

        b_disconnect.setEnabled(false); //서버 종료 버튼은 서버가 실행 중일 때만 활성화됨

        b_connect.addActionListener(e -> startServer()); //서버 시작 버튼 클릭 시 startServer() 호출
        b_disconnect.addActionListener(e -> disconnect()); //서버 종료 버튼 클릭 시 disconnect() 호출
        b_exit.addActionListener(e -> System.exit(0)); //종료 버튼 클릭 시 프로그램 종료

        p.add(b_connect); //버튼 패널에 추가
        p.add(b_disconnect);
        p.add(b_exit);
        return p; //버튼 패널 반환
    }

    private void printDisplay(String msg){
        t_display.append(msg + "\n"); //로그 메시지를 텍스트 영역에 추가
        t_display.setCaretPosition(t_display.getDocument().getLength()); //스크롤을 항상 최신 메시지로 유지
    }

    private void startServer(){
        try{
            serverSocket = new ServerSocket(port); //포트에 서버 소켓 바인딩
            printDisplay("서버가 시작되었습니다: " + getLocalAddr()); //서버 시작 메시지 출력

            acceptThread = new Thread(() -> { //클라이언트 연결 요청을 처리할 스레드 시작
                while (acceptThread == Thread.currentThread()){ //현재 스레드가 활성 상태일 때
                    try{
                        Socket clientSocket = serverSocket.accept(); //클라이언트 연결 요청 수락
                        printDisplay("클라이언트가 연결되었습니다: " + clientSocket.getInetAddress().getHostAddress());

                        ClientHandler handler = new ClientHandler(clientSocket); //클라이언트 처리용 핸들러 생성
                        users.add(handler); //벡터에 핸들러 추가
                        handler.start(); //클라이언트 처리 스레드 시작
                    } catch (IOException e){
                        printDisplay("클라이언트 연결 오류: " + e.getMessage()); //오류 발생 시 로그 출력
                    }
                }
            });
            acceptThread.start(); //클라이언트 연결 처리 스레드 시작

            b_connect.setEnabled(false); //서버 시작 버튼 비활성화
            b_disconnect.setEnabled(true); //서버 종료 버튼 활성화
        } catch (IOException e){
            printDisplay("서버 시작 오류: " + e.getMessage()); //서버 시작 중 오류 발생 시 로그 출력
        }
    }

    private void disconnect(){
        try{
            if (acceptThread != null) acceptThread = null; //스레드 종료
            if (serverSocket != null) serverSocket.close(); //서버 소켓 닫기
            for (ClientHandler user : users) user.disconnect(); //모든 클라이언트 연결 종료
            users.clear(); //벡터 초기화
            printDisplay("서버가 종료되었습니다."); //종료 메시지 출력

            b_connect.setEnabled(true); //서버 시작 버튼 활성화
            b_disconnect.setEnabled(false); //서버 종료 버튼 비활성화
        } catch (IOException e){
            printDisplay("서버 종료 오류: " + e.getMessage()); //서버 종료 중 오류 발생 시 로그 출력
        }
    }

    private String getLocalAddr(){
        try{
            return InetAddress.getLocalHost().getHostAddress(); //로컬 IP 주소 반환
        } catch (UnknownHostException e){
            return "Unknown"; //IP 주소를 얻지 못했을 때 기본값 반환
        }
    }

    private class ClientHandler extends Thread {
        private Socket clientSocket; //클라이언트 소켓
        private ObjectOutputStream out; //클라이언트로 데이터를 보내는 스트림
        private ObjectInputStream in; //클라이언트로부터 데이터를 받는 스트림
        private String userID; //클라이언트의 사용자 ID

        public ClientHandler(Socket clientSocket) throws IOException{
            this.clientSocket = clientSocket; //클라이언트 소켓 저장
            this.out = new ObjectOutputStream(clientSocket.getOutputStream()); //출력 스트림 초기화
            this.in = new ObjectInputStream(clientSocket.getInputStream()); //입력 스트림 초기화
        }

        @Override
        public void run(){
            try{
                while (true){ //클라이언트 메시지를 계속 수신
                    ChatMsg msg = (ChatMsg) in.readObject(); //클라이언트로부터 메시지 읽기
                    handleMessage(msg); //메시지 처리
                }
            } catch (IOException | ClassNotFoundException e){
                //통신 중 오류 발생 시 클라이언트 연결 종료
            } finally{
                disconnect(); //스레드 종료 시 클라이언트 연결 해제
            }
        }

        private void handleMessage(ChatMsg msg) throws IOException{

            switch (msg.getMode()){ //msg.getMode() 즉 메시지 모드에 따라 case 나눔

                case ChatMsg.MODE_LOGIN:
                    userID = msg.getUserID(); //사용자 ID 저장
                    printDisplay("새 참가자: " + userID); //로그에 참가자 정보 출력
                    printDisplay("현재 참가자 수: " + users.size()); //현재 참가자 수 출력
                    break;

                case ChatMsg.MODE_LOGOUT:
                    users.remove(this); //벡터에서 클라이언트 핸들러 제거!!
                    printDisplay(userID + " 퇴장. 현재 참가자 수: " + users.size()); //퇴장 메시지 출력
                    disconnect(); //클라이언트 연결 해제시킴
                    break;

                case ChatMsg.MODE_TX_STRING:
                    printDisplay(msg.toString()); //텍스트 메시지 출력
                    broadcasting(msg); //다른 클라이언트에 메시지 전달
                    break;

                case ChatMsg.MODE_TX_IMAGE:
                    //이미지 메시지가 수신되면 서버 디스플레이에 msg.getUserID() + ": [Image] " + msg.getMessage() 출력 후 브로드캐스팅. 즉 모든 사용자에게 이미지 전달
                    printDisplay(msg.getUserID() + ": [Image] " + msg.getMessage());
                    broadcasting(msg);
                    break;
            }
        }

        public void send(ChatMsg msg) throws IOException{
            out.writeObject(msg); //클라이언트로 메시지 전송.
            out.flush(); //flush()해서 버퍼 비워버리기
        }

        //모든 사용자에게 메시지 전달해주는 함수!!!
        public void broadcasting(ChatMsg msg){
            for (ClientHandler user : users){ //모든 연결된 클라이언트에게 메시지 전송
                try{
                    user.send(msg); //메시지 전송
                } catch (IOException e){
                    printDisplay("브로드캐스팅 실패");
                }
            }
        }

        public void disconnect(){
            try{
                if (clientSocket != null) clientSocket.close(); //클라이언트 소켓 닫기
            } catch (IOException e){
                //클라이언트 소켓 닫기 실패 시 로그 출력
                printDisplay("클라이언트 disconnect 실패");
            }
        }
    }

    public static void main(String[] args){
        int port = 54321; //기본 포트 설정. 동적포트중에 하나로 일부러 설정했음
        new WithChatServer(port); //서버 WithChatServer 시작
    }
}

