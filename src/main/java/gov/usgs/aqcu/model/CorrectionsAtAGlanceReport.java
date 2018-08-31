package gov.usgs.aqcu.model;

import java.util.List;

import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.TimeSeriesThreshold;

public class CorrectionsAtAGlanceReport {	
	private TimeSeriesCorrectedData primarySeries;
	private List<TimeSeriesThreshold> thresholds;
	private CorrectionsAtAGlanceCorrections corrections;
	private List<CorrectionsAtAGlanceFieldVisitDescription> fieldVisits;
	private CorrectionsAtAGlanceReportMetadata reportMetadata;
	
	public CorrectionsAtAGlanceReportMetadata getReportMetadata() {
		return reportMetadata;
	}
	
	public CorrectionsAtAGlanceCorrections getCorrections() {
		return corrections;
	}
	
	public TimeSeriesCorrectedData getPrimaryTsData() {
		return primarySeries;
	}
	
	public List<TimeSeriesThreshold> getThresholds() {
		return thresholds;
	}
	
	public List<CorrectionsAtAGlanceFieldVisitDescription> getFieldVisits() {
		return fieldVisits;
	}
	
	public void setReportMetadata(CorrectionsAtAGlanceReportMetadata val) {
		reportMetadata = val;
	}
	
	public void setCorrections(CorrectionsAtAGlanceCorrections val) {
		corrections = val;
	}
	
	public void setPrimaryTsData(TimeSeriesCorrectedData val) {
		primarySeries = val;
	}
	
	public void setThresholds(List<TimeSeriesThreshold> val) {
		thresholds = val;
	}
	
	public void setFieldVisits(List<CorrectionsAtAGlanceFieldVisitDescription> val) {
		fieldVisits = val;
	}
}
	
