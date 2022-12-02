import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class Token {
    private UUID id;
    private int number;
    private String description;

    private long issued;

    ConcurrentHashMap<String, String> tokenTimes = new ConcurrentHashMap<>();

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setIssued(long time) {
        this.issued = time;
    }

    public long getIssued() {
        return issued;
    }

    @Override
    public String toString() {
        return "Token [id=" + id + ", number=" + number + ", description=" + description + "]";
    }

    public ConcurrentHashMap<String, String> getCustomerTimes() {
        return tokenTimes;
    }

    public void putInCustomerTimes(String description, String timeStamp) {
        tokenTimes.put(description, timeStamp);
    }
}
