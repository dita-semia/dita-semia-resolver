package org.DitaSemia.Base;

public interface ProgressListener {
	void setProgress(int progress, int total);
	
	void setCachingStatistics(String[] cachingStatistics);
}
