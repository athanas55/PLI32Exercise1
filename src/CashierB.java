import java.util.SplittableRandom;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;

public class CashierB extends Cashier {

    public CashierB(BlockingQueue<Token> blockingQueue,
                    CountDownLatch countDownLatch) {
        super(blockingQueue, countDownLatch);
    }

    @Override
    public int setServiceTime() {
        SplittableRandom random = new SplittableRandom();
        int serviceTime;
        int number = random.nextInt(0, 101);
        if (number <= 35)
            serviceTime = 3;
        else if (number <= 60)
            serviceTime = 4;
        else if (number <= 80)
            serviceTime = 5;
        else
            serviceTime = 6;
        return serviceTime;
    }
}