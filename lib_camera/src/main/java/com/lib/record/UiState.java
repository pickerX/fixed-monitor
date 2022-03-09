package com.lib.record;

/**
 * @author pengfei.huang
 * create on 2022/3/9
 */
public enum UiState {
    IDLE,       // Not recording, all UI controls are active.
    RECORDING,  // Camera is recording, only display Pause/Resume & Stop button.
    FINALIZED,  // Recording just completes, disable all RECORDING UI controls.
    RECOVERY    // For future use.

}
