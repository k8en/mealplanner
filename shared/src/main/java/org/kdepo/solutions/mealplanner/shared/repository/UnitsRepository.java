package org.kdepo.solutions.mealplanner.shared.repository;

import org.kdepo.solutions.mealplanner.shared.model.Unit;

import java.util.List;

public interface UnitsRepository {

    Unit addUnit(Integer unitId, String name, String shortName, Integer accuracy);

    void deleteUnit(Integer unitId);

    List<Unit> getAllUnits();

    Unit getUnit(Integer unitId);

    boolean isUnitUsed(Integer unitId);

    void updateUnit(Integer unitId, String name, String shortName, Integer accuracy);

}
