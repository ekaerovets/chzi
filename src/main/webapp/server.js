function Server() {

    let that = this;

    this.ajax = function(url, method, data) {
        let xhr = new XMLHttpRequest();
        xhr.open(method, HOST + url, false);
        xhr.send(JSON.stringify(data));
        if (xhr.status == 200) {
            return xhr.responseText;
        } else {
            alert("Network error");
            throw "error";
        }
    };

    this.insert = function(item, type) {
        that.ajax("insert", "POST", {item: item, type: type});
    };

    this.update = function(item, type) {
        that.ajax("update", "POST", {item: item, type: type});
    };

    this.getInitialData = function() {
        return JSON.parse(that.ajax("data", "GET", null));
    };

    this.lookup = function(word) {
        if (word) {
            return that.ajax("lookup/" + word, "GET", null);
        }
    }
}
