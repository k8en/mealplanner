package org.kdepo.solutions.mealplanner.model;

import java.util.Objects;

public class TagSelectable {

    private Integer tagId;
    private String name;
    private Boolean selected;

    public Integer getTagId() {
        return tagId;
    }

    public void setTagId(Integer tagId) {
        this.tagId = tagId;
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
        return "TagSelectable{" +
                "tagId=" + tagId +
                ", name='" + name + '\'' +
                ", selected=" + selected +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        TagSelectable that = (TagSelectable) o;
        return Objects.equals(tagId, that.tagId)
                && Objects.equals(name, that.name)
                && Objects.equals(selected, that.selected);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tagId, name, selected);
    }
}
