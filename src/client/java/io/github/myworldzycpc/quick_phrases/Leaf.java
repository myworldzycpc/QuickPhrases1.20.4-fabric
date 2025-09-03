package io.github.myworldzycpc.quick_phrases;

public class Leaf {
    public String content;

    public enum Type {
        CHAT_MESSAGE,
    }

    public Type type;

    public Leaf(String content, Type type) {
        this.content = content;
        this.type = type;
    }

    public Leaf(String content) {
        this(content, Type.CHAT_MESSAGE);
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }
}
