package sdk.payexpresse.com;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Handler;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.util.Log;

import com.thefinestartist.finestwebview.FinestWebView;
import com.thefinestartist.finestwebview.listeners.WebViewListener;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;


public class PayExpresse {

    private HashMap<String,Object> params = new HashMap<>();
    private HashMap<String,String> headers = new HashMap<>();
    private Activity activity;
    private ProgressDialog progress;
    private String requestTokenUrl;
    private String requestMethod = "POST";
    private String errorMessage = "Erreur lors du chargement";
    private String loadingDialogTitle = "PayExpresse";
    private String loadingDialogText = "Chargement";
    private String tokenUrl;
    private String _cancelUrl = "https://payexpresse.com/mobile/cancel";
    private String _successUrl = "https://payexpresse.com/mobile/success";
    private FinestWebView finestWebView;
    private PCallback callback;

    public PayExpresse(@NonNull Activity activity, @NonNull String requestTokenUrl, @NonNull String requestMethod){
        this.activity = activity;
        this.requestTokenUrl = requestTokenUrl;
        this.requestMethod = requestMethod;
    }

    public PayExpresse(@NonNull Activity activity){
        this.activity = activity;
    }

    public PayExpresse(@NonNull Activity activity, @NonNull String requestTokenUrl){
        this.activity = activity;
        this.requestTokenUrl = requestTokenUrl;
    }


    public void send(){
        this.showLoading();

        final Handler handler = new Handler();

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    PayExpresse.this.fetchData();
                    Log.i("fetche", PayExpresse.this.tokenUrl);
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            PayExpresse.this.openWebView();
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            PayExpresse.this.dismissLoadingDialog();
                            PayExpresse.this.callback.onResult(PCallback.Result.ERROR);
                        }
                    });

                }
            }
        };

        new Thread(runnable).start();

    }


    private void fetchData() throws Exception {
            Log.i("url request", this.requestTokenUrl);

            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
            URL url = new URL(this.requestTokenUrl);
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod(this.requestMethod);
            this.setHeaders(conn);
            conn.setUseCaches(false);
            conn.setDoInput(true);
            conn.setDoOutput(true);


            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(os, "UTF-8"));
            writer.write(this.getQuery());
            writer.flush();
            writer.close();
            os.close();

            conn.connect();

            String result  = this.readInputStreamToString(conn);
            JSONObject obj = new JSONObject(result);
            Log.i("query_result", result + this.getQuery());


            if(obj.has("redirect_url"))
            {
                this.tokenUrl = obj.getString("redirect_url");
            }
            else{
                throw new Exception("Non expected response: " + result);
            }


    }


    private void closeWebView(){
        Intent activityPrevious = new Intent(this.activity, this.activity.getClass());
        activityPrevious.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        this.activity.startActivity(activityPrevious);
    }

    private void openWebView(){
        new FinestWebView
                .Builder(this.activity)
                .webViewJavaScriptEnabled(true)
                .webViewJavaScriptCanOpenWindowsAutomatically(true)
                .webViewSupportZoom(false)
                .webViewSupportMultipleWindows(true)
                .webViewAllowUniversalAccessFromFileURLs(true)
                .webViewLoadWithOverviewMode(true)
                .disableIconClose(true)
                .disableIconBack(true)
                .disableIconForward(true)
                .disableIconMenu(true)
                .showIconBack(false)
                .showIconClose(false)
                .showIconForward(false)
                .showIconMenu(false)
                .showMenuCopyLink(false)
                .showMenuFind(false)
                .showMenuShareVia(false)
                .showUrl(false)
                .showSwipeRefreshLayout(false)
                .titleDefault("PayExpresse")
                .setWebViewListener(new WebViewListener() {
                    @Override
                    public void onPageStarted(String url) {
                        super.onPageStarted(url);
                        Log.i("Overide url", url);

                        if(PayExpresse.this._cancelUrl.equals(url)) {
                            PayExpresse.this.callback.onResult(PCallback.Result.CANCEL);
                            PayExpresse.this.closeWebView();
                        }
                        else if(PayExpresse.this._successUrl.equals(url)) {
                            PayExpresse.this.callback.onResult(PCallback.Result.SUCCESS);
                            PayExpresse.this.closeWebView();
                        }
                    }
                })
                .show(this.tokenUrl);
        PayExpresse.this.dismissLoadingDialog();

    }

    private void showLoading()
    {
        Log.i("Loading", "loadinf");
        if (progress == null) {
            progress = new ProgressDialog(this.activity);
            progress.setTitle(this.loadingDialogTitle);
            progress.setMessage(this.loadingDialogText);

        }
        progress.show();
    }

    private void dismissLoadingDialog() {

        if (progress != null && progress.isShowing()) {
            progress.dismiss();
        }
    }

    private String getQuery() throws UnsupportedEncodingException
    {
        params.put("success_url", this._successUrl);
        params.put("cancel_url", this._cancelUrl);
        params.put("is_mobile", "1");

        StringBuilder result = new StringBuilder();
        boolean first = true;

        for (Map.Entry<String, Object> entry : this.params.entrySet())
        {
            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(String.valueOf(entry.getValue()), "UTF-8"));
        }


        return result.toString();
    }

    private void setHeaders(HttpURLConnection connection)
    {
        for (Map.Entry<String, String> entry : this.headers.entrySet())
        {
            connection.setRequestProperty(entry.getKey(), entry.getValue());
        }
    }

    private String readInputStreamToString(HttpURLConnection connection) {
        String result = null;
        StringBuffer sb = new StringBuffer();
        InputStream is = null;

        try {
            is = new BufferedInputStream(connection.getInputStream());
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String inputLine = "";
            while ((inputLine = br.readLine()) != null) {
                sb.append(inputLine);
            }
            result = sb.toString();
        }
        catch (Exception e) {
            Log.i("error", "Error reading InputStream");
            result = null;
            e.printStackTrace();
        }
        finally {
            if (is != null) {
                try {
                    is.close();
                }
                catch (IOException e) {
                    Log.i("error", "Error closing InputStream");
                    e.printStackTrace();
                }
            }
        }

        return result;
    }

    public PayExpresse setParams(HashMap<String, Object> params) {
        this.params = params;

        return this;
    }

    public PayExpresse setHeaders(HashMap<String, String> headers) {
        this.headers = headers;

        return this;
    }


    public PayExpresse setRequestTokenUrl(String requestTokenUrl) {
        this.requestTokenUrl = requestTokenUrl;

        return this;
    }

    public PayExpresse setRequestMethod(String requestMethod) {
        this.requestMethod = requestMethod;

        return this;
    }

    public PayExpresse setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;

        return this;
    }


    public PayExpresse setLoadingDialogText(String loadingDialogText) {
        this.loadingDialogText = loadingDialogText;

        return this;
    }

    public PayExpresse setCallback(PCallback callback) {
        this.callback = callback;
        return this;
    }
}

