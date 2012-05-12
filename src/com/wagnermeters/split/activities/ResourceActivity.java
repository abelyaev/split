package com.wagnermeters.split.activities;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import com.wagnermeters.split.R;
import com.wagnermeters.split.cproviders.SplitProvider;
import com.wagnermeters.split.activities.RCHostActivity;

public class ResourceActivity extends Activity {
	
	private class SplitWebViewClient extends WebViewClient {

	    public boolean shouldOverrideUrlLoading(WebView view, String url) {
	    	Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setData(Uri.parse(url));
			try {
				startActivity(intent);
			} catch (ActivityNotFoundException e) {}

	        return true;
	    }

	}
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.resource);
        
        int id = getIntent().getIntExtra("id", 0);
        int pid = getIntent().getIntExtra("pid", 0);
        updateInterface(id, pid);
        
        ((WebView)findViewById(R.id.full)).setWebViewClient(new SplitWebViewClient());
	}
	
	public void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		
		int id = intent.getIntExtra("id", 0);
		int pid = intent.getIntExtra("pid", 0);
		updateInterface(id, pid);
	}
	
	private void updateInterface(final int id, final int pid) {
        Cursor c = getContentResolver().query(
			SplitProvider.ARTICLES_URI,
			new String[] {"title", "teaser", "link"},
			"backend_id=?",
			new String[] {Integer.toString(id)},
			null
		);
        c.moveToFirst();
        
        ((TextView)findViewById(R.id.title)).setText(c.getString(0));
        ((TextView)findViewById(R.id.teaser)).setText(c.getString(1));
        findViewById(R.id.teaser).setVisibility(View.VISIBLE);
        findViewById(R.id.full).setVisibility(View.GONE);
        
        final ProgressDialog d = new ProgressDialog(this.getParent());
        
        final Handler h = new Handler() {
			
			public void handleMessage(Message msg) {
				d.dismiss();
				
				if(msg.what == 0) {
					String html = msg.getData().getString("html");
					
					((WebView)findViewById(R.id.full)).setBackgroundColor(0);
			        ((WebView)findViewById(R.id.full)).loadData("<style>a{color:#BF7C08!important}</style><div style=\"color:white!important;\">" + html + "</div>", "text/html", null);
				}
			}
			
		};
		
		Thread t = new Thread(new Runnable() {
			
			private String base_uri = "http://woodapp.moisturemeters.com/nodehtml/";

			public void run() {
				Bundle data = null;
				
				NetworkInfo info = ((ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
			    boolean connected = info != null ? info.isConnected() : false;
			    if(!connected) {
			    	h.sendMessage(h.obtainMessage(1));
			    	
			    	return;
			    }

				HttpClient client = new DefaultHttpClient();
				HttpGet request = new HttpGet(base_uri + Integer.toString(id));
				ResponseHandler<String> responseHandler = new BasicResponseHandler();
				try {
					JSONObject response = new JSONObject(client.execute(request, responseHandler));
					JSONObject html = (JSONObject)response.getJSONArray("nodes").get(0);
					String html_str = html.getString("html");
					
					Message msg = h.obtainMessage(0);
					data = new Bundle();
					data.putString("html", html_str);
					msg.setData(data);
					h.sendMessage(msg);
				} catch(JSONException e) {
				} catch (ClientProtocolException e) {
				} catch (IOException e) {
				}
			}
			
		});

		d.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		d.setCancelable(false);
		d.show();
		
		t.start();
        
        findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				switch(pid) {
					case 0:
						((RCHostActivity)getParent()).shareTab("ResourceCenter", ResourceCenterActivity.class, 0);
						break;
					case 1:
						((WMHostActivity)getParent()).shareTab("WagnerMeters", WagnerMetersActivity.class, 0);
						break;
					case 2:
						((HelpHostActivity)getParent()).shareTab("Help", HelpActivity.class, 0);
						break;
				}
			}
		});
        
        findViewById(R.id.read_more).setTag(c.getString(2));
        findViewById(R.id.read_more).setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setData(Uri.parse(v.getTag().toString()));
				startActivity(intent);
			}
		});
        
        findViewById(R.id.show_teaser).setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				findViewById(R.id.teaser).setVisibility(View.VISIBLE);
		        findViewById(R.id.full).setVisibility(View.GONE);
			}
		});
        
        findViewById(R.id.show_article).setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				findViewById(R.id.full).setVisibility(View.VISIBLE);
		        findViewById(R.id.teaser).setVisibility(View.GONE);
			}
		});
        
        c.close();
	}

}