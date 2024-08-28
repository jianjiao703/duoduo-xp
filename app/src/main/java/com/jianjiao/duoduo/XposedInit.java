package com.jianjiao.duoduo;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;

public class XposedInit implements IXposedHookLoadPackage {
    public static String uin = "";
    Context applicationContext;
    ClassLoader classLoader;
    Activity mActivity;
    public String TAG = "__尖叫__xp";
    String ACTIONR = "com.jianjiao.test.PDDGUANGBO";

    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) throws ClassNotFoundException {
        this.classLoader = lpparam.classLoader;
        if (!"com.xunmeng.pinduoduo".equals(lpparam.packageName)) {
            return;
        }
        XposedHelpers.findAndHookMethod("com.xunmeng.pinduoduo.ui.activity.HomeActivity", lpparam.classLoader, "onCreate", new Object[]{Bundle.class, new XC_MethodHook() {
            protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                XposedInit.this.mActivity = (Activity) param.thisObject;
                XposedInit xposedInit = XposedInit.this;
                xposedInit.applicationContext = xposedInit.mActivity.getApplicationContext();
                Toast.makeText(XposedInit.this.applicationContext, "插件已加载", 0);
                XposedInit.this.sendIntent(1, "插件已加载");
            }
        }});
        XposedHelpers.findAndHookMethod("com.xunmeng.pinduoduo.ui.activity.HomeActivity", lpparam.classLoader, "onStart", new Object[]{new XC_MethodHook() {
            protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
            }
        }});
        XposedHelpers.findAndHookMethod("com.aimi.android.common.http.HttpCall$Builder", lpparam.classLoader, "params", new Object[]{String.class, new XC_MethodHook() {
            protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
            }
        }});
        Class GsonClass = lpparam.classLoader.loadClass("com.google.gson.Gson");
        final Object gsonClass = XposedHelpers.newInstance(GsonClass, new Object[0]);
        Class<?> IntegrationRenderResponse = XposedHelpers.findClass("com.xunmeng.pinduoduo.goods.entity.IntegrationRenderResponse", lpparam.classLoader);
        XposedHelpers.findAndHookMethod("com.xunmeng.pinduoduo.goods.entity.GoodsResponse", lpparam.classLoader, "setRenderResponse", new Object[]{IntegrationRenderResponse, new XC_MethodHook() {
            public void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                Object renderResponse = param.args[0];
                final String str = (String) XposedHelpers.callMethod(gsonClass, "toJson", new Object[]{renderResponse});
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        XposedInit.this.sendIntent(9, str);
                        try {
                            JSONObject jsonObject = new JSONObject(str);
                            JSONObject goods = (JSONObject) jsonObject.get("goods");
                            goods.getString("goods_id");
                            XposedBridge.log("____获取渲染数据222:" + str);
                            File directory = XposedInit.this.applicationContext.getFilesDir();
                            File file = new File(directory, "goods_id.txt");
                            try {
                                FileWriter writer = new FileWriter(file);
                                try {
                                    writer.write(str);
                                    XposedBridge.log("文件已创建并写入成功：" + file.getAbsolutePath());
                                    writer.close();
                                } catch (Throwable th) {
                                    try {
                                        writer.close();
                                    } catch (Throwable th2) {
                                        th.addSuppressed(th2);
                                    }
                                    throw th;
                                }
                            } catch (IOException e) {
                                XposedBridge.log("写入文件时出错" + e);
                            }
                        } catch (JSONException e2) {
                            throw new RuntimeException(e2);
                        }
                    }
                }).start();
            }
        }});
        XposedHelpers.findAndHookMethod("com.xunmeng.moore.model.SupplementResponse", lpparam.classLoader, "getResult", new Object[]{new XC_MethodHook() {
            public void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                Object result;
                Object CoShootingModel;
                int i;
                super.afterHookedMethod(param);
                XposedBridge.log("_________获取result_________:" + param.getResult());
                Object result2 = param.getResult();
                if (result2 != null) {
                    int i2 = 0;
                    Object CoShootingModel2 = XposedHelpers.callMethod(result2, "getApodisInstepEntry", new Object[0]);
                    if (CoShootingModel2 != null) {
                        String videoUrl = (String) XposedHelpers.callMethod(CoShootingModel2, "getResourceUrl", new Object[0]);
                        Object fortunePrompts = XposedHelpers.getObjectField(result2, "fortunePrompts");
                        XposedBridge.log("视频下载地址:" + videoUrl);
                        List legosList = (List) XposedHelpers.getObjectField(fortunePrompts, "legos");
                        int i3 = 0;
                        while (i3 < legosList.size()) {
                            Object lego = legosList.get(i3);
                            Object prompt = XposedHelpers.getObjectField(lego, "prompt");
                            Object ext = XposedHelpers.getObjectField(prompt, "ext");
                            String json = (String) XposedHelpers.callMethod(ext, "toString", new Object[i2]);
                            XposedBridge.log("_________ext_________:" + ext);
                            XposedBridge.log("_________json_________:" + json);
                            JSONObject extJson = new JSONObject(json);
                            String goodsId = extJson.get("goods_id").toString();
                            String goodsName = extJson.get("goods_name").toString();
                            if (XposedInit.this.mActivity != null) {
                                result = result2;
                                CoShootingModel = CoShootingModel2;
                                i = i2;
                            } else {
                                Class PddActivityThread = lpparam.classLoader.loadClass("android.app.PddActivityThread");
                                result = result2;
                                i = 0;
                                Object pddActivityThread = XposedHelpers.newInstance(PddActivityThread, new Object[0]);
                                CoShootingModel = CoShootingModel2;
                                Context context = (Context) XposedHelpers.callMethod(pddActivityThread, "getApplication", new Object[0]);
                            }
                            XposedInit.this.sendIntent(10, goodsId + "|" + goodsName + "|" + videoUrl + "|" + XposedInit.uin);
                            i3++;
                            i2 = i;
                            result2 = result;
                            CoShootingModel2 = CoShootingModel;
                        }
                        return;
                    }
                    XposedBridge.log("_________获取视频下载地址_________:null");
                    return;
                }
                XposedBridge.log("_________获取result_________:null");
            }
        }});
        XposedHelpers.findAndHookMethod("com.xunmeng.moore.model.FeedModel$AuthorInfo", lpparam.classLoader, "getUin", new Object[]{new XC_MethodHook() {
            public void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                String uins = (String) param.getResult();
                XposedBridge.log("_________获取uin_________:" + uins);
                if (!uins.contains("null")) {
                    XposedInit.uin = (String) param.getResult();
                }
            }
        }});
    }

    public void sendIntent(int code, String data) {
        sendIntentSliced(data, code, 102400);
    }

    public void sendIntentSliced(String data, int code, int sliceSize) {
        List<String> slices = new ArrayList<>();
        int i = 0;
        while (i < data.length()) {
            slices.add(data.substring(i, Math.min(i + sliceSize, data.length())));
            i += sliceSize;
        }
        Log.d(this.TAG, "开始发送数据: " + data.length() + "| " + slices.size());
        for (int i2 = 0; i2 < slices.size(); i2++) {
            String sliceData = slices.get(i2);
            Intent intent = new Intent();
            intent.setAction(this.ACTIONR);
            intent.putExtra("code", code);
            intent.putExtra("index", i2);
            intent.putExtra("total", slices.size());
            intent.putExtra("data", sliceData);
            Log.d(this.TAG, "发送: " + i2 + "|" + slices.size() + "|" + sliceData);
            try {
                Activity activity = this.mActivity;
                if (activity == null) {
                    Class<?> PddActivityThread = this.classLoader.loadClass("android.app.PddActivityThread");
                    Object pddActivityThread = XposedHelpers.newInstance(PddActivityThread, new Object[0]);
                    Context appContext = (Context) XposedHelpers.callMethod(pddActivityThread, "getApplication", new Object[0]);
                    appContext.sendBroadcast(intent);
                    XposedBridge.log("发送广播: 自行获取application " + appContext);
                } else {
                    activity.sendBroadcast(intent);
                    XposedBridge.log("发送广播: 使用原context " + this.mActivity);
                }
            } catch (Exception e) {
                XposedBridge.log("发送消息失败: " + e);
                Log.d(this.TAG, "发送消息失败: " + e);
            }
        }
    }

    public class DataReceiver extends BroadcastReceiver {
        public DataReceiver() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            int code = intent.getIntExtra("code", 0);
            String data = intent.getStringExtra("data");
            Log.d(XposedInit.this.TAG, "监听到宿主消息: " + code + "|" + data);
        }
    }
}