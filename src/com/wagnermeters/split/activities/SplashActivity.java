package com.wagnermeters.split.activities;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.Window;

import com.wagnermeters.split.R;

public class SplashActivity extends Activity {
	
	private Runnable action;
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);
        
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if(prefs.getInt("status_bar_height", -1) == -1) {
        	Timer timer = new Timer();
        	Date when = new Date(System.currentTimeMillis() + 1000);
        	timer.schedule(new TimerTask() {
    			public void run() {
    				SharedPreferences.Editor editor = prefs.edit();
    				editor.putInt("status_bar_height", getStatusBarHeight());
    				editor.commit();
    			}
    		}, when);
        }
        
        action = new Runnable() {
			public void run() {
				startActivity(new Intent(SplashActivity.this, TabsActivity.class));
				finish();
			}
        };
        findViewById(R.id.splash_root).postDelayed(action, 3000);
	}
	
	private int getStatusBarHeight() {
    	Rect rect = new Rect();
    	Window window = getWindow();
    	if(window != null) {
    		window.getDecorView().getWindowVisibleDisplayFrame(rect);
    		View v = window.findViewById(Window.ID_ANDROID_CONTENT);
    		
    		return getWindowManager().getDefaultDisplay().getHeight() - v.getBottom() + rect.top;   
    	}

    	return 0;
    }
	
	public void onBackPressed() {
		findViewById(R.id.splash_root).removeCallbacks(action);
		
		super.onBackPressed();
	}

}