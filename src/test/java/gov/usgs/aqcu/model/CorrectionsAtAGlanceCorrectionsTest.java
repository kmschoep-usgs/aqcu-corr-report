package gov.usgs.aqcu.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.Correction;

import gov.usgs.aqcu.retrieval.CorrectionListServiceTest;

public class CorrectionsAtAGlanceCorrectionsTest {
    List<Correction> corrList;

    @Before
    public void setup() {
        corrList = new ArrayList<>();
        Correction corr1 = new Correction();
        corr1.setAppliedTimeUtc(CorrectionListServiceTest.CORR_A.getAppliedTimeUtc());
        corr1.setComment(CorrectionListServiceTest.CORR_A.getComment());
        corr1.setEndTime(CorrectionListServiceTest.CORR_A.getEndTime());
        corr1.setParameters(CorrectionListServiceTest.CORR_A.getParameters());
        corr1.setProcessingOrder(CorrectionListServiceTest.CORR_A.getProcessingOrder());
        corr1.setStartTime(CorrectionListServiceTest.CORR_A.getStartTime());
        corr1.setType(CorrectionListServiceTest.CORR_A.getType());
        corr1.setUser(CorrectionListServiceTest.CORR_A.getUser());
        
        Correction corr2 = new Correction();
        corr2.setAppliedTimeUtc(CorrectionListServiceTest.CORR_B.getAppliedTimeUtc());
        corr2.setComment(CorrectionListServiceTest.CORR_B.getComment());
        corr2.setEndTime(CorrectionListServiceTest.CORR_B.getEndTime());
        corr2.setParameters(CorrectionListServiceTest.CORR_B.getParameters());
        corr2.setProcessingOrder(CorrectionListServiceTest.CORR_B.getProcessingOrder());
        corr2.setStartTime(CorrectionListServiceTest.CORR_B.getStartTime());
        corr2.setType(CorrectionListServiceTest.CORR_B.getType());
        corr2.setUser(CorrectionListServiceTest.CORR_B.getUser());
        
        corrList.add(corr1);
        corrList.add(corr2);
    }

    @Test
	public void constructorTest() {
        CorrectionsAtAGlanceCorrections corrections = new CorrectionsAtAGlanceCorrections(corrList);
		assertEquals(corrections.getPreProcessing().size(), 0);
		assertEquals(corrections.getNormal().size(), 1);
		assertThat(corrections.getNormal(), containsInAnyOrder(corrList.get(0)));
		assertEquals(corrections.getPostProcessing().size(), 1);
		assertThat(corrections.getPostProcessing(), containsInAnyOrder(corrList.get(1)));
	}
}