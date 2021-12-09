package com.example.mealsmanager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Blob;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    ImageView pic;
    ImageButton ib,ib_add,ib_del,ib_modify,ib_clc;
    Spinner sp;
    int spt=0;

    private MyDAO myDAO;  //数据库访问对象
    private ListView listView;
    private List<Map<String,Object>> listData;
    private Map<String,Object> listItem;
    private SimpleAdapter listAdapter;
    private EditText et_ms;  //数据表包含3个字段，第1字段为自增长类型
    private EditText et_xf;
    private EditText et_rl;
    private EditText et_date;
    private  String selId=null;  //选择项id

    String basePath = Environment.getExternalStorageDirectory().getPath();
    String filePath = basePath + "/myImage";
    String fileName=filePath+"/111.jpg";
//    Bitmap mymap = BitmapFactory.decodeFile(fileName);
//    File myfile = new File(fileName);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ib=(ImageButton) findViewById(R.id.ib);ib.setOnClickListener(this);
        ib_add=(ImageButton)findViewById(R.id.ib_add);ib_add.setOnClickListener(this);
        ib_del=(ImageButton)findViewById(R.id.ib_del);ib_del.setOnClickListener(this);
        ib_modify=(ImageButton)findViewById(R.id.ib_modify);ib_modify.setOnClickListener(this);
        ib_clc=(ImageButton)findViewById(R.id.ib_clc);ib_clc.setOnClickListener(this);
        sp=(Spinner)findViewById(R.id.sp);

        et_ms=(EditText)findViewById(R.id.et_ms);
        et_xf=(EditText)findViewById(R.id.et_xf);
        et_rl=(EditText)findViewById(R.id.et_rl);
        et_date=(EditText)findViewById(R.id.et_date);

        myDAO = new MyDAO(this);  //创建数据库访问对象
        if(myDAO.getRecordsNumber()==0) {  //防止重复运行时重复插入记录
//            Bitmap mymap = BitmapFactory.decodeFile(fileName);
            Resources res = MainActivity.this.getResources();
            Bitmap init_bmp= BitmapFactory.decodeResource(res, R.raw.header);
            myDAO.insertInfo(init_bmp,"rice","2","300","1208","Lunch");   //插入记录
            myDAO.insertInfo(init_bmp,"noodle","4","200","1209","Supper"); //插入记录
        }
        displayRecords();   //显示记录
    }

    public void displayRecords(){  //显示记录方法定义
        listView = (ListView)findViewById(R.id.lv_food);
        listData = new ArrayList<Map<String,Object>>();
        Cursor cursor = myDAO.allQuery();
        while (cursor.moveToNext()){
            String id=cursor.getString(0);  //获取字段值
            byte[] image=cursor.getBlob(cursor.getColumnIndex("image"));
            String meal=cursor.getString(2);
            String cost=cursor.getString(3);
            String heat=cursor.getString(4);
            String date=cursor.getString(5);
            String time=cursor.getString(6);
            //int age=cursor.getInt(2);
//            int age=cursor.getInt(cursor.getColumnIndex("age"));//推荐此种方式
            listItem=new HashMap<String,Object>(); //必须在循环体里新建
            Bitmap mymap = BitmapFactory.decodeFile(fileName);
            ByteArrayOutputStream os=new ByteArrayOutputStream();
            mymap.compress(Bitmap.CompressFormat.PNG,100,os);
            listItem.put("_id", id);  //第1参数为键名，第2参数为键值
            listItem.put("image",image);
            listItem.put("meal", meal);
            listItem.put("cost", cost);
            listItem.put("heat", heat);
            listItem.put("date", date);
            listItem.put("time", time);
            listData.add(listItem);   //添加一条记录
        }
        listAdapter = new SimpleAdapter(this,
                listData,
                R.layout.listview, //自行创建的列表项布局
                new String[]{"_id","meal","cost","heat","date","time"},
                new int[]{R.id.l_id,R.id.l_meal,R.id.l_cost,R.id.l_heat,R.id.l_date,R.id.l_time});
        listView.setAdapter(listAdapter);  //应用适配器
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {  //列表项监听
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Map<String,Object> rec= (Map<String, Object>) listAdapter.getItem(position);  //从适配器取记录

                et_ms.setText(rec.get("meal").toString());  //刷新文本框
                et_xf.setText(rec.get("cost").toString());
                et_rl.setText(rec.get("heat").toString());
                et_date.setText(rec.get("date").toString());
                if(rec.get("time").toString().equals("Breakfast"))spt=0;
                else if(rec.get("time").toString().equals("Lunch"))spt=1;
                else if(rec.get("time").toString().equals("Supper"))spt=2;
                else if(rec.get("time").toString().equals("Snack"))spt=3;
                sp.setSelection(spt,true);
                Log.i("ly",rec.get("_id").toString());
                selId=rec.get("_id").toString();  //供修改和删除时使用
            }
        });
    }

    @Override
    public void onClick(View v) {
        if(selId!=null) {  //选择了列表项后，可以增加/删除/修改
            Bitmap mymap = BitmapFactory.decodeFile(fileName);
            String p1 = et_ms.getText().toString().trim();
            String p2 = et_xf.getText().toString().trim();
            String p3 = et_rl.getText().toString().trim();
            String p4 = et_date.getText().toString().trim();
            String p5 = sp.getSelectedItem().toString().trim();
            switch (v.getId()){
                case  R.id.ib_add:
                    myDAO.insertInfo(mymap,p1,p2,p3,p4,p5);
                    break;
                case  R.id.ib_modify:
                    myDAO.updateInfo(mymap,p1,p2,p3,p4,p5,selId);
                    Toast.makeText(getApplicationContext(),"更新成功！",Toast.LENGTH_SHORT).show();
                    break;
                case  R.id.ib_del:
                    myDAO.deleteInfo(selId);
                    Toast.makeText(getApplicationContext(),"删除成功！",Toast.LENGTH_SHORT).show();
                    et_ms.setText(null);
                    et_xf.setText(null);
                    et_rl.setText(null);
                    et_date.setText(null);
                    selId=null; //提示
                    break;
                case R.id.ib_clc:
                    Toast.makeText(getApplicationContext(),"输入已清空！",Toast.LENGTH_SHORT).show();
                    et_ms.setText(null);
                    et_xf.setText(null);
                    et_rl.setText(null);
                    et_date.setText(null);
                    selId=null;
                    break;
            }
        }else{  //未选择列表项
            if(v.getId()==R.id.ib_add) {  //单击添加按钮
                Bitmap mymap = BitmapFactory.decodeFile(fileName);
                String p1 = et_ms.getText().toString();
                String p2 = et_xf.getText().toString();
                String p3 = et_rl.getText().toString();
                String p4 = et_date.getText().toString();
                String p5 = sp.getSelectedItem().toString();
                if(p1.equals("")){  //要求输入了信息
                    Toast.makeText(getApplicationContext(),"美食输入不能为空！",Toast.LENGTH_SHORT).show();
                }else{
                    if(p1.equals(""))p1="--";
                    if(p2.equals(""))p2="--";
                    if(p3.equals(""))p3="--";
                    if(p4.equals(""))p4="--";
                    myDAO.insertInfo(mymap,p1,p2,p3,p4,p5);  //第2参数转型
                }
            }
            else if (v.getId()==R.id.ib){
                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    //没有权限时请求该权限：出现是否型对话框由用户选择是否授权
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                } else {
                    fun();
                }
            }
            else if (v.getId()==R.id.ib_clc){
                Toast.makeText(getApplicationContext(),"输入已清空！",Toast.LENGTH_SHORT).show();
                et_ms.setText(null);
                et_xf.setText(null);
                et_rl.setText(null);
                et_date.setText(null);
                selId=null;
            }
            else{   //单击了修改或删除按钮
                Toast.makeText(getApplicationContext(),"请先选择记录！",Toast.LENGTH_SHORT).show();
            }
        }
        displayRecords();//刷新ListView对象
    }

    ByteArrayOutputStream get_image(Intent data){
        Bundle bundle=data.getExtras();
        Bitmap bitmap=(Bitmap) bundle.get("data");
        FileOutputStream fos=null;
        File file=new File(filePath);
        file.mkdir();
        try {
            fos=new FileOutputStream(fileName);
            bitmap.compress(Bitmap.CompressFormat.JPEG,100,fos);
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            try {
                fos.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        pic.setImageBitmap(bitmap);
        ByteArrayOutputStream os=new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG,100,os);
        return os;
    }

    void fun(){
        pic=findViewById(R.id.pic);
        Intent intent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE); //调用系统相机程序
        startActivityForResult(intent,1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==1){
            if(resultCode==RESULT_OK){
                ByteArrayOutputStream myos=get_image(data);
            }
        }
    }
    @Override  //Android 6.0动态权限处理的接口回调方法
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {  //所需关键权限
                    fun();
                } else {
                    Toast.makeText(this, "没有写SD卡权限", Toast.LENGTH_LONG).show();
                    finish();
                }
        }
    }
}