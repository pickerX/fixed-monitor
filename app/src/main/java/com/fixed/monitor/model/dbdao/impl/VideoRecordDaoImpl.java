package com.fixed.monitor.model.dbdao.impl;

import android.content.Context;
import android.provider.MediaStore;
import android.util.Log;

import com.fixed.monitor.base.DatabaseHelper;
import com.fixed.monitor.bean.VideoRecordBean;
import com.fixed.monitor.model.App;
import com.fixed.monitor.model.dbdao.IVideoRecordDao;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class VideoRecordDaoImpl implements IVideoRecordDao {

    public Context context;
    private Dao<VideoRecordBean, Integer> fOpe;
    private DatabaseHelper helper;

    public VideoRecordDaoImpl() {
    }

    @SuppressWarnings("unchecked")
    public VideoRecordDaoImpl(Context context) {
        this.context = context;
        try {
            helper = App.getApp().getHelper();
            fOpe = helper.getDao(VideoRecordBean.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }


    @Override
    public boolean insert(VideoRecordBean videoRecordBean) {
        if (fOpe != null) {
            try {
                fOpe.create(videoRecordBean);
                return true;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

//    @Override
//    public boolean delete(int id) {
//        if (fOpe != null) {
//            try {
//                fOpe.deleteById(id);
//                return true;
//            } catch (SQLException e) {
//                e.printStackTrace();
//            }
//        }
//        return false;
//    }

    @Override
    public long queryRowNumByID(int id) {
        if (fOpe != null) {
            try {
                QueryBuilder<VideoRecordBean, Integer> q = fOpe.queryBuilder();

                q.where().eq("", id);


            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return 0;
    }

    @Override
    public List<VideoRecordBean> queryAll() {
        List<VideoRecordBean> videoRecordBeans = new ArrayList<>();
        if (fOpe != null) {
            try {
                QueryBuilder<VideoRecordBean, Integer> q = fOpe.queryBuilder();
                q.orderBy("_id", false);
                videoRecordBeans = q.query();
            } catch (SQLException e) {
            }
        }
        return videoRecordBeans;
    }

    @Override
    public List<VideoRecordBean> queryByDate_Page(int offset, int pageSize, long startTime, long endTime) {
        List<VideoRecordBean> videoRecordBeans = new ArrayList<>();
        if (fOpe != null) {
            try {
//                long startRow = (long)startID;
                QueryBuilder<VideoRecordBean, Integer> q = fOpe.queryBuilder();
                Where<VideoRecordBean,Integer> where =  q.where().raw("1=1");
                if (startTime != 0) {
                    where.and().ge("videoCreateTime", startTime);
                }
                if (endTime != 0) {
                    where.and().le("videoCreateTime", endTime);
                }
                q.offset((long) offset).limit((long) pageSize);
                q.orderBy("_id", false);
                Log.i("jjjjjjjjaack", q.prepareStatementString());
                videoRecordBeans = q.query();
            } catch (SQLException e) {
            }
        }
        return videoRecordBeans;
    }
}
