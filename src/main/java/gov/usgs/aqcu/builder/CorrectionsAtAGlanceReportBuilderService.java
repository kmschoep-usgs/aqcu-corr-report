package gov.usgs.aqcu.builder;

import java.util.List;
import java.time.ZoneOffset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.Qualifier;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.Grade;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.TimeSeriesDescription;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.TimeSeriesDataServiceResponse;

import gov.usgs.aqcu.parameter.CorrectionsAtAGlanceRequestParameters;
import gov.usgs.aqcu.util.TimeSeriesUtils;
import gov.usgs.aqcu.model.*;
import gov.usgs.aqcu.retrieval.*;

@Service
public class CorrectionsAtAGlanceReportBuilderService {
	public static final String REPORT_TITLE = "Corrections at a Glance";
	public static final String REPORT_TYPE = "correctionsataglance";

	private static final Logger LOG = LoggerFactory.getLogger(CorrectionsAtAGlanceReportBuilderService.class);

	private DataGapListBuilderService dataGapListBuilderService;
	private ReportUrlBuilderService reportUrlBuilderService;
	private GradeLookupService gradeLookupService;
	private QualifierLookupService qualifierLookupService;
	private LocationDescriptionListService locationDescriptionListService;
	private TimeSeriesDescriptionListService timeSeriesDescriptionListService;
	private TimeSeriesDataCorrectedService timeSeriesDataCorrectedService;
	private CorrectionListService correctionListService;

	@Autowired
	public CorrectionsAtAGlanceReportBuilderService(
		DataGapListBuilderService dataGapListBuilderService,
		ReportUrlBuilderService reportUrlBuilderService,
		GradeLookupService gradeLookupService, 
		QualifierLookupService qualifierLookupService,
		LocationDescriptionListService locationDescriptionListService,
		TimeSeriesDescriptionListService timeSeriesDescriptionListService,
		TimeSeriesDataCorrectedService timeSeriesDataCorrectedService,
		CorrectionListService correctionListService) {
		this.timeSeriesDataCorrectedService = timeSeriesDataCorrectedService;
		this.correctionListService = correctionListService;
		this.dataGapListBuilderService = dataGapListBuilderService;
		this.reportUrlBuilderService = reportUrlBuilderService;
		this.gradeLookupService = gradeLookupService;
		this.qualifierLookupService = qualifierLookupService;
		this.locationDescriptionListService = locationDescriptionListService;
		this.timeSeriesDescriptionListService = timeSeriesDescriptionListService;
	}

	public CorrectionsAtAGlanceReport buildReport(CorrectionsAtAGlanceRequestParameters requestParameters, String requestingUser) {
		CorrectionsAtAGlanceReport report = new CorrectionsAtAGlanceReport();

		//Primary TS Metadata
		TimeSeriesDescription primaryDescription = timeSeriesDescriptionListService.getTimeSeriesDescription(requestParameters.getPrimaryTimeseriesIdentifier());
		ZoneOffset primaryZoneOffset = TimeSeriesUtils.getZoneOffset(primaryDescription);
		String primaryStationId = primaryDescription.getLocationIdentifier();
		report.setPrimaryTsMetadata(primaryDescription);

		//Primary TS Data
		report.setPrimaryTsData(getCorrectedData(requestParameters, primaryZoneOffset, TimeSeriesUtils.isDailyTimeSeries(primaryDescription)));
		
		//Corrections Data
		report.setCorrections(getCorrectionsData(requestParameters, primaryZoneOffset, primaryStationId));

		//Report Metadata
		report.setReportMetadata(getReportMetadata(requestParameters,
			requestingUser,
			report.getPrimaryTsMetadata().getLocationIdentifier(), 
			report.getPrimaryTsMetadata().getIdentifier(),
			report.getPrimaryTsMetadata().getUtcOffset(),
			report.getPrimaryTsData().getGrades(), 
			report.getPrimaryTsData().getQualifiers()
		));

		return report;
	}

	protected CorrectionsAtAGlanceCorrections getCorrectionsData(CorrectionsAtAGlanceRequestParameters requestParameters, ZoneOffset primaryZoneOffset, String stationId) {
		String corrUrl = reportUrlBuilderService.buildAqcuReportUrl("correctionsataglance", stationId, requestParameters, null);
		List<ExtendedCorrection> correctionList = correctionListService.getExtendedCorrectionList(
			requestParameters.getPrimaryTimeseriesIdentifier(), 
			requestParameters.getStartInstant(primaryZoneOffset), 
			requestParameters.getEndInstant(primaryZoneOffset), 
			requestParameters.getExcludedCorrections());

		return new CorrectionsAtAGlanceCorrections(correctionList, corrUrl);
	}

	protected CorrectionsAtAGlanceCorrectedData getCorrectedData(CorrectionsAtAGlanceRequestParameters requestParameters, ZoneOffset primaryZoneOffset, boolean isDVSeries) {
		//Fetch Corrected Data
		TimeSeriesDataServiceResponse dataResponse = timeSeriesDataCorrectedService.getRawResponse(
			requestParameters.getPrimaryTimeseriesIdentifier(), 
			requestParameters.getStartInstant(primaryZoneOffset), 
			requestParameters.getEndInstant(primaryZoneOffset));

		//Calculate Data Gaps
		List<DataGap> gapList = dataGapListBuilderService.buildGapList(dataResponse.getPoints(), isDVSeries, primaryZoneOffset);

		return new CorrectionsAtAGlanceCorrectedData(dataResponse, gapList);
	}

	protected CorrectionsAtAGlanceReportMetadata getReportMetadata(CorrectionsAtAGlanceRequestParameters requestParameters, String requestingUser, String stationId, String primaryParameter, Double utcOffset, List<Grade> gradeList, List<Qualifier> qualifierList) {
		CorrectionsAtAGlanceReportMetadata metadata = new CorrectionsAtAGlanceReportMetadata();
		metadata.setTitle(REPORT_TITLE);
		metadata.setRequestingUser(requestingUser);
		metadata.setRequestParameters(requestParameters);
		metadata.setStationId(stationId);
		metadata.setStationName(locationDescriptionListService.getByLocationIdentifier(stationId).getName());
		metadata.setTimezone(utcOffset);
		metadata.setPrimaryParameter(primaryParameter);

		if(gradeList != null && !gradeList.isEmpty()) {
			metadata.setGradeMetadata(gradeLookupService.getByGradeList(gradeList));
		}
		
		if(qualifierList != null && !qualifierList.isEmpty()) {
			metadata.setQualifierMetadata(qualifierLookupService.getByQualifierList(qualifierList));
		}
		
		return metadata;
	}
}