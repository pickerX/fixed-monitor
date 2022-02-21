package com.fixed.monitor.bean;

import com.fixed.monitor.base.BaseBean;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;

public class LifeLogBean extends BaseBean {


    public LifeLogBean() {
    }

    public LifeLogBean(int type, String msg, String erroMsg, long createTime) {
        this.type = type;
        this.msg = msg;
        this.erroMsg = erroMsg;
        this.createTime = createTime;
    }

    @DatabaseField(dataType = DataType.INTEGER, columnName = "_id", generatedId = true)
    public int id;
    @DatabaseField()
    public int type ;//0普通 -1异常
    @DatabaseField(defaultValue = "")
    public String msg;//信息
    @DatabaseField(defaultValue = "")
    public String erroMsg;//异常内容
    @DatabaseField(dataType = DataType.LONG)
    public long createTime;//视频持续时间
}
