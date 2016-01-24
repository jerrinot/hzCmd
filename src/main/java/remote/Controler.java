package remote;

import global.Args;
import global.NodeType;
import jms.MQ;
import javax.jms.JMSException;

import java.io.*;
import java.lang.management.ManagementFactory;

import remote.command.Cmd;

public abstract class Controler{

    protected static TaskManager tasks;

    public static final String ID = System.getProperty(Args.ID.name());
    public static final String jvmPidId = ManagementFactory.getRuntimeMXBean().getName();

    public final PrintStream exceptionWrite = new PrintStream(new FileOutputStream("exception.txt", true));

    public final NodeType type;

    public Controler(NodeType type) throws Exception {
        this.type=type;

        try {
            init(type);
            tasks = new TaskManager(getVendorObject());
            MQ.sendObj(ID, "OK");
        }catch (Exception e){
            e.printStackTrace();
            e.printStackTrace(exceptionWrite);
            MQ.sendObj(ID, e);
            throw e;
        }
    }

    public abstract void init(NodeType type)  throws Exception ;

    public abstract Object getVendorObject();

    public void load(String taskId, String clazz){
        try {
            tasks.loadClass(taskId, clazz);
        } catch (Exception e) {
            e.printStackTrace();
            e.printStackTrace(exceptionWrite);
            try {
                MQ.sendObj(ID, e);
            } catch (JMSException jmsError) {
                jmsError.printStackTrace();
                jmsError.printStackTrace(exceptionWrite);
            }
        }
    }

    public void run() throws IOException {
        while (true){
            try {
                Object obj = MQ.receiveObj(ID);
                System.out.println("recived MQ msg = "+obj);

                if(obj instanceof Cmd){
                    ((Cmd) obj).exicute(this);
                }
            } catch (JMSException e) {
                e.printStackTrace();
            }


          /*
                case invoke:
                    tasks.invokeNonBlocking(Integer.parseInt(words[1]), words[2], words[3]);
                    break;
                case info:
                    sendBack(this.toString());
                    break;
            */
        }
    }

    public String idString(){
        return "HzCmd{" + "ID=" + ID +", "+ "jvmPidId=" + jvmPidId + '}';
    }

    @Override
    public String toString() {
        return "HzCmd{" +
                "ID=" + ID +
                "jvmPidId=" + jvmPidId +
                ", tasks=" + tasks +
                '}';
    }
}