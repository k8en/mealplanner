package org.kdepo.solutions.mealplanner.repository.impl;

import org.kdepo.solutions.mealplanner.model.Profile;
import org.kdepo.solutions.mealplanner.repository.MealPlannerProfilesRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class MealPlannerProfilesRepositoryImpl implements MealPlannerProfilesRepository {

    private static final String SQL_ADD_PROFILE = "INSERT INTO profiles (profile_id, name, order_number) VALUES (?, ?, ?)";
    private static final String SQL_DELETE_PROFILE = "DELETE FROM profiles WHERE profile_id = ?";
    private static final String SQL_GET_ALL_PROFILES = "SELECT * FROM profiles ORDER BY order_number ASC";
    private static final String SQL_GET_ORDER_NUMBER = "SELECT IFNULL(MAX(order_number) + 1, 1) AS order_number FROM profiles";
    private static final String SQL_GET_PROFILE = "SELECT * FROM profiles WHERE profile_id = ?";
    private static final String SQL_IS_USED = "SELECT profile_id FROM weeks WHERE profile_id = ? LIMIT 1";
    private static final String SQL_UPDATE_PROFILE = "UPDATE profiles SET name = ?, order_number = ? WHERE profile_id = ?";

    private final JdbcTemplate jdbcTemplate;

    public MealPlannerProfilesRepositoryImpl(@Qualifier("mealPlannerJdbcTemplate") JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Profile addProfile(Integer profileId, String name, Integer orderNumber) {
        System.out.println("[ML][ProfileDao][addProfile] Invoked with parameters:"
                + " profileId=" + profileId
                + ", name='" + name + "'"
                + ", orderNumber=" + orderNumber
        );
        jdbcTemplate.update(SQL_ADD_PROFILE, profileId, name, orderNumber);

        return getProfile(profileId);
    }

    @Override
    public void deleteProfile(Integer profileId) {
        System.out.println("[ML][ProfileDao][deleteProfile] Invoked with parameters: profileId=" + profileId);

        jdbcTemplate.update(SQL_DELETE_PROFILE, profileId);
    }

    @Override
    public List<Profile> getAllProfiles() {
        System.out.println("[ML][ProfileDao][getAllProfiles] Invoked without parameters");
        return jdbcTemplate.query(
                SQL_GET_ALL_PROFILES,
                (resultSet, rowNum) -> {
                    Integer profileId = resultSet.getInt("profile_id");
                    String name = resultSet.getString("name");
                    Integer orderNumber = resultSet.getInt("order_number");

                    Profile profile = new Profile();
                    profile.setProfileId(profileId);
                    profile.setName(name);
                    profile.setOrderNumber(orderNumber);

                    return profile;
                }
        );
    }

    @Override
    public Integer getOrderNumber() {
        System.out.println("[ML][ProfileDao][getOrderNumber] Invoked without parameters");
        return jdbcTemplate.query(
                SQL_GET_ORDER_NUMBER,
                resultSet -> {
                    return resultSet.getInt("order_number");
                }
        );
    }

    @Override
    public Profile getProfile(Integer profileId) {
        System.out.println("[ML][ProfileDao][getProfile] Invoked with parameters: profileId=" + profileId);
        return jdbcTemplate.query(
                SQL_GET_PROFILE,
                resultSet -> {
                    //Integer profileId = resultSet.getInt("profile_id");
                    String name = resultSet.getString("name");
                    Integer orderNumber = resultSet.getInt("order_number");

                    Profile profile = new Profile();
                    profile.setProfileId(profileId);
                    profile.setName(name);
                    profile.setOrderNumber(orderNumber);

                    return profile;
                },
                profileId
        );
    }

    @Override
    public boolean isUsed(Integer profileId) {
        System.out.println("[ML][ProfileDao][isUsed] Invoked with parameters: profileId=" + profileId);
        Integer objectId = jdbcTemplate.query(
                SQL_IS_USED,
                resultSet -> {
                    return resultSet.getInt("profile_id");
                },
                profileId
        );
        return objectId != null;
    }

    @Override
    public void updateProfile(Integer profileId, String name, Integer orderNumber) {
        System.out.println("[ML][ProfileDao][updateProfile] Invoked with parameters:"
                + " profileId=" + profileId
                + ", name='" + name + "'"
                + ", orderNumber=" + orderNumber
        );

        jdbcTemplate.update(SQL_UPDATE_PROFILE, name, orderNumber, profileId);
    }
}
