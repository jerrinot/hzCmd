package cmdline.util;

import cmdline.base.Command;
import com.github.rvesse.airline.annotations.Arguments;
import com.github.rvesse.airline.annotations.Option;
import global.ClusterSize;
import global.ClusterType;
import main.HzCmd;

import java.util.List;

@com.github.rvesse.airline.annotations.Command(name="cluster", description = "create/add cluster members")
public  class ClusterInit extends Command {

    @Option(name = "-user", description = "user name to access boxes default ec2-user")
    public String user="ec2-user";

    @Option(name = "-size", description = "M[0-9]+C[0-9]+  default M4C4")
    public String size="M4C4";

    @Option(name = "-boxes", description = "boxes file")
    public String boxes;

    @Option(name = "-id", description = "set name of cluster")
    public String id = "HZ";

    @Option(name = "-type", description = "cluster tye [HZ|GG|GEM] default HZ" )
    public String type = "HZ";

    @Option(name = "-ee", description = "enterprise switch default false")
    public boolean ee = false;

    @Option(name = "-v", description = "jar version's string")
    public String versions=null;

    @Option(name = "-upcwd", description = "contigues list of file in a comma delimited string, upload to cwd")
    public String cwd_file=null;

    @Option(name = "-uplib", description = "contigues list of file in a comma delimited string, upload to lib dir")
    public String lib_files=null;

    public void exe(HzCmd hzCmd) {
        try {
            ClusterType clusterType = ClusterType.valueOf(type);
            ClusterSize clusterSize = new ClusterSize(size);

            String[] versionsList = null;
            if(versions!=null){
                versionsList = versions.split(",");
            }

            hzCmd.initCluster(user, boxes, id, clusterType, clusterSize, ee, versionsList, lib_files, cwd_file);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
