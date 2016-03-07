package local;

import remote.BenchType;

import java.io.Serializable;

/**
 * Created by danny on 04/03/2016.
 */
public class BenchMarkSettings implements Serializable {

    private String drivers="Members";

    private String threads="64";

    private int warmupSec=30;

    private int durationSec=60;

    private String types= "";


    public BenchMarkSettings(){}

    public void setDrivers(String drivers) {
        this.drivers = drivers;
    }

    public void setDurationSec(int durationSec) {
        this.durationSec = durationSec;
    }

    public void setType(String types) { this.types = types; }

    public void setWarmupSec(int warmupSec) {
        this.warmupSec = warmupSec;
    }

    public void setThreads(String threads) {
        this.threads = threads;
    }



    public String[] getDrivers() {
        return drivers.split(",");
    }

    public int[] getThreads() {

        String[] numberStrs = threads.split(",");
        int[] numbers = new int[numberStrs.length];
        for(int i=0; i<numberStrs.length; i++) {
            numbers[i] = Integer.parseInt(numberStrs[i]);
        }
        return numbers;
    }

    public String[] getTypes() {
        return types.split(",");
    }


    public String getWarmupSec() {
        return Integer.toString(warmupSec);
    }

    public String getDurationSec() {
        return Integer.toString(durationSec);
    }
}
