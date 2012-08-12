package com.wagnermeters.split.activities;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.wagnermeters.split.R;
import com.wagnermeters.split.cproviders.SplitProvider;
import com.wagnermeters.split.feedback.FeedbackActivity;

public class HelpActivity extends Activity implements Serializable {

	private static final long serialVersionUID = -4864538468764827199L;

	private class HelpAdapter extends CursorAdapter {
		
		LayoutInflater inflater;

		public HelpAdapter(Context context, Cursor c) {
			super(context, c);
			
			inflater = getLayoutInflater();
		}

		public void bindView(View view, Context context, Cursor cursor) {
			LinearLayout list_item = (LinearLayout)view;
			
			list_item.setId(cursor.getInt(1));
			list_item.setTag(cursor.getInt(1));
			list_item.setOnClickListener(new View.OnClickListener() {

				public void onClick(View v) {
					if(v.getId() != 0) {
						((HelpHostActivity)getParent()).shareTab("Resource", ResourceActivity.class, (Integer)v.getTag());
					} else {
						((HelpHostActivity)getParent()).shareTab("Resource", FeedbackActivity.class, 0);
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
        setContentView(R.layout.help);
        
        Cursor help = managedQuery(
            SplitProvider.ARTICLES_URI,
            new String[] {"_id", "backend_id", "title"},
            "deleted = ? AND section = 'help'",
            new String[] {"0"},
            "title"
        );
        HelpAdapter help_adapter = new HelpAdapter(getParent(), help);
        ListView help_list = (ListView)findViewById(R.id.help);
        help_list.setAdapter(help_adapter);
        help_list.setDividerHeight(0);
	}
	
	public static byte[] getSerializedClass() {		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		byte[] data = new byte[0];
		try {
			new ObjectOutputStream(out).writeObject(HelpActivity.class);
			data = out.toByteArray();
	        out.close();
		} catch (IOException e) {}
		
		return data;
	}

}