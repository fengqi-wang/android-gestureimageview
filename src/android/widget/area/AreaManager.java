package android.widget.area;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.util.AttributeSet;
import android.util.FloatMath;
import android.util.Log;
import android.util.SparseArray;
import ch.codepanda.gestureimage.R;

public class AreaManager {

	private ArrayList<Area> mAreaList = new ArrayList<Area>();
	private SparseArray<Area> mIdToArea = new SparseArray<Area>();
	private Context mContext;
	
	private boolean hasMap = false;

	/**
	 * get the map name from the attributes and load areas from xml
	 * 
	 * @param attrs
	 */
	public AreaManager(Context context, AttributeSet attrs) {
		mContext = context;
		TypedArray a = mContext.obtainStyledAttributes(attrs, R.styleable.ImageMap);
		String map = a.getString(R.styleable.ImageMap_map);
		if (map != null) {
			loadMap(map);
		}
	}

	/**
	 * parse the maps.xml resource and pull out the areas
	 * 
	 * @param map
	 *            - the name of the map to load
	 */
	public void loadMap(String map) {
		boolean loading = false;
		mAreaList.clear(); // remove any skeletons
		mIdToArea.clear();
		try {
			XmlResourceParser xpp = mContext.getResources().getXml(mContext.getResources().getIdentifier("maps", "xml", mContext.getPackageName()));

			int eventType = xpp.getEventType();
			while (eventType != XmlPullParser.END_DOCUMENT) {
				if (eventType == XmlPullParser.START_DOCUMENT) {
					// Start document
					// This is a useful branch for a debug log if
					// parsing is not working
				} else if (eventType == XmlPullParser.START_TAG) {
					String tag = xpp.getName();

					if (tag.equalsIgnoreCase("map")) {
						String mapname = xpp.getAttributeValue(null, "name");
						if (mapname != null) {
							if (mapname.equalsIgnoreCase(map)) {
								loading = true;
							}
						}
					}
					if (loading) {
						if (tag.equalsIgnoreCase("area")) {
							Area a = null;
							String shape = xpp.getAttributeValue(null, "shape");
							String coords = xpp.getAttributeValue(null,
									"coords");
							String id = xpp.getAttributeValue(null, "id");

							// as a name for this area, try to find any of these
							// attributes
							// name attribute is custom to this impl (not
							// standard in html area tag)
							String name = xpp.getAttributeValue(null, "name");
							if (name == null) {
								name = xpp.getAttributeValue(null, "title");
							}
							if (name == null) {
								name = xpp.getAttributeValue(null, "alt");
							}

							if ((shape != null) && (coords != null)) {
								a = addShape(shape, name, coords, id);
								if (a != null) {
									// add all of the area tag attributes
									// so that they are available to the
									// implementation if needed (see
									// getAreaAttribute)
									for (int i = 0; i < xpp.getAttributeCount(); i++) {
										String attrName = xpp
												.getAttributeName(i);
										String attrVal = xpp.getAttributeValue(
												null, attrName);
										a.addValue(attrName, attrVal);
									}
								}
							}
						}
					}
				} else if (eventType == XmlPullParser.END_TAG) {
					String tag = xpp.getName();
					if (tag.equalsIgnoreCase("map")) {
						loading = false;
					}
				}
				eventType = xpp.next();
			}
			
			hasMap = true;
		} catch (XmlPullParserException xppe) {
			Log.e("loadMap::XmlPullParserException", "", xppe);
		} catch (IOException ioe) {
			Log.e("loadMap::IOException", "", ioe);
		}
	}
	
	public boolean hasMap() {
		return hasMap;
	}

	public Area addShape(String shape, String name, String coords, String id) {
		Area a = null;
		String rid = id.replace("@+id/", "");
		int resID = 0;

		try {
			resID = mContext.getResources().getIdentifier(rid, "id", mContext.getPackageName());
		} catch (Exception e) {
			resID = 0;
		}
		if (resID != 0) {
			if (shape.equalsIgnoreCase("rect")) {
				String[] v = coords.split(",");
				if (v.length == 4) {
					a = new RectArea(resID, name, Float.parseFloat(v[0]),
							Float.parseFloat(v[1]), Float.parseFloat(v[2]),
							Float.parseFloat(v[3]));
				}
			}
			if (shape.equalsIgnoreCase("circle")) {
				String[] v = coords.split(",");
				if (v.length == 3) {
					a = new CircleArea(resID, name, Float.parseFloat(v[0]),
							Float.parseFloat(v[1]), Float.parseFloat(v[2]));
				}
			}
			if (shape.equalsIgnoreCase("poly")) {
				a = new PolyArea(resID, name, coords);
			}
			if (a != null) {
				addArea(a);
			}
		}
		return a;
	}

	public void addArea(Area a) {
		mAreaList.add(a);
		mIdToArea.put(a.getId(), a);
	}

	/**
	 * Map tapped callback interface
	 */
	public interface OnClickedHandler {
		/**
		 * Area with 'id' has been tapped
		 * 
		 * @param id
		 */
		void onClick(int id);
	}

	private OnClickedHandler mClickHandler;

	public void setOnClickHandler(OnClickedHandler handler) {
		mClickHandler = handler;
	}

	public void click(float relativeX, float relativeY) {
		for (Area a : mAreaList) {
			if (a.isInArea(relativeX, relativeY)) {
				if (mClickHandler != null) {
					mClickHandler.onClick(a.getId());
				}
				// only fire click for one area
				break;
			}
		}
	}
	
	public abstract class Area {
		private int mResId;
		private String mName;
		private HashMap<String, String> mValues;

		public Area(int id, String name) {
			mResId = id;
			if (name != null) {
				mName = name;
			}
		}

		public int getId() {
			return mResId;
		}

		public String getName() {
			return mName;
		}

		// all xml values for the area are passed to the object
		// the default impl just puts them into a hashmap for
		// retrieval later
		public void addValue(String key, String value) {
			if (mValues == null) {
				mValues = new HashMap<String, String>();
			}
			mValues.put(key, value);
		}

		public String getValue(String key) {
			String value = null;
			if (mValues != null) {
				value = mValues.get(key);
			}
			return value;
		}

		abstract boolean isInArea(float x, float y);

		abstract float getOriginX();

		abstract float getOriginY();
	}
	
	public class CircleArea extends Area {
		private float mPointX;
		private float mPointY;
		private float mRadius;

		CircleArea(int id, String name, float x, float y, float radius) {
			super(id, name);
			mPointX = x;
			mPointY = y;
			mRadius = radius;
		}

		public boolean isInArea(float x, float y) {
			boolean rValue = false;

			float deltaX = mPointX - x;
			float deltaY = mPointY - y;

			// if tap is less than radius distance from the center
			float dist = FloatMath.sqrt((deltaX * deltaX) + (deltaY * deltaY));
			if (dist < mRadius) {
				rValue = true;
			}

			return rValue;
		}

		public float getOriginX() {
			return mPointX;
		}

		public float getOriginY() {
			return mPointY;
		}
	}
	
	public class PolyArea extends Area {
		private ArrayList<Integer> mPointsX = new ArrayList<Integer>();
		private ArrayList<Integer> mPointsY = new ArrayList<Integer>();

		// centroid point for this poly
		private float mPointX;
		private float mPointY;

		// number of points (don't rely on array size)
		private int mTotalPoints;

		// bounding box
		private int mBoundTop = -1;
		private int mBoundBottom = -1;
		private int mBoundLeft = -1;
		private int mBoundRight = -1;

		public PolyArea(int id, String name, String coords) {
			super(id, name);

			// split the list of coordinates into points of the
			// polygon and compute a bounding box
			String[] v = coords.split(",");

			for (int i = 0; i < v.length - 1; i += 2) {
				int x = Integer.parseInt(v[i]);
				int y = Integer.parseInt(v[i + 1]);
				mPointsX.add(x);
				mPointsY.add(y);
				mBoundTop = (mBoundTop == -1) ? y : Math.min(mBoundTop, y);
				mBoundBottom = (mBoundBottom == -1) ? y : Math.max(mBoundBottom, y);
				mBoundLeft = (mBoundLeft == -1) ? x : Math.min(mBoundLeft, x);
				mBoundRight = (mBoundRight == -1) ? x : Math.max(mBoundRight, x);
			}
			mTotalPoints = mPointsX.size();

			// add point zero to the end to make
			// computing area and centroid easier
			mPointsX.add(mPointsX.get(0));
			mPointsY.add(mPointsY.get(0));

			computeCentroid();
		}

		/**
		 * area() and computeCentroid() are adapted from the implementation of
		 * polygon.java published from a princeton case study The study is here:
		 * http://introcs.cs.princeton.edu/java/35purple/ The polygon.java source is
		 * here: http://introcs.cs.princeton.edu/java/35purple/Polygon.java.html
		 */

		// return area of polygon
		public double getArea() {
			double sum = 0.0;
			for (int i = 0, j = 1; i < mTotalPoints; i++, j++) {
				sum += (mPointsX.get(i) * mPointsY.get(j))
						- (mPointsY.get(i) * mPointsX.get(j));
			}
			sum *= 0.5;
			return Math.abs(sum);
		}

		// compute the centroid of the polygon
		public void computeCentroid() {
			double cx = 0.0, cy = 0.0;
			for (int i = 0; i < mTotalPoints; i++) {
				cx = cx
						+ (mPointsX.get(i) + mPointsX.get(i + 1))
						* (mPointsY.get(i) * mPointsX.get(i + 1) - mPointsX.get(i)
								* mPointsY.get(i + 1));
				cy = cy
						+ (mPointsY.get(i) + mPointsY.get(i + 1))
						* (mPointsY.get(i) * mPointsX.get(i + 1) - mPointsX.get(i)
								* mPointsY.get(i + 1));
			}
			double area = (6 * getArea());
			cx /= area;
			cy /= area;
			mPointX = Math.abs((int) cx);
			mPointY = Math.abs((int) cy);
		}

		@Override
		public float getOriginX() {
			return mPointX;
		}

		@Override
		public float getOriginY() {
			return mPointY;
		}

		/**
		 * This is a java port of the W. Randolph Franklin algorithm explained here
		 * http://www.ecse.rpi.edu/Homepages/wrf/Research/Short_Notes/pnpoly.html
		 */
		@Override
		public boolean isInArea(float testx, float testy) {
			int i, j;
			boolean c = false;
			for (i = 0, j = mTotalPoints - 1; i < mTotalPoints; j = i++) {
				if (((mPointsY.get(i) > testy) != (mPointsY.get(j) > testy))
						&& (testx < (mPointsX.get(j) - mPointsX.get(i))
								* (testy - mPointsY.get(i))
								/ (mPointsY.get(j) - mPointsY.get(i))
								+ mPointsX.get(i)))
					c = !c;
			}
			return c;
		}
	}
	
	public class RectArea extends Area {
		private float mBoundLeft;
		private float mBoundTop;
		private float mBoundRight;
		private float mBoundBottom;

		public RectArea(int id, String name, float left, float top, float right,
				float bottom) {
			super(id, name);
			mBoundLeft = left;
			mBoundTop = top;
			mBoundRight = right;
			mBoundBottom = bottom;
		}

		public boolean isInArea(float x, float y) {
			boolean rValue = false;
			if ((x > mBoundLeft) && (x < mBoundRight) && (y > mBoundTop) && (y < mBoundBottom)) {
				rValue = true;
			}
			return rValue;
		}

		public float getOriginX() {
			return mBoundLeft;
		}

		public float getOriginY() {
			return mBoundTop;
		}
	}
}