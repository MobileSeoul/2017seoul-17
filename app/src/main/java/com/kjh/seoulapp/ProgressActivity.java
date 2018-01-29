package com.kjh.seoulapp;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.kjh.seoulapp.data.SharedData;

import java.util.Timer;
import java.util.TimerTask;

import static com.kjh.seoulapp.data.SharedData.cultural;
import static com.kjh.seoulapp.data.SharedData.userData;

public abstract class ProgressActivity extends AppCompatActivity
{
	static long TIMEOUT = 3000L;
	protected ProgressBar progressBar;
	protected boolean isProgress;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
	}

	public void showProgressDialog() { progressBar.setVisibility(View.VISIBLE); isProgress = true; }
	public void hideProgressDialog() { progressBar.setVisibility(View.GONE); isProgress = false; }


	public void addListenerWithTimeout(final DatabaseReference ref, final ValueEventListener listener, final SharedData.DATA_NAME dataName)
	{
		ref.addListenerForSingleValueEvent(listener);

		final Timer timer = new Timer();
		TimerTask timerTask = new TimerTask() {
			@Override
			public void run() {
				timer.cancel();
				boolean timeoutFlag = false;

				if (dataName == SharedData.DATA_NAME.USER_DATA)
					timeoutFlag = userData == null;
				else if (dataName == SharedData.DATA_NAME.CULTURAL)
					timeoutFlag = cultural == null;

				if (timeoutFlag) //  Timeout
				{
					ref.removeEventListener(listener);
					Log.d("Timeout", ref.toString());

					new Handler(Looper.getMainLooper()).post(new Runnable()
					{
						@Override
						public void run()
						{
							Toast.makeText(ProgressActivity.this, "데이터 읽기 실패", Toast.LENGTH_SHORT).show();
							hideProgressDialog();
						}
					});
				}
			}
		};
		// Setting timeout of 10 sec to the request
		timer.schedule(timerTask, TIMEOUT);
	}
}
