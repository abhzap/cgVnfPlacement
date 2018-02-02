package ILP;
import edu.asu.emit.qyan.alg.model.abstracts.BaseVertex;

public class TrafficNodes {
	
	public BaseVertex v1;
	public BaseVertex v2;	
	
	public int flow_traffic; //expressed in Mbps
	
	public TrafficNodes(){
		
	}
	
	public TrafficNodes(BaseVertex v1,BaseVertex v2){
		   this.v1 = v1;
		   this.v2 = v2;		 
		   this.flow_traffic = 0;
	}
	
	
}
