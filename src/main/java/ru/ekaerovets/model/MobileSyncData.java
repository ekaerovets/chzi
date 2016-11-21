package ru.ekaerovets.model;

import java.util.List;

/**
 * @author karyakin dmitry
 *         date 03.06.16.
 */
public class MobileSyncData {

    private List<Char> chars;
    private List<Word> words;
    private List<Pinyin> pinyins;
    private List<Stat> stat;

    public MobileSyncData(List<Char> chars, List<Word> words, List<Pinyin> pinyins) {
        this.chars = chars;
        this.words = words;
        this.pinyins = pinyins;
    }

    public List<Char> getChars() {
        return chars;
    }

    public List<Word> getWords() {
        return words;
    }

    public List<Pinyin> getPinyins() {
        return pinyins;
    }

    public List<Stat> getStat() {
        return stat;
    }
}
