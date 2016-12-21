function TextProcessor(dataHolder) {

    let that = this;
    this.dataHolder = dataHolder;

    this.getInnerClass = function(cur, colorChars, colorPinyins) {
        let curClass = "";
        if (that.dataHolder.isUnihan(cur)) {
            if (colorChars) {
                let stage = that.dataHolder.getItemStage(cur, "c");
                if (!stage) {
                    stage = 5;
                }
                if (stage > 1) {
                    curClass = 'char' + stage;
                }
            }
            if (colorPinyins) {
                let stage = that.dataHolder.getItemStage(cur, "p");
                if (!stage) {
                    stage = 5;
                }
                if (stage > 1) {
                    curClass += ' pinyin' + stage;
                }
            }
        }
        return curClass;
    };

    this.processTextLine = function(line, options) {

        let colorChars = options.c;
        let colorPinyins = options.p;
        let colorWords = options.w;
        let text = "<p>";
        let fragLen = 0;
        let forceBreak = false;
        let innerClass = "";
        let outerClass = "";
        let prevInnerClass = "";
        let prevOuterClass = "";
        for (let i = 0; i < line.length; i++) {
            let cur = line[i];
            forceBreak = false;
            innerClass = that.getInnerClass(cur, colorChars, colorPinyins);
            if (fragLen > 1) {
                outerClass = prevOuterClass;
                if (outerClass == "fragment") {
                    innerClass = "";
                }
                fragLen--;
            } else if (fragLen <= 1) {
                if (fragLen == 1) {
                    fragLen--;
                }
                outerClass = "";
                let word = colorWords && that.dataHolder.getWordStage(line.substring(i, i + 5));
                if (word) {
                    fragLen = word.len;
                    outerClass = 'word' + word.stage;
                    forceBreak = true;
                }
            }
            if (prevOuterClass != outerClass || forceBreak) {
                if (prevInnerClass) {
                    text += "</span>";
                    prevInnerClass = "";
                }
                if (prevOuterClass) {
                    text += "</span>";
                    prevOuterClass = "";
                }
            }
            if (outerClass != prevOuterClass) {
                text += '<span class="' + outerClass + '">';
            }
            if (innerClass != prevInnerClass) {
                if (prevInnerClass) {
                    text += "</span>";
                }
                if (innerClass) {
                    text += '<span class="' + innerClass + '">';
                }
            }
            text += cur;
            prevOuterClass = outerClass;
            prevInnerClass = innerClass;
        }
        if (prevInnerClass) {
            text += "</span>";
        }
        if (prevOuterClass) {
            text += "</span>";
        }
        text += "</p>";
        return text;
    };

    this.text2html = function(text, options) {
        let res = "";
        let lines = text.split("\n");
        for (let i = 0; i < lines.length; i++) {
            res += that.processTextLine(lines[i].trim(), options);
        }
        return res;
    };

}
