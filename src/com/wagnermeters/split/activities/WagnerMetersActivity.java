package com.wagnermeters.split.activities;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.wagnermeters.split.R;
import com.wagnermeters.split.cproviders.SplitProvider;

public class WagnerMetersActivity extends Activity implements Serializable {

	private static final long serialVersionUID = -3842661333033076346L;

	private class WMAdapter extends CursorAdapter {
		
		LayoutInflater inflater;

		public WMAdapter(Context context, Cursor c) {
			super(context, c);
			
			inflater = getLayoutInflater();
		}

		public void bindView(View view, Context context, Cursor cursor) {
			LinearLayout list_item = (LinearLayout)view;
			
			list_item.setId(cursor.getInt(1));
			list_item.setTag(cursor.getString(3));
			list_item.setOnClickListener(new View.OnClickListener() {

				public void onClick(View v) {
					if(v.getId() != 0) {
						((WMHostActivity)getParent()).shareTab("Resource", ResourceActivity.class, v.getId());
					} else {
						File sd_card = Environment.getExternalStorageDirectory();
						File lookup = new File(sd_card, "lookup.jpg");
						try {
							FileOutputStream fos = new FileOutputStream(lookup);
							InputStream is = getResources().openRawResource(Integer.parseInt(v.getTag().toString()));
							
							byte[] buf = new byte[1024];
		        			int len;
		        			while((len = is.read(buf)) > 0) {
		        				fos.write(buf, 0, len);
		        			}
		        			
		        			is.close();
		        			fos.close();

		        			Intent i = new Intent(Intent.ACTION_VIEW);
		        			i.setDataAndType(Uri.fromFile(lookup), "image/*");
		        			startActivity(i);
						} catch (FileNotFoundException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}

			});
			
			TextView label = (TextView)list_item.findViewWithTag("label");
			label.setText(cursor.getString(2));
		}

		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			View list_item = inflater.inflate(R.layout.list_item, null);

			return list_item;
		}
		
	}
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wagner_meters);
        
        Cursor wm = managedQuery(
            SplitProvider.ARTICLES_URI,
            new String[] {"_id", "backend_id", "title", "link"},
            "deleted = ? AND section = 'wm'",
            new String[] {"0"},
            "backend_id"
        );
        WMAdapter wm_adapter = new WMAdapter(getParent(), wm);
        ListView wm_list = (ListView)findViewById(R.id.wm);
        wm_list.setAdapter(wm_adapter);
        wm_list.setDividerHeight(0);
	}
	
	public static byte[] getSerializedClass() {		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		byte[] data = new byte[0];
		try {
			new ObjectOutputStream(out).writeObject(WagnerMetersActivity.class);
			data = out.toByteArray();
	        out.close();
		} catch (IOException e) {}
		
		return data;
	}

}