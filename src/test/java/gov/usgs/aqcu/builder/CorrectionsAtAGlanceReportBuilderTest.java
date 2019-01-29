package gov.usgs.aqcu.builder;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import gov.usgs.aqcu.model.ExtendedCorrection;
import gov.usgs.aqcu.model.TimeSeriesCorrectedData;
import gov.usgs.aqcu.model.CorrectionsAtAGlanceCorrections;
import gov.usgs.aqcu.model.CorrectionsAtAGlanceReport;
import gov.usgs.aqcu.model.CorrectionsAtAGlanceReportMetadata;
import gov.usgs.aqcu.parameter.CorrectionsAtAGlanceRequestParameters;
import gov.usgs.aqcu.retrieval.AquariusRetrievalService;
import gov.usgs.aqcu.retrieval.CorrectionListService;
import gov.usgs.aqcu.retrieval.CorrectionListServiceTest;
import gov.usgs.aqcu.retrieval.FieldVisitDescriptionService;
import gov.usgs.aqcu.retrieval.GradeLookupService;
import gov.usgs.aqcu.retrieval.LocationDescriptionListService;
import gov.usgs.aqcu.retrieval.QualifierLookupService;
import gov.usgs.aqcu.retrieval.TimeSeriesDataService;
import gov.usgs.aqcu.retrieval.TimeSeriesDataServiceTest;
import gov.usgs.aqcu.retrieval.TimeSeriesDescriptionListService;
import gov.usgs.aqcu.retrieval.TimeSeriesDescriptionListServiceTest;

import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.Correction;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.LocationDescription;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.TimeSeriesDataServiceResponse;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.TimeSeriesDescription;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
public class CorrectionsAtAGlanceReportBuilderTest {
	@Value("${aqcu.reports.webservice}")
	String aqcuWebserviceUrl;
    
	@MockBean
	private AquariusRetrievalService aquariusService;
	@MockBean
	private TimeSeriesDescriptionListService descService;
	@MockBean
	private TimeSeriesDataService tsDataService;
	@MockBean
	private FieldVisitDescriptionService fieldVisitDescriptionService;
	@MockBean
	private CorrectionListService corrListService;
	@MockBean
	private GradeLookupService gradeService;
	@MockBean
	private QualifierLookupService qualService;
	@MockBean
	private LocationDescriptionListService locService;

	private CorrectionsAtAGlanceReportBuilderService service;
	private final String REQUESTING_USER = "test-user";
	private CorrectionsAtAGlanceRequestParameters requestParams;
	CorrectionsAtAGlanceReportMetadata metadata;
	TimeSeriesDescription primaryDesc = TimeSeriesDescriptionListServiceTest.DESC_1;
	TimeSeriesDataServiceResponse primaryData;
	List<ExtendedCorrection> extCorrs;
	LocationDescription primaryLoc = new LocationDescription().setIdentifier(primaryDesc.getLocationIdentifier()).setName("loc-name");

	@Before
	public void setup() {
		//Builder Services
		service = new CorrectionsAtAGlanceReportBuilderService(gradeService, qualService, locService, descService, tsDataService, corrListService, fieldVisitDescriptionService);

		//Request Parameters
		requestParams = new CorrectionsAtAGlanceRequestParameters();
		requestParams.setStartDate(LocalDate.parse("2017-01-01"));
		requestParams.setEndDate(LocalDate.parse("2017-02-01"));
		requestParams.setPrimaryTimeseriesIdentifier(primaryDesc.getUniqueId());
		requestParams.setExcludedCorrections(Arrays.asList("corr1", "corr2"));
		primaryData = TimeSeriesDataServiceTest.buildData();

		//Metadata
		metadata = new CorrectionsAtAGlanceReportMetadata();
		metadata.setPrimaryParameter(primaryDesc.getIdentifier());
		metadata.setRequestParameters(requestParams);
		metadata.setStationId(primaryDesc.getLocationIdentifier());
		metadata.setStationName(primaryLoc.getName());
		metadata.setTimezone(primaryDesc.getUtcOffset());
		metadata.setTitle(CorrectionsAtAGlanceReportBuilderService.REPORT_TITLE);

		//Corrections
		extCorrs = new ArrayList<>();
		for(Correction corr : CorrectionListServiceTest.CORR_LIST) {
			extCorrs.add(new ExtendedCorrection(corr));
		}
	}
	
	@Test
	@SuppressWarnings("unchecked")
	public void buildReportBasicTest() {
		given(descService.getTimeSeriesDescription(any(String.class)))
			.willReturn(primaryDesc);
		given(descService.getTimeSeriesDescriptionList(any(List.class)))
			.willReturn(TimeSeriesDescriptionListServiceTest.DESC_LIST);
		given(tsDataService.get(requestParams.getPrimaryTimeseriesIdentifier(), requestParams, ZoneOffset.UTC, false, false, true, null))
			.willReturn(primaryData);
		given(corrListService.getExtendedCorrectionList(requestParams.getPrimaryTimeseriesIdentifier(), requestParams.getStartInstant(ZoneOffset.UTC), requestParams.getEndInstant(ZoneOffset.UTC), requestParams.getExcludedCorrections()))
			.willReturn(extCorrs);
		given(locService.getByLocationIdentifier(metadata.getStationId()))
			.willReturn(primaryLoc);
		given(gradeService.getByGradeList(any(ArrayList.class)))
			.willReturn(new HashMap<>());
		given(qualService.getByQualifierList(any(ArrayList.class)))
			.willReturn(new HashMap<>());
		
		CorrectionsAtAGlanceReport report = service.buildReport(requestParams, REQUESTING_USER);
		assertTrue(report != null);
		assertTrue(report.getReportMetadata() != null);
		assertEquals(report.getReportMetadata().getRequestingUser(), REQUESTING_USER);
		assertEquals(report.getReportMetadata().getPrimaryTimeSeriesIdentifier(), metadata.getPrimaryTimeSeriesIdentifier());
		assertEquals(report.getReportMetadata().getRequestParameters(), metadata.getRequestParameters());
		assertEquals(report.getReportMetadata().getStartDate(), metadata.getStartDate());
		assertEquals(report.getReportMetadata().getEndDate(), metadata.getEndDate());		
		assertEquals(report.getCorrections().getPreProcessing().size(), 0);
		assertEquals(report.getCorrections().getNormal().size(), 1);
		assertThat(report.getCorrections().getNormal(), containsInAnyOrder(extCorrs.get(0)));
		assertEquals(report.getCorrections().getPostProcessing().size(), 1);
		assertThat(report.getCorrections().getPostProcessing(), containsInAnyOrder(extCorrs.get(1)));
		assertEquals(report.getPrimaryTsData().getApprovals(), primaryData.getApprovals());
		assertEquals(report.getPrimaryTsData().getGrades(), primaryData.getGrades());
		assertEquals(report.getPrimaryTsData().getNotes(), primaryData.getNotes());
		assertEquals(report.getPrimaryTsData().getQualifiers(), primaryData.getQualifiers());
		assertEquals(report.getReportMetadata().getStationId(), primaryDesc.getLocationIdentifier());
		assertEquals(report.getReportMetadata().getPrimaryParameter(), primaryDesc.getIdentifier());
		assertEquals(report.getReportMetadata().getTimezone(), metadata.getTimezone());
		assertEquals(report.getReportMetadata().getStationName(), metadata.getStationName());
		assertEquals(report.getReportMetadata().getGradeMetadata(), new HashMap<>());
		assertEquals(report.getReportMetadata().getQualifierMetadata(), new HashMap<>());
		assertEquals(report.getThresholds(), TimeSeriesDescriptionListServiceTest.DESC_1.getThresholds());
	}

	@Test
	public void getCorrectionDataTest() {
		given(corrListService.getExtendedCorrectionList(requestParams.getPrimaryTimeseriesIdentifier(), requestParams.getStartInstant(ZoneOffset.UTC), requestParams.getEndInstant(ZoneOffset.UTC), requestParams.getExcludedCorrections()))
			.willReturn(extCorrs);

		CorrectionsAtAGlanceCorrections corrs = service.getCorrectionsData(requestParams, ZoneOffset.UTC, metadata.getStationId());
		assertEquals(corrs.getPreProcessing().size(), 0);
		assertEquals(corrs.getNormal().size(), 1);
		assertThat(corrs.getNormal(), containsInAnyOrder(extCorrs.get(0)));
		assertEquals(corrs.getPostProcessing().size(), 1);
		assertThat(corrs.getPostProcessing(), containsInAnyOrder(extCorrs.get(1)));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void getReportMetadataTest() {
		given(gradeService.getByGradeList(any(ArrayList.class)))
			.willReturn(new HashMap<>());
		given(qualService.getByQualifierList(any(ArrayList.class)))
			.willReturn(new HashMap<>());
		given(locService.getByLocationIdentifier(metadata.getStationId()))
			.willReturn(primaryLoc);

		CorrectionsAtAGlanceReportMetadata newMetadata = service.getReportMetadata(requestParams, REQUESTING_USER, primaryLoc.getIdentifier(), primaryDesc.getIdentifier(), primaryDesc.getUtcOffset(), new ArrayList<>(), new ArrayList<>());
		assertTrue(newMetadata != null);
		assertEquals(newMetadata.getRequestingUser(), REQUESTING_USER);
		assertEquals(newMetadata.getPrimaryTimeSeriesIdentifier(), metadata.getPrimaryTimeSeriesIdentifier());
		assertEquals(newMetadata.getRequestParameters(), metadata.getRequestParameters());
		assertEquals(newMetadata.getStartDate(), metadata.getStartDate());
		assertEquals(newMetadata.getEndDate(), metadata.getEndDate());
		assertEquals(newMetadata.getStationId(), primaryDesc.getLocationIdentifier());
		assertEquals(newMetadata.getStationName(), primaryLoc.getName());
		assertEquals(newMetadata.getPrimaryParameter(), primaryDesc.getIdentifier());
		assertEquals(newMetadata.getTimezone(), metadata.getTimezone());
		assertEquals(newMetadata.getGradeMetadata(), new HashMap<>());
		assertEquals(newMetadata.getQualifierMetadata(), new HashMap<>());
	}

	@Test
	public void getCorrectedDataTest() {
		given(tsDataService.get(requestParams.getPrimaryTimeseriesIdentifier(), requestParams, ZoneOffset.UTC, false, false, true, null))
			.willReturn(primaryData);
		TimeSeriesCorrectedData corrData = service.getCorrectedData(requestParams, ZoneOffset.UTC, false);
		assertTrue(corrData != null);
		assertEquals(corrData.getApprovals(), primaryData.getApprovals());
		assertEquals(corrData.getGrades(), primaryData.getGrades());
		assertEquals(corrData.getNotes(), primaryData.getNotes());
		assertEquals(corrData.getQualifiers(), primaryData.getQualifiers());
	}

}
