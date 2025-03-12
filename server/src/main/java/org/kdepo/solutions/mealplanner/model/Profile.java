package org.kdepo.solutions.mealplanner.model;

import java.util.Objects;

public class Profile {

    private Integer profileId;
    private String name;
    private Integer orderNumber;

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

    public Integer getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(Integer orderNumber) {
        this.orderNumber = orderNumber;
    }

    @Override
    public String toString() {
        return "Profile{" +
                "profileId=" + profileId +
                ", name='" + name + '\'' +
                ", orderNumber=" + orderNumber +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Profile profile = (Profile) o;
        return Objects.equals(profileId, profile.profileId)
                && Objects.equals(name, profile.name)
                && Objects.equals(orderNumber, profile.orderNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(profileId, name, orderNumber);
    }
}
