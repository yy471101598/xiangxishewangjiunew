package com.shoppay.xxswj.crash;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.shoppay.xxswj.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * UncaughtException处理类,当程序发生Uncaught异常的时候,有该类来接管程序,并记录发送错误报告.
 * <p>
 * 需要在Application中注册，为了要在程序启动器就监控整个程序。
 */
public class CrashHandler implements UncaughtExceptionHandler {

    public static final String TAG = "CrashHandler";

    // 系统默认的UncaughtException处理类
    private UncaughtExceptionHandler mDefaultHandler;
    // CrashHandler实例
    private static CrashHandler instance;
    // 程序的Context对象
    private Context mContext;
    // 用来存储设备信息和异常信息
    private Map<String, String> infos = new HashMap<String, String>();

    // 用于格式化日期,作为日志文件名的一部分
    // yyyy-MM-dd-HH-mm-ss
    private DateFormat formatter = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");

    /**
     * 保证只有一个CrashHandler实例
     */
    private CrashHandler() {
    }

    /**
     * 获取CrashHandler实例 ,单例模式
     */
    public static CrashHandler getInstance() {
        if (instance == null)
            instance = new CrashHandler();
        return instance;
    }

    /**
     * 初始化
     */
    public void init(Context context) {
        mContext = context;
        // 获取系统默认的UncaughtException处理器
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        // 设置该CrashHandler为程序的默认处理器
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    /**
     * 当UncaughtException发生时会转入该函数来处理
     */
    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        if (!handleException(ex) && mDefaultHandler != null) {
            // 如果用户没有处理则让系统默认的异常处理器来处理
            mDefaultHandler.uncaughtException(thread, ex);
        } else {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                // Log.e(TAG, "error : ", e);
            }
            // 退出程序
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(1);
        }
    }

    /**
     * 自定义错误处理,收集错误信息 发送错误报告等操作均在此完成.
     *
     * @param ex
     * @return true:如果处理了该异常信息;否则返回false.
     */
    private boolean handleException(Throwable ex) {
        if (ex == null) {
            return false;
        }
        // 收集设备参数信息
        collectDeviceInfo(mContext);

        // 使用Toast来显示异常信息
        // new Thread() {
        // @Override
        // public void run() {
        // Looper.prepare();
        // Toast.makeText(mContext, "很抱歉,程序出现异常,即将退出.",
        // Toast.LENGTH_SHORT).show();
        // Looper.loop();
        // }
        // }.start();
        // 保存日志文件
        saveCatchInfo2File(ex);
        // Intent intent=new Intent(mContext,CrashService.class);
        // intent.putExtra("msg", msg);
        // intent.putExtra("type", "crash");
        // mContext.startService(intent);
        return false;
    }

    /**
     * 收集设备参数信息
     *
     * @param ctx
     */
    public void collectDeviceInfo(Context ctx) {
        try {
            SharedPreferences p = ctx.getSharedPreferences("ykdl",
                    Context.MODE_PRIVATE);

            PackageManager pm = ctx.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(ctx.getPackageName(), 0);
            if (pi != null) {
                String versionName = pi.versionName == null ? "null"
                        : pi.versionName;
                String versionCode = pi.versionCode + "";
                infos.put("AppversionName", mContext.getString(R.string.app_name));
                infos.put("AppversionCode", versionName);
                infos.put("Time",formatter.format(new Date()));
            }
        } catch (NameNotFoundException e) {
            Log.e(TAG, "an error occured when collect package info", e);
        }
//        Field[] fields = Build.class.getDeclaredFields();
//        for (Field field : fields) {
//            try {
//                field.setAccessible(true);
//                infos.put(field.getName(), field.get(null).toString());
//                // Log.d(TAG, field.getName() + " : " + field.get(null));
//            } catch (Exception e) {
//                Log.e(TAG, "an error occured when collect crash info", e);
//            }
//        }
    }

    /**
     * 保存错误信息到文件中
     *
     * @param ex
     * @return 返回文件名称, 便于将文件传送到服务器
     */
    private void saveCatchInfo2File(Throwable ex) {
        Log.d("crash", ex.getLocalizedMessage() + ";" + ex.getMessage());
        StringBuffer sb = new StringBuffer();
        for (Map.Entry<String, String> entry : infos.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            sb.append(key + "=" + value + "\n");
        }
        File file = new File(Environment.getExternalStorageDirectory(),
                "error.log");
        Writer writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        ex.printStackTrace(printWriter);
        Throwable cause = ex.getCause();
        while (cause != null) {
            cause.printStackTrace(printWriter);
            cause = cause.getCause();
        }
        printWriter.close();
        String result = writer.toString();
        sb.append(result);

        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {

            try {
                FileOutputStream fos = new FileOutputStream(file);
                fos.write(sb.toString().getBytes());
                fos.close();
            } catch (Exception e) {
            }
        } else {
            // 此时SDcard不存在或者不能进行读写操作的
        }

//        Map<String, Object> map = new HashMap<String, Object>();
//        map.put("CreateTime", formatter.format(new Date()));
//        Log.d("crash", formatter.format(new Date()));
//        map.put("EntityKey", UUID.randomUUID());
//        map.put("ErrorLevel", 2);
//        map.put("ExceptionMsg", sb.toString());
//        map.put("LogFrom", 304);
//        map.put("Message", "app报错");
//        if (CommonUtils.checkWifiConnect(mContext)) {
//            if (CommonUtils.getLocalIpAddress(mContext).length() > 12) {
//                map.put("IP",
//                        "error");
//            } else {
//                map.put("IP",
//                        CommonUtils.getLocalIpAddress(mContext));
//            }
//        } else {
//            if (CommonUtils.getIpAddress().length() > 12) {
//                map.put("IP", "error");
//            } else {
//                map.put("IP", CommonUtils.getIpAddress());
//            }
//        }
//        VolleyResponse.instance().getVolleyResponse(mContext,
//                ConstantUtil.RECORD + "/api/ErrorLog/WriteErrorLog", map,
//                new InterfaceVolleyResponse() {
//
//                    @Override
//                    public void onResponse(int code, Object response) {
//                        // TODO 自动生成的方法存根
//                        Log.d("crashS", response.toString());
//                    }
//
//                    @Override
//                    public void onErrorResponse(int code, Object msg) {
//                        // TODO 自动生成的方法存根
//                        Log.d("crashS", msg.toString());
//                    }
//                });
    }

    /**
     * 将捕获的导致崩溃的错误信息发送给开发人员
     * <p>
     * 目前只将log日志保存在sdcard 和输出到LogCat中，并未发送给后台。
     */
    private void sendCrashLog2PM(String fileName) {
        if (!new File(fileName).exists()) {
            Toast.makeText(mContext, "日志文件不存在！", Toast.LENGTH_SHORT).show();
            return;
        }
        FileInputStream fis = null;
        BufferedReader reader = null;
        String s = null;
        try {
            fis = new FileInputStream(fileName);
            reader = new BufferedReader(new InputStreamReader(fis, "GBK"));
            while (true) {
                s = reader.readLine();
                if (s == null)
                    break;
                // 由于目前尚未确定以何种方式发送，所以先打出log日志。
                Log.i("info", s.toString());
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally { // 关闭流
            try {
                reader.close();
                fis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
