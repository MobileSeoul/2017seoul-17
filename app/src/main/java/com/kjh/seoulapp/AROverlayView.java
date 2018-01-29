package com.kjh.seoulapp;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.location.Location;
import android.opengl.Matrix;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.kjh.seoulapp.helper.LocationHelper;
import com.kjh.seoulapp.model.ARPoint;

import static com.kjh.seoulapp.data.SharedData.USER_REF;
import static com.kjh.seoulapp.data.SharedData.cultural;
import static com.kjh.seoulapp.data.SharedData.locRegion;
import static com.kjh.seoulapp.data.SharedData.regionIndex;
import static com.kjh.seoulapp.data.SharedData.stampLevel;
import static com.kjh.seoulapp.data.SharedData.userData;

/**
 * Created by ntdat on 1/13/17.
 */

public class AROverlayView extends View implements View.OnClickListener
{
    static final String TAG = "AROverlayView";
    Activity activity;
    private float[] rotatedProjectionMatrix = new float[16];
    private Location currentLocation;
    private List<ARPoint> arPoints;
    int nowStampLevel;
    boolean isEnd;
    Button hiddenBtn;

    public AROverlayView(Activity activity) {
        super(activity);

        this.activity = activity;

        //Demo points
        arPoints = new ArrayList<ARPoint>() {{
            add(new ARPoint(cultural.title, locRegion.getLatitude(), locRegion.getLongitude(), 0));
        }};
        nowStampLevel = 0;
        isEnd = false;

        hiddenBtn = activity.findViewById(R.id.btn_stamp);
        hiddenBtn.setVisibility(View.GONE);
        hiddenBtn.setOnClickListener(this);

        Toast.makeText(activity, "카메라로 도장을 찾아 클릭하세요!", Toast.LENGTH_LONG).show();
    }

    public void updateRotatedProjectionMatrix(float[] rotatedProjectionMatrix) {
        this.rotatedProjectionMatrix = rotatedProjectionMatrix;
        this.invalidate();
    }

    public void updateCurrentLocation(Location currentLocation){
        this.currentLocation = currentLocation;
        this.invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (currentLocation == null) {
            return;
        }

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.BLUE);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        paint.setTextSize(60);

        for (int i = 0; i < arPoints.size(); i ++) {
            float[] currentLocationInECEF = LocationHelper.WSG84toECEF(currentLocation);
            float[] pointInECEF = LocationHelper.WSG84toECEF(arPoints.get(i).getLocation());
            float[] pointInENU = LocationHelper.ECEFtoENU(currentLocation, currentLocationInECEF, pointInECEF);

            float[] cameraCoordinateVector = new float[4];
            Matrix.multiplyMV(cameraCoordinateVector, 0, rotatedProjectionMatrix, 0, pointInENU, 0);

            // cameraCoordinateVector[2] is z, that always less than 0 to display on right position
            // if z > 0, the point will display on the opposite
            if (cameraCoordinateVector[2] < 0)
            {
                float x  = (0.5f + cameraCoordinateVector[0]/cameraCoordinateVector[3]) * canvas.getWidth();
                float y = (0.5f - cameraCoordinateVector[1]/cameraCoordinateVector[3]) * canvas.getHeight();

                BitmapFactory.Options opt = new BitmapFactory.Options();
                opt.inSampleSize = 3; // resize to (1/inSampleSize)
                Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.stamp_color, opt);
                canvas.drawBitmap(bitmap, x - opt.outWidth/2, y - opt.outHeight/2, paint);
                hiddenBtn.setVisibility(View.VISIBLE);
                hiddenBtn.setWidth(opt.outWidth);
                hiddenBtn.setHeight(opt.outHeight);
                hiddenBtn.setX(x - opt.outWidth/2);
                hiddenBtn.setY(y - opt.outHeight/2);
                //Log.d(TAG, (x - opt.outWidth/2) + ", " + (y - opt.outHeight/2));
                //Log.d(TAG, cameraCoordinateVector[0] + ", " + cameraCoordinateVector[1]);

//                canvas.drawText(arPoints.get(i).getName(), x - (30 * arPoints.get(i).getName().length() / 2), y - 80, paint);
            }
        }
    }

    @Override
    public void onClick(View v)
    {
        if (isEnd)
            return;

        int id = v.getId();

        switch(id)
        {
            case R.id.btn_stamp:
                nowStampLevel++;
                Log.d(TAG, "stamp: " + nowStampLevel + "," + stampLevel);
                if (nowStampLevel < stampLevel)
                    Toast.makeText(activity, "획득 도장갯수: " + nowStampLevel, Toast.LENGTH_SHORT).show();
                else {
                    isEnd = true;
                    Toast.makeText(activity, "총 획득 도장갯수: " + nowStampLevel, Toast.LENGTH_SHORT).show();
                    sendStampInfo();
                    Intent intent = new Intent(activity, TourRegionActivity.class);
                    activity.startActivity(intent);
                    activity.finish();
                }
                break;
        }
    }

    void sendStampInfo()
    {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseDatabase database = FirebaseDatabase.getInstance();

        String uid = auth.getCurrentUser().getUid();
        Log.d(TAG, "before: "+userData);
        userData.stampList.set(regionIndex, stampLevel);
        Log.d(TAG, "after: "+userData);

        database.getReference(USER_REF).child(uid).setValue(userData);
    }
}
