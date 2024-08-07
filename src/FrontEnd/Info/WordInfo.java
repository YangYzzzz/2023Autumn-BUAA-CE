package FrontEnd.Info;

import FrontEnd.NonTerminal.Sign;

public class WordInfo {
    private String word;
    private Sign category;
    private int value;
    private int line;

    public WordInfo(String word, Sign category, int value, int line) {
        this.word = word;
        this.category = category;
        this.value = value; // 若-1代表字符串中出现非法字符
        this.line = line;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public int getLine() {
        return line;
    }

    public void setLine(int line) {
        this.line = line;
    }

    public Sign getCategory() {
        return category;
    }

    public void setCategory(Sign category) {
        this.category = category;
    }
}
