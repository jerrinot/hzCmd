package cmdline;

import com.github.rvesse.airline.annotations.Arguments;
import main.HzCmd;

@com.github.rvesse.airline.annotations.Command(name = "type", description = "type of benchmark")
public class BenchTypes extends Command {

    @Arguments(description = "e.g. METRICS HDR")
    public String types;


    public void exe(HzCmd hzCmd) {
        try {
            hzCmd.setBenchType(types);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
