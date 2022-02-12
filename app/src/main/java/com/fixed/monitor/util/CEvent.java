package com.fixed.monitor.util;

public class CEvent {

    public static class MonitorRecordingEvent{

      public  String recordStatus;
        public long recordDuringTime;
        int startMillis;

        public MonitorRecordingEvent(String recordStatus, long recordDuringTime) {
            this.recordStatus = recordStatus;
            this.recordDuringTime = recordDuringTime;
        }
    }

}
