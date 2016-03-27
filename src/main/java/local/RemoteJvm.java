package local;

import global.Args;
import global.Bash;
import global.NodeType;
import jms.MQ;
import remote.command.*;

import javax.jms.JMSException;
import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import static global.Utils.myIp;

public abstract class RemoteJvm implements Serializable {


    public static final String outFile = "out.txt";

    protected String jhicAgent="";

    protected final Box box;
    protected final NodeType type;
    protected final String id;
    protected final String clusterId;

    protected final String dir;

    //protected String version;
    protected String vendorLibDir;

    private String launchCmd;
    protected int pid = 0;

    public RemoteJvm(Box box, NodeType type, String id, String clusterId) throws IOException, InterruptedException {
        this.box = box;
        this.type = type;
        this.id = id;
        this.clusterId=clusterId;
        this.dir = Installer.REMOTE_HZCMD_ROOT + "/" + id;
        box.ssh("mkdir -p " + dir);
    }

    public abstract String getClassToRun();

    public abstract void beforeJvmStart(ClusterManager myCluster) throws Exception;

    public abstract String setJvmStartOptions(Box thisBox, ClusterManager myCluster) throws Exception;


    public void startJvm(String jvmOptions, String libDir, ClusterManager myCluster, String brokerIP) throws Exception {

        this.vendorLibDir = libDir;

        if (isRunning()) {
            System.out.println(Bash.ANSI_CYAN+"all ready started " + this +Bash.ANSI_RESET);
            return;
        }

        beforeJvmStart(myCluster);

        String jvmArgs = setJvmStartOptions(box, myCluster);
        if(jvmArgs==null){
            jvmArgs = new String();
        }

        String classToRun = getClassToRun();

        jvmArgs +=" ";
        jvmArgs += "-D"+"MQ_BROKER_IP="+brokerIP+" ";
        jvmArgs += "-D"+Args.EVENTQ+"="+getEventQueueName() + " ";
        jvmArgs += "-D"+Args.ID+"=" + id + " ";
        jvmArgs += "-XX:+HeapDumpOnOutOfMemoryError" + " ";
        jvmArgs += "-XX:HeapDumpPath="+id+".hprof" + " ";
        jvmArgs += "-XX:OnOutOfMemoryError=\" date >> " + id + ".oome" + "\" ";



        //String takipiJavaAgent = "-agentlib:TakipiAgent";    String takipiProp = "\"-Dtakipi.name=\"" + id;


        HzCmdProperties properties = new HzCmdProperties();
        if(properties.getBoolean(HzCmdProperties.jhic, "false")) {
            jhicAgent = "-javaagent:jHiccup.jar=\"-d 0 -i 1000 -l " + clusterId + "-hiccuplog -c\"";
        }

        launchCmd = "cd " + dir + "; nohup java "+jhicAgent+" -cp \"" + Installer.REMOTE_HZCMD_LIB_FULL_PATH+"/*" + ":" +  vendorLibDir+"/*"  + "\" " + jvmArgs + " " + jvmOptions + " " + classToRun + " >> " + outFile + " 2>&1 & echo $!";

        launchJvm(launchCmd);
    }

    public String getEventQueueName(){
        return  System.getProperty("user.dir")+"/"+Args.EVENTQ.name();
    }

    public final void reStartJvm(ClusterManager myCluster) throws Exception {
        if(launchCmd==null){
            System.out.println(Bash.ANSI_RED+"NO launchCmd, jvm never started"+this+Bash.ANSI_RESET);
            return;
        }
        if(  isRunning() ){
            System.out.println(Bash.ANSI_RED+" JVM is Running "+this+Bash.ANSI_RESET);
            return;
        }

        System.out.println(launchCmd);
        beforeJvmStart(myCluster);
        launchJvm(launchCmd);
    }

    private void launchJvm(String launch) throws IOException, InterruptedException {
        String pidStr = box.ssh(launch);
        pid = Integer.parseInt(pidStr.trim());
    }

    public void clean() throws IOException, InterruptedException {
        kill();
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

    public String ls() throws IOException, InterruptedException {
        return box.ssh("ls " + dir + "/");
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
        box.downlonad(dir + "/*", destDir + "/" + id);
    }

    public void upload(String src, String dst) throws IOException, InterruptedException {
        box.upload(src, dst);
    }

    public void uploadcwd(String src) throws IOException, InterruptedException {
        if (src!=null){
            List<String> files = Arrays.asList(src.split(","));
            for (String file : files) {
                box.upload(file, dir + "/");
            }
        }
    }

    public Box getBox(){return box;}

    public String getId(){ return id; }

    public String toString() {
        boolean running = isRunning();
        String color = running ? Bash.ANSI_GREEN : Bash.ANSI_RED;
        return color + "RemoteJvm{" +
                " ID=" + id +
                ", isRunning=" + running +
                ", pid=" + pid +
                ", lib=" + vendorLibDir +
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