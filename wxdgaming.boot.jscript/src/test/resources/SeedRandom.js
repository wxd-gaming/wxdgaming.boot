class SeedRandom {

    oldSeed = 0;
    addSeed = 0;

    constructor(seed) {
        this.oldSeed = seed;
    }

    refreshSeed() {
        this.oldSeed = (this.oldSeed * 9301 + this.addSeed++) & 233280;
    }

    nextInt(min, max) {
        if (max <= min) {
            return min;
        }
        let tmp = max - min;
        let next00 = this.next();
        return Math.round(min + Math.round(tmp * next00));
    }


    nextFloat(min, max) {
        if (max <= min) {
            return min;
        }
        let tmp = max - min;
        let next00 = this.next();
        return min + tmp * next00;
    }


    /**
     * 自定义随机算法, 0 ~ 1
     */
    next() {
        this.refreshSeed();
        let v = this.oldSeed / 233280.0;
        if (v < 0) {
            v = 0;
        }
        if (v > 1) {
            v = 1;
        }
        return v;
    }

}

function onInit() {
    console.log("js SeedRandom.onInit")
    let seedRandom = new SeedRandom(9527003456)
    for (let i = 1; i <= 10; i++) {
        console.log(i, seedRandom.nextInt(1, 1000))
    }
}