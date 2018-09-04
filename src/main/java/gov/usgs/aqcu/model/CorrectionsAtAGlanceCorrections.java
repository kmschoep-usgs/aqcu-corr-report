package gov.usgs.aqcu.model;

import java.util.List;

import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.CorrectionProcessingOrder;

import java.util.ArrayList;

public class CorrectionsAtAGlanceCorrections {	
	private List<ExtendedCorrection> preProcessing;
	private List<ExtendedCorrection> normal;
	private List<ExtendedCorrection> postProcessing;

	public CorrectionsAtAGlanceCorrections(List<ExtendedCorrection> correctionList) {
		preProcessing = new ArrayList<>();
		normal = new ArrayList<>();
		postProcessing = new ArrayList<>();

		if(!correctionList.isEmpty()) {
			for(ExtendedCorrection corr : correctionList) {
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
	
	public List<ExtendedCorrection> getPreProcessing() {
		return preProcessing;
	}
	
	public List<ExtendedCorrection> getNormal() {
		return normal;
	}
	
	public List<ExtendedCorrection> getPostProcessing() {
		return postProcessing;
	}

	public void setPreProcessing(List<ExtendedCorrection> val) {
		preProcessing = val;
	}
	
	public void setNormal(List<ExtendedCorrection> val) {
		normal = val;
	}
	
	public void setPostProcessing(List<ExtendedCorrection> val) {
		postProcessing = val;
	}
}
