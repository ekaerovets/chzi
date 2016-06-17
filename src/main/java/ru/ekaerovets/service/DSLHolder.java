package ru.ekaerovets.service;

import org.springframework.stereotype.Component;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * @author karyakin dmitry
 *         date 13.06.16.
 */
@Component
public class DSLHolder {

    private static final String DSLFile = "/home/laby/proj/chinese_zi/dabkrs_v70_1/大БКРС_v70.dsl";

    public static void main(String[] args) {
        new DSLHolder();
    }


    private Map<String, Entry> entries;

    public DSLHolder() {

        try {
            entries = readDSL(DSLFile);
            System.out.println(entries.size());
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    private Map<String, Entry> readDSL(String filename) throws IOException {
        Map<String, Entry> res = new HashMap<>();
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filename), "UTF-16"));
        br.readLine();
        br.readLine();
        br.readLine();

        int lineNumber = 4;

        String line;
        while ((line = br.readLine()) != null) {
            if (line.length() > 0) {
                throw new RuntimeException("Expected empty line at line " + lineNumber);
            }
            String key = br.readLine().trim();
            if (key.length() == 0) {
                throw new RuntimeException("Key length must not be empty " + (lineNumber + 1));
            }
            String pinyin = br.readLine().trim();
            if (pinyin.length() == 0) {
                throw new RuntimeException("Pinyin must not be empty " + (lineNumber + 2));
            }
            String meaning = br.readLine().trim();
            if (meaning.length() == 0) {
                throw new RuntimeException("Meaning must not be empty " + (lineNumber + 3));
            }
            lineNumber += 4;
            res.put(key, new Entry(pinyin, meaning));
        }
        return res;
    }

    public Entry lookup(String word) {
        return entries.get(word);
    }

    private static class Entry {
        private String pinyin;
        private String meaning;

        public Entry(String pinyin, String meaning) {
            this.pinyin = pinyin;
            this.meaning = meaning;
        }
    }

}
