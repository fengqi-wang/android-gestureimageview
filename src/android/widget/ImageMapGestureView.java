package android.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.area.AreaManager;

public class ImageMapGestureView extends GestureImageView implements GestureImageView.OnClickListener {

	private AreaManager mAreaManager;
	
	public ImageMapGestureView(Context context) {
        super(context);
    }
	
	public ImageMapGestureView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mAreaManager = new AreaManager(context, attrs);
        setOnClickListener(this);
    }
	
	public void setAreaClickListener(AreaManager.OnClickedHandler listener) {
		if(mAreaManager != null) {
			mAreaManager.setOnClickHandler(listener);
		}
	}
	
	public void loadMap(String name) {
		if(mAreaManager != null && mAreaManager.hasMap()) {
			mAreaManager.loadMap(name);
		}
	}
	
	public AreaManager getAreaManager() {
		return mAreaManager;
	}

	public void onClick(GestureImageView view, float eventX, float eventY) {
		mAreaManager.click(eventX, eventY);
	}
	
}
