package gov.usgs.aqcu.builder;

import java.util.ArrayList;
import java.util.List;
import java.time.ZoneOffset;

import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.Qualifier;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.FieldVisitDescription;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.Grade;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.TimeSeriesDescription;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.TimeSeriesDataServiceResponse;

import gov.usgs.aqcu.parameter.CorrectionsAtAGlanceRequestParameters;
import gov.usgs.aqcu.util.AqcuTimeUtils;
import gov.usgs.aqcu.util.LogExecutionTime;
import gov.usgs.aqcu.util.TimeSeriesUtils;
import gov.usgs.aqcu.model.*;
import gov.usgs.aqcu.retrieval.*;

@Service
public class CorrectionsAtAGlanceReportBuilderService {
	private Logger log = LoggerFactory.getLogger(CorrectionsAtAGlanceReportBuilderService.class);
	public static final String REPORT_TITLE = "Corrections at a Glance";
	public static final String REPORT_TYPE = "correctionsataglance";

	private GradeLookupService gradeLookupService;
	private QualifierLookupService qualifierLookupService;
	private LocationDescriptionListService locationDescriptionListService;
	private TimeSeriesDescriptionListService timeSeriesDescriptionListService;
	private TimeSeriesDataService timeSeriesDataService;
	private CorrectionListService correctionListService;
	private FieldVisitDescriptionService fieldVisitDescriptionService;

	@Autowired
	public CorrectionsAtAGlanceReportBuilderService(
		GradeLookupService gradeLookupService, 
		QualifierLookupService qualifierLookupService,
		LocationDescriptionListService locationDescriptionListService,
		TimeSeriesDescriptionListService timeSeriesDescriptionListService,
		TimeSeriesDataService timeSeriesDataService,
		CorrectionListService correctionListService,
		FieldVisitDescriptionService  fieldVisitDescriptionService ) {
		this.timeSeriesDataService = timeSeriesDataService;
		this.correctionListService = correctionListService;
		this.gradeLookupService = gradeLookupService;
		this.qualifierLookupService = qualifierLookupService;
		this.locationDescriptionListService = locationDescriptionListService;
		this.timeSeriesDescriptionListService = timeSeriesDescriptionListService;
		this.fieldVisitDescriptionService = fieldVisitDescriptionService;
	}
	
	@LogExecutionTime
	public CorrectionsAtAGlanceReport buildReport(CorrectionsAtAGlanceRequestParameters requestParameters, String requestingUser) {
		CorrectionsAtAGlanceReport report = new CorrectionsAtAGlanceReport();
		//Primary TS Metadata
		log.debug("Get time series descriptions");
		TimeSeriesDescription primaryDescription = timeSeriesDescriptionListService.getTimeSeriesDescription(requestParameters.getPrimaryTimeseriesIdentifier());
		ZoneOffset primaryZoneOffset = TimeSeriesUtils.getZoneOffset(primaryDescription);
		String primaryStationId = primaryDescription.getLocationIdentifier();

		//Primary TS Data
		log.debug("Get primary time series data points");
		report.setPrimaryTsData(getCorrectedData(requestParameters, primaryZoneOffset, TimeSeriesUtils.isDailyTimeSeries(primaryDescription)));
		
		//Thresholds
		log.debug("Get primary time series thresholds");
		report.setThresholds(primaryDescription.getThresholds());
		
		//Corrections Data
		log.debug("Get primary time series corrections");
		report.setCorrections(getCorrectionsData(requestParameters, primaryZoneOffset, primaryStationId));
		
		//Field Visits
		log.debug("Get primary time series field visits");
		report.setFieldVisits(getFieldVisits(primaryStationId, primaryZoneOffset, requestParameters));
		
		//Report Metadata
		log.debug("Set report metadata");
		report.setReportMetadata(getReportMetadata(requestParameters,
			requestingUser,
			primaryDescription.getLocationIdentifier(), 
			primaryDescription.getIdentifier(),
			primaryDescription.getUtcOffset(),
			report.getPrimaryTsData().getGrades(), 
			report.getPrimaryTsData().getQualifiers()
		));

		return report;
	}

	protected CorrectionsAtAGlanceCorrections getCorrectionsData(CorrectionsAtAGlanceRequestParameters requestParameters, ZoneOffset primaryZoneOffset, String stationId) {
		List<ExtendedCorrection> correctionList = correctionListService.getExtendedCorrectionList(
			requestParameters.getPrimaryTimeseriesIdentifier(), 
			requestParameters.getStartInstant(primaryZoneOffset), 
			requestParameters.getEndInstant(primaryZoneOffset), 
			requestParameters.getExcludedCorrections());

		return new CorrectionsAtAGlanceCorrections(correctionList);
	}

	protected TimeSeriesCorrectedData createTimeSeriesCorrectedData(CorrectionsAtAGlanceRequestParameters requestParameters,
			TimeSeriesDataServiceResponse timeSeriesDataServiceResponse, boolean isDaily, 
			ZoneOffset zoneOffset) {
		TimeSeriesCorrectedData timeSeriesCorrectedData = new TimeSeriesCorrectedData();

		if (timeSeriesDataServiceResponse.getTimeRange() != null) {
			timeSeriesCorrectedData.setStartTime(AqcuTimeUtils
					.getTemporal(timeSeriesDataServiceResponse.getTimeRange().getStartTime(), isDaily, zoneOffset));
			timeSeriesCorrectedData.setEndTime(AqcuTimeUtils
					.getTemporal(timeSeriesDataServiceResponse.getTimeRange().getEndTime(), isDaily, zoneOffset));
		}
		timeSeriesCorrectedData.setNotes(timeSeriesDataServiceResponse.getNotes());
		timeSeriesCorrectedData.setUnit(timeSeriesDataServiceResponse.getUnit());
		timeSeriesCorrectedData.setType(timeSeriesDataServiceResponse.getParameter());

		timeSeriesCorrectedData.setApprovals(timeSeriesDataServiceResponse.getApprovals());
		timeSeriesCorrectedData.setQualifiers(timeSeriesDataServiceResponse.getQualifiers());
		timeSeriesCorrectedData.setGrades(timeSeriesDataServiceResponse.getGrades());
		// Repgen just pulls the date for the headings, so we need to be sure and get
		// the "correct" date - its internal filtering is potentially slightly skewed
		// by this.
		timeSeriesCorrectedData.setStartTime(requestParameters.getStartInstant(ZoneOffset.UTC));
		timeSeriesCorrectedData.setEndTime(requestParameters.getEndInstant(ZoneOffset.UTC));

		return timeSeriesCorrectedData;
	}
	protected TimeSeriesCorrectedData getCorrectedData(CorrectionsAtAGlanceRequestParameters requestParameters, ZoneOffset primaryZoneOffset, boolean isDaily) {
		TimeSeriesCorrectedData timeSeriesCorrectedData = null;

		//Fetch Corrected Data
		TimeSeriesDataServiceResponse dataResponse = timeSeriesDataService.get(
			requestParameters.getPrimaryTimeseriesIdentifier(), 
			requestParameters,
			primaryZoneOffset,
			isDaily, false, true, null
		);

		if (dataResponse != null) {
			timeSeriesCorrectedData = createTimeSeriesCorrectedData(requestParameters, dataResponse, isDaily, primaryZoneOffset);
		}

		return timeSeriesCorrectedData;
	}
	
	protected List<CorrectionsAtAGlanceFieldVisitDescription> getFieldVisits(String stationId, ZoneOffset zoneOffset, CorrectionsAtAGlanceRequestParameters requestParameters) {
		List<FieldVisitDescription> fieldVisitList =  fieldVisitDescriptionService.getDescriptions(stationId, zoneOffset, requestParameters);

		//Create stripped-down field visits
		List<CorrectionsAtAGlanceFieldVisitDescription> fieldVisits = new ArrayList<>();
		for(FieldVisitDescription fieldVisit : fieldVisitList) {
			{
			CorrectionsAtAGlanceFieldVisitDescription newFieldVisit = new CorrectionsAtAGlanceFieldVisitDescription();
			newFieldVisit.setStartTime(fieldVisit.StartTime);
			newFieldVisit.setEndTime(fieldVisit.EndTime);
			fieldVisits.add(newFieldVisit);
			}
		}

		return fieldVisits;
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