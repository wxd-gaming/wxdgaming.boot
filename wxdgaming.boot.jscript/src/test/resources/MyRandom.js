var oldSeed = Date.now() ^ 999999999;
var addSeed = 0;

/** 刷新随机数种子 */
function refreshSeed() {
    oldSeed = (oldSeed * 9301 + addSeed++) & 233280;
}

function nextInt(min, max, seed) {
    if (max <= min) {
        return min;
    }
    var tmp = max - min;
    var next00 = next(seed);
    return Math.round(min + Math.round(tmp * next00));
}

/**
 * 自定义随机算法, 0 ~ 1
 */
function next(seed) {
    if (!(seed === null
        || typeof (seed) === "undefined")) {
        oldSeed = seed;
    } else {
        refreshSeed();
    }

    var v = oldSeed / 233280.0;
    if (v < 0) {
        v = 0;
    }
    if (v > 1) {
        v = 1;
    }
    return v;
}