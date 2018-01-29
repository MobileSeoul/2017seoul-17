package com.kjh.seoulapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kjh.seoulapp.data.UserData;

import static com.kjh.seoulapp.data.SharedData.DATA_NAME;
import static com.kjh.seoulapp.data.SharedData.USER_REF;
import static com.kjh.seoulapp.data.SharedData.userData;

public class SocialLoginActivity extends GoogleApiClientActivity implements View.OnClickListener
{
	final String TAG = "SocialLoginActivity";
	final int RC_SIGN_IN = 9001;
	ImageButton btnLogin;
	FirebaseAuth auth;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		Log.d(TAG, "onCreate()");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_social_login);

		progressBar = (ProgressBar) findViewById(R.id.progressBarLogin);
		hideProgressDialog();

		btnLogin = (ImageButton) findViewById(R.id.btn_login);
		btnLogin.setOnClickListener(this);

		auth = FirebaseAuth.getInstance();
		Log.d(TAG, "progressBar: " + progressBar);

		android.util.Log.d(TAG,"TOTAL MEMORY : "+(Runtime.getRuntime().totalMemory() / (1024 * 1024)) + "MB");
		android.util.Log.d(TAG,"MAX MEMORY : "+(Runtime.getRuntime().maxMemory() / (1024 * 1024)) + "MB");
		android.util.Log.d(TAG,"FREE MEMORY : "+(Runtime.getRuntime().freeMemory() / (1024 * 1024)) + "MB");
		android.util.Log.d(TAG,"ALLOCATION MEMORY : "+((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (1024 * 1024)) + "MB");
	}

	// [START on_start_check_user]
	@Override
	protected void onStart()
	{
		Log.d(TAG, "onStart()");
		super.onStart();

		// Check if currentUser is signed in (non-null) and update UI accordingly.
		FirebaseUser currentUser = auth.getCurrentUser();
		updateUI(currentUser);
	}
	// [END on_start_check_user]

	@Override
	public void onClick(View v)
	{
		if (isProgress)
			return;

		switch (v.getId())
		{
			case R.id.btn_login:
				signIn();
				break;
		}
	}

	// [START signin]
	private void signIn()
	{
		Log.d(TAG, "signIn");
		Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
		startActivityForResult(signInIntent, RC_SIGN_IN);
	}
	// [END signin]

	// [START onActivityResult]
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);
		// Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
		if (requestCode == RC_SIGN_IN)
		{
			GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
			if (result.isSuccess())
			{
				// Google Sign In was successful, authenticate with Firebase
				GoogleSignInAccount account = result.getSignInAccount();
				firebaseAuthWithGoogle(account);
			} else
			{
				// Google Sign In failed, update UI appropriately
				// [START_EXCLUDE]
				Log.d(TAG, "status: " + result.getStatus());
				updateUI(null);
				// [END_EXCLUDE]
			}
		}
	}
	// [END onActivityResult]

	// [START auth_with_google]
	private void firebaseAuthWithGoogle(GoogleSignInAccount acct)
	{
		Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());
		// [START_EXCLUDE silent]
		showProgressDialog();
		// [END_EXCLUDE]

		AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
		auth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>()
		{
			@Override
			public void onComplete(@NonNull Task<AuthResult> task)
			{
				if (task.isSuccessful())
				{
					// Sign in success, update UI with the signed-in currentUser's information
					Log.d(TAG, "signInWithCredential:success");
					Toast.makeText(SocialLoginActivity.this, "인증 성공.", Toast.LENGTH_SHORT).show();

					FirebaseUser currentUser = auth.getCurrentUser();
					updateUI(currentUser);
				} else
				{
					// If sign in fails, display a message to the currentUser.
					Log.w(TAG, "signInWithCredential:failure", task.getException());
					Toast.makeText(SocialLoginActivity.this, "인증 실패.", Toast.LENGTH_SHORT).show();
					updateUI(null);
				}

				// [START_EXCLUDE]
				hideProgressDialog();
				// [END_EXCLUDE]
			}
		});
	}
	// [END auth_with_google]

	public void startMainActivity()
	{
		Log.d(TAG, "startMainActivity");
		FirebaseDatabase database = FirebaseDatabase.getInstance();

		FirebaseUser currentUser = auth.getCurrentUser();
		String uid = currentUser.getUid();

		Log.d(TAG, currentUser.getEmail() + ", " + uid);

		DatabaseReference ref = database.getReference(USER_REF).child(uid);
		Log.d(TAG, ref.toString());

		ValueEventListener listener = new ValueEventListener()
		{
			@Override
			public void onDataChange(DataSnapshot dataSnapshot)
			{
				hideProgressDialog();
				UserData _userData = dataSnapshot.getValue(UserData.class);

				if (_userData == null) // 네트워크는 성공했으나 read fail
				{
					// 새로운 유저 데이터 Insert
					FirebaseUser currentUser = auth.getCurrentUser();
					_userData = new UserData(currentUser.getDisplayName());

					String uid = currentUser.getUid();

					FirebaseDatabase database = FirebaseDatabase.getInstance();
					database.getReference(USER_REF).child(uid).setValue(_userData);
				}
				userData = _userData;
				Log.d(TAG, "userData: " + _userData);

				// change activity
				Intent intent = new Intent(SocialLoginActivity.this, TourMainActivity.class);
				startActivity(intent);
				finish();

				// TODO 스탬프 리스트 UI 업데이트
			}

			@Override
			public void onCancelled(DatabaseError e)
			{
				hideProgressDialog();
				Log.w(TAG, "Failed to read value.", e.toException());
				Toast.makeText(SocialLoginActivity.this, "데이터 가져오기 실패", Toast.LENGTH_SHORT).show();
			}
		};

		showProgressDialog();
		addListenerWithTimeout(ref, listener, DATA_NAME.USER_DATA);
	} // startMainActivity()

	private void updateUI(FirebaseUser user)
	{
		if (user != null)
			startMainActivity();
		else
		{
			Log.d(TAG, "non auth state");
			// TODO: 네트워크 등의 이유로 인증 실패
		}
	}

	void revokeAccess()
	{
		// Firebase sign out
		auth.signOut();

		// Google revoke access
		Auth.GoogleSignInApi.revokeAccess(mGoogleApiClient).setResultCallback(new ResultCallback<Status>()
		{
			@Override
			public void onResult(@NonNull Status status)
			{
				//enableBtnNext(null);
			}
		});
	}
}
