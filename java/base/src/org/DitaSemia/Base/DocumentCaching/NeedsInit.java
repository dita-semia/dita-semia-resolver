package org.DitaSemia.Base.DocumentCaching;

import java.util.Comparator;

public interface NeedsInit {
	
	public void init();
	
	public default int getPriority() {
		return 0;
	}
	
	public static class PriorityComparator implements Comparator<NeedsInit> {

		@Override
		public int compare(NeedsInit arg0, NeedsInit arg1) {
			return Integer.compare(arg0.getPriority(), arg1.getPriority());
		}
		
	}

}
