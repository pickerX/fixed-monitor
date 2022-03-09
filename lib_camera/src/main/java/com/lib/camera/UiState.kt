package com.lib.camera

/**
 *
 * @author pengfei.huang
 * create on 2022/3/9
 */
// Camera UI  states and inputs
enum class UiState {
    IDLE,       // Not recording, all UI controls are active.
    RECORDING,  // Camera is recording, only display Pause/Resume & Stop button.
    FINALIZED,  // Recording just completes, disable all RECORDING UI controls.
    RECOVERY    // For future use.
}