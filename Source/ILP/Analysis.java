package ILP;

import ilog.concert.IloException;
import ilog.concert.IloIntVar;
import ilog.cplex.IloCplex;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.asu.emit.qyan.alg.model.Path;
import edu.asu.emit.qyan.alg.model.abstracts.BaseVertex;

public class Analysis {
	
	public static double linkAnalysis(IloCplex ilpObject,Map<IlpVarR, IloIntVar> setVarR, Map<TrafficNodes,List<Path>> sdpaths){		
		//create a map of the edges used
		Map<EdgePair,FlowCount> edgeUsed = new HashMap<EdgePair,FlowCount>();
		try{
		//iterate through the links and find the maximum
		for(Map.Entry<IlpVarR, IloIntVar> entry : setVarR.entrySet()){
			//check if the particular R variable has been set
			if(ilpObject.getValue(entry.getValue()) == 1.0){
				//get the R object
				IlpVarR tempVarR = entry.getKey();
				//The source-destinatio pair for the path and the flow on it
				System.out.println("( " + tempVarR.tn.v1.get_id() + " , " + tempVarR.tn.v2.get_id() + " ) : " + tempVarR.tn.flow_traffic);
				System.out.print("Path taken = ");
				//print out the path taken by it
				for(BaseVertex pathVrt : tempVarR.p.get_vertices()){
					System.out.print(pathVrt.get_id() + "->");
				}
				System.out.println();
				//populate the edgepair list
				for(int vPos = 0; vPos < (tempVarR.p.get_vertices().size()-1); vPos++){
					int srcIndex = vPos;//edge source index
					int sinkIndex = vPos + 1;//edge target index
					BaseVertex srcVrt = tempVarR.p.get_vertices().get(srcIndex); //get source vertex
					BaseVertex sinkVrt = tempVarR.p.get_vertices().get(sinkIndex); //get destination vertex
					EdgePair tempEP = new EdgePair(srcVrt, sinkVrt);
					//if object not present in edgeUsed
					if(edgeUsed.get(tempEP) == null){
						//make a flow count object
						//add the flow traffic
						FlowCount flCnt = new FlowCount(tempVarR.tn.flow_traffic);	
						//add the object to the array list
						edgeUsed.put(tempEP,flCnt);
					}else{
						//add the flow to the already existing flowCount Object
						edgeUsed.get(tempEP).addFlow(tempVarR.tn.flow_traffic);
					}
				}
			}
		}
		}catch(IloException exIlo){
			System.err.println("Error has been caught in link Analysis!");
		}
		//iterate through the edges that have been used and 
		//show the flow count on them and the number of times they have been used
		System.out.println("src->sink ; totalFlow ; numOfTraversals ; avgFlow");
		for(Map.Entry<EdgePair, FlowCount> entry : edgeUsed.entrySet()){
			System.out.println(entry.getKey().source.get_id() + "->" + entry.getKey().sink.get_id() + " ; " + entry.getValue().totalFlow + " ; " + entry.getValue().traversalCount );
		}
		//create an ArrayList that stores the averages flow count on every link
		ArrayList<Double> FlowPerLink = new ArrayList<Double>(); 
		//iterate through the set of values that are created
		for(FlowCount flCntr : edgeUsed.values()){
			FlowPerLink.add(flCntr.totalFlow);
		}
		//find the maximum flow link that is returned		
		return Collections.max(FlowPerLink);
	}

}
