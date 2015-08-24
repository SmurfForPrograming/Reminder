package bajomoj.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;


public class More extends ActionBarActivity {

    private Boolean active = true;
    private String location = "swag";
    private String description = "gay";
    public enum DepArr {Departure, Arrival}
    DepArr depArr;
    Integer choose = 0;
    String dateTime = "01/23/1996 12:12:11";
    private int  repeatInterval = 40; //samo za probu
    private double radius = 100; //samo za probu
    double latitude;
    double longitude;
    static final int EXPECTED_CODE_MAIN_MAP = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_more);

        latitude = getIntent().getDoubleExtra("lat", latitude);
        longitude = getIntent().getDoubleExtra("lng", longitude);
        //listData oneRow = b.getParcelable("parcable");

        Button delete = (Button) findViewById(R.id.getBackButton);
        delete.setOnClickListener(mainHandler);

        Button seeLocation = (Button) findViewById(R.id.seeLocation);
        seeLocation.setOnClickListener(mainHandler);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode==1) {
            if (resultCode == RESULT_OK) {
                //Toast.makeText(getApplicationContext(), "onactivityresult", Toast.LENGTH_SHORT).show();
            }
        }
    }


    View.OnClickListener mainHandler = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.getBackButton: {

                    listData object = new listData(active, location, description, choose, dateTime, repeatInterval, radius, latitude, longitude);
                    Intent mainActivityIntent = new Intent(More.this, MainActivity.class);
                    mainActivityIntent.putExtra("parcel", (android.os.Parcelable) object);
                    setResult(RESULT_OK, mainActivityIntent);
                    finish();
                    break;
                }

                case R.id.seeLocation: {
                    Intent mainMapIntent = new Intent(More.this, MainMap.class);
                    mainMapIntent.putExtra("latitude", latitude);
                    mainMapIntent.putExtra("longitude", longitude);
                    startActivityForResult(mainMapIntent, EXPECTED_CODE_MAIN_MAP);
                }
            }
        }
    };
}
