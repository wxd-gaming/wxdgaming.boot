function myFun(param) {
    console.log('hello ' + param);
    return 1;
}

function random0() {
    let random = new Random(3);
    for (let i = 0; i < 20; i++) {
        console.log(random.next());
    }
}

class Random {

    // 实例化一个随机数生成器，seed=随机数种子，默认当前时间
    constructor(seed) {
        this.seed = (seed || Date.now()) % 999999999;
    }

    // 取一个随机整数 max=最大值（0开始，不超过该值） 默认10
    next(max) {
        max = max || 10;
        this.seed = (this.seed * 9301 + 49297) % 233280;
        let val = this.seed / 233280.0;
        return Math.floor(val * max);
    }

}