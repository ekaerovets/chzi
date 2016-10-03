package ru.ekaerovets.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.ekaerovets.model.Char;
import ru.ekaerovets.model.Pinyin;
import ru.ekaerovets.model.Word;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

/**
 * @author karyakin dmitry
 *         date 30.10.15.
 */
@Component
public class Dao {

    @Autowired
    private DataSource dataSource;

    private JdbcTemplate jt;

    @PostConstruct
    private void init() {
        jt = new JdbcTemplate(dataSource);
    }

    private RowMapper<Char> CHAR_ROW_MAPPER = (rs, i) ->
    {
        Char c = new Char();
        c.setWord(rs.getString("word"));
        c.setMeaning(rs.getString("meaning"));
        c.setStage(rs.getInt("stage"));
        c.setDiff(rs.getDouble("diff"));
        c.setOverride(rs.getBoolean("override"));
        c.setMark(rs.getBoolean("mark"));
        c.setExample(rs.getString("example"));
        c.setRadix(rs.getString("radix"));
        return c;
    };

    private RowMapper<Word> WORD_ROW_MAPPER = (rs, i) ->
    {
        Word w = new Word();
        w.setWord(rs.getString("word"));
        w.setMeaning(rs.getString("meaning"));
        w.setPinyin(rs.getString("pinyin"));
        w.setStage(rs.getInt("stage"));
        w.setDiff(rs.getDouble("diff"));
        w.setOverride(rs.getBoolean("override"));
        w.setMark(rs.getBoolean("mark"));
        w.setExample(rs.getString("example"));
        return w;
    };

    private RowMapper<Pinyin> PINYIN_ROW_MAPPER = (rs, i) ->
    {
        Pinyin p = new Pinyin();
        p.setWord(rs.getString("word"));
        p.setPinyin(rs.getString("pinyin"));
        p.setStage(rs.getInt("stage"));
        p.setDiff(rs.getDouble("diff"));
        p.setOverride(rs.getBoolean("override"));
        p.setMark(rs.getBoolean("mark"));
        p.setExample(rs.getString("example"));
        return p;
    };

    public List<Char> loadChars() {
        return jt.query("select * from chars", CHAR_ROW_MAPPER);
    }

    public List<Word> loadWords() {
        return jt.query("select * from words", WORD_ROW_MAPPER);
    }

    public List<Pinyin> loadPinyins() {
        return jt.query("select * from pinyins", PINYIN_ROW_MAPPER);
    }

    public void updateCharsOnSync(List<Char> chars) {
        String sql = "update chars set stage = ?, diff = ?, override = false where word = ?";
        jt.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setInt(1, chars.get(i).getStage());
                ps.setDouble(2, chars.get(i).getDiff());
                ps.setString(3, chars.get(i).getWord());
            }

            @Override
            public int getBatchSize() {
                return chars.size();
            }
        });
    }

    public void updateWordsOnSync(List<Word> words) {
        String sql = "update words set stage = ?, diff = ?, override = false where word = ?";
        jt.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setInt(1, words.get(i).getStage());
                ps.setDouble(2, words.get(i).getDiff());
                ps.setString(3, words.get(i).getWord());
            }

            @Override
            public int getBatchSize() {
                return words.size();
            }
        });
    }

    public void updatePinyinsOnSync(List<Pinyin> pinyins) {
        String sql = "update pinyins set stage = ?, diff = ?, override = false where word = ?";
        jt.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setInt(1, pinyins.get(i).getStage());
                ps.setDouble(2, pinyins.get(i).getDiff());
                ps.setString(3, pinyins.get(i).getWord());
            }

            @Override
            public int getBatchSize() {
                return pinyins.size();
            }
        });
    }

    public boolean charExists(Char c) {
        String sql = "select count(*) from chars where word = ?";
        Integer count = jt.queryForObject(sql, new Object[] {c.getWord()}, Integer.class);
        return count > 0;
    }

    public boolean wordExists(Word w) {
        String sql = "select count(*) from words where word = ?";
        Integer count = jt.queryForObject(sql, new Object[] {w.getWord()}, Integer.class);
        return count > 0;
    }

    public boolean pinyinExists(Pinyin p) {
        String sql = "select count(*) from pinyins where word = ?";
        Integer count = jt.queryForObject(sql, new Object[] {p.getWord()}, Integer.class);
        return count > 0;
    }

    public void insertChar(Char c) {
        String sql = "insert into chars(word, meaning, stage, diff, override, mark, example) values (?, ?, 3, -1, false, false, null)";
        jt.update(sql, ps -> {
            ps.setString(1, c.getWord());
            ps.setString(2, c.getMeaning());
        });
    }

    public void insertWord(Word w) {
        String sql = "insert into words(word, meaning, pinyin, stage, diff, override, mark, example) values (?, ?, ?, 2, -1, false, false, null)";
        jt.update(sql, ps -> {
            ps.setString(1, w.getWord());
            ps.setString(2, w.getMeaning());
            ps.setString(3, w.getPinyin());
        });
    }

    public void insertPinyin(Pinyin p) {
        String sql = "insert into pinyins(word, pinyin, stage, diff, override, mark, example) values (?, ?, 3, -1, false, false, null)";
        jt.update(sql, ps -> {
            ps.setString(1, p.getWord());
            ps.setString(2, p.getPinyin());
        });
    }

    public void updateChar(Char c) {
        String sql = "update chars set meaning = ?, mark = ?, example = ? where word = ?";
        jt.update(sql, ps -> {
            ps.setString(1, c.getMeaning());
            ps.setBoolean(2, c.isMark());
            ps.setString(3, c.getExample());
            ps.setString(4, c.getWord());
        });
    }

    public void updateWord(Word w) {
        String sql = "update words set meaning = ?, pinyin = ?, mark = ?, example = ? where word = ?";
        jt.update(sql, ps -> {
            ps.setString(1, w.getMeaning());
            ps.setString(2, w.getPinyin());
            ps.setBoolean(3, w.isMark());
            ps.setString(4, w.getExample());
            ps.setString(5, w.getWord());
        });
    }

    public void updatePinyin(Pinyin p) {
        String sql = "update pinyins set pinyin = ?, mark = ?, example = ? where word = ?";
        jt.update(sql, ps -> {
            ps.setString(1, p.getPinyin());
            ps.setBoolean(2, p.isMark());
            ps.setString(3, p.getExample());
            ps.setString(4, p.getWord());
        });
    }

    public void setCharStage(String word, int stage) {
        String sql = "update chars set stage = ?, override = true, diff = -1 where word = ?";
        jt.update(sql, ps -> {
            ps.setInt(1, stage);
            ps.setString(2, word);
        });
    }

    public void setWordStage(String word, int stage) {
        String sql = "update words set stage = ?, override = true, diff = -1 where word = ?";
        jt.update(sql, ps -> {
            ps.setInt(1, stage);
            ps.setString(2, word);
        });
    }


    public void setPinyinStage(String word, int stage) {
        String sql = "update pinyins set stage = ?, override = true, diff = -1 where word = ?";
        jt.update(sql, ps -> {
            ps.setInt(1, stage);
            ps.setString(2, word);
        });
    }

    public void wordsAnki(List<String> words) {
        String sql = "update words set stage = 1, override = true, diff = -1 where word in (";
        for (int i = 0; i < words.size(); i++) {
            if (i > 0) {
                sql += ",";
            }
            sql += "?";
        }
        sql += ")";
        jt.update(sql, ps -> {
            for (int i = 0; i < words.size(); i++) {
                ps.setString(i + 1, words.get(i));
            }
        });
    }
}
