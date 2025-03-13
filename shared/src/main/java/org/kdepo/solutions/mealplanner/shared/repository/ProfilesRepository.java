package org.kdepo.solutions.mealplanner.shared.repository;

import org.kdepo.solutions.mealplanner.shared.model.Profile;

import java.util.List;

public interface ProfilesRepository {

    Profile addProfile(Integer profileId, String name, Integer orderNumber);

    void deleteProfile(Integer profileId);

    List<Profile> getAllProfiles();

    Integer getOrderNumber();

    Profile getProfile(Integer profileId);

    boolean isUsed(Integer profileId);

    void updateProfile(Integer profileId, String name, Integer orderNumber);

}
