package org.rx.test.perf;

import org.rx.test.tools.Coster;
import org.rx.test.tools.MLock;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ThreadSwitchPerforamce {
    private static volatile boolean go = false;

    public static void main(String[] args) throws Exception{
        String a = ManagementFactory.getRuntimeMXBean().getName();
        final Lock readyLock = new ReentrantLock();
        final Coster coster = new Coster();

        final List<Long> costList = new ArrayList<Long>();

        // B
        final Thread b = new Thread(new Runnable() {
                    public void run() {
                        while (!go){
                            try {
                                Thread.sleep(1);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        while (true){
                            readyLock.lock();
                            costList.add(coster.getNS());
                            System.out.println("B");

                            readyLock.unlock();

                            if(costList.size() == 10000){
                                print(costList);
                                System.exit(0);
                            }
                        }
                    }
                });
        b.start();

        // A
        new Thread(new Runnable() {
            public void run() {
                while (true){
                    readyLock.lock();
                    go = true;

                    while (b.getState() != Thread.State.WAITING){
                        try {
                            Thread.sleep(1);
                            //System.out.println("sleep:" + b.getState().name());
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    System.out.println("A");
                    coster.reset();
                    readyLock.unlock();

                }
            }
        }).start();

        Thread.sleep(1000*1000);

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
