package ru.ekaerovets.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.ekaerovets.model.Item;
import ru.ekaerovets.model.Stat;
import ru.ekaerovets.model.SyncData;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

/**
 * @author karyakin dmitry
 *         date 30.10.15.
 */
@Component
@Transactional
public class Dao {

    @Autowired
    private DataSource dataSource;

    private JdbcTemplate jt;

    @PostConstruct
    private void init() {
        jt = new JdbcTemplate(dataSource);
    }

    private RowMapper<Item> CHAR_ROW_MAPPER = (rs, i) ->
    {
        Item item = new Item();
        item.setWord(rs.getString("word"));
        item.setMeaning(rs.getString("meaning"));
        item.setStage(rs.getInt("stage"));
        item.setDiff(rs.getInt("diff"));
        item.setDue(rs.getInt("due"));
        item.setOverride(rs.getBoolean("override"));
        item.setMark(rs.getBoolean("mark"));
        item.setExample(rs.getString("example"));
        return item;
    };

    private RowMapper<Item> PINYIN_ROW_MAPPER = (rs, i) ->
    {
        Item item = new Item();
        item.setWord(rs.getString("word"));
        item.setPinyin(rs.getString("pinyin"));
        item.setStage(rs.getInt("stage"));
        item.setDiff(rs.getInt("diff"));
        item.setDue(rs.getInt("due"));
        item.setOverride(rs.getBoolean("override"));
        item.setMark(rs.getBoolean("mark"));
        item.setExample(rs.getString("example"));
        return item;
    };

    private RowMapper<Item> WORD_ROW_MAPPER = (rs, i) ->
    {
        Item item = new Item();
        item.setWord(rs.getString("word"));
        item.setMeaning(rs.getString("meaning"));
        item.setPinyin(rs.getString("pinyin"));
        item.setStage(rs.getInt("stage"));
        item.setDiff(rs.getInt("diff"));
        item.setDue(rs.getInt("due"));
        item.setOverride(rs.getBoolean("override"));
        item.setMark(rs.getBoolean("mark"));
        item.setExample(rs.getString("example"));
        return item;
    };

    public List<Item> loadChars() {
        return jt.query("select * from chars", CHAR_ROW_MAPPER);
    }

    public List<Item> loadPinyins() {
        return jt.query("select * from pinyins", PINYIN_ROW_MAPPER);
    }

    public List<Item> loadWords() {
        return jt.query("select * from words", WORD_ROW_MAPPER);
    }

    public void deleteData() {
        jt.update("delete from chars");
        jt.update("delete from pinyins");
        jt.update("delete from words");
    }

    public void updateOnSync(List<Item> items, boolean m, boolean p) {
        String sql = "update ";
        if (m && p) {
            sql += "words ";
        } else if (m) {
            sql += "chars ";
        } else {
            sql += "pinyins ";
        }
        sql += "set ";
        if (m) {
            sql += "meaning = ?, ";
        }
        if (p) {
            sql += "pinyin = ?, ";
        }
        sql += "stage = ?, diff = ?, due = ?, mark = ?, override = false where word = ?";
        jt.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                int index = 1;
                Item item = items.get(i);
                if (m) {
                    ps.setString(index++, item.getMeaning());
                }
                if (p) {
                    ps.setString(index++, item.getPinyin());
                }
                ps.setInt(index++, item.getStage());
                ps.setInt(index++, item.getDiff());
                ps.setInt(index++, item.getDue());
                ps.setBoolean(index++, item.isMark());
                ps.setString(index, item.getWord());
            }

            @Override
            public int getBatchSize() {
                return items.size();
            }
        });
    }

    private static class InsertBatchPSSetter implements BatchPreparedStatementSetter {

        private List<Item> items;
        private boolean hasMeaning;
        private boolean hasPinyin;

        public InsertBatchPSSetter(List<Item> items, boolean hasMeaning, boolean hasPinyin) {
            this.items = items;
            this.hasMeaning = hasMeaning;
            this.hasPinyin = hasPinyin;
        }

        @Override
        public void setValues(PreparedStatement ps, int i) throws SQLException {
            int index = 1;
            Item item = items.get(i);
            ps.setString(index++, item.getWord());
            if (hasMeaning) {
                ps.setString(index++, item.getMeaning());
            }
            if (hasPinyin) {
                ps.setString(index++, item.getPinyin());
            }
            ps.setInt(index++, item.getStage());
            ps.setInt(index++, item.getDiff());
            ps.setInt(index++, item.getDue());
            ps.setBoolean(index++, item.isMark());
            ps.setBoolean(index, item.isOverride());
        }

        @Override
        public int getBatchSize() {
            return items.size();
        }
    }

    public void restoreFromSnapshot(SyncData syncData) {
        deleteData();
        jt.batchUpdate("insert into chars(word, meaning, stage, diff, due, mark, override) VALUES (?, ?, ?, ?, ?, ?, ?)",
                new InsertBatchPSSetter(syncData.getChars(), true, false));
        jt.batchUpdate("insert into pinyins(word, pinyin, stage, diff, due, mark, override) values (?, ?, ?, ?, ?, ?, ?)",
                new InsertBatchPSSetter(syncData.getPinyins(), false, true));
        jt.batchUpdate("insert into words(word, meaning, pinyin, stage, diff, due, mark, override) values (?, ?, ?, ?, ?, ?, ?, ?)",
                new InsertBatchPSSetter(syncData.getWords(), true, true));
    }

    public void insertChar(Item item) {
        String sql = "insert into chars(word, meaning, stage, diff, due, override, mark, example) values (?, ?, 3, -1, -1, false, false, null)";
        jt.update(sql, ps -> {
            ps.setString(1, item.getWord());
            ps.setString(2, item.getMeaning());
        });
    }

    public void insertPinyin(Item item) {
        String sql = "insert into pinyins(word, pinyin, stage, diff, due, override, mark, example) values (?, ?, 3, -1, -1, false, false, null)";
        jt.update(sql, ps -> {
            ps.setString(1, item.getWord());
            ps.setString(2, item.getPinyin());
        });
    }

    public void insertWord(Item item) {
        String sql = "insert into words(word, meaning, pinyin, stage, diff, due, override, mark, example) values (?, ?, ?, 2, -1, -1, false, false, null)";
        jt.update(sql, ps -> {
            ps.setString(1, item.getWord());
            ps.setString(2, item.getMeaning());
            ps.setString(3, item.getPinyin());
        });
    }

    public void insertStat(Stat s) {
        String sql = "insert into stat(session_start, session_end, type, total_answers, review_wrong, review_correct, " +
                "learn_wrong, learn_correct) values (?, ?, ?, ?, ?, ?, ?, ?)";
        jt.update(sql, ps -> {
            ps.setDate(1, new Date(s.getSessionStart().getTime()));
            ps.setDate(2, new Date(s.getSessionEnd().getTime()));
            ps.setString(3, Character.toString(s.getType()));
            ps.setInt(4, s.getTotalAnswers());
            ps.setInt(5, s.getReviewWrong());
            ps.setInt(6, s.getReviewCorrect());
            ps.setInt(7, s.getLearnWrong());
            ps.setInt(8, s.getLearnCorrect());
        });
    }

    public void updateChar(Item item) {
        String sql = "update chars set meaning = ?, stage = ?, diff = ?, due = ?, mark = ?, example = ?, override = true where word = ?";
        jt.update(sql, ps -> {
            ps.setString(1, item.getMeaning());
            ps.setInt(2, item.getStage());
            ps.setInt(3, item.getDiff());
            ps.setInt(4, item.getDue());
            ps.setBoolean(5, item.isMark());
            ps.setString(6, item.getExample());
            ps.setString(7, item.getWord());
        });
    }

    public void updatePinyin(Item item) {
        String sql = "update pinyins set pinyin = ?, stage = ?, diff = ?, due = ?, mark = ?, example = ?, override = true where word = ?";
        jt.update(sql, ps -> {
            ps.setString(1, item.getPinyin());
            ps.setInt(2, item.getStage());
            ps.setInt(3, item.getDiff());
            ps.setInt(4, item.getDue());
            ps.setBoolean(5, item.isMark());
            ps.setString(6, item.getExample());
            ps.setString(7, item.getWord());
        });
    }

    public void updateWord(Item item) {
        String sql = "update words set meaning = ?, pinyin = ?, stage = ?, diff = ?, due = ?, mark = ?, example = ?, override = true where word = ?";
        jt.update(sql, ps -> {
            ps.setString(1, item.getMeaning());
            ps.setString(2, item.getPinyin());
            ps.setInt(3, item.getStage());
            ps.setInt(4, item.getDiff());
            ps.setInt(5, item.getDue());
            ps.setBoolean(6, item.isMark());
            ps.setString(7, item.getExample());
            ps.setString(8, item.getWord());
        });
    }

}
