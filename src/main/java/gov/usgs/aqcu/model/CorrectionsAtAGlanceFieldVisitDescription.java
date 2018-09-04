package gov.usgs.aqcu.model;

import java.time.Instant;

public class CorrectionsAtAGlanceFieldVisitDescription {	
	private Instant startTime;
	private Instant endTime;

	public CorrectionsAtAGlanceFieldVisitDescription() {

	}
	
	public Instant getStartTime() {
		return startTime;
	}
	
	public Instant getEndTime() {
		return endTime;
	}

	public void setStartTime(Instant val) {
		startTime = val;
	}
	
	public void setEndTime(Instant val) {
		endTime = val;
	}
}
