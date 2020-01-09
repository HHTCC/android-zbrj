package com.example.zbrj;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.zbrj.utils.GPSUtils;
import com.example.zbrj.utils.StatusBarUtil;
import com.example.zbrj.widegt.HintDialog;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class BaseActivity extends Activity {

    //1、首先声明一个数组permissions，将需要的权限都放在里面
    public String[] permissions = new String[]
            {Manifest.permission.READ_CONTACTS,Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.READ_SMS,Manifest.permission.RECEIVE_SMS,Manifest.permission.READ_PHONE_STATE};
    //2、创建一个mPermissionList，逐个判断哪些权限未授予，未授予的权限存储到mPerrrmissionList中
    public List<String> mPermissionList = new ArrayList<>();
    public final int mRequestCode = 100;//权限请求码
    String mPackName = "com.example.zbrj";

    public int baseHint = -1;
    public HintDialog hBaseDialog;
    public static String address="";//详细地址
    public static double Latitude=0;  //纬度
    public static double Longitude=0;  //经度

    public Dialog dialog1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //根据状态栏颜色来决定状态栏文字用黑色还是白色
        StatusBarUtil.setStatusBarMode(this, true, R.color.app_skin_black);

        hBaseDialog = new HintDialog(this, "提示", "", new HintDialog.HintDialogListener() {
            @Override
            public void onClick(boolean isConfirm) {
                if (baseHint==1&&isConfirm){
                    Uri packageURI = Uri.parse("package:" + mPackName);
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageURI);
                    startActivity(intent);
                }
            }
        });

    }

    private LocationManager lm;//位置管理
    private static final int LOCATION_CODE = 1;
    /**
     *  定位权限申请
     */
    public void GPSQunaxian(){
        lm = (LocationManager) this.getSystemService(this.LOCATION_SERVICE);
        boolean ok = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (ok) {//开了定位服务
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                Log.d("kly","没有权限");
                // 没有权限，申请权限。
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_CODE);
            } else {
                // 有权限了
                GPSUtils.getInstance(this);
            }
        } else {
            Log.d("kly","d系统检测到未开启GPS定位服务");
            Toast.makeText(this, "系统检测到未开启GPS定位服务", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivityForResult(intent, 1315);
        }
    }

    public void requireSomePermission() {
        mPermissionList.clear();//清空没有通过的权限

        //逐个判断你要的权限是否已经通过
        for (int i = 0; i < permissions.length; i++) {
            if (ContextCompat.checkSelfPermission(this, permissions[i]) != PackageManager.PERMISSION_GRANTED) {
                mPermissionList.add(permissions[i]);//添加还未授予的权限
            }
        }
    }

    public Dialog createLoadingDialog(Context context, String msg) {

        LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(R.layout.progress_diaolog_new, null);// 得到加载view
        LinearLayout layout = (LinearLayout) v.findViewById(R.id.dialog_view);// 加载布局
        // main.xml中的ImageView
        ImageView spaceshipImage = (ImageView) v.findViewById(R.id.img);
        TextView tipTextView = (TextView) v.findViewById(R.id.tipTextView);// 提示文字
        // 加载动画
        Animation hyperspaceJumpAnimation = AnimationUtils.loadAnimation(
                context, R.anim.loading_animation);
        // 使用ImageView显示动画
        spaceshipImage.startAnimation(hyperspaceJumpAnimation);
        tipTextView.setText(msg);// 设置加载信息

        Dialog loadingDialog = new Dialog(context, R.style.loading_dialog);// 创建自定义样式dialog

        loadingDialog.setCancelable(false);// 不可以用“返回键”取消
        loadingDialog.setContentView(layout, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.FILL_PARENT,
                LinearLayout.LayoutParams.FILL_PARENT));// 设置布局
        return loadingDialog;

    }

    /**
     * 关闭键盘
     */
    public void closeKeyboardBase() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm.isActive() && getCurrentFocus() != null) {
            if (getCurrentFocus().getWindowToken() != null) {
                imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }
    }

    // 提示对话框
    public void showBaseDialog(int h, int showType, String content) {
        baseHint = h;
        hBaseDialog.setButtonIsShow(showType);
        hBaseDialog.setContent(content);
        hBaseDialog.setCancelable(false);
        hBaseDialog.show();
    }
}
