package Main;

import javax.swing.*;
import java.io.Serializable;

// ChatMsg 클래스는 채팅 메시지를 나타내며, 직렬화 가능
public class ChatMsg implements Serializable {
    private static final long serialVersionUID = 1L; // 직렬화 버전 ID

    // 메시지 모드 상수 정의
    public static final int MODE_LOGIN = 1;    // 로그인 모드
    public static final int MODE_LOGOUT = 2;   // 로그아웃 모드

    public static final int MODE_TX_COORDINATE = 4; // 좌표 전송 모드

    public static final int MODE_TX_STRING = 16; // 텍스트 전송 모드
    public static final int MODE_TX_IMAGE = 64;  // 이미지 전송 모드

    public static final int MODE_SPAWN_UNIT = 32; // 유닛 소환 모드
    public static final int MODE_SPAWN_SKILL = 48; // 스킬 소환 모드

    public static final int MODE_GAME_START = 100; // 게임시작 모드

    private String userID;   // 사용자 ID
    private int mode;        // 메시지 모드
    private String message;  // 메시지 내용
    private ImageIcon image;    // 이미지 데이터

    // 생성자: 텍스트 메시지용
    public ChatMsg(String userID, int mode, String message) {
        this.userID = userID;
        this.mode = mode;
        this.message = message;
        this.image = null; // 이미지 없음
    }

    // 생성자 오버로딩: 이미지 포함 메시지용
    public ChatMsg(String userID, int mode, String message, ImageIcon image) {
        this.userID = userID;
        this.mode = mode;
        this.message = message;
        this.image = image;
    }

    // 사용자 ID 반환
    public String getUserID() {
        return userID;
    }

    // 메시지 모드 반환
    public int getMode() {
        return mode;
    }

    // 메시지 내용 반환
    public String getMessage() {
        return message;
    }

    // 이미지 데이터 반환
    public ImageIcon getImage() {
        return image;
    }

    // 메시지 출력 형식 재정의
    @Override
    public String toString() {
        if (mode == MODE_TX_IMAGE) {
            return userID + ": [Image]"; // 이미지 메시지 표시
        }
        return userID + ": " + message; // 텍스트 메시지 표시
    }
}
