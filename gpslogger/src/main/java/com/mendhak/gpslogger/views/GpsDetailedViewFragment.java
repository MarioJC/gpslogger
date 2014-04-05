package com.mendhak.gpslogger.views;

import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.mendhak.gpslogger.R;
import com.mendhak.gpslogger.common.AppSettings;
import com.mendhak.gpslogger.common.Session;
import com.mendhak.gpslogger.common.Utilities;
import com.mendhak.gpslogger.views.component.ToggleComponent;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by oceanebelle on 03/04/14.
 */
public class GpsDetailedViewFragment extends GenericViewFragment {

    private ToggleComponent toggleComponent;
    private View rootView;

    public static final GpsDetailedViewFragment newInstance() {

        GpsDetailedViewFragment fragment = new GpsDetailedViewFragment();
        Bundle bundle = new Bundle(1);
        bundle.putInt("a_number",1);

        fragment.setArguments(bundle);
        return fragment;


    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // TODO: Inflates the detailed layout

        rootView = inflater.inflate(R.layout.fragment_detailed_view, container, false);

        // Toggle the play and pause views.
        toggleComponent = ToggleComponent.getBuilder()
                .addOnView(rootView.findViewById(R.id.detailedview_play))
                .addOffView(rootView.findViewById(R.id.detailedview_stop))
                .setDefaultState(!Session.isStarted())
                .addHandler(new ToggleComponent.ToggleHandler() {
                    @Override
                    public void onStatusChange(boolean status) {
                        if (status) {
                            requestStartLogging();
                        } else {
                            requestStopLogging();
                        }
                    }
                })
                .build();

        return rootView;
    }

    @Override
    public void SetLocation(Location locationInfo) {
        if (locationInfo == null)
        {
            return;
        }

        TextView tvLatitude = (TextView) rootView.findViewById(R.id.detailedview_lat_text);
        TextView tvLongitude = (TextView) rootView.findViewById(R.id.detailedview_lon_text);
        TextView tvDateTime = (TextView) rootView.findViewById(R.id.detailedview_datetime_text);

        TextView tvAltitude = (TextView) rootView.findViewById(R.id.detailedview_altitude_text);

        TextView txtSpeed = (TextView) rootView.findViewById(R.id.detailedview_speed_text);

        TextView txtSatellites = (TextView) rootView.findViewById(R.id.detailedview_satellites_text);
        TextView txtDirection = (TextView) rootView.findViewById(R.id.detailedview_direction_text);
        TextView txtAccuracy = (TextView) rootView.findViewById(R.id.detailedview_accuracy_text);
        TextView txtTravelled = (TextView) rootView.findViewById(R.id.detailedview_travelled_text);
        TextView txtTime = (TextView) rootView.findViewById(R.id.detailedview_duration_text);
        String providerName = locationInfo.getProvider();
        if (providerName.equalsIgnoreCase("gps"))
        {
            providerName = getString(R.string.providername_gps);
        }
        else
        {
            providerName = getString(R.string.providername_celltower);
        }

        tvDateTime.setText(new Date(Session.getLatestTimeStamp()).toLocaleString()
                + getString(R.string.providername_using, providerName));

        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(3);


        tvLatitude.setText(String.valueOf(   locationInfo.getLatitude()));
        tvLongitude.setText(String.valueOf(locationInfo.getLongitude()));

        if (locationInfo.hasAltitude())
        {

            double altitude = locationInfo.getAltitude();

            if (AppSettings.shouldUseImperial())
            {
                tvAltitude.setText( nf.format(Utilities.MetersToFeet(altitude))
                        + getString(R.string.feet));
            }
            else
            {
                tvAltitude.setText(nf.format(altitude) + getString(R.string.meters));
            }

        }
        else
        {
            tvAltitude.setText(R.string.not_applicable);
        }

        if (locationInfo.hasSpeed())
        {

            float speed = locationInfo.getSpeed();
            String unit;
            if (AppSettings.shouldUseImperial())
            {
                if (speed > 1.47)
                {
                    speed = speed * 0.6818f;
                    unit = getString(R.string.miles_per_hour);

                }
                else
                {
                    speed = Utilities.MetersToFeet(speed);
                    unit = getString(R.string.feet_per_second);
                }
            }
            else
            {
                if (speed > 0.277)
                {
                    speed = speed * 3.6f;
                    unit = getString(R.string.kilometers_per_hour);
                }
                else
                {
                    unit = getString(R.string.meters_per_second);
                }
            }

            txtSpeed.setText(String.valueOf(nf.format(speed)) + unit);

        }
        else
        {
            txtSpeed.setText(R.string.not_applicable);
        }

        if (locationInfo.hasBearing())
        {

            float bearingDegrees = locationInfo.getBearing();
            String direction;

            direction = Utilities.GetBearingDescription(bearingDegrees, getActivity().getApplicationContext());

            txtDirection.setText(direction + "(" + String.valueOf(Math.round(bearingDegrees))
                    + getString(R.string.degree_symbol) + ")");
        }
        else
        {
            txtDirection.setText(R.string.not_applicable);
        }

        if (!Session.isUsingGps())
        {
            txtSatellites.setText(R.string.not_applicable);
            Session.setSatelliteCount(0);
        }

        if (locationInfo.hasAccuracy())
        {

            float accuracy = locationInfo.getAccuracy();

            if (AppSettings.shouldUseImperial())
            {
                txtAccuracy.setText(getString(R.string.accuracy_within,
                        nf.format(Utilities.MetersToFeet(accuracy)), getString(R.string.feet)));

            }
            else
            {
                txtAccuracy.setText(getString(R.string.accuracy_within, nf.format(accuracy),
                        getString(R.string.meters)));
            }

        }
        else
        {
            txtAccuracy.setText(R.string.not_applicable);
        }


        String distanceUnit;
        double distanceValue = Session.getTotalTravelled();
        if (AppSettings.shouldUseImperial())
        {
            distanceUnit = getString(R.string.feet);
            distanceValue = Utilities.MetersToFeet(distanceValue);
            // When it passes more than 1 kilometer, convert to miles.
            if (distanceValue > 3281)
            {
                distanceUnit = getString(R.string.miles);
                distanceValue = distanceValue / 5280;
            }
        }
        else
        {
            distanceUnit = getString(R.string.meters);
            if (distanceValue > 1000)
            {
                distanceUnit = getString(R.string.kilometers);
                distanceValue = distanceValue / 1000;
            }
        }

        txtTravelled.setText(nf.format(distanceValue) + " " + distanceUnit +
                " (" + Session.getNumLegs() + " points)");

        long startTime = Session.getStartTimeStamp();
        Date d = new Date(startTime);
        long currentTime = System.currentTimeMillis();
        String duration = getInterval(startTime, currentTime);

        DateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
        DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(getActivity().getApplicationContext());
        txtTime.setText(duration+" (started at "+dateFormat.format(d)+" "+timeFormat.format(d)+")");

    }

    private String getInterval(long startTime, long endTime)
    {
        StringBuffer sb = new StringBuffer();
        long diff = endTime - startTime;
        long diffSeconds = diff / 1000 % 60;
        long diffMinutes = diff / (60 * 1000) % 60;
        long diffHours = diff / (60 * 60 * 1000) % 24;
        long diffDays = diff / (24 * 60 * 60 * 1000);
        if (diffDays > 0)
        {
            sb.append(diffDays + " days ");
        }
        if (diffHours > 0)
        {
            sb.append(String.format("%02d", diffHours)+":");
        }
        sb.append(String.format("%02d", diffMinutes)+":");
        sb.append(String.format("%02d", diffSeconds));
        return sb.toString();
    }

    @Override
    public void SetSatelliteCount(int count) {

    }

    @Override
    public void SetLoggingStarted() {
        toggleComponent.SetEnabled(false);
    }

    @Override
    public void SetLoggingStopped() {
        toggleComponent.SetEnabled(true);
    }
}
