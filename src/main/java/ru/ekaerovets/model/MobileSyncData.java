package ru.ekaerovets.model;

import java.util.List;

/**
 * @author karyakin dmitry
 *         date 03.06.16.
 */
public class MobileSyncData {

    private List<Char> chars;
    private List<Pinyin> pinyins;

    public MobileSyncData(List<Char> chars, List<Pinyin> pinyins) {
        this.chars = chars;
        this.pinyins = pinyins;
    }

    public List<Char> getChars() {
        return chars;
    }

    public List<Pinyin> getPinyins() {
        return pinyins;
    }
}
