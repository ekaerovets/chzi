package ru.ekaerovets.model;

/**
 * @author karyakin dmitry
 *         date 30.10.15.
 */
public class Char {

    private String word;
    private String meaning;
    private long due;
    private long interval;
    private int stage;
    private Boolean marked;
    private String pinyin;

    public Char() {

    }

    public Char(String word, String meaning, long due, long interval, int stage) {
        this.word = word;
        this.meaning = meaning;
        this.due = due;
        this.interval = interval;
        this.stage = stage;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getMeaning() {
        return meaning;
    }

    public void setMeaning(String meaning) {
        this.meaning = meaning;
    }

    public long getDue() {
        return due;
    }

    public void setDue(long due) {
        this.due = due;
    }

    public long getInterval() {
        return interval;
    }

    public void setInterval(long interval) {
        this.interval = interval;
    }

    public int getStage() {
        return stage;
    }

    public void setStage(int stage) {
        this.stage = stage;
    }

    public Boolean isMarked() {
        return marked;
    }

    public void setMarked(Boolean marked) {
        this.marked = marked;
    }

    public String getPinyin() {
        return pinyin;
    }

    public void setPinyin(String pinyin) {
        this.pinyin = pinyin;
    }
}
