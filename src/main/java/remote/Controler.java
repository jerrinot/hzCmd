package remote;

import global.Args;
import global.NodeType;
import jms.MQ;
import javax.jms.JMSException;

import java.io.*;
import java.lang.management.ManagementFactory;
import java.util.Enumeration;
import java.util.Properties;

import remote.bench.BenchContainer;
import remote.bench.BenchManager;
import remote.command.Cmd;

import static remote.Utils.recordeException;

public abstract class Controler{

    private BenchManager benchManager;
    private static final String ID = System.getProperty(Args.ID.name());
    private static final String REPLYQ = ID+"reply";
    private static final String jvmPidId = ManagementFactory.getRuntimeMXBean().getName();

    public final NodeType type;

    public Controler(NodeType type) throws Exception {
        this.type=type;
        printProperties();

        try {
            init(type);
            benchManager = new BenchManager(getVendorObject());
            MQ.sendObj(REPLYQ, ID+" Started");
        }catch (Exception e){
            recordeException(e);
            MQ.sendObj(REPLYQ, e);
            throw e;
        }
    }


    public abstract void init(NodeType type)  throws Exception ;

    public abstract Object getVendorObject();

    public void load(String taskId, String clazz, int threadCount){
        try {
            benchManager.loadClass(taskId, clazz, threadCount);
            MQ.sendObj(REPLYQ, "loaded "+taskId+" "+clazz+" "+threadCount);
        } catch (Exception e) {
            try {
                MQ.sendObj(REPLYQ, e);
            } catch (JMSException e2) {}
        }
    }

    public void setField(String taskId, String field, String value){
        try {
            benchManager.setField(taskId, field, value);
            MQ.sendObj(REPLYQ, "set "+taskId+" "+field+" "+value );
        } catch (Exception e) {
            try {
                MQ.sendObj(REPLYQ, e);
            } catch (JMSException e2) {}
        }
    }

    public void ping(){
        try {
            MQ.sendObj(REPLYQ, ID+" ping");
        } catch (JMSException jmsError) {
            recordeException(jmsError);
        }
    }

    public void run() throws IOException {
        while (true){
            try {
                Object obj = MQ.receiveObj(ID);
                System.out.println("MQ msg in = "+obj);

                if(obj instanceof Cmd){
                    ((Cmd) obj).exicute(this);
                }
            } catch (JMSException e) {
                recordeException(e);
            }
        }
    }

    private static void printProperties(){
        Properties p = System.getProperties();
        Enumeration keys = p.keys();
        while (keys.hasMoreElements()) {
            String key = (String)keys.nextElement();
            String value = (String)p.get(key);
            System.out.println(key + ": " + value);
        }
    }

    public String toString() {
        return "HzCmd{" +
                "ID=" + ID +
                "jvmPidId=" + jvmPidId +
                '}';
    }
}