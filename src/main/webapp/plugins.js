var PluginManager = function () {

    let that = this;

    this.plugins = {};

    this.register = function (prefix, entryFcn) {
        that.plugins[prefix] = entryFcn;
    };

    this.run = function(command, data) {
        command = command.trim();
        let indexOf = command.indexOf(" ");
        if (indexOf != -1) {
            data.params = command.substr(indexOf).trim();
            command = command.substr(0, indexOf);
        }

        if (!that.plugins[command]) {
            alert("Plugin not found");
        } else {
            return that.plugins[command](data);
        }

    }

};

var pluginManager = new PluginManager();

/* --- PLUGINS --- */

function shuffle(array) {
    let counter = array.length;

    // While there are elements in the array
    while (counter > 0) {
        // Pick a random index
        let index = Math.floor(Math.random() * counter);

        // Decrease counter by 1
        counter--;

        // And swap the last element with it
        let temp = array[counter];
        array[counter] = array[index];
        array[index] = temp;
    }

    return array;
}

/* --- prelearn pinyins --- */

pluginManager.register("test", function(input) {
    alert("hello");
});


pluginManager.register("prelearn", function(input) {
    let dataHolder = input.dataHolder;
    let chars = dataHolder.chars;
    let pinyins = dataHolder.pinyins;
    let pinsToLearn = [];
    for (let k in pinyins) {
        if (pinyins.hasOwnProperty(k)) {
            let pin = pinyins[k];
            if (pin.stage == 2 && pin.diff < 0) {
                pinsToLearn.push(pin);
            }
        }
    }

    function remove(key) {
        for (let i = 0; i < pinsToLearn.length; i++) {
            if (pinsToLearn[i].word == key) {
                pinsToLearn.splice(i, 1);
                return;
            }
        }
    }

    function reset() {
        shuffle(pinsToLearn);
        var root = document.getElementById("divPrelearn");
        if (!root) {
            root = document.createElement("div");
            root.id = "divPrelearn";
            document.body.appendChild(root);
        }

        let html = "<div style='position: fixed;width: 400px;height: 400px;background: white;border: 2px solid orange;border-radius: 10px;left: 10px;top: 10px;'>";
        let l = 10;
        let t = 10;
        for (let i = 0; i < pinsToLearn.length; i++) {
            let tooltip = pinsToLearn[i].pinyin;
            let key = pinsToLearn[i].word;

            if (chars[key]) {
                tooltip += " (" + chars[key].meaning + ")";
            }

            html += "<div id='prel" + i + "' data-key='" + key +
                "' style='position: absolute; left:" + l + "px; top:" + t +
                "px;background: #ffffcf;padding: 3px 4px;cursor: default;border: 1px solid orange;border-radius: 5px;' title='" +
                tooltip + "'>" + pinsToLearn[i].word + "</div>";
            l += 30;
            if (l > 380) {
                l = 10;
                t += 30;
            }
        }
        html += "<input type='button' id='btnPrelearn' style='position:absolute; left:320px; top:370px' value='reset'>";
        html += "</div>";
        root.innerHTML = html;

        for (let i = 0; i < pinsToLearn.length; i++) {
            var el = document.getElementById("prel" + i);
            el.onclick = function() {
                this.style.display = "none";
            };
            el.oncontextmenu = function(event) {
                this.style.display = "none";
                remove(this.getAttribute("data-key"));
                event.preventDefault();
            }
        }

        document.getElementById("btnPrelearn").onclick = reset;

    }

    reset();

});

/* --- show stat on words --- */
pluginManager.register("wordstat", function(input) {
    let words = input.dataHolder.words;

    let usedChars = {};
    let count = 0;

    for (let key in words) {
        if (words.hasOwnProperty(key)) {
            for (let i = 0; i < key.length; i++) {
                if (!usedChars[key[i]]) {
                    count++;
                    usedChars[key[i]] = true;
                }
            }
        }
    }

    console.log("Total used characters count is " + count);

});
