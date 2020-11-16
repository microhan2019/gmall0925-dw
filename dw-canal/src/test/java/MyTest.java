public class MyTest {
    public static void main(String[] args) {

        for (int i = 1; i <= 5; i++) {
            for (int j = 1; j <= 5; j++) {
                System.out.print("*");
                if (i == j) {
                    break;
                   // continue;  //如果有continue，后面的语句就不执行了
                }
            }
            System.out.println();
        }
    }
}
