package gov.usgs.aqcu.model;

import java.time.temporal.Temporal;
import java.util.List;

import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.Grade;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.Approval;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.Note;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.Qualifier;

public class TimeSeriesCorrectedData {

	private List<Approval> approvals; 
	private List<Note> notes;
	private Temporal requestedEndTime;
	private List<Grade> grades; 
	private String name;
	private List<Qualifier> qualifiers; 
	private Temporal requestedStartTime;
	private String type;
	private String unit;

	public List<Approval> getApprovals() {
		return approvals;
	}
	public void setApprovals(List<Approval> approvals) {
		this.approvals = approvals;
	}
	public List<Note> getNotes() {
		return notes;
	}
	public void setNotes(List<Note> notes) {
		this.notes = notes;
	}
	public Temporal getEndTime() {
		return requestedEndTime;
	}
	public void setEndTime(Temporal endTime) {
		this.requestedEndTime = endTime;
	}
	public List<Grade> getGrades() {
		return grades;
	}
	public void setGrades(List<Grade> grades) {
		this.grades = grades;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List<Qualifier> getQualifiers() {
		return qualifiers;
	}
	public void setQualifiers(List<Qualifier> qualifiers) {
		this.qualifiers = qualifiers;
	}
	public Temporal getStartTime() {
		return requestedStartTime;
	}
	public void setStartTime(Temporal startTime) {
		this.requestedStartTime = startTime;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getUnit() {
		return unit;
	}
	public void setUnit(String unit) {
		this.unit = unit;
	}
}
