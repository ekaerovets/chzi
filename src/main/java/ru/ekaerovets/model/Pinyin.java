package ru.ekaerovets.model;

import com.google.gson.annotations.SerializedName;

/**
 * @author karyakin dmitry
 *         date 31.10.15.
 */
public class Pinyin {

    private String word;
    private String pinyin;
    private Double diff;

    public Pinyin(String word, String pinyin, Double diff) {
        this.word = word;
        this.pinyin = pinyin;
        this.diff = diff;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getPinyin() {
        return pinyin;
    }

    public void setPinyin(String pinyin) {
        this.pinyin = pinyin;
    }

    public Double getDiff() {
        return diff;
    }

    public void setDiff(Double diff) {
        this.diff = diff;
    }
}
