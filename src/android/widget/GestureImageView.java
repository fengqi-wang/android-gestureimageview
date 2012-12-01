package android.widget;

import android.content.Context;
import android.gestures.MoveGestureDetector;
import android.gestures.RotateGestureDetector;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

public class GestureImageView extends ImageView {

	public static final int CLICK = 3; // max delta of pixels between ACTION_DOWN and ACTION_UP (x/y)
	public static final float MAX_SCALE = 4.0f; 
	public static final float MIN_SCALE = 0.8f;
	
	private Matrix mMatrix = new Matrix();
    private float mScaleFactor = 1.0f;
    private float mRotationDegrees = 0.f;
    private float mFocusX = 0.f;
    private float mFocusY = 0.f;  
    private int mImageHeight, mImageWidth, mViewWidth, mViewHeight;
    
    // for historical event data
    private PointF start = new PointF();
    private PointF curr = new PointF();

    // gesture detectors
    private ScaleGestureDetector mScaleDetector;
    private RotateGestureDetector mRotateDetector;
    private MoveGestureDetector mMoveDetector;
    
    // advanced OnClick listener
    private OnClickListener mClickListener;
    private OnChangeMatrixListener mChangeMatrixListener;
    
    public GestureImageView(Context context) {
        super(context);
        init(context);
    }
    
    public GestureImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }
    
    // init setup
	private void init(Context context) {

		// View is scaled by matrix, so scale initially
		setImageMatrix(mMatrix);
		setScaleType(ScaleType.MATRIX);
		
		// Setup Gesture Detectors
		mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
		mRotateDetector = new RotateGestureDetector(context, new RotateListener());
		mMoveDetector = new MoveGestureDetector(context, new MoveListener());
	}
	
	public void setOnClickListener(OnClickListener listener) {
		mClickListener = listener;
	}
	
	public void setOnMatrixChangeListener(OnChangeMatrixListener listener) {
		mChangeMatrixListener = listener;
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		
		// can be called several times, logic only needs to be done once though
		if(mViewWidth == MeasureSpec.getSize(widthMeasureSpec) && mViewHeight == MeasureSpec.getSize(heightMeasureSpec)) return;
		
		mViewWidth = MeasureSpec.getSize(widthMeasureSpec);
		mViewHeight = MeasureSpec.getSize(heightMeasureSpec);
		
		Drawable drawable = getDrawable(); // if no drawable
        if (drawable == null || drawable.getIntrinsicWidth() == 0 || drawable.getIntrinsicHeight() == 0)
            return;
     	
		mImageHeight = drawable.getIntrinsicHeight();
		mImageWidth = drawable.getIntrinsicWidth();
		
		initialiseImage();
	}
	
	// can also be called externally to reset image to original state
	public void initialiseImage() {
		mMatrix.reset();
		
		// Fill screen with image
		float scaleX = (float) mViewWidth / (float) mImageWidth;
        float scaleY = (float) mViewHeight / (float) mImageHeight;
        mScaleFactor = Math.min(scaleX, scaleY);
        mMatrix.setScale(mScaleFactor, mScaleFactor);
        
        // Center the image
        float redundantYSpace = (float) mViewHeight - (mScaleFactor * (float) mImageHeight);
        float redundantXSpace = (float) mViewWidth - (mScaleFactor * (float) mImageWidth);
        redundantYSpace /= (float) 2;
        redundantXSpace /= (float) 2;
        mMatrix.postTranslate(redundantXSpace, redundantYSpace);
        
        // set initial focus values (otherwise jumps on first touch)
        mFocusX = redundantXSpace + getScaledImageCenterX();
        mFocusY = redundantYSpace +  getScaledImageCenterY();
        
        mRotationDegrees = 0f;
        mMatrix.postRotate(mRotationDegrees);
        
        setImageMatrix(mMatrix);
        
        if(mChangeMatrixListener != null) {
        	mChangeMatrixListener.onChange(this, getImageMatrix());
        }
	}
	
	private float getScaledImageCenterX() {
		return (mImageWidth * mScaleFactor) / 2;
	}
	
	private float getScaledImageCenterY() {
		return (mImageHeight * mScaleFactor) / 2;
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		mScaleDetector.onTouchEvent(event);
        mRotateDetector.onTouchEvent(event);
        mMoveDetector.onTouchEvent(event);
        
        float scaledImageCenterX = getScaledImageCenterX();
        float scaledImageCenterY = getScaledImageCenterY();
        
        float deltaX = mFocusX - scaledImageCenterX, deltaY = mFocusY - scaledImageCenterY;
        
        mMatrix.reset();
        mMatrix.postScale(mScaleFactor, mScaleFactor);
        mMatrix.postRotate(mRotationDegrees,  scaledImageCenterX, scaledImageCenterY);
        mMatrix.postTranslate(deltaX, deltaY);
        
        curr = new PointF(event.getX(), event.getY());
        if(event.getAction() == MotionEvent.ACTION_DOWN) {
        	start.set(curr); // track movement
        	// not best practice but is faster than batching Historical data (http://developer.android.com/reference/android/view/MotionEvent.html)
        }
        
		if(event.getAction() == MotionEvent.ACTION_UP) {
			
			int xDiff = (int) Math.abs(curr.x - start.x);
            int yDiff = (int) Math.abs(curr.y - start.y);
			
            // check if distance traveled in click threshold
            if (xDiff < CLICK && yDiff < CLICK) {
				performClick(); // default click
				
				if(mClickListener != null) { // advanced click
					Matrix inverse = new Matrix();
					mMatrix.invert(inverse);
					
					float[] points = new float[] { event.getX(), event.getY() };
					inverse.mapPoints(points);
					
					mClickListener.onClick(this, points[0], points[1]);
				}
            }
		}
		
		setImageMatrix(mMatrix);
		
		if(mChangeMatrixListener != null) {
        	mChangeMatrixListener.onChange(this, getImageMatrix());
        }

		return true; // indicate event was handled
	}
	
	public interface OnClickListener {
		public void onClick(GestureImageView view, float eventX, float eventY);
	}
	
	public interface OnChangeMatrixListener {
		public void onChange(GestureImageView view, Matrix matrix);
	}
	
	private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
		@Override
		public boolean onScale(ScaleGestureDetector detector) {
			mScaleFactor *= detector.getScaleFactor(); // scale change since previous event
			
			// Don't let the object get too small or too large.
			mScaleFactor = Math.max(MIN_SCALE, Math.min(mScaleFactor, MAX_SCALE)); 
			return true;
		}
	}
	
	private class RotateListener extends RotateGestureDetector.SimpleOnRotateGestureListener {
		@Override
		public boolean onRotate(RotateGestureDetector detector) {
			mRotationDegrees -= detector.getRotationDegreesDelta();
			return true;
		}
	}	
	
	private class MoveListener extends MoveGestureDetector.SimpleOnMoveGestureListener {
		@Override
		public boolean onMove(MoveGestureDetector detector) {
			PointF d = detector.getFocusDelta();
			mFocusX += d.x;
			mFocusY += d.y;		
			return true;
		}
	}
	
}
