package umk.jakuburb.mars.Teraformacja.Marsa.game;

public class ChatRecord {

    private String userName;
    private String msg;

    public ChatRecord() {
    }

    public ChatRecord(String userName, String msg) {
        this.userName = userName;
        this.msg = msg;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
