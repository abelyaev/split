package com.wagnermeters.split.activities;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.wagnermeters.split.R;
import com.wagnermeters.split.cproviders.SplitProvider;
import com.wagnermeters.split.activities.RCHostActivity;

public class ResourceActivity extends Activity {
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.resource);
        
        int id = getIntent().getIntExtra("id", 0);
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
        
        findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				switch(getIntent().getIntExtra("pid", 0)) {
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

}