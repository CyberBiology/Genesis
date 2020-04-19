public interface GuiCallback {
    void drawStepChanged(int value);
    void mapGenerationStarted(int canvasWidth, int canvasHeight);
    void seaLevelChanged(int value);
    boolean startedOrStopped();
    void viewModeChanged(int viewMode);
}
