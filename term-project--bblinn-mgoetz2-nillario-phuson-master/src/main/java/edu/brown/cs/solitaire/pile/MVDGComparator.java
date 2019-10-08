package edu.brown.cs.solitaire.pile;

import java.util.Comparator;

public class MVDGComparator implements Comparator<MVDG>{

	/* (non-Javadoc)
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	@Override
	public int compare(MVDG o1, MVDG o2) {
		if(o1.getValue() > o2.getValue()) {
			return -1;
			
		}else if(o1.getValue() < o2.getValue()) {
			return 1;
		}else {
			return 0;
		}
	}
	
	

}
