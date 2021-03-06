package bajomoj.myapplication;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;


public class AddActivity extends Activity
{
    private Boolean active = false;
    private String location;
    private String description;
    public enum DepArr {Departure, Arrival}
    DepArr depArr;
    Integer choose;
    String dateTime;
    private int  repeatInterval = 40; //samo za probu
    private double radius; //samo za probu
    ArrayList<listData> myListdata = new ArrayList<listData>();
    Spinner choseDepArr;
    private double latitude;
    private double longitude;
    static final int EXPECTED_CODE_MAPA = 1;
    Button btn;
    int year, month, day;
    static final int DIALOG_ID = 0;
    Button button_stpd;
    static final int DIALOG_ID2 = 2;
    int hour, minute;
    static String date = "";
    static String time = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent addLayoutIntent = getIntent();
        setContentView(R.layout.add_item_view);

        Bundle b = getIntent().getExtras();
        /*myParcelable object = b.getParcelable("parcel"); ZA DOBITI CIJELU LISTU
        myListdata = object.getMyListdata();*/

        CheckBox addCheckBox = (CheckBox) findViewById(R.id.activeCheckBox);
        addCheckBox.setOnCheckedChangeListener(addCheckBoxHandler);

        EditText addDescription = (EditText) findViewById(R.id.addDescription);
        addDescription.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                description = s.toString();

            }
        });


        EditText addDate = (EditText) findViewById(R.id.addDate);
        addDate.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                      //dateTime = datum + vrijeme;
                    }
        });



        choseDepArr = (Spinner) findViewById(R.id.choseDepArr);
        choseDepArr.setAdapter(new ArrayAdapter<DepArr>(this,
                R.layout.support_simple_spinner_dropdown_item, depArr.values()));
        choseDepArr.setOnItemSelectedListener(choseDepArrHanlder);

        Button saveButton = (Button) findViewById(R.id.saveButton);
        saveButton.setOnClickListener(allButtonsHandler);

        Button discardButton = (Button) findViewById(R.id.discardButton);
        discardButton.setOnClickListener(allButtonsHandler);

        Button locationButton = (Button) findViewById(R.id.locationButton);
        locationButton.setOnClickListener(allButtonsHandler);


        final Calendar cal = Calendar.getInstance();
        year = cal.get(Calendar.YEAR);
        month = cal.get(Calendar.MONTH);
        day = cal.get(Calendar.DAY_OF_MONTH);

        showDialogOnButtonClick();
        showTimePickerDialog();
    }


    //////////////////////////////////////
    public void showDialogOnButtonClick() {
        btn = (Button)findViewById(R.id.showDate);
        btn.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showDialog(DIALOG_ID);
                    }
                }
        );
    }

    private DatePickerDialog.OnDateSetListener dpickerListener
            = new
            DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                    year = year;
                    month = monthOfYear + 1;
                    day = dayOfMonth;
                    date = String.format("%02d" + "/" + "%02d" + "/" + "%04d", month, day, year);
                    Toast.makeText(getApplicationContext(), date, Toast.LENGTH_SHORT).show();
                }
            };

    public void showTimePickerDialog() {
        button_stpd = (Button)findViewById(R.id.showTime);
        button_stpd.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showDialog(2);
                    }
                }
        );
    }

    protected TimePickerDialog.OnTimeSetListener kTimePickerListener =
            new TimePickerDialog.OnTimeSetListener() {
                @Override
                public void onTimeSet(TimePicker view, int hourOfDay, int minuteOfDay) {
                    if (hourOfDay < 10) {

                    }
                    hour = hourOfDay;
                    minute= minuteOfDay;
                    time = String.format("%02d:%02d", hour, minute);
                    EditText editText = (EditText)findViewById(R.id.addDate);
                    editText.setText("Date: " + date + " Time: " + time, TextView.BufferType.EDITABLE);
                    dateTime = date + " " + time;
                    Toast.makeText(getApplicationContext(), time, Toast.LENGTH_SHORT).show();
                }
            };

    @Override
    protected Dialog onCreateDialog(int id) {
        if (id == DIALOG_ID) {
            return new DatePickerDialog(this, dpickerListener, year, month, day);
        } else
        if  (id == 2) {
            return new TimePickerDialog(this, kTimePickerListener, hour, minute, true);
        }
        else
            return null;
    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode==1) {
            if (resultCode == RESULT_OK) {
                latitude = data.getDoubleExtra("lat", latitude);
                longitude = data.getDoubleExtra("lng", longitude);
                radius = data.getDoubleExtra("radius", radius);
                location = data.getStringExtra("location");
            }
        }
    }


    CompoundButton.OnCheckedChangeListener addCheckBoxHandler = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked) {
                active = true;
            } else {
                active = false;
            }
        }
    };


    AdapterView.OnItemSelectedListener choseDepArrHanlder = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {


            if (parent.getItemAtPosition(position).toString() == "Departure") {
                choose = 0;
            } else if (parent.getItemAtPosition(position).toString() == "Arrival") {
               choose = 1;
            }
            else {
                Toast.makeText(getApplicationContext(), parent.getItemAtPosition(position).toString() +"treci", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };

    View.OnClickListener allButtonsHandler = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.saveButton: {
                    //SLANJE CIJELE LISTE
                    /*myListdata.add(new listData(active, location, description, choose, repeatInterval, radius));

                    myParcelable object = new myParcelable();
                    object.setMyListdata(myListdata);
                    Intent mainActivityIntent = new Intent(AddActivity.this, MainActivity.class);
                    mainActivityIntent.putExtra("parcel", object);
                    setResult(RESULT_OK,mainActivityIntent);
                    finish();*/

                    listData object = new listData(active, location, description, choose,dateTime, repeatInterval, radius, latitude, longitude);
                    Intent mainActivityIntent = new Intent(AddActivity.this, MainActivity.class);
                    mainActivityIntent.putExtra("parcel", (android.os.Parcelable) object);
                    setResult(RESULT_OK, mainActivityIntent);
                    finish();
                    break;
                }
                case R.id.discardButton: {
                    Intent mainActivityIntent = new Intent(AddActivity.this, MainActivity.class);
                    setResult(RESULT_CANCELED, mainActivityIntent);
                    finish();
                    break;
                }
                case R.id.locationButton: {
                    Intent addLayoutIntent = new Intent(AddActivity.this, InitLocation.class);
                    startActivityForResult(addLayoutIntent, EXPECTED_CODE_MAPA);
                    break;
                }
            }
        }
    };
}