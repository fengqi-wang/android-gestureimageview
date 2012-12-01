package ch.codepanda.gestureimage;

import android.app.Activity;
import android.graphics.Matrix;
import android.os.Bundle;
import android.util.SparseArray;
import android.widget.GestureImageView;
import android.widget.TextView;

public class GestureImageActivity extends Activity {

	private GestureImageView mImage;
	
	private SparseArray<TextView> mTextViews;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_gesture);
		
		mTextViews = new SparseArray<TextView>();
		
		for(int i=0, j=0, k=0; i < 9; i++) {
			mTextViews.put(i, (TextView)findViewById(getResources().getIdentifier(String.format("matrix_%d%d", j, k), "id", getPackageName())));
			if(k == 2) j++;
			k = k < 2 ? ++k : 0; 
		}
		
		mImage = (GestureImageView)findViewById(R.id.iv_gesture);
		mImage.setOnMatrixChangeListener(new GestureImageView.OnChangeMatrixListener() {
			public void onChange(GestureImageView view, Matrix matrix) {
				float[] values = new float[9];
				matrix.getValues(values);
				for(int i=0; i < values.length; i++) {
					mTextViews.get(i).setText(getResources().getString(getResources().getIdentifier("matrix_value_" + i, "string", getPackageName()), Float.toString(((int)(values[i] * 100)) / 100.0f)));
				}
				
			}
		});
	}
}
