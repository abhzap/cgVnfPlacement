package ILP;

import edu.asu.emit.qyan.alg.model.Path;
import edu.asu.emit.qyan.alg.model.abstracts.BaseVertex;

public class IlpVarQ {

	public TrafficNodes tn;
	public Path p;
	public BaseVertex vrt;
	public int funcID;
	public int pathIndex;	
	
	public IlpVarQ(TrafficNodes tn, Path p, int pathIndex, BaseVertex vrt, int funcID){
		this.tn = tn;
		this.p = p;
		this.pathIndex = pathIndex;
		this.vrt = vrt;
		this.funcID = funcID;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		boolean result = false;
		if(obj == null || obj.getClass() != getClass()){
			result = false;
		} else {
			IlpVarQ o = (IlpVarQ) obj;
			if( this.tn.equals(o.tn) && this.p.equals(o.p) && (this.vrt.get_id()==o.vrt.get_id()) && (this.funcID==o.funcID) && (this.pathIndex == o.pathIndex) ){
				result = true;
			}
		}
	    return result;
	}	
	
	@Override
	public int hashCode()
	{
	    return this.tn.hashCode() + this.p.hashCode() + this.vrt.hashCode() + this.funcID + this.pathIndex;
	}
	
	@Override
	public String toString(){
		return "Q_s" + this.tn.v1.get_id() + "_d" + this.tn.v2.get_id() + "_p" + this.pathIndex + "_Node" + this.vrt.get_id() + "_VNF" + this.funcID;
	}
}
