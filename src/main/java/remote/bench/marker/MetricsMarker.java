package remote.bench.marker;

import com.codahale.metrics.CsvReporter;
import com.codahale.metrics.MetricRegistry;
import remote.bench.Bench;

import java.io.File;
import java.util.concurrent.TimeUnit;


public class MetricsMarker extends BenchMarker {

    private static MetricRegistry metrics = new MetricRegistry();
    private int reportSecondsInterval = 5;
    private CsvReporter csvReporter;


    public void preBench(String fileName){
        System.out.println("preBench");
        outputFileName=fileName;

        File file = new File(System.getProperty("user.dir"));
        csvReporter = CsvReporter.forRegistry(metrics).build(file);
        csvReporter.start(reportSecondsInterval, TimeUnit.SECONDS);
    }

    public void postBench(){
        System.out.println("postBench");
        csvReporter.stop();
    }

    public void bench(Bench bench) throws Exception{
        com.codahale.metrics.Timer timer = metrics.timer(outputFileName);
        com.codahale.metrics.Timer.Context context;

        long startTime = System.currentTimeMillis();
        long endTime = startTime + (durationSeconds * 1000);
        while(System.currentTimeMillis() < endTime){
            context = timer.time();

            try {
                bench.timeStep();
            }catch (Exception e){
                if(stopAtException){
                    System.out.println(e);
                    throw e;
                }
            }

            context.stop();
        }
        metrics.remove(outputFileName);
    }

    public void benchInterval(Bench bench) throws Exception {
        com.codahale.metrics.Timer timer = metrics.timer(outputFileName);
        com.codahale.metrics.Timer.Context context;

        long startTime = System.currentTimeMillis();
        long endTime = startTime + (durationSeconds * 1000);
        while(System.currentTimeMillis() < endTime){
            context = timer.time();
            long start = System.nanoTime();

            try {
                bench.timeStep();
            }catch (Exception e){
                if(stopAtException){
                    System.out.println(e);
                    throw e;
                }
            }

            context.stop();

            long nextStart = start + expectedIntervalNanos;
            while( System.nanoTime() < nextStart ) {
                //busy-waiting until the next expected interval
            }
        }
        metrics.remove(outputFileName);
    }

}