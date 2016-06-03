package ru.ekaerovets.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.ekaerovets.dao.Dao;
import ru.ekaerovets.model.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author karyakin dmitry
 *         date 30.10.15.
 */
@Component
public class ZiService {

    @Autowired
    private Dao dao;

    @Autowired
    private PinyinFetcher pinyinFetcher;

    public SyncData syncData(SyncData input) {
        storeToDb(input);
        return loadFromDb();
    }

    public void updatePinyins(int count) {
        List<String> pinyinsToUpdate = dao.getPinyinsToUpdate(count);
        List<Char> withPinyins = pinyinsToUpdate.stream().map((s) -> {
            String pinyin = pinyinFetcher.fetch(s);
            Char c = new Char();
            c.setWord(s);
            c.setPinyin(pinyin);
            return c;
        }).collect(Collectors.toList());
        dao.updatePinyinsForChars(withPinyins);

        List<String> wordsToUodate = dao.getWordsToUpdate(count);
        List<Word> wordsWithPinyins = wordsToUodate.stream().parallel().map((s) -> {
            String pinyin = pinyinFetcher.fetch(s);
            Word w = new Word();
            w.setWord(s);
            w.setPinyin(pinyin);
            return w;
        }).collect(Collectors.toList());
        dao.updatePinyinsForWords(wordsWithPinyins);
    }

    private void storeToDb(SyncData data) {
        storeChars(data.getChars(), true);
        dao.updateWords(data.getWords());
        dao.updatePinyins(data.getPinyins());
    }

    private SyncData loadFromDb() {
        List<Char> chars = dao.loadChars();
        List<Word> words = dao.loadWords();
        List<Pinyin> pinyins = dao.loadPinyins();
        return new SyncData(chars, words, pinyins);
    }

    public List<Char> loadChars() {
        return dao.loadChars();
    }

    public void storeChars(List<Char> chars, boolean update) {
        List<Char> current = loadChars();
        Set<String> existing = current.stream().map(Char::getWord).collect(Collectors.toSet());
        if (update) {
            List<Char> toUpdate = chars.stream().filter((c) -> existing.contains(c.getWord())).collect(Collectors.toList());
            dao.updateChars(toUpdate);
        }
        List<Char> toInsert = chars.stream().filter((c) -> !existing.contains(c.getWord())).collect(Collectors.toList());
        dao.insertChars(toInsert);
    }

    public List<PinyinDroid> syncPinyins(List<PinyinDroid> source) {
        dao.updatePinyinsDroid(source);
        return dao.loadPinyinsDroid();
    }

    public void addWords(List<Word> words) {
        dao.insertWords(words);
    }

    public void insertPinyins(List<Pinyin> pinyins) {
        dao.insertPinyins(pinyins);
    }

    public void updateChar(Char c) {
        if (dao.charExists(c)) {
            dao.updateChar(c);
        } else {
            dao.insertChar(c);
        }
    }

    public void updatePinyin(Pinyin p) {
        if (dao.pinyinExists(p)) {
            dao.updatePinyin(p);
        } else {
            dao.insertPinyin(p);
        }
    }

    // at 4 a.m every night
    @Scheduled(cron = "0 0 4 * * ?")
    public void doStat() {
        dao.doStat();
    }

}
