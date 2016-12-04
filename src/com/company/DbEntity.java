package com.company;

/**
 * Created by bxtr on 04.12.2016.
 */
public class DbEntity {

    public int id;
    public String name;
    public String changesName;
    public String parentTag;
    public String childTag;
    public String patternType;

    public DbEntity() {
        //empty
    }

    public DbEntity setId(int id) {
        this.id = id;
        return this;
    }

    public DbEntity setName(String name) {
        this.name = name;
        return this;
    }

    public DbEntity setChangesName(String changesName) {
        this.changesName = changesName;
        return this;
    }

    public DbEntity setParentTag(String parentTag) {
        this.parentTag = parentTag;
        return this;
    }

    public DbEntity setChildTag(String childTag) {
        this.childTag = childTag;
        return this;
    }

    public DbEntity setPatternType(String patternType) {
        this.patternType = patternType;
        return this;
    }
}
