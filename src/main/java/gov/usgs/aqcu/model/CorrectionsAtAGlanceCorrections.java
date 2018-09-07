package gov.usgs.aqcu.model;

import java.util.List;

import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.Correction;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.CorrectionProcessingOrder;

import java.util.ArrayList;

public class CorrectionsAtAGlanceCorrections {	
	private List<Correction> preProcessing;
	private List<Correction> normal;
	private List<Correction> postProcessing;

	public CorrectionsAtAGlanceCorrections(List<Correction> correctionList) {
		preProcessing = new ArrayList<>();
		normal = new ArrayList<>();
		postProcessing = new ArrayList<>();

		if(!correctionList.isEmpty()) {
			for(Correction corr : correctionList) {
				if(corr.getProcessingOrder() == CorrectionProcessingOrder.PreProcessing) {
					preProcessing.add(corr);
				} else if(corr.getProcessingOrder() == CorrectionProcessingOrder.Normal) {
					normal.add(corr);
				} else if(corr.getProcessingOrder() == CorrectionProcessingOrder.PostProcessing) {
					postProcessing.add(corr);
				}
			}
		}

	}
	
	public CorrectionsAtAGlanceCorrections() {
		preProcessing = new ArrayList<>();
		normal = new ArrayList<>();
		postProcessing  = new ArrayList<>();
	}
	
	public List<Correction> getPreProcessing() {
		return preProcessing;
	}
	
	public List<Correction> getNormal() {
		return normal;
	}
	
	public List<Correction> getPostProcessing() {
		return postProcessing;
	}

	public void setPreProcessing(List<Correction> val) {
		preProcessing = val;
	}
	
	public void setNormal(List<Correction> val) {
		normal = val;
	}
	
	public void setPostProcessing(List<Correction> val) {
		postProcessing = val;
	}
}
