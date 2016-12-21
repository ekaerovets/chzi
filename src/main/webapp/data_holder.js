function DataHolder() {
    let that = this;

    this.server = new Server();

    this.freqHskData = {};
    this.hskWords = {};

    this.chars = {};
    this.pinyins = {};
    this.words = {};

    this.init = function() {

        let initialData = that.server.getInitialData();

        for (let i = 0; i < initialData.chars.length; i++) {
            let char = initialData.chars[i];
            that.chars[char.word] = char;
        }

        for (let i = 0; i < initialData.pinyins.length; i++) {
            let pinyin = initialData.pinyins[i];
            that.pinyins[pinyin.word] = pinyin;
        }

        for (let i = 0; i < initialData.words.length; i++) {
            let word = initialData.words[i];
            that.words[word.word] = word;
        }

        for (let i = 0; i < FREQ_DATA.length; i++) {
            let char = FREQ_DATA[i];
            that.freqHskData[char] = {i: i + 1};
        }

        let hsk = 1;
        for (let i = 0; i < HSK_CHARS.length; i++) {
            let char = HSK_CHARS[i];
            if (char == ":") {
                hsk++;
            } else {
                if (that.freqHskData[char]) {
                    that.freqHskData[char].h = hsk;
                } else {
                    that.freqHskData[char] = {h: hsk};
                }
            }
        }

        let wordsByLevel = HSK_WORDS.split(":");
        for (let i = 0; i < wordsByLevel.length; i++) {
            let ww = wordsByLevel[i];
            let www = ww.split(".");
            for (let j = 0; j < www.length; j++) {
                that.hskWords[www[j]] = i + 1;
            }
        }

    };


    /* helper functions */

    // checks if all the characters of a string are unihan characters
    this.isUnihan = function(text) {
        for (let i = 0; i < text.length; i++) {
            let code = text.charCodeAt(i);
            if ((code >= 0x4E00 && code <= 0x9FFF) || (code >= 0x3400 && code <= 0x4DBF) ||
                (code >= 0x20000 && code <= 0x2A6DF) || (code >= 0x2A700 && code <= 0x2B73F)) {
                continue;
            }
            return false;
        }
        return true;
    };

    this.getStorageByType = function(type) {
        if (type == 'c') {
            return that.chars;
        } else if (type == 'p') {
            return that.pinyins;
        } else if (type == 'w') {
            return that.words;
        } else {
            throw "Unknown type: " + type;
        }
    };

    /* getting information about an element */

    // returns meaning, pinyin and hsk/freq info for a character
    this.getChar = function(char) {
        let info = {};
        let freqHsk = that.freqHskData[char];
        if (freqHsk) {
            info.freq = freqHsk.i;
            info.hsk = freqHsk.h;
        }
        info.char = that.chars[char];
        info.pinyin = that.pinyins[char];

        let n_words = 0;
        for (let word in that.words) {
            if (that.words.hasOwnProperty(word) && word.indexOf(char) != -1) {
                n_words++;
            }
        }
        info.n_words = n_words;

        return info;
    };

    // returns word and hsk/freq info for it
    this.getWord = function(word) {
        let info = {};
        info.hsk = that.hskWords[word];
        info.word = that.words[word];
        return info;
    };


    /* data modification functions functions */

    this.setStage = function(key, type, stage) {
        let item = that.getStorageByType(type)[key];
        if (!item) {
            throw "Item " + key + " of type " + type + " not found";
        }
        if (stage == STAGE_TRIVIA) {
            item.stage = 1;
            item.diff = 8;
            item.due = today() + 8;
        } else if (stage == STAGE_LEARN) {
            // queue it actually
            item.stage = 2;
            item.diff = -1;
            item.due = -1;
        } else if (stage == STAGE_NEW) {
            item.stage = 3;
            item.diff = -1;
            item.due = -1;
        } else {
            throw "Invalid stage: " + stage;
        }
        that.server.update(item, type);
        return item;
    };

    // function inserts or updates new char/pinyin/word
    this.upsertItem = function(key, type, pinyin, meaning, example) {
        var storage = that.getStorageByType(type);
        let item = storage[key];
        let isNew = !item;
        if (!item) {
            item = {word: key, stage: STAGE_NEW, diff: -1, due: -1};
        }
        item.pinyin = pinyin;
        item.meaning = meaning;
        item.example = example;

        if (isNew) {
            that.server.insert(item, type);
            storage[key] = item;
        } else {
            that.server.update(item, type);
        }
    };

    /* getting information for text processor */

    this.getItemStage = function(key, type) {
        let c = that.getStorageByType(type)[key];
        if (!c) {
            return STAGE_UNKNOWN;
        } else {
            if (c.stage == 1) {
                return STAGE_TRIVIA;
            } else if (c.stage == 2) {
                return c.diff < 0 ? STAGE_QUEUE : STAGE_LEARN;
            } else if (c.stage == 3) {
                return STAGE_NEW;
            }
        }
    };

    this.getWordStage = function(fragment) {
        for (let i = 2; i < 7; i++) {
            var word = that.words[fragment.substr(0, i)];
            if (word) {
                return {len: i, stage: word.stage == STAGE_LEARN ? (word.diff < 0 ? STAGE_QUEUE : STAGE_LEARN) : word.stage}
            }
        }
    };

    /* extra word functions */

    this.lookup = function(word) {
        if (word && that.isUnihan(word)) {
            let lookup = that.server.lookup(word);
            if (lookup) {
                let res = JSON.parse(lookup);
                res.word = word;
                return res;
            }
        }
        return null;
    };

    this.getContainingWords = function(char) {
        let res = [];
        for (let word in that.words) {
            if (that.words.hasOwnProperty(word) && word.indexOf(char) != -1) {
                res.push(that.words[word]);
            }
        }
        return res;
    };

}
