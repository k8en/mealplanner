package org.kdepo.solutions.mealplanner.server.repository.impl;

import org.kdepo.solutions.mealplanner.shared.model.Profile;
import org.kdepo.solutions.mealplanner.shared.repository.ProfilesRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Repository
public class ProfilesRepositoryImpl implements ProfilesRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProfilesRepositoryImpl.class);

    private static final String SQL_ADD_PROFILE = "INSERT INTO profiles (profile_id, name, is_default) VALUES (?, ?, ?)";
    private static final String SQL_DELETE_PROFILE = "DELETE FROM profiles WHERE profile_id = ?";
    private static final String SQL_GET_ALL_PROFILES = "SELECT * FROM profiles ORDER BY order_number ASC";
    private static final String SQL_GET_PROFILE = "SELECT * FROM profiles WHERE profile_id = ?";
    private static final String SQL_IS_USED = "SELECT profile_id FROM weeks WHERE profile_id = ? LIMIT 1";
    private static final String SQL_UPDATE_PROFILE = "UPDATE profiles SET name = ?, is_default = ? WHERE profile_id = ?";

    private final JdbcTemplate jdbcTemplate;

    public ProfilesRepositoryImpl(@Qualifier("mealPlannerJdbcTemplate") JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Profile addProfile(Integer profileId, String name, Boolean isDefault) {
        LOGGER.trace("[DBR][addProfile] Invoked with parameters: profileId={}, name={}, isDefault={}",
                profileId, name, isDefault
        );

        int isDefaultAsInt = isDefault ? 1 : 0;

        jdbcTemplate.update(
                SQL_ADD_PROFILE,
                ps -> {
                    ps.setInt(1, profileId);
                    ps.setString(2, name);
                    ps.setInt(3, isDefaultAsInt);
                }
        );

        return getProfile(profileId);
    }

    @Override
    public void deleteProfile(Integer profileId) {
        LOGGER.trace("[DBR][deleteProfile] Invoked with parameters: profileId={}", profileId);

        jdbcTemplate.update(
                SQL_DELETE_PROFILE,
                ps -> ps.setInt(1, profileId)
        );
    }

    @Override
    public List<Profile> getAllProfiles() {
        LOGGER.trace("[DBR][getAllProfiles] Invoked without parameters");
        return jdbcTemplate.query(
                SQL_GET_ALL_PROFILES,
                rs -> {
                    List<Profile> result = new ArrayList<>();
                    while (rs.next()) {
                        result.add(convert(rs));
                    }
                    return result;
                }
        );
    }

    @Override
    public Profile getProfile(Integer profileId) {
        LOGGER.trace("[DBR][getProfile] Invoked with parameters: profileId={}", profileId);
        return jdbcTemplate.query(
                SQL_GET_PROFILE,
                ps -> ps.setInt(1, profileId),
                rs -> {
                    Profile profile = null;
                    if (rs.next()) {
                        profile = convert(rs);
                    }
                    return profile;
                }
        );
    }

    @Override
    public boolean isUsed(Integer profileId) {
        LOGGER.trace("[DBR][isUsed] Invoked with parameters: profileId={}", profileId);
        return Boolean.TRUE.equals(jdbcTemplate.query(
                SQL_IS_USED,
                ps -> ps.setInt(1, profileId),
                ResultSet::next
        ));
    }

    @Override
    public void updateProfile(Integer profileId, String name, Boolean isDefault) {
        LOGGER.trace("[DBR][updateProfile] Invoked with parameters: profileId={}, name={}, isDefault={}",
                profileId, name, isDefault
        );

        int isDefaultAsInt = isDefault ? 1 : 0;

        jdbcTemplate.update(
                SQL_UPDATE_PROFILE,
                ps -> {
                    ps.setString(1, name);
                    ps.setInt(2, isDefaultAsInt);
                    ps.setInt(3, profileId);
                }
        );
    }

    private Profile convert(ResultSet rs) throws SQLException {
        Integer profileId = rs.getInt("profile_id");
        String name = rs.getString("name");
        Integer isDefaultAsInt = rs.getInt("is_default");

        Profile profile = new Profile();
        profile.setProfileId(profileId);
        profile.setName(name);
        profile.setDefault(isDefaultAsInt != 0);

        return profile;
    }
}
