package ru.ekaerovets.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ekaerovets.dao.Dao;
import ru.ekaerovets.model.Char;
import ru.ekaerovets.model.MobileSyncData;
import ru.ekaerovets.model.Pinyin;
import ru.ekaerovets.model.Word;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author karyakin dmitry
 *         date 30.10.15.
 */
@Component
public class ZiService {

    @Autowired
    private Dao dao;

    public MobileSyncData syncMobile(MobileSyncData input) {
        MobileSyncData current = loadData();
        return new MobileSyncData(mergeChars(input.getChars(), current.getChars()),
                mergeWords(input.getWords(), current.getWords()),
                mergePinyins(input.getPinyins(), current.getPinyins()));
    }

    private List<Char> mergeChars(List<Char> mobile, List<Char> db) {
        Map<String, Char> mobileMap = mobile.stream().collect(Collectors.toMap(Char::getWord, Function.identity()));
        db.forEach((c) -> {
            if (mobileMap.containsKey(c.getWord())) {
                if (!c.isOverride()) {
                    Char m = mobileMap.get(c.getWord());
                    c.setStage(m.getStage());
                    c.setDiff(m.getDiff());
                }
            }
        });
        dao.updateCharsOnSync(db);
        return db;
    }

    private List<Word> mergeWords(List<Word> mobile, List<Word> db) {
        if (mobile == null) {
            mobile = new ArrayList<>();
        }
        Map<String, Word> mobileMap = mobile.stream().collect(Collectors.toMap(Word::getWord, Function.identity()));
        db.forEach((c) -> {
            if (mobileMap.containsKey(c.getWord())) {
                if (!c.isOverride()) {
                    Word m = mobileMap.get(c.getWord());
                    c.setStage(m.getStage());
                    c.setDiff(m.getDiff());
                }
            }
        });
        dao.updateWordsOnSync(db);
        return db;
    }

    private List<Pinyin> mergePinyins(List<Pinyin> mobile, List<Pinyin> db) {
        Map<String, Pinyin> mobileMap = mobile.stream().collect(Collectors.toMap(Pinyin::getWord, Function.identity()));
        db.forEach((p) -> {
            if (mobileMap.containsKey(p.getWord())) {
                if (!p.isOverride()) {
                    Pinyin m = mobileMap.get(p.getWord());
                    p.setStage(m.getStage());
                    p.setDiff(m.getDiff());
                }
            }
        });
        dao.updatePinyinsOnSync(db);
        return db;
    }


    public MobileSyncData loadData() {
        return new MobileSyncData(dao.loadChars(), dao.loadWords(), dao.loadPinyins());
    }

    public void upsertChar(Char c) {
        if (dao.charExists(c)) {
            dao.updateChar(c);
        } else {
            dao.insertChar(c);
        }
    }

    public void upsertWord(Word w) {
        if (dao.wordExists(w)) {
            dao.updateWord(w);
        } else {
            dao.insertWord(w);
        }
    }

    public void upsertPinyin(Pinyin p) {
        if (dao.pinyinExists(p)) {
            dao.updatePinyin(p);
        } else {
            dao.insertPinyin(p);
        }
    }

    public void setStage(String word, int stage, String type) {
        if ("c".equals(type)) {
            dao.setCharStage(word, stage);
        } else if ("w".equals(type)) {
            dao.setWordStage(word, stage);
        } else if ("p".equals(type)) {
            dao.setPinyinStage(word, stage);
        }
    }

    public void wordsAnki(List<String> words) {
        dao.wordsAnki(words);
    }
}
