package me.walkerknapp.devolay;

/**
 * An object representing the result of a {@link DevolaySender#getTally(int)} call, describing where a {@link DevolaySender}
 * feed is currently being displayed.
 */
public class DevolayTally {

    private final boolean isOnProgram;
    private final boolean isOnPreview;

    /**
     * Creates a tally object to represent the result of a {@link DevolaySender#getTally(int)} call.
     *
     * @param isOnProgram If the current sender is being displayed on program.
     * @param isOnPreview If the current sender is being displayed on preview.
     */
    public DevolayTally(boolean isOnProgram, boolean isOnPreview) {
        this.isOnProgram = isOnProgram;
        this.isOnPreview = isOnPreview;
    }

    /**
     * Gets whether the current {@link DevolaySender} is being displayed on program.
     *
     * @return true if the sender is being displayed, false if it is not being displayed.
     */
    public boolean isOnProgram() {
        return isOnProgram;
    }

    /**
     * Gets whether the current {@link DevolaySender} is being displayed on preview.
     *
     * @return true if the sender is being displayed, false if it is not being displayed.
     */
    public boolean isOnPreview() {
        return isOnPreview;
    }
}
