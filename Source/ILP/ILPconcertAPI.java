package ILP;

import ilog.concert.IloColumn;
import ilog.concert.IloException;
import ilog.concert.IloIntVar;
import ilog.concert.IloObjective;
import ilog.concert.IloRange;
import ilog.cplex.IloCplex;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import FileOps.ReadFile;
import Given.InputConstantsAPI;
import edu.asu.emit.qyan.alg.control.YenTopKShortestPathsAlg;
import edu.asu.emit.qyan.alg.model.Graph;
import edu.asu.emit.qyan.alg.model.Path;
import edu.asu.emit.qyan.alg.model.abstracts.BaseVertex;

public class ILPconcertAPI {
	
	public double makeLPModel(Graph g, List<TrafficNodes> pairList, Map<TrafficNodes,List<Path>> sdpaths, ArrayList<BaseVertex> nfvList, 
			ArrayList<BaseVertex> nfvDCList, List<FuncPt> funcList, String ilpInstanceDetails, Writer wrt, Writer wrtInf, 
			double traf_prcnt, double total_flow, int dcNode, int core_count){
	
		//the return value
		double rValue = 0.0;	
		
	try{	
		//creating the CPLEX object
		IloCplex ilpObject = new IloCplex();
		//declare the objective of the ILP - (1)
		IloObjective minBandwidth =  ilpObject.addMinimize();
		
		//############## CONSTRAINTS #################
		//Path selection constraint - (2)
		ArrayList<IloRange> pathSelection = new ArrayList<IloRange>();
		//iterate through the SD pairs
		for(int sdPair = 0; sdPair < pairList.size(); sdPair++){
			pathSelection.add(ilpObject.addRange(1.0, 1.0));
		}
		//Bandwidth constraint - (3)
		int linkNumber = 0;
		ArrayList<IloRange> bandwidthConstraint = new ArrayList<IloRange>();
		//iterate through the links
		for(BaseVertex srcVertex : g._vertex_list){
			for(BaseVertex destVertex : g.get_adjacent_vertices(srcVertex)){
				//add the bandwidth constraint
				bandwidthConstraint.add(linkNumber,ilpObject.addRange(-Double.MAX_VALUE, InputConstantsAPI.BANDWIDTH_PER_LAMBDA));
				//increment the link number
				linkNumber++;
			}
		}
		//Core capacity constraint - (4)
		ArrayList<IloRange> coreCapacity = new ArrayList<IloRange>();
		//iterate through the list of NFV Capable Nodes //not DC, since it has unlimited compute capacity
		for(int nodeIndex = 0; nodeIndex < nfvList.size(); nodeIndex++){
			coreCapacity.add(ilpObject.addRange(-Double.MAX_VALUE, core_count));
		}
		//Memory capacity constraint - (5)
//		ArrayList<IloRange> memCapacity = new ArrayList<IloRange>();
//		//iterate through the list of NFV Capable Nodes //not DC, since it has unlimited compute capacity
//		for(int nodeIndex = 0; nodeIndex < nfvList.size(); nodeIndex++){
//			memCapacity.add(ilpObject.addRange(-Double.MAX_VALUE, mem_count));
//		}
		//VNF is located on path constraint - (5)
			//VNF location in path - (12)
		    Map<IlpCstr5,IloRange> vnfLocationInPath = new HashMap<IlpCstr5,IloRange>();			    
		    //Path has VNF location - (13)
		    Map<IlpCstr5,IloRange> pathHasVNFLocation = new HashMap<IlpCstr5,IloRange>();
		    //Both VNF and Path align, Variable Q is set - (14)
		    Map<IlpCstr5,IloRange> varQIsSet = new HashMap<IlpCstr5,IloRange>();			  
	    //iterate through the list of SD pairs
	    for(TrafficNodes tn : pairList){
	    	List<Path> routeListSDpair = sdpaths.get(tn);
	    	//iterate through the list of paths
	    	for(Path p : routeListSDpair){
	    		//iterate through the list of vertices through the path
	    		for(BaseVertex vrt : p.get_vertices()){
	    			//check if NFV-capable or DC
	    			if(nfvDCList.contains(vrt)){
	    				//iterate through the VNFs in the service chain
	    				for(int funcID : InputConstantsAPI.FUNC_REQ){
	    					IlpCstr5 temp = new IlpCstr5(tn,p,vrt,funcID);
	    					//adding new constraint of type (12)
	    					vnfLocationInPath.put(temp, ilpObject.addRange(-Double.MAX_VALUE, 0.0));
	    					//adding new constraint of type (13)
	    					pathHasVNFLocation.put(temp, ilpObject.addRange(-Double.MAX_VALUE, 0.0));
	    					//adding new constraint of type (14)
	    					varQIsSet.put(temp, ilpObject.addRange(-1.0, Double.MAX_VALUE));
	    				}
	    			}
	    		}
	    	}
	    }
	    //check the number of constraints 
//	    System.out.println("Constraint 12 size : " + vnfLocationInPath.values().size());
//	    System.out.println("Constraint 13 size : " + pathHasVNFLocation.values().size());
//	    System.out.println("Constraint 14 size : " + varQIsSet.values().size());
	    
		//Only one variable Q can be selected - (6)
	    Map<IlpCstr6,IloRange> varQSelection = new HashMap<IlpCstr6,IloRange>();
		//iterate through the list of SD pairs
	    for(TrafficNodes tn : pairList){    			    			
			//iterate through the VNFs in the service chain
			for(int funcID : InputConstantsAPI.FUNC_REQ){
				IlpCstr6 temp = new IlpCstr6(tn,funcID);
				//add the constraint
				varQSelection.put(temp, ilpObject.addRange(1.0, Double.MAX_VALUE));
			}    		
	    }
		//If VNF located in a DC - (7)
	    Map<IlpCstr7,IloRange> vnfLocatedInDC = new HashMap<IlpCstr7,IloRange>();
	    //iterate through the list of SD pairs
	    for(TrafficNodes tn : pairList){
	    	List<Path> routeListSDpair = sdpaths.get(tn);
	    	//iterate through the list of paths
	    	for(Path p : routeListSDpair){
	    		//iterate through the list of vertices through the path
	    		for(BaseVertex vrt : p.get_vertices()){
	    			//check if node is a DC
	    			if(vrt.get_type().equalsIgnoreCase("dc")){
	    				//iterate through the VNFs in the service chain
	    				for(int index = 0 ; index < InputConstantsAPI.FUNC_REQ.size()-1; index++){
	    					int funcID = InputConstantsAPI.FUNC_REQ.get(index);
	    					int nxtFuncID = InputConstantsAPI.FUNC_REQ.get(index+1);
	    					IlpCstr7 temp = new IlpCstr7(tn,p,vrt,funcID,nxtFuncID);
	    					//add the constraint
	    					vnfLocatedInDC.put(temp, ilpObject.addRange(0.0, Double.MAX_VALUE));
//	    					vnfLocatedInDC.put(temp, ilpObject.addRange(0.0, 0.0));
	    				}
	    			}
	    		}
	    	}
	    }
		//If 2 VNFs are on the same path - (8)
			//first VNF is on path - (15)
	    	Map<IlpCstr8,IloRange> firstVNFOnPath = new HashMap<IlpCstr8,IloRange>();
		    //second VNF is on path - (16)
	    	Map<IlpCstr8,IloRange> secondVNFOnPath = new HashMap<IlpCstr8,IloRange>();
		    //When second VNF is on the same path as first VNF, Variable J is set - (17)
	    	Map<IlpCstr8,IloRange> varJIsSet = new HashMap<IlpCstr8,IloRange>();
		//iterate through the SD pairs
		for(TrafficNodes tn : pairList){
	    	List<Path> routeListSDpair = sdpaths.get(tn);
	    	//iterate through the list of paths
	    	for(Path p : routeListSDpair){
	    		//iterate through the list of vertices through the path
	    		for(BaseVertex vrt : p.get_vertices()){
	    			//check if node is a NFV-capable
	    			if(vrt.get_type().equalsIgnoreCase("nfv")){
	    				//index of the vertex
	    				int indexVrtU = p.get_vertices().indexOf(vrt);
	    				//iterating through the successive nodes in path
	    				for(BaseVertex nxtVrt : p.get_vertices().subList(indexVrtU, p.get_vertices().size())){
	    					 //check if the next vertex is a DC or a NFV-capable node
							 if(nfvDCList.contains(nxtVrt)){
			    				//iterate through the VNFs in the service chain
			    				for(int index = 0 ; index < InputConstantsAPI.FUNC_REQ.size()-1; index++){
			    					int funcID = InputConstantsAPI.FUNC_REQ.get(index);
			    					int nxtFuncID = InputConstantsAPI.FUNC_REQ.get(index+1);
			    					IlpCstr8 temp = new IlpCstr8(tn,p,vrt,nxtVrt,funcID,nxtFuncID);
			    					//adding new constraint of type (15)
			    					firstVNFOnPath.put(temp, ilpObject.addRange(-Double.MAX_VALUE,0.0));
			    					//adding new constraint of type (16)
			    					secondVNFOnPath.put(temp, ilpObject.addRange(-Double.MAX_VALUE,0.0));
			    					//adding new constraint of type (17)
			    					varJIsSet.put(temp, ilpObject.addRange(-1.0,Double.MAX_VALUE));
			    				}
							 }
	    				}
	    			}
	    		}
	    	}
		}
		//Only one J can be selected - (9)
	    Map<IlpCstr9, IloRange> varJSelection = new HashMap<IlpCstr9, IloRange>();	
	    //iterate through the SD pairs
		for(TrafficNodes tn : pairList){
			//iterate through the VNFs in the service chain
			for(int index = 0 ; index < InputConstantsAPI.FUNC_REQ.size()-1; index++){
				int funcID = InputConstantsAPI.FUNC_REQ.get(index);
				int nxtFuncID = InputConstantsAPI.FUNC_REQ.get(index+1);
				IlpCstr9 temp = new IlpCstr9(tn, funcID, nxtFuncID);				    					
				varJSelection.put(temp, ilpObject.addRange(1.0, Double.MAX_VALUE));
			}
		}
	    //Enforce service chaining inside a node - (10)
		Map<IlpCstr10, IloRange> scInSameNode = new  HashMap<IlpCstr10, IloRange>();
	    //iterate through the SD pairs
		for(TrafficNodes tn : pairList){
	    	List<Path> routeListSDpair = sdpaths.get(tn);
	    	//iterate through the list of paths
	    	for(Path p : routeListSDpair){
	    		//iterate through the list of vertices through the path
	    		for(BaseVertex vrt : p.get_vertices()){
	    			//check if node is a NFV-capable
	    			if(vrt.get_type().equalsIgnoreCase("nfv")){
	    				//index of the vertex
	    				int indexVrtU = p.get_vertices().indexOf(vrt);
	    				//iterating through the successive nodes in path
	    				for(BaseVertex nxtVrt : p.get_vertices().subList(indexVrtU, p.get_vertices().size())){
	    					 //check if the next vertex is a DC or a NFV-capable node
							 if(nfvDCList.contains(nxtVrt)){
			    				//iterate through the VNFs in the service chain
			    				for(int index = 0 ; index < InputConstantsAPI.FUNC_REQ.size()-2; index++){
			    					int funcID = InputConstantsAPI.FUNC_REQ.get(index);
			    					int nxtFuncID = InputConstantsAPI.FUNC_REQ.get(index+1);
			    					int nxtNxtFuncID = InputConstantsAPI.FUNC_REQ.get(index+2);
			    					IlpCstr10 temp = new IlpCstr10(tn, p, vrt, nxtVrt, funcID, nxtFuncID, nxtNxtFuncID);
			    					//add the constraint
			    					scInSameNode.put(temp, ilpObject.addRange(0.0, Double.MAX_VALUE));
			    				}
							 }
	    				}
	    			}
	    		}
	    	}
		}
	    //Dependency path Selection - (11)
		Map<IlpCstr11, IloRange> dependencyPathSelection = new HashMap<IlpCstr11, IloRange>();
	    //iterate through the SD pairs
		for(TrafficNodes tn : pairList){
	    	List<Path> routeListSDpair = sdpaths.get(tn);
	    	//iterate through the list of paths
	    	for(Path p : routeListSDpair){
	    		//iterate through the VNFs in the service chain
				for(int index = 0 ; index < InputConstantsAPI.FUNC_REQ.size()-1; index++){
					int funcID = InputConstantsAPI.FUNC_REQ.get(index);
					int nxtFuncID = InputConstantsAPI.FUNC_REQ.get(index+1);
					IlpCstr11 temp = new IlpCstr11(tn, p, funcID, nxtFuncID);
					//add the constraint
					dependencyPathSelection.put(temp, ilpObject.addRange(-Double.MAX_VALUE, 1.0));
				}
	    	}
		}
		
		
		
		
		
		
		
		
		
		
		
		
		//############## VARIABLES ###############
		//######## R variable ########			
		Map<IlpVarR, IloColumn> varR = new HashMap<IlpVarR, IloColumn>();
		//####Objective-(1)
		for(int sdPair = 0; sdPair < pairList.size(); sdPair++){
			TrafficNodes tn = pairList.get(sdPair);
			int pathIndex = 0;
			for(Path p : sdpaths.get(tn)){
				//path length //L^p_(s,d)
				int pathLength = p.get_vertices().size()-1;
				//create the new R object
				IlpVarR temp = new IlpVarR(tn,p,pathIndex);
				//check if object exists in map
				IloColumn col = varR.get(temp);
				if(col!=null){
					//add the coefficient of the variable to the corresponding column
					col = col.and(ilpObject.column(minBandwidth, pathLength*tn.flow_traffic));
					//keep track of the column corresponding to the variable
					varR.put(temp, col);
				}else{
					//add the coefficient of the variable to the corresponding column
					col = ilpObject.column(minBandwidth, pathLength*tn.flow_traffic);
					//keep track of the column corresponding to the variable
					varR.put(temp, col);
				}
				//increment the path Index
				pathIndex++;
			}
		}
		//####Path selection constraint - (2)
		//iterate through the SD pairs
		for(int sdPair = 0; sdPair < pairList.size(); sdPair++){
			TrafficNodes tn = pairList.get(sdPair);
			int pathIndex = 0;			
			for(Path p : sdpaths.get(tn)){
				//create the new R object
				IlpVarR temp = new IlpVarR(tn,p,pathIndex);
				//check if object exists in map
				IloColumn col = varR.get(temp);
				if(col!=null){
					//add the coefficient of the variable to the corresponding column
					col = col.and(ilpObject.column(pathSelection.get(sdPair), 1.0));					
					//keep track of the column corresponding to the variable
					varR.put(temp, col);
				}else{
					//add the coefficient of the variable to the corresponding column
					col = ilpObject.column(pathSelection.get(sdPair), 1.0);
					//keep track of the column corresponding to the variable
					varR.put(temp, col);
				}
				//increment the path index
				pathIndex++;
			}
		}
		//#####Bandwidth constraint - (3)
		linkNumber = 0;//reset the link counter			
		//iterate through the links
		for(BaseVertex srcVertex : g._vertex_list){
			for(BaseVertex destVertex : g.get_adjacent_vertices(srcVertex)){
				//iterate through the SD pairs
				for(int sdPair = 0; sdPair < pairList.size(); sdPair++){
					TrafficNodes tn = pairList.get(sdPair);
					int pathIndex = 0;
					for(Path p : sdpaths.get(tn)){
						//check if both the source and target vertices of the link are in the path
						if( p.get_vertices().contains(srcVertex) && p.get_vertices().contains(destVertex) ){
							int indexSrcVrt = p.get_vertices().indexOf(srcVertex);
							int indexDestVrt = p.get_vertices().indexOf(destVertex);							
							//check if the link is part of the path
							if(indexDestVrt == indexSrcVrt + 1){
								//create the new R object
								IlpVarR temp = new IlpVarR(tn,p,pathIndex);
								//check if object exists in map
								IloColumn col = varR.get(temp);
								if(col!=null){
									//add the coefficient of the variable to the corresponding column
									col = col.and(ilpObject.column(bandwidthConstraint.get(linkNumber), tn.flow_traffic));
									//keep track of the column corresponding to the variable
									varR.put(temp, col);
								}else{
									//add the coefficient of the variable to the corresponding column
									col = ilpObject.column(bandwidthConstraint.get(linkNumber), tn.flow_traffic);
									//keep track of the column corresponding to the variable
									varR.put(temp, col);
								}
							}
						}
						//increment the path index
						pathIndex++;
					}
				}
				//increment the link number
				linkNumber++;
			}
		}
		//#####VNF is located on path constraint - (5)
		//iterate through the list of SD pairs
	    for(TrafficNodes tn : pairList){
	    	List<Path> routeListSDpair = sdpaths.get(tn);
	    	int pathIndex = 0;
	    	//iterate through the list of paths
	    	for(Path p : routeListSDpair){
	    		//iterate through the list of vertices through the path
	    		for(BaseVertex vrt : p.get_vertices()){
	    			//check if NFV-capable or DC
	    			if(nfvDCList.contains(vrt)){
	    				//iterate through the VNFs in the service chain
	    				for(int funcID : InputConstantsAPI.FUNC_REQ){		    					  				
	    					//create the new R object
							IlpVarR tempVar = new IlpVarR(tn,p,pathIndex);
	    					//check if object exists in map
							IloColumn col = varR.get(tempVar);
							//constraint object
							IlpCstr5 tempCstr = new IlpCstr5(tn,p,vrt,funcID);	 
							if(col!=null){
								//adding new constraint of type (13)
								col = col.and(ilpObject.column(pathHasVNFLocation.get(tempCstr), -1.0));
								//adding new constraint of type (14)
								col = col.and(ilpObject.column(varQIsSet.get(tempCstr), -1.0));
								//keep track of the column corresponding to the variable
								varR.put(tempVar, col);
							}else{
								//adding new constraint of type (13)
								col = ilpObject.column(pathHasVNFLocation.get(tempCstr), -1.0);
								//adding new constraint of type (14)
								col = col.and(ilpObject.column(varQIsSet.get(tempCstr), -1.0));
								//keep track of the column corresponding to the variable
								varR.put(tempVar, col);
							}
	    				}
	    			}
	    		}
	    		pathIndex++;
	    	}
	    }
	    //add the variables to the master problem
	    Map<IlpVarR, IloIntVar> setVarR= new HashMap<IlpVarR, IloIntVar>();
	    //add the columns to the variables
	    for(Map.Entry<IlpVarR, IloColumn> entry : varR.entrySet()){
	    	setVarR.put(entry.getKey(), ilpObject.intVar(entry.getValue(), 0, 1, entry.getKey().toString()));
	    }
	    
	    
	    //####### L variable #######
	    Map<IlpVarL, IloColumn> varL = new HashMap<IlpVarL, IloColumn>();
	    //Core capacity constraint - (4)		
		//iterate through the list of NFV Capable Nodes
		for(int nodeIndex = 0; nodeIndex < nfvList.size(); nodeIndex++){
			BaseVertex nfvNode = nfvList.get(nodeIndex);
			//iterate through the list of SD pairs
			for(TrafficNodes tn : pairList){
				//iterate through the VNFs in the service chain
				for(int funcID : InputConstantsAPI.FUNC_REQ){
					//create the new L object
					IlpVarL tempVar = new IlpVarL(tn,nfvNode,funcID);
					//If the vertex is a DC node
//					if(nfvNode.get_type().equalsIgnoreCase("dc")){
//    					//check if object exists in map
//						IloColumn col = varL.get(tempVar);
//						if(col!=null){							
//							col = col.and(ilpObject.column(coreCapacity.get(nodeIndex),1.0));
//							//keep track of the column corresponding to the variable
//							varL.put(tempVar, col);
//						}else{							
//							col = ilpObject.column(coreCapacity.get(nodeIndex),1.0);
//							//keep track of the column corresponding to the variable
//							varL.put(tempVar, col);
//						}	
//					}
					//If the vertex is a NFV-capable node
					if(nfvNode.get_type().equalsIgnoreCase("nfv")){
						double coreCount = 0;
    					for(FuncPt fpt : funcList){
	 						if(fpt.getid() == funcID){
	 							coreCount = fpt.getcore();
	 						}
	 					}    					
    					//check if object exists in map
						IloColumn col = varL.get(tempVar);
						if(col!=null){							
							col = col.and(ilpObject.column(coreCapacity.get(nodeIndex), tn.flow_traffic*coreCount));
							//keep track of the column corresponding to the variable
							varL.put(tempVar, col);
						}else{							
							col = ilpObject.column(coreCapacity.get(nodeIndex), tn.flow_traffic*coreCount);
							//keep track of the column corresponding to the variable
							varL.put(tempVar, col);
						}
					}
					
				}
			}				
		}	
	    //Memory capacity constraint (Non Scalable) - (5)		
		//iterate through the list of NFV Capable Nodes
//		for(int nodeIndex = 0; nodeIndex < nfvList.size(); nodeIndex++){
//			BaseVertex nfvNode = nfvList.get(nodeIndex);
//			//iterate through the list of SD pairs
//			for(TrafficNodes tn : pairList){
//				//iterate through the VNFs in the service chain
//				for(int funcID : InputConstantsAPI.FUNC_REQ){
//					double memCount = 0;
//					for(FuncPt fpt : funcList){
// 						if(fpt.getid() == funcID){
// 							memCount = fpt.getcore();
// 						}
// 					}   
//					//create the new L object
//					IlpVarL tempVar = new IlpVarL(tn,nfvNode,funcID);
//					//If the vertex is a DC node
////					if(nfvNode.get_type().equalsIgnoreCase("dc")){
////    					//check if object exists in map
////						IloColumn col = varL.get(tempVar);
////						if(col!=null){							
////							col = col.and(ilpObject.column(coreCapacity.get(nodeIndex),1.0));
////							//keep track of the column corresponding to the variable
////							varL.put(tempVar, col);
////						}else{							
////							col = ilpObject.column(coreCapacity.get(nodeIndex),1.0);
////							//keep track of the column corresponding to the variable
////							varL.put(tempVar, col);
////						}	
////					}
//					//If the vertex is a NFV-capable node
//					if(nfvNode.get_type().equalsIgnoreCase("nfv")){    					
//    					//check if object exists in map
//						IloColumn col = varL.get(tempVar);
//						if(col!=null){							
//							col = col.and(ilpObject.column(memCapacity.get(nodeIndex), memCount));
//							//keep track of the column corresponding to the variable
//							varL.put(tempVar, col);
//						}else{							
//							col = ilpObject.column(memCapacity.get(nodeIndex), memCount);
//							//keep track of the column corresponding to the variable
//							varL.put(tempVar, col);
//						}
//					}
//					
//				}
//			}				
//		}
	    //Memory capacity constraint (Scalable) - (5)		
		//iterate through the list of NFV Capable Nodes
//		for(int nodeIndex = 0; nodeIndex < nfvList.size(); nodeIndex++){
//			BaseVertex nfvNode = nfvList.get(nodeIndex);
//			//iterate through the list of SD pairs
//			for(TrafficNodes tn : pairList){
//				//iterate through the VNFs in the service chain
//				for(int funcID : InputConstantsAPI.FUNC_REQ){
//					//create the new L object
//					IlpVarL tempVar = new IlpVarL(tn,nfvNode,funcID);
//					//If the vertex is a DC node
////					if(nfvNode.get_type().equalsIgnoreCase("dc")){
////    					//check if object exists in map
////						IloColumn col = varL.get(tempVar);
////						if(col!=null){							
////							col = col.and(ilpObject.column(coreCapacity.get(nodeIndex),1.0));
////							//keep track of the column corresponding to the variable
////							varL.put(tempVar, col);
////						}else{							
////							col = ilpObject.column(coreCapacity.get(nodeIndex),1.0);
////							//keep track of the column corresponding to the variable
////							varL.put(tempVar, col);
////						}	
////					}
//					//If the vertex is a NFV-capable node
//					if(nfvNode.get_type().equalsIgnoreCase("nfv")){
//						double memCount = 0;
//    					for(FuncPt fpt : funcList){
//	 						if(fpt.getid() == funcID){
//	 							memCount = fpt.getcore();
//	 						}
//	 					}    					
//    					//check if object exists in map
//						IloColumn col = varL.get(tempVar);
//						if(col!=null){							
//							col = col.and(ilpObject.column(memCapacity.get(nodeIndex), tn.flow_traffic*memCount));
//							//keep track of the column corresponding to the variable
//							varL.put(tempVar, col);
//						}else{							
//							col = ilpObject.column(memCapacity.get(nodeIndex), tn.flow_traffic*memCount);
//							//keep track of the column corresponding to the variable
//							varL.put(tempVar, col);
//						}
//					}
//					
//				}
//			}				
//		}
		//########VNF is located on path constraint - (5)
		//iterate through the list of SD pairs
	    for(TrafficNodes tn : pairList){
	    	List<Path> routeListSDpair = sdpaths.get(tn);
	    	//iterate through the list of paths
	    	for(Path p : routeListSDpair){
	    		//iterate through the list of vertices through the path
	    		for(BaseVertex vrt : p.get_vertices()){
	    			//check if NFV-capable or DC
	    			if(nfvDCList.contains(vrt)){
	    				//iterate through the VNFs in the service chain
	    				for(int funcID : InputConstantsAPI.FUNC_REQ){		    					  				
	    					//create the new L object
							IlpVarL tempVar = new IlpVarL(tn,vrt,funcID);
	    					//check if object exists in map
							IloColumn col = varL.get(tempVar);
							//constraint object
							IlpCstr5 tempCstr = new IlpCstr5(tn,p,vrt,funcID);	 
							if(col!=null){
								//adding new constraint of type (12)
								col = col.and(ilpObject.column(vnfLocationInPath.get(tempCstr), -1.0));
								//adding new constraint of type (14)
								col = col.and(ilpObject.column(varQIsSet.get(tempCstr), -1.0));
								//keep track of the column corresponding to the variable
								varL.put(tempVar, col);
							}else{
								//adding new constraint of type (12)
								col = ilpObject.column(vnfLocationInPath.get(tempCstr), -1.0);
								//adding new constraint of type (14)
								col = col.and(ilpObject.column(varQIsSet.get(tempCstr), -1.0));
								//keep track of the column corresponding to the variable
								varL.put(tempVar, col);
							}
	    				}
	    			}
	    		}
	    	}
	    }
	    //add the variables to the master problem
	    Map<IlpVarL, IloIntVar> setVarL= new HashMap<IlpVarL, IloIntVar>();
	    //add the columns to the variables
	    for(Map.Entry<IlpVarL, IloColumn> entry : varL.entrySet()){
	    	setVarL.put(entry.getKey(), ilpObject.intVar(entry.getValue(), 0, 1,entry.getKey().toString()));
	    }
	    
	    
	  //######## Q variable #########
	  Map<IlpVarQ, IloColumn> varQ = new HashMap<IlpVarQ, IloColumn>();
	  //######VNF is located on path constraint - (5)
	  //iterate through the list of SD pairs
	  for(TrafficNodes tn : pairList){
	    	List<Path> routeListSDpair = sdpaths.get(tn);
	    	int pathIndex = 0;
	    	//iterate through the list of paths
	    	for(Path p : routeListSDpair){
	    		//iterate through the list of vertices through the path
	    		for(BaseVertex vrt : p.get_vertices()){
	    			//check if NFV-capable or DC
	    			if(nfvDCList.contains(vrt)){
	    				//iterate through the VNFs in the service chain
	    				for(int funcID : InputConstantsAPI.FUNC_REQ){		    					  				
	    					//create the new Q object
							IlpVarQ tempVar = new IlpVarQ(tn,p,pathIndex,vrt,funcID);
	    					//check if object exists in map
							IloColumn col = varQ.get(tempVar);
							//constraint object
							IlpCstr5 tempCstr = new IlpCstr5(tn,p,vrt,funcID);	 
							if(col!=null){
								//adding new constraint of type (12)
								col = col.and(ilpObject.column(vnfLocationInPath.get(tempCstr), 1.0));
								//adding new constraint of type (13)
								col = col.and(ilpObject.column(pathHasVNFLocation.get(tempCstr), 1.0));
								//adding new constraint of type (14)
								col = col.and(ilpObject.column(varQIsSet.get(tempCstr), 1.0));
								//keep track of the column corresponding to the variable
								varQ.put(tempVar, col);
							}else{
								//adding new constraint of type (12)
								col = ilpObject.column(vnfLocationInPath.get(tempCstr), 1.0);
								//adding new constraint of type (13)
								col = col.and(ilpObject.column(pathHasVNFLocation.get(tempCstr), 1.0));
								//adding new constraint of type (14)
								col = col.and(ilpObject.column(varQIsSet.get(tempCstr), 1.0));
								//keep track of the column corresponding to the variable
								varQ.put(tempVar, col);
							}
	    				}
	    			}
	    		}
	    		pathIndex++;
	    	}
	   }
	   //#####Only one variable Q can be selected - (6)		  
	   //iterate through the list of SD pairs
	   for(TrafficNodes tn : pairList){
	    	List<Path> routeListSDpair = sdpaths.get(tn);
	    	int pathIndex = 0;
	    	//iterate through the list of paths
	    	for(Path p : routeListSDpair){
	    		//iterate through the list of vertices through the path
	    		for(BaseVertex vrt : p.get_vertices()){
	    			//check if NFV-capable or DC
	    			if(nfvDCList.contains(vrt)){
	    				//iterate through the VNFs in the service chain
	    				for(int funcID : InputConstantsAPI.FUNC_REQ){		    				
	    						//create the constraint object
		    					IlpCstr6 tempCstr = new IlpCstr6(tn,funcID);		    					
		    					//create the Q object
		    					IlpVarQ tempVar = new IlpVarQ(tn,p,pathIndex,vrt,funcID);
		    					//get the column corresponding to the variable
		    					IloColumn col = varQ.get(tempVar);
		    					if(col != null){
		    						//adding the column to the constraint
		    						col = col.and(ilpObject.column(varQSelection.get(tempCstr), 1.0));
		    						//keep track of the column
		    						varQ.put(tempVar, col);
		    					}else{
		    						//adding the column to the constraint
		    						col = ilpObject.column(varQSelection.get(tempCstr), 1.0);
		    						//keep track of the column
		    						varQ.put(tempVar, col);
		    					}	    							    					
	    				}
	    			}
	    		}
	    		pathIndex++;
	    	}
	    }
	   	//#####If VNF located in a DC - (7)		  
	    //iterate through the list of SD pairs
	    for(TrafficNodes tn : pairList){
	    	List<Path> routeListSDpair = sdpaths.get(tn);
	    	int pathIndex = 0;
	    	//iterate through the list of paths
	    	for(Path p : routeListSDpair){
	    		//iterate through the list of vertices through the path
	    		for(BaseVertex vrt : p.get_vertices()){
	    			//check if node is a DC
	    			if(vrt.get_type().equalsIgnoreCase("dc")){
	    				//iterate through the VNFs in the service chain
	    				for(int index = 0 ; index < InputConstantsAPI.FUNC_REQ.size()-1; index++){
	    					int funcID = InputConstantsAPI.FUNC_REQ.get(index);
	    					int nxtFuncID = InputConstantsAPI.FUNC_REQ.get(index+1);
	    					//create the constraint object
	    					IlpCstr7 tempCstr = new IlpCstr7(tn,p,vrt,funcID,nxtFuncID);
	    					//create the Q object
	    					IlpVarQ tempVar = new IlpVarQ(tn,p,pathIndex,vrt,funcID);
	    					//get the column corresponding to the variable
	    					IloColumn col = varQ.get(tempVar);
	    					if(col != null){
	    						//adding the column to the constraint
	    						col = col.and(ilpObject.column(vnfLocatedInDC.get(tempCstr), -1.0));
	    						//keep track of the column
	    						varQ.put(tempVar, col);
	    					}else{
	    						//adding the column to the constraint
	    						col = ilpObject.column(vnfLocatedInDC.get(tempCstr), -1.0);
	    						//keep track of the column
	    						varQ.put(tempVar, col);
	    					}	    					
	    				}
	    			}
	    		}
	    		pathIndex++;
	    	}
	    }
	    //######If 2 VNFs are on the same path - (8)		
		//iterate through the SD pairs
		for(TrafficNodes tn : pairList){
	    	List<Path> routeListSDpair = sdpaths.get(tn);
	    	int pathIndex = 0;
	    	//iterate through the list of paths
	    	for(Path p : routeListSDpair){
	    		//iterate through the list of vertices through the path
	    		for(BaseVertex vrt : p.get_vertices()){
	    			//check if node is a NFV-capable
	    			if(vrt.get_type().equalsIgnoreCase("nfv")){
	    				//index of the vertex
	    				int indexVrtU = p.get_vertices().indexOf(vrt);
	    				//iterating through the successive nodes in path
	    				for(BaseVertex nxtVrt : p.get_vertices().subList(indexVrtU, p.get_vertices().size())){
	    					 //check if the next vertex is a DC or a NFV-capable node
							 if(nfvDCList.contains(nxtVrt)){
			    				//iterate through the VNFs in the service chain
			    				for(int index = 0 ; index < InputConstantsAPI.FUNC_REQ.size()-1; index++){
			    					int funcID = InputConstantsAPI.FUNC_REQ.get(index);
			    					int nxtFuncID = InputConstantsAPI.FUNC_REQ.get(index+1);
			    					//create the constraint object
			    					IlpCstr8 tempCstr = new IlpCstr8(tn,p,vrt,nxtVrt,funcID,nxtFuncID);
			    					//create the Q object
			    					IlpVarQ tempVar = new IlpVarQ(tn,p,pathIndex,vrt,funcID);
			    					IlpVarQ tempVarNxt = new IlpVarQ(tn,p,pathIndex,nxtVrt,nxtFuncID);
			    					//get the column corresponding to the variable
			    					IloColumn col = varQ.get(tempVar);
			    					IloColumn colNxt = varQ.get(tempVarNxt);
			    					if(col != null){
			    						//adding new constraint of type (15)
			    						col = col.and(ilpObject.column(firstVNFOnPath.get(tempCstr), -1.0));
			    						//adding new constraint of type (17)
			    						col = col.and(ilpObject.column(varJIsSet.get(tempCstr), -1.0));
			    						//keep track of the column
			    						varQ.put(tempVar, col);
			    					}else{
			    						//adding new constraint of type (15)
			    						col = ilpObject.column(firstVNFOnPath.get(tempCstr), -1.0);
			    						//adding new constraint of type (17)
			    						col = col.and(ilpObject.column(varJIsSet.get(tempCstr), -1.0));
			    						//keep track of the column
			    						varQ.put(tempVar, col);
			    					}
			    					if(colNxt != null){
			    						//adding new constraint of type (16)
			    						colNxt = colNxt.and(ilpObject.column(secondVNFOnPath.get(tempCstr), -1.0));
			    						//adding new constraint of type (17)
			    						colNxt = colNxt.and(ilpObject.column(varJIsSet.get(tempCstr), -1.0));
			    						//keep track of the column
			    						varQ.put(tempVarNxt, colNxt);
			    					}else{
			    						//adding new constraint of type (16)
			    						colNxt = ilpObject.column(secondVNFOnPath.get(tempCstr), -1.0);
			    						//adding new constraint of type (17)
			    						colNxt = colNxt.and(ilpObject.column(varJIsSet.get(tempCstr), -1.0));
			    						//keep track of the column
			    						varQ.put(tempVarNxt, colNxt);
			    					}				    				
			    				}
							 }
	    				}
	    			}
	    		}
	    		pathIndex++;
	    	}
		}
		//add the variables to the master problem
	    Map<IlpVarQ, IloIntVar> setVarQ = new HashMap<IlpVarQ, IloIntVar>();
	    //add the columns to the variables
	    for(Map.Entry<IlpVarQ, IloColumn> entry : varQ.entrySet()){
	    	setVarQ.put(entry.getKey(), ilpObject.intVar(entry.getValue(), 0, 1, entry.getKey().toString()));
	    }
		
		
		//########## J variable ###########
		Map<IlpVarJ, IloColumn> varJ = new HashMap<IlpVarJ, IloColumn>();
		//If VNF located in a DC - (7)		   
	    //iterate through the list of SD pairs
	    for(TrafficNodes tn : pairList){
	    	List<Path> routeListSDpair = sdpaths.get(tn);
	    	int pathIndex = 0;
	    	//iterate through the list of paths
	    	for(Path p : routeListSDpair){
	    		//iterate through the list of vertices through the path
	    		for(BaseVertex vrt : p.get_vertices()){
	    			//check if node is a DC
	    			if(vrt.get_type().equalsIgnoreCase("dc")){
	    				//iterate through the VNFs in the service chain
	    				for(int index = 0 ; index < InputConstantsAPI.FUNC_REQ.size()-1; index++){
	    					int funcID = InputConstantsAPI.FUNC_REQ.get(index);
	    					int nxtFuncID = InputConstantsAPI.FUNC_REQ.get(index+1);
	    					//create the constraint object
	    					IlpCstr7 tempCstr = new IlpCstr7(tn,p,vrt,funcID,nxtFuncID);
	    					//create the J object
	    					IlpVarJ tempVar = new IlpVarJ(tn,p,pathIndex,vrt,vrt,funcID,nxtFuncID);
	    					//get the column for the variable
	    					IloColumn col = varJ.get(tempVar);
	    					//check if column exists
	    					if(col != null){
	    						//add the column to the constraint
	    						col = col.and(ilpObject.column(vnfLocatedInDC.get(tempCstr), 1.0));
	    						//keep track of the column associated to the constraint
	    						varJ.put(tempVar, col);
	    					}else{
	    						//add the column to the constraint
	    						col = ilpObject.column(vnfLocatedInDC.get(tempCstr), 1.0);
	    						//keep track of the column associated to the constraint
	    						varJ.put(tempVar, col);
	    					}		    					
	    				}
	    			}
	    		}
	    		pathIndex++;
	    	}
	    }
	    //If 2 VNFs are on the same path - (8)
	    //iterate through the SD pairs
		for(TrafficNodes tn : pairList){
	    	List<Path> routeListSDpair = sdpaths.get(tn);
	    	int pathIndex = 0;
	    	//iterate through the list of paths
	    	for(Path p : routeListSDpair){
	    		//iterate through the list of vertices through the path
	    		for(BaseVertex vrt : p.get_vertices()){
	    			//check if node is a NFV-capable
	    			if(vrt.get_type().equalsIgnoreCase("nfv")){
	    				//index of the vertex
	    				int indexVrtU = p.get_vertices().indexOf(vrt);
	    				//iterating through the successive nodes in path
	    				for(BaseVertex nxtVrt : p.get_vertices().subList(indexVrtU, p.get_vertices().size())){
	    					 //check if the next vertex is a DC or a NFV-capable node
							 if(nfvDCList.contains(nxtVrt)){
			    				//iterate through the VNFs in the service chain
			    				for(int index = 0 ; index < InputConstantsAPI.FUNC_REQ.size()-1; index++){
			    					int funcID = InputConstantsAPI.FUNC_REQ.get(index);
			    					int nxtFuncID = InputConstantsAPI.FUNC_REQ.get(index+1);
			    					//create the constraint object
			    					IlpCstr8 tempCstr = new IlpCstr8(tn,p,vrt,nxtVrt,funcID,nxtFuncID);
			    					//create the J object
			    					IlpVarJ tempVar = new IlpVarJ(tn,p,pathIndex,vrt,nxtVrt,funcID,nxtFuncID);
			    					//get the column for the variable
			    					IloColumn col = varJ.get(tempVar);
			    					//check if column exists
			    					if(col != null){
			    						//adding new constraint of type (15)
			    						col = col.and(ilpObject.column(firstVNFOnPath.get(tempCstr), 1.0));
			    						//adding new constraint of type (16)
			    						col = col.and(ilpObject.column(secondVNFOnPath.get(tempCstr), 1.0));
			    						//adding new constraint of type (17)
			    						col = col.and(ilpObject.column(varJIsSet.get(tempCstr), 1.0));
			    						//keep track of the column associated to the constraint
			    						varJ.put(tempVar, col);
			    					}else{
			    						//adding new constraint of type (15)
			    						col = ilpObject.column(firstVNFOnPath.get(tempCstr), 1.0);
			    						//adding new constraint of type (16)
			    						col = col.and(ilpObject.column(secondVNFOnPath.get(tempCstr), 1.0));
			    						//adding new constraint of type (17)
			    						col = col.and(ilpObject.column(varJIsSet.get(tempCstr), 1.0));
			    						//keep track of the column associated to the constraint
			    						varJ.put(tempVar, col);
			    					}				    					
			    				}
							 }
	    				}
	    			}
	    		}
	    		pathIndex++;
	    	}
		}
		//Only one J can be selected - (9)		
	    //iterate through the SD pairs
		for(TrafficNodes tn : pairList){
	    	List<Path> routeListSDpair = sdpaths.get(tn);
	    	//initialize the path Index
	    	int pathIndex = 0;
	    	//iterate through the list of paths
	    	for(Path p : routeListSDpair){
	    		//iterate through the list of vertices through the path
	    		for(BaseVertex vrt : p.get_vertices()){
	    			//check if node is a NFV-capable
	    			if(vrt.get_type().equalsIgnoreCase("nfv")){
	    				//index of the vertex
	    				int indexVrtU = p.get_vertices().indexOf(vrt);
	    				//iterating through the successive nodes in path
	    				for(BaseVertex nxtVrt : p.get_vertices().subList(indexVrtU, p.get_vertices().size())){
	    					 //check if the next vertex is a DC or a NFV-capable node
							 if(nfvDCList.contains(nxtVrt)){
			    				//iterate through the VNFs in the service chain
			    				for(int index = 0 ; index < InputConstantsAPI.FUNC_REQ.size()-1; index++){
			    					int funcID = InputConstantsAPI.FUNC_REQ.get(index);
			    					int nxtFuncID = InputConstantsAPI.FUNC_REQ.get(index+1);
			    					//create the constraint object
			    					IlpCstr9 tempCstr = new IlpCstr9(tn, funcID, nxtFuncID);
			    					//create the J object
			    					IlpVarJ tempVar = new IlpVarJ(tn, p, pathIndex, vrt, nxtVrt, funcID, nxtFuncID);
			    					//get the column for the variable
			    					IloColumn col = varJ.get(tempVar);
			    					//check if column exists
			    					if(col != null){
			    						//add the column to the constraint
			    						col = col.and(ilpObject.column(varJSelection.get(tempCstr), 1.0));
			    						//keep track of the column associated to the constraint
			    						varJ.put(tempVar, col);
			    					}else{
			    						//add the column to the constraint
			    						col = ilpObject.column(varJSelection.get(tempCstr), 1.0);
			    						//keep track of the column associated to the constraint
			    						varJ.put(tempVar, col);
			    					}				    				
			    				}
							 }
	    				}
	    			}
	    			//check if node is a DC
	    			if(vrt.get_type().equalsIgnoreCase("dc")){
	    				//iterate through the VNFs in the service chain
	    				for(int index = 0 ; index < InputConstantsAPI.FUNC_REQ.size()-1; index++){
	    					int funcID = InputConstantsAPI.FUNC_REQ.get(index);
	    					int nxtFuncID = InputConstantsAPI.FUNC_REQ.get(index+1);
	    					//create the constraint object
	    					IlpCstr9 tempCstr = new IlpCstr9(tn, funcID, nxtFuncID);
	    					//create the J object
	    					IlpVarJ tempVar = new IlpVarJ(tn, p, pathIndex, vrt, vrt, funcID, nxtFuncID);
	    					//get the column for the variable
	    					IloColumn col = varJ.get(tempVar);
	    					//check if column exists
	    					if(col != null){
	    						//add the column to the constraint
	    						col = col.and(ilpObject.column(varJSelection.get(tempCstr), 1.0));
	    						//keep track of the column associated to the constraint
	    						varJ.put(tempVar, col);
	    					}else{
	    						//add the column to the constraint
	    						col = ilpObject.column(varJSelection.get(tempCstr), 1.0);
	    						//keep track of the column associated to the constraint
	    						varJ.put(tempVar, col);
	    					}				    				
	    				}
	    			}
	    		}
	    		//increment the path index
	    		pathIndex++;
	    	}
		}
	    //Enforce service chaining inside a node - (10)		
	    //iterate through the SD pairs
		for(TrafficNodes tn : pairList){
	    	List<Path> routeListSDpair = sdpaths.get(tn);
	    	int pathIndex = 0;
	    	//iterate through the list of paths
	    	for(Path p : routeListSDpair){
	    		//iterate through the list of vertices through the path
	    		for(BaseVertex vrt : p.get_vertices()){
	    			//check if node is a NFV-capable
	    			if(vrt.get_type().equalsIgnoreCase("nfv")){
	    				//index of the vertex
	    				int indexVrtU = p.get_vertices().indexOf(vrt);
	    				//iterating through the successive nodes in path
	    				for(BaseVertex nxtVrt : p.get_vertices().subList(indexVrtU, p.get_vertices().size())){
	    					 //check if the next vertex is a DC or a NFV-capable node
							 if(nfvDCList.contains(nxtVrt)){
			    				//iterate through the VNFs in the service chain
			    				for(int index = 0 ; index < InputConstantsAPI.FUNC_REQ.size()-2; index++){
			    					int funcID = InputConstantsAPI.FUNC_REQ.get(index);
			    					int nxtFuncID = InputConstantsAPI.FUNC_REQ.get(index+1);
			    					int nxtNxtFuncID = InputConstantsAPI.FUNC_REQ.get(index+2);
			    					//create the constraint object
			    					IlpCstr10 tempCstr = new IlpCstr10(tn, p, vrt, nxtVrt, funcID, nxtFuncID, nxtNxtFuncID);
			    					for(BaseVertex prevVrt : p.get_vertices().subList(0, indexVrtU+1)){
			    						//if vrt1 is a DC we would assume all the succeeding dependencies are resolved at the DC
 										//relation will only hold for DC nodes
 										if(prevVrt.get_type().equalsIgnoreCase("nfv")){
					    					//create the J object //(t,u)
					    					IlpVarJ tempVar = new IlpVarJ(tn, p, pathIndex, prevVrt, vrt, funcID, nxtFuncID);
					    					//get the column for the variable
					    					IloColumn col = varJ.get(tempVar);
					    					//check if column exists
					    					if(col != null){
					    						//add the column to the constraint
					    						col = col.and(ilpObject.column(scInSameNode.get(tempCstr), 1.0));
					    						//keep track of the column associated to the constraint
					    						varJ.put(tempVar, col);
					    					}else{
					    						//add the column to the constraint
					    						col = ilpObject.column(scInSameNode.get(tempCstr), 1.0);
					    						//keep track of the column associated to the constraint
					    						varJ.put(tempVar, col);
					    					}
 										}
			    					}
			    					//create the J object //(u,v)
			    					IlpVarJ tempVarNxt = new IlpVarJ(tn, p, pathIndex, vrt, nxtVrt, nxtFuncID, nxtNxtFuncID);
			    					//get the column for the variable
			    					IloColumn colNxt = varJ.get(tempVarNxt);
			    					//check if column exists
			    					if(colNxt != null){
			    						//add the column to the constraint
			    						colNxt = colNxt.and(ilpObject.column(scInSameNode.get(tempCstr), -1.0));
			    						//keep track of the column associated to the constraint
			    						varJ.put(tempVarNxt, colNxt);
			    					}else{
			    						//add the column to the constraint
			    						colNxt = ilpObject.column(scInSameNode.get(tempCstr), -1.0);
			    						//keep track of the column associated to the constraint
			    						varJ.put(tempVarNxt, colNxt);
			    					}
			    				}
							 }
	    				}
	    			}
	    		}
	    		pathIndex++;
	    	}
		}
	    //Dependency path Selection - (11)		
	    //iterate through the SD pairs
		for(TrafficNodes tn : pairList){
	    	List<Path> routeListSDpair = sdpaths.get(tn);
	    	int pathIndex = 0;
	    	//iterate through the list of paths
	    	for(Path p : routeListSDpair){
	    		//iterate through the VNFs in the service chain
				for(int index = 0 ; index < InputConstantsAPI.FUNC_REQ.size()-1; index++){
					int funcID = InputConstantsAPI.FUNC_REQ.get(index);
					int nxtFuncID = InputConstantsAPI.FUNC_REQ.get(index+1);
					//create the constraint object
					IlpCstr11 tempCstr = new IlpCstr11(tn, p, funcID, nxtFuncID);
					//iterate through the list of vertices through the path
		    		for(BaseVertex vrt : p.get_vertices()){
		    			//check if node is a DC
		    			if(vrt.get_type().equalsIgnoreCase("dc")){
		    				//create the J object
	    					IlpVarJ tempVar = new IlpVarJ(tn, p, pathIndex, vrt, vrt, funcID, nxtFuncID);
	    					//get the column for the variable
	    					IloColumn col = varJ.get(tempVar);
	    					//check if column exists
	    					if(col != null){
	    						//add the column to the constraint
	    						col = col.and(ilpObject.column(dependencyPathSelection.get(tempCstr), 1.0));
	    						//keep track of the column associated to the constraint
	    						varJ.put(tempVar, col);
	    					}else{
	    						//add the column to the constraint
	    						col = ilpObject.column(dependencyPathSelection.get(tempCstr), 1.0);
	    						//keep track of the column associated to the constraint
	    						varJ.put(tempVar, col);
	    					} 
		    			}
		    			//check if node is NFV-capable
		    			if(vrt.get_type().equalsIgnoreCase("nfv")){
		    				//index of the vertex
		    				int indexVrtU = p.get_vertices().indexOf(vrt);
		    				//iterating through the successive nodes in path
		    				for(BaseVertex nxtVrt : p.get_vertices().subList(indexVrtU, p.get_vertices().size())){
		    					//checks if the node is a DC or NFV-capable node
		    					if(nfvList.contains(nxtVrt)){
		    						//create the J object
			    					IlpVarJ tempVar = new IlpVarJ(tn, p, pathIndex, vrt, nxtVrt, funcID, nxtFuncID);
			    					//get the column for the variable
			    					IloColumn col = varJ.get(tempVar);
			    					//check if column exists
			    					if(col != null){
			    						//add the column to the constraint
			    						col = col.and(ilpObject.column(dependencyPathSelection.get(tempCstr), 1.0));
			    						//keep track of the column associated to the constraint
			    						varJ.put(tempVar, col);
			    					}else{
			    						//add the column to the constraint
			    						col = ilpObject.column(dependencyPathSelection.get(tempCstr), 1.0);
			    						//keep track of the column associated to the constraint
			    						varJ.put(tempVar, col);
			    					}
		    					}
		    				}
		    			}    		    				
		    		}		    					    					
				}
				pathIndex++;
	    	}
		}
		//add the variables to the master problem
	    Map<IlpVarJ, IloIntVar> setVarJ = new HashMap<IlpVarJ, IloIntVar>();
	    //add the columns to the variables
	    for(Map.Entry<IlpVarJ, IloColumn> entry : varJ.entrySet()){
	    	setVarJ.put(entry.getKey(), ilpObject.intVar(entry.getValue(), 0, 1, entry.getKey().toString()));
	    }	    
	 
		//export the ILP models
//		ilpObject.exportModel("master_problem_initial.lp");
//		ilpObject.exportModel("master_problem_initial.sav");
	    //set the MIP Gap //relative tolerance
	    ilpObject.setParam(IloCplex.DoubleParam.EpGap, InputConstantsAPI.MIP_GAP);	 
	    //set the time for which the ILP need to run
	    ilpObject.setParam(IloCplex.DoubleParam.TiLim, InputConstantsAPI.TIME_LIMIT);
	    //get the start time
	    //ILP start time
	  	long ilpStartTime = new Date().getTime();
		//solve the model
		ilpObject.solve();
		System.out.println();
		//check the feasibility of the ILPmodel
	    if(ilpObject.isPrimalFeasible()){
	    	System.out.println("ILP is primal feasible!");			
	    	//ILP end time
			long ilpEndTime = new Date().getTime();
		    //get the solution time
		    long execTime = ilpEndTime - ilpStartTime;
			//get the optimization gap		
		    double optimalityGap = ilpObject.getMIPRelativeGap();
		    String ilpInstanceDetails2 = ilpInstanceDetails + "_traf_prcnt_" + traf_prcnt + "_total_flow_" + total_flow + "_dc_" + dcNode;
//		    String ilpInstanceDetails3 = ilpInstanceDetails + "_mem_count_" + mem_count + "_traf_prcnt_" + traf_prcnt + "_total_flow_" + total_flow + "_dc_" + dcNode;
			//get the objective value
			System.out.println(ilpInstanceDetails2 + " : " + ilpObject.getObjValue());
			//return the objective value for the memory case
			rValue = ilpObject.getObjValue();
//		    System.out.println(ilpInstanceDetails3 + " : " + ilpObject.getObjValue());
			//write the objective value to the file
			//InstanceName \t value \t optimization gap \t execution time
			String outputString = ilpInstanceDetails2 + "\t" + ilpObject.getObjValue() + "\t\t" + execTime + "\t" + optimalityGap + "\n";
//		    String outputStringMem = ilpInstanceDetails3 + "\t" + ilpObject.getObjValue() + "\t\t" + execTime + "\t" + optimalityGap + "\n";
			//catch the exception
			try{
				wrt.write(outputString);
//				wrt.write(outputStringMem);
			}catch(IOException exio){
				System.err.println("Write exception in results file : " + exio + " caught");
				exio.printStackTrace();
				System.out.println("*********************************************");
			}
			//write the solution
//			ilpObject.writeSolution(ilpInstanceDetails + "_" + "Sol");
			//print out the DC node and Traffic prcnt
			System.out.println("########## " + "DC " + dcNode + "Traffic (Gbps) " + 1.25*traf_prcnt*InputConstantsAPI.BANDWIDTH_PER_LAMBDA/100000 + " ##########");
			//find out the maximum loaded link 
//			rValue = Analysis.linkAnalysis(ilpObject, setVarR,sdpaths);
			//print out the value of the maximum loaded link
			System.out.println("########## " + "Max Loaded Link Value : " + rValue + " ###########");
	    }else{
	    	System.err.println(ilpInstanceDetails + " is infeasible!!!");	
	    	//InstanceName \t Infeasible \t DC Node
			String outputStringInf = "\t" + ilpInstanceDetails + "\t" + traf_prcnt + "\t" + total_flow + "\t" + "INF" + "\t" + dcNode + "\n";
			//catch the exception
			try{
				wrtInf.write(outputStringInf);
			}catch(IOException exio){
				System.err.println("Write exception in results file : " + exio + " caught");
				exio.printStackTrace();
				System.out.println("*********************************************");
			}
	    }
	    //destroy the ilp object
	    ilpObject.end();
		
	}catch(IloException exc){
		System.err.println("Concert exception '" + exc + "' caught");				
		exc.printStackTrace(); 
		System.out.println("*********************************************");
	}
	
	//return the double value
	return rValue;
}
	
	
	
public static void main(String args[]) throws Exception{ 
		
		
		//build graph object from given network file
	    Class<?> cls = Class.forName("ILP.ILPconcertAPI");		
	    //returns the ClassLoader
	    ClassLoader cLoader = cls.getClassLoader();
	    //print out the class name
	    System.out.println("Class Name : " + cLoader.getClass());
	    //finds the resource with the given name
	    InputStream networkFileStream = cLoader.getResourceAsStream(InputConstantsAPI.FILE_READ_PATH + InputConstantsAPI.NETWORK_FILE_NAME);
	    //generate the graph object
	    Graph g = new Graph(networkFileStream);    
	    
		
		//print out the edges
		for(BaseVertex s_vert : g._vertex_list){
			for(BaseVertex t_vert : g.get_adjacent_vertices(s_vert)){
				System.out.println( s_vert.get_id() + "->" + t_vert.get_id());
			}
		}
		//k shortest paths
		int top_k = InputConstantsAPI.k_paths;
		//k shortest path objects
		YenTopKShortestPathsAlg kpaths = new YenTopKShortestPathsAlg(g);			
		//Store paths for each s-d pair
		Map<TrafficNodes,List<Path>> sdpaths = new HashMap<TrafficNodes,List<Path>>();
		for(BaseVertex source_vert : g._vertex_list){
			for(BaseVertex target_vert : g._vertex_list){
				if(source_vert != target_vert){
					List<Path> path_temp = new ArrayList<Path>(kpaths.get_shortest_paths(source_vert,target_vert, top_k));					
					//create the sd-pair for that pair of nodes
					TrafficNodes sd_temp = new TrafficNodes(source_vert, target_vert);
					//add to list of paths depending on s-d pair
					sdpaths.put(sd_temp, path_temp);
				}
			}
		}
		
		//print out the graph
		/*int sd_count = 0;
		for( Map.Entry<TrafficNodes,List<Path>> entry : sdpaths.entrySet()){
			sd_count++;
			System.out.println(sd_count + " s-d pair : ( " +  entry.getKey().v1.get_id() + " , " + entry.getKey().v2.get_id() + " )");
		    System.out.println("paths : " + entry.getValue());
	    }*/
		
		
		//allocate traffic to the given SD pairs
		//SD pairs between which we desire traffic to be
		//Store each s-d pair
		List<TrafficNodes> pairList = new ArrayList<TrafficNodes>();
		//finds the resource with the given name    
	    InputStream sdPairStream = cLoader.getResourceAsStream(InputConstantsAPI.FILE_READ_PATH + InputConstantsAPI.SD_PAIRS);
	    //read the resource
		pairList = ReadFile.readSDPairs(sdpaths,sdPairStream);
		//check if the pairList is empty
		if(pairList.size()==0)
			System.out.println("####### PairList is empty #########");
		int sdCounter = 0;
	    //print out the pairLists that have been read
		for(TrafficNodes tmp : pairList){
//			System.out.println("SD pair " + sdCounter );
			System.out.println("SD pair " + sdCounter + " : " + tmp.v1.get_id() + " , " + tmp.v2.get_id());
			//increment the sd counter
			sdCounter++;
		}
		
		//list all the paths between s-d pairs that contain the node 13 and 14
		/*for(TrafficNodes tn : pairList){
			if(tn.v1.get_id()!=14 && tn.v2.get_id()!=14)
				for(Path p : sdpaths.get(tn)){
					for(BaseVertex pathVrt : p.get_vertices()){
						if(pathVrt.get_id() == 13){
							System.out.println("( " + tn.v1.get_id() + " , " + tn.v2.get_id() + " ) has 13 in path" );
							System.out.print("Path : ");
							//print out the path
							for(BaseVertex pVrt : p.get_vertices()){
								System.out.print(pVrt.get_id() + "->");
							}
							System.out.println();
						}
						if(pathVrt.get_id() == 14){
							System.out.println("( " + tn.v1.get_id() + " , " + tn.v2.get_id() + " ) has 14 in path" );
							System.out.print("Path : ");
							//print out the path
							for(BaseVertex pVrt : p.get_vertices()){
								System.out.print(pVrt.get_id() + "->");
							}
							System.out.println();
						}
						
					}
				}
		}*/
		
		//read the function point details
		List<FuncPt> funcList = new ArrayList<FuncPt>();
		//finds the resource with the given name
		InputStream funcListFileStream = cLoader.getResourceAsStream(InputConstantsAPI.FILE_READ_PATH + InputConstantsAPI.FUNCTION_DETAILS);
		funcList = ReadFile.readFnPt(funcListFileStream);
		/*for(FuncPt fpt : func_list){
			System.out.println(fpt.getid() + " -- " + fpt.getcore());
		}*/	
		//check if the pair in the list is the key for the graph
//		for(TrafficNodes tmp_pr : pairList){
//			for( Map.Entry<TrafficNodes,List<Path>> entry : sdpaths.entrySet()){				
//				 if(tmp_pr == entry.getKey()){
//					 System.out.println(entry.getValue());
//				 }
//			}
//		}	
		
		
		
		
			
		int total_flow = 0;
		//ILP Generator object
		ILPconcertAPI ilpModel = new ILPconcertAPI();
		String sdpairTMName = new String(InputConstantsAPI.SD_PAIRS);
		String[] psdpairTMName = sdpairTMName.split("\\.");
		//include the shortest paths in the discussion too
		//create the output file
		File outputFile = new File(InputConstantsAPI.SC_STRATEGY + "_" + psdpairTMName[0] + "_" + top_k +".txt");		
//		File outputFileLink = new File(InputConstantsAPI.SC_STRATEGY + "_Link_" + psdpairTMName[0] + "_" + top_k + ".txt");
		//change rvalue return if changing below
		File outputFileLink = new File(InputConstantsAPI.SC_STRATEGY + "_BW_" + psdpairTMName[0] + "_" + top_k + ".txt");
		File outputFileInf = new File(InputConstantsAPI.SC_STRATEGY + "_INF_" + psdpairTMName[0] + "_" + top_k + ".txt");
		//File names for memory operations
//		File outputFile = new File("Mem_S_" + InputConstantsAPI.SC_STRATEGY + "_" +sdpairTMName + ".txt");		
//		File outputFileLink = new File("Mem_S_" + InputConstantsAPI.SC_STRATEGY + "_BWConsumption_" +sdpairTMName + ".txt");
//		File outputFileInf = new File("Mem_S_" + InputConstantsAPI.SC_STRATEGY + "_INF_" +sdpairTMName + ".txt");
		//create the output stream
		FileOutputStream outStream = new FileOutputStream(outputFile);	
		FileOutputStream outStream2 = new FileOutputStream(outputFileLink);
		FileOutputStream outStream3 = new FileOutputStream(outputFileInf);
		//create the output stream writer
		OutputStreamWriter osw = new OutputStreamWriter(outStream);		
		OutputStreamWriter osw2 = new OutputStreamWriter(outStream2);
		OutputStreamWriter osw3 = new OutputStreamWriter(outStream3);
		//file writer
		Writer wrt = new BufferedWriter(osw);
		Writer wrt2 = new BufferedWriter(osw2);
		Writer wrt3 = new BufferedWriter(osw3);
		
		
		
		//assign num of cores to NFV capable nodes
		for(int core_count: InputConstantsAPI.CORE_NUM_LIST){
//		for(int mem_count: InputConstantsAPI.MEMORY_NUM_LIST){
	        // assign traffic to sd pairs and generate ILP
			for(int traf_prcnt : InputConstantsAPI.TRAF_INT){
				
				total_flow = 0;
				int br_flow = traf_prcnt*InputConstantsAPI.BANDWIDTH_PER_LAMBDA/100;
				int hq_flow = traf_prcnt*InputConstantsAPI.BANDWIDTH_PER_LAMBDA*3/200;
				
				for(TrafficNodes tmp : pairList){
					//check for hq id 
					if( tmp.v1.get_id()==InputConstantsAPI.HQ_Node || tmp.v2.get_id()==InputConstantsAPI.HQ_Node ){
						tmp.flow_traffic = hq_flow;
						total_flow += hq_flow;
					}
					else{
						tmp.flow_traffic = br_flow;
						total_flow += br_flow;						
					}					
				}
				System.out.println("Percentage: " + traf_prcnt + " Total Flow: " + total_flow);	
	
				//create arraylist to store max link values returned
				ArrayList<Double> maxLinkPerTrafficIntPerStrategy = new ArrayList<Double>();
				//create arraylist to store average of the objective values
//				ArrayList<Double> avgBWConsumpPerTrafficIntPerMem = new ArrayList<Double>();
				
				//implementing the DC NFV ALL strategy; //implementing the DC NFV 4 strategy; //implementing the DC only strategy;
				//iterate through the vertex list
			    for(BaseVertex node : g._vertex_list){
			    	//get the DC node
			    	int dcNode = 0;
			    	//assign the DC nodes
			    	if(InputConstantsAPI.DC_SET.contains(node.get_id())){
			    		node.set_type("dc");
			    		dcNode = node.get_id();	
			    		
			    		//get the strategy
			    		String strategy = InputConstantsAPI.SC_STRATEGY;
			    		if(strategy.equals("DC_Only")){			    	
					    	//assign normal switches in the case of DC-only
					    	for(BaseVertex tempNode : g._vertex_list){
					    		//set the rest of switches to type 'sw'
					    		if(tempNode.get_id() != dcNode){
					    			tempNode.set_type("sw");
					    		}
					    	}
			    		}else if(strategy.equals("DC_NFV_ALL")){				    	
					    	//assign all nodes as NFV in the case of DC NFV ALL
					    	for(BaseVertex tempNode : g._vertex_list){
					    		//set the rest of switches to type 'nfv'
					    		if(tempNode.get_id() != dcNode){
					    			tempNode.set_type("nfv");
					    		}
					    	}
			    		}else if(strategy.equals("DC_NFV_4")){				    	
					    	//DC NFV 4 : assign the NFV-capable nodes and the normal switches
					    	for(BaseVertex tempNode : g._vertex_list){
						    	if(InputConstantsAPI.NFV_SET.contains(tempNode.get_id())){
						    		tempNode.set_type("nfv");
						    	}else if(tempNode.get_id() != dcNode){
						    		tempNode.set_type("sw");
						    	}
					    	}
			    		}
						//print out the traffic details
						for(TrafficNodes tmp : pairList){
							System.out.println(tmp.v1.get_type() + " , " + tmp.v2.get_type() + " , " + tmp.flow_traffic);
						}
				    	//print out the all the vertices and their types
	//			    	for(BaseVertex vrtNode : g._vertex_list){
	//			    		//print out the vertex node and type
	//			    		System.out.println(vrtNode.get_id() + " , " + vrtNode.get_type());
	//			    	}
				    	
				    	//List of NFV-capable vertices		
						ArrayList<BaseVertex> nfvList =  new ArrayList<BaseVertex>();
						//List of NFV-capable vertices along with the DC
						ArrayList<BaseVertex> nfvDCList = new ArrayList<BaseVertex>();
						//build the lists
						for(BaseVertex tmpVrt : g._vertex_list ){			
							System.out.println("Vertex ID : " + tmpVrt.get_id() + " , " + tmpVrt.get_type());			
							if(tmpVrt.get_type().equalsIgnoreCase("dc")){			
								nfvDCList.add(tmpVrt);
							}
							if(tmpVrt.get_type().equalsIgnoreCase("nfv")){
								nfvList.add(tmpVrt);
								nfvDCList.add(tmpVrt);
							}			
						}
						//details of the ilp instance
						String ilpInstanceDetails = InputConstantsAPI.SC_STRATEGY + "_" + sdpairTMName;
						//generate the ILP model and solve it
						maxLinkPerTrafficIntPerStrategy.add(ilpModel.makeLPModel(g,pairList,sdpaths,nfvList,nfvDCList,funcList,ilpInstanceDetails,wrt,wrt3,traf_prcnt,total_flow,dcNode,core_count));	
//						avgBWConsumpPerTrafficIntPerMem.add(ilpModel.makeLPModel(g,pairList,sdpaths,nfvList,nfvDCList,funcList,ilpInstanceDetails,wrt,wrt3,traf_prcnt,total_flow,dcNode,mem_count));
			    	}	
			    }
			    double totalLinkValues = 0.0;
			    double numLinkValues = 0;
			    //find the average of all the link loads across the DC placements
			    for(double linkVal : maxLinkPerTrafficIntPerStrategy){
			    	if(linkVal != 0){
			    		//add up the total link values
			    		totalLinkValues += linkVal;
			    		//add up the average link loads across the DC placements
			    		numLinkValues += 1;
			    	}
			    }
			    //write the average of the maximum link value (across DC placements)
			    wrt2.write("Traffic Percentage" + "\t" + traf_prcnt + "\t" + (totalLinkValues/numLinkValues) + "\n");
//			    System.out.println(" #### Traffic Percentage #### " +  traf_prcnt + " #### Avg. Link Values #### " + (totalLinkValues/numLinkValues));
			    System.out.println(" #### Traffic Percentage #### " +  traf_prcnt + " #### Avg. BW Consumption #### " + (totalLinkValues/numLinkValues));
			    //write a new line character after every change in traffic intensity
			    wrt3.write("\n");
//			    double totalBWValues = 0.0;
//			    double numBWValues = 0;
//			    //find the average of all the link loads across the DC placements
//			    for(double bwVal : avgBWConsumpPerTrafficIntPerMem){
//			    	if(bwVal != 0){
//			    		//add up the total link values
//			    		totalBWValues += bwVal;
//			    		//add up the average link loads across the DC placements
//			    		numBWValues += 1;
//			    	}
//			    }
//			    //write the average of the maximum link value (across DC placements)
//			    wrt2.write("Memory" + "\t" + mem_count + "\t" + "Traffic Percentage" + "\t" + traf_prcnt + "\t" + (totalBWValues/numBWValues) + "\n");
			    
			}				
		}
		//close the file
		wrt.close();
		//close the file
		wrt2.close();
		//close the file
		wrt3.close();
		

		
		
		//generate the traffic between source_node pairs
//		ilpgen.generateTraffic_1(pair_list);
//		ilpgen.generateTraffic(pair_list);	
		//write the generated traffic matrix between the source-destination pairs
//		WriteFile.TrafficDetails(pair_list);
		//read the traffic details	
//		ReadFile.readTraffic(pair_list);
		//check the read traffic values
		/*for(int i=0; i<=0; i++){
			for(TrafficNodes pr : pair_list){				
				System.out.println("Originating node " + pr.v1.get_id() + " ; " + pr.flow_traffic.get(i));				
			}
		}*/		
	 	
		   
		//print out the traffic between all traffic nodes
		/*for(int i=0; i<=2; i++){
			for(TrafficNodes pr : pair_list){				
					System.out.println("Originating node " + pr.v1.get_id() + " ; " + pr.flow_traffic.get(i));				
			}
		}*/
	       	
				
		//printing out the k-shortest paths between the source-destination pairs
		/*for(Map.Entry<TrafficNodes,List<Path>> entry : sdpaths.entrySet()){
			System.out.println("s-d pair : ( " +  entry.getKey().v1.get_id() + " , " + entry.getKey().v2.get_id() + " )");
		    System.out.println("paths : " + entry.getValue().get(0) + " ; " +entry.getValue().get(1));
		}
		*/
		/*System.out.println(g._BaseVertex_num);
		System.out.println(g._edge_num);*/
		//Read the output variable file
        //ReadFile.outputVariables();	
		//calculate network resources consumed by hard-wired network middle-boxes
		//input  : the locations of the middle-boxes
		
		//store the list of source vertices
//		List<BaseVertex> source_vertice = new ArrayList<BaseVertex>();
//		//store the list of destination vertices
//		List<BaseVertex> destination_vertice = new ArrayList<BaseVertex>();
//		
//		//filling up the source_vertice and destination_vertice lists
//		for(TrafficNodes tn_entr : pairList){
//			if( ! source_vertice.contains(tn_entr.v1) || source_vertice == null ){
//				source_vertice.add(tn_entr.v1);
//			}
//			if( ! destination_vertice.contains(tn_entr.v2) || destination_vertice == null ){
//				destination_vertice.add(tn_entr.v2);
//			}
//		}	
			
			
		
			    
			 	
			
	}

}
