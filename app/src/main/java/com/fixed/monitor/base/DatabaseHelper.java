package com.fixed.monitor.base;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.fixed.monitor.bean.VideoRecordBean;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class DatabaseHelper extends OrmLiteSqliteOpenHelper {

	private static final int db_version = 6;
	private static final String TABLE_NAME = "fixedmonitor-sqlite.db";
	private Map<String, Dao> daos = new HashMap<String, Dao>();

	public DatabaseHelper(Context context) {
		super(context, TABLE_NAME, null, db_version);
	}

	@Override
	public void onCreate(SQLiteDatabase database,
			ConnectionSource connectionSource) {
		try {
			TableUtils.createTable(connectionSource, VideoRecordBean.class);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase database,
			ConnectionSource connectionSource, int oldVersion, int newVersion) {
		if (newVersion != oldVersion) {
			try {
				TableUtils.dropTable(connectionSource, VideoRecordBean.class,
						true);
				onCreate(database, connectionSource);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	// private static DatabaseHelper instance;
	//
	// /**
	// * 单例获取该Helper
	// *
	// * @param context
	// * @return
	// */
	// public static synchronized DatabaseHelper getHelper(Context context)
	// {
	// context = context.getApplicationContext();
	// if (instance == null)
	// {
	// synchronized (DatabaseHelper.class)
	// {
	// if (instance == null)
	// instance = new DatabaseHelper(context);
	// }
	// }
	//
	// return instance;
	// }

	@SuppressWarnings("unchecked")
	public synchronized Dao getDao(Class clazz) throws SQLException {
		Dao dao = null;
		String className = clazz.getSimpleName();

		if (daos.containsKey(className)) {
			dao = daos.get(className);
		}
		if (dao == null) {
			dao = super.getDao(clazz);
			daos.put(className, dao);
		}
		return dao;
	}

	/**
	 */
	@Override
	public void close() {
		super.close();

		for (String key : daos.keySet()) {
			Dao dao = daos.get(key);
			dao = null;
		}
	}

}
