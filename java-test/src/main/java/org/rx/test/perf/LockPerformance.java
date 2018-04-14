package org.rx.test.perf;

import org.rx.test.tools.Coster;

import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class LockPerformance {
    private static Object o = new Object();

    public static void main(String[] args) throws Exception{
        for(int i = 0; i < 10; i++){
            test();
            System.out.println("----------------------------");
        }
    }
    public static void test() throws Exception{
        Lock mlock = new ReentrantLock();


        List<Long> lock = new ArrayList<Long>();
        List<Long> unlock = new ArrayList<Long>();

        List<Long> clock = new ArrayList<Long>();
        List<Long> unclock = new ArrayList<Long>();
        Coster coster = new Coster();

        for (int i = 0; i < 500000; i++) {
            nothing(3);coster.reset();
            synchronized (o) {
                lock.add(coster.getNS());

                nothing(3);coster.reset();
            }
            unlock.add(coster.getNS());

            nothing(3);coster.reset();
            mlock.lock();
            clock.add(coster.getNS());

            nothing(3);coster.reset();
            mlock.unlock();
            unclock.add(coster.getNS());
        }
        print(lock);
        print(unlock);
        print(clock);
        print(unclock);

        FileWriter writer = new FileWriter("a.txt");
        for(long l: lock){
            writer.write("" + l + "\n");
        }
        writer.close();
    }
    public static void nothing(int i){
        if(i < 0){
            return;
        }else{
            nothing(i-1);
        }
        if(i < 1){
            nothing(i-2);
        }
    }
    public static void print(List<Long> vals){
        System.out.println(String.format("%10d,%10d,%10d", avg(vals), max(vals), min(vals)));
    }
    public static long avg(List<Long> vals){
        long sum = 0;
        for(Long l: vals){
            sum += l;
        }
        return sum/vals.size();
    }
    public static long min(List<Long> vals){
        long val = Long.MAX_VALUE;
        for(Long l: vals){
            val = l < val ? l : val;
        }
        return val;
    }
    public static long max(List<Long> vals){
        long val = Long.MIN_VALUE;
        for(Long l: vals){
            val = l > val ? l : val;
        }
        return val;
    }


}