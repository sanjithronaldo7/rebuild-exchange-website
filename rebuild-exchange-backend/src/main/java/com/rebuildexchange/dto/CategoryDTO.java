package com.rebuildexchange.dto;

import com.rebuildexchange.entity.MaterialCategory;

public class CategoryDTO {

    private Long id;
    private String name;
    private String slug;
    private String iconSymbol;
    private String description;
    private Integer displayOrder;

    public CategoryDTO() {}

    public CategoryDTO(MaterialCategory category) {
        this.id = category.getId();
        this.name = category.getName();
        this.slug = category.getSlug();
        this.iconSymbol = category.getIconSymbol();
        this.description = category.getDescription();
        this.displayOrder = category.getDisplayOrder();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getIconSymbol() {
        return iconSymbol;
    }

    public void setIconSymbol(String iconSymbol) {
        this.iconSymbol = iconSymbol;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }
}
