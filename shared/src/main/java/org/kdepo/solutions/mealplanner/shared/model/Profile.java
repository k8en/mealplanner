package org.kdepo.solutions.mealplanner.shared.model;

import java.util.Objects;

public class Profile {

    private Integer profileId;
    private String name;
    private Boolean active;

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

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Profile profile = (Profile) o;
        return Objects.equals(profileId, profile.profileId)
                && Objects.equals(name, profile.name)
                && Objects.equals(active, profile.active);
    }

    @Override
    public int hashCode() {
        return Objects.hash(profileId, name, active);
    }

    @Override
    public String toString() {
        return "Profile{" +
                "profileId=" + profileId +
                ", name='" + name + '\'' +
                ", active=" + active +
                '}';
    }
}
