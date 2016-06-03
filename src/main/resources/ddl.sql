CREATE TABLE words (
  word     TEXT,
  meaning  TEXT,
  pinyin   TEXT,
  due      BIGINT,
  interval BIGINT
);

CREATE TABLE chars (
  word     TEXT,
  meaning  TEXT,
  due      BIGINT,
  interval BIGINT,
  stage    INT
);

CREATE TABLE pinyins (
  word   TEXT,
  pinyin TEXT,
  diff   DOUBLE PRECISION
);

CREATE TABLE stat (
  stat_date           TIMESTAMP,
  chars_total         INTEGER,
  chars_learn         INTEGER,
  chars_new           INTEGER,
  chars_avg_interval  DOUBLE PRECISION,
  chars_mature_trivia INTEGER,
  chars_short         INTEGER,
  words_total         INTEGER,
  words_avg_interval  DOUBLE PRECISION,
  words_mature        INTEGER,
  pinyins_known       INTEGER,
  pinyins_total       INTEGER
);