let STAGE_TRIVIA = 1;
let STAGE_LEARN = 2;
let STAGE_NEW = 3;
let STAGE_QUEUE = 4;
let STAGE_UNKNOWN = 5;
let STAGE_ANKI = 4;

let HOST = "rest/";

var today = function() {
    return ((new Date().getTime() / 1000 / 3600 / 24) | 0) - 16200;
};