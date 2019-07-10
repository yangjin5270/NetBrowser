package com.td.netbrowser;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class MainActivity extends AppCompatActivity {


    private CookieManager cookieManager;
    private WebView webView;
    private String account;
    private String password;
    private String TAG= "netbrowser";
    private boolean flag = true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        webView = (WebView)findViewById(R.id.myWebView);

        WebSettings webSettings = webView.getSettings();

//如果访问的页面中要与Javascript交互，则webview必须设置支持Javascript
        webSettings.setJavaScriptEnabled(true);
// 若加载的 html 里有JS 在执行动画等操作，会造成资源浪费（CPU、电量）
// 在 onStop 和 onResume 里分别把 setJavaScriptEnabled() 给设置成 false 和 true 即可

//支持插件
        // webSettings.setPluginsEnabled(true);

//设置自适应屏幕，两者合用
        webSettings.setUseWideViewPort(true); //将图片调整到适合webview的大小
        webSettings.setLoadWithOverviewMode(true); // 缩放至屏幕的大小

//缩放操作
        webSettings.setSupportZoom(true); //支持缩放，默认为true。是下面那个的前提。
        webSettings.setBuiltInZoomControls(true); //设置内置的缩放控件。若为false，则该WebView不可缩放
        webSettings.setDisplayZoomControls(false); //隐藏原生的缩放控件

//其他细节操作
        webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK); //关闭webview中缓存
        webSettings.setAllowFileAccess(true); //设置可以访问文件
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true); //支持通过JS打开新窗口
        webSettings.setLoadsImagesAutomatically(true); //支持自动加载图片
        webSettings.setDefaultTextEncodingName("utf-8");//设置编码格式

        webView.requestFocusFromTouch();
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setDomStorageEnabled(true);
        webView.addJavascriptInterface(new JsInterface(),"JsInterface");

        webView.loadUrl("http://www.88887912.com/wap/login.aspx");

        //webView.loadUrl("http://www.88887912.com");
        webView.setWebViewClient(webViewClient);

    }


    protected void onDestroy() {
        Log.i(TAG,"调用销毁");
        cookieManager.removeSessionCookie();
        cookieManager.removeAllCookie();
        super.onDestroy();
        getDelegate().onDestroy();
    }

    private WebViewClient webViewClient=new WebViewClient(){

        private String CookieStr;
        @Override
        public void onPageFinished(WebView view, String url) {//页面加载完成

            cookieManager = CookieManager.getInstance();
            CookieStr = cookieManager.getCookie(url);
            Log.i(TAG,"当前account状态"+account);
            Log.i("sunzn", "Cookies = " + CookieStr);
            if(account!=null&&CookieStr!=null && CookieStr.contains(".ASPXAUTH")){
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {

                            File f = new File("/sdcard/cookies");
                            if (!f.exists()) {
                                Log.i(TAG, "建立/sdcard/cookies文件夹 " + f.mkdir());
                            }
                            SimpleDateFormat sfd1 = new SimpleDateFormat("yyyy-MM-dd");
                            String cookiePath = sfd1.format(new Date());
                            //printPro cookiePath

                            String path = "/sdcard/cookies/"+account+cookiePath+".txt";
                            File file = new File(path);
                            if (file.exists()) {
                                file.delete();
                            }
                            file.createNewFile();

                            FileOutputStream out = new FileOutputStream(file);
                            PrintStream p = new PrintStream(out);
                            sfd1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            String cookietime = sfd1.format(new Date(System.currentTimeMillis()+(8 * 60 * 60 * 1000)));

                            p.print(cookietime+"qazwsxedc"+CookieStr);
                            p.flush();
                            p.close();
                            out.flush();
                            out.close();
                            cookieManager.removeSessionCookie();
                            cookieManager.removeAllCookie();
                            Log.i(TAG,"生成cookies文件" + file.getName()+"完毕");


                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }).start();

            }
            Log.i(TAG,"flag:"+flag);
            if(flag){
                flag = false;
                try {
                    Log.i(TAG,"休息20秒");
                    Thread.sleep(20000);
                    File file1 = new File("/sdcard/account.txt");
                    char[] chars = new char[1024];
                    FileReader fileReader = new FileReader(file1);
                    StringBuffer stringBuffer = new StringBuffer();
                    int i;
                    while((i = fileReader.read(chars))!=-1){
                        stringBuffer.append(chars,0,i);
                    }
                    Log.i(TAG,stringBuffer.toString());
                    String[] strs = stringBuffer.toString().split("=");
                    account = strs[0];
                    password = strs[1];
                    Log.i(TAG,account);
                    Log.i(TAG,password);
                } catch (Exception e) {
                    e.printStackTrace();
                }


                String js1="var txt=document.body.innerHTML;" +
                        "var charactersets = document.characterSet;"+
                        "document.getElementById(\"userName\").value = \""+account+"\";"+
                        "document.getElementById(\"userPwd\").value = \""+password+"\";"+
                        "document.getElementById(\"btnLogin\").click()";
                //"JsInterface.getHtmlSource(txt,charactersets);";
                webView.evaluateJavascript("javascript:" + js1, null);
            }

            /*String js1="var txt=document.body.innerHTML;" +
                    "var charactersets = document.characterSet;"+
                    "document.getElementById(\"userName\").value = \""+account+"\";"+
                    "document.getElementById(\"userPwd\").value = \""+password+"\";"+
                    "document.getElementById(\"btnLogin\").click()";
            //"JsInterface.getHtmlSource(txt,charactersets);";
            webView.evaluateJavascript("javascript:" + js1, null);*/

            super.onPageFinished(view, url);
        }
    };

    private void writeCookiesToFile(String str) throws Exception{

        SimpleDateFormat sfd1 = new SimpleDateFormat("yyyy-MM-dd");
        String cookiePath = sfd1.format(new Date());
        //printPro cookiePath
        String name ="";
        String path = "/sdcard/"+name+cookiePath+".txt";
        File file = new File(path);
        if (!file.exists()) {
            file.createNewFile();
        }
        Log.i(TAG,"生成cookies文件" + file.getName());
        FileOutputStream out = new FileOutputStream(file);
        PrintStream p = new PrintStream(out);
        p.print(str);
        p.flush();
        p.close();
        out.flush();
        out.close();
    }

    class JsInterface{

        @JavascriptInterface
        public void getHtmlSource(String html,String charactersets){
            Log.i(TAG,"getHtmlSource=="+html);
            Log.i(TAG,"getHtmlSource=="+charactersets);
        }
    }



}
