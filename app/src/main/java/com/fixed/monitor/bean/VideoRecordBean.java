package com.fixed.monitor.bean;

import com.fixed.monitor.base.BaseBean;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "tb_fhousecallrecord")
public class VideoRecordBean extends BaseBean {
    @DatabaseField(dataType = DataType.INTEGER, columnName = "_id", generatedId = true)
    public int id;
    @DatabaseField(defaultValue = "")
    public String videoName;//视频名字
    @DatabaseField(dataType = DataType.FLOAT)
    public float videoSize;//视频大小
    @DatabaseField(dataType = DataType.LONG)
    public long videoDuringTime;//视频持续时间
    @DatabaseField(defaultValue = "")
    public String videoCover;//视频封面
    @DatabaseField(defaultValue = "")
    public String videoCachePath;//视频缓存路径
    @DatabaseField(dataType = DataType.LONG)
    public long videoCreateTime= 0 ;//视频创建时间
}
