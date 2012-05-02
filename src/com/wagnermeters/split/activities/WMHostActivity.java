package com.wagnermeters.split.activities;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.StreamCorruptedException;

import android.app.ActivityGroup;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class WMHostActivity extends ActivityGroup {
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        String tag = getIntent().getExtras().getString("startActivityTag");
        byte[] in = getIntent().getExtras().getByteArray("startActivityClass");
        Class<?> start = null;
		try {
			ObjectInputStream in_stream = new ObjectInputStream(new ByteArrayInputStream(in));
			try {
				start = (Class<?>) in_stream.readObject();
			} catch (ClassNotFoundException e) {}
	        in_stream.close();
		} catch (StreamCorruptedException e) {} catch (IOException e) {}
        
        shareTab(tag, start, 0);
	}
	
	public void onResume() {
		super.onResume();
		
		int r_id = ((TabsActivity)getParent()).r_id;
		if(r_id != 0) {
			shareTab("Resource", ResourceActivity.class, r_id);
		}
	}
	
	public void shareTab(String tag, Class<?> activity, int id) {
		Intent i = new Intent(this, activity);
		i.putExtra("id", id);
		i.putExtra("pid", 1);
		View view = getLocalActivityManager().startActivity(tag, i).getDecorView();
		setContentView(view);
	}

}