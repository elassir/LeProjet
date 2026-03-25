package org.example.leprojet.common;
import org.example.leprojet.Action;

import java.io.Serializable;

public class Message implements Serializable {
    private static final long serialVersionUID = 1L;
    private String sender;
    private Action action;
    private String content;

    public Message(String sender, String content) {
        this.sender = sender;
        this.action = action;
        this.content = content;
    }

    public void setSender(String sender) { this.sender = sender; }

    public String getContent(){
        return this.content;
    }

    public Action getAction() {
        return this.action;
    }

    @Override
    public String toString() {
        return sender + " : " + content;
    }
}