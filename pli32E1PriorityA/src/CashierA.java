import java.util.SplittableRandom;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;

public class CashierA extends Cashier {

    public CashierA(BlockingQueue<Token> blockingQueue,
                    CountDownLatch countDownLatch) {
        super(blockingQueue, countDownLatch);
    }

    @Override
    public int setServiceTime() {
        SplittableRandom random = new SplittableRandom();
        int serviceTime;
        int number = random.nextInt(0, 101);
        if (number <= 30)
            serviceTime = 2;
        else if (number <= 58)
            serviceTime = 3;
        else if (number <= 83)
            serviceTime = 4;
        else
            serviceTime = 5;
        return serviceTime;
    }
}
