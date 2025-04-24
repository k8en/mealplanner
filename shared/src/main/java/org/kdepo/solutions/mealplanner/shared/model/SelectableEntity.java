package org.kdepo.solutions.mealplanner.shared.model;

import java.util.Objects;

public class SelectableEntity {

    private Integer id;
    private String name;
    private Boolean selected;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getSelected() {
        return selected;
    }

    public void setSelected(Boolean selected) {
        this.selected = selected;
    }

    @Override
    public String toString() {
        return "SelectableEntity{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", selected=" + selected +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        SelectableEntity that = (SelectableEntity) o;
        return Objects.equals(id, that.id)
                && Objects.equals(name, that.name)
                && Objects.equals(selected, that.selected);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, selected);
    }
}
