package com.wagnermeters.split.activities;

import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.Locale;

import android.app.Activity;
import android.content.ContentProviderClient;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.wagnermeters.split.R;
import com.wagnermeters.split.cproviders.SplitProvider;

public class EmcCalculatorActivity extends Activity implements View.OnFocusChangeListener {
	
	private static int MIN_H = 0;
	
	private static int MAX_H = 100;
	
	private static int H_STEP = 1;
	
	private static int MIN_T = 0;
	
	private static int MAX_T = 150;
	
	private static int T_STEP = 1;
	
	private static int MIN_W = 0;
	
	private static int MAX_W = 30;

	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.emc_calculator);
        
        fillEMCTable();
        
        findViewById(R.id.get_emc).setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				EditText rh_input = (EditText)findViewById(R.id.rel_hum);
				EditText at_input = (EditText)findViewById(R.id.amb_temp);
				String rel_hum = rh_input.getText().toString();
				String amb_temp = at_input.getText().toString();
				if(rel_hum.length() == 0) {
					rh_input.setBackgroundResource(R.drawable.calc_input_error);
					rh_input.setTextColor(Color.RED);
					Toast.makeText(EmcCalculatorActivity.this, getString(R.string.rel_hum_empty), Toast.LENGTH_SHORT).show();
				} else if(amb_temp.length() == 0) {
					at_input.setBackgroundResource(R.drawable.calc_input_error);
					at_input.setTextColor(Color.RED);
					Toast.makeText(EmcCalculatorActivity.this, getString(R.string.amb_temp_empty), Toast.LENGTH_SHORT).show();
				} else {
					float T = Float.parseFloat(amb_temp);
					float h = Float.parseFloat(rel_hum);
					
					if(T < MIN_T || T > MAX_T) {
						at_input.setBackgroundResource(R.drawable.calc_input_error);
						at_input.setTextColor(Color.RED);
						Toast.makeText(EmcCalculatorActivity.this, getString(R.string.amb_temp_wrong), Toast.LENGTH_SHORT).show();
					} else if(h < MIN_H || h > MAX_H) {
						rh_input.setBackgroundResource(R.drawable.calc_input_error);
						rh_input.setTextColor(Color.RED);
						Toast.makeText(EmcCalculatorActivity.this, getString(R.string.rel_hum_wrong), Toast.LENGTH_SHORT).show();
					} else {
						at_input.setBackgroundResource(R.drawable.calc_input_default);
						at_input.setTextColor(Color.WHITE);
						rh_input.setBackgroundResource(R.drawable.calc_input_default);
						rh_input.setTextColor(Color.WHITE);

						((EditText)findViewById(R.id.emc)).setText(calculateEMC(h, T, 1));
						findViewById(R.id.emc).setTag(calculateEMC(h, T, 3));
					}
				}
			}
		});
        
        ((EditText)findViewById(R.id.emc)).addTextChangedListener(new TextWatcher(){

            public void afterTextChanged(Editable s) {}

            public void beforeTextChanged(CharSequence s, int start, int count, int after){
            	findViewById(R.id.emc).setTag(null);
            }

            public void onTextChanged(CharSequence s, int start, int before, int count){}

        });
        
        findViewById(R.id.get_temp).setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				EditText rh_input = (EditText)findViewById(R.id.rel_hum);
				EditText emc_input = (EditText)findViewById(R.id.emc);
				String rel_hum = rh_input.getText().toString();

				String emc;
				if(emc_input.getTag() != null) {
					emc = emc_input.getTag().toString();
				} else {
					emc = emc_input.getText().toString();
				}

				if(rel_hum.length() == 0) {
					rh_input.setBackgroundResource(R.drawable.calc_input_error);
					rh_input.setTextColor(Color.RED);
					Toast.makeText(EmcCalculatorActivity.this, getString(R.string.rel_hum_empty), Toast.LENGTH_SHORT).show();
				} else if(emc.length() == 0) {
					emc_input.setBackgroundResource(R.drawable.calc_input_error);
					emc_input.setTextColor(Color.RED);
					Toast.makeText(EmcCalculatorActivity.this, getString(R.string.emc_empty), Toast.LENGTH_SHORT).show();
				} else {
					float M = Float.parseFloat(emc);
					int h = Integer.parseInt(rel_hum);
					
					if(M < MIN_W || M > MAX_W) {
						emc_input.setBackgroundResource(R.drawable.calc_input_error);
						emc_input.setTextColor(Color.RED);
						Toast.makeText(EmcCalculatorActivity.this, getString(R.string.emc_wrong), Toast.LENGTH_SHORT).show();
					} else if(h < MIN_H || h > MAX_H) {
						rh_input.setBackgroundResource(R.drawable.calc_input_error);
						rh_input.setTextColor(Color.RED);
						Toast.makeText(EmcCalculatorActivity.this, getString(R.string.rel_hum_wrong), Toast.LENGTH_SHORT).show();
					} else {
						emc_input.setBackgroundResource(R.drawable.calc_input_default);
						emc_input.setTextColor(Color.WHITE);
						rh_input.setBackgroundResource(R.drawable.calc_input_default);
						rh_input.setTextColor(Color.WHITE);

						((EditText)findViewById(R.id.amb_temp)).setText(calculateTemp(h, M));
					}
				}
			}
        });
        
        final EditText rel_hum = (EditText)findViewById(R.id.rel_hum);
        final EditText amb_temp = (EditText)findViewById(R.id.amb_temp);
        final EditText emc = (EditText)findViewById(R.id.emc);
        rel_hum.setOnFocusChangeListener(this);
        rel_hum.setOnKeyListener(new View.OnKeyListener() {
			
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if(keyCode == KeyEvent.KEYCODE_ENTER) {
					if(event.getAction() == KeyEvent.ACTION_UP) {
						if(amb_temp.getText().length() == 0) {
							//v.clearFocus();
							amb_temp.requestFocus();
						} else if(rel_hum.getText().length() > 0) {
							findViewById(R.id.get_emc).performClick();
						} else {
							//v.clearFocus();
							emc.requestFocus();
						}
					}
					
					return true;
				}
				
				return false;
			}

		});
        
        amb_temp.setOnFocusChangeListener(this);
        amb_temp.setOnKeyListener(new View.OnKeyListener() {
			
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if(keyCode == KeyEvent.KEYCODE_ENTER) {
					if(event.getAction() == KeyEvent.ACTION_UP) {
						if(rel_hum.getText().length() == 0) {
							//v.clearFocus();
							emc.requestFocus();
						} else if(amb_temp.getText().length() > 0) {
							findViewById(R.id.get_emc).performClick();
						} else {
							//v.clearFocus();
							emc.requestFocus();
						}
					}
					
					return true;
				}
				
				return false;
			}

		});
        
        emc.setOnFocusChangeListener(this);
        emc.setOnKeyListener(new View.OnKeyListener() {
			
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if(keyCode == KeyEvent.KEYCODE_ENTER) {
					if(event.getAction() == KeyEvent.ACTION_UP) {
						if(rel_hum.getText().length() == 0) {
							//v.clearFocus();
							rel_hum.requestFocus();
						} else if(emc.getText().length() > 0) {
							findViewById(R.id.get_temp).performClick();
						} else {
							//v.clearFocus();
							rel_hum.requestFocus();
						}
					}
					
					return true;
				}
				
				return false;
			}

		});
	}
	
	public void onFocusChange(View v, boolean hasFocus) {
		((EditText)v).setTextColor(Color.WHITE);
		
		if(hasFocus) {
			v.setBackgroundResource(R.drawable.calc_input_focused);
		} else {
			v.setBackgroundResource(R.drawable.calc_input_default);
		}
	}
	
	private void fillEMCTable() {
		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		if(!prefs.getBoolean("emc2temp_table_ready", false)) {
			final Handler handler = new Handler() {
				public void handleMessage(Message msg) {
					SharedPreferences.Editor editor = prefs.edit();
					editor.putBoolean("emc2temp_table_ready", true);
					editor.commit();
				}
			};
			
			Thread filler = new Thread(new Runnable() {
				public void run() {
					try {
						InputStream in = getResources().getAssets().open("split.db");
						SplitProvider.importDB(getBaseContext(), in);
						in.close();
					} catch (IOException e1) {
						ContentProviderClient split_provider = getContentResolver().acquireContentProviderClient(SplitProvider.EMC2TEMP_URI);
						ContentValues[] db_string = new ContentValues[(MAX_H / H_STEP + 1) * (MAX_T / T_STEP + 1)];
						int i = 0;
						
						for(int h = MIN_H; h <= MAX_H; h+=H_STEP) {
							for(int T = MIN_T; T <= MAX_T; T+=T_STEP) {
								db_string[i] = new ContentValues();
								db_string[i].put("h", h);
								db_string[i].put("T", T);
								db_string[i].put("M", calculateEMC(h, T, 3));
								i++;
							}
						}
						
						try {
							split_provider.bulkInsert(SplitProvider.EMC2TEMP_URI, db_string);
						} catch (RemoteException e2) {}
						split_provider.release();
					}

					handler.sendMessage(handler.obtainMessage());
				}
			});
			filler.run();
		}
	}
	
	private String calculateEMC(float h, float T, int round) {
		float H = h / 100;
		double W = 330 + 0.452 * T + 0.00415 * T * T;
		double K = 0.791 + 0.000463 * T - 0.000000844 * T * T;
		double KH = K * H;
		double K1 = 6.34 + 0.000775 * T - 0.0000935 * T * T;
		double K2 = 1.09 + 0.0284 * T - 0.0000904 * T * T;
		double M = 1800 / W * (KH / (1 - KH) + ((K1 * KH + 2 * K1 * K2 * K * K * H * H) / (1 + K1 * KH + K1 * K2 * K * K * H * H)));
		
		DecimalFormat df = (DecimalFormat)DecimalFormat.getInstance(Locale.US);
		df.setMaximumFractionDigits(round);

		return df.format(M);
	}
	
	private String calculateTemp(int h_raw, float M_raw) {
		String T = "0";
		
		if(h_raw == 0) {
			return T;
		}
		
		Cursor result = getContentResolver().query(
			SplitProvider.EMC2TEMP_URI,
			new String[] {"T"},
			"h = ? AND M >= ?",
			new String[] {Integer.toString(h_raw), Float.toString(M_raw)},
			"M"
		);
		
		if(result == null || result.getCount() == 0) {
			Toast.makeText(EmcCalculatorActivity.this, getString(R.string.bad_emc), Toast.LENGTH_SHORT).show();
		} else {
			result.moveToFirst();
			T = result.getString(0);
		}
		result.close();
		
		return T;
	}

}