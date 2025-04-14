package org.kdepo.solutions.mealplanner.shared.model;

import java.util.Objects;

public class Profile {

    private Integer profileId;
    private Integer profileTypeId;
    private String name;
    private Boolean active;

    public Integer getProfileId() {
        return profileId;
    }

    public void setProfileId(Integer profileId) {
        this.profileId = profileId;
    }

    public Integer getProfileTypeId() {
        return profileTypeId;
    }

    public void setProfileTypeId(Integer profileTypeId) {
        this.profileTypeId = profileTypeId;
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
                && Objects.equals(profileTypeId, profile.profileTypeId)
                && Objects.equals(name, profile.name)
                && Objects.equals(active, profile.active);
    }

    @Override
    public int hashCode() {
        return Objects.hash(profileId, profileTypeId, name, active);
    }

    @Override
    public String toString() {
        return "Profile{" +
                "profileId=" + profileId +
                ", profileTypeId=" + profileTypeId +
                ", name='" + name + '\'' +
                ", active=" + active +
                '}';
    }
}
