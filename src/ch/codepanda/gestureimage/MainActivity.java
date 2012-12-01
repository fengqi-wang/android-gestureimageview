package ch.codepanda.gestureimage;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

public class MainActivity extends Activity {

	private Button mButton1;
	private Button mButton2;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        mButton1 = (Button)findViewById(R.id.btn_gestureimage);
        mButton1.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				startActivity(new Intent(MainActivity.this, GestureImageActivity.class));
			}
		});
        
        mButton2 = (Button)findViewById(R.id.btn_imagemapgestureview);
        mButton2.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				startActivity(new Intent(MainActivity.this, ImageMapActivity.class));
			}
		});
    }
 
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	if(item.getItemId() == R.id.menu_info) {
    		startActivity(new Intent(MainActivity.this, AboutActivity.class));
    		return true;
    	}
    	return super.onOptionsItemSelected(item);
    }
}
