package ru.ekaerovets.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.ekaerovets.model.Char;
import ru.ekaerovets.model.Pinyin;

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
        return c;
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

    public void insertChar(Char c) {
        String sql = "insert into chars(word, meaning, stage, diff, override, mark, example) values (?, ?, 3, -1, false, false, null)";
        jt.update(sql, ps -> {
            ps.setString(1, c.getWord());
            ps.setString(2, c.getMeaning());
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

    public boolean pinyinExists(Pinyin p) {
        String sql = "select count(*) from pinyins where word = ?";
        Integer count = jt.queryForObject(sql, new Object[] {p.getWord()}, Integer.class);
        return count > 0;
    }

    public void insertPinyin(Pinyin p) {
        String sql = "insert into pinyins(word, pinyin, stage, diff, override, mark, example) values (?, ?, 3, -1, false, false, null)";
        jt.update(sql, ps -> {
            ps.setString(1, p.getWord());
            ps.setString(2, p.getPinyin());
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

    public void setPinyinStage(String word, int stage) {
        String sql = "update pinyins set stage = ?, override = true, diff = -1 where word = ?";
        jt.update(sql, ps -> {
            ps.setInt(1, stage);
            ps.setString(2, word);
        });
    }


    public void setCharStage(String word, int stage) {
        String sql = "update chars set stage = ?, override = true, diff = -1 where word = ?";
        jt.update(sql, ps -> {
            ps.setInt(1, stage);
            ps.setString(2, word);
        });
    }

}