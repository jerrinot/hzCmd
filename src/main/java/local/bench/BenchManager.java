package local.bench;

import local.properties.OrderedProperties;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;


public class BenchManager {

    private String file;
    private Map<String, BenchMark> benchMarks = new HashMap();

    public BenchManager(String file) {
        this.file=file;
        InputStream input;
        try {
            Properties prop = new OrderedProperties();
            input = new FileInputStream(file);
            prop.load(input);

            Enumeration<?> enumeration = prop.propertyNames();
            while (enumeration.hasMoreElements()) {
                String key = (String) enumeration.nextElement();
                String value = prop.getProperty(key);

                String[] keyFile = key.split("@");

                key = keyFile[0];
                String feild = keyFile[1];

                FieldValue fv = new FieldValue(feild, value);

                BenchMark benchMark = benchMarks.get(key);
                if(benchMark==null){
                    benchMark=new BenchMark(key);
                    benchMarks.put(benchMark.getId(), benchMark);
                }
                benchMark.addAttribute(fv);
            }
        } catch (Exception e) {
            System.out.println(e);
            System.exit(1);
        }

        for (BenchMark benchMark : benchMarks.values()) {
            benchMark.setupBenchMarkStack();
        }

    }

    public boolean hasBench() {
        for (BenchMark benchMark : benchMarks.values()) {
            int count = benchMark.benchStackCount();
            if(count > 0){
                return true;
            }
        }
        return false;
    }

    public void popBenchMarks(){
        for (BenchMark benchMark : benchMarks.values()) {
            benchMark.popBenchMark();
        }
    }

    public Collection<BenchMark> getBenchMarks(){
        return benchMarks.values();
    }

    public String currentBench_toString() {

        String str = new String();
        for (BenchMark benchMark : benchMarks.values()) {
            str+=benchMark.currentBench_toString()+"\n";
        }
        str = str.trim();
        return str;
    }

    public String toString() {

        String str = "file="+file+"\n";
        for (BenchMark benchMark : benchMarks.values()) {
            str+=benchMark+"\n";
        }
        str = str.trim();
        return str;
    }

    public static void main(String[] args) throws InterruptedException, IOException {
        BenchManager benchManager = new BenchManager("config.properties");
        System.out.println(benchManager);
    }

}
