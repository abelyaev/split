package com.wagnermeters.split.activities;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.wagnermeters.split.R;
import com.wagnermeters.split.cproviders.SplitProvider;
import com.wagnermeters.split.activities.RCHostActivity;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class ResourceCenterActivity extends Activity implements Serializable {

	private static final long serialVersionUID = 680546077992122603L;
	
	private class RCAdapter extends CursorAdapter {
		
		LayoutInflater inflater;

		public RCAdapter(Context context, Cursor c) {
			super(context, c);
			
			inflater = getLayoutInflater();
		}

		public void bindView(View view, Context context, Cursor cursor) {
			LinearLayout rc_item = (LinearLayout)view;

			rc_item.setOnClickListener(new View.OnClickListener() {

				public void onClick(View v) {
					ListView rc_list = (ListView)findViewById(R.id.rc);
					
					LinearLayout.LayoutParams closed = new LinearLayout.LayoutParams(
    					LinearLayout.LayoutParams.FILL_PARENT,
    					0
    				);
    				LinearLayout.LayoutParams opened = new LinearLayout.LayoutParams(
    					LinearLayout.LayoutParams.FILL_PARENT,
    					LinearLayout.LayoutParams.WRAP_CONTENT
    				);

    				LinearLayout item;
    				for(int i = 0; i < rc_list.getChildCount(); i++) {
    					item = (LinearLayout)rc_list.getChildAt(i);
    					item.getChildAt(1).setLayoutParams(closed);
    				}
    				
    				v.findViewWithTag("items").setLayoutParams(opened);
				}

			});
			
			TextView label = (TextView)rc_item.findViewWithTag("label");
			label.setText(cursor.getString(2));
			
			LinearLayout articles_holder = (LinearLayout)rc_item.findViewWithTag("items");
			articles_holder.removeAllViews();
			
			Cursor articles = getContentResolver().query(
				SplitProvider.ARTICLES_URI,
				new String[] {"backend_id", "title"},
				"deleted = ? AND category_id = ? AND section = 'rc'",
				new String[] {"0", cursor.getString(1)},
				"title"
			);
			if(articles.getCount() > 0) {
				articles.moveToFirst();
				while(!articles.isAfterLast()) {
					TextView article = new TextView(view.getContext());
					article.setGravity(Gravity.CENTER);
					article.setPadding(10, 10, 10, 10);
					article.setText(articles.getString(1));
					article.setTextSize(16);

					article.setTag(articles.getInt(0));
					article.setOnClickListener(new View.OnClickListener() {
						public void onClick(View v) {
							((RCHostActivity)getParent()).shareTab("Resource", ResourceActivity.class, (Integer)v.getTag());
						}
					});
					
					articles_holder.addView(article);
					articles.moveToNext();
				}
			}
			articles.close();
		}

		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			View rc_item = inflater.inflate(R.layout.rc_item, null);

			return rc_item;
		}
		
	}

	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.resource_center);
        
        Cursor rc = managedQuery(
        	SplitProvider.CATEGORIES_URI,
        	new String[] {"_id", "backend_id", "title"},
        	"deleted = ?",
        	new String[] {"0"},
        	"title"
        );
        RCAdapter rc_adapter = new RCAdapter(getParent(), rc);
        ListView rc_list = (ListView)findViewById(R.id.rc);
        rc_list.setAdapter(rc_adapter);
        rc_list.setDividerHeight(0);
	}
	
	public static byte[] getSerializedClass() {		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		byte[] data = new byte[0];
		try {
			new ObjectOutputStream(out).writeObject(ResourceCenterActivity.class);
			data = out.toByteArray();
	        out.close();
		} catch (IOException e) {}
		
		return data;
	}

}