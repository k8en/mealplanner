package org.kdepo.solutions.mealplanner.model;

import java.util.Objects;

public class Unit {

    private Integer unitId;
    private String name;
    private String shortName;
    private Integer accuracy;

    public Integer getUnitId() {
        return unitId;
    }

    public void setUnitId(Integer unitId) {
        this.unitId = unitId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public Integer getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(Integer accuracy) {
        this.accuracy = accuracy;
    }

    @Override
    public String toString() {
        return "Unit{" +
                "unitId=" + unitId +
                ", name='" + name + '\'' +
                ", shortName='" + shortName + '\'' +
                ", accuracy=" + accuracy +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Unit unit = (Unit) o;
        return Objects.equals(unitId, unit.unitId)
                && Objects.equals(name, unit.name)
                && Objects.equals(shortName, unit.shortName)
                && Objects.equals(accuracy, unit.accuracy);
    }

    @Override
    public int hashCode() {
        return Objects.hash(unitId, name, shortName, accuracy);
    }
}
