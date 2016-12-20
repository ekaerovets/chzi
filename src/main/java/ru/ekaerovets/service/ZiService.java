package ru.ekaerovets.service;

import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.ekaerovets.dao.Dao;
import ru.ekaerovets.model.Item;
import ru.ekaerovets.model.ItemWrapper;
import ru.ekaerovets.model.Stat;
import ru.ekaerovets.model.SyncData;
import sun.misc.IOUtils;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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

    @Value("${backup.dir}")
    private String backupDir;

    @Transactional
    public SyncData syncMobile(SyncData input) {
        SyncData current = loadData();
        addStat(input.getStat());
        SyncData res = new SyncData();
        res.setChars(merge(input.getChars(), current.getChars(), true, false));
        res.setPinyins(merge(input.getPinyins(), current.getPinyins(), false, true));
        res.setWords(merge(input.getWords(), current.getWords(), true, true));
        return res;
    }

    private List<Item> merge(List<Item> mobile, List<Item> db, boolean m, boolean p) {
        Map<String, Item> mobileMap = mobile.stream().collect(Collectors.toMap(Item::getWord, Function.<Item>identity()));
        List<Item> toUpdate = new ArrayList<>();
        db.forEach((item) -> {
            if (mobileMap.containsKey(item.getWord())) {
                if (item.isOverride()) {
                    // flag override should be cleared
                    toUpdate.add(item);
                } else {
                    // Update existing items only if there were changes in this item
                    // (thus reducing DB traffic significantly)
                    Item mobileItem = mobileMap.get(item.getWord());
                    if (mobileItem.getStage() != item.getStage() || mobileItem.getDiff() != item.getDiff()
                            || mobileItem.getDue() != item.getDue() || mobileItem.isMark() != item.isMark()) {
                        item.setStage(mobileItem.getStage());
                        item.setDiff(mobileItem.getDiff());
                        item.setDue(mobileItem.getDue());
                        item.setMark(mobileItem.isMark());
                        toUpdate.add(mobileItem);
                    }
                }
            } else {
                // a new item, the override flag should be cleared
                toUpdate.add(item);
            }
        });
        dao.updateOnSync(toUpdate, m, p);
        return db;
    }


    public void addStat(List<Stat> stat) {
        if (stat != null) {
            stat.forEach(dao::insertStat);
        }
    }

    @Transactional
    public SyncData loadData() {
        SyncData data = new SyncData();
        data.setChars(dao.loadChars());
        data.setPinyins(dao.loadPinyins());
        data.setWords(dao.loadWords());
        return data;
    }

    @Transactional
    public void insertItem(ItemWrapper wrapper) {
        if (wrapper.getType() == 'c') {
            dao.insertChar(wrapper.getItem());
        } else if (wrapper.getType() == 'p') {
            dao.insertPinyin(wrapper.getItem());
        } else if (wrapper.getType() == 'w') {
            dao.insertWord(wrapper.getItem());
        } else {
            throw new RuntimeException("Unknown type: " + wrapper.getType());
        }
    }

    @Transactional
    public void updateItem(ItemWrapper wrapper) {
        if (wrapper.getType() == 'c') {
            dao.updateChar(wrapper.getItem());
        } else if (wrapper.getType() == 'p') {
            dao.updatePinyin(wrapper.getItem());
        } else if (wrapper.getType() == 'w') {
            dao.updateWord(wrapper.getItem());
        } else {
            throw new RuntimeException("Unknown type: " + wrapper.getType());
        }
    }

    public void backup(String json, boolean isMobile) {
        if (backupDir != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss.sss");
            String format = sdf.format(new Date());
            String fName = (isMobile ? "m_" : "") + format + ".json";
            Path path = Paths.get(backupDir, fName);
            try (OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(path.toFile()), "UTF8")) {
                out.write(json);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void snapshot(String dest, Gson gson) {
        if (backupDir != null) {
            String fName = dest + ".json";
            Path path = Paths.get(backupDir, fName);
            try (OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(path.toFile()), "UTF8")) {
                out.write(gson.toJson(loadData()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void restore(String src, Gson gson) {
        if (backupDir != null) {
            String fName = src + ".json";
            Path path = Paths.get(backupDir, fName);
            try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(path.toFile()), "UTF8"))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
                SyncData syncData = gson.fromJson(sb.toString(), SyncData.class);
                dao.restoreFromSnapshot(syncData);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

}
