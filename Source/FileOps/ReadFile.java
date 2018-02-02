package FileOps;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import Given.InputConstantsAPI;
import ILP.FuncPt;
import ILP.TrafficNodes;
import edu.asu.emit.qyan.alg.model.Path;

public class ReadFile {
	
	//read file with core-throughput details
//	public static  List<FuncPt> readFnPt(String filename){
	public static  List<FuncPt> readFnPt(InputStream fileStream){
		List<FuncPt> fdet = new ArrayList<FuncPt>();
		int lineNum = 0;
		BufferedReader br = null;		
		String line_data;
		boolean read_vars = false;
	
				try{
//					br = new BufferedReader(new FileReader(filename));
					br = new BufferedReader(new InputStreamReader(fileStream));
					//skip the comments in the file
					do{
						line_data = br.readLine();
						lineNum++;						
					}while(line_data != null
							&& !line_data.contains(InputConstantsAPI.START_OF_FILE_DELIMETER));
					//Error while reading file
					if (line_data == null) {
						System.err.println("ERROR: Incorrect file syntax at line number:"
										+ lineNum);
						
					}
					//Check that the end of file has not reached        	
					while((line_data=br.readLine()) != null && !line_data.contains(InputConstantsAPI.END_OF_FILE_DELIMETER)){
						read_vars = true;							
						if(read_vars){
						    //value separated from variable
							String[] parts = line_data.split("\t");	
							System.out.println(parts[0] + "," + parts[1]);							;							
							//add the TrafficNodes object to the list
							fdet.add(new FuncPt(Integer.valueOf(parts[0]),Double.valueOf(parts[1])));
						}
					}		
					//close the buffered writer from writing the file
					br.close();				
				}catch(Exception exp){
					System.err.println("Error in reading the file");				
				}
		System.out.println("Function details have been read");
		return fdet;
		
	}
	
	
	//read the given s-d pairs and add the traffic flows to them
//	public static List<TrafficNodes> readSDPairs(Map<TrafficNodes,List<Path>> sdpaths, String filePath){
	public static List<TrafficNodes> readSDPairs(Map<TrafficNodes,List<Path>> sdpaths, InputStream fileStream){
		ArrayList<TrafficNodes> sd_traffic = new ArrayList<TrafficNodes>();
		int lineNum = 0;
		BufferedReader br = null;		
		String line_data;
		boolean read_vars = false;
	
				try{
//					br = new BufferedReader(new FileReader(InputConstants.FILE_READ_PATH + InputConstants.SD_PAIRS ));	
//					br = new BufferedReader(new FileReader(filePath));	
					br = new BufferedReader(new InputStreamReader(fileStream));	
					//skip the comments in the file
					do{
						line_data = br.readLine();
						lineNum++;						
					}while(line_data != null
							&& !line_data.contains(InputConstantsAPI.START_OF_FILE_DELIMETER));
					//Error while reading file
					if (line_data == null) {
						System.err.println("ERROR: Incorrect file syntax at line number:"
										+ lineNum);
						
					}
					//Check that the end of file has not reached        	
					while((line_data=br.readLine()) != null && !line_data.contains(InputConstantsAPI.END_OF_FILE_DELIMETER)){
						read_vars = true;							
						if(read_vars){
						    //value separated from variable
							String[] parts = line_data.split("\t");	
							int src_index = Integer.valueOf(parts[0]);							
							int dest_index = Integer.valueOf(parts[1]);						
							TrafficNodes temp = new TrafficNodes();
							for( Map.Entry<TrafficNodes,List<Path>> entry : sdpaths.entrySet()){
								if(entry.getKey().v1.get_id()==src_index && entry.getKey().v2.get_id()==dest_index){
//									System.out.println("Source vertex " + entry.getKey().v1.get_id());
//									System.out.println("Destination vertex " + entry.getKey().v2.get_id());
									temp = entry.getKey();
								}
							}						
							//add the TrafficNodes object to the list
							sd_traffic.add(temp);
						}
					}		
					//close the buffered writer from writing the file
					br.close();				
				}catch(Exception exp){
					System.err.println("Error in reading the file");				
				}
		System.out.println("Source Destination pairs have been read");
		return sd_traffic;
	}
	
	
	public static void readTraffic(List<TrafficNodes> pair_list){
		
		int lineNum = 0;
		BufferedReader br = null;		
		String line_data;
		boolean read_vars = false;
	
				try{
					br = new BufferedReader(new FileReader(InputConstantsAPI.FILE_READ_PATH + InputConstantsAPI.TRAFFIC_FILE ));					
					//skip the comments in the file
					do{
						line_data = br.readLine();
						lineNum++;
						System.out.println(lineNum + " : " + line_data);
					}while(line_data != null
							&& !line_data.contains(InputConstantsAPI.START_OF_FILE_DELIMETER));
					//Error while reading file
					if (line_data == null) {
						System.err.println("ERROR: Incorrect file syntax at line number:"
										+ lineNum);
						
					}
					//Check that the end of file has not reached        	
					while((line_data=br.readLine()) != null && !line_data.contains(InputConstantsAPI.END_OF_FILE_DELIMETER)){
						read_vars = true;							
						if(read_vars){
						    //value separated from variable
							String[] parts = line_data.split("\t");		
							int src_index = Integer.valueOf(parts[0]);
							int dest_index = Integer.valueOf(parts[1]);
							System.out.println(src_index + "," + dest_index);
							for(TrafficNodes pr : pair_list){
								if(pr.v1.get_id() == src_index && pr.v2.get_id() == dest_index){
									pr.flow_traffic = Integer.valueOf(parts[2]);
								}
							}
							System.out.println("source : " + parts[0] + " destination : " + parts[1] + " traffic : " + parts[2] + " , " + parts[3] + " , " + parts[4]);							
						}
					}		
					//close the buffered writer from writing the file
					br.close();				
				}catch(Exception exp){
					System.out.println("Error in reading the file");				
				}
		System.out.println("Traffic File has been read");
//		return pair_list;
	}
	
	
	
	public static void outputVariables(){		
		
		int lineNum = 0;
		BufferedReader br = null;		
		String line_data;
		boolean read_vars = false;			
				try{
					br = new BufferedReader(new FileReader(InputConstantsAPI.FILE_READ_PATH + InputConstantsAPI.OUTPUT_VAR +  ".txt"));
					System.out.println(InputConstantsAPI.FILE_READ_PATH + InputConstantsAPI.OUTPUT_VAR +  ".txt");
					//skip the comments in the file
					do{
						line_data = br.readLine();
						lineNum++;
						System.out.println(lineNum + " : " + line_data);
					}while(line_data != null
							&& !line_data.contains(InputConstantsAPI.START_OF_FILE_DELIMETER));
					//Error while reading file
					if (line_data == null) {
						System.err.println("ERROR: Incorrect file syntax at line number:"
										+ lineNum);
						
					}
					//Check that the end of file has not reached        	
					while((line_data=br.readLine()) != null && !line_data.contains(InputConstantsAPI.END_OF_FILE_DELIMETER)){
						read_vars = true;							
						if(read_vars){
						    //value separated from variable
							String[] parts = line_data.split("\\s+");
							String[] gvar = parts[0].split("_");		
							System.out.println("func_id : " + gvar[1] + ", " + gvar[2] + ", " + gvar[3]);							
						}
					}		
					//close the buffered writer from writing the file
					br.close();				
				}catch(Exception exp){
					System.out.println("Error in reading the file");				
				}		
	}
	
				
}
