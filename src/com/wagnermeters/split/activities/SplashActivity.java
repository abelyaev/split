package com.wagnermeters.split.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.wagnermeters.split.R;

public class SplashActivity extends Activity {
	
	private Runnable action;
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);
        
        action = new Runnable() {
			public void run() {
				startActivity(new Intent(SplashActivity.this, TabsActivity.class));
				finish();
			}
        };
        findViewById(R.id.splash_root).postDelayed(action, 3000);
	}
	
	public void onBackPressed() {
		findViewById(R.id.splash_root).removeCallbacks(action);
		
		super.onBackPressed();
	}

}