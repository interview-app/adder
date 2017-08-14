package interview.app;

public class Main {

    public static void main(String args[]) throws Exception {
        String filePath = args[0];
        int coresNumber = Runtime.getRuntime().availableProcessors();
        int addersNumber = (int) Math.ceil(coresNumber * 0.33);
        int readersNumber = addersNumber * 2;

        String result = CalculationManager.calculate(filePath, readersNumber, addersNumber);
        System.out.println(result);
    }
}
