package org.rx.test.tools;

public class CPUCounter {
    static {
        System.loadLibrary("tools");
    }
    public native long count();

    public static void main(String[] args) throws Exception{
        CPUCounter cpuCounter = new CPUCounter();
        while (true){
            System.out.println(cpuCounter.count());
            Thread.sleep(1000);
        }
    }
}
