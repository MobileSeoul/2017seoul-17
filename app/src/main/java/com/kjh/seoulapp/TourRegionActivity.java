package com.kjh.seoulapp;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.tsengvn.typekit.TypekitContextWrapper;

import net.daum.mf.map.api.MapPOIItem;
import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapView;

import java.util.ArrayList;
import java.util.List;

import static com.kjh.seoulapp.data.SharedData.*;

public class TourRegionActivity extends AppCompatActivity
		implements View.OnClickListener
{
    final String TAG = "TourRegionActivity";
    final int GPS_PERMISSION_REQUEST = 1235;
	static final int INFO_TAB = 0;
	static final int ROAD_TAB = 1;
	static final int QUIZ_START_TAB = 2;
	static final float DIST_LIMIT = 1000;
	static float distance;

    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(TypekitContextWrapper.wrap(newBase));
    }
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		/* auto-generated code */
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tour_region);

		/* init members */
		locRegion = new Location("locRegion");
		locRegion.setLatitude(cultural.latitude);
		locRegion.setLongitude(cultural.longitude);
		locNow = new Location("locNow");

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		SectionsPagerAdapter mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

		ViewPager mViewPager = (ViewPager) findViewById(R.id.container);
		mViewPager.setAdapter(mSectionsPagerAdapter);

		TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
		tabLayout.setupWithViewPager(mViewPager);

/*
       for(int num1=0 ; num1 < 2 ; num1++){
            ImageView img = new ImageView(this);
            img.setImageResource(R.drawable.t4+num1);
            flipper.addView(img);
        }
        Animation showIn= AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left);
        flipper.setInAnimation(showIn);
        flipper.setOutAnimation(this, android.R.anim.slide_out_right);
*/
/*
        toggleFlipping.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    flipper.setFlipInterval(2000);
                    flipper.startFlipping();
                }else{
                    flipper.stopFlipping();
                }
            }
        });
*/

		/* check gps permissions */
		if (ContextCompat.checkSelfPermission(TourRegionActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_DENIED)
			ActivityCompat.requestPermissions(TourRegionActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, GPS_PERMISSION_REQUEST);
		if (ContextCompat.checkSelfPermission(TourRegionActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED)
			ActivityCompat.requestPermissions(TourRegionActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, GPS_PERMISSION_REQUEST);

		// Acquire a reference to the system Location Manager
		LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		// GPS, 네트워크 프로바이더 사용가능여부
		boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
		boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
		Log.d(TAG, "isGPSEnabled=" + isGPSEnabled);
		Log.d(TAG, "isNetworkEnabled=" + isNetworkEnabled);

		final LocationListener locationListener = new LocationListener()
		{
			public void onLocationChanged(Location location)
			{
				double nowLatitude = location.getLatitude();
				double nowLongitude = location.getLongitude();
				//Log.d(TAG, "nowLatitude: " + nowLatitude + ", nowLongitude: " + nowLongitude);

				locNow.setLatitude(nowLatitude);
				locNow.setLongitude(nowLongitude);

				distance = locNow.distanceTo(locRegion);
				Log.d(TAG, "distance: " + distance);
			}
			public void onStatusChanged(String provider, int status, Bundle extras) { Log.d(TAG, "onStatusChanged"); }
			public void onProviderEnabled(String provider) { Log.d(TAG, "onProviderEnabled"); }
			public void onProviderDisabled(String provider) { Log.d(TAG, "onProviderDisabled"); }
		};

		if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
				ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
		{
			// Register the listener with the Location Manager to receive location updates
			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
			locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);

//						// 수동으로 위치 구하기
//						String locationProvider = LocationManager.GPS_PROVIDER;
//						Location lastKnownLocation = locationManager.getLastKnownLocation(locationProvider);
//						if (lastKnownLocation != null)
//						{
//							double lng = lastKnownLocation.getLatitude();
//							double lat = lastKnownLocation.getLatitude();
//							Log.d(TAG, "longtitude=" + lng + ", latitude=" + lat);
//						}
		}

		setTitle(cultural.title);
	}

    @Override
    public void onStart()
    {
        super.onStart();
    } // onStart()

	@Override
	public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults)
	{
		switch (requestCode)
		{
			case GPS_PERMISSION_REQUEST:
			{
				if (grantResults.length > 0)
				{
					for (int i = 0; i < grantResults.length; i++)
					{
						if (grantResults[i] == PackageManager.PERMISSION_GRANTED)
						{
							// granted
						}
						else
						{
							// permission denied
						}
					}
				}
				break;
			}
		} // switch()
	} // onRequestPermissionsResult()

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
//		getMenuInflater().inflate(R.menu.menu_search, menu);
//		return initSearch(menu);
        return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		int id = item.getItemId();

		if (id == R.id.action_search)
			return true;

		return super.onOptionsItemSelected(item);
	}

    @Override
    public void onClick(View v)
    {
        switch(v.getId())
        {
            case R.id.quiz_start:
                Intent intent = new Intent(TourRegionActivity.this, QuizProblemActivity.class);
				// TODO:
                startActivity(intent);
				finish();
                break;
        }
    }

	public static class PlaceholderFragment extends Fragment
			implements View.OnClickListener, MapView.OpenAPIKeyAuthenticationResultListener
	{
		private static final String ARG_SECTION_NUMBER = "section_number";
		ViewFlipper flipper;

		public static PlaceholderFragment newInstance(int sectionNumber)
		{
			PlaceholderFragment fragment = new PlaceholderFragment();
			Bundle args = new Bundle();
			args.putInt(ARG_SECTION_NUMBER, sectionNumber);
			fragment.setArguments(args);
			return fragment;
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
		{
			int sectionNumber = getArguments().getInt(ARG_SECTION_NUMBER);
			View rootView = null;

			Log.d("log", "onCreateView" + sectionNumber);

			switch (sectionNumber)
			{
				case INFO_TAB:
					rootView = inflater.inflate(R.layout.fragment_region_info, container, false);

					flipper = rootView.findViewById(R.id.ViewFlipperID);
					Button flipperPrev = rootView.findViewById(R.id.flipper_pre);
					Button flipperNext = rootView.findViewById(R.id.flipper_next);

					flipperPrev.setOnClickListener(this);
					flipperNext.setOnClickListener(this);

//					toggleFlipping = rootView.findViewById(R.id.toggle_auto);

					List<ImageView> imgViewList = new ArrayList<>();
					imgViewList.add((ImageView)rootView.findViewById(R.id.img1));
					imgViewList.add((ImageView)rootView.findViewById(R.id.img2));
					imgViewList.add((ImageView)rootView.findViewById(R.id.img3));

					int i=1;
					for(ImageView imgView : imgViewList)
					{
						Resources res = getResources();
						final int resourceId = res.getIdentifier("regpic_"+regionIndex+"_"+i, "drawable",
								getContext().getPackageName());
						imgView.setImageDrawable(res.getDrawable(resourceId));
						i++;
					}

					// 설명 텍스트뷰
					TextView infotextview = rootView.findViewById(R.id.infotext);
					infotextview.setText(cultural.content);

					break;
				case ROAD_TAB:
					//////////////////////////////////////////////////////////////////////
					//					Road Tab with Daum Map API begin				//
					//////////////////////////////////////////////////////////////////////
					rootView = inflater.inflate(R.layout.fragment_region_road, container, false);

					MapView mapView = new MapView(getActivity());
					mapView.setOpenAPIKeyAuthenticationResultListener(this);

					MapPoint mapPoint = MapPoint.mapPointWithGeoCoord(locRegion.getLatitude(), locRegion.getLongitude());
					mapView.setMapCenterPointAndZoomLevel(mapPoint, 2, true);
					MapPOIItem marker = new MapPOIItem();
					marker.setItemName("Default Marker");
					marker.setTag(0);
					marker.setMapPoint(mapPoint);
					marker.setMarkerType(MapPOIItem.MarkerType.BluePin); // 기본으로 제공하는 BluePin 마커 모양.
					marker.setSelectedMarkerType(MapPOIItem.MarkerType.RedPin); // 마커를 클릭했을때, 기본으로 제공하는 RedPin 마커 모양.

					mapView.addPOIItem(marker);

					ViewGroup mapViewContainer = rootView.findViewById(R.id.map_view);
					mapViewContainer.addView(mapView);

					TextView carLink = rootView.findViewById(R.id.carLink);
					TextView transitLink = rootView.findViewById(R.id.transitLink);
					TextView walkLink = rootView.findViewById(R.id.walkLink);

					carLink.setOnClickListener(this);
					transitLink.setOnClickListener(this);
					walkLink.setOnClickListener(this);

					break;
					//////////////////////////////////////////////////////////////////////
					//					Road Tab with Daum Map API end					//
					//////////////////////////////////////////////////////////////////////

				case QUIZ_START_TAB:
					rootView = inflater.inflate(R.layout.fragment_region_quiz_start, container, false);

					updateStampView(rootView);

					Button quizStart = rootView.findViewById(R.id.quiz_start);
					if (distance < DIST_LIMIT)
					{
						//quizStart.setText("문제풀이");
						quizStart.setEnabled(true);
					}
					else
					{
						//quizStart.setText("유적지에서만 가능합니다!");
						quizStart.setEnabled(false);
					}
					break;
			}
			return rootView;
		} // onCreateView()

		void updateStampView(View rootView)
		{
			List<ImageView> stampViewList = new ArrayList<>();
			stampViewList.add((ImageView)rootView.findViewById(R.id.my_stamp_image_1));
			stampViewList.add((ImageView)rootView.findViewById(R.id.my_stamp_image_2));
			stampViewList.add((ImageView)rootView.findViewById(R.id.my_stamp_image_3));

			int stampCnt = userData.stampList.get(regionIndex);

			for(int i=0;i<stampCnt;i++)
				stampViewList.get(i).setImageResource(R.drawable.stamp_color);
		}

		@Override
		public void onDaumMapOpenAPIKeyAuthenticationResult(MapView mapView, int i, String s)
		{
//			Log.d(TAG, "Daum Map API Auth: " + s);
		}

		@Override
		public void onClick(View v)
		{
			int id = v.getId();
			String strUri = "daummaps://route?sp=" + 37.537229 + "," + 127.005515 + "&ep=" + 37.4979502 + "," + "127.0276368&by=";

			switch(id)
			{
				case R.id.carLink:
					strUri +=  "CAR";
					break;
				case R.id.transitLink:
					strUri += "PUBLICTRANSIT";
					break;
				case R.id.walkLink:
					strUri += "FOOT";
					break;
				case R.id.flipper_pre:
					flipper.showPrevious();
					Log.d("flipper onClick", "showPrev()");
					return;
				case R.id.flipper_next:
					flipper.showNext();
					Log.d("flipper onClick", "showNext()");
					return;
			}

			try
			{
				Uri uri = Uri.parse(strUri);
				Intent intent = new Intent(Intent.ACTION_VIEW, uri);
				startActivity(intent);
			} catch (ActivityNotFoundException e) {
				Uri uri = Uri.parse("market://details?id=net.daum.android.map");
				Intent intent = new Intent(Intent.ACTION_VIEW, uri);
				startActivity(intent);
			}
		}
	}

	public class SectionsPagerAdapter extends FragmentStatePagerAdapter
	{
		final int COUNT = 3;

		public SectionsPagerAdapter(FragmentManager fm) { super(fm); }

		// getItem() -> newInstance() -> onCreateView()
		@Override
		public Fragment getItem(int position)
		{
			Log.d(TAG, "getItem() " + (position));
			return PlaceholderFragment.newInstance(position);
		}

		@Override
		public int getCount() { return COUNT; }

		@Override
		public CharSequence getPageTitle(int position)
		{
			switch (position)
			{
				case INFO_TAB: return "유적지 설명";
				case ROAD_TAB: return "가는길";
				case QUIZ_START_TAB: return "문제풀이&도장";
				default: return null;
			}
		}
	} // SectionPagerAdapter
} // TourRegionActivity
