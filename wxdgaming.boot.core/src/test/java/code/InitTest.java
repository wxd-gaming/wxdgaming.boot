package code;

/**
 * 测试
 *
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2024-01-17 15:15
 **/
public class InitTest {

    public static void main(String[] args) {
        B b = new B();
        System.out.println("=============================");
        B b1 = new B(new A());
    }

    public static class B {
        final A a;

        public B() {
            System.out.println("B()");
            a = new A();
        }

        public B(A a) {
            System.out.println("B(A a)");
            this.a = a;
        }
    }

    public static class A {
        public A() {
            System.out.println("A()");
            System.out.println(1);
        }
    }

}
