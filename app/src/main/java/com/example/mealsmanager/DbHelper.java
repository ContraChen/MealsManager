package com.example.mealsmanager;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase.CursorFactory;

public class DbHelper extends SQLiteOpenHelper {
    public static final String TB_NAME = "meals";  //表名

    //构造方法：第1参数为上下文，第2参数库库名，第3参数为游标工厂，第4参数为版本
    public DbHelper(Context context, String dbname, CursorFactory factory, int version) {
        super(context, dbname, factory, version);  //创建或打开数据库
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        //当表不存在时，创建表；第一字段为自增长类型
        db.execSQL("CREATE TABLE IF NOT EXISTS " +
                TB_NAME + "( _id integer primary key autoincrement," +"image blob,"+
                "meal varchar," + "cost varchar,"+"heat varchar,"+"date varchar,"+"time varchar"+ ")");
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // 执行SQL命令
        db.execSQL("DROP TABLE IF EXISTS " + TB_NAME);
        onCreate(db);
    }

//    /*
//    long主键能通过自增序列来赋
//    */
//    public static  long insert(SQLiteDatabase db, String TableName, ContentValues values){
//        return db.insert(TableName,null,values);
//    }
}
