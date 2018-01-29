package com.kjh.seoulapp.data;

import android.location.Location;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;

import com.kjh.seoulapp.R;

public class SharedData
{
	public enum POPUP_TYPE { APP_INFO, CONTACT, DONATE, END_QUIZ }
	public enum DATA_NAME { USER_DATA, CULTURAL };

	public static final String USER_REF = "user";
	public static final String CULTURAL_REF = "cultural";

	public static final String EXTRA_POPUP_TYPE = "POPUP_TYPE";
	public static final String EXTRA_CORRECT_CNT = "correctCnt";

	public static UserData userData;
	public static CulturalData cultural;
	public static int correctCnt;
	public static int regionIndex;
	public static int stampLevel; // 획득한 도장갯수 (문제풀기 결과)
	public static Location locRegion, locNow;

	public static boolean initSearch(Menu menu)
	{
		MenuItem searchItem = menu.findItem(R.id.action_search);
		SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
		searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener()
		{
			@Override
			public boolean onQueryTextSubmit(String s)
			{
				return false;
			}

			@Override
			public boolean onQueryTextChange(String s)
			{
				System.out.println(s);
				return false;
			}
		});
		return true;
	}
}
