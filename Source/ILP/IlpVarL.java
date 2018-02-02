package ILP;

import edu.asu.emit.qyan.alg.model.abstracts.BaseVertex;

public class IlpVarL {

	public TrafficNodes tn;
	public BaseVertex vrt;
	public int funcID;
	
	
	public IlpVarL(TrafficNodes tn, BaseVertex vrt, int funcID){
		this.tn = tn;
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
			IlpVarL o = (IlpVarL) obj;
			if( this.tn.equals(o.tn) && (this.vrt.get_id()==o.vrt.get_id()) && (this.funcID==o.funcID)){
				result = true;
			}
		}
	    return result;
	}	
	
	@Override
	public int hashCode()
	{
	    return this.tn.hashCode() + this.vrt.hashCode() + this.funcID;
	}
	
	@Override
	public String toString(){
		return "L_s" + this.tn.v1.get_id() + "_d" + this.tn.v2.get_id() + "_Node" + this.vrt.get_id() + "_VNF" + this.funcID;
	}
}
