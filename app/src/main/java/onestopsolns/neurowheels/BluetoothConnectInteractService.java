package onestopsolns.neurowheels;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class BluetoothConnectInteractService {
    enum ConnStates {
        STATE_NONE, STATE_CONNECTING, STATE_CONNECTED
    }
    private BluetoothDevice bluetoothDevice;
    private BluetoothConnectInteractServiceDelegate delegate = null;
    private ConnStates currentState = ConnStates.STATE_NONE;
    private ConnectionThread connectionThread = null;
    private ConnectedAndTransmittingThread connectedAndTransmittingThread = null;

    private UUID secureAppUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    BluetoothConnectInteractService(BluetoothDevice device, BluetoothConnectInteractServiceDelegate delegate){
        this.bluetoothDevice = device;
        this.delegate = delegate;
    }

    private void connectionFailed(String errMsg){
        if (delegate != null) {
            delegate.onConnectionFailed(errMsg);
        }
    }

    private void connectionStateChanged(String state){
        if (delegate != null) {
            delegate.onConnectionStateChanged(state);
        }
    }

    private void connectionLost(){
        if (delegate != null) {
            delegate.onConnectionLost();
        }
        currentState = ConnStates.STATE_NONE;
    }

    synchronized void start(){

        //cancelling any previous connection threads
        if (connectionThread != null){
            connectionThread.cancel();
            connectionThread = null;
        }

        //cancel any currently transmitting threads
        if (connectedAndTransmittingThread != null){
            connectedAndTransmittingThread.cancel();
            connectedAndTransmittingThread = null;
        }

        //start thread to create bluetooth socket
        connectionThread = new ConnectionThread(bluetoothDevice);
        connectionThread.start();
    }

    synchronized private void connectToTransmit(BluetoothSocket socket){
        //cancelling any previous connection threads
        if (connectionThread != null){
            connectionThread.cancel();
            connectionThread = null;
        }

        //cancel any currently transmitting threads
        if (connectedAndTransmittingThread != null){
            connectedAndTransmittingThread.cancel();
            connectedAndTransmittingThread = null;
        }

        //start the transmit thread now that the connection has been established
        connectedAndTransmittingThread = new ConnectedAndTransmittingThread(socket);
        connectedAndTransmittingThread.start();
    }

    synchronized void stop(){

        delegate = null;
        //cancelling any previous connection threads
        if (connectionThread != null){
            connectionThread.cancel();
            connectionThread = null;
        }

        //cancel any currently transmitting threads
        if (connectedAndTransmittingThread != null){
            connectedAndTransmittingThread.cancel();
            connectedAndTransmittingThread = null;
        }

        currentState = ConnStates.STATE_NONE;
    }

    void write(byte[] output){
        ConnectedAndTransmittingThread tmpConnectedThread = null;
        synchronized(this){
            if (currentState != ConnStates.STATE_CONNECTED){
                return;
            }
            else{
                tmpConnectedThread = connectedAndTransmittingThread;
            }
        }
        if (tmpConnectedThread != null) {
            tmpConnectedThread.write(output);
        }
        Log.d("connected write", output.toString());
    }

    //Used to create a socket connection between the bluetooth devices
    private class ConnectionThread extends Thread{
        private BluetoothSocket socket = null;

        ConnectionThread(BluetoothDevice device){
            BluetoothSocket tmpSocket = null;
            try{
                //trying to create a socket with app UUID to connect to remote device
                if (device != null) {
                    tmpSocket = device.createRfcommSocketToServiceRecord(secureAppUUID);
                }
            }
            catch (IOException e){
                Log.e("get socket error:",e.getLocalizedMessage());
            }
            socket = tmpSocket;
            currentState = ConnStates.STATE_CONNECTING;
            connectionStateChanged("Connecting");
        }

        @Override
        public void run() {
            super.run();
            if (socket != null){
                try{
                    //attempt to make a connection with the socket
                    socket.connect();
                }
                catch (IOException e){
                    Log.e("socket conn error:",e.getLocalizedMessage());
                    try {
                        socket.close();
                    }
                    catch (IOException ex){
                        Log.e("close socket err:", ex.getLocalizedMessage());
                    }
                    connectionFailed("Failed to connect to device");
                    return;
                }

                //socket has connected successfully can set the thread to null
                synchronized(BluetoothConnectInteractService.this){
                    connectionThread = null;
                }

                //make a call to start transmitting
                connectToTransmit(socket);
            }
            else{
                connectionFailed("No socket created");
            }
        }

        void cancel(){
            try {
                if (socket != null){
                    socket.close();
                }
            }
            catch (IOException ex){
                Log.e("close socket err:", ex.getLocalizedMessage());
            }
        }
    }

    private class ConnectedAndTransmittingThread extends Thread{
        private BluetoothSocket bluetoothSocket = null;
        private InputStream inputStream = null;
        private OutputStream outputStream = null;

        ConnectedAndTransmittingThread(BluetoothSocket socket){
            bluetoothSocket = socket;
            InputStream tmpIPStream = null;
            OutputStream tmpOPStream = null;

            //get bluetooth ip and op streams
            try{
                tmpIPStream = socket.getInputStream();
                tmpOPStream = socket.getOutputStream();
            }
            catch (IOException e){
                Log.e("fetching io stream", e.getLocalizedMessage());
            }

            inputStream = tmpIPStream;
            outputStream = tmpOPStream;
            currentState = ConnStates.STATE_CONNECTED;
            connectionStateChanged("Connected");
        }

        @Override
        public void run() {
            super.run();
            while (currentState == ConnStates.STATE_CONNECTED) {
                try {
                    if (inputStream != null){
                        inputStream.read();
                    }
                } catch (IOException e){
                    connectionLost();
                    connectionStateChanged("Disconnected");
                    break;
                }
            }
        }

        void write(byte[] buffer){
            try {
                if (outputStream != null) {
                    outputStream.write(buffer);
                }
            }
            catch (IOException e){
                Log.d("write failed", e.getLocalizedMessage());
            }
        }

        void cancel(){
            try {
                if (bluetoothSocket != null) {
                    bluetoothSocket.close();
                }
            }
            catch (IOException ex){
                Log.e("close socket err:", ex.getLocalizedMessage());
            }
        }
    }
}

interface BluetoothConnectInteractServiceDelegate {
    void onConnectionLost();
    void onConnectionStateChanged(String state);
    void onConnectionFailed(String errMsg);
}
