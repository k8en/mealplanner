package org.kdepo.solutions.mealplanner.repository.impl;

import org.kdepo.solutions.mealplanner.model.Unit;
import org.kdepo.solutions.mealplanner.repository.UnitsRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class UnitsRepositoryImpl implements UnitsRepository {

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
        System.out.println("[UnitDao][addUnit] Invoked with parameters:"
                + " unit_id=" + unitId
                + ", name='" + name + "'"
                + ", shortName='" + shortName + "'"
                + ", accuracy=" + accuracy
        );

        jdbcTemplate.update(SQL_ADD_UNIT, unitId, name, shortName, accuracy);

        return getUnit(unitId);
    }

    @Override
    public void deleteUnit(Integer unitId) {
        System.out.println("[UnitDao][deleteUnit] Invoked with parameters: unitId=" + unitId);
        jdbcTemplate.update(SQL_DELETE_UNIT, unitId);
    }

    @Override
    public List<Unit> getAllUnits() {
        System.out.println("[UnitDao][getAllUnits] Invoked without parameters");
        return jdbcTemplate.query(
                SQL_GET_ALL_UNITS,
                (resultSet, rowNum) -> {
                    Integer unitId = resultSet.getInt("unit_id");
                    String name = resultSet.getString("name");
                    String shortName = resultSet.getString("short_name");
                    Integer accuracy = resultSet.getInt("accuracy");

                    Unit unit = new Unit();
                    unit.setUnitId(unitId);
                    unit.setName(name);
                    unit.setShortName(shortName);
                    unit.setAccuracy(accuracy);

                    return unit;
                }
        );
    }

    @Override
    public Unit getUnit(Integer unitId) {
        System.out.println("[UnitDao][getUnit] Invoked with parameters: unitId=" + unitId);
        return jdbcTemplate.query(
                SQL_GET_UNIT,
                resultSet -> {
                    //Integer unitId = resultSet.getInt("unit_id");
                    String name = resultSet.getString("name");
                    String shortName = resultSet.getString("short_name");
                    Integer accuracy = resultSet.getInt("accuracy");

                    Unit unit = new Unit();
                    unit.setUnitId(unitId);
                    unit.setName(name);
                    unit.setShortName(shortName);
                    unit.setAccuracy(accuracy);

                    return unit;
                },
                unitId
        );
    }

    @Override
    public boolean isUnitUsed(Integer unitId) {
        System.out.println("[UnitDao][isUnitUsed] Invoked with parameters: unitId=" + unitId);
        Integer objectId = jdbcTemplate.query(
                SQL_IS_USED,
                resultSet -> {
                    return resultSet.getInt("unit_id");
                },
                unitId
        );
        return objectId != null;
    }

    @Override
    public void updateUnit(Integer unitId, String name, String shortName, Integer accuracy) {
        System.out.println("[UnitDao][updateUnit] Invoked with parameters:"
                + " unitId=" + unitId
                + ", name='" + name + "'"
                + ", shortName='" + shortName + "'"
                + ", accuracy=" + accuracy
        );
        jdbcTemplate.update(SQL_UPDATE_UNIT, name, shortName, accuracy, unitId);
    }
}
