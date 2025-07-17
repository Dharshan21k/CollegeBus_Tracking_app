package com.example.showlocation;

import android.Manifest;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.FragmentActivity;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private DatabaseReference busLocationRef;
    private Marker busMarker;
    private Polyline travelPath;
    private List<LatLng> pathCoordinates;
    private Map<String, LatLng> stops;
    private String busId;

    private final String CHANNEL_ID = "bus_notification_channel";
    private final int NOTIFICATION_ID = 1;
    private NotificationManagerCompat notificationManager;

    // Button for toggling notifications
    private Button notificationButton;
    private LinearLayout notificationBox;
    private List<String> notifications; // Store notifications
    private List<String> receivedNotifications; // Track which notifications have been shown

    // Predefined stops (with actual LatLng values)
    private void setStops() {
        stops = new HashMap<>();
        stops.put("PSR Engineering college", new LatLng( 9.290209,77.701579 ));
        stops.put("Kundampatti", new LatLng(9.281791,77.695555 ));
        stops.put("Bus Stop 3", new LatLng(9.191185, 77.843512));
        stops.put("New bus stand", new LatLng(9.169861,77.862597 ));
        stops.put("Av School", new LatLng(9.170628,77.864935 ));
        stops.put("Prabakaran furniture", new LatLng(9.170740,77.865257 ));
        stops.put("Balaji bakery", new LatLng(9.172519, 77.871601));
        stops.put("Mahalakshmi mahal", new LatLng(9.171882, 77.871776));
        stops.put("Haya Bakery", new LatLng(9.169417,77.872871 ));
        stops.put("bus stop 6", new LatLng(9.166037,77.875498 ));
        stops.put("Rajiv nagar", new LatLng(9.164394,77.876542 ));
        stops.put("bus stop12", new LatLng(9.290209,77.701579 ));


    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        busId = getIntent().getStringExtra("busId"); // Get bus ID (e.g., "bus28location")
        setStops(); // Initialize predefined stops

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Initialize Firebase Database reference
        busLocationRef = FirebaseDatabase.getInstance().getReference("buses").child(busId);

        notificationManager = NotificationManagerCompat.from(this);
        createNotificationChannel();
        scheduleDailyReset();
        initializeTravelPath();

        // Initialize Notification Button and Box
        notificationButton = findViewById(R.id.notification_button);
        notificationBox = findViewById(R.id.notification_box);
        notifications = new ArrayList<>();
        receivedNotifications = new ArrayList<>(); // To track shown notifications

        // Initially hide the notification box
        notificationBox.setVisibility(View.GONE);

        notificationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Toggle visibility of notification box
                if (notificationBox.getVisibility() == View.GONE) {
                    notificationBox.setVisibility(View.VISIBLE);
                    displayAllNotifications(); // Show all notifications when box is opened
                } else {
                    notificationBox.setVisibility(View.GONE);
                }
            }
        });

        // Listen to bus location updates
        busLocationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Double lat = snapshot.child("latitude").getValue(Double.class);
                    Double lng = snapshot.child("longitude").getValue(Double.class);
                    Log.d("MapActivity", "Received location - Latitude: " + lat + ", Longitude: " + lng);

                    if (lat != null && lng != null) {
                        LatLng busPosition = new LatLng(lat, lng);
                        updateBusLocation(busPosition); // Update the marker
                        checkStopArrival(busPosition); // Check if the bus is at a stop
                    } else {
                        Log.e("MapActivity", "Invalid location data");
                    }
                } else {
                    Log.e("MapActivity", "No data available at bus location");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("MapActivity", "Firebase error: " + error.getMessage());
            }
        });
    }

    private void initializeTravelPath() {
        pathCoordinates = new ArrayList<>();

        pathCoordinates.add(new LatLng( 9.290209,77.701579 ));
        pathCoordinates.add(new LatLng(9.281791,77.695555 ));
        pathCoordinates.add(new LatLng(9.191185, 77.843512));
        pathCoordinates.add(new LatLng(9.169861,77.862597 ));
        pathCoordinates.add(new LatLng(9.170628,77.864935 ));
        pathCoordinates.add(new LatLng(9.170740,77.865257 ));
        pathCoordinates.add(new LatLng(9.172519, 77.871601));
        pathCoordinates.add(new LatLng(9.171882, 77.871776));
        pathCoordinates.add(new LatLng(9.169417,77.872871 ));
        pathCoordinates.add(new LatLng(9.166037,77.875498 ));
        pathCoordinates.add(new LatLng(9.164394,77.876542 ));
        pathCoordinates.add(new LatLng(9.290209,77.701579 ));// End point
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(9.5148132, 77.629775), 15));

        // Draw travel path on the map
        PolylineOptions polylineOptions = new PolylineOptions().addAll(pathCoordinates).clickable(false);
        travelPath = mMap.addPolyline(polylineOptions);

        // Add markers for each stop with a unique icon
        for (Map.Entry<String, LatLng> stop : stops.entrySet()) {
            mMap.addMarker(new MarkerOptions().position(stop.getValue()).title(stop.getKey())
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))); // Change the color/icon
        }
    }

    private void updateBusLocation(LatLng busPosition) {
        if (busMarker == null) {
            busMarker = mMap.addMarker(new MarkerOptions().position(busPosition).title("Bus Location")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))); // Bus icon in green
        } else {
            busMarker.setPosition(busPosition);
        }
        mMap.animateCamera(CameraUpdateFactory.newLatLng(busPosition)); // Smooth camera movement
    }

    private void checkStopArrival(LatLng busPosition) {
        for (Map.Entry<String, LatLng> stop : stops.entrySet()) {
            if (isBusAtStop(busPosition, stop.getValue())) {
                String notificationMessage = "Bus has arrived at " + stop.getKey();
                String timestamp = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());

                // Send notification only if not already received
                if (!receivedNotifications.contains(notificationMessage)) {
                    showArrivalNotification(notificationMessage + " at " + timestamp);
                    notifications.add(notificationMessage + " at " + timestamp);
                    receivedNotifications.add(notificationMessage); // Mark as received
                }
            }
        }
    }

    private boolean isBusAtStop(LatLng busPosition, LatLng stopPosition) {
        float[] results = new float[1];
        android.location.Location.distanceBetween(
                busPosition.latitude, busPosition.longitude,
                stopPosition.latitude, stopPosition.longitude,
                results
        );
        return results[0] < 50; // Within 50 meters
    }

    private void showArrivalNotification(String message) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_bus)
                .setContentTitle("Bus Arrival")
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            return;
        }
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "  Bus28 Arrival Notifications";
            String description = "Notifications when the bus arrives at a stop";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    private void scheduleDailyReset() {
        Intent intent = new Intent(this, ResetNotificationsReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        if (alarmManager != null) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, 23);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);

            if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
                calendar.add(Calendar.DAY_OF_MONTH, 1); // Schedule for the next day
            }
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
        }
    }

    private void displayAllNotifications() {
        notificationBox.removeAllViews(); // Clear previous views
        for (String notification : notifications) {
            TextView textView = new TextView(this);
            textView.setText(notification);
            notificationBox.addView(textView);
        }
    }
}
