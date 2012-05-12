package com.wagnermeters.split.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;

import com.wagnermeters.split.R;
import com.wagnermeters.split.activities.TabsActivity;
import com.wagnermeters.split.cproviders.SplitProvider;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class FetchService extends Service {
	
	NotificationManager nm;
	
	private final String LAST_RC_PREF = "last_RC_sync";
	
	private final int REFRESH_PERIOD = 3;
	
	Timer t;

	public IBinder onBind(Intent arg0) {
		return null;
	}
	
	public void onCreate() {
		nm = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
		
		final Handler h = new Handler() {
			
			public void handleMessage(Message msg) {
				Bundle notifications = msg.getData();
				if(notifications != null) {
					for(int i = 0; i < notifications.getInt("length"); i++) {
						showNotification(notifications.getStringArray((Integer.toString(i))));
					}
				}
			}
			
		};
		
		t = new Timer();
		t.schedule(new TimerTask() {
			
			private String base_uri = "http://woodapp.moisturemeters.com/sync/";

			private long last_sync;

			public void run() {
				Bundle notifications = null;
				
				NetworkInfo info = ((ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
			    boolean connected = info != null ? info.isConnected() : false;
			    if(!connected) {
			    	return;
			    }

				SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
				last_sync = prefs.getLong(LAST_RC_PREF, 0);
				HttpClient client = new DefaultHttpClient();
				HttpGet request = new HttpGet(base_uri + Long.toString(last_sync));
				ResponseHandler<String> responseHandler = new BasicResponseHandler();
				try {
					JSONObject response = new JSONObject(client.execute(request, responseHandler));
					Cursor c;

					JSONArray categories = response.getJSONArray("categories");
					JSONObject category;
					int length = categories.length();
					ContentValues values;
					for(int i = 0; i < length; i++) {
						category = categories.getJSONObject(i);
						values = new ContentValues();
						values.put("backend_id", category.getInt("id"));
						values.put("title", category.getString("title"));
						values.put("deleted", category.getInt("deleted"));
						values.put("type", "categories");
						
						c = getContentResolver().query(
							SplitProvider.CATEGORIES_URI,
							new String[] {"_id"},
							"backend_id=?",
							new String[] {Integer.toString(category.getInt("id"))},
							null
						);
						if(c.getCount() == 0) {
							getContentResolver().insert(SplitProvider.CATEGORIES_URI, values);
						} else {
							getContentResolver().update(
								SplitProvider.CATEGORIES_URI,
								values,
								"backend_id=?",
								new String[] {Integer.toString(category.getInt("id"))}
							);
						}
						c.close();
					}
					
					categories = response.getJSONArray("jobtypes");
					length = categories.length();
					for(int i = 0; i < length; i++) {
						category = categories.getJSONObject(i);
						values = new ContentValues();
						values.put("backend_id", category.getInt("id"));
						values.put("title", category.getString("title"));
						values.put("deleted", 0);
						values.put("type", "jobtypes");
						
						c = getContentResolver().query(
							SplitProvider.CATEGORIES_URI,
							new String[] {"_id"},
							"backend_id=?",
							new String[] {Integer.toString(category.getInt("id"))},
							null
						);
						if(c.getCount() == 0) {
							getContentResolver().insert(SplitProvider.CATEGORIES_URI, values);
						} else {
							getContentResolver().update(
								SplitProvider.CATEGORIES_URI,
								values,
								"backend_id=?",
								new String[] {Integer.toString(category.getInt("id"))}
							);
						}
						c.close();
					}
					
					JSONArray articles = response.getJSONArray("articles");
					JSONObject article;
					length = articles.length();
					if(last_sync != 0 && length > 0) {
						notifications = new Bundle();
						notifications.putInt("length", length);
					}
					for(int i = 0; i < length; i++) {
						article = articles.getJSONObject(i);
						values = new ContentValues();
						values.put("backend_id", article.getInt("nid"));
						values.put("category_id", article.getInt("category_id"));

						JSONArray types = article.getJSONArray("tags");
						getContentResolver().delete(
							SplitProvider.ARTICLES_CATEGORIES_URI,
							"article_id=?",
							new String[] {Integer.toString(article.getInt("nid"))}
						);
						for(int j = 0; j < types.length(); j++) {
							ContentValues cp_values = new ContentValues();
							cp_values.put("category_id", types.getJSONObject(j).getInt("tid"));
							cp_values.put("article_id", article.getInt("nid"));

							getContentResolver().insert(SplitProvider.ARTICLES_CATEGORIES_URI, cp_values);
						}

						switch(article.getInt("section_id")) {
							case 2:
								values.put("section", "rc");
								break;
							case 3:
								values.put("section", "wm");
								break;
							case 4:
								values.put("section", "help");
								break;
							default:
								values.put("section", "push");
						}

						values.put("title", article.getString("title"));
						values.put("teaser", article.getString("teaser"));
						values.put("link", article.getString("link"));
						values.put("deleted", article.getInt("deleted"));
						
						c = getContentResolver().query(
							SplitProvider.ARTICLES_URI,
							new String[] {"_id"},
							"backend_id=?",
							new String[] {Integer.toString(article.getInt("nid"))},
							null
						);
						if(c.getCount() == 0) {
							getContentResolver().insert(SplitProvider.ARTICLES_URI, values);
						} else {
							getContentResolver().update(
								SplitProvider.ARTICLES_URI,
								values,
								"backend_id=?",
								new String[] {Integer.toString(article.getInt("nid"))}
							);
						}
						c.close();
						
						if(last_sync != 0) {
							String[] notification = new String[] {
								Integer.toString(article.getInt("section_id")),
								Integer.toString(article.getInt("nid")),
								values.getAsString("title"),
								values.getAsString("link")
							};
							notifications.putStringArray(Integer.toString(i), notification);
						}
					}
				} catch (ClientProtocolException e) {
				} catch (IOException e) {
				} catch (JSONException e) {
				}
				
				SharedPreferences.Editor editor = prefs.edit();
				editor.putLong(LAST_RC_PREF, System.currentTimeMillis() / 1000);
				editor.commit();

				Message msg = h.obtainMessage();
				msg.setData(notifications);
				h.sendMessage(msg);
			}
			
		}, 0, REFRESH_PERIOD * 60 * 1000);
    }
	
	public void onDestroy() {
		t.cancel();
	}
	
	private void showNotification(String[] notification_data) {
		int section = Integer.parseInt(notification_data[0]);
		int r_id = Integer.parseInt(notification_data[1]);
		CharSequence title;
		Intent intent = new Intent(this, TabsActivity.class);
		//intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		switch(section) {
			case 2:
				intent.putExtra("section", 1);
				intent.putExtra("r_id", r_id);
				title = getText(R.string.new_rc);
				break;
			case 3:
				intent.putExtra("section", 2);
				intent.putExtra("r_id", r_id);
				title = getText(R.string.new_wm);
				break;
			case 4:
				intent.putExtra("section", 3);
				intent.putExtra("r_id", r_id);
				title = getText(R.string.new_help);
				break;
			default:
				title = getText(R.string.new_push);
				intent = new Intent(Intent.ACTION_VIEW);
				intent.setData(Uri.parse(notification_data[3]));
		}
		
        Notification notification = new Notification(R.drawable.icon_notify, title, System.currentTimeMillis());
        int id = (int)(Math.random() * 1000);
        intent.putExtra("n_id", id);
        PendingIntent contentIntent = PendingIntent.getActivity(this, id, intent, 0);
        notification.setLatestEventInfo(this, title, notification_data[2], contentIntent);
        nm.notify(id, notification);
    }

}