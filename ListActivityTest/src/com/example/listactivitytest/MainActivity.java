package com.example.listactivitytest;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;


public class MainActivity extends ListActivity {

	static final String[] ACTIVITY_CHOICES = new String[] {
		 "Home",
		 "OpenCV Camera",
		 "Contact Us"
		};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    	final Context context = getApplicationContext();
        final Intent home_intent = new Intent(context, Home.class);
        final Intent intent = new Intent(context, ContactUs.class);
        final Intent cam_intent = new Intent(context, OpenCVCamera.class);
        
        //setContentView(R.layout.activity_main);
        setListAdapter(new ArrayAdapter<String>(this,
        		 android.R.layout.simple_selectable_list_item,
        		 ACTIVITY_CHOICES));
        		  getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        		  getListView().setTextFilterEnabled(true);
        
        getListView().setOnItemClickListener(new OnItemClickListener()
        {
        @Override
        public void onItemClick(AdapterView<?> arg0, View arg1,
        int arg2, long arg3) {
        switch(arg2) {
        case 0:
        startActivity(home_intent);
        break;
        case 1:
            startActivity(cam_intent);
        break;
        case 2: 
            startActivity(intent);
        break;
        default: break;
        }
        }		
    });
    
    }
}
