import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;


public class Simulate {

    private final static AtomicInteger CUSTOMERS = new AtomicInteger(100);
    private static final ArrayList<Token> allCustomers = new ArrayList<>();

    public static void main(String[] args) throws InterruptedException {

        BlockingQueue<Token> blockingQueue = new LinkedBlockingQueue<>(CUSTOMERS.get() + 1);

        // Create and start the vending Machine. The Machine will create
        // 100 token/day. Which will be served by the 2 Cashiers
        TokenVendingMachine tokenVendingMachine = new TokenVendingMachine(blockingQueue, CUSTOMERS, allCustomers);
        Thread tokenMachineThread = new Thread(tokenVendingMachine);
        CountDownLatch countDownLatch = new CountDownLatch(CUSTOMERS.get());

        // Here we have the Token consumers. We create 2 Cashiers
        CashierA cashierA = new CashierA(blockingQueue, countDownLatch);
        CashierB cashierB = new CashierB(blockingQueue, countDownLatch);

        Thread threadA = new Thread(cashierA);
        Thread threadB = new Thread(cashierB);

        // we are setting equal priority between Cashiers
        // so that choice of cashier is based on OS scheduler
        threadA.setPriority(5);
        threadB.setPriority(5);

        tokenMachineThread.start();
        threadA.start();
        threadB.start();

        tokenMachineThread.join();
        threadB.join();
        threadA.join();

        System.out.println("\nBank closed for the day!");

        System.out.println("\n____________________________________________________________________________________________________________________________________");
        System.out.println("****************************************************** printing statistics *********************************************************");
        try {
            printResults();
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public static void printResults() throws ParseException {
        ArrayList<String[]> customers = new ArrayList<>();
        for (Token customer : allCustomers) {
            int counter = 0;
            String[] thisCustomer = new String[customer.getCustomerTimes().size() + 1];
            thisCustomer[counter] = "1. |" + customer.getDescription();
            counter++;
            for (Map.Entry<String, String> entry : customer.getCustomerTimes().entrySet()) {
                thisCustomer[counter] = entry.getKey() + "| " + entry.getValue();
                counter++;
            }
            //sort ascending
            Arrays.sort(thisCustomer);
            customers.add(thisCustomer);
        }

        Date firstCustomerArrived = new SimpleDateFormat("HH:mm:ss").parse(customers.get(0)[2].
                substring(customers.get(0)[2].lastIndexOf("|") + 1));
        Date lastCustomerServed = new SimpleDateFormat("HH:mm:ss").parse(customers.get(customers.size() - 1)[6].
                substring(customers.get(customers.size() - 1)[6].lastIndexOf("|") + 1));

        long totalTime = Duration.between(firstCustomerArrived.toInstant(), lastCustomerServed.toInstant()).toMinutes();

        // print the table header
        System.out.printf("%s\t%s\t%s\t\t%s\t\t\t%s\t\t%s\t\t%s\t\t%s%n",
                "Customer", "interArrival-time", "arrival-time", "Cashier",
                "service-start", "Duration", "service-end", "waiting-time");
        // print the results
        for (String[] customer : customers) {
            Date arrivalTime = new SimpleDateFormat("HH:mm:ss").parse(customer[2].
                    substring(customer[2].lastIndexOf("|") + 1));
            Date startService = new SimpleDateFormat("HH:mm:ss").parse(customer[4].
                    substring(customer[4].lastIndexOf("|") + 1));
            long waiting = Duration.between(arrivalTime.toInstant(), startService.toInstant()).toMinutes();
            for (String s : customer) System.out.printf("%s\t\t\t", s.substring(s.lastIndexOf("|") + 1));
            System.out.printf("%d%n", waiting);
        }

        System.out.println("\nTotal Duration: " + (totalTime / 60) + " hours and " + (totalTime % 60) + " minutes");
    }
}