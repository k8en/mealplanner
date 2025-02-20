package org.kdepo.solutions.mealplanner.repository;

import org.kdepo.solutions.mealplanner.model.Profile;

import java.util.List;

public interface MealPlannerProfilesRepository {

    Profile addProfile(Integer profileId, String name, Integer orderNumber);

    void deleteProfile(Integer profileId);

    List<Profile> getAllProfiles();

    Integer getOrderNumber();

    Profile getProfile(Integer profileId);

    boolean isUsed(Integer profileId);

    void updateProfile(Integer profileId, String name, Integer orderNumber);

}
