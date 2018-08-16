package ukalwa.moledetection;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.*;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;


public class MainActivity extends Activity {
	private static final String mDirName = "MoleDetection";
	private static final String TAG = "MoleDetection::Main";

	private static final String classifierFile = "opencv_svm.xml";
	private File mDirectory = new File(Environment.getExternalStorageDirectory(), "/" + mDirName);
	private File classifierXml = new File(mDirectory, "/" + classifierFile);
    private static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE  = 0;

	@SuppressLint("LongLogTag")
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    	final Context context = getApplicationContext();
    	final Intent listActivityIntent = new Intent(context, MyListActivity.class);
    	
        setContentView(R.layout.activity_main);
        Button beginBtn = (Button) findViewById(R.id.angry_btn);
        beginBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                startActivity(listActivityIntent);
            }
        });
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);

        }
        createNecessaryFiles();
    }

    private void createNecessaryFiles(){
        File mDirectory = new File(Environment.getExternalStorageDirectory(), "/" + mDirName);
        if(!mDirectory.exists())
        {
            Toast.makeText(this, "Directory : " + Environment.getExternalStorageDirectory() + "/" + mDirName, Toast.LENGTH_SHORT).show();
            Log.i(TAG, "Directory " + mDirName + " does not exist");
            if(!mDirectory.mkdir())
                Log.i(TAG, "Unable to create directory " + mDirName);
            else
                Log.i(TAG, "Directory " + mDirName + " Created");
        }

        if(!classifierXml.exists()){
            Log.i(TAG,"Classifier does not exist, Copying now");
            copyClassifier();
        }
    }

	private void copyClassifier() {
		AssetManager assetManager = getAssets();
		InputStream in = null;
		OutputStream out = null;
		try {
			in = assetManager.open(classifierFile);
			out = new FileOutputStream(classifierXml);
			copyFile(in, out);
		}
		catch (IOException e) {
			Log.e("tag", "Failed to load file : " + classifierFile, e);
		}
		finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
                    Log.e("tag", "Unable to close assetManager", e);
				}
			}
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
                    Log.e("tag", "Unable to close file output stream", e);
				}
			}
		}
	}

	private void copyFile(InputStream in, OutputStream out) throws IOException {
		byte[] buffer = new byte[1024];
		int read;
		while((read = in.read(buffer)) != -1){
			out.write(buffer, 0, read);
		}
	}

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    createNecessaryFiles();

                }
            }
        }
    }
}
