package gov.usgs.aqcu.retrieval;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.TimeSeriesDataServiceResponse;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.Note;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.Qualifier;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.StatisticalDateTimeOffset;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.StatisticalTimeRange;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.Approval;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.Grade;

import net.servicestack.client.IReturn;

@RunWith(SpringRunner.class)
public class TimeSeriesDataCorrectedServiceTest {
    
	@MockBean
	private AquariusRetrievalService aquariusService;
	private TimeSeriesDataCorrectedService service;
	private static final ArrayList<Approval> approvals = new ArrayList<>(Arrays.asList(
		new Approval()
			.setApprovalLevel(1)
			.setComment("test-1")
			.setDateAppliedUtc(Instant.parse("2017-01-01T00:00:00Z"))
			.setLevelDescription("desc-1")
			.setUser("user-1"),
		new Approval()
			.setApprovalLevel(2)
			.setComment("test-2")
			.setDateAppliedUtc(Instant.parse("2017-01-01T00:00:00Z"))
			.setLevelDescription("desc-2")
			.setUser("user-2")
	));
	
	private static final Grade gradeA = new Grade().setGradeCode("1.0");
	private static final Grade gradeB = new Grade().setGradeCode("2.0");
	
	private static final Note noteA = new Note().setNoteText("note text");
	
	private static final Qualifier qualifierA = new Qualifier().setIdentifier("a");
	private static final Qualifier qualifierB = new Qualifier().setIdentifier("b");
	private static final Qualifier qualifierC = new Qualifier().setIdentifier("c");
	
	public static final TimeSeriesDataServiceResponse TS_DATA_RESPONSE = new TimeSeriesDataServiceResponse()
		.setApprovals(approvals)
		.setGrades(new ArrayList<Grade>(Arrays.asList(gradeA, gradeB)))
		.setLabel("label")
		.setLocationIdentifier("loc-id")
		.setNotes(new ArrayList<Note>(Arrays.asList(noteA)))
		.setParameter("param")
		.setQualifiers(new ArrayList<Qualifier>(Arrays.asList(qualifierA, qualifierB, qualifierC)))
		.setTimeRange(new StatisticalTimeRange()
			.setStartTime(new StatisticalDateTimeOffset().setDateTimeOffset(Instant.parse("2017-01-01T00:00:00Z")))
			.setEndTime(new StatisticalDateTimeOffset().setDateTimeOffset(Instant.parse("2017-03-01T00:00:00Z"))))
		.setUniqueId("uuid")
		.setUnit("unit")
		;	

    @Before
	@SuppressWarnings("unchecked")
	public void setup() {
		service = new TimeSeriesDataCorrectedService(aquariusService);
		given(aquariusService.executePublishApiRequest(any(IReturn.class))).willReturn(TS_DATA_RESPONSE);
	}

	@Test
	public void getRawResponseTest() {
		TimeSeriesDataServiceResponse result = service.getRawResponse("tsid", Instant.parse("2017-01-01T00:00:00Z"), Instant.parse("2017-03-01T00:00:00Z"));
		assertEquals(result, TS_DATA_RESPONSE);
	}
	
	@Test
	public void getQualifierTest() throws Exception {
		TimeSeriesDataServiceResponse actual = service.getRawResponse("", null, null);
		assertEquals(3, actual.getQualifiers().size());
		assertThat(actual.getQualifiers(), containsInAnyOrder(qualifierA, qualifierB, qualifierC));
	}
	
	@Test
	public void getGradeTest() throws Exception {
		TimeSeriesDataServiceResponse actual = service.getRawResponse("", null, null);
		assertEquals(2, actual.getGrades().size());
		assertThat(actual.getGrades(), containsInAnyOrder(gradeA, gradeB));
	}
}