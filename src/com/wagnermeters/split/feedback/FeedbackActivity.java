package com.wagnermeters.split.feedback;

import com.wagnermeters.split.R;
import com.wagnermeters.split.activities.HelpActivity;
import com.wagnermeters.split.activities.HelpHostActivity;
import com.wagnermeters.split.activities.RCHostActivity;
import com.wagnermeters.split.activities.ResourceCenterActivity;
import com.wagnermeters.split.activities.WMHostActivity;
import com.wagnermeters.split.activities.WagnerMetersActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

public class FeedbackActivity extends Activity {
	
	private int pid;
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.feedback);
        
        pid = getIntent().getIntExtra("pid", 0);

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
        ((EditText)findViewById(R.id.feedback)).setText("");
        findViewById(R.id.send_feedback).setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				try {
					EditText body = ((EditText)findViewById(R.id.feedback));
					if(body.getText().length() > 0) {
						GMailSender sender = new GMailSender("woodh2oandroid@gmail.com", "wagnermeters");
						sender.sendMail(
								"WoodH2O for Android Feedback",
								body.getText().toString(),
								"woodh2oandroid@gmail.com",
								"abel.the.first@gmail.com"
						);
						body.setText("");
					}
                } catch (Exception e) {
                    Log.e("SendMail", e.getMessage(), e);
                }
			}
		});
	}
	
	public void onNewIntent(Intent intent) {
		super.onNewIntent(intent);

		pid = intent.getIntExtra("pid", 0);
		
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
		((EditText)findViewById(R.id.feedback)).setText("");
		findViewById(R.id.send_feedback).setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				try {   
					EditText body = ((EditText)findViewById(R.id.feedback));
					if(body.getText().length() > 0) {
						GMailSender sender = new GMailSender("woodh2oandroid@gmail.com", "wagnermeters");
						sender.sendMail(
								"WoodH2O for Android Feedback",
								body.getText().toString(),
								"woodh2oandroid@gmail.com",
								"abel.the.first@gmail.com"
						);
						body.setText("");
					}
                } catch (Exception e) {
                    Log.e("SendMail", e.getMessage(), e);
                }
			}
		});
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
