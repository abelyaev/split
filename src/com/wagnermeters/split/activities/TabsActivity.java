package com.wagnermeters.split.activities;

import android.app.NotificationManager;
import android.app.TabActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.TabHost;
import android.widget.TextView;

import com.wagnermeters.split.R;
import com.wagnermeters.split.services.FetchService;

public class TabsActivity extends TabActivity {
	
	private TabHost tabs;
	
	protected int current_tab = 0;
	
	protected int pending_tab = 0;
	
	protected int r_id = 0;
	
	private int n_id = 0;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tabs);
        
        startService(new Intent(this, FetchService.class));

        tabs = getTabHost();
        Bundle extra;
        tabs.addTab(createTab(tabs, "0", R.string.emc_calc_label, R.drawable.calc_tab, EmcCalculatorActivity.class, null));
        extra = new Bundle();
        extra.putString("startActivityTag", "ResourceCenter");
        extra.putByteArray("startActivityClass", ResourceCenterActivity.getSerializedClass());
        tabs.addTab(createTab(tabs, "1", R.string.res_center_label, R.drawable.rc_tab, RCHostActivity.class, extra));
        extra = new Bundle();
        extra.putString("startActivityTag", "WagnerMeters");
        extra.putByteArray("startActivityClass", WagnerMetersActivity.getSerializedClass());
        tabs.addTab(createTab(tabs, "2", R.string.wagner_meters_label, R.drawable.wm_tab, WMHostActivity.class, extra));
        extra = new Bundle();
        extra.putString("startActivityTag", "Help");
        extra.putByteArray("startActivityClass", HelpActivity.getSerializedClass());
        tabs.addTab(createTab(tabs, "3", R.string.help_label, R.drawable.help_tab, HelpHostActivity.class, extra));
        tabs.setCurrentTab(0);
        
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        int contentHeight = getWindowManager().getDefaultDisplay().getHeight() - prefs.getInt("status_bar_height", 0);        
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.FILL_PARENT, contentHeight);
        findViewById(android.R.id.tabhost).setLayoutParams(lp);
        
        tabs.setOnTabChangedListener(new TabHost.OnTabChangeListener() {

			public void onTabChanged(String tabId) {
				current_tab = Integer.parseInt(tabId);
			}

		});
    }
    
    public void onResume() {
    	super.onResume();
    	
    	if(n_id == 0) {
    		n_id = getIntent().getIntExtra("n_id", 0);
    	}
    	if(n_id != 0) {
			NotificationManager nm = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
			nm.cancel(n_id);
		}
    	
    	if(r_id == 0) {
    		r_id = getIntent().getIntExtra("r_id", 0);
    	}

    	if(pending_tab == 0) {
    		pending_tab = getIntent().getIntExtra("section", current_tab);
    	}

    	tabs.setCurrentTab(pending_tab);
    }
    
    public void onStop() {
    	super.onPause();
    	
    	pending_tab = 0;
    	r_id = 0;
    }
    
    protected void onNewIntent(Intent intent) {
    	super.onNewIntent(intent);

    	pending_tab = intent.getIntExtra("section", 0);
    	r_id = intent.getIntExtra("r_id", 0);
    	n_id = intent.getIntExtra("n_id", 0);
    }
    
    private TabHost.TabSpec createTab(TabHost tabs, String tag, int title, int icon, Class<?> intentClass, Bundle extra) {
    	TabHost.TabSpec spec;
    	
    	spec = tabs.newTabSpec(tag);
    	TextView indicator = new TextView(this);
    	indicator.setText(title);
    	indicator.setTextColor(getResources().getColor(android.R.color.white));
    	indicator.setGravity(Gravity.BOTTOM | Gravity.CENTER);
    	indicator.setBackgroundResource(icon);
    	//indicator.setScaleType(ImageView.ScaleType.FIT_END);
    	indicator.setPadding(1, 0, 1, 0);
        spec.setIndicator(indicator);
        if(extra == null) {
        	spec.setContent(new Intent(this, intentClass));
        } else {
        	Intent intent = new Intent(this, intentClass);
        	intent.putExtras(extra);
        	spec.setContent(intent);
        }
    	
    	return spec;
    }

}