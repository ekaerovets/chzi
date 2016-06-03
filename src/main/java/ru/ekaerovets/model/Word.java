package ru.ekaerovets.model;

/**
 * @author karyakin dmitry
 *         date 31.10.15.
 */
public class Word {

    String word;
    String meaning;
    String pinyin;
    Long due;
    Long interval;

    public Word() {
    }

    public Word(String word, String meaning, String pinyin, Long due, Long interval) {
        this.word = word;
        this.meaning = meaning;
        this.pinyin = pinyin;
        this.due = due;
        this.interval = interval;
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

    public String getPinyin() {
        return pinyin;
    }

    public void setPinyin(String pinyin) {
        this.pinyin = pinyin;
    }

    public Long getDue() {
        return due;
    }

    public void setDue(Long due) {
        this.due = due;
    }

    public Long getInterval() {
        return interval;
    }

    public void setInterval(Long interval) {
        this.interval = interval;
    }
}
