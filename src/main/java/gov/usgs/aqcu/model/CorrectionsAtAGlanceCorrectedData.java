package gov.usgs.aqcu.model;

import java.util.List;
import java.util.ArrayList;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.TimeSeriesDataServiceResponse;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.Processor;

public class CorrectionsAtAGlanceCorrectedData extends TimeSeriesDataServiceResponse {
	private List<DataGap> gaps;

	public CorrectionsAtAGlanceCorrectedData(TimeSeriesDataServiceResponse response, List<DataGap> gapList) {
		setApprovals(response.getApprovals());
		setQualifiers(response.getQualifiers());
		setNotes(response.getNotes());
		setMethods(response.getMethods());
		setGapTolerances(response.getGapTolerances());
		setInterpolationTypes(response.getInterpolationTypes());
		setGrades(response.getGrades());
		setGaps(gapList);
	}
	
	public CorrectionsAtAGlanceCorrectedData() {
		gaps = new ArrayList<>();
	}
	
	public List<DataGap> getGaps() {
		return gaps;
	}
	
	public void setGaps(List<DataGap> val) {
		gaps = val;
	}
}
