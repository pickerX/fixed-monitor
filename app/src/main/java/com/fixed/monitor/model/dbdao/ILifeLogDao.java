package com.fixed.monitor.model.dbdao;

import com.fixed.monitor.bean.LifeLogBean;
import com.fixed.monitor.bean.VideoRecordBean;

import java.util.List;

public interface ILifeLogDao {
    /**
     * @param
     * @return
     * @description 插入一条数据
     * @author jiejack
     * @time 2022/2/4 9:00 下午
     */
    boolean insert(LifeLogBean lifeLogBean);


    /**
     * @param
     * @return
     * @description 查询所有数据
     * @author jiejack
     * @time 2022/2/4 9:02 下午
     */
    List<LifeLogBean> queryAll();
}
