package gem;

import global.NodeType;
import local.Box;
import local.Installer;
import local.JvmFactory;
import local.RemoteJvm;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by danny on 21/01/2016.
 */
public class GemJvmFactory implements JvmFactory, Serializable {

    private static final String ggPath = Installer.REMOTE_HZCMD_ROOT_FULL_PATH+"/"+"gemfire-lib";

    public String getVendorLibDir(String version) {
        return ggPath+"/"+version;
    }

    public List<String> getVendorLibNames(String version, boolean ee) {
        List<String> jars = new ArrayList();

        //8.2.0
        jars.add("gemfire-"+version+".jar");
        jars.add("log4j-core-2.1.jar");
        jars.add("log4j-api-2.1.jar");

        return jars;
    }

    public RemoteJvm createJvm(Box box, NodeType type, int count, String clusterId) throws IOException, InterruptedException {

        String id;
        if ( type == NodeType.Member ){
            id = GemMember.class.getSimpleName();
        }else {
            id = GemClient.class.getSimpleName();
        }
        id += count+""+clusterId;

        return new RemoteGemJvm(box, type, id);
    }
}