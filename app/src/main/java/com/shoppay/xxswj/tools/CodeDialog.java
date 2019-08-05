package com.shoppay.xxswj.tools;

import android.app.Dialog;
import android.content.Context;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;
import com.loopj.android.http.RequestParams;
import com.shoppay.xxswj.MyApplication;
import com.shoppay.xxswj.R;
import com.shoppay.xxswj.bean.VipInfoMsg;
import com.shoppay.xxswj.http.InterfaceBack;

import org.json.JSONObject;
import org.w3c.dom.Text;

import cz.msebera.android.httpclient.Header;

/**
 * Created by Administrator on 2018/6/6 0006.
 */

public class CodeDialog {
    public static  String phonecode="";
    public static Dialog pwdDialog(final Context context, final Dialog loadingDialog, final String phone,
                                   int showingLocation, final InterfaceBack handle) {
        final Dialog dialog;
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.dialog_code, null);
        RelativeLayout rl_confirm = (RelativeLayout) view.findViewById(R.id.code_rl_confirm);
        final RelativeLayout rl_code = (RelativeLayout) view.findViewById(R.id.code_rl_fasong);
        final EditText et_pwd = (EditText) view.findViewById(R.id.code_et_code);
        final TextView tv_code = (TextView) view.findViewById(R.id.code_tv_code);
        dialog = new Dialog(context, R.style.DialogNotitle1);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        int screenWidth = ((WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay()
                .getWidth();
        dialog.setContentView(view, new LinearLayout.LayoutParams(
                screenWidth - 100, LinearLayout.LayoutParams.WRAP_CONTENT));
        dialog.show();
        MyTimer myTimer = new MyTimer(context, 90000,
                1000, tv_code, rl_code);
        myTimer.start();
        sendSms(loadingDialog,context,phone);
        rl_code.setOnClickListener(new NoDoubleClickListener() {
            @Override
            protected void onNoDoubleClick(View view) {
                MyTimer myTimer = new MyTimer(context, 90000,
                        1000, tv_code, rl_code);
                myTimer.start();
                sendSms(loadingDialog,context,phone);
            }
        });
        rl_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(null==et_pwd.getText().toString()||et_pwd.getText().toString().equals("")){
                    Toast.makeText(context,"请输入验证码",Toast.LENGTH_SHORT).show();
                }else {
                    if(et_pwd.getText().toString().equals(phonecode)) {
                        handle.onResponse(et_pwd.getText().toString());
                        dialog.dismiss();
                    }else{
                        Toast.makeText(context,"验证码错误",Toast.LENGTH_SHORT).show();
                    }
                }
//					vippwdchecked(type, context, et_pwd.getText().toString(), handle, dialog, false, null);
            }
        });
        Window window = dialog.getWindow();
        switch (showingLocation) {
            case 0:
                window.setGravity(Gravity.TOP); // 此处可以设置dialog显示的位置
                break;
            case 1:
                window.setGravity(Gravity.CENTER);
                break;
            case 2:
                window.setGravity(Gravity.BOTTOM);
                break;
            case 3:
                WindowManager.LayoutParams params = window.getAttributes();
                dialog.onWindowAttributesChanged(params);
                params.x = screenWidth - dip2px(context, 100);// 设置x坐标
                params.gravity = Gravity.TOP;
                params.y = dip2px(context, 45);// 设置y坐标
                Log.d("xx", params.y + "");
                window.setGravity(Gravity.TOP);
                window.setAttributes(params);
                break;
            default:
                window.setGravity(Gravity.CENTER);
                break;
        }
        return dialog;
    }

    private static void sendSms(final Dialog loaddialog, Context context, String phone) {
        loaddialog.show();
        AsyncHttpClient client = new AsyncHttpClient();
        final PersistentCookieStore myCookieStore = new PersistentCookieStore(context);
        client.setCookieStore(myCookieStore);
        RequestParams params = new RequestParams();
        params.put("mobile", phone);
        LogUtils.d("xxparams", params.toString());
        String url = UrlTools.obtainUrl(context, "?Source=3", "SendMessage");
        LogUtils.d("xxurl", url);
        client.post(url, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                try {
                    loaddialog.dismiss();
                    LogUtils.d("xxVipinfoS", new String(responseBody, "UTF-8"));
                    JSONObject jso = new JSONObject(new String(responseBody, "UTF-8"));
                    if (jso.getInt("flag") == 1) {
                       phonecode=jso.getString("msg");
                    } else {
                        Toast.makeText(MyApplication.context,jso.getString("msg"),Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    loaddialog.dismiss();
                    Toast.makeText(MyApplication.context,"服务器异常，请稍后再试",Toast.LENGTH_SHORT).show();
            }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                loaddialog.dismiss();
                Toast.makeText(MyApplication.context,"服务器异常，请稍后再试",Toast.LENGTH_SHORT).show();
            }
        });

    }

    public static int dip2px(Context context, float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

}
