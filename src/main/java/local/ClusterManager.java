package local;

import global.Bash;
import global.NodeType;

import javax.jms.JMSException;
import java.io.*;
import java.util.*;

import static global.Utils.rangeMap;

public class ClusterManager implements Serializable {

    private final String clusterId;
    private BoxManager boxes;
    private Map<String, RemoteJvm> jvms = new HashMap();

    private int membersOnlyCount;
    private int memberCount=0;
    private int clientCount=0;
    private String homeIp;

    private JvmFactory jvmFactory;

    public ClusterManager(String clusterId, BoxManager boxes, String homeIp, JvmFactory jvmFactory) throws Exception {
        this.clusterId =clusterId;
        this.boxes=boxes;
        this.homeIp=homeIp;
        this.jvmFactory=jvmFactory;
    }

    public boolean matchClusterId(String clusterId){
        return this.clusterId.matches(clusterId);
    }

    public String getClusterId() {
        return clusterId;
    }

    public BoxManager getBoxManager( ){
        return boxes;
    }


    public void setMembersOnlyCount(int count) {
        if(count < 0 || count > boxes.size()){
            membersOnlyCount=0;
        }else{
            membersOnlyCount = count;
        }
    }


    public void addMembers(int qty, String version, String options) throws Exception {
        addJvms(qty, version, options, NodeType.Member);
    }

    public void addClients(int qty, String version, String options) throws Exception {
        addJvms(qty, version, options, NodeType.Client);
    }

    private void addJvms(int qty, String hzVersion, String options, NodeType type) throws Exception {
        List<RemoteJvm> check = new ArrayList();
        for(int i=0; i<qty; i++) {
            check.add(addJvm(hzVersion, options, type));
        }
        for (RemoteJvm jvm : check) {
            System.out.println(jvm);
        }
        for (RemoteJvm jvm : check) {
            Object o = jvm.jvmStartResponse();
            System.out.println(o);
        }
    }

    private RemoteJvm addJvm(String jarVersion, String options, NodeType type) throws Exception {
        int idx;
        int count;
        if(type == NodeType.Member) {
            idx = rangeMap(memberCount++, 0, boxes.size() - membersOnlyCount);
            count=memberCount;
        }else {
            idx = rangeMap(clientCount++, membersOnlyCount, boxes.size());
            count=clientCount;
        }
        RemoteJvm jvm = jvmFactory.createJvm(boxes.get(idx), type, count, clusterId);
        jvms.put(jvm.getId(), jvm);
        jvm.startJvm(jarVersion, options, this);
        return jvm;
    }

    public List<RemoteJvm> getMatchingJms(String jvmId) {
        List<RemoteJvm> matching = new ArrayList<RemoteJvm>();
        for(RemoteJvm jvm : jvms.values()){
            if ( jvm.getId().matches(jvmId) ){
                matching.add(jvm);
            }
        }
        return matching;
    }

    public void load(String jvmId, String taskId, String className) throws IOException, InterruptedException, JMSException{
        for(RemoteJvm jvm : getMatchingJms(jvmId)){
            jvm.load(taskId, className);
        }
    }

    public void invokeAsync(String jvmId, int threadCount, String method, String taskId) throws IOException, InterruptedException, JMSException {
        for(RemoteJvm jvm : getMatchingJms(jvmId)){
            jvm.invokeAsync(threadCount, method, taskId);
        }
    }

    public void invokeSync(String jvmId, int threadCount, String method, String taskId) throws IOException, InterruptedException, JMSException {
        for(RemoteJvm jvm : getMatchingJms(jvmId)){
            jvm.invokeSync(threadCount, method, taskId);
        }
    }

    public void getResponse(String jvmId) throws IOException, InterruptedException, JMSException {
        for(RemoteJvm jvm : getMatchingJms(jvmId)){
            System.out.println(jvm.getResponse());
        }
    }

    public void stop(String jvmId, String taskId) throws IOException, InterruptedException {
        for(RemoteJvm jvm : getMatchingJms(jvmId)){
            //jvm.stop();
        }
    }

    public void restart(String jvmId, String version, String options) throws Exception {
        for(RemoteJvm jvm : getMatchingJms(jvmId)){
            jvm.startJvm(version, options, this);
        }
    }

    public void clean(String jvmId) throws IOException, InterruptedException {
        for(RemoteJvm jvm : getMatchingJms(jvmId)){
            jvm.clean();
        }
    }

    public void exit(String jvmId) throws JMSException {
        for(RemoteJvm jvm : getMatchingJms(jvmId)){
            jvm.exit();
        }
    }

    public void kill(String jvmId) throws IOException, InterruptedException {
        for(RemoteJvm jvm : getMatchingJms(jvmId)){
            jvm.kill();
        }
    }

    public void cat(String jvmId) throws IOException, InterruptedException {
        for(RemoteJvm jvm : getMatchingJms(jvmId)){
            System.out.println(jvm);
            System.out.println(jvm.cat());
        }
    }

    public void tail(String jvmId) throws IOException, InterruptedException {
        for(RemoteJvm jvm : getMatchingJms(jvmId)){
            System.out.println(jvm);
            jvm.tail();
        }
    }

    public void grep(String jvmId, String args) throws IOException, InterruptedException {
        for(RemoteJvm jvm : getMatchingJms(jvmId)){
            System.out.println(jvm);
            System.out.println(jvm.grep(args));
        }
    }

    public void downlonad(String jvmId, String destDir) throws IOException, InterruptedException {
        for(RemoteJvm jvm : getMatchingJms(jvmId)){
            jvm.downlonad(destDir);
        }
    }


    private String toString_memberJvms(){
        String jvms = new String();
        for(RemoteJvm jvm : this.jvms.values()){
            if(jvm.isMember()){
                jvms+=jvm+"\n";
            }
        }
        return jvms;
    }

    private String toString_clientJvms(){
        String jvms = new String();
        for(RemoteJvm jvm : this.jvms.values()){
            if(jvm.isClient()){
                jvms+=jvm+"\n";
            }
        }
        return jvms;
    }

    public void clearStoped(){
        Iterator<Map.Entry<String, RemoteJvm>> i = jvms.entrySet().iterator();
        while (i.hasNext()) {
            Map.Entry<String, RemoteJvm> e = i.next();
            RemoteJvm jvm = e.getValue();
            if(! jvm.isRunning()){
                if(jvm.isMember()){
                    this.memberCount--;
                }else{
                    this.clientCount--;
                }
                i.remove();
            }
        }
    }


    @Override
    public String toString() {

        String jvms = toString_memberJvms();
        jvms += toString_clientJvms();

        return Bash.ANSI_YELLOW+"ClusterManager" +
                " clusterId=" + clusterId +
                ", membersOnlyCount=" + membersOnlyCount +
                ", memberCount=" + memberCount +
                ", clientCount=" + clientCount +
                ", boxCount=" + boxes.size() +
                ", " + boxes +
                "" + jvms +
                Bash.ANSI_RESET;
    }
}