function randomInt(min, max, seed) {
    return R.nextInt(min, max, seed);
}

function randomFloat(min, max, seed) {
    return R.nextFloat(min, max, seed);
}

var R = {
    oldSeed: Date.now() ^ 999999999,
    addSeed: 0,

    /** 刷新随机数种子 */
    refreshSeed: function () {
        this.oldSeed = (this.oldSeed * 9301 + this.addSeed++) & 233280;
    },

    nextInt: function (min, max, seed) {
        if (max <= min) {
            return min;
        }
        var tmp = max - min;
        var next00 = this.next(seed);
        console.log(next00);
        return Math.round(min + Math.round(tmp * next00));
    },

    nextFloat: function (min, max, seed) {
        if (max <= min) {
            return min;
        }
        var tmp = max - min;
        var next00 = this.next(seed);
        // console.log(tmp + " - " + next00);
        return min + tmp * next00;
    },

    /**
     * 自定义随机算法, 0 ~ 1
     */
    next: function (seed) {

        if (!(seed === null
            || typeof (seed) === "undefined")) {
            this.oldSeed = seed;
        } else {
            this.refreshSeed();
        }
        var v = this.oldSeed / 233280.0;
        if (v < 0) {
            v = 0;
        }
        if (v > 1) {
            v = 1;
        }
        return v;
    }
}

function test11() {
    var xhr = new XMLHttpRequest();
    xhr.open('POST', 'http://127.0.0.1:18611/sign');
    xhr.setRequestHeader('Content-Type', 'application/json;charset=utf-8');
    xhr.send(JSON.stringify({"userName": "test", "userPwd": "dGVzdA=="}));
    xhr.onload = function (e) {
        var xhr = e.target;
        console.log(xhr.responseText);
    }
}