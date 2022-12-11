import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.SplittableRandom;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class TokenVendingMachine implements Runnable {
    private final BlockingQueue<Token> blockingQueue;
    private final AtomicInteger customers;
    ArrayList<Token> allCustomers;
    SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");

    public TokenVendingMachine(BlockingQueue<Token> blockingQueue,
                               AtomicInteger customers,
                               ArrayList<Token> allCustomers) {
        this.blockingQueue = blockingQueue;
        this.customers = customers;
        this.allCustomers = allCustomers;
    }

    @Override
    public void run() {
        long lastCustomerArrival = 0;
        // generate tokens
        for (int i = 0; i < customers.get(); i++) {
            Token token = new Token();
            token.setId(UUID.randomUUID());
            token.setNumber(i);
            token.setDescription("Customer" + (i + 1));

            long currentCustomerArrival;
            // if this is the first token, then set time to 08:00
            if (i == 0) {
                String bankOpen = "08:00";
                try {
                    Date openHour = timeFormat.parse(bankOpen);
                    currentCustomerArrival = openHour.getTime();
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
            }
            //else set current customer arrival time based on the inter-arrival delay
            else
                currentCustomerArrival = lastCustomerArrival;
            token.setIssued(currentCustomerArrival);
            Date arrival = new Date(currentCustomerArrival);
            String arrived = timeFormat.format(arrival);
            token.putInCustomerTimes("3. Arrived", arrived);

            // Insert the token element in the Queue. Wait if no space is available
            try {
                System.out.println(token.getDescription() + " arrived on " + arrived);
                blockingQueue.put(token);
                allCustomers.add(token);
                // put the thread to sleep for time equal to the
                // inter-arrival time * 10 milliseconds (just to give a sense of reality)
                int timeTillNextArrival = setTimeBetweenTokens();
                Thread.sleep(timeTillNextArrival * 100L);


                // calculate inter-arrival time and add to the HashMap
                long nextArrived = currentCustomerArrival + timeTillNextArrival * 60 * 1000L;
                lastCustomerArrival = nextArrived;
                Date nextArrival = new Date(nextArrived);
                String interArrival = String.valueOf(Duration.between(arrival.toInstant(), nextArrival.toInstant()).toMinutes());
                token.putInCustomerTimes("2. Inter-arrival time", interArrival);

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        // create a token to signal that the number of customers
        // has been reached
        Token finalToken = new Token();
        finalToken.setDescription("DONE");
        try {
            blockingQueue.put(finalToken);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    // method to simulate the discrete probability distribution
    // of inter-arrival times
    public static int setTimeBetweenTokens() {
        int interArrival;
        SplittableRandom random = new SplittableRandom();
        int number = random.nextInt(0, 101);
        if (number <= 40)
            interArrival = 2;
        else if (number <= 65)
            interArrival = 1;
        else if (number <= 85)
            interArrival = 3;
        else
            interArrival = 4;
        return interArrival;
    }
}
