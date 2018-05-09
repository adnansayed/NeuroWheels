package onestopsolns.neurowheels;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Set;


public class Dashboard extends Fragment implements BluetoothConnectInteractServiceDelegate {
    private final int REQ_ENABLE_BT = 101;
    private final int REQ_ENABLE_LOC = 102;
    private BluetoothAdapter btAdapter = null;
    private BluetoothDevice targetBTDevice = null;
    private boolean isInitialCheckPerformed = false;
    private boolean shouldCheckforHC05 = true;
    private BluetoothConnectInteractService bluetoothConnectionService = null;

    private RadioGroup radioBtnGrp;
    private ImageView centerImage;
    private ImageView dotsTopImage;
    private ImageView dotsBottomImage;
    private ImageView dotsLeftImage;
    private ImageView dotsRightImage;
    private Button btnFront;
    private Button btnBack;
    private Button btnLeft;
    private Button btnRight;
    private ConstraintLayout errorView;
    private ImageView errorImage;
    private TextView errorText;

    //create a broadcast receiver to get the info about discovered devices
    private BroadcastReceiver deviceDiscoveryReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null && !action.isEmpty()) {
                if (action.equalsIgnoreCase(BluetoothAdapter.ACTION_DISCOVERY_STARTED)) {
                    Log.d("bluetooth discovery", "started");
                    errorText.setText("Scanning for device");
                    errorView.setBackgroundColor(ContextCompat.getColor(Dashboard.this.getActivity(), android.R.color.holo_purple));
                    errorImage.setVisibility(View.GONE);
                    errorView.setVisibility(View.VISIBLE);
                }
                if (action.equalsIgnoreCase(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)) {
                    Log.d("bluetooth discovery", "ended");
                    errorView.setVisibility(View.GONE);
                }
                if (action.equalsIgnoreCase(BluetoothDevice.ACTION_FOUND)) {
                    BluetoothDevice discoveredDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    if (discoveredDevice != null) {
                        if (isRequiredDevice(discoveredDevice)) {
                            if (btAdapter != null && btAdapter.isDiscovering()) {
                                btAdapter.cancelDiscovery();
                            }
                            targetBTDevice = discoveredDevice;
                            if (bluetoothConnectionService != null) {
                                bluetoothConnectionService.stop();
                                bluetoothConnectionService = null;
                            }
                            bluetoothConnectionService = new BluetoothConnectInteractService(targetBTDevice, Dashboard.this);
                            bluetoothConnectionService.start();
                            Log.d("device obj", "found and loaded");
                        }
                    }
                }
            }
        }
    };

    //Broadcast receiver for bluetooth state changes
    private BroadcastReceiver bluetoothStateChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null && !action.isEmpty()) {
                if (action.equalsIgnoreCase(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                    int changeState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                    switch (changeState) {
                        case BluetoothAdapter.STATE_ON:
                            errorView.setVisibility(View.GONE);
                            break;

                        case BluetoothAdapter.STATE_OFF:
                            errorText.setText("Bluetooth connection is required, Please enable Bluetooth");
                            errorView.setBackgroundColor(ContextCompat.getColor(Dashboard.this.getActivity(), android.R.color.holo_red_dark));
                            errorImage.setVisibility(View.VISIBLE);
                            errorView.setVisibility(View.VISIBLE);
                            break;
                    }
                    Log.d("state changed", "bluetooth state changed");
                }
            }
        }
    };

    //Delegate Implementations
    @Override
    public void onConnectionLost() {
        Dashboard.this.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
//                Toast.makeText(MainFragment.this.getActivity()," connection lost",Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onConnectionStateChanged(final String state) {
        Dashboard.this.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (state.equalsIgnoreCase("Connecting")) {
                    errorText.setText("Attempting Connection to Device");
                    errorView.setBackgroundColor(ContextCompat.getColor(Dashboard.this.getActivity(), android.R.color.holo_green_light));
                    errorImage.setVisibility(View.GONE);
                    errorView.setVisibility(View.VISIBLE);
                }
                if (state.equalsIgnoreCase("Connected")) {
                    errorText.setText("Connected to Bluetooth device");
                    errorView.setBackgroundColor(ContextCompat.getColor(Dashboard.this.getActivity(), android.R.color.holo_green_dark));
                    errorImage.setVisibility(View.GONE);
                    errorView.setVisibility(View.VISIBLE);
                }
                if (state.equalsIgnoreCase("Disconnected")) {
                    if (bluetoothConnectionService != null) {
                        bluetoothConnectionService.stop();
                        bluetoothConnectionService = null;
                    }
                    errorText.setText("Bluetooth connection lost");
                    errorView.setBackgroundColor(ContextCompat.getColor(Dashboard.this.getActivity(), android.R.color.holo_orange_dark));
                    errorImage.setVisibility(View.VISIBLE);
                    errorView.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    @Override
    public void onConnectionFailed(final String errMsg) {
        Dashboard.this.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (errMsg != null && !errMsg.isEmpty()) {
                    Toast.makeText(Dashboard.this.getActivity(), errMsg, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    //Constructor
    public Dashboard() {
        // Required empty public constructor
    }

    //Fragment Lifecycle Methods
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View fragmentView = inflater.inflate(R.layout.fragment_dashboard, container, false);

        //attaching UI elements
        radioBtnGrp = (RadioGroup) fragmentView.findViewById(R.id.radioButtonGroup);
        centerImage = (ImageView) fragmentView.findViewById(R.id.centerImage);
        dotsTopImage = (ImageView) fragmentView.findViewById(R.id.topDots);
        dotsBottomImage = (ImageView) fragmentView.findViewById(R.id.bottomDots);
        dotsLeftImage = (ImageView) fragmentView.findViewById(R.id.leftDots);
        dotsRightImage = (ImageView) fragmentView.findViewById(R.id.rightDots);
        btnFront = (Button) fragmentView.findViewById(R.id.btnFront);
        btnBack = (Button) fragmentView.findViewById(R.id.btnBack);
        btnLeft = (Button) fragmentView.findViewById(R.id.btnLeft);
        btnRight = (Button) fragmentView.findViewById(R.id.btnRight);
        errorView = (ConstraintLayout) fragmentView.findViewById(R.id.errorView);
        errorImage = (ImageView) fragmentView.findViewById(R.id.alertIcon);
        errorText = (TextView) fragmentView.findViewById(R.id.errMsg);

        //fetch default adapter
        btAdapter = BluetoothAdapter.getDefaultAdapter();

        errorView.setVisibility(View.GONE);

        //register the broadcast receiver for all intent actions needed
        IntentFilter filterStateChange = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        IntentFilter filterDiscoveryStart = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        IntentFilter filterDiscoveryFin = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        IntentFilter filterDeviceFound = new IntentFilter(BluetoothDevice.ACTION_FOUND);

        Dashboard.this.getActivity().registerReceiver(bluetoothStateChangeReceiver, filterStateChange);
        Dashboard.this.getActivity().registerReceiver(deviceDiscoveryReceiver, filterDeviceFound);
        Dashboard.this.getActivity().registerReceiver(deviceDiscoveryReceiver, filterDiscoveryStart);
        Dashboard.this.getActivity().registerReceiver(deviceDiscoveryReceiver, filterDiscoveryFin);

        //button click listeners
        //setting up front movement click
        final Handler frontHandler = new Handler();

        final Runnable frontRunnable = new Runnable() {
            @Override
            public void run() {
                if (bluetoothConnectionService != null) {
                    bluetoothConnectionService.write("w".getBytes());
                }
                frontHandler.post(this);
            }
        };

        btnFront.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_UP:
                        frontHandler.removeCallbacks(frontRunnable);
                        centerImage.setImageResource(R.drawable.center_basic);
                        dotsTopImage.setImageResource(R.drawable.top_dots_inactive);
                        dotsBottomImage.setImageResource(R.drawable.down_dots_inactive);
                        dotsLeftImage.setImageResource(R.drawable.left_dots_inactive);
                        dotsRightImage.setImageResource(R.drawable.right_dots_inactive);
                        btnFront.setBackgroundResource(R.drawable.btn_up_inactive);
                        break;
                    case MotionEvent.ACTION_DOWN:
                        frontHandler.post(frontRunnable);
                        centerImage.setImageResource(R.drawable.top_center);
                        dotsTopImage.setImageResource(R.drawable.top_dots);
                        dotsBottomImage.setImageResource(R.drawable.down_dots_inactive);
                        dotsLeftImage.setImageResource(R.drawable.left_dots_inactive);
                        dotsRightImage.setImageResource(R.drawable.right_dots_inactive);
                        btnFront.setBackgroundResource(R.drawable.btn_up_active);
                        break;
                    default:
                        break;
                }
                return true;
            }
        });


        //setting up back movement click
        final Handler backHandler = new Handler();

        final Runnable backRunnable = new Runnable() {
            @Override
            public void run() {
                if (bluetoothConnectionService != null) {
                    bluetoothConnectionService.write("s".getBytes());
                }
                backHandler.post(this);
            }
        };

        btnBack.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_UP:
                        backHandler.removeCallbacks(backRunnable);
                        centerImage.setImageResource(R.drawable.center_basic);
                        dotsTopImage.setImageResource(R.drawable.top_dots_inactive);
                        dotsBottomImage.setImageResource(R.drawable.down_dots_inactive);
                        dotsLeftImage.setImageResource(R.drawable.left_dots_inactive);
                        dotsRightImage.setImageResource(R.drawable.right_dots_inactive);
                        btnBack.setBackgroundResource(R.drawable.btn_down_inactive);
                        break;
                    case MotionEvent.ACTION_DOWN:
                        backHandler.post(backRunnable);
                        centerImage.setImageResource(R.drawable.down_center);
                        dotsTopImage.setImageResource(R.drawable.top_dots_inactive);
                        dotsBottomImage.setImageResource(R.drawable.down_dots_active);
                        dotsLeftImage.setImageResource(R.drawable.left_dots_inactive);
                        dotsRightImage.setImageResource(R.drawable.right_dots_inactive);
                        btnBack.setBackgroundResource(R.drawable.btn_down_active);
                        break;
                    default:
                        break;
                }
                return true;
            }
        });

        //setting up left movement click
        final Handler leftHandler = new Handler();

        final Runnable leftRunnable = new Runnable() {
            @Override
            public void run() {
                if (bluetoothConnectionService != null) {
                    bluetoothConnectionService.write("a".getBytes());
                }
                leftHandler.post(this);
            }
        };

        btnLeft.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_UP:
                        leftHandler.removeCallbacks(leftRunnable);
                        centerImage.setImageResource(R.drawable.center_basic);
                        dotsTopImage.setImageResource(R.drawable.top_dots_inactive);
                        dotsBottomImage.setImageResource(R.drawable.down_dots_inactive);
                        dotsLeftImage.setImageResource(R.drawable.left_dots_inactive);
                        dotsRightImage.setImageResource(R.drawable.right_dots_inactive);
                        btnLeft.setBackgroundResource(R.drawable.btn_left_inactive);
                        break;
                    case MotionEvent.ACTION_DOWN:
                        leftHandler.post(leftRunnable);
                        centerImage.setImageResource(R.drawable.left_center);
                        dotsTopImage.setImageResource(R.drawable.top_dots_inactive);
                        dotsBottomImage.setImageResource(R.drawable.down_dots_inactive);
                        dotsLeftImage.setImageResource(R.drawable.left_dots_active);
                        dotsRightImage.setImageResource(R.drawable.right_dots_inactive);
                        btnLeft.setBackgroundResource(R.drawable.btn_left_active);
                        break;
                    default:
                        break;
                }
                return true;
            }
        });

        //setting up right movement click
        final Handler rightHandler = new Handler();

        final Runnable rightRunnable = new Runnable() {
            @Override
            public void run() {
                if (bluetoothConnectionService != null) {
                    bluetoothConnectionService.write("d".getBytes());
                }
                rightHandler.post(this);
            }
        };

        btnRight.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_UP:
                        rightHandler.removeCallbacks(rightRunnable);
                        centerImage.setImageResource(R.drawable.center_basic);
                        dotsTopImage.setImageResource(R.drawable.top_dots_inactive);
                        dotsBottomImage.setImageResource(R.drawable.down_dots_inactive);
                        dotsLeftImage.setImageResource(R.drawable.left_dots_inactive);
                        dotsRightImage.setImageResource(R.drawable.right_dots_inactive);
                        btnRight.setBackgroundResource(R.drawable.btn_right_inactive);
                        break;
                    case MotionEvent.ACTION_DOWN:
                        rightHandler.post(rightRunnable);
                        centerImage.setImageResource(R.drawable.right_center);
                        dotsTopImage.setImageResource(R.drawable.top_dots_inactive);
                        dotsBottomImage.setImageResource(R.drawable.down_dots_inactive);
                        dotsLeftImage.setImageResource(R.drawable.left_dots_inactive);
                        dotsRightImage.setImageResource(R.drawable.right_dots_active);
                        btnRight.setBackgroundResource(R.drawable.btn_right_active);
                        break;
                    default:
                        break;
                }
                return true;
            }
        });


        radioBtnGrp.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (fragmentView.findViewById(checkedId).getTag() instanceof String) {
                    if (((String) fragmentView.findViewById(checkedId).getTag()).equalsIgnoreCase("0")) {
                        if (!shouldCheckforHC05) {
                            shouldCheckforHC05 = true;
                            if (btAdapter != null && btAdapter.isEnabled()) {
                                if (bluetoothConnectionService != null) {
                                    bluetoothConnectionService.stop();
                                    bluetoothConnectionService = null;
                                }
                                checkPairedDevices();
                            }
                        }
                    } else {
                        if (shouldCheckforHC05) {
                            shouldCheckforHC05 = false;
                            if (btAdapter != null && btAdapter.isEnabled()) {
                                if (bluetoothConnectionService != null) {
                                    bluetoothConnectionService.stop();
                                    bluetoothConnectionService = null;
                                }
                                checkPairedDevices();
                            }
                        }
                    }
                }
            }
        });

        return fragmentView;
    }

    @Override
    public void onStart() {
        super.onStart();
        //If the adapter is null the device does not support bluetooth
        if (btAdapter == null) {
            Toast.makeText(Dashboard.this.getActivity(), "This device does not support Bluetooth", Toast.LENGTH_LONG).show();
            errorText.setText("Device does not support Bluetooth.");
            errorView.setBackgroundColor(ContextCompat.getColor(Dashboard.this.getActivity(), android.R.color.holo_red_dark));
            errorView.setVisibility(View.VISIBLE);
        } else {
            beginChecks();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (bluetoothConnectionService != null) {
            bluetoothConnectionService.stop();
            bluetoothConnectionService = null;
        }
        Dashboard.this.getActivity().unregisterReceiver(deviceDiscoveryReceiver);
        Dashboard.this.getActivity().unregisterReceiver(bluetoothStateChangeReceiver);
    }

     /*=======================================
    * Auxiliary Methods
    *========================================*/

    //check if bluetooth is on, if not start action to request to turn it on
    private void beginChecks() {
        if (btAdapter != null && !btAdapter.isEnabled()) {
            if (!isInitialCheckPerformed) {
                isInitialCheckPerformed = true;
                Intent enableBTintent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBTintent, REQ_ENABLE_BT);
            }
        } else {
            checkPairedDevices();
        }
    }

    //check for coarse location permission before starting discovery
    private boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(Dashboard.this.getActivity(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(Dashboard.this.getActivity(), new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION}, REQ_ENABLE_LOC);
            return false;
        } else {
            return true;
        }
    }

    /*check if Bt module is present amoung the paired devices
    * we are looking for bluetooth module HC-06 with specs below
    * BT Name: HC-06
    * BT Address: 00:21:13:01:7C:59
    * and trying to pair only with it*/
    private void checkPairedDevices() {
        if (btAdapter != null) {
            Set<BluetoothDevice> pairedBTdevices = btAdapter.getBondedDevices();

            if (!pairedBTdevices.isEmpty()) {
                errorText.setText("Searching paired devices");
                errorView.setBackgroundColor(ContextCompat.getColor(Dashboard.this.getActivity(), android.R.color.holo_purple));
                errorImage.setVisibility(View.GONE);
                errorView.setVisibility(View.VISIBLE);
                boolean isDeviceAmoungPaired = false;
                for (BluetoothDevice devices : pairedBTdevices) {
                    if (isRequiredDevice(devices)) {
                        isDeviceAmoungPaired = true;
                        targetBTDevice = devices;

                        //device found start service to form connection and begin transmitting
                        if (btAdapter != null && btAdapter.isDiscovering()) {
                            btAdapter.cancelDiscovery();
                        }
                        if (bluetoothConnectionService != null) {
                            bluetoothConnectionService.stop();
                            bluetoothConnectionService = null;
                        }
                        bluetoothConnectionService = new BluetoothConnectInteractService(targetBTDevice, Dashboard.this);
                        bluetoothConnectionService.start();
                        Log.d("device obj", "found and loaded");
                        break;
                    }
                }
                if (!isDeviceAmoungPaired) {
                    if (checkLocationPermission()) {
                        btAdapter.startDiscovery();
                    }
                }
            } else {
                if (checkLocationPermission()) {
                    btAdapter.startDiscovery();
                }
            }
        }
    }

    /*Check device to see if it matches the name and mac address
    * we are looking for bluetooth module HC-06 with specs below
    * BT Name: HC-06
    * BT Address: 00:21:13:01:7C:59
    * hc05 addr 00:15:83:35:87:30

    * and trying to pair only with it*/
    private boolean isRequiredDevice(BluetoothDevice device) {
        String checkAddress;
        if (shouldCheckforHC05) {
            checkAddress = "00:15:83:35:87:30";
        } else {
            checkAddress = "00:21:13:01:7C:59";
        }
        return device.getAddress().contentEquals(checkAddress);
    }
}
