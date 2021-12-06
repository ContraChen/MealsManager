package com.example.mealsmanager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    ImageView pic;
    ImageButton ib,ib_add,ib_del;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ib=(ImageButton) findViewById(R.id.ib);ib.setOnClickListener(this);
        ib_add=(ImageButton)findViewById(R.id.ib_add);ib_add.setOnClickListener(this);
        ib_del=(ImageButton)findViewById(R.id.ib_del);ib_del.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        Intent intent=new Intent();
        switch (id){
            case R.id.ib:
                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    //没有权限时请求该权限：出现是否型对话框由用户选择是否授权
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                } else {
                    fun();
                }
                break;
            default:
                break;
        }
    }

    void fun(){
        pic=findViewById(R.id.pic);
        Intent intent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE); //调用系统相机程序
        startActivityForResult(intent,1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        String basePath = Environment.getExternalStorageDirectory().getPath();
        String filePath = basePath + "/myImage";
        if(requestCode==1){
            if(resultCode==RESULT_OK){
                Bundle bundle=data.getExtras();
                Bitmap bitmap=(Bitmap) bundle.get("data");
                FileOutputStream fos=null;
                File file=new File(filePath);
                file.mkdir();
                String fileName=filePath+"/111.jpg";
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