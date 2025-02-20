package org.kdepo.solutions.mealplanner.dto;

import java.util.List;

public class ProfileDto {

    private Integer profileId;
    private String name;
    private List<WeekDto> weeks;

    public Integer getProfileId() {
        return profileId;
    }

    public void setProfileId(Integer profileId) {
        this.profileId = profileId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<WeekDto> getWeeks() {
        return weeks;
    }

    public void setWeeks(List<WeekDto> weeks) {
        this.weeks = weeks;
    }
}
