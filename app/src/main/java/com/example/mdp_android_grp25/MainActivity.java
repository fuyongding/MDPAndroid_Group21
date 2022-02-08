package com.example.mdp_android_grp25;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.viewpager.widget.ViewPager;

import com.example.mdp_android_grp25.ui.main.Bluetooth.BluetoothConnectionService;
import com.example.mdp_android_grp25.ui.main.Bluetooth.BluetoothPage;
//import com.example.mdp_android_grp25.ui.main.BluetoothChatFragment;
import com.example.mdp_android_grp25.ui.main.ControlFragment;
import com.example.mdp_android_grp25.ui.main.GridMap;
import com.example.mdp_android_grp25.ui.main.SectionsPagerAdapter;
import com.google.android.material.tabs.TabLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.Charset;
import java.util.UUID;


public class MainActivity extends AppCompatActivity {

    // Declaration Variables
    private static SharedPreferences sharedPreferences;
    private static SharedPreferences.Editor editor;
    private static Context context;

    private static GridMap gridMap;
    private ControlFragment controlFragment;
    static TextView xAxisTextView, yAxisTextView, directionAxisTextView, commandLog;
    static TextView robotStatusTextView, bluetoothStatus, bluetoothDevice;
    static Button upBtn, downBtn, leftBtn, rightBtn;

    BluetoothDevice mBTDevice;
    private static UUID myUUID;
    ProgressDialog myDialog;

    String obstacleID = "";
    String imageID = "";

    private static final String TAG = "Main Activity";
    public static boolean stopTimerFlag = false;
    public static boolean stopWk9TimerFlag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // Initialization
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //this is for the bottom fragment
        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this,
                getSupportFragmentManager());
        ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(sectionsPagerAdapter);
        viewPager.setOffscreenPageLimit(9999);
        //TabLayout tabs = findViewById(R.id.tabs);
        //tabs.setupWithViewPager(viewPager);
        LocalBroadcastManager
                .getInstance(this)
                .registerReceiver(messageReceiver, new IntentFilter("incomingMessage"));

        // Set up sharedPreferences
        MainActivity.context = getApplicationContext();
        this.sharedPreferences();
        editor.putString("message", "");
        editor.putString("direction","None");
        editor.putString("connStatus", "Disconnected");
        editor.commit();

        //Bluetooth Button
        Button bluetoothButton = (Button) findViewById(R.id.bluetoothButton);
        bluetoothButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent popup = new Intent(MainActivity.this, BluetoothPage.class);
                startActivity(popup);
            }
        });

        // Bluetooth Status
        bluetoothStatus = findViewById(R.id.bluetoothStatus);
        bluetoothDevice = findViewById(R.id.bluetoothConnectedDevice);

        //Command Log

        commandLog = findViewById(R.id.commandlog_textview);
        commandLog.setMovementMethod(new ScrollingMovementMethod()); //to make it scrollable

        // Robot Status
        robotStatusTextView = findViewById(R.id.robotStatus);

        if(BluetoothConnectionService.BluetoothConnectionStatus == true){
            showLog("Update robotstatusTextView ready to move");
            robotStatusTextView.setText("Ready to Move");
        }else{
            showLog("Update robotstatusTextView disconnected");
            robotStatusTextView.setText("Disconnected");
        }

        // Controller & on click listeners for controller
        upBtn = findViewById(R.id.upBtn);
        downBtn = findViewById(R.id.downBtn);
        leftBtn = findViewById(R.id.leftBtn);
        rightBtn = findViewById(R.id.rightBtn);

        //if robot is not on map --> notify user to put robot on map --> don't send message
        //if movement will make robot out of bound --> notify user that robot is unable to make the movement --> don't send message
        upBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLog("Up Button pressed");
                //printMessage("f");
                if (gridMap.getCanDrawRobot() && !gridMap.getAutoUpdate()) {
                    gridMap.moveRobot("forward");
                    refreshLabel();
                    if (gridMap.getValidPosition()){
                        Toast.makeText(MainActivity.this, "Moving forward", Toast.LENGTH_SHORT).show();
                        printMessage("f");
                    }
                    else{
                        Toast.makeText(MainActivity.this, "Unable to move forward", Toast.LENGTH_SHORT).show();
                    }
                }
                else {
                    Toast.makeText(MainActivity.this, "Please press 'STARTING POINT'", Toast.LENGTH_SHORT).show();
                }
                showLog("Exiting moving Forward");

            }
        });

        downBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLog("Back Button pressed");
                //printMessage("b");
                if (gridMap.getCanDrawRobot() && !gridMap.getAutoUpdate()) {
                    gridMap.moveRobot("back");
                    refreshLabel();
                    if (gridMap.getValidPosition()){
                        Toast.makeText(MainActivity.this, "Moving backward", Toast.LENGTH_SHORT).show();
                        printMessage("b");
                    }
                    else{
                        Toast.makeText(MainActivity.this, "Unable to move backward", Toast.LENGTH_SHORT).show();
                    }
                }
                else {
                    Toast.makeText(MainActivity.this, "Please press 'STARTING POINT'", Toast.LENGTH_SHORT).show();
                }
                showLog("Exiting moving backwards");
            }
        });

        leftBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLog("Left Button pressed");
                //printMessage("tl");

                if (gridMap.getCanDrawRobot() && !gridMap.getAutoUpdate()) {
                    gridMap.moveRobot("left");
                    refreshLabel();
                    if (gridMap.getValidPosition()){
                        Toast.makeText(MainActivity.this, "Turning left", Toast.LENGTH_SHORT).show();
                        printMessage("tl");
                    }
                    else{
                        Toast.makeText(MainActivity.this, "Unable to turn left", Toast.LENGTH_SHORT).show();
                    }
                }
                else {
                    Toast.makeText(MainActivity.this, "Please press 'STARTING POINT'", Toast.LENGTH_SHORT).show();
                }

                showLog("Exiting left button pressed");


                /**
                if (gridMap.getAutoUpdate())
                    Toast.makeText(MainActivity.this, "Press Manual", Toast.LENGTH_SHORT).show(); //what is thisss
                else if (gridMap.getCanDrawRobot() && !gridMap.getAutoUpdate()) {
                    gridMap.moveRobot("left");
                    MainActivity.refreshLabel();
                    Toast.makeText(MainActivity.this, "Turning left", Toast.LENGTH_SHORT).show();
                    MainActivity.printMessage("sl");
                }
                else
                    Toast.makeText(MainActivity.this, "Please press 'STARTING POINT'", Toast.LENGTH_SHORT).show();
                 **/
            }
        });

        rightBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLog("Right Button pressed");
                //printMessage("tr");

                if (gridMap.getCanDrawRobot() && !gridMap.getAutoUpdate()) {
                    gridMap.moveRobot("right");
                    refreshLabel();
                    if (gridMap.getValidPosition()){
                        Toast.makeText(MainActivity.this, "Turning right", Toast.LENGTH_SHORT).show();
                        printMessage("tr");
                    }
                    else{
                        Toast.makeText(MainActivity.this, "Unable to turn right", Toast.LENGTH_SHORT).show();
                    }
                }
                else {
                    Toast.makeText(MainActivity.this, "Please press 'STARTING POINT'", Toast.LENGTH_SHORT).show();
                }

                showLog("Exiting right button pressed");

                /**
                if (gridMap.getAutoUpdate())
                    Toast.makeText(MainActivity.this, "Press Manual", Toast.LENGTH_SHORT).show(); //what is thisss
                else if (gridMap.getCanDrawRobot() && !gridMap.getAutoUpdate()) {
                    gridMap.moveRobot("right");
                    MainActivity.refreshLabel();
                    Toast.makeText(MainActivity.this, "Turning right", Toast.LENGTH_SHORT).show();
                    MainActivity.printMessage("sr");
                }
                else
                    Toast.makeText(MainActivity.this, "Please press 'STARTING POINT'", Toast.LENGTH_SHORT).show();
                 **/
            }
        });


        myDialog = new ProgressDialog(MainActivity.this);
        myDialog.setMessage("Waiting for other device to reconnect...");
        myDialog.setCancelable(false);
        myDialog.setButton(
            DialogInterface.BUTTON_NEGATIVE,
            "Cancel",
            new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            }
        );

        // Map
        gridMap = new GridMap(this);
        gridMap = findViewById(R.id.mapView);
        xAxisTextView = findViewById(R.id.xAxisTextView);
        yAxisTextView = findViewById(R.id.yAxisTextView);
        directionAxisTextView = findViewById(R.id.directionAxisTextView);

        //```ControlFragment for Timer
        //controlFragment = new ControlFragment();

        // initialize ITEM_LIST and imageBearings strings
        for (int i = 0; i < 20; i++) {
            for (int j = 0; j < 20; j++) {
                gridMap.ITEM_LIST.get(i)[j] = "";
                gridMap.imageBearings.get(i)[j] = "";
                gridMap.IMAGE_LIST.get(i)[j] = "";
            }
        }
    }

    public static GridMap getGridMap() {
        return gridMap;
    }

    public static TextView getRobotStatusTextView() {  return robotStatusTextView; }

    //public static Button getUpBtn() { return upBtn; }
    //public static Button getDownBtn() { return downBtn; }
    //public static Button getLeftBtn() { return leftBtn; }
    //public static Button getRightBtn() { return rightBtn; }

    public static TextView getBluetoothStatus() { return bluetoothStatus; } //KEEP
    public static TextView getConnectedDevice() { return bluetoothDevice; } //KEEP

    public static void sharedPreferences() {
        sharedPreferences = MainActivity.getSharedPreferences(MainActivity.context);
        editor = sharedPreferences.edit();
    }

    // Use this method to send message through bluetooth to robot
    public static void printMessage(String message) {
        showLog("Entering printMessage");
        //editor = sharedPreferences.edit();

        if (BluetoothConnectionService.BluetoothConnectionStatus == true) {
            byte[] bytes = message.getBytes(Charset.defaultCharset());
            BluetoothConnectionService.write(bytes);
        }

        showLog("Exiting printMessage");

        /**
        showLog(message);
        editor.putString("message",
            BluetoothChatFragment.getMessageReceivedTextView().getText() + "\n" + message);
        editor.commit();
        refreshMessageReceived();
         **/
    }

    // Store received message into string
    public static void printMessage(String name, int x, int y) throws JSONException {
        showLog("Entering printMessage");
        sharedPreferences();

        JSONObject jsonObject = new JSONObject();
        String message;

        switch(name) {
            case "waypoint":
                jsonObject.put(name, name);
                jsonObject.put("x", x);
                jsonObject.put("y", y);
                message = name + " (" + x + "," + y + ")";
                break;
            default:
                message = "Unexpected default for printMessage: " + name;
                break;
        }
        //editor.putString("message", BluetoothChatFragment.getMessageReceivedTextView().getText() + "\n" + message);
        editor.commit();
        if (BluetoothConnectionService.BluetoothConnectionStatus == true) {
            byte[] bytes = message.getBytes(Charset.defaultCharset());
            BluetoothConnectionService.write(bytes);
        }
        showLog("Exiting printMessage");
    }

    /**
    public static void refreshMessageReceived() {
        BluetoothChatFragment
            .getMessageReceivedTextView()
            .setText(sharedPreferences.getString("message", ""));
    }
     **/

    public void refreshDirection(String direction) {
        gridMap.setRobotDirection(direction);
        directionAxisTextView.setText(sharedPreferences.getString("direction",""));
        printMessage("Direction is set to " + direction);
    }

    public static void refreshLabel() {
        xAxisTextView.setText(String.valueOf(gridMap.getCurCoord()[0]-1));
        yAxisTextView.setText(String.valueOf(gridMap.getCurCoord()[1]-1));
        directionAxisTextView.setText(sharedPreferences.getString("direction",""));
    }

    public static void receiveMessage(String message) {
        showLog("Entering receiveMessage");
        sharedPreferences();
        editor.putString("message",
            sharedPreferences.getString("message", "") + "\n" + message);
        editor.commit();
        showLog("Exiting receiveMessage");
    }

    private BroadcastReceiver mBroadcastReceiver5 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            BluetoothDevice mDevice = intent.getParcelableExtra("Device");
            String status = intent.getStringExtra("Status");
            sharedPreferences();

            if(status.equals("connected")){
                try {
                    myDialog.dismiss();
                } catch(NullPointerException e){
                    e.printStackTrace();
                }

                Log.d(TAG, "mBroadcastReceiver5: Device now connected to "+mDevice.getName());
                Toast.makeText(MainActivity.this, "Device now connected to "
                        + mDevice.getName(), Toast.LENGTH_LONG).show();
                editor.putString("connStatus", "Connected to " + mDevice.getName());
                robotStatusTextView.setText("Ready to move"); //why is this not showinggg
            }
            else if(status.equals("disconnected")){
                Log.d(TAG, "mBroadcastReceiver5: Disconnected from "+mDevice.getName());
                Toast.makeText(MainActivity.this, "Disconnected from "
                        + mDevice.getName(), Toast.LENGTH_LONG).show();

                editor.putString("connStatus", "Disconnected");
                robotStatusTextView.setText("Disconnected");

                myDialog.show();
            }
            editor.commit();
        }
    };

    // message handler
    // alg sends x,y,robotDirection,movementAction
    // alg sends ALG,<obstacle id>
    // rpi sends RPI,<image id>
    BroadcastReceiver messageReceiver;

    {
        messageReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String message = intent.getStringExtra("receivedMessage");
                showLog("receivedMessage: message --- " + message);

                if (message.contains(",")) {
                    String[] cmd = message.split(",");
                    showLog(cmd.toString()); //to get the received message

                    // check if string is cmd sent by ALG/RPI to get obstacle/image id, to update image ID
                    if (cmd[0].equals("ALG") || cmd[0].equals("RPI")) {
                        showLog("cmd[0] is ALG or RPI");
                        if (obstacleID.equals(""))
                            obstacleID = cmd[0].equals("ALG") ? cmd[1] : "";
                        if (imageID.equals(""))
                            imageID = cmd[0].equals("RPI") ? cmd[1] : "";

                        showLog("obstacleID = " + obstacleID);
                        showLog("imageID = " + imageID);

                        // call update fn only when both IDs are obtained
                        if (!(obstacleID.equals("") || imageID.equals(""))) {
                            showLog("imageID and obstacleID not empty");
                            gridMap.updateIDFromRpi(obstacleID, imageID);
                            obstacleID = ""; //reset it to empty after updating
                            imageID = ""; //reset it to empty after updating
                        }

                        commandLog.append(cmd.toString() + "\n");
                    }
                    //For checklist C9
                    else if(cmd[0].equals("TARGET")){
                        showLog("cmd[0] is Target");
                        obstacleID = cmd[1];
                        imageID = cmd[2];
                        showLog("obstacleID = " + obstacleID);
                        showLog("imageID = " + imageID);
                        gridMap.updateIDFromRpi(obstacleID, imageID);
                        commandLog.append(message + "\n");
                    }

                    //for robot status
                    else if(cmd[0].equals("status")){
                        showLog("Updating robot status");
                        robotStatusTextView.setText(cmd[1]);
                    }

                    else if(cmd[0].equals("ROBOT")){
                        showLog("Updating robot position");
                        int x = Integer.parseInt(cmd[1]);
                        int y = Integer.parseInt(cmd[2]);
                        String direction = cmd[3];

                        gridMap.setCurCoord(x, y, direction); //must get integer for x and y

                        commandLog.append(message + "\n");


                    }

                    else {

                        // alg sends in cm and float e.g. 100,100,N
                        float x = Integer.parseInt(cmd[1]);
                        float y = Integer.parseInt(cmd[2]);

                        // process received figures to pass into our fn
                        int a = Math.round(x);
                        int b = Math.round(y);
                        a = (a / 10) + 1;
                        b = (b / 10) + 1;

                        String direction = cmd[3];

                        // allow robot to show up on grid if its on the very boundary
                        if (a == 1) a++;
                        if (b == 20) b--;

                        //This is to move the robot, performAlgoCommand
                        if (cmd.length == 4) {
                            String command = cmd[3];

                            // if move forward, reverse, turn on the spot left/right
                            if (command.equals("f") || command.equals("r") || command.equals("sr")
                                    || command.equals("sl")) {
                                showLog("forward, reverse or turn on spot");
                                gridMap.performAlgoCommand(a, b, direction);
                            }
                            // for all other turning cmds
                            else {
                                gridMap.performAlgoTurning(a, b, direction, command);
                            }
                        }
                    }
                } else if (message.equals("END")) {
                    // if wk 8 btn is checked, means running wk 8 challenge and likewise for wk 9
                    // end the corresponding timer
                    ToggleButton exploreButton = findViewById(R.id.exploreToggleBtn3);
                    ToggleButton fastestButton = findViewById(R.id.fastestToggleBtn3);

                    if (exploreButton.isChecked()) {
                        showLog("explorebutton is checked");
                        stopTimerFlag = true;
                        exploreButton.setChecked(false);
                        robotStatusTextView.setText("Auto Movement/ImageRecog Stopped");
                    } else if (fastestButton.isChecked()) {
                        showLog("fastestbutton is checked");
                        stopTimerFlag = true;
                        fastestButton.setChecked(false);
                        robotStatusTextView.setText("Week 9 Stopped");
                    }
                }
            }
        };
    }


    private static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences("Shared Preferences", Context.MODE_PRIVATE);
    }

    private static void showLog(String message) {
        Log.d(TAG, message);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode){
            case 1:
                if(resultCode == Activity.RESULT_OK){
                    mBTDevice = (BluetoothDevice) data.getExtras().getParcelable("mBTDevice");
                    myUUID = (UUID) data.getSerializableExtra("myUUID");
                }
        }
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        try{
            LocalBroadcastManager.getInstance(this).unregisterReceiver(messageReceiver);
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver5);
        } catch(IllegalArgumentException e){
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause(){
        super.onPause();
        try{
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver5);
        } catch(IllegalArgumentException e){
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
        try{
            IntentFilter filter2 = new IntentFilter("ConnectionStatus");
            LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver5, filter2);
        } catch(IllegalArgumentException e){
            e.printStackTrace();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        showLog("Entering onSaveInstanceState");
        super.onSaveInstanceState(outState);

        outState.putString(TAG, "onSaveInstanceState");
        showLog("Exiting onSaveInstanceState");
    }
}

