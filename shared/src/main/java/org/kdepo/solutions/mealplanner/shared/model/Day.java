package org.kdepo.solutions.mealplanner.shared.model;

import java.util.Objects;

public class Day {

    private Integer dayId;
    private Integer profileId;
    private Integer weekId;
    private String name;
    private Integer orderNumber;

    public Integer getDayId() {
        return dayId;
    }

    public void setDayId(Integer dayId) {
        this.dayId = dayId;
    }

    public Integer getProfileId() {
        return profileId;
    }

    public void setProfileId(Integer profileId) {
        this.profileId = profileId;
    }

    public Integer getWeekId() {
        return weekId;
    }

    public void setWeekId(Integer weekId) {
        this.weekId = weekId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(Integer orderNumber) {
        this.orderNumber = orderNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Day day = (Day) o;
        return Objects.equals(dayId, day.dayId)
                && Objects.equals(profileId, day.profileId)
                && Objects.equals(weekId, day.weekId)
                && Objects.equals(name, day.name)
                && Objects.equals(orderNumber, day.orderNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dayId, profileId, weekId, name, orderNumber);
    }

    @Override
    public String toString() {
        return "Day{" +
                "dayId=" + dayId +
                ", profileId=" + profileId +
                ", weekId=" + weekId +
                ", name='" + name + '\'' +
                ", orderNumber=" + orderNumber +
                '}';
    }
}
