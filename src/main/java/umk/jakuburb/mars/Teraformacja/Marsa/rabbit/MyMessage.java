package umk.jakuburb.mars.Teraformacja.Marsa.rabbit;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.List;

public class MyMessage implements Serializable {

    @JsonProperty("messageType")
    private MessageType messageType;

    @JsonProperty("from")
    private String from;

    @JsonProperty("msg")
    private List<String> msg;

    @JsonProperty("lobby")
    private String lobby;

    public MyMessage(){}

    public MyMessage(MessageType messageType, String from, List<String> msg, String lobby) {
        this.messageType = messageType;
        this.from = from;
        this.msg = msg;
        this.lobby = lobby;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public List<String> getMsg() {
        return msg;
    }

    public void setMsg(List<String> msg) {
        this.msg = msg;
    }

    public String getLobby() {
        return lobby;
    }

    public void setLobby(String lobby) {
        this.lobby = lobby;
    }

    @Override
    public String toString() {
        return "MyMessage{" +
                "messageType=" + messageType +
                ", from='" + from + '\'' +
                ", msg=" + msg +
                ", lobby='" + lobby + '\'' +
                '}';
    }
}
