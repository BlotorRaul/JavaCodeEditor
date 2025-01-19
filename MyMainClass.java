public class MyMainClass {
    public static void main(String[] args) {
        System.out.println("[Line 1] Start debugging test");
        int x = 10;
        int y = 0;

        // Un mic for, astfel încât să avem mai multe linii
        for (int i = 0; i < 5; i++) {
            x += i;                     // Line 8
            System.out.println("[Line 9] i=" + i + ", x=" + x);

            if (i == 2) {
                y = x + 100;           // Line 13
            }
        }

        System.out.println("[Line 15] final x = " + x);
        y *= 2;                        // Line 16 (aici e un loc bun pentru breakpoint)
        System.out.println("[Line 17] End test: y=" + y);
    }
}


