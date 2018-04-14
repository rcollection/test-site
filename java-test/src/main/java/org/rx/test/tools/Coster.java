package org.rx.test.tools;

public class Coster {
    private CPUCounter counter = new CPUCounter();
    private long start = counter.count();

    public long getNS(){
        return counter.count() - start;
    }
    public long getAndReset(){
        long mark = counter.count();
        long ret = mark - start;
        start = mark;
        return ret;
    }
    public void reset(){
        start = counter.count();
    }
}
