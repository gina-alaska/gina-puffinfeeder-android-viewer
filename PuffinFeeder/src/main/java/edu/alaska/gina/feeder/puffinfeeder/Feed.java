package edu.alaska.gina.feeder.puffinfeeder;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Feed {
    private boolean status;
    private String title;
    private String entries;

    public boolean getStatus() {
        return status;
    }

    public String getTitle() {
        return title;
    }

    public String getEntries() {
        return entries;
    }

    public void setStatus(String status) {
        if (status.equals("online"))
            this.status = true;
        else
            this.status = false;
    }

    public void setEntries(String entries) {
        this.entries = entries;
    }
}
