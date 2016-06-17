package ru.ekaerovets.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ekaerovets.dao.Dao;
import ru.ekaerovets.model.Pinyin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author karyakin dmitry
 *         date 08.11.15.
 */
@Component
public class PinyinParser {

    @Autowired
    private Dao dao;

    private static final String SPECIAL = "āáǎàēéěèīíǐìōóǒòūúǔùǖǘǚǜ";
    private static final String SPECIAL_UNTONED = "aeiouü";

    private static final String HEAD = "<html><head><meta charset=\"utf8\"><style>.tone1{color:#B9B94A}.tone2{color:red}" +
            ".tone3{color:green}.tone4{color:blue}table, th, td {border: 1px solid black;}</style></head><body>";
    private static final String TAIL = "</body></html>";

    private static final String[] INITIALS = {"", "b", "p", "m", "f", "d", "t", "n", "l", "g", "k", "h", "z", "c", "s",
            "zh", "ch", "sh", "r", "j", "q", "x", "y", "w" };
    private static final String[] FINALS = {"a", "o", "e", "i", "er", "ai", "ei", "ao", "ou", "an", "en", "ang", "eng",
            "ong", "ia", "iao", "ie", "iu", "ian", "in", "iang", "ing", "iong", "u", "ua", "uo", "uai", "ui", "ue", "uan", "un",
            "uang", "ü", "üe"};

    private Map<String, List<CharInfo>> parseData(List<Pinyin> input) {
        Map<String, List<CharInfo>> res = new HashMap<>();
        for (String initial : INITIALS) {
            for (String finale : FINALS) {
                res.put(initial + finale, new ArrayList<>());
            }
        }
        for (Pinyin pinyin : input) {
            List<String> untoned = parsePinyin(pinyin.getPinyin());
            for (String val : untoned) {
                CharInfo charInfo = getCharInfo(val, pinyin.getWord());
                if (res.containsKey(charInfo.word)) {
                    res.get(charInfo.word).add(charInfo);
                } else {
                    System.out.println("No tone found for string " + val + " as part of " + pinyin.getPinyin());
                }
            }
        }
        return res;
    }

    public String getHtml() {
        List<Pinyin> pinyins = dao.loadPinyins();
        Map<String, List<CharInfo>> parsedMap = parseData(pinyins);
        String[][] cells = new String[INITIALS.length + 1][];
        for (int i = 0; i < cells.length; i++) {
            cells[i] = new String[FINALS.length + 1];
        }
        for (int i = 0; i < INITIALS.length; i++) {
            cells[i + 1][0] = INITIALS[i];
        }
        System.arraycopy(FINALS, 0, cells[0], 1, FINALS.length);
        for (int i = 0; i < INITIALS.length; i++) {
            for (int j = 0; j < FINALS.length; j++) {

                String key = INITIALS[i] + FINALS[j];
                List<CharInfo> value = parsedMap.get(key);
                cells[i + 1][j + 1] = charInfoToHtml(value);
            }
        }

        StringBuilder sb = new StringBuilder(HEAD);
        sb.append("<table style=\"border-spacing:1px;\">");
        for (String[] cell : cells) {
            sb.append("<tr>");
            for (int j = 0; j < FINALS.length + 1; j++) {
                sb.append("<td>");
                sb.append(cell[j]);
                sb.append("</td>");
            }
            sb.append("</tr>");
        }
        sb.append("</table>");
        sb.append(TAIL);
        return sb.toString();
    }

    private String charInfoToHtml(List<CharInfo> input) {
        StringBuilder res = new StringBuilder();
        int currentTone = 0;
     //   int count = 0;
        for (CharInfo info : input) {
            int newTone = info.tone;
            if (newTone != currentTone && currentTone != 0) {
                res.append("</span>");
            }
          //  count++;
/*            if (count == 9) {
                res.append("<br>");
                count = 1;
            }*/
            if (newTone != currentTone && newTone != 0) {
                res.append("<span class=\"tone").append(newTone).append("\">");
            }
            currentTone = newTone;
            res.append(info.ch);
        }
        if (currentTone != 0) {
            res.append("</span>");
        }
        return res.toString();
    }

    private CharInfo getCharInfo(String input, String word) {
        int idx = -1;
        for (int i = 0; i < input.length(); i++) {
            int newIdx = SPECIAL.indexOf(input.charAt(i));
            if (newIdx != -1) {
                if (idx == -1) {
                    idx = newIdx;
                } else {
                    System.out.println("Multiple occurences in string " + input);
                }
            }
        }
        if (idx == -1) {
            return new CharInfo(input, 0, word);
        } else {
            int charId = idx / 4;
            int toneId = (idx % 4) + 1;
            String untoned = input.replace(SPECIAL.charAt(idx), SPECIAL_UNTONED.charAt(charId));
            return new CharInfo(untoned, toneId, word);
        }
    }

    private List<String> parsePinyin(String src) {
        List<String> res = new ArrayList<>();
        String cur = "";
        for (int i = 0; i < src.length(); i++) {
            Character c = src.charAt(i);
            if (isLetter(c)) {
                cur += c;
            } else {
                if (cur.length() > 0) {
                    res.add(cur);
                    cur = "";
                }
            }
        }
        if (cur.length() > 0) {
            res.add(cur);
        }
        return res;
    }

    private boolean isLetter(Character c) {
        return 'a' <= c && 'z' >= c || SPECIAL.indexOf(c) >= 0;
    }

    private static class CharInfo {
        private String word;
        private int tone;
        private String ch;

        public CharInfo(String word, int tone, String ch) {
            this.ch = ch;
            this.word = word;
            this.tone = tone;
        }
    }

}
