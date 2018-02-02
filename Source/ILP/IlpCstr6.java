package ILP;

import edu.asu.emit.qyan.alg.model.abstracts.BaseVertex;

public class IlpCstr6 {
	public TrafficNodes tn;	
	public int funcID;
	
	public IlpCstr6(TrafficNodes tn, int funcID){
		this.tn = tn;	
		this.funcID = funcID;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		boolean result = false;
		if(obj == null || obj.getClass() != getClass()){
			result = false;
		} else {
			IlpCstr6 o = (IlpCstr6) obj;
			if( this.tn.equals(o.tn) && (this.funcID==o.funcID) ){
				result = true;
			}
		}
	    return result;
	}	
	
	@Override
	public int hashCode()
	{
	    return this.tn.hashCode() + this.funcID;
	}
}
