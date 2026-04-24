package model;

import java.util.List;

public class ApiResponse {
    private String regNo;
    private String setId;
    private int pollIndex;
    private List<Event> events;

    public ApiResponse() {}

    public String      getRegNo()      { return regNo; }
    public String      getSetId()      { return setId; }
    public int         getPollIndex()  { return pollIndex; }
    public List<Event> getEvents()     { return events; }

    public void setRegNo(String regNo)         { this.regNo = regNo; }
    public void setSetId(String setId)         { this.setId = setId; }
    public void setPollIndex(int pollIndex)     { this.pollIndex = pollIndex; }
    public void setEvents(List<Event> events)  { this.events = events; }
}
