package ru.ekaerovets.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.ekaerovets.model.Char;
import ru.ekaerovets.model.Pinyin;
import ru.ekaerovets.model.PinyinDroid;
import ru.ekaerovets.model.Word;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
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
        Char c = new Char(rs.getString("word"), rs.getString("meaning"), rs.getLong("due"), rs.getLong("interval"), rs.getInt("stage"));
        c.setPinyin(rs.getString("pinyin"));
        return c;
    };

    public List<Char> loadChars() {
        return jt.query("select * from chars", CHAR_ROW_MAPPER);
    }

    public List<Pinyin> loadPinyins() {
        return jt.query("select * from pinyins", (rs, i) -> {
            return new Pinyin(rs.getString("word"), rs.getString("pinyin"), rs.getDouble("diff"));
        });
    }

    public List<Word> loadWords() {
        return jt.query("select * from words", (rs, i) -> {
            return new Word(rs.getString("word"), rs.getString("meaning"), rs.getString("pinyin"),
                    rs.getLong("due"), rs.getLong("interval"));
        });
    }

    public List<PinyinDroid> loadPinyinsDroid() {
        String sql = "select c.word as char, c.meaning as meaning, p.pinyin as pinyin, p.diff as diff, " +
                "p.mark as mark, p.example as example from pinyins p left join chars c on c.word = p.word";
        return jt.query(sql, (rs, i) -> {
            PinyinDroid p = new PinyinDroid();
            p.setZi(rs.getString("char"));
            p.setMeaning(rs.getString("meaning"));
            String[] vals = rs.getString("pinyin").split("/");
            List<String> pronunciations = new ArrayList<>();
            for (String val: vals) {
                pronunciations.add(val.trim());
            }
            p.setPronunciation(pronunciations);
            p.setDiff(rs.getDouble("diff"));
            p.setMarked(rs.getBoolean("mark"));
            p.setExample(rs.getString("example"));
            return p;
        });
    }

    public List<String> getPinyinsToUpdate(int limit) {
        String sql = "select word from chars where pinyin is null limit " + limit;
        return jt.queryForList(sql, String.class);
    }

    public List<String> getWordsToUpdate(int limit) {
        String sql = "select word from words where pinyin is null limit " + limit;
        return jt.queryForList(sql, String.class);
    }

    public void updatePinyinsForChars(List<Char> chars) {
        String sql = "update chars set pinyin = ? where word = ?";
        jt.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setString(1, chars.get(i).getPinyin());
                ps.setString(2, chars.get(i).getWord());
            }

            @Override
            public int getBatchSize() {
                return chars.size();
            }
        });
    }

    public void updatePinyinsForWords(List<Word> words) {
        String sql = "update words set pinyin = ? where word = ?";
        jt.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setString(1, words.get(i).getPinyin());
                ps.setString(2, words.get(i).getWord());
            }

            @Override
            public int getBatchSize() {
                return words.size();
            }
        });
    }

    public void insertChars(List<Char> chars) {
        String sql = "insert into chars(word, meaning, due, interval, stage) values(?, ?, 0, 0, ?)";
        jt.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setString(1, chars.get(i).getWord());
                ps.setString(2, chars.get(i).getMeaning());
                ps.setInt(3, chars.get(i).getStage());
            }

            @Override
            public int getBatchSize() {
                return chars.size();
            }
        });
    }

    public void updateChars(List<Char> chars) {
        String sql = "update chars set meaning = ?, due = ?, interval = ?, stage = ? where word = ?";
        jt.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setString(1, chars.get(i).getMeaning());
                ps.setLong(2, chars.get(i).getDue());
                ps.setLong(3, chars.get(i).getInterval());
                ps.setInt(4, chars.get(i).getStage());
                ps.setString(5, chars.get(i).getWord());
            }

            @Override
            public int getBatchSize() {
                return chars.size();
            }
        });
    }

    public void updateWords(List<Word> words) {
        String sql = "update words set due = ?, interval = ? where word = ?";
        jt.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setLong(1, words.get(i).getDue());
                ps.setLong(2, words.get(i).getInterval());
                ps.setString(3, words.get(i).getWord());
            }

            @Override
            public int getBatchSize() {
                return words.size();
            }
        });
    }

    public void updatePinyins(List<Pinyin> pinyins) {
        String sql = "update pinyins set diff = ? where word = ?";
        jt.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setDouble(1, pinyins.get(i).getDiff());
                ps.setString(2, pinyins.get(i).getWord());
            }

            @Override
            public int getBatchSize() {
                return pinyins.size();
            }
        });
    }

    public void updatePinyinsDroid(List<PinyinDroid> data) {
        String sql = "update pinyins set diff = ?, mark = ? where word = ?";
        jt.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setDouble(1, data.get(i).getDiff());
                ps.setBoolean(2, data.get(i).isMarked());
                ps.setString(3, data.get(i).getZi());
            }

            @Override
            public int getBatchSize() {
                return data.size();
            }
        });
    }

    public boolean charExists(Char c) {
        String sql = "select count(*) from chars where word = ?";
        Integer count = jt.queryForObject(sql, new Object[] {c.getWord()}, Integer.class);
        return count > 0;
    }

    public void insertChar(Char c) {
        String sql = "insert into chars(word, meaning, due, interval, stage, mark, pinyin) values (?, ?, 0, 0, 3, false, '')";
        jt.update(sql, ps -> {
            ps.setString(1, c.getWord());
            ps.setString(2, c.getMeaning());
        });
    }

    public void updateChar(Char c) {
        String sql = "update chars set meaning = ? where word = ?";
        jt.update(sql, ps -> {
            ps.setString(1, c.getMeaning());
            ps.setString(2, c.getWord());
        });
    }

    public boolean pinyinExists(Pinyin p) {
        String sql = "select count(*) from pinyins where word = ?";
        Integer count = jt.queryForObject(sql, new Object[] {p.getWord()}, Integer.class);
        return count > 0;
    }

    public void insertPinyin(Pinyin p) {
        String sql = "insert into pinyins(word, pinyin, diff) values (?, ?, -1)";
        jt.update(sql, ps -> {
            ps.setString(1, p.getWord());
            ps.setString(2, p.getPinyin());
        });
    }

    public void updatePinyin(Pinyin p) {
        String sql = "update pinyins set pinyin = ? where word = ?";
        jt.update(sql, ps -> {
            ps.setString(1, p.getPinyin());
            ps.setString(2, p.getWord());
        });
    }



    public void insertWords(List<Word> words) {
/*        String sql = "insert into words(word, meaning, due, interval) values (?, ?, 0, 0)";
        jt.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setString(1, words.get(i).getWord());
                ps.setString(2, words.get(i).getMeaning());
            }

            @Override
            public int getBatchSize() {
                return words.size();
            }
        });*/
    }

    public void insertPinyins(List<Pinyin> pinyins) {
        throw new RuntimeException("Not supported");
/*        String sql = "insert into pinyins(word, pinyin, diff) values(?, ?, ?)";
        jt.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setString(1, pinyins.get(i).getWord());
                ps.setString(2, pinyins.get(i).getPinyin());
                ps.setDouble(3, pinyins.get(i).getDiff());
            }

            @Override
            public int getBatchSize() {
                return pinyins.size();
            }
        });*/
    }


    public void doStat() {
        String sql = "INSERT INTO stat (stat_date, chars_total, chars_learn, chars_new, chars_avg_interval, chars_mature_trivia,\n" +
                "                  chars_short, words_total, words_avg_interval, words_mature, pinyins_known, pinyins_total)\n" +
                "VALUES\n" +
                "  (current_timestamp, (SELECT count(*)\n" +
                "                       FROM chars), (SELECT count(*)\n" +
                "                                     FROM chars\n" +
                "                                     WHERE stage IN (1, 2)),\n" +
                "   (SELECT count(*)\n" +
                "    FROM chars\n" +
                "    WHERE stage = 3), (SELECT avg(interval) / 86400\n" +
                "                       FROM chars\n" +
                "                       WHERE stage = 2),\n" +
                "   (SELECT count(*)\n" +
                "    FROM chars\n" +
                "    WHERE stage = 1 OR (stage = 2 AND interval > 86400 * 7)), (SELECT count(*)\n" +
                "                                                               FROM chars\n" +
                "                                                               WHERE stage = 2 AND interval < 86400 * 3),\n" +
                "   (SELECT count(*)\n" +
                "    FROM words), (SELECT avg(interval) / 86400\n" +
                "                  FROM words), (SELECT count(*)\n" +
                "                                FROM words\n" +
                "                                WHERE interval > 86400 * 7),\n" +
                "   (SELECT count(*)\n" +
                "    FROM pinyins\n" +
                "    WHERE diff > 0), (SELECT count(*)\n" +
                "                      FROM pinyins))";
        jt.update(sql);
    }

}
