package com.lib.record;

/**
 * @author pickerx
 * @date 2022/1/27 8:26 下午
 */
public class MonitorFactory implements Monitor.Factory {

    public static MonitorFactory getInstance() {
        return new MonitorFactory();
    }

    @Override
    public Monitor create(Config config) {
        Monitor m = new MonitorImpl(config);
        return m;
    }

}
