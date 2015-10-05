package etapps.library.instagram.oauth;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatDialog;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import etapps.library.R;

/**
 * Display 37Signals authentication dialog.
 *
 * @author Thiago Locatelli <thiago.locatelli@gmail.com>
 * @author Lorensius W. L T <lorenz@londatiga.net>
 */
public class InstagramDialog extends AppCompatDialog {

    static final FrameLayout.LayoutParams FILL = new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT);

    static final int MARGIN = 4;
    static final int PADDING = 40;

    private String mUrl;
    private OAuthDialogListener mListener;
    private ProgressDialog mSpinner;
    private WebView mWebView;
    private LinearLayout mContent;

    private static final String TAG = "Instagram-WebView";

    public InstagramDialog(Context context, String url,
                           OAuthDialogListener listener) {
        super(context);

        mUrl = url;
        mListener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.dialog);

        mSpinner = new ProgressDialog(getContext());
        mSpinner.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mSpinner.setMessage("Loading...");
        mContent = new LinearLayout(getContext());
        mContent.setOrientation(LinearLayout.VERTICAL);

        setUpWebView();

        final float scale;
        float[] dimensions = {300, 500};

        if (Build.VERSION.SDK_INT >= 17) {
            Display display = getWindow().getWindowManager().getDefaultDisplay();
            DisplayMetrics realMetrics = new DisplayMetrics();
            display.getRealMetrics(realMetrics);
            scale = realMetrics.density;
            dimensions[0] = realMetrics.widthPixels - PADDING;
            dimensions[1] = realMetrics.heightPixels - PADDING;
        } else {
            Display display = getWindow().getWindowManager().getDefaultDisplay();
            scale = getContext().getResources().getDisplayMetrics().density;
            dimensions[0] = display.getWidth() - PADDING;
            dimensions[1] = display.getHeight() - PADDING;
        }

        addContentView(mContent, new FrameLayout.LayoutParams(
                (int) (dimensions[0] * scale + 0.5f), (int) (dimensions[1]
                * scale + 0.5f)));

        CookieSyncManager.createInstance(getContext());
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.removeAllCookie();
    }

    @Override
    public boolean supportRequestWindowFeature(int featureId) {
        return supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
    }


    private void setUpWebView() {
        mWebView = (WebView) findViewById(R.id.webview);
        mWebView.setWebViewClient(new OAuthWebViewClient());
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.loadUrl(mUrl);
    }

    private class OAuthWebViewClient extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Log.d(TAG, "Redirecting URL " + url);

            if (url.startsWith(InstagramApp.mCallbackUrl)) {
                String urls[] = url.split("=");
                mListener.onComplete(urls[1]);
                InstagramDialog.this.dismiss();
                return true;
            }
            return false;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            Log.d(TAG, "Loading URL: " + url);

            super.onPageStarted(view, url, favicon);
            mSpinner.show();
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            Log.d(TAG, "onPageFinished URL: " + url);
            mSpinner.dismiss();
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);
            Log.d(TAG, "Page error: " + description);
            mListener.onError(description);
            InstagramDialog.this.dismiss();
        }

        @TargetApi(Build.VERSION_CODES.M)
        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            super.onReceivedError(view, request, error);
            Log.d(TAG, "Page error: " + error.getDescription());
            mListener.onError(error.getDescription().toString());
            InstagramDialog.this.dismiss();
        }
    }

    public interface OAuthDialogListener {
        public abstract void onComplete(String accessToken);

        public abstract void onError(String error);
    }

}