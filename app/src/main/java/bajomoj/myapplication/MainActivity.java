package bajomoj.myapplication;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.Circle;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends Activity  {
    ArrayList<listData> myListdata = new ArrayList<listData>();
    String FILENAME = "GPS_reminder_sort_vol1";
    static final int EXPECTED_CODE_ADD = 1;
    static final int EXPECTED_CODE_MORE = 2;
    static int rowIndex = -1;
    static final double radius = 60; //za promjenit kasnije
    double latitude = 0;
    double longitude = 0;
    Circle mapCircle;
    static LocationManager manager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        registerClickCallBack();

        Button addLayout = (Button) findViewById(R.id.addButton);
        addLayout.setOnClickListener(addLayoutHandler);


        FileInputStream fileInputStream;
        ObjectInputStream objectInputStream = null;
        try {
            Integer size;
            fileInputStream = getApplicationContext().openFileInput(FILENAME);
            objectInputStream = new ObjectInputStream(fileInputStream);
            size = (int) objectInputStream.readInt();
            for (int counter = 0; counter < size; counter++) {
                myListdata.add((listData) objectInputStream.readObject());
            }
            objectInputStream.close();

        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        } catch (NotSerializableException e1) {
            e1.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (objectInputStream != null) {
                try {
                    objectInputStream.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }

        final Handler handler = new Handler();
        Timer timer = new Timer();
        TimerTask doAsynchronousTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @SuppressWarnings("unchecked")
                    public void run() {
                        try {
                            check();
                        } catch (Exception e) {
                        }
                    }
                });
            }
        };
        timer.schedule(doAsynchronousTask, 0, 5000);

        LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Location location = manager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode==1) {
            if (resultCode == RESULT_OK) {
                Bundle b = data.getExtras();
                /*myParcelable object = b.getParcelable("parcel");  KAKO DOBITI CIJELU LISTU
                myListdata = object.getMyListdata();*/
                listData object = b.getParcelable("parcel");
                myListdata.add(object);
                String msg = String.valueOf(object.getLatitude());
                Toast.makeText(getApplicationContext(),msg , Toast.LENGTH_SHORT).show();
            }
        }
        if (requestCode==2) {
            if (resultCode==RESULT_OK) {
                Bundle b = data.getExtras();
                deleteItem();
                //listData object = b.getParcelable("parcel");
                //myListdata.add(object);
                Toast.makeText(getApplicationContext(), "from more", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        populateListView();
    }

    /*@Override
    protected void onStop() {
    }*/

    @Override
    protected void onPause() {
        super.onPause();

        FileOutputStream outStream;
        ObjectOutputStream objectOutputStream = null;
        try {
                outStream = getApplicationContext().openFileOutput(FILENAME, Context.MODE_PRIVATE);
                objectOutputStream = new ObjectOutputStream(outStream);
                objectOutputStream.writeInt(myListdata.size());
                objectOutputStream.flush();
                for (listData oneData : myListdata) {
                    objectOutputStream.writeObject(oneData);
                    objectOutputStream.flush();
                }
                objectOutputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            if (objectOutputStream!=null)
                try {
                    objectOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
    }

        private void populateListView() {
            Collections.sort(myListdata, new listComparator());
            ArrayAdapter<listData> adapter = new MyListAdapter();

            ListView list = (ListView) findViewById(R.id.reminderListView);
            list.setAdapter(adapter);
        }

        private void registerClickCallBack() {
            ListView list = (ListView) findViewById(R.id.reminderListView);
        }


    private  class MyListAdapter extends ArrayAdapter {
            public MyListAdapter() {
                super(MainActivity.this, R.layout.item_view, myListdata);
            }

            @Override
            public View getView(final int position, View convertView, ViewGroup parent) {
                View itemView = convertView;
                //make sure we have a view to work with
                if (itemView == null) {
                    itemView = getLayoutInflater().inflate(R.layout.item_view, parent, false);
                }
                //find the item to work with
                 final listData currentData = myListdata.get(position);
                rowIndex = position;

                //fill the view

                final CheckBox activeCheckBox = (CheckBox) itemView.findViewById(R.id.checkBox);
                activeCheckBox.setChecked(currentData.getActive());
                //activeCheckBox.setOnClickListener((OnClickListener) this); CHEKBOX IO KOJI TREBA DOVRSIIT

                TextView locationText = (TextView) itemView.findViewById(R.id.item_Location);
                locationText.setText(currentData.getLocation());

                TextView descriptionText = (TextView) itemView.findViewById(R.id.item_Description);
                descriptionText.setText(currentData.getDescription());

                final Button moreButton = (Button) itemView.findViewById(R.id.moreButton);

                activeCheckBox.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        currentData.setActive(!currentData.getActive());
                        populateListView();
                    }
                });

                moreButton.setOnClickListener( new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        rowIndex = position;
                        Intent moreIntent = new Intent(MainActivity.this, More.class);
                        moreIntent.putExtra("lat", currentData.getLatitude());
                        moreIntent.putExtra("lng", currentData.getLongitude());
                        startActivityForResult(moreIntent, EXPECTED_CODE_MORE);
                    }
                });
                return itemView;
            }
        }


        OnClickListener addLayoutHandler = new OnClickListener() {
            public void onClick(View v) {
                //ZA POSLATI CIJELU LISTU
               /* myParcelable object = new myParcelable();
                object.setMyListdata(myListdata);

                Intent addLayoutIntent = new Intent(MainActivity.this, AddActivity.class);
                addLayoutIntent.putExtra("parcel", object);

                startActivityForResult(addLayoutIntent, EXPECTED_CODE);*/

                Intent addLayoutIntent = new Intent(MainActivity.this, AddActivity.class);
                startActivityForResult(addLayoutIntent, EXPECTED_CODE_ADD);
            }
        };


        void check() {
            for(int counter=0; counter < myListdata.size(); counter++) {
                if (myListdata.get(counter).getActive()) {
                    if (myListdata.get(counter).getDateTime().compareTo(new Date()) < 0) {
                        //compareTo returns positive int if first item is bigger

                        LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE );
                        Criteria criteria = new Criteria();
                        manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 8000, 8000, new LocationListener() {
                            @Override
                            public void onLocationChanged(Location location) {
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                                String msg1 = String.valueOf(latitude);
                                String msg2 = String.valueOf(longitude);
                                Toast.makeText(getApplicationContext(), "Trenutna lokacija: " + msg1 + " AND " + msg2, Toast.LENGTH_SHORT).show();
                            }
                            @Override
                            public void onProviderDisabled(String provider) {
                                // TODO Auto-generated method stub
                            }
                            @Override
                            public void onProviderEnabled(String provider) {
                                // TODO Auto-generated method stub
                            }
                            @Override
                            public void onStatusChanged(String provider, int status,
                                                        Bundle extras) {
                                // TODO Auto-generated method stub
                            }
                        });

                        float[] distance = new float[2];

                        Location.distanceBetween(latitude, longitude,
                                myListdata.get(counter).getLatitude(),myListdata.get(counter).getLongitude(), distance);

                    if (latitude != 0 && longitude != 0) {
                        if (distance[0] > radius) {
                            Toast.makeText(getBaseContext(), "Izvan kruga si ", Toast.LENGTH_LONG).show();
                            if (myListdata.get(counter).getDepArr() == 0) {
                                MediaPlayer mp;
                                mp = MediaPlayer.create(this, R.raw.glass_ping);
                                mp.start();
                                Vibrator v = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
                                v.vibrate(500);
                            }
                        } else {
                            Toast.makeText(getBaseContext(), "Unutar kruga si ", Toast.LENGTH_LONG).show();
                            if (myListdata.get(counter).getDepArr() == 1) {
                                MediaPlayer mp;
                                mp = MediaPlayer.create(this, R.raw.glass_ping);
                                mp.start();
                                Vibrator v = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
                                v.vibrate(500);
                            }
                        }
                    }
                    } else {
                        Toast.makeText(getApplicationContext(), "manje", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }


    void  deleteItem() {
        myListdata.remove(rowIndex);
    }

}

class listComparator implements Comparator<listData> {
    @Override
    public int compare(listData itemLeft, listData itemRight) {
        int activeCompare = itemLeft.getActive().compareTo(itemRight.getActive());
        if (activeCompare > 0) {
            return  -activeCompare;
        } else if (activeCompare == 0) {
            return itemLeft.getDateTime().compareTo(itemRight.getDateTime());
        }
        else return 0;
    };
}

