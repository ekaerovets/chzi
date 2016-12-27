function UI(dataHolder, textProcessor) {
    let that = this;
    this.dataHolder = dataHolder;
    this.textProcessor = textProcessor;

    this.currentText = "多年以后，奥雷连诺上校站在行刑队面前，准会想起父亲带他去参观冰块的那个遥远的下午。";

    this.selection = "";

    this.options = {c: true, p: true, w: true, d: true};

    /* ==============================
    ======== UTIL FUNCTIONS =========
    ============================== */

    // function for accessing html elements by ID with easy manipulation methods.
    // e.g. to hide #cpText just call _.cpText.visible = false;
    // to set text value to #wpMeaning call _.wpMeaning.text = "new value";
    let _ = new Proxy({}, {
        get: function(target, id) {
            if (target[id]) {
                return target[id];
            } else {
                var el = document.getElementById(id);
                if (el) {
                    let proxy = new Proxy(el, {
                        set: function(target, name, value) {
                            if (name == "visible") {
                                el.classList.toggle("hidden", !value);
                            } else if (name == "value") {
                                let tagName = el.tagName;
                                if (tagName == "DIV") {
                                    el.innerHTML = value;
                                } else if (tagName == "SPAN") {
                                    el.textContent = value;
                                } else if (tagName == "INPUT") {
                                    el.value = value;
                                } else {
                                    throw "Unknown tag name : " + tagName;
                                }
                            } else {
                                el[name] = value;
                            }
                        },
                        get: function(target, name) {
                            if (name == "this") {
                                return el;
                            }
                            return el[name];
                        }
                    });
                    target[id] = proxy;
                    return proxy;
                } else {
                    throw "Cannot find element by id " + id;
                }
            }
        }
    });

    this.invalidate = function() {
        _.text.value = that.textProcessor.text2html(that.currentText, that.options);
    };

    /* ==========================
    ====== EVENT HANDLERS =======
    ========================== */

    _.text.onmouseup = function() {
        let selection = window.getSelection();
        let txt = selection.toString();
        that.onTextSelected(txt);
        if (txt && that.options.d) {
            let res = dataHolder.lookup(txt);
            let clipRect = selection.getRangeAt(0).getBoundingClientRect();
            if (res) {
                that.showLookup(clipRect, res);
            }
        }
    };

    _.cpCharTrivia.onclick = function() {that.updateStage("c", STAGE_TRIVIA)};
    _.cpCharQueue.onclick = function() {that.updateStage("c", STAGE_LEARN)};
    _.cpCharNew.onclick = function() {that.updateStage("c", STAGE_NEW)};
    _.cpPinTrivia.onclick = function() {that.updateStage("p", STAGE_TRIVIA)};
    _.cpPinQueue.onclick = function() {that.updateStage("p", STAGE_LEARN)};
    _.cpPinNew.onclick = function() {that.updateStage("p", STAGE_NEW)};
/*    _.wpTrivia.onclick = function() {that.updateStage("w", STAGE_TRIVIA)};
    _.wpQueue.onclick = function() {that.updateStage("w", STAGE_LEARN)};
    _.wpNew.onclick = function() {that.updateStage("w", STAGE_NEW)};*/

    _.tpNew.onclick = function() {
        _.ovOverlay.visible = true;
        _.ovNewText.visible = true;
        _.newText.this.focus();
    }

    _.btnNewText.onclick = function() {
        that.currentText = _.newText.value;
        that.invalidate();
        _.ovOverlay.visible = false;
        _.ovNewText.visible = false;
    };


    _.ovDict.onblur = function() {
       _.ovDict.visible = false;
    };

    /* ==========================
    = DATA PROCESSING FUNCTIONS =
    ========================== */

    this.updateStage = function(type, newStage) {
        let key = that.selection;
        let item = that.dataHolder.setStage(key, type, newStage);
        that.invalidate();
        that.updateDiffSpanAndButtons(item, type);
    };

    this.updateDiffSpanAndButtons = function(item, type) {
        let val = "";
        let color = null;
        let margin = 2;
        let canTrivia = false;
        let canQueue = false;
        let canNew = false;
        if (item != null) {
            let stage = item.stage;
            if (stage == 1) {
                canQueue = true;
                canNew = true;
                color = "green";
                if (item.due < 0) {
                    val = "OK";
                } else {
                    let d = item.due - today();
                    if (d > 0) {
                        val = d;
                    } else {
                        val = "Due";
                    }
                }
            } else if (stage == 2) {
                canTrivia = true;
                canNew = true;
                canQueue = item.diff > -1;
                margin = 5;
                if (item.diff == -1) {
                    color = "magenta";
                    val = "Q";
                    margin = 2;
                } else if (item.diff == 25) {
                    color = "red";
                    val = "*";
                } else if (item.diff == 125) {
                    color = "orange";
                    val = "**";
                } else if (item.diff == 625) {
                    color = "green";
                    val = "***";
                }
            } else if (stage == 3) {
                canTrivia = true;
                canQueue = true;
                color = "blue";
                val = "N";
            }
        }
        let prefix;
        if (type == "c") {
            prefix = "cpChar";
        } else if (type == "p") {
            prefix = "cpPin";
        } else if (type == "w") {
            prefix = "wp";
        }
        let span = _[prefix + "StatSpan"];
        span.style.color = color;
        span.style["margin-top"] = margin + "px";
        span.value = val;
        _[prefix + "Trivia"].disabled = !canTrivia;
        _[prefix + "Queue"].disabled = !canQueue;
        _[prefix + "New"].disabled = !canNew;
    };

    this.setupChar = function(char) {
        let charInfo = that.dataHolder.getChar(char);
        _.cpChar.value = char;
        _.cpFreq.value = charInfo.freq ? charInfo.freq : "--";
        _.cpHsk.value = charInfo.hsk ? "HSK" + charInfo.hsk : "--";

        let c = charInfo.char;
        that.updateDiffSpanAndButtons(c, "c");
        _.cpCharSave.visible = !c;
        _.cpCharView.visible = !!c;
        if (c) {
            _.cpCharViewSpan.value = c.meaning;
            _.cpCharViewSpan.title = c.meaning;
        } else {
            _.cpCharEditInput.value = "";
        }

        let p = charInfo.pinyin;
        that.updateDiffSpanAndButtons(p, "p");
        _.cpPinSave.visible = !p;
        _.cpPinView.visible = !!p;
        if (p) {
            _.cpPinViewSpan.value = p.pinyin;
            _.cpPinViewSpan.title = p.pinyin;
        } else {
            _.cpPinEditInput.value = "";
        }

        let cnt = charInfo.n_words;
        _.cpWords.value = cnt == 0 ? "Words" : "Words (" + cnt + ")";
        _.cpWords.disabled = cnt == 0;
    };

    this.setupWord = function(word) {
        let wordInfo = that.dataHolder.getWord(word);
        _.wpWord.value = word;
        _.wpHsk.value = wordInfo.hsk ? "HSK" + wordInfo.hsk : "--";
        let w = wordInfo.word;
        if (w) {
            if (w.diff > 0) {
                _.wpStage.value = that.formatDiff(w.diff);
            } else {
                _.wpStage.value = w.stage == STAGE_LEARN ? "Q" : "T";
            }
        } else {
            _.wpStage.value = "--";
        }

        _.wpMeanWrapper.visible = !w;
        _.wpPinWrapper.visible = !w;
        _.wpMeanSpan.visible, w;
        _.wpPinSpan.visible = w;
        that.wordEdit = !w;
        that.wpButton.disabled = !w;
        that.wpButton.value = w ? "Edit" : "Save";
        if (w) {
            that.wpMeanSpan.textContent = w.meaning;
            that.wpPinSpan.textContent = w.pinyin;
        } else {
            that.wpMeanInput.value = "";
            that.wpPinInput.value = "";
        }
    };


    this.onTextSelected = function(text) {
        that.selection = text;
        if (!that.dataHolder.isUnihan(text)) {
            text = "";
        }
        _.cpPanel.visible = text.length == 1;
        _.wpPanel.visible = text.length > 1;
        if (text.length == 1) {
            that.setupChar(text);
        } else if (text.length > 1 && text.length < 7) {
         //   that.setupWord(text);
        }
    };

    this.dictReplace = function(str) {
        str = str.replace(/\[m1]/g, "<div class='m1'>");
        str = str.replace(/\[m2]/g, "<div class='m2'>");
        str = str.replace(/\[m4]/g, "<div class='m4'>");
        str = str.replace(/\[\/m]/g, "</div>");
        str = str.replace(/\[i]/g, "<i>");
        str = str.replace(/\[\/i]/g, "</i>");
        str = str.replace(/\[c]/g, "<span class='c'>");
        str = str.replace(/\[\/c]/g, "</span>");
        str = str.replace(/\[b]/g, "<b>");
        str = str.replace(/\[\/b]/g, "</b>");
        str = str.replace(/\[\*]/g, "");
        str = str.replace(/\[\/\*]/g, "");
        str = str.replace(/\[ex]/g, "<span class='ex'>");
        str = str.replace(/\[\/ex]/g, "</span>");
        str = str.replace(/\\]/g, "]");
        str = str.replace(/\\\[/g, "[");
        return str;
    };

    this.dictFormat = function(obj) {
        var res = "";
        res += "<div class='dict-pinyin'>";
        let word = obj.word;
        if (word.length > 1 && word.length <= 6 && !that.dataHolder.getWord(obj.word).word) {
            res += '<span onclick=\'document.getElementById("wpPinInput").value = "' +
                obj.pinyin + '"\' class="copy-pinyin">&nearr;</span>';
        }
        res += "<span>" + that.dictReplace(obj.pinyin) + "</span></div>";
        res += '<hr style="margin: 4px 0;border: 1px solid #c5c5c5;">';
        res += "<div class='dict-meaning'>" + that.dictReplace(obj.meaning) + "</div>";
        return res;
    };

    this.showLookup = function(clipRect, res) {
        let w = window.innerWidth;
        let h = window.innerHeight;
        _.ovDict.style.width = "500px";
        _.ovDict.style.height = "300px";
        _.ovDict.style.top = clipRect.bottom > 400 ? (clipRect.top - 315) + "px" : clipRect.bottom + "px";
        let l = clipRect.left < 110 ? 10 : clipRect.left - 100;
        if (l + 523 > w) {
            l = w - 523;
        }
        _.ovDict.style.left = l + "px";
        _.ovDict.value = that.dictFormat(res);
        _.ovDict.visible = true;
        _.ovDict.this.focus();
    };


    this.textMouseUp = function() {
        let selection = window.getSelection();
        let txt = selection.toString();
        that.onTextSelected(txt);
        if (txt && that.options.d) {
            let res = dataHolder.lookup(txt);
            let clipRect = selection.getRangeAt(0).getBoundingClientRect();
            if (res) {
                that.showLookup(clipRect, res);
            }
        }
    };

    this.meaningKeyUp = function() {
        that.cpMeanBtn.disabled = !that.cpMeanInput.value.length;
    };

    this.pinKeyUp = function() {
        that.cpPinBtn.disabled = !that.cpPinInput.value.length;
    };

    that.checkWpButton = function() {
        that.wpButton.disabled = !that.wpMeanInput.value.length || !that.wpPinInput.value.length;
    };

    this.saveMeaning = function() {
        let value = that.cpMeanInput.value;
        that.cpMeanSpan.textContent = value;
        that.showhide(that.cpMeanWrapper, false);
        that.showhide(that.cpMeanSpan, true);
        that.cpMeanBtn.value = "Edit";
        that.charEdit = false;
        if (that.dataHolder.updateMeaning(that.selection, value)) {
            that.invalidate();
            that.cpMeanStage.textContent = "N";
            that.cpMeanStage.className = that.getStageClass(STAGE_NEW);
        }
    };

    this.editMeaning = function() {
        that.cpMeanInput.value = that.cpMeanSpan.textContent;
        that.showhide(that.cpMeanWrapper, true);
        that.showhide(that.cpMeanSpan, false);
        that.cpMeanBtn.value = "Save";
        that.charEdit = true;
    };

    this.savePinyin = function() {
        let value = that.cpPinInput.value;
        that.cpPinSpan.textContent = value;
        that.showhide(that.cpPinWrapper, false);
        that.showhide(that.cpPinSpan, true);
        that.cpPinBtn.value = "Edit";
        that.pinyinEdit = false;
        if (that.dataHolder.updatePinyin(that.selection, value)) {
            that.invalidate();
            that.cpPinStage.textContent = "N";
            that.cpPinStage.className = that.getStageClass(STAGE_NEW);
        }
    };

    this.editPinyin = function() {
        that.cpPinInput.value = that.cpPinSpan.textContent;
        that.showhide(that.cpPinWrapper, true);
        that.showhide(that.cpPinSpan, false);
        that.cpPinBtn.value = "Save";
        that.pinyinEdit = true;
    };

    this.saveWord = function() {
        let meaning = that.wpMeanInput.value;
        that.wpMeanSpan.textContent = meaning;
        let pinyin = that.wpPinInput.value;
        that.wpPinSpan.textContent = pinyin;
        that.showhide(that.wpMeanWrapper, false);
        that.showhide(that.wpMeanSpan, true);
        that.showhide(that.wpPinWrapper, false);
        that.showhide(that.wpPinSpan, true);
        that.wpButton.value = "Edit";
        that.wordEdit = false;
        if (that.dataHolder.updateWord(that.selection, meaning, pinyin)) {
            that.invalidate();
        }
    };

    this.editWord = function() {
        that.wpMeanInput.value = that.wpMeanSpan.textContent;
        that.wpPinInput.value = that.wpPinSpan.textContent;
        that.showhide(that.wpMeanWrapper, true);
        that.showhide(that.wpMeanSpan, false);
        that.showhide(that.wpPinWrapper, true);
        that.showhide(that.wpPinSpan, false);
        that.wpButton.value = "Save";
        that.wordEdit = true;
    };

    this.meanBtnClick = function() {
        if (that.charEdit) {
            that.saveMeaning();
        } else {
            that.editMeaning();
        }
    };

    this.pinBtnClick = function() {
        if (that.pinyinEdit) {
            that.savePinyin();
        } else {
            that.editPinyin();
        }
    };

    this.wordBtnClick = function() {
        if (that.wordEdit) {
            that.saveWord();
        } else {
            that.editWord();
        }
    };

    this.tpOrderedClick = function() {
        that.currentText = FREQ_DATA;
        that.invalidate();
    };

    this.tpPluginKeyPress = function(event) {
        if (event.ctrlKey && event.keyCode == 10) {
            var command = that.tpPlugin.value;
            that.tpPlugin.value = "";
            runCommand(command);
        }
    };

    this.tpBackupClick = function() {
        that.dataHolder.backup();
        alert("success");
    };

    this.tpAnkiClick = function() {
        let res = that.dataHolder.getAnki();
        that.text.innerHTML = '<div style="font-size:12px;text-indent: 0"><pre>' + res + "</pre></div>";
    };

    this.tpWordsClick = function() {
        let words = that.dataHolder.wordsForAnki();
        words.sort(function(a, b) {
            return b.diff - a.diff;
        });
        that.ankiWordsCount = words.length;
        let html = "<table class='words-table'><tbody>";
        for (let i = 0; i < words.length; i++) {
            html += "<tr><td>" + words[i].word + "</td><td>" + words[i].pinyin + "</td><td>" +
                words[i].meaning + "</td><td>" + that.formatDiff(words[i].diff) + "</td><td>" +
                "<input type='checkbox' id='ankiChb" + i + "' data-word='" + words[i].word + "'></tr>";
        }
        html += "</tbody></table>";
        html += "<input id='btnGoWords' style='float: right; margin: 5px 0' type='button' value='Go'>";
        that.text.innerHTML = html;
        document.getElementById('btnGoWords').onclick = that.goAnki;
    };

    this.tpNewText = function() {
        that.currentText = that.newText.value;
        that.invalidate();
        that.showhide(that.ovOverlay, false);
        that.showhide(that.ovNewText, false);
    };



    this.ovStageBlur = function() {
        that.showhide(that.ovStage, false);
    };

    this.cpMeanStageMouseUp = function() {
        let stage = dataHolder.getCharStage(that.selection);
        if (stage != STAGE_UNKNOWN) {
            that.showhide(that.ovStageTrivia, stage != STAGE_TRIVIA);
            that.showhide(that.ovStageLearn, stage != STAGE_QUEUE);
            that.showhide(that.ovStageNew, stage != STAGE_NEW);
            that.ovStage.style.bottom = stage == STAGE_LEARN ? "51px" : "63px";
            that.showhide(that.ovStage, true);
            that.ovStage.focus();
            that.stageChangeKey = "c";
        }
    };

    this.cpPinStageMouseUp = function() {
        let stage = dataHolder.getPinyinStage(that.selection);
        if (stage != STAGE_UNKNOWN) {
            that.showhide(that.ovStageTrivia, stage != STAGE_TRIVIA);
            that.showhide(that.ovStageLearn, stage != STAGE_QUEUE);
            that.showhide(that.ovStageNew, stage != STAGE_NEW);
            that.ovStage.style.bottom = stage == STAGE_LEARN ? "21px" : "33px";
            that.showhide(that.ovStage, true);
            that.ovStage.focus();
            that.stageChangeKey = "p";
        }
    };

    this.cpWordsClick = function() {
        let words = that.dataHolder.getContainingWords(that.selection);
        let html = "<table class='words-table'><tbody>";
        for (let i = 0; i < words.length; i++) {
            let diff;
            if (words[i].stage == STAGE_TRIVIA) {
                diff = "T";
            } else if (words[i].stage == STAGE_ANKI) {
                diff = "A"
            } else {
                diff = that.formatDiff(words[i].diff);
            }
            html += "<tr><td>" + words[i].word + "</td><td>" + words[i].pinyin + "</td><td>" +
                words[i].meaning + "</td><td>" + diff + "</td></tr>";
        }
        html += "</tbody></table>";
        that.showhide(that.ovOverlay, true);
        that.showhide(that.ovContainingWords, true);
        that.containingWordsList.innerHTML = html;
    };

    this.updateOptions = function() {
        that.options.c = that.tpColorMeaning.checked;
        that.options.p = that.tpColorPinyin.checked;
        that.options.w = that.tpColorWords.checked;
        that.options.d = that.tpAutoDict.checked;
        that.invalidate();
    };

    this.closeContainingWordsClick = function() {
        that.showhide(that.ovOverlay, false);
        that.showhide(that.ovContainingWords, false);
    };

    this.uiInit = function() {
        _.cpPanel.visible = false;
        _.wpPanel.visible = false;
        that.invalidate();
    };

    this.init = function() {
        that.uiInit();
    }
}