package analytics.google.v2;

import java.util.ArrayList;
import java.util.Collection;

public class BillObject {
	private String billNo;
	private Collection<SourceObject> source;
	
	public BillObject() {
		source = new ArrayList<SourceObject>();
	}
	
	public BillObject(String billNo, Collection<SourceObject> source) {
		this.billNo = billNo;
		this.source = source;
	}
	
	public String getBillNo() {
		return billNo;
	}
	public Collection<SourceObject> getSource() {
		return source;
	}
	
	public void setBillNo(String billNo) {
		this.billNo = billNo;
	}
	public void setSource(Collection<SourceObject> source) {
		this.source = source;
	}
}
