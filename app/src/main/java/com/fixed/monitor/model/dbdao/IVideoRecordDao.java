package com.fixed.monitor.model.dbdao;

import com.fixed.monitor.bean.VideoRecordBean;

import java.util.List;

/**
 * @param
 * @author jiejack
 * @return
 * @description 视频缓存记录DAO
 * @time 2022/2/4 8:59 下午
 */
public interface IVideoRecordDao {

    /**
     * @param
     * @return
     * @description 插入一条数据
     * @author jiejack
     * @time 2022/2/4 9:00 下午
     */
    boolean insert(VideoRecordBean videoRecordBean);

//    /**
//     * @param
//     * @return
//     * @description 删除一条数据
//     * @author jiejack
//     * @time 2022/2/4 9:01 下午
//     */
//    boolean delete(int id);

    /**
     * @param
     * @return
     * @description 查询对应ID是第几行
     * @author jiejack
     * @time 2022/2/4 9:41 下午
     */
    long queryRowNumByID(int id);

    /**
     * @param
     * @return
     * @description 查询所有数据
     * @author jiejack
     * @time 2022/2/4 9:02 下午
     */
    List<VideoRecordBean> queryAll();


    /**
     * @param
     * @return
     * @description 分页查询数据
     * @author jiejack
     * @time 2022/2/4 9:02 下午
     */
    List<VideoRecordBean> queryByDate_Page(int offset,int pageSize,long startTime,long endTime);
}

