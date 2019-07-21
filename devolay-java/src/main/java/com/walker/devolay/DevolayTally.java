package com.walker.devolay;

public class DevolayTally {

    private boolean isOnProgram;
    private boolean isOnPreview;

    DevolayTally(boolean isOnProgram, boolean isOnPreview) {
        this.isOnProgram = isOnProgram;
        this.isOnPreview = isOnPreview;
    }

    public boolean isOnProgram() {
        return isOnProgram;
    }

    public boolean isOnPreview() {
        return isOnPreview;
    }
}
