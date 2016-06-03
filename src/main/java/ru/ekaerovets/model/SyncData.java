package ru.ekaerovets.model;

import java.util.List;

/**
 * @author karyakin dmitry
 *         date 31.10.15.
 */
public class SyncData {

    private String secret;
    private List<Char> chars;
    private List<Word> words;
    private List<Pinyin> pinyins;

    public SyncData(List<Char> chars, List<Word> words, List<Pinyin> pinyins) {
        this.secret = "xuehanyu";
        this.chars = chars;
        this.words = words;
        this.pinyins = pinyins;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public List<Char> getChars() {
        return chars;
    }

    public void setChars(List<Char> chars) {
        this.chars = chars;
    }

    public List<Word> getWords() {
        return words;
    }

    public void setWords(List<Word> words) {
        this.words = words;
    }

    public List<Pinyin> getPinyins() {
        return pinyins;
    }

    public void setPinyins(List<Pinyin> pinyins) {
        this.pinyins = pinyins;
    }
}
