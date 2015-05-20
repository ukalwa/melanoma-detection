package com.moledetection.android;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;


public class MainActivity extends Activity {
	private Button BeginBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    	final Context context = getApplicationContext();
    	final Intent listActivityIntent = new Intent(context, MyListActivity.class);
    	
        setContentView(R.layout.activity_main);
        
        BeginBtn = (Button) findViewById(R.id.angry_btn);
        BeginBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				startActivity(listActivityIntent);
			}
		});
    
    }
}
