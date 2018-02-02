package Given;

import java.util.Arrays;
import java.util.List;

public class InputConstantsAPI {
	public static final int Hour  = 1;
	public static final int k_paths = 80;//atleast 15 paths to satisfy the connections
	//"DC_NFV_ALL";//"DC_NFV_4";//"DC_Only";
	public static final String SC_STRATEGY = "DC_NFV_ALL";
	//"internet2.txt"; //"geant.txt";//"nsf_14_network.txt";
	public static final String NETWORK_FILE_NAME = "geant.txt";//"internet2.txt";//"3nodenet.txt";//"nsf_14_network.txt";//"geant.txt";
	//NSF14 - Arrays.asList(1,2,3,4,5,6,7,8,9,10,11,12,13,14); //Internet 2 - Arrays.asList(1,2,3,4,5,6,7,8,9,10,11,12,13,14,15); //Geant - Arrays.asList(1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18);
	public static final List<Integer> DC_SET = Arrays.asList(1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18);
	//NSF14 - Arrays.asList(3,5,8,10); //Internet2 - Arrays.asList(4,8,10,7); //Geant - Arrays.asList(6,7,9,12);
	public static final List<Integer> NFV_SET = Arrays.asList(6,7,9,12);
	//"sdpairsInternet2TM1.txt"; "sdpairsInternet2TM2.txt"; "sdpairsInternet2TM3.txt"; "sdpairsGeantTM1.txt"; "sdpairsGeantTM2.txt"; "sdpairsGeantTM3.txt";
	public static final String SD_PAIRS = "sdpairsGeantTM3.txt";//"sdpairsNSF14.txt";//"3nodenetSD.txt";//
	//NSF - 2; //I2TM1 - 2; I2TM2 - 10; I2TM3 - 9; //GTM1 - 14; GTM2 - 11; GTM3 - 2 
	public static final int HQ_Node = 2;
	//memory - Arrays.asList(2,15,25);//Arrays.asList(1,2,5,10,15,20,25,30,35,45,50,55,60,65,70,75,80,85,90,95,100);//Arrays.asList(2,5,10,15,20);//Arrays.asList(20,25,30,35,40,45,50);
	//TM1 - Arrays.asList(25,29,30,34,35,40,45,46,50,51,55,57,60,63,65,69,70,75)
	//TM2,TM3 - Arrays.asList(25,26,30,31,35,39,40,44,45,48,50,53,55,57,60,61,65,70,75)
	//Load traffics
		//geant - TM1 - Arrays.asList(2,5,10,30,45,55)
	    //geant - TM2,TM3 - Arrays.asList(2,5,10,30,40,50) 
	public static final List<Integer> TRAF_INT = Arrays.asList(10,20,40);
//	public static final List<Integer> MEMORY_NUM_LIST = Arrays.asList(1000,2000,4000,8000);
	//"function_memory_throughput.txt";//"function_core_throughput.txt";
	public static final String FUNCTION_DETAILS = "function_core_throughput.txt";
	
	//Input parameter for setting Optimization gap and optimization time
	public static final double MIP_GAP = .05;//set it at 5%
	public static final double TIME_LIMIT = 3600;//given in seconds (1 hour)	
	
		
	public static final String TRAFFIC_FILE = "traffic.txt";
	public static final String OUTPUT_VAR = "output_var";
//	public static final String ILP_FILE_NAME = "14node_dc_all_nfv";//"14node_dc_all_nfv","14node_dc_1_nfv"
	public static final int NO_OF_LAMBDA = 1;
	public static final int BANDWIDTH_PER_LAMBDA = 40000;//40 Gbps
	public static final double POWER_CONSUMED_PER_Mbps = .011;//in Watts
	public static final int NFV_NODE_CORE_COUNT = 192000;//1,2,4,8,12,16,20,24,48
	public static final List<Integer> CORE_NUM_LIST = Arrays.asList(8000,48000,192000);//Arrays.asList(2000,4000,8000,12000,24000,48000,96000,192000);//2000,4000,8000,12000,24000,48000,96000,192000
	public static final List<Integer> FUNC_REQ = Arrays.asList(3,7,6,2,4);//5 function chain Arrays.asList(3,7,6,2,4);// 4 function chain Arrays.asList(1,5,2,8)	
	public static final int Big_M = 9999999;	
//	public static final String FILE_READ_PATH = "C:/Users/abgupta/Box Sync/Luna - Eclipse/MultiHourVNF/Data/" ;
	public static final String FILE_READ_PATH = "Data/";
	public static final String ILP_WRITE_PATH = "C:/Users/abgupta/Box Sync/Luna - Eclipse/MultiHourVNF/";	
	public static final String START_OF_FILE_DELIMETER = "START OF FILE";
	public static final String END_OF_FILE_DELIMETER = "END OF FILE";
	public static final String START_OF_NODES_DELIMETER = "START OF NODE";
	public static final String START_OF_LINKS_DELIMETER = "START OF LINK";
}
