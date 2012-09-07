package com.wagnermeters.split.activities;

import java.io.IOException;
import java.util.Random;

import org.apache.http.HttpResponse;
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
import android.content.ContentValues;
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
import android.widget.Toast;

import com.wagnermeters.split.R;
import com.wagnermeters.split.cproviders.SplitProvider;
import com.wagnermeters.split.activities.RCHostActivity;

public class ResourceActivity extends Activity {

	private int pid;

	private class SplitWebViewClient extends WebViewClient {

	    public boolean shouldOverrideUrlLoading(WebView view, String url) {
	    	Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setData(Uri.parse(url));
			try {
				startActivity(intent);
			} catch (ActivityNotFoundException e) {}

	        return true;
	    }

	    public void onPageFinished(WebView view, String url) {
	    	if(view.getTag().equals("ready")) {
	    		view.setVisibility(View.VISIBLE);
	    	}
	    }

	}

	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.resource);

        int id = getIntent().getIntExtra("id", 0);
        pid = getIntent().getIntExtra("pid", 0);
        updateInterface(id);

        ((WebView)findViewById(R.id.full)).setWebViewClient(new SplitWebViewClient());
        ((WebView)findViewById(R.id.full)).setBackgroundColor(0);
	}

	public void onNewIntent(Intent intent) {
		super.onNewIntent(intent);

		int id = intent.getIntExtra("id", 0);
		pid = intent.getIntExtra("pid", 0);
		updateInterface(id);
	}

	private void updateInterface(final int id) {
        Cursor c = getContentResolver().query(
			SplitProvider.RC_ARTICLE_URI,
			null,
			"a.backend_id = " + Integer.toString(id),
			null,
			null
		);
        if(c.getCount() == 0) {
        	c.close();
        	c = getContentResolver().query(
        		SplitProvider.ARTICLES_URI,
        		new String[] {"title", "teaser", "link"},
        		"backend_id = ?",
        		new String[] {Integer.toString(id)},
        		null
        	);
        }
        c.moveToFirst();

        ((TextView)findViewById(R.id.title)).setText(c.getString(0));

        //((TextView)findViewById(R.id.teaser)).setText(c.getString(1));
        //findViewById(R.id.teaser).setVisibility(View.GONE);

        WebView full = (WebView)findViewById(R.id.full);
        full.setVisibility(View.GONE);
        full.setTag("not-ready");
        full.loadData("<style>a{color:#BF7C08!important} h2{display:none} div.field-name-field-problem, div.field-name-field-tags{display:none}</style><div style=\"color:white!important;\">" + c.getString(1) + "</div>", "text/html", null);

        //findViewById(R.id.read_more).setVisibility(View.GONE);

        final ProgressDialog d = new ProgressDialog(this.getParent());

        final Handler h = new Handler() {

			public void handleMessage(Message msg) {
				d.dismiss();
				WebView full = (WebView)findViewById(R.id.full);

				if(msg.what == 0) {
					String html = msg.getData().getString("html");

					ContentValues values = new ContentValues();
					values.put("teaser", html);
					getContentResolver().update(
						SplitProvider.ARTICLES_URI,
						values,
						"backend_id=?",
						new String[] {Integer.toString(id)}
					);

					full.loadData("<style>a{color:#BF7C08!important} h2{display:none} div.field-name-field-problem, div.field-name-field-tags{display:none}</style><div style=\"color:white!important;\">" + html + "</div>", "text/html", null);
				} else {
					Toast.makeText(getParent(), getString(R.string.online), Toast.LENGTH_LONG).show();
				}

				full.setTag("ready");
				full.reload();
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
				Random randomGenerator = new Random();
				HttpGet request = new HttpGet(base_uri + Integer.toString(id) + "/" + Integer.toString(randomGenerator.nextInt(10000)));
				try {
					HttpResponse resp = client.execute(request);
					if(resp.getStatusLine().getStatusCode() != 200) {
						h.sendMessage(h.obtainMessage(1));

						return;
					}

					ResponseHandler<String> responseHandler = new BasicResponseHandler();
					JSONObject response = new JSONObject(responseHandler.handleResponse(resp));
					JSONObject html = (JSONObject)response.getJSONArray("nodes").get(0);
					String html_str = html.getString("html").replace("Â ", "");

					Message msg = h.obtainMessage(0);
					data = new Bundle();
					data.putString("html", html_str);
					msg.setData(data);
					h.sendMessage(msg);
				} catch(JSONException e) {
					h.sendMessage(h.obtainMessage(1));
					e.printStackTrace();
				} catch (ClientProtocolException e) {
					h.sendMessage(h.obtainMessage(1));
					e.printStackTrace();
				} catch (IOException e) {
					h.sendMessage(h.obtainMessage(1));
					e.printStackTrace();
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

        c.close();
	}

	public void onBackPressed() {
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

}