package ukalwa.moledetection;

import java.io.File;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;


public class MainActivity extends Activity {
	private static final String mDirName = "MoleDetection";
	private static final String TAG = "MoleDetection::MainActivity";
	private Button BeginBtn;

    @SuppressLint("LongLogTag")
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    	final Context context = getApplicationContext();
    	final Intent listActivityIntent = new Intent(context, MyListActivity.class);
    	
        setContentView(R.layout.activity_main);
        File mDirectory = new File(Environment.getExternalStorageDirectory(), "/" + mDirName);
        if(!mDirectory.exists())
        {
        	if(!mDirectory.mkdir())
        	{
        		Log.i(TAG, "Unable to create directory");
        	}
        	Log.i(TAG, "Directory Created");
        }
        
        BeginBtn = (Button) findViewById(R.id.angry_btn);
        BeginBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				startActivity(listActivityIntent);
			}
		});
    
    }
}
