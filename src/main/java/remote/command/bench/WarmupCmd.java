package remote.command.bench;

import remote.Controler;
import remote.command.Cmd;

import java.io.Serializable;

/**
 * Created by danny on 22/01/2016.
 */
public class WarmupCmd implements Cmd, Serializable{

    private String taskId;
    private int seconds ;

    public WarmupCmd(String taskId, int seconds){
        this.taskId = taskId;
        this.seconds = seconds;
    }

    public void exicute(Controler c){
        c.warmupBench(taskId, seconds);
    }
}
