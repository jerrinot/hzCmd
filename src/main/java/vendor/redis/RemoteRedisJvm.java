package vendor.redis;

import global.NodeType;
import local.Box;
import local.ClusterManager;
import local.RemoteJvm;

import java.io.IOException;
import java.util.List;


public class RemoteRedisJvm extends RemoteJvm {

    public RemoteRedisJvm(Box box, NodeType type, String id) throws IOException, InterruptedException {
        super(box, type, id);
    }

    public String getClassToRun() {
        if (isMember()){
            return RedisMember.class.getName();
        }
        return RedisClient.class.getName();
    }

    public void beforeJvmStart(ClusterManager myCluster) throws Exception {

        if(isMember()) {
            box.upload("config-redis/redis.conf", dir + "/");
        }else {

        }
    }

    @Override
    public String setJvmStartOptions(Box thisBox, ClusterManager myCluster) throws Exception {

       return null;
    }

}