package com.lib.record;

/**
 * @author pickerx
 * @date 2022/1/27 8:26 下午
 */
public class MonitorFactory implements Monitor.Factory {

    public static Monitor.Factory getInstance() {
        return new MonitorFactory();
    }

    @Override
    public Monitor create(Config config) {
        Monitor monitor;
        if (config.cameraX) monitor = new MonitorX(config);
        else monitor = new MonitorImpl(config);

        return monitor;
    }

}
