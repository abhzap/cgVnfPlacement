package Given;

import java.util.Arrays;
import java.util.List;

public class InputConstants {	
	public static final int Hour  = 1;
	public static final int k_paths = 1;//atleast 15 paths to satisfy the connections
	public static final String NETWORK_FILE_NAME = "3nodenet.txt";//"nsf_14_network.txt";//"3nodenet.txt";//"nsfnet.txt";
	public static final String FUNCTION_DETAILS = "function_core_throughput.txt";
	public static final String SD_PAIRS =  "3nodenetSD.txt";//"sdpairs.txt";
	public static final String TRAFFIC_FILE = "traffic.txt";
	public static final String OUTPUT_VAR = "output_var";
	public static final String ILP_FILE_NAME = "3node_dc_all_nfv";//"14node_dc_all_nfv";//"14node_dc_all_nfv","14node_dc_1_nfv"
	public static final int NO_OF_LAMBDA = 1;
	public static final int BANDWIDTH_PER_LAMBDA = 40000;//40 Gbps
	public static final double POWER_CONSUMED_PER_Mbps = .011;//in Watts
	public static final int NFV_NODE_CORE_COUNT = 48000 ;//1,2,4,8,12,16,20,24,48
	public static final List<Integer> CORE_NUM_LIST = Arrays.asList(2000,4000,8000,12000,24000,48000,96000,192000);//2000,4000,8000,12000,24000,48000,96000,192000
	public static final List<Integer> FUNC_REQ = Arrays.asList(3,7,6,2,4);//5 function chain Arrays.asList(3,7,6,2,4);// 4 function chain Arrays.asList(1,5,2,8)
	public static final List<Integer> TRAF_INT = Arrays.asList(2,5,10,15,20);
	public static final int Big_M = 9999999;
	public static final String FILE_READ_PATH = "C:/Users/abgupta/Box Sync/Luna - Eclipse/MultiHourVNF/Data/" ;
	public static final String ILP_WRITE_PATH = "C:/Users/abgupta/Box Sync/Luna - Eclipse/MultiHourVNF/";	
	public static final String START_OF_FILE_DELIMETER = "START OF FILE";
	public static final String END_OF_FILE_DELIMETER = "END OF FILE";
	public static final String START_OF_NODES_DELIMETER = "START OF NODE";
	public static final String START_OF_LINKS_DELIMETER = "START OF LINK";
}
