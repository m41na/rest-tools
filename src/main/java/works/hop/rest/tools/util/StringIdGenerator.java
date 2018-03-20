package works.hop.rest.tools.util;

import java.util.Random;
import java.util.UUID;

public class StringIdGenerator implements MockIdGenerator<String> {

    private final String table;

    public StringIdGenerator(String table) {
        this.table = table;
    }

    public String getTable() {
        return table;
    }

    @Override
    public String nextId() {
        Random r = new Random();
        int Low = 65; //'A'
        int High = 90; //'Z'
        int result = r.nextInt(High + 1 - Low) + Low;
        return Character.toString((char)result) + UUID.randomUUID();
    }
}
