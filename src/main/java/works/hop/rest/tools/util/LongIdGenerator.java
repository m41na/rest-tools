package works.hop.rest.tools.util;

import java.util.concurrent.atomic.AtomicLong;

public class LongIdGenerator implements MockIdGenerator<Long>{
    
    private final String table;
    private final AtomicLong gen;

    public LongIdGenerator(String table, Long start) {
        this.table = table;
        this.gen = new AtomicLong(start);
    }

    public String getTable() {
        return table;
    }

    @Override
    public Long nextId() {
        return gen.incrementAndGet();
    }
}
