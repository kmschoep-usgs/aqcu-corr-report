package gov.usgs.aqcu.model;

import java.util.List;
import java.util.ArrayList;

import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.TimeSeriesDescription;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.RatingCurve;

public class CorrectionsAtAGlanceReport {	
	private CorrectionsAtAGlanceCorrectedData primaryTsData;
	private TimeSeriesDescription primaryTsMetadata;
	private CorrectionsAtAGlanceCorrections corrections;
	private CorrectionsAtAGlanceReportMetadata reportMetadata;
	
	
	public CorrectionsAtAGlanceReport() {
		primaryTsData = new CorrectionsAtAGlanceCorrectedData();
		primaryTsMetadata = new TimeSeriesDescription();
		corrections = new CorrectionsAtAGlanceCorrections();
		reportMetadata = new CorrectionsAtAGlanceReportMetadata();
	}
	
	public CorrectionsAtAGlanceReportMetadata getReportMetadata() {
		return reportMetadata;
	}
	
	public CorrectionsAtAGlanceCorrections getCorrections() {
		return corrections;
	}
	
	public CorrectionsAtAGlanceCorrectedData getPrimaryTsData() {
		return primaryTsData;
	}
	
	public TimeSeriesDescription getPrimaryTsMetadata() {
		return primaryTsMetadata;
	}
	
	public void setReportMetadata(CorrectionsAtAGlanceReportMetadata val) {
		reportMetadata = val;
	}
	
	public void setCorrections(CorrectionsAtAGlanceCorrections val) {
		corrections = val;
	}
	
	public void setPrimaryTsData(CorrectionsAtAGlanceCorrectedData val) {
		primaryTsData = val;
	}
	
	public void setPrimaryTsMetadata(TimeSeriesDescription val) {
		primaryTsMetadata = val;
	}
}
	
