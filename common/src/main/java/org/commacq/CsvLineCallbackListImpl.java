package org.commacq;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;

/**
 * A default implementation of CsvLineCallback that just collects all
 * the CsvLines into a list and returns them.
 */
public class CsvLineCallbackListImpl extends CsvLineCallbackAbstractSimple {

	@Getter
	private final List<CsvLine> updateList;
	@Getter
	private final List<String> removeList;
	@Getter
	private String columnNamesCsv;
	
	public CsvLineCallbackListImpl() {
		updateList = new ArrayList<>();
		removeList = new ArrayList<>();
	}
	
	/**
	 * Allow for initialisation of list to a sensible initial size.
	 * @param listSize
	 */
	public CsvLineCallbackListImpl(int listSize) {
		updateList = new ArrayList<>(listSize);
		removeList = new ArrayList<>(listSize);
	}
	
	@Override
	public void processUpdate(String columnNamesCsv, CsvLine csvLine) {
		this.columnNamesCsv = columnNamesCsv; //Set to the latest value
		updateList.add(csvLine);
	}
	
	@Override
	public void processRemove(String id) {
		removeList.add(id);
	}
	
}
