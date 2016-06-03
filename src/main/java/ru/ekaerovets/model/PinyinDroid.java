package ru.ekaerovets.model;

import java.util.List;

/**
 * @author karyakin dmitry
 *         date 22.04.16.
 */
public class PinyinDroid {

    private String zi;
    private List<String> pronunciation;
    private double diff;
    private String example;
    private boolean marked;
    private String meaning;

    public String getZi() {
        return zi;
    }

    public void setZi(String zi) {
        this.zi = zi;
    }

    public List<String> getPronunciation() {
        return pronunciation;
    }

    public void setPronunciation(List<String> pronunciation) {
        this.pronunciation = pronunciation;
    }

    public double getDiff() {
        return diff;
    }

    public void setDiff(double diff) {
        this.diff = diff;
    }

    public String getExample() {
        return example;
    }

    public void setExample(String example) {
        this.example = example;
    }

    public boolean isMarked() {
        return marked;
    }

    public void setMarked(boolean marked) {
        this.marked = marked;
    }

    public String getMeaning() {
        return meaning;
    }

    public void setMeaning(String meaning) {
        this.meaning = meaning;
    }

}
