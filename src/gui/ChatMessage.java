package gui;

public class ChatMessage {
    public final String text;
    public final double timeCreated;

    public ChatMessage(String text, double timeCreated) {
        this.text = text;
        this.timeCreated = timeCreated;
    }
}