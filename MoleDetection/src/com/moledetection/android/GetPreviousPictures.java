package com.moledetection.android;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;

public class GetPreviousPictures extends Activity {
	public final static String FILEPATH = "MESSAGE";
    private static final String mDirName = "MoleDetection";
	List<String> tFileList;
    public Bitmap bm;
    ByteArrayOutputStream baos;
    ImageView jpgView;
    File f;
    File[] files;
    ImageView imageView;
	protected String filePath;
	private Button b2,proceed_button;
	private GridView g;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_previous_pictures);

        g = (GridView) findViewById(R.id.get_pictures);
        g.setAdapter(new ImageAdapter(this, ReadSDCard()));
        
        //registerForContextMenu(g);
        
        

        g.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> parent, View v,
                    int position, long id) {
                
    
                
                b2 = (Button) findViewById(R.id.button2);
                proceed_button = (Button) findViewById(R.id.proceed_button);
                imageView = (ImageView) findViewById(R.id.full_image_view);
                
                g.setVisibility(View.GONE);
                imageView.setVisibility(View.VISIBLE);
                b2.setVisibility(View.VISIBLE);
                proceed_button.setVisibility(View.VISIBLE);
               // Toast.makeText(GetPreviousPictures.this, "" + position,
                //        Toast.LENGTH_SHORT).show();
                
                System.out.println("Inside Onclick...............+position");

                //setContentView(R.layout.full_image);
                
                addListenerOnButton2();
                addListenerOnButtonProceed();

    System.out.println("inside the full image?????????????????????????????????");


                 //Intent i = new Intent(GetPreviousPictures.this, imageViewFlipper.class);
                // passing array index
                // i.putExtra("id", position);
                //  startActivity(i);
                System.out
                        .println("Inside the intent////////////////////////////////////");
                filePath = files[position].getPath();
                bm = BitmapFactory.decodeFile(filePath);
                imageView.setImageBitmap(bm);
                
                
                System.out
                        .println("Inside ihe image view///////////////////????????????????????????");
            }
        });
        //registerForContextMenu(g);
        
    }
    
    public void addListenerOnButton2() {
		 
		Button button = (Button) findViewById(R.id.button2);
		button.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				imageView.setVisibility(View.GONE);
                b2.setVisibility(View.GONE);
                proceed_button.setVisibility(View.GONE);
                g.setVisibility(View.VISIBLE);
			}
 
		});
 
	}
    
    public void addListenerOnButtonProceed() {
		 
		Button button = (Button) findViewById(R.id.proceed_button);
		button.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent i = new Intent(GetPreviousPictures.this, ProcessImage.class);
				i.putExtra(FILEPATH, filePath);
				startActivity(i);
			}
 
		});
 
	}

    private List<String> ReadSDCard() {
        tFileList = new ArrayList<String>();

        // It have to be matched with the directory in SDCard
        //f = new File("/data/digitechimages");// Here you take your specific folder//
        f = new File(Environment.getExternalStorageDirectory(), "/" + mDirName);

        files = f.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return ((name.endsWith(".jpg")) || (name.endsWith(".JPG")) ||(name.endsWith(".png")));
            }
        });

        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            /*
             * It's assumed that all file in the path are in supported type
             */
            tFileList.add(file.getPath());
        }
        return tFileList;
    }

    public class ImageAdapter extends BaseAdapter {
        int mGalleryItemBackground;
        private Context mContext;
        private List<String> FileList;

        public ImageAdapter(Context c, List<String> fList) {
            mContext = c;
            FileList = fList;
        }

        public int getCount() {
            return FileList.size();
        }

        public Object getItem(int position) {
            return position;
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView imageView;
            if (convertView == null) {
                imageView = new ImageView(mContext);
                Bitmap bm = BitmapFactory.decodeFile(FileList.get(position)
                        .toString());
                imageView.setImageBitmap(bm);
                // imageView.setImageResource(FileList[position]);
                imageView.setLayoutParams(new GridView.LayoutParams(175, 175));
                imageView.setAdjustViewBounds(true);
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                imageView.setPadding(8, 5, 8, 5);
                //imageView.setBackgroundResource(mGalleryItemBackground);

            } else {
                imageView = (ImageView) convertView;
            }

            // imageView.setImageResource(fi[position]);

            return imageView;
        }

    }

}