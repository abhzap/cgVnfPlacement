package ILP;

public class FlowCount {

		//total amount of flow passing through the link
		public double totalFlow;
		//keeps count of the number of flows that traverse the link
		public int traversalCount;
		
		public FlowCount(double flow){
			this.totalFlow = flow;
			this.traversalCount = 1;
		}
		
		public void addFlow(double flow){
			this.totalFlow += flow;
			this.traversalCount += 1;
		}
}
