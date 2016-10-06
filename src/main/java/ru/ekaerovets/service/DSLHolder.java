package ru.ekaerovets.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author karyakin dmitry
 *         date 13.06.16.
 */
@Component
public class DSLHolder {

    @Value("${bkrs.path}")
    private String DSLFile;

    private Map<String, Entry> entries;

    public static void main(String[] args) throws IOException {
        Map<String, Entry> entries = new DSLHolder().readDSL("C:/proj/chzi/dabkrs/bkrs_v71.dsl");

        List<String> words = entries.keySet().stream().filter((e) -> e.length() == 2).collect(Collectors.toList());
        System.out.println(words.size());

        Set<String> rand = new HashSet<>();
        for (int i = 0; i < 100; i++) {
            rand.add(words.get((int) (Math.random() * words.size())));
        }

        String s = rand.toString();
        System.out.println(s);

    }

    public DSLHolder() {

    }

    @PostConstruct
    public void init() {
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
                //throw new RuntimeException("Expected empty line at line " + lineNumber);
                lineNumber++;
                continue;
            }
            String s = br.readLine();
            if (s == null) {
                break;
            }
            String key = s.trim();
            if (key.length() == 0) {
               // throw new RuntimeException("Key length must not be empty " + (lineNumber + 1));
                continue;
            }
            String pinyin = br.readLine().trim();
            if (pinyin.length() == 0) {
                //throw new RuntimeException("Pinyin must not be empty " + (lineNumber + 2));
                continue;
            }
            String meaning = br.readLine().trim();
            if (meaning.length() == 0) {
                //throw new RuntimeException("Meaning must not be empty " + (lineNumber + 3));
                continue;
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
