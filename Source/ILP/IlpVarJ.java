package ILP;

import edu.asu.emit.qyan.alg.model.Path;
import edu.asu.emit.qyan.alg.model.abstracts.BaseVertex;

public class IlpVarJ {

	public TrafficNodes tn;
	public Path p;
	public int pathIndex;
	public BaseVertex vrt;
	public BaseVertex nxtVrt;
	public int funcID;
	public int nxtFuncID;
	
	public IlpVarJ(TrafficNodes tn, Path p, int pathIndex, BaseVertex vrt, BaseVertex nxtVrt, int funcID, int nxtFuncID){
		this.tn = tn;
		this.p = p;
		this.pathIndex = pathIndex;
		this.vrt = vrt;
		this.nxtVrt = nxtVrt;
		this.funcID = funcID;
		this.nxtFuncID = nxtFuncID;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		boolean result = false;
		if(obj == null || obj.getClass() != getClass()){
			result = false;
		} else {
			IlpVarJ o = (IlpVarJ) obj;
			if( this.tn.equals(o.tn) && this.p.equals(o.p) && (this.vrt.get_id()==o.vrt.get_id()) && (this.nxtVrt.get_id()==o.nxtVrt.get_id()) && (this.funcID==o.funcID) && (this.nxtFuncID==o.nxtFuncID) && (this.pathIndex == o.pathIndex) ){
				result = true;
			}
		}
	    return result;
	}	
	
	@Override
	public int hashCode()
	{
	    return this.tn.hashCode() + this.p.hashCode() + this.vrt.hashCode() + this.nxtVrt.hashCode() +this.funcID + this.nxtFuncID  + this.pathIndex;
	}
	@Override
	public String toString(){
		return "J_s" + this.tn.v1.get_id() + "_d" + this.tn.v2.get_id() + "_p" + this.pathIndex + "_sNode" + this.vrt.get_id() + "_tNode" + this.nxtVrt.get_id() + "_sVNF" + this.funcID  + "_tVNF" + this.nxtFuncID;
	}
}
