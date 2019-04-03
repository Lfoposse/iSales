package com.iSales.utility;

import android.content.Context;
import android.graphics.Bitmap;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

/**
 * Created by netserve on 04/01/2019.
 */

public class ISalesWebClient extends WebViewClient {
    private Context mContext;

    public ISalesWebClient(Context mContext) {
        this.mContext = mContext;
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        // TODO Auto-generated method stub
        super.onPageStarted(view, url, favicon);
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        // TODO Auto-generated method stub

        view.loadUrl(url);
        return true;

    }

    @Override
    public void onReceivedError(WebView view, int errorCode,
                                String description, String failingUrl) {
        // TODO Auto-generated method stub
        Toast.makeText(this.mContext, "Impossible de charger la page. " + description, Toast.LENGTH_SHORT).show();
        //super.onReceivedError(view, errorCode, description, failingUrl);
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        // TODO Auto-generated method stub
        super.onPageFinished(view, url);
    }
}
