package com.cringe_studios.cringe_authenticator.backup;

import org.jetbrains.annotations.Contract;

public class BackupGroup {

    public String id;
    public String name;

    private BackupGroup() {}

    public BackupGroup(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public boolean isValid() {
        return id != null && name != null;
    }

}
