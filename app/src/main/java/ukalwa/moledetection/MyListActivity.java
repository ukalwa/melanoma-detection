package ukalwa.moledetection;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class MyListActivity extends ListActivity {

	static final String[] ACTIVITY_CHOICES = new String[] {
		 "About the App",
		 "Take a Photo",
		 "Get stored pictures",
		 "Contact Us"
		};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    	final Context context = getApplicationContext();
        final Intent home_intent = new Intent(context, Home.class);
        final Intent prev_intent = new Intent(context, GetPreviousPictures.class);
        final Intent contact_intent = new Intent(context, ContactUs.class);
        final Intent cam_intent = new Intent(context, OpenCVCamera.class);
        
        setContentView(R.layout.activity_list);
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
            startActivity(prev_intent);
        break;
        case 3: 
            startActivity(contact_intent);
        break;
        default: break;
        }
        }		
    });
    
    }
}
