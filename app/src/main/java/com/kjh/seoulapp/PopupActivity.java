package com.kjh.seoulapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import static com.kjh.seoulapp.data.SharedData.EXTRA_CORRECT_CNT;
import static com.kjh.seoulapp.data.SharedData.EXTRA_POPUP_TYPE;
import static com.kjh.seoulapp.data.SharedData.POPUP_TYPE;

public class PopupActivity extends AppCompatActivity implements View.OnClickListener
{
	final String TAG = "PopupActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_popup);

		Intent intent = getIntent();
		POPUP_TYPE type = (POPUP_TYPE) intent.getSerializableExtra(EXTRA_POPUP_TYPE);
		TextView titleView = (TextView) findViewById(R.id.popup_title);
		TextView msgView = (TextView) findViewById(R.id.popup_msg);

		switch (type)
		{
			case APP_INFO:
				titleView.setText("APP_INFO");
				msgView.setText(
					"이름: 역사의제왕, 서울원정대! \n" +
					"개발진: 김명길, 전종호, 한제우(디자이너)");
				break;
			case CONTACT:
				titleView.setText("CONTACT");
				msgView.setText(
					"연락처: kjh@gmail.com");
				break;
			case DONATE:
				titleView.setText("DONATE");
				msgView.setText(
					"계좌번호: 01234-56789-01 (OO은행)"
				);
				break;
			case END_QUIZ: // from QuizProblemActivity
				titleView.setText("퀴즈 결과");
				int correctCnt = intent.getIntExtra(EXTRA_CORRECT_CNT, -1);
				msgView.setText(
					"맞춘 갯수: " + correctCnt + "\n" +
					"축하드립니다! 다음 화면에서 스탬프를 획득하세요!"
				);
				break;
			default:
				Log.d(TAG, "unknown type");
				break;
		}
	}

	@Override
	protected void onStart()
	{
		super.onStart();
	}

	@Override
	public void onClick(View v)
	{
		int id = v.getId();

		switch (id)
		{
			case R.id.popup_close:
				Intent intent = new Intent();
				//intent.putExtra("result", "Close Popup");
				setResult(
					RESULT_OK,
					intent);
				finish();
				break;
		}
	}
}
