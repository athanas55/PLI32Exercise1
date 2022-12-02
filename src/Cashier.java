import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Date;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;

public abstract class Cashier implements Runnable {
    protected final BlockingQueue<Token> blockingQueue;
    protected final CountDownLatch countDownLatch;
    SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
    private long busyHour = 0;

    protected Cashier(BlockingQueue<Token> blockingQueue,
                      CountDownLatch countDownLatch) {
        this.blockingQueue = blockingQueue;
        this.countDownLatch = countDownLatch;
    }

    public void run() {
        // Serving token one by one in an infinite loop.
        //The Loop will break while there are no more token to serve
        int countServed = 0;
        long previousServiceEnd = 0;
        while (countDownLatch.getCount() > 0) {
            try {
                // Serving the customer with the token
                Token token = blockingQueue.take();
                token.putInCustomerTimes("4. Cashier", this.getClass().getName());

                // take the timestamp for the initiation
                // of the customer service by the cashier
                long serviceStart;
                if (countServed == 0 || previousServiceEnd < token.getIssued())
                    serviceStart = token.getIssued();
                else
                    serviceStart = previousServiceEnd;

                Date sStart = new Date(serviceStart);
                String servStart = timeFormat.format(sStart);
                token.putInCustomerTimes("5. Service start", servStart);

                if (token.getDescription().equals("DONE"))
                    break;
                System.out.println(getClass().getName() +
                        " started serving customer " + (token.getNumber() + 1) +
                        " Service start time: " + servStart);
                int serviceDuration = setServiceTime();
                busyHour += serviceDuration;
                Thread.sleep(serviceDuration * 100L);

                countServed++;
                // take the timestamp for the completion
                // of the customer service by the cashier
                long serviceEnd = serviceStart + serviceDuration * 60 * 1000L;
                previousServiceEnd = serviceEnd;
                Date sEnd = new Date(serviceEnd);
                String servEnd = timeFormat.format(sEnd);
                token.putInCustomerTimes("7. Serviced", servEnd);

                //calculate service duration and put to the HashMap
                String serviceDuration2 = String.valueOf(Duration.between(sStart.toInstant(), sEnd.toInstant()).toMinutes());
                token.putInCustomerTimes("6. Service duration", serviceDuration2);

                System.out.println("customer " + (token.getNumber() + 1) + " served in " + (serviceEnd - serviceStart) / (1000 * 60) + " minutes");
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                //Decrementing count from the Countdown Latch as the token is served
                countDownLatch.countDown();
            }
        }
    }

    public abstract int setServiceTime();

    public long getBusyHour() {
        return busyHour;
    }
}
