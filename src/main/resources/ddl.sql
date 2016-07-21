CREATE TABLE chars (
  word     TEXT,
  meaning  TEXT,
  stage    INT,
  diff     DOUBLE PRECISION,
  override BOOLEAN,
  mark     BOOLEAN,
  example  TEXT
);

CREATE TABLE words (
  word     TEXT,
  meaning  TEXT,
  stage    INT,
  diff     DOUBLE PRECISION,
  override BOOLEAN,
  mark     BOOLEAN,
  example  TEXT
);

CREATE TABLE pinyins (
  word     TEXT,
  pinyin   TEXT,
  stage    INT,
  diff     DOUBLE PRECISION,
  override BOOLEAN,
  mark     BOOLEAN,
  example  TEXT
);
