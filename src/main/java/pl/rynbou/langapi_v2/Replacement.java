package pl.rynbou.langapi_v2;

public class Replacement {

    private String from;
    private String to;

    public Replacement(String from, String to) {
        this.from = from;
        this.to = to;
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }
}
