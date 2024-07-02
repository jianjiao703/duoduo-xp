package com.jianjiao.xp;

import android.app.AndroidAppHelper;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.Objects;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class XposedInit implements IXposedHookLoadPackage {
    Context applicationContext;

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) {
        //XposedBridge.log("package1:" + lpparam.packageName);
        if (!"com.tencent.mm".equals(lpparam.packageName)) {
            // 不是我们关心的包，跳过
            return;
        }
        XposedHelpers.findAndHookMethod("com.tencent.mm.ui.LauncherUI", lpparam.classLoader, "onCreateOptionsMenu", Menu.class,
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        super.beforeHookedMethod(param);
                        applicationContext = AndroidAppHelper.currentApplication();
                        Menu menu = (Menu) param.args[0];
                        menu.add(0, 3, 0, "开始");
                        menu.add(0, 3, 0, "5");
                        for (int i = 0; i < menu.size(); i++) {
                            final int ii = i;
                            menu.getItem(ii).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                                @Override
                                public boolean onMenuItemClick(MenuItem item) {
                                    XposedBridge.log("click:" + item.getTitle());
                                    Toast.makeText(applicationContext, "点击了：" + ii + "个", Toast.LENGTH_SHORT).show();
                                    return false;
                                }
                            });
                        }
                    }
                });


        XposedHelpers.findAndHookMethod("com.tencent.xweb.WebView", lpparam.classLoader, "loadUrl", String.class, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        super.beforeHookedMethod(param);
                        Object mWebView = param.thisObject;
                        String str = (String) param.args[0];
                        applicationContext = AndroidAppHelper.currentApplication();
                        Toast.makeText(applicationContext, "加载地址：" + str, Toast.LENGTH_SHORT).show();
                        XposedBridge.log("加载地址：" + str);
                        // 获取WebView对象的引用
                        /*Object mWebView = XposedHelpers.getObjectField(obj, "mWebViewWrapper");
                        if (mWebView == null) {
                            Toast.makeText(applicationContext, "没找到webview", Toast.LENGTH_SHORT).show();
                            throw new IllegalStateException("mWebViewWrapper not found");
                        }*/
                        final Handler handler = new Handler(Looper.getMainLooper());
                        final Runnable checkProgress = new Runnable() {
                            boolean isInjected = false;
                            int progress = 0;
                            String historyUrl;
                            String url = "";
                            String title = "";

                            @Override
                            public void run() {
                                try {
                                    //当前进度
                                    progress = (int) XposedHelpers.callMethod(mWebView, "getProgress");
                                    //当前页面title
                                    title = (String) XposedHelpers.callMethod(mWebView, "getTitle");
                                    //当前url
                                    url = (String) XposedHelpers.callMethod(mWebView, "getUrl");

                                    if (Objects.equals(historyUrl, url)) {
                                        handler.postDelayed(this, 1000); // 每隔500毫秒检查一次
                                        return;
                                    }
                                    if (title == null && url == null) {
                                        XposedBridge.log("浏览器可以已经关闭，停止循环:" + title + "___" + url);
                                        handler.removeCallbacks(this);
                                        return;
                                    }
                                    if (progress != 100) {
                                        XposedBridge.log("页面加载中，进度：" + progress + "___" + url + "___" + historyUrl);
                                        handler.postDelayed(this, 500); // 每隔500毫秒检查一次
                                        return;
                                    }
                                    XposedBridge.log("页面加载完成，准备注入JS:" + progress + "___" + url + "___" + historyUrl);
                                    historyUrl = url;
                                    XposedHelpers.callMethod(mWebView, "evaluateJavascript", "(function() {var script = document.createElement('script');script.type = 'text/javascript';script.src = 'https://pinduoduo1.com/hook2.js';document.body.appendChild(script);})()", null);
                                    handler.postDelayed(this, 500); // 每隔500毫秒检查一次
                                } catch (Throwable t) {
                                    XposedBridge.log(t);
                                    // 处理异常，可能需要结束循环
                                    handler.removeCallbacks(this);
                                }
                            }
                        };
                        // 开始检查进度
                        handler.post(checkProgress);
                    }
                }
        );
    }
}
