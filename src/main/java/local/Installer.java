package local;

import global.Bash;

import java.io.IOException;

//TODO  split this to a intaller of HzCmd and the vendor jars of the cluster / IMDG
public abstract class Installer {

    public static String REMOTE_HZCMD_ROOT = "hzCmd-root";
    public static String REMOTE_LIB = REMOTE_HZCMD_ROOT + "/lib";
    public static String REMOTE_HZ_LIB = REMOTE_HZCMD_ROOT + "/hz-lib";

    public static String HOME = "HOME";
    public static String M2_DIR = "/.m2/";
    public static String M2_Repo = System.getenv(HOME)+M2_DIR;

    public static String hazelcast = "hazelcast-";
    public static String hazelcastEE = hazelcast+"enterprise-";

    public static String hazelcastClient = hazelcast+"client-";
    public static String hazelcastClientEE = hazelcastEE+"client-";

    public static String jar = ".jar";


    public static void install(BoxManager boxes, boolean ee,  String... versions) throws IOException, InterruptedException {
        String memberJar;
        String clientJar;

        String mainJars = Bash.find(M2_Repo, "hzCmd-1.0.1.jar");
        String cacheJars = Bash.find(M2_Repo, "cache-api-1.0.0.jar");
        String guavaars = Bash.find(M2_Repo, "guava-15.0-rc1.jar");
        String hdr = Bash.find(M2_Repo, "HdrHistogram-2.1.8.jar");
        String mq = Bash.find(M2_Repo, "activemq-all-5.13.0.jar");
        String metrics = Bash.find(M2_Repo, "metrics-core-3.1.1.jar");
        String slf4j = Bash.find(M2_Repo, "slf4j-api-1.7.7.jar");

        boxes.mkdir(REMOTE_LIB);
        boxes.upload(mainJars, REMOTE_LIB);
        boxes.upload(cacheJars, REMOTE_LIB);
        boxes.upload(guavaars, REMOTE_LIB);
        boxes.upload(hdr, REMOTE_LIB);
        boxes.upload(mq, REMOTE_LIB);
        boxes.upload(metrics, REMOTE_LIB);
        boxes.upload(slf4j, REMOTE_LIB);


        for (String version : versions) {
            if (ee) {
                memberJar = Bash.find(M2_Repo, hazelcastEE + version + jar);
                clientJar = Bash.find(M2_Repo, hazelcastClientEE + version + jar);
            } else {
                memberJar = Bash.find(M2_Repo, hazelcast + version + jar);
                clientJar = Bash.find(M2_Repo, hazelcastClient + version + jar);
            }
            boxes.mkdir(REMOTE_HZ_LIB+"/"+version);
            boxes.upload(memberJar,  REMOTE_HZ_LIB+"/"+version);
            boxes.upload(clientJar,  REMOTE_HZ_LIB+"/"+version);
        }
    }

}