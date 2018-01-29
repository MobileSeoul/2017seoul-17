package com.kjh.seoulapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.util.ArrayMap;
import android.support.v4.util.Pair;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.text.Layout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Context;
import com.tsengvn.typekit.TypekitContextWrapper;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kjh.seoulapp.data.CulturalData;
import com.kjh.seoulapp.data.SharedData;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.kjh.seoulapp.data.SharedData.CULTURAL_REF;
import static com.kjh.seoulapp.data.SharedData.DATA_NAME;
import static com.kjh.seoulapp.data.SharedData.POPUP_TYPE;
import static com.kjh.seoulapp.data.SharedData.regionIndex;
import static com.kjh.seoulapp.data.SharedData.userData;

public class TourMainActivity extends GoogleApiClientActivity
		implements View.OnClickListener, NavigationView.OnNavigationItemSelectedListener
{
	enum MAP_TYPE { FULL, MID, EAST, WEST, SOUTH, NORTH}
	final long FINISH_INTERVAL_TIME = 2000;
	final String TAG = "TourMainActivity";

	long backPressedTime;
	FirebaseAuth auth;
	MAP_TYPE nowMap;
	ImageView fullmapView;
	Map<MAP_TYPE, ImageView> mapMap;
	List<Button> hiddenBtnList;
	Map<MAP_TYPE, List<ImageButton>> mapRegion;
	Button btnTabMap, btnTabStampAll;
	ViewGroup tabMap, tabStampAll;
	List<Pair<Integer, Integer>> bottomList;

    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(TypekitContextWrapper.wrap(newBase));
    }

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		android.util.Log.d("TAG","TOTAL MEMORY : "+(Runtime.getRuntime().totalMemory() / (1024 * 1024)) + "MB");
		android.util.Log.d("TAG","MAX MEMORY : "+(Runtime.getRuntime().maxMemory() / (1024 * 1024)) + "MB");
		android.util.Log.d("TAG","FREE MEMORY : "+(Runtime.getRuntime().freeMemory() / (1024 * 1024)) + "MB");
		android.util.Log.d("TAG","ALLOCATION MEMORY : "+((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (1024 * 1024)) + "MB");

		super.onCreate(savedInstanceState);
		try {
			setContentView(R.layout.activity_tour_main);
		}
		catch(OutOfMemoryError e)
		{
			Toast.makeText(this, "메모리가 부족합니다", Toast.LENGTH_LONG).show();
			finish();
		}
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_main);
		setSupportActionBar(toolbar);

		ActionBar actionBar = getSupportActionBar();
		Log.d(TAG, "ActionBar: " + actionBar);
//		actionBar.setElevation(0);

		DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
		ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
		drawer.setDrawerListener(toggle);
		toggle.syncState();

		NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
		navigationView.setNavigationItemSelectedListener(this);


		/* init members */
		progressBar = (ProgressBar) findViewById(R.id.progressBarMain);
		hideProgressDialog();
		regionIndex = 0;
		btnTabMap = (Button) findViewById(R.id.tab_map);
		btnTabStampAll = (Button) findViewById(R.id.tab_stamp_all);
		btnTabMap.setOnClickListener(this);
		btnTabStampAll.setOnClickListener(this);
		tabMap = (ViewGroup) findViewById(R.id.main_map_tab);
		tabStampAll = (ViewGroup) findViewById(R.id.main_stampall_tab);

		backPressedTime = 0;
		auth = FirebaseAuth.getInstance();
		nowMap = MAP_TYPE.FULL;

		fullmapView = (ImageView) findViewById(R.id.map_full);

		mapMap = new ArrayMap<>();
		mapMap.put(MAP_TYPE.MID, (ImageView) findViewById(R.id.map_1));
		mapMap.put(MAP_TYPE.EAST, (ImageView) findViewById(R.id.map_2));
		mapMap.put(MAP_TYPE.WEST, (ImageView) findViewById(R.id.map_3));
		mapMap.put(MAP_TYPE.SOUTH, (ImageView) findViewById(R.id.map_4));
		mapMap.put(MAP_TYPE.NORTH, (ImageView) findViewById(R.id.map_5));

		hiddenBtnList = new ArrayList<>();
		hiddenBtnList.add((Button) findViewById(R.id.map_mid_button));
		hiddenBtnList.add((Button) findViewById(R.id.map_east_button));
		hiddenBtnList.add((Button) findViewById(R.id.map_west_button));
		hiddenBtnList.add((Button) findViewById(R.id.map_south_button));
		hiddenBtnList.add((Button) findViewById(R.id.map_north_button));

		mapRegion = new ArrayMap<>();
		mapRegion.put(MAP_TYPE.MID, new ArrayList<ImageButton>());
		mapRegion.put(MAP_TYPE.EAST, new ArrayList<ImageButton>());
		mapRegion.put(MAP_TYPE.WEST, new ArrayList<ImageButton>());
		mapRegion.put(MAP_TYPE.SOUTH, new ArrayList<ImageButton>());
		mapRegion.put(MAP_TYPE.NORTH, new ArrayList<ImageButton>());

		List<ImageButton> tempList;

		tempList = mapRegion.get(MAP_TYPE.MID);
		tempList.add((ImageButton) findViewById(R.id.icon_jongmyo));
		tempList.add((ImageButton) findViewById(R.id.icon_indepen));
		tempList.add((ImageButton) findViewById(R.id.icon_gyungbok));
		tempList.add((ImageButton) findViewById(R.id.icon_changduck));
		tempList.add((ImageButton) findViewById(R.id.icon_changgyung));
		tempList.add((ImageButton) findViewById(R.id.icon_gyunghee));
		tempList.add((ImageButton) findViewById(R.id.icon_ducksu));
		tempList.add((ImageButton) findViewById(R.id.icon_busin));
		tempList.add((ImageButton) findViewById(R.id.icon_dongdaemun));
		tempList.add((ImageButton) findViewById(R.id.icon_namdaemun));
		tempList.add((ImageButton) findViewById(R.id.icon_bukdaemun));


		tempList = mapRegion.get(MAP_TYPE.EAST);
		tempList.add((ImageButton) findViewById(R.id.icon_amsadong));

		tempList = mapRegion.get(MAP_TYPE.WEST);
		tempList.add((ImageButton) findViewById(R.id.icon_yangchun));

		tempList = mapRegion.get(MAP_TYPE.SOUTH);
		tempList.add((ImageButton) findViewById(R.id.icon_nakjungdae));
		tempList.add((ImageButton) findViewById(R.id.icon_huninreung));


		tempList = mapRegion.get(MAP_TYPE.NORTH);
		tempList.add((ImageButton) findViewById(R.id.icon_taereung));

		FirebaseUser user = auth.getCurrentUser();
		if (user != null)
		{
			View nav_header = navigationView.getHeaderView(0);
			ImageView user_photo = nav_header.findViewById(R.id.user_photo);
			TextView user_name = nav_header.findViewById(R.id.user_name);
			TextView user_email = nav_header.findViewById(R.id.user_email);

			String strName = user.getDisplayName();
			String strEmail = user.getEmail();

//            if (user_photo != null) {
//                Glide
//                        .with(nav_header.getContext())
//                        .load(currentUser.getPhotoUrl()) // the uri you got from Firebase
//                        .override(200,200)
//                        .into(user_photo); // Your imageView variable
//            }
			if (user_name  != null && strName  != null) user_name.setText(strName);
			if (user_email != null && strEmail != null) user_email.setText(strEmail);
		}

		List<Button> stampAllList = new ArrayList<>();
        stampAllList.add((Button)findViewById(R.id.stamp1));
        stampAllList.add((Button)findViewById(R.id.stamp2));
        stampAllList.add((Button)findViewById(R.id.stamp3));
        stampAllList.add((Button)findViewById(R.id.stamp4));
        stampAllList.add((Button)findViewById(R.id.stamp5));
        stampAllList.add((Button)findViewById(R.id.stamp6));
        stampAllList.add((Button)findViewById(R.id.stamp7));
        stampAllList.add((Button)findViewById(R.id.stamp8));
        stampAllList.add((Button)findViewById(R.id.stamp9));
        stampAllList.add((Button)findViewById(R.id.stamp10));
        stampAllList.add((Button)findViewById(R.id.stamp12));
        stampAllList.add((Button)findViewById(R.id.stamp13));
        stampAllList.add((Button)findViewById(R.id.stamp14));
        stampAllList.add((Button)findViewById(R.id.stamp15));
        stampAllList.add((Button)findViewById(R.id.stamp16));
        stampAllList.add((Button)findViewById(R.id.stamp17));

        int i=1;
        for(Button stamp : stampAllList)
        {
			if (i == 11)
				i++;

			int stampLevel = userData.stampList.get(i);
			Log.d(TAG, "stampLevel:" + stampLevel);

            if (stampLevel == 0)
                stamp.setBackgroundResource(R.drawable.main_1_2_graybox);
            else
				stamp.setBackgroundResource(R.drawable.main_1_2_box);

            i++;
        }

		bottomList = new ArrayList<Pair<Integer, Integer>>();
        bottomList.add(new Pair<>(R.id.btn_gyeonbok, 4));
		bottomList.add(new Pair<>(R.id.btn_changduk, 5));
		bottomList.add(new Pair<>(R.id.btn_changgyung, 6));
		bottomList.add(new Pair<>(R.id.btn_gyunghee, 7));
		bottomList.add(new Pair<>(R.id.btn_duksu, 8));
		bottomList.add(new Pair<>(R.id.btn_dongdae, 10));
		bottomList.add(new Pair<>(R.id.btn_namdae, 12));
		bottomList.add(new Pair<>(R.id.btn_bukdae, 13));
		bottomList.add(new Pair<>(R.id.btn_taereung, 14));
		bottomList.add(new Pair<>(R.id.btn_hyuninreung, 15));

		for(Pair<Integer, Integer> pair : bottomList)
		{
			Button btn = (Button)findViewById(pair.first);
			btn.setOnClickListener(this);
		}

		changeTabTo(0);
	} // onCreate()

	@Override // button event: open drawer
	public void onBackPressed()
	{
		DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

		if (drawer.isDrawerOpen(GravityCompat.START))
			drawer.closeDrawer(GravityCompat.START);
		else
		{
			if(nowMap == MAP_TYPE.FULL) // 전체지도 상태일 경우
			{
				long tempTime = System.currentTimeMillis();
				long intervalTime = tempTime - backPressedTime;

				if (!(0 <= intervalTime && FINISH_INTERVAL_TIME >= intervalTime)) // 최초 Back Pressed일 경우
				{
					backPressedTime = tempTime;
					Toast.makeText(TourMainActivity.this, "종료하시려면 한번 더 누르세요", Toast.LENGTH_SHORT).show();
				}
				else // 연속 Back Pressed일 경우
				{
					super.onBackPressed();
				}
			}
			else // 구역지도 상태일 경우
				goOutMap();
		}
	}

	@Override
	public void onClick(View v)
	{
		if (isProgress)
			return;

		int id = v.getId();

		switch (id)
		{
			case R.id.tab_map: changeTabTo(0); break;
			case R.id.tab_stamp_all: changeTabTo(1); break;
			case R.id.icon_jongmyo: startRegionActivity(1); break;
			case R.id.icon_nakjungdae: startRegionActivity(2); break;
			case R.id.icon_indepen: startRegionActivity(3); break;
			case R.id.icon_gyungbok: startRegionActivity(4); break;
			case R.id.icon_changduck: startRegionActivity(5); break;
			case R.id.icon_changgyung: startRegionActivity(6); break;
			case R.id.icon_gyunghee: startRegionActivity(7); break;
			case R.id.icon_ducksu: startRegionActivity(8); break;
			case R.id.icon_busin: startRegionActivity(9); break;
			case R.id.icon_dongdaemun: startRegionActivity(10); break;
			case R.id.icon_namdaemun: startRegionActivity(12); break;
			case R.id.icon_bukdaemun: startRegionActivity(13); break;
			case R.id.icon_taereung: startRegionActivity(14); break;
			case R.id.icon_huninreung: startRegionActivity(15); break;
			case R.id.icon_yangchun: startRegionActivity(16); break;
			case R.id.icon_amsadong: startRegionActivity(17); break;

			case R.id.map_mid_button: goInMap(MAP_TYPE.MID); break;
			case R.id.map_east_button: goInMap(MAP_TYPE.EAST); break;
			case R.id.map_west_button: goInMap(MAP_TYPE.WEST); break;
			case R.id.map_south_button: goInMap(MAP_TYPE.SOUTH); break;
			case R.id.map_north_button: goInMap(MAP_TYPE.NORTH); break;
			default:
				for(Pair<Integer, Integer> pair : bottomList)
				{
					if (id == pair.first)
					{
						startRegionActivity(pair.second);
						break;
					}
				}
		}
	} // onClick()

	void changeTabTo(int targetIdx)
	{
		switch(targetIdx)
		{
			case 0:
				btnTabMap.setBackgroundColor(getResources().getColor(R.color.colorPressed));
				btnTabStampAll.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
				tabMap.setVisibility(View.VISIBLE);
				tabStampAll.setVisibility(View.GONE);
				break;
			case 1:
				btnTabMap.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
				btnTabStampAll.setBackgroundColor(getResources().getColor(R.color.colorPressed));
				tabMap.setVisibility(View.GONE);
				tabStampAll.setVisibility(View.VISIBLE);
				break;
		}
	}

	void goInMap(MAP_TYPE map)
	{
		fullmapView.setVisibility(View.GONE);
		for (Button btn : hiddenBtnList)
			btn.setVisibility(View.GONE);
		_updateMap(map, View.VISIBLE);
		nowMap = map;
	}

	void goOutMap()
	{
		fullmapView.setVisibility(View.VISIBLE);
		for (Button btn : hiddenBtnList)
			btn.setVisibility(View.VISIBLE);
		_updateMap(nowMap, View.GONE);
		nowMap = MAP_TYPE.FULL;
	}

	void _updateMap(MAP_TYPE map, int visibility)
	{
		mapMap.get(map).setVisibility(visibility);
		List<ImageButton> tempList = mapRegion.get(map);
		for(ImageButton btnRegion : tempList)
			btnRegion.setVisibility(visibility);
	}

	private void signOut()
	{
		// Firebase sign out
		auth.signOut();

		// Google sign out
		Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(new ResultCallback<Status>()
		{
			@Override
			public void onResult(@NonNull Status status)
			{
				Intent intent = new Intent(TourMainActivity.this, SocialLoginActivity.class);
				startActivity(intent);
				finish();
			}
		});
	} // signOut()

	void startRegionActivity(int idx)
	{
		regionIndex = idx;

		DatabaseReference ref = FirebaseDatabase.getInstance().getReference(CULTURAL_REF).child(""+regionIndex);
		Log.v(TAG, ref.toString());

		ValueEventListener listener = new ValueEventListener() {
			@Override
			public void onDataChange(DataSnapshot dataSnapshot) {
				hideProgressDialog();
				CulturalData cultural = dataSnapshot.getValue(CulturalData.class);
				Log.d(TAG, "cultural: " + cultural);

				SharedData.cultural = cultural;
				hideProgressDialog();

				Intent intent = new Intent(TourMainActivity.this, TourRegionActivity.class);
				startActivity(intent);
			}

			@Override
			public void onCancelled(DatabaseError e) {
				hideProgressDialog();
				Log.w(TAG, "Failed to read value.", e.toException());
				Toast.makeText(TourMainActivity.this, "데이터 가져오기 실패", Toast.LENGTH_SHORT).show();
			}
		};

		showProgressDialog();
		addListenerWithTimeout(ref, listener, DATA_NAME.CULTURAL);
	} // startRegionActivity()

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.menu_search, menu);

		//return initSearch(menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		//noinspection SimplifiableIfStatement
		if (id == R.id.action_search)
		{
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onNavigationItemSelected(MenuItem item)
	{
		// Handle navigation view item clicks here.
		int id = item.getItemId();

		switch(id)
		{
			case R.id.nav_sign_out: signOut(); break;
			case R.id.nav_app_info: popupActivity(POPUP_TYPE.APP_INFO); break;
			case R.id.nav_contact: popupActivity(POPUP_TYPE.CONTACT); break;
		}

		DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
		drawer.closeDrawer(GravityCompat.START);

		return true;
	} // onNavigationItemSelected()

	void popupActivity(POPUP_TYPE type)
	{
		Intent intent = new Intent(TourMainActivity.this, PopupActivity.class);
		intent.putExtra("POPUP_TYPE", type);
		startActivity(intent);
	}
} // class
