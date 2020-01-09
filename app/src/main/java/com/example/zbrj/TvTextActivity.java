package com.example.zbrj;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;


public class TvTextActivity extends BaseActivity {

    private TextView tv_dz, tv_jd, tv_wd, tv_txl, tv_dx;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.text_layout);
        initView();

        String txl = getIntent().getStringExtra("txl") + "";
        tv_txl.setText(txl);

        String dx = getIntent().getStringExtra("dx") + "";
        tv_dx.setText(dx);

        TelephonyManager tm = (TelephonyManager) this.getSystemService(this.TELEPHONY_SERVICE);
        tv_dz.setText(address);
        tv_jd.setText(Longitude+"");
        if (checkSelfPermission(Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.READ_PHONE_NUMBERS) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this,"请打开相关权限",Toast.LENGTH_SHORT).show();
            return;
        }
        tv_wd.setText(Latitude+"");
        Log.i("JJJ","phoen:"+tm.getSimSerialNumber());
    }

    private void initView() {
        tv_dz = findViewById(R.id.tv_dz);
        tv_jd = findViewById(R.id.tv_jd);
        tv_wd=findViewById(R.id.tv_wd);
        tv_txl=findViewById(R.id.tv_txl);
        tv_dx=findViewById(R.id.tv_dx);
    }
}
