package org.kdepo.solutions.mealplanner.server.repository.impl;

import org.kdepo.solutions.mealplanner.shared.model.Unit;
import org.kdepo.solutions.mealplanner.shared.repository.UnitsRepository;
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
public class UnitsRepositoryImpl implements UnitsRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(UnitsRepositoryImpl.class);

    private static final String SQL_ADD_UNIT = "INSERT INTO units (unit_id, name, short_name, accuracy) VALUES (?, ?, ?, ?)";
    private static final String SQL_DELETE_UNIT = "DELETE FROM units WHERE unit_id = ?";
    private static final String SQL_GET_ALL_UNITS = "SELECT * FROM units ORDER BY unit_id ASC";
    private static final String SQL_GET_UNIT = "SELECT * FROM units WHERE unit_id = ?";
    private static final String SQL_IS_USED = "SELECT unit_id FROM ingredients WHERE unit_id = ? LIMIT 1";
    private static final String SQL_UPDATE_UNIT = "UPDATE units SET name = ?, short_name = ?, accuracy = ? WHERE unit_id = ?";

    private final JdbcTemplate jdbcTemplate;

    public UnitsRepositoryImpl(@Qualifier("mealPlannerJdbcTemplate") JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Unit addUnit(Integer unitId, String name, String shortName, Integer accuracy) {
        LOGGER.trace("[DBR][addUnit] Invoked with parameters: unit_id={}, name={}, shortName={}, accuracy={}",
                unitId, name, shortName, accuracy
        );
        jdbcTemplate.update(
                SQL_ADD_UNIT,
                ps -> {
                    ps.setInt(1, unitId);
                    ps.setString(2, name);
                    ps.setString(3, shortName);
                    ps.setInt(4, accuracy);
                }
        );
        return getUnit(unitId);
    }

    @Override
    public void deleteUnit(Integer unitId) {
        LOGGER.trace("[DBR][deleteUnit] Invoked with parameters: unitId={}", unitId);
        jdbcTemplate.update(
                SQL_DELETE_UNIT,
                ps -> ps.setInt(1, unitId)
        );
    }

    @Override
    public List<Unit> getAllUnits() {
        LOGGER.trace("[DBR][getAllUnits] Invoked without parameters");
        return jdbcTemplate.query(
                SQL_GET_ALL_UNITS,
                rs -> {
                    List<Unit> result = new ArrayList<>();
                    while (rs.next()) {
                        result.add(convert(rs));
                    }
                    return result;
                }
        );
    }

    @Override
    public Unit getUnit(Integer unitId) {
        LOGGER.trace("[DBR][getUnit] Invoked with parameters: unitId={}", unitId);
        return jdbcTemplate.query(
                SQL_GET_UNIT,
                ps -> ps.setInt(1, unitId),
                rs -> {
                    Unit unit = null;
                    if (rs.next()) {
                        unit = convert(rs);
                    }
                    return unit;
                }
        );
    }

    @Override
    public boolean isUnitUsed(Integer unitId) {
        LOGGER.trace("[DBR][isUnitUsed] Invoked with parameters: unitId={}", unitId);
        return Boolean.TRUE.equals(jdbcTemplate.query(
                SQL_IS_USED,
                ps -> ps.setInt(1, unitId),
                ResultSet::next
        ));
    }

    @Override
    public void updateUnit(Integer unitId, String name, String shortName, Integer accuracy) {
        LOGGER.trace("[DBR][updateUnit] Invoked with parameters: unitId={}, name={}, shortName={}, accuracy={}",
                unitId, name, shortName, accuracy
        );
        jdbcTemplate.update(
                SQL_UPDATE_UNIT,
                ps -> {
                    ps.setString(1, name);
                    ps.setString(2, shortName);
                    ps.setInt(3, accuracy);
                    ps.setInt(4, unitId);
                }
        );
    }

    private Unit convert(ResultSet rs) throws SQLException {
        Integer unitId = rs.getInt("unit_id");
        String name = rs.getString("name");
        String shortName = rs.getString("short_name");
        Integer accuracy = rs.getInt("accuracy");

        Unit unit = new Unit();
        unit.setUnitId(unitId);
        unit.setName(name);
        unit.setShortName(shortName);
        unit.setAccuracy(accuracy);

        return unit;
    }
}
