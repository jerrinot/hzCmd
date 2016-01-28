package local;

import global.Args;
import global.Bash;
import global.NodeType;
import jms.MQ;
import remote.command.*;

import javax.jms.JMSException;
import java.io.IOException;
import java.io.Serializable;

import static global.Utils.myIp;

public abstract class RemoteJvm implements Serializable {

    public static final String libPath = "$HOME/" + Installer.REMOTE_LIB + "/*";

    public static final String outFile = "out.txt";

    public static String homeIP;

    static {
        try {
            homeIP = myIp();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected final Box box;
    protected final NodeType type;
    protected final String id;
    protected final String dir;
    protected int pid = 0;

    public RemoteJvm(Box box, NodeType type, String id) throws IOException, InterruptedException {
        this.box = box;
        this.type = type;
        this.id = id;
        this.dir = Installer.REMOTE_HZCMD_ROOT + "/" + id;
        box.ssh("mkdir -p " + dir);
    }

    public abstract String getClassToRun();

    public abstract String getVendorLibDir(String version);

    public abstract void beforeJvmStart(ClusterManager myCluster) throws Exception;


    public final void startJvm(String version, String jvmOptions, ClusterManager myCluster) throws Exception {

        beforeJvmStart(myCluster);

        if (isRunning()) {
            System.out.println("all ready started " + this);
            return;
        }

        String classToRun = getClassToRun();
        String vendorLibDir = getVendorLibDir(version) + "/*";

        String jvmArgs = new String();

        //jvmArgs += "-D"+"MQ_BROKER_IP="+homeIP+" ";
        jvmArgs += "-D"+Args.EVENTQ+"="+System.getProperty("user.dir")+"/"+Args.EVENTQ.name() + " ";
        jvmArgs += "-D"+Args.ID+"=" + id + " ";
        jvmArgs += "-XX:OnOutOfMemoryError=\"touch " + id + ".oome" + "\" ";

        /*
        String takipiJavaAgent = "-agentlib:TakipiAgent";
        String takipiProp = "\"-Dtakipi.name=\"" + id;
        String goString = "cd " + dir + "; nohup java -cp \"" + libPath + ":" + vendorLibDir + "\" " + jvmArgs + " " + jvmOptions + " " + classToRun + " >> " + outFile + " 2>&1 & echo $!";
        System.out.println(goString);
        */

        String pidStr = box.ssh("cd " + dir + "; nohup java -cp \"" + libPath + ":" + vendorLibDir + "\" " + jvmArgs + " " + jvmOptions + " " + classToRun + " >> " + outFile + " 2>&1 & echo $!");
        pid = Integer.parseInt(pidStr.trim());
    }

    public void clean() throws IOException, InterruptedException {
        box.rm(dir + "/*");
    }

    public void kill() throws IOException, InterruptedException {
        if (pid != 0) {
            box.killHard(pid);
            pid = 0;
        }
    }

    public void exit() throws JMSException {
        MQ.sendObj(id, new ExitCmd());
    }

    public boolean isRunning() {
        try {
            if (pid == 0) {
                return false;
            }

            boolean running = box.sshWithExitCode("ps -p " + pid) == 0;

            if (!running) {
                pid = 0;
            }
            return running;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public Object jvmStartResponse() throws JMSException {
        return MQ.receiveObj(id);
    }

    public void load(String taskId, String className) throws IOException, InterruptedException, JMSException {
        LoadCmd cmd = new LoadCmd(taskId, className);
        MQ.sendObj(id, cmd);
    }

    public void setField(String taskId, String field, String value) throws Exception {
        SetFieldCmd cmd = new SetFieldCmd(taskId, field, value);
        MQ.sendObj(id, cmd);
    }

    public void invokeAsync(int threadCount, String method, String taskId) throws IOException, InterruptedException, JMSException {
        InvokeAsyncCmd cmd = new InvokeAsyncCmd(threadCount, method, taskId);
        MQ.sendObj(id, cmd);
    }

    public void invokeSync(int threadCount, String method, String taskId) throws IOException, InterruptedException, JMSException {
        InvokeSyncCmd cmd = new InvokeSyncCmd(threadCount, method, taskId);
        MQ.sendObj(id, cmd);
    }

    public void ping() throws IOException, InterruptedException, JMSException {
        PingCmd cmd = new PingCmd();
        MQ.sendObj(id, cmd);
    }

    public Object getResponse() throws IOException, InterruptedException, JMSException {
        return MQ.receiveObj(id+"reply");
    }

    public Object getResponse(long timeout) throws IOException, InterruptedException, JMSException {
        return MQ.receiveObj(id+"reply", timeout);
    }

    public String cat() throws IOException, InterruptedException {
        return box.cat(dir + "/" + outFile);
    }

    public String ssh(String cmd) throws IOException, InterruptedException {
        return box.ssh("cd " + dir + "; " + cmd);
    }

    public void tail() throws IOException, InterruptedException {
         box.tail(dir+"/"+outFile);
    }

    public String grep(String args) throws IOException, InterruptedException {
        return box.grep("'"+args+"' "+dir+"/"+outFile);
    }

    public void downlonad(String destDir) throws IOException, InterruptedException {
        box.downlonad(dir+"/*", destDir+"/"+id+"-"+box.pri);
    }

    public String getId(){ return id; }

    public String toString() {
        boolean running = isRunning();
        String color = running ? Bash.ANSI_GREEN : Bash.ANSI_RED;
        return color + "RemoteHzJvm{" +
                " ID=" + id +
                ", isRunning=" + running +
                ", pid=" + pid +
                ", type=" + type +
                ", dir=" + dir +
                " ip=" + box +
                '}' + Bash.ANSI_RESET;
    }

    public boolean isMember(){
        return type == NodeType.Member;
    }

    public boolean isClient(){
        return type == NodeType.Client;
    }
}