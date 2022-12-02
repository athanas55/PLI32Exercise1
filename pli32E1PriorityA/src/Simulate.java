import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;


public class Simulate {
    private final static AtomicInteger CUSTOMERS = new AtomicInteger(100);
    private static final ArrayList<Token> allCustomers = new ArrayList<>();

    public static void main(String[] args) throws InterruptedException {

        Scanner scanner = new Scanner(System.in);
        int priority;
        do {
            System.out.println("if Cashier A has priority, type 1. \nFor equal priority, type 0");
            while (!scanner.hasNextInt()) {
                System.out.println("Wrong Input! The input must be 0 or 1:");
                scanner.next();
                //System.out.println("please choose 0 or 1");
            }
            priority = scanner.nextInt();
            if(priority != 0 && priority != 1)
                System.out.println("Wrong input!");
        }while(priority != 0 && priority != 1);

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

        // we are setting Cashier A priority higher than Cashier B
        // so that if both are available, Cashier A will be chosen
        switch (priority) {
            case 0:
                threadA.setPriority(5);
                threadB.setPriority(5);
            case 1:
                threadA.setPriority(10);
                threadB.setPriority(6);
        }

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
            printResults(cashierA, cashierB);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public static void printResults(Cashier ca, Cashier cb) throws ParseException {
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
        long cashierABusyTime = ca.getBusyHour();
        long cashierBBusyTime = cb.getBusyHour();

        // print a header
        System.out.printf("%s\t%s\t%s\t\t%s\t\t\t%s\t\t%s\t\t%s\t\t%s%n",
                "Customer", "interArrival-time", "arrival-time", "Cashier",
                "service-start", "Duration", "service-end", "waiting-time");
        // iterate through customers to print the details for each one
        int customersWaited = 0;
        int totalWaitingTime = 0;
        for (String[] customer : customers) {
            Date arrivalTime = new SimpleDateFormat("HH:mm:ss").parse(customer[2].
                    substring(customer[2].lastIndexOf("|") + 1));
            Date startService = new SimpleDateFormat("HH:mm:ss").parse(customer[4].
                    substring(customer[4].lastIndexOf("|") + 1));
            long waiting = Duration.between(arrivalTime.toInstant(), startService.toInstant()).toMinutes();
            if(waiting != 0)
                customersWaited++;
            totalWaitingTime += waiting;
            for (String s : customer) System.out.printf("%s\t\t\t", s.substring(s.lastIndexOf("|") + 1));
            System.out.printf("%d%n", waiting);
        }
        // finally, print the total time for servicing all customers
        System.out.println("\nTotal duration: " + (totalTime / 60) + " hours and " + (totalTime % 60) + " minutes");
        System.out.printf("%n%s%.1f%s%n", "CashierA was occupied ", (double) cashierABusyTime / totalTime * 100, "% of the time");
        System.out.printf("%s%.1f%s%n", "CashierB was occupied ", (double) cashierBBusyTime / totalTime * 100, "% of the time");
        System.out.printf("%n%.1f%s%n", (double) customersWaited/customers.size() * 100, "% of the customers had to wait in queue");
        System.out.printf("%s%.1f%s%n", "Average waiting time of all customers: ", (double) totalWaitingTime/customers.size(), " minutes");
        System.out.printf("%s%.1f%s%n", "Average waiting time for those who had to wait in queue: ", (double) totalWaitingTime/customersWaited, " minutes");
    }
}