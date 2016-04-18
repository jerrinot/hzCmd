package remote.command.bench;

import remote.Controler;
import remote.command.Cmd;

import java.io.Serializable;

/**
 * Created by danny on 22/01/2016.
 */
public class MetaDataCmd implements Cmd, Serializable{

    private String taskId;
    private String metaData;

    public MetaDataCmd(String taskId, String metaData){
        this.taskId = taskId;
        this.metaData = metaData;
    }

    public void exicute(Controler c){
        c.writeMetaData(taskId, metaData);
    }

    @Override
    public String toString() {
        return "MetaDataCmd{" +
                "metaData='" + metaData + '\'' +
                ", taskId='" + taskId + '\'' +
                '}';
    }
}
