package ILP;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import FileOps.ReadFile;
import Given.InputConstants;
import edu.asu.emit.qyan.alg.control.YenTopKShortestPathsAlg;
import edu.asu.emit.qyan.alg.model.Graph;
import edu.asu.emit.qyan.alg.model.Path;
import edu.asu.emit.qyan.alg.model.abstracts.BaseVertex;

public class ILPGenerator {
	
	private BufferedWriter writer ;
	
	    //create the desired file
		protected void createFileWriter(String fileName) throws Exception{
			BufferedWriter out = null;
			try {
			    out = new BufferedWriter(new FileWriter(fileName));
			} catch (IOException exp) {
				throw exp;
			} catch(Exception exp){
				throw exp;
			}
			this.writer = out;
		}
		
		//write to the file
		protected void writeToFile(String line) throws Exception{
			try{
				//System.out.println("Writing to file:"+line);
				this.writer.write("\n"+line);
			}catch (IOException exp){
				throw exp;
			} catch(Exception exp) {
				throw exp;
			}
		}
		
		//close the file
		protected void closeFile() throws Exception{
			try {
				this.writer.close();
			}catch(Exception exp){
				throw exp;
			}
		}
		
		
		
	
	
	public void generateLPFile(Graph g, List<TrafficNodes> pair_list, HashMap<TrafficNodes,List<Path>> sdpaths,int dc_node,ArrayList<Integer> nfv_list,List<FuncPt> func_list,int top_k,int traf_int){
//	public void generateLPFile(Graph g, List<TrafficNodes> pair_list, HashMap<TrafficNodes,List<Path>> sdpaths,int dc_node,ArrayList<Integer> nfv_list,List<FuncPt> func_list,int top_k,int traf_int,int cr_count){	
		
		//store the list of source vertices
		List<BaseVertex> source_vertice = new ArrayList<BaseVertex>();
		//store the list of destination vertices
		List<BaseVertex> destination_vertice = new ArrayList<BaseVertex>();
		
		//filling up the source_vertice and destination_vertice lists
		for(TrafficNodes tn_entr : pair_list){
			if( ! source_vertice.contains(tn_entr.v1) || source_vertice == null ){
				source_vertice.add(tn_entr.v1);
			}
			if( ! destination_vertice.contains(tn_entr.v2) || destination_vertice == null ){
				destination_vertice.add(tn_entr.v2);
			}
		}
		
		try{		
			// Constructs a StringBuilder with an initial value of "" (an empty string) and a capacity of 16.
		    StringBuffer buf = new StringBuffer();
			//NumOfDCs, Hrs, TotalVMCount, DCcapacity, VMsPerRack , VMsPerServer 
//		    this.createFileWriter(InputConstants.ILP_WRITE_PATH + InputConstants.ILP_FILE_NAME + "_" + dc_node + "DC_13_node_NFV_" + InputConstants.FUNC_REQ.size() + "_" + traf_int + "_" + cr_count + ".lp");
		    this.createFileWriter(InputConstants.ILP_WRITE_PATH + InputConstants.ILP_FILE_NAME + "_" + dc_node + "DC_13_node_NFV_" + InputConstants.FUNC_REQ.size() + "_" + traf_int + "_" + InputConstants.NFV_NODE_CORE_COUNT + ".lp");
		    
		    
		    
		    
		    
			  //**********OBJECTIVE FUNCTION*********************
			  this.writeToFile("minimize");
			  System.out.println("\n\n**********************Generating Objective**************\n\n");	
			  //clear the buffer
			  buf.delete(0, buf.length());			 
//			  System.out.println("in loop");
			  //iterate through the SD pairs
			  for(TrafficNodes pr : pair_list){
				  System.out.println(" in traffic nodes");
				  //from path "number 1" to "total number of paths"
				  for(int p=1; p <= top_k; p++){
					  System.out.println(" in traffic paths");
					  buf.append(sdpaths.get(pr).get(p-1).get_weight()*pr.flow_traffic);
					  buf.append(" ");	 
					  buf.append("R_" + pr.v1.get_id() + "_" + pr.v2.get_id() + "_" + p);						
					  buf.append(" + ");
				  }
				  buf.append("\n");
			  }			 
//			  System.out.println(" out of loops");
			  //Delete the last + sign before writing the rest 
			  if(buf.length() >= 3)
				  buf.delete(buf.length()-3, buf.length());
			  // write the equation to the file
	 		  if(buf.length() > 0)		 
	 			  this.writeToFile(buf.substring(0));
//	 		  System.out.println("written strings to file");
	 		  //clear buffer
	 		  buf.delete(0, buf.length()); 			 		
	 		  System.out.println("The objective function has been generated");
	 		  
	 		  
	 		 //*******CONSTRAINTS******
	 		 this.writeToFile("\n\n");
	 		 this.writeToFile("subject to");
	 		 
	 		 //****************SINGLE FLOW CONSTRAINT*********************//	 		
			  for(TrafficNodes pr : pair_list){
				  //from path "number 1" to "total number of paths"
				  for(int p=1; p <= top_k; p++){
					  buf.append("R_" + pr.v1.get_id() + "_" + pr.v2.get_id() + "_" + p);
//						  System.out.println("R_" + pr.v1.get_id() + "_" + pr.v2.get_id() + "_" + hr + "_" + p);
					  buf.append(" + ");						 
				  }		
				  //Delete the last + sign before writing the rest 
				  if(buf.length() >= 3)
					  buf.delete(buf.length()-3, buf.length());
				  buf.append(" = 1 ");
				  //write the buffer to file
 				  this.writeToFile(buf.substring(0));
 				  //clear the buffer for next equation
 				  buf.delete(0, buf.length());	
			  }			
	 		 System.out.println("Demand Constraint");
	 		 
	 		 
	 		 
//	 		int edge_count = 0;
	 		//****************CAPACITY CONSTRAINT*****************//	 		
		 	//checks whether given edge in path	
		 	boolean edge_in_path = false;
	 		 for(BaseVertex start_vrt : g._vertex_list){
	 			Set<BaseVertex> adj_end_vrt = g.get_adjacent_vertices(start_vrt);
	 		        //given an edge in the graph
		 			for(BaseVertex end_vrt : adj_end_vrt){
		 				boolean edge_used  = false;
//			 				edge_count++;
//			 				int pair_count = 0;		 				
				 			//for each pair of vertices
				 			for(TrafficNodes pr : pair_list){
//					 				  pair_count++;
								  //consider the paths that pass through that link
								  //from path "number 1" to "total number of paths"
								  for(int p=1; p <= top_k; p++){							  
									  edge_in_path = false;
									  //print out the path being checked
//										  System.out.println(edge_count + " : " + start_vrt.get_id() + " -> " + end_vrt.get_id());
//										  System.out.println(pair_count + " : " + " ( " + pr.v1.get_id() + "," + pr.v2.get_id() + " ) " + sdpaths.get(pr).get(p-1));
										  if(sdpaths.get(pr).get(p-1).get_vertices().contains(start_vrt)&&
											 sdpaths.get(pr).get(p-1).get_vertices().contains(end_vrt)){											  
											  int end_vrt_index = sdpaths.get(pr).get(p-1).get_vertices().indexOf(end_vrt);
											  int start_vrt_index = sdpaths.get(pr).get(p-1).get_vertices().indexOf(start_vrt);
													 if(end_vrt_index == start_vrt_index + 1){
														 edge_in_path = true;
														 edge_used = true;
														 //if edge is part of the path
//															 System.out.println("*****All passed********"); 
													 }
										  }	
										  
										  if(edge_in_path){
											  buf.append(pr.flow_traffic);
											  buf.append(" ");	
											  buf.append("R_" + pr.v1.get_id() + "_" + pr.v2.get_id() + "_" + p);										  					
										      buf.append(" + ");  
										  }				  
									  										  
								  }									  
													  
							  }
					 		  if(edge_used){
					 			  //Delete the last + sign before writing the rest 
								  if(buf.length() >= 3)
									  buf.delete(buf.length()-3, buf.length());	
								  buf.append(" <= ");
								  buf.append(InputConstants.BANDWIDTH_PER_LAMBDA);							  
								  //write the buffer to file
				 				  this.writeToFile(buf.substring(0));
				 				  //clear the buffer for next equation
				 				  buf.delete(0, buf.length());	
					 		  } 
						  }
				 		 
	 			}	 		 
		 	 System.out.println("Capacity constraint");
	 		
	 		 
	 		 //**********************FUNCTION LOCATION AT A NODE************************//		 	 
	 		 for(BaseVertex vrt_loc : g._vertex_list){
	 			    if(vrt_loc.get_type().equalsIgnoreCase("dc")){
	 			    	//This constraint is not required in the case of DCs
//	 			    	for(TrafficNodes  pr : pair_list){
//			 				for(int f : InputConstants.FUNC_REQ){			 					
//			 					buf.append("L_" + f + "_" + vrt_loc.get_id() + "_" + pr.v1.get_id() + "_" + pr.v2.get_id());
//			 					buf.append(" + ");					
//			 				}		 				
//			 				//Delete the last + sign before writing the rest 
//							if(buf.length() >= 3)
//								buf.delete(buf.length()-3, buf.length());	
//							buf.append(" <= ");
//	 						buf.append(InputConstants.FUNC_REQ.size());
//	 						//write the buffer to file
//			 				this.writeToFile(buf.substring(0));
//			 				//clear the buffer for next equation
//			 				buf.delete(0, buf.length());
//	 			    	}					
	 			    }else if(vrt_loc.get_type().equalsIgnoreCase("nfv")){
	 			    	for(TrafficNodes  pr : pair_list){
			 				for(int f : InputConstants.FUNC_REQ){
			 					double core_count = 0.0;
			 					for(FuncPt fpt : func_list){
			 						if(fpt.getid() == f){
			 							core_count = fpt.getcore();
			 						}
			 					}
			 					buf.append(pr.flow_traffic*core_count);
			 					buf.append(" ");
			 					buf.append("L_" + f + "_" + vrt_loc.get_id() + "_" + pr.v1.get_id() + "_" + pr.v2.get_id());
			 					buf.append(" + ");					
			 				}		 				
	 			    	}
	 			    	//Delete the last + sign before writing the rest 
						if(buf.length() >= 3)
							buf.delete(buf.length()-3, buf.length());	
						buf.append(" <= ");
// 						buf.append(cr_count);
						buf.append(InputConstants.NFV_NODE_CORE_COUNT);
 						//write the buffer to file
		 				this.writeToFile(buf.substring(0));
		 				//clear the buffer for next equation
		 				buf.delete(0, buf.length());
	 			    	
	 			    	
	 			    	
		 			   /*for(TrafficNodes  pr : pair_list){
			 				for(int f : InputConstants.FUNC_REQ){			 					
			 					buf.append("L_" + f + "_" + vrt_loc.get_id() + "_" + pr.v1.get_id() + "_" + pr.v2.get_id());
			 					buf.append(" + ");					
			 				}		 				
		 			    }
	 			    	//Delete the last + sign before writing the rest 
						if(buf.length() >= 3)
							buf.delete(buf.length()-3, buf.length());	
						buf.append(" <= ");
						buf.append(6);
						//write the buffer to file
		 				this.writeToFile(buf.substring(0));
		 				//clear the buffer for next equation
		 				buf.delete(0, buf.length());*/
	 			    	
			 		}else{
//			 			for(TrafficNodes  pr : pair_list){
//			 				for(int f : InputConstants.FUNC_REQ){
//			 					buf.append("L_" + f + "_" + vrt_loc.get_id() + "_" + pr.v1.get_id() + "_" + pr.v2.get_id());
//			 			    	buf.append(" = 0");		
//			 			    	//write the buffer to file
//				 				this.writeToFile(buf.substring(0));
//				 				//clear the buffer for next equation
//				 				buf.delete(0, buf.length());	
//			 				}
//			 			}
		 			} 					
 					
 			}	  		 
	 		System.out.println("Function Location");
	 		 		
	 		 
	 		 //********************DEPENDENCIES IN SERVICE CHAIN*******************//
	 		 //Set Q-(5) includes (12), (13), (14)
			for(int f : InputConstants.FUNC_REQ){					
				boolean in_path = false;
		 			for(TrafficNodes pr : pair_list){		
		 				  //from path "number 1" to "total number of paths"
						  for(int p=1; p <= top_k; p++){
							  for(BaseVertex vrt_loc : g._vertex_list){
								  in_path = false;
								  //check whether 'v' is part of path p
								  if(sdpaths.get(pr).get(p-1).get_vertices().contains(vrt_loc)){
									  in_path = true;									  							  
								  }	
								  if(in_path){
									 if(vrt_loc.get_type().equalsIgnoreCase("dc")||vrt_loc.get_type().equalsIgnoreCase("nfv")){ 
										  buf.append("Q_" + f + "_" + vrt_loc.get_id() + "_" + p + "_" + pr.v1.get_id() + "_" + pr.v2.get_id());
										  buf.append(" - ");
										  buf.append("L_" + f + "_" + vrt_loc.get_id() + "_" + pr.v1.get_id() + "_" + pr.v2.get_id());
										  buf.append(" <= 0");
										  //write the buffer to file
							 			  this.writeToFile(buf.substring(0));
							 			  //clear the buffer for next equation
							 			  buf.delete(0, buf.length());
							 			  
										  buf.append("Q_" + f + "_" + vrt_loc.get_id() + "_" + p + "_" + pr.v1.get_id() + "_" + pr.v2.get_id());
										  buf.append(" - ");
										  buf.append("R_" + pr.v1.get_id() + "_" + pr.v2.get_id() + "_" + p);
										  buf.append(" <= 0");
										  //write the buffer to file
							 			  this.writeToFile(buf.substring(0));
							 			  //clear the buffer for next equation
							 			  buf.delete(0, buf.length());
							 			  
										  buf.append("Q_" + f + "_" + vrt_loc.get_id() + "_" + p + "_" + pr.v1.get_id() + "_" + pr.v2.get_id());	
										  buf.append(" - ");
										  buf.append("L_" + f + "_" + vrt_loc.get_id() + "_" + pr.v1.get_id() + "_" + pr.v2.get_id());
										  buf.append(" - ");
										  buf.append("R_" + pr.v1.get_id() + "_" + pr.v2.get_id() + "_" + p);
										  buf.append(" >= ");
										  buf.append("- 1");									  
										  //write the buffer to file
							 			  this.writeToFile(buf.substring(0));
							 			  //clear the buffer for next equation
							 			  buf.delete(0, buf.length());
									 }										  
								  }
																	  
					 		}
						  }
		 			}
		 			
			}
	 	    System.out.println("Location of function on path p between s-d");
	 	    
	 	    
	 	    
	 		//Constraint Q (one function atleast once across all paths)-(6)		
			for(int f : InputConstants.FUNC_REQ){
		 			for(TrafficNodes pr : pair_list){
						  //from path "number 1" to "total number of paths"
						  for(int p=1; p <= top_k; p++){
							  //for all the nodes on that path
							  for(BaseVertex vrt_loc : sdpaths.get(pr).get(p-1).get_vertices()){
								  if(vrt_loc.get_type().equalsIgnoreCase("dc")||vrt_loc.get_type().equalsIgnoreCase("nfv")){
									  buf.append("Q_" + f + "_" + vrt_loc.get_id() + "_" + p + "_" + pr.v1.get_id() + "_" + pr.v2.get_id());	
									  buf.append(" + ");
								  }
							  }								 
						  }
						  //Delete the last + sign before writing the rest 
						  if(buf.length() >= 3)
							buf.delete(buf.length()-3, buf.length());				 			 
			 			  buf.append(" >= 1");
			 			  //write the buffer to file
			 			  this.writeToFile(buf.substring(0));
			 			  //clear the buffer for next equation
			 			  buf.delete(0, buf.length());								  
		 			}	
			}		 	
	 		System.out.println("Function located in atleast one path between s-d");
	 		
	 		
	 		
			//Set J	- (7), (8), (15), (16), (17) 		
//	 		boolean nodes_in_order = false;
			for(int f : InputConstants.FUNC_REQ.subList(0,InputConstants.FUNC_REQ.size()-1)){												
				int nxt_function = InputConstants.FUNC_REQ.get(InputConstants.FUNC_REQ.indexOf(f)+1);
		 			for(TrafficNodes pr : pair_list){
						  //from path "number 1" to "total number of paths"
						  for(int p=1; p <= top_k; p++){								 
							  for(BaseVertex vrtx : sdpaths.get(pr).get(p-1).get_vertices() ){									  
								      // the node is a DC node then these set of constraints
									  if(vrtx.get_type().equalsIgnoreCase("dc")){	
//											  buf.append("J_" + f + "_" + nxt_function + "_" + p + "_" + pr.v1.get_id() + "_" + pr.v2.get_id() + "_" + vrtx.get_id() + "_" + vrtx.get_id());
//											  buf.append(" - ");
//											  buf.append("Q_" + f + "_" + vrtx.get_id() + "_" + p + "_" + pr.v1.get_id() + "_" + pr.v2.get_id()); 
//											  buf.append(" <= 0");
//											  //write the buffer to file
//									 		  this.writeToFile(buf.substring(0));
//									 	      //clear the buffer for next equation
//									 		  buf.delete(0, buf.length());
								 		  
											  buf.append("J_" + f + "_" + nxt_function + "_" + p + "_" + pr.v1.get_id() + "_" + pr.v2.get_id() + "_" + vrtx.get_id() + "_" + vrtx.get_id());
											  buf.append(" - ");
											  buf.append("Q_" + f + "_" + vrtx.get_id()+ "_" + p + "_" + pr.v1.get_id() + "_" + pr.v2.get_id()); 
											  buf.append(" >= 0");
											  //write the buffer to file
									 		  this.writeToFile(buf.substring(0));
									 	      //clear the buffer for next equation
									 		  buf.delete(0, buf.length());
									 		  continue;
									  }  
									  // the node is a NFV capable node
									  if(vrtx.get_type().equalsIgnoreCase("nfv")){
										for(BaseVertex nxt_vrtx : sdpaths.get(pr).get(p-1).get_vertices()){
											 if(nxt_vrtx.get_type().equalsIgnoreCase("dc")||nxt_vrtx.get_type().equalsIgnoreCase("nfv")){
												 //check for cases
												 // next_vertex is equal to vertex
												 // next_vertex is greater than vertex
												 if(  sdpaths.get(pr).get(p-1).get_vertices().indexOf(nxt_vrtx)  >=  sdpaths.get(pr).get(p-1).get_vertices().indexOf(vrtx)  ){
													 buf.append("J_" + f + "_" + nxt_function + "_" + p + "_" + pr.v1.get_id() + "_" + pr.v2.get_id() + "_" + vrtx.get_id() + "_" + nxt_vrtx.get_id());
													 buf.append(" - ");
													 buf.append("Q_" + f + "_" + vrtx.get_id() + "_" + p + "_" + pr.v1.get_id() + "_" + pr.v2.get_id()); 
													 buf.append(" <= 0");
													 //write the buffer to file
											 		 this.writeToFile(buf.substring(0));
											 		 //clear the buffer for next equation
											 		 buf.delete(0, buf.length());
													 
											 		 buf.append("J_" + f + "_" + nxt_function + "_" + p + "_" + pr.v1.get_id() + "_" + pr.v2.get_id() + "_" + vrtx.get_id() + "_" + nxt_vrtx.get_id());
													 buf.append(" - ");
													 buf.append("Q_" + nxt_function + "_" + nxt_vrtx.get_id() + "_" + p + "_" + pr.v1.get_id() + "_" + pr.v2.get_id());	
													 buf.append(" <= 0");
													 //write the buffer to file
											 		 this.writeToFile(buf.substring(0));
											 		 //clear the buffer for next equation
											 		 buf.delete(0, buf.length());				 
													 
													 
													 
													//for each pair of vertices this has to be checked
//														nodes_in_order = false;
													buf.append("J_" + f + "_" + nxt_function + "_" + p + "_" + pr.v1.get_id() + "_" + pr.v2.get_id() + "_" + vrtx.get_id() + "_" + nxt_vrtx.get_id());
												    buf.append(" - ");
												    buf.append("Q_" + f + "_" + vrtx.get_id() + "_" + p + "_" + pr.v1.get_id() + "_" + pr.v2.get_id());	
													buf.append(" - ");
													buf.append("Q_" + nxt_function + "_" + nxt_vrtx.get_id() + "_" + p + "_" + pr.v1.get_id() + "_" + pr.v2.get_id());	
													buf.append(" >= ");
												    buf.append("- 1 ");														
												    //write the buffer to file
										 			this.writeToFile(buf.substring(0));
										 			//clear the buffer for next equation
										 			buf.delete(0, buf.length());
												 }
											 }
									  }
									}
								 
								 
							  }
							  
						  }							  
					 }			 		 
			}
	 		System.out.println("Both functions in the path between s-d");
	 		
	 		
	 		
	 		//Constraint J (atleast one path has the service-chain implemented)-(9)		
			for(int f : InputConstants.FUNC_REQ.subList(0,InputConstants.FUNC_REQ.size()-1)){
				int nxt_function = InputConstants.FUNC_REQ.get(InputConstants.FUNC_REQ.indexOf(f)+1);
		 			for(TrafficNodes pr : pair_list){
						  //from path "number 1" to "total number of paths"
						  for(int p=1; p <= top_k; p++){
							  for(BaseVertex vrtx : sdpaths.get(pr).get(p-1).get_vertices() ){									  
							      // the node is a DC node then these set of constraints
								  if(vrtx.get_type().equalsIgnoreCase("dc")){												 
										  buf.append("J_" + f + "_" + nxt_function + "_" + p + "_" + pr.v1.get_id() + "_" + pr.v2.get_id() + "_" + vrtx.get_id() + "_" + vrtx.get_id());
										  buf.append(" + ");
								  }  
								  // the node is a NFV capable node
								  if(vrtx.get_type().equalsIgnoreCase("nfv")){
									for(BaseVertex nxt_vrtx : sdpaths.get(pr).get(p-1).get_vertices()){
										 if(nxt_vrtx.get_type().equalsIgnoreCase("dc")||nxt_vrtx.get_type().equalsIgnoreCase("nfv")){
											//check for cases
											 // next_vertex is equal to vertex
											 // next_vertex is greater than vertex
											 if(  sdpaths.get(pr).get(p-1).get_vertices().indexOf(nxt_vrtx)  >=  sdpaths.get(pr).get(p-1).get_vertices().indexOf(vrtx) ){
												 buf.append("J_" + f + "_" + nxt_function + "_" + p + "_" + pr.v1.get_id() + "_" + pr.v2.get_id() + "_" + vrtx.get_id() + "_" + nxt_vrtx.get_id());
												 buf.append(" + ");
											 }
										 }
								  }
								} 
							 
							  }								  
						  }
						  //Delete the last + sign before writing the rest 
						  if(buf.length() >= 3)
							buf.delete(buf.length()-3, buf.length());							  
						  buf.append(" >= 1");
						  //write the buffer to file
				 		  this.writeToFile(buf.substring(0));
				 		  //clear the buffer for next equation
				 		  buf.delete(0, buf.length());	
					 }
			}
	 		System.out.println("Functions are in the right order in atleast one path");
	 		
//******* Check for NFV / DC constraint later*******
	 		//Constraint J (for location of function chains in order in a path) - (10)
	 		for(TrafficNodes pr : pair_list ){
	 			for(int p=1; p <= top_k; p++){
	 				int path_length = sdpaths.get(pr).get(p-1).get_vertices().size();
	 				for(int f : InputConstants.FUNC_REQ.subList(1,InputConstants.FUNC_REQ.size()-1)){
	 					int func_prev = InputConstants.FUNC_REQ.get(InputConstants.FUNC_REQ.indexOf(f)-1);
	 					int func_next = InputConstants.FUNC_REQ.get(InputConstants.FUNC_REQ.indexOf(f)+1);
	 					for(BaseVertex vrt2 : sdpaths.get(pr).get(p-1).get_vertices()){
	 						//only for cases where "u" is a NFV node
	 						//if it is a DC, all the previous and preceding dependencies will be satisfied at the DC
	 						if(vrt2.get_type().equalsIgnoreCase("nfv")){	
		 						int index_vrt2 = sdpaths.get(pr).get(p-1).get_vertices().indexOf(vrt2);	 						
		 							for(BaseVertex vrt3 : sdpaths.get(pr).get(p-1).get_vertices().subList(index_vrt2,path_length) ){	 								
		 									for(BaseVertex vrt1 : sdpaths.get(pr).get(p-1).get_vertices().subList(0,index_vrt2+1)){	
		 										// if vrt1 is a DC we would assume all the succeeding dependencies are resolved at the DC
		 										//relation will only hold for DC nodes
		 										if(vrt1.get_type().equalsIgnoreCase("nfv")){
		 													buf.append("J_" + func_prev  + "_" + f + "_" + p + "_" + pr.v1.get_id() + "_" + pr.v2.get_id() + "_" + vrt1.get_id() + "_" + vrt2.get_id());
			 												buf.append(" + ");
		 										}		
		 									}
		 									//Delete the last + sign before writing the rest 
		 									if(buf.length() >= 3)
		 										buf.delete(buf.length()-3, buf.length());
		 									buf.append(" - ");
											buf.append("J_" + f + "_" + func_next + "_" + p + "_" + pr.v1.get_id() + "_" + pr.v2.get_id() + "_" + vrt2.get_id() + "_" + vrt3.get_id());
											buf.append(" >= 0 ");
											//write the buffer to file
											this.writeToFile(buf.substring(0));
											//clear the buffer for next equation
											buf.delete(0, buf.length());	
		 							}
	 					   }
	 					}
	 				}
	 				
	 			}
	 		}
	 		System.out.println("New constraint generated");
	 		

	 		//Constraint J (in a single path which chain has to be selected) - (11)
	 		for(int f : InputConstants.FUNC_REQ.subList(0,InputConstants.FUNC_REQ.size()-1)){
	 			int nxt_function = InputConstants.FUNC_REQ.get(InputConstants.FUNC_REQ.indexOf(f)+1);
			 			for(TrafficNodes pr : pair_list){
							  //from path "number 1" to "total number of paths"
							  for(int p=1; p <= top_k; p++){
								  boolean var_generated = false;
								  for(BaseVertex vrtx : sdpaths.get(pr).get(p-1).get_vertices() ){									  
								      // the node is a DC node then these set of constraints
									  if(vrtx.get_type().equalsIgnoreCase("dc")){												 
//											  buf.append("J_" + f + "_" + nxt_function + "_" + p + "_" + pr.v1.get_id() + "_" + pr.v2.get_id() + "_" + vrtx.get_id() + "_" + vrtx.get_id());
										      buf.append("J_s" + pr.v1.get_id() + "_d" + pr.v2.get_id() + "_p" + p + "_sNode"    + vrtx.get_id() + "_tNode" + vrtx.get_id() + "_sVNF" + f + "_tVNF" + nxt_function  );
											  buf.append(" + ");
											  var_generated = true;
									  }  
									  // the node is a NFV capable node
									  if(vrtx.get_type().equalsIgnoreCase("nfv")){
										for(BaseVertex nxt_vrtx : sdpaths.get(pr).get(p-1).get_vertices()){
											 if(nxt_vrtx.get_type().equalsIgnoreCase("dc")||nxt_vrtx.get_type().equalsIgnoreCase("nfv")){
												//check for cases
												 // next_vertex is equal to vertex
												 // next_vertex is greater than vertex
												 if(  sdpaths.get(pr).get(p-1).get_vertices().indexOf(nxt_vrtx)  >=  sdpaths.get(pr).get(p-1).get_vertices().indexOf(vrtx) ){
//													 buf.append("J_" + f + "_" + nxt_function + "_" + p + "_" + pr.v1.get_id() + "_" + pr.v2.get_id() + "_" + vrtx.get_id() + "_" + nxt_vrtx.get_id());
												     buf.append("J_s" + pr.v1.get_id() + "_d" + pr.v2.get_id() + "_p" + p + "_sNode"    + vrtx.get_id() + "_tNode" + nxt_vrtx.get_id() + "_sVNF" + f + "_tVNF" + nxt_function  );
													 buf.append(" + ");
													 var_generated = true;
												 }
											 }
									  }
									} 
								 
								  }	
								//Delete the last + sign before writing the rest 
								  if(var_generated == true){
								  if(buf.length() >= 3)
									buf.delete(buf.length()-3, buf.length());							  
								  buf.append(" <= 1");
								  //write the buffer to file
						 		  this.writeToFile(buf.substring(0));
						 		  //clear the buffer for next equation
						 		  buf.delete(0, buf.length());
								  }
							  }
								
							  
						 }
 				}
	 		System.out.println("Single path which chain has to be selected");
	 		
	  		
	 		 
	 		/* //Integer variables 		
	  		this.writeToFile("\n\n" + "general");*/
	  		
	  	    
	  		//Binary variables 		
	 		this.writeToFile("\n\n" + "binary");  
	 		//R	 		
			for(TrafficNodes pr : pair_list){
					  //from path "number 1" to "total number of paths"
					  for(int p=1; p <= top_k; p++){
						  buf.append("R_" + pr.v1.get_id() + "_" + pr.v2.get_id() + "_" + p);
						  //write the buffer to file
				 		  this.writeToFile(buf.substring(0));
				 		  //clear the buffer for next equation
				 		  buf.delete(0, buf.length());	
					  }
			}	 		
	 		//L
	 		for(int f : InputConstants.FUNC_REQ){	 			
			 		 for(BaseVertex vrt_loc : g._vertex_list){
			 			for(TrafficNodes  pr : pair_list){
			 			  buf.append("L_" + f + "_" + vrt_loc.get_id() + "_" + pr.v1.get_id() + "_" + pr.v2.get_id());
			 			  //write the buffer to file
				 		  this.writeToFile(buf.substring(0));
				 		  //clear the buffer for next equation
				 		  buf.delete(0, buf.length());	
			 			}
			 		 }
 			}
	 		
	 		//Q
	 		for(int f : InputConstants.FUNC_REQ){
			 			for(TrafficNodes pr : pair_list){
							  //from path "number 1" to "total number of paths"
							  for(int p=1; p <= top_k; p++){
								  for(BaseVertex vrt_loc : g._vertex_list){
									  boolean in_path = false;
									  //check whether 'v' is part of path p
									  if(sdpaths.get(pr).get(p-1).get_vertices().contains(vrt_loc)){
										  in_path = true;									  							  
									  }	
									  if(in_path){
										  if(vrt_loc.get_type().equalsIgnoreCase("dc")||vrt_loc.get_type().equalsIgnoreCase("nfv")){ 
											  buf.append("Q_" + f + "_" + vrt_loc.get_id() + "_" + p + "_" + pr.v1.get_id() + "_" + pr.v2.get_id());
											  //write the buffer to file
									 		  this.writeToFile(buf.substring(0));
									 		  //clear the buffer for next equation
									 		  buf.delete(0, buf.length());
										  }
									  }
								  }						 		  
							  }
			 			}
 			}	  		
	 		//J	 		
			for(int f : InputConstants.FUNC_REQ.subList(0,InputConstants.FUNC_REQ.size()-1)){	
				int nxt_function = InputConstants.FUNC_REQ.get(InputConstants.FUNC_REQ.indexOf(f)+1);
		 			for(TrafficNodes pr : pair_list){
						  //from path "number 1" to "total number of paths"
						  for(int p=1; p <= top_k; p++){
							  for(BaseVertex vrtx : sdpaths.get(pr).get(p-1).get_vertices() ){									  
							      // the node is a DC node then these set of constraints
								  if(vrtx.get_type().equalsIgnoreCase("dc")){												 
										  buf.append("J_" + f + "_" + nxt_function + "_" + p + "_" + pr.v1.get_id() + "_" + pr.v2.get_id() + "_" + vrtx.get_id() + "_" + vrtx.get_id());	
										  //write the buffer to file
								 		  this.writeToFile(buf.substring(0));
								 		  //clear the buffer for next equation
								 		  buf.delete(0, buf.length());	
								  }  
								  // the node is a NFV capable node
								  if(vrtx.get_type().equalsIgnoreCase("nfv")){
									for(BaseVertex nxt_vrtx : sdpaths.get(pr).get(p-1).get_vertices()){
										 if(nxt_vrtx.get_type().equalsIgnoreCase("dc")||nxt_vrtx.get_type().equalsIgnoreCase("nfv")){
											 //check if target vertex is equal to source vertex
											 if(  sdpaths.get(pr).get(p-1).get_vertices().indexOf(nxt_vrtx)  >=  sdpaths.get(pr).get(p-1).get_vertices().indexOf(vrtx) ){
												 buf.append("J_" + f + "_" + nxt_function + "_" + p + "_" + pr.v1.get_id() + "_" + pr.v2.get_id() + "_" + vrtx.get_id() + "_" + nxt_vrtx.get_id());
												 //write the buffer to file
										 		 this.writeToFile(buf.substring(0));
										 		 //clear the buffer for next equation
										 		 buf.delete(0, buf.length());	
											 }
										 }
								  }
								} 
							 
							  }								  
						  }							  
						  
					 }
			} 		
	 		
	 		//finish the ILP file
	 		this.writeToFile("\n\n" + "end");
	 		
	 		System.out.println("#####The ILP file has been generated!!#####");
	 		//flush the file
	 		//close the file
	 		this.closeFile();
	 		
	        			
		}catch(Exception exp){			
			System.err.println("Error in Creating/Writing the ILP file");
		}
		finally{
			try{
			//close the file	
            this.closeFile();
			}
			catch(Exception exp){
				System.out.println("Error in Closing the ILP file");
			}
		}		
	}	
	
//	public static void main(String args[]){ 
//		
//		//build graph object from given network file
//		Graph g = new Graph(InputConstants.FILE_READ_PATH + InputConstants.NETWORK_FILE_NAME);		
//		//print out the edges
//		for(BaseVertex s_vert : g._vertex_list){
//			for(BaseVertex t_vert : g.get_adjacent_vertices(s_vert)){
//				System.out.println( s_vert.get_id() + "->" + t_vert.get_id());
//			}
//		}
//		//k shortest paths
//		int top_k = InputConstants.k_paths;
//		//k shortest path objects
//		YenTopKShortestPathsAlg kpaths = new YenTopKShortestPathsAlg(g);			
//		//Store paths for each s-d pair
//		HashMap<TrafficNodes,List<Path>> sdpaths = new HashMap<TrafficNodes,List<Path>>();
//		for(BaseVertex source_vert : g._vertex_list){
//			for(BaseVertex target_vert : g._vertex_list){
//				if(source_vert != target_vert){
//				List<Path> path_temp = new ArrayList<Path>(kpaths.get_shortest_paths(source_vert,target_vert, top_k));					
//				//create the sd-pair for that pair of nodes
//				TrafficNodes sd_temp = new TrafficNodes(source_vert, target_vert);
//				//add to list of paths depending on s-d pair
//				sdpaths.put(sd_temp, path_temp);
//				}
//			}
//		}
//		
//		//print out the graph
//		/*int sd_count = 0;
//		for( Map.Entry<TrafficNodes,List<Path>> entry : sdpaths.entrySet()){
//		sd_count++;
//		System.out.println(sd_count + " s-d pair : ( " +  entry.getKey().v1.get_id() + " , " + entry.getKey().v2.get_id() + " )");
//	    System.out.println("paths : " + entry.getValue());
//	    }*/
//		
//		
//		//allocate traffic to the given SD pairs
//		//SD pairs between which we desire traffic to be
//		//Store each s-d pair
//		List<TrafficNodes> pair_list = new ArrayList<TrafficNodes>() ;	
//		pair_list = ReadFile.readSDPairs(sdpaths);		
//	
//		
//		
//		//read the function point details
//		List<FuncPt> func_list = new ArrayList<FuncPt>();
//		func_list = ReadFile.readFnPt(InputConstants.FILE_READ_PATH + InputConstants.FUNCTION_DETAILS);
//		/*for(FuncPt fpt : func_list){
//			System.out.println(fpt.getid() + " -- " + fpt.getcore());
//		}*/
//		
//		
//		//check if the pair in the list is the key for the graph
//		/*for(TrafficNodes tmp_pr : pair_list){
//			for( Map.Entry<TrafficNodes,List<Path>> entry : sdpaths.entrySet()){				
//				 if(tmp_pr == entry.getKey()){
//					 System.out.println(entry.getValue());
//				 }
//			}
//		}	*/
//		
//		
//		//DC node
//		int dc_node = 0;
//		ArrayList<Integer> nfv_list =  new ArrayList<Integer>();
//		for(BaseVertex tmp_vrt : g._vertex_list ){			
//			System.out.println("Vertex ID : " + tmp_vrt.get_id() + " , " + tmp_vrt.get_type());
//			if(tmp_vrt.get_type().equalsIgnoreCase("dc")){
//				dc_node = tmp_vrt.get_id();				
//			}
//			if(tmp_vrt.get_type().equalsIgnoreCase("nfv")){
//				nfv_list.add(tmp_vrt.get_id());
//			}
//		}
//		
//		
//		int total_flow = 0;
//		// ILP Generator object
//		ILPGenerator ilpgen = new ILPGenerator();
//		//assign num of cores to NFV capable nodes
////		for(int core_count: InputConstants.CORE_NUM_LIST){
//			//assign traffic to sdpairs and generate ILP
//			for(int traf_prcnt : InputConstants.TRAF_INT){
//				total_flow = 0;
//				int br_flow = traf_prcnt*InputConstants.BANDWIDTH_PER_LAMBDA/100;
//				int hq_flow = traf_prcnt*InputConstants.BANDWIDTH_PER_LAMBDA*3/200;
//				for(TrafficNodes tmp : pair_list){
//					//check for hq id //which is 2 now
//					if( tmp.v1.get_id()==2 || tmp.v2.get_id()==2 ){
//						tmp.flow_traffic = hq_flow;
//						total_flow += hq_flow;
//					}
//					else{
//						tmp.flow_traffic = br_flow;
//						total_flow += br_flow;						
//					}					
//				}
//				System.out.println("Percentage: " + traf_prcnt + " Total Flow: " + total_flow);
//				
////				ilpgen.generateLPFile(g,pair_list,sdpaths,dc_node,nfv_list,func_list,top_k,traf_prcnt,core_count);	
//				ilpgen.generateLPFile(g,pair_list,sdpaths,dc_node,nfv_list,func_list,top_k,traf_prcnt);
//				//generate ILP file
//				System.out.println("ILP file generated");
//			}				
////		}
//		
//		
//		//print out the traffic details
//		/*for(TrafficNodes tmp : pair_list){
//			System.out.println(tmp.v1.get_id() + " , " + tmp.v2.get_id() + " , " + tmp.flow_traffic);
//		}*/
//		
//		
//		//generate the traffic between source_node pairs
////		ilpgen.generateTraffic_1(pair_list);
////		ilpgen.generateTraffic(pair_list);	
//		//write the generated traffic matrix between the source-destination pairs
////		WriteFile.TrafficDetails(pair_list);
//		//read the traffic details	
////		ReadFile.readTraffic(pair_list);
//		//check the read traffic values
//		/*for(int i=0; i<=0; i++){
//			for(TrafficNodes pr : pair_list){				
//				System.out.println("Originating node " + pr.v1.get_id() + " ; " + pr.flow_traffic.get(i));				
//			}
//		}*/		
//	 	
//		   
//		//print out the traffic between all traffic nodes
//		/*for(int i=0; i<=2; i++){
//			for(TrafficNodes pr : pair_list){				
//					System.out.println("Originating node " + pr.v1.get_id() + " ; " + pr.flow_traffic.get(i));				
//			}
//		}*/
//	       	
//				
//		//printing out the k-shortest paths between the source-destination pairs
//		/*for(Map.Entry<TrafficNodes,List<Path>> entry : sdpaths.entrySet()){
//			System.out.println("s-d pair : ( " +  entry.getKey().v1.get_id() + " , " + entry.getKey().v2.get_id() + " )");
//		    System.out.println("paths : " + entry.getValue().get(0) + " ; " +entry.getValue().get(1));
//		}
//		*/
//		/*System.out.println(g._BaseVertex_num);
//		System.out.println(g._edge_num);*/
//		//Read the output variable file
//        //ReadFile.outputVariables();	
//		//calculate network resources consumed by hard-wired network middle-boxes
//		//input  : the locations of the middle-boxes
//			    
//			 	
//			
//	}

}
