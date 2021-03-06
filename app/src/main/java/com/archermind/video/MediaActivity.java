package com.archermind.video;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothProfile;
/*import android.car.export.Car;
import android.car.export.CarHUDManager;
import android.car.export.CarNotConnectedException;*/
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
/*import android.os.SystemProperties;*/
import android.provider.MediaStore.Images;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
/*import android.widget.Toast;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothProfile;
import com.archermind.media.music.BtMusicFragment;
import com.archermind.media.music.MusicFragment;
import com.archermind.media.music.MusicListFragment;
import com.archermind.media.music.utils.ScanMusic;
import com.archermind.media.photo.PictureFragment;*/
import com.archermind.video.FileInfo;
import com.archermind.video.VideoFragment;
import com.archermind.video.VideoPlayFragment;
import com.archermind.video.VideoUtils;
import com.archermind.video.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MediaActivity extends FragmentActivity implements VideoFragment.OnFragmentInteractionListener,VideoPlayFragment.OnFragmentInteractionListener/*,View.OnClickListener*/{
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_SETTINGS,
            Manifest.permission.MANAGE_DOCUMENTS,
          };

    public static int displayTab = 2;
    private int videoTab =2;
    private int videoPlayTab = 4;
    private FragmentManager fragmentManager;
    private FragmentTransaction fragmentTransaction;
    private VideoFragment videoFragment;
    private VideoPlayFragment videoPlayFragment;
    private Intent intent;
    public static String current_source_name = "本地";
    public static String current_source_path = "external";
    public static HashMap<String,String> name_path = new HashMap();
    private String startFragment;
    private BroadcastReceiver usb_out;
    private String type;
    public  static int currentPosition =0;
    private AudioManager audioManager;
    private BluetoothAdapter bluetoothAdapter;
    private ArrayList<FileInfo> videoArraryList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        requestPermissions(PERMISSIONS_STORAGE,REQUEST_EXTERNAL_STORAGE);
        checkRequiredPermission(this);
        setContentView(R.layout.activity_media);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        // CH <BugId:2419> <lizhi> <20180324> modify begin
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        // CH <BugId:2419> <lizhi> <20180324> modify end
        IntentFilter filter = new IntentFilter("com.archermind.media.USBOUT");
        usb_out = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (name_path.containsKey(intent.getStringExtra("name"))){
                    name_path.remove(intent.getStringExtra("name"));
                    Map.Entry<String,String> entry = name_path.entrySet().iterator().next();
                    current_source_name = entry.getKey();
                    current_source_path = entry.getValue();
                    if (displayTab == videoTab){

                    }else if (displayTab == videoPlayTab) {
                        fragmentTransaction = fragmentManager.beginTransaction();
                        fragmentTransaction.show(videoFragment).hide(videoPlayFragment);
                        fragmentTransaction.commit();
                        displayTab = videoTab;
                    }
                }

            }
        };
        registerReceiver(usb_out,filter);
        name_path.put("本地","external");
        videoFragment = new VideoFragment();
        videoPlayFragment = new VideoPlayFragment();
        fragmentManager = getSupportFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();
        intent = getIntent();
        startFragment = intent.getStringExtra("startFragment");
        if (startFragment != null){
            current_source_name = intent.getStringExtra("name");
            current_source_path = intent.getStringExtra("path");
            if (!name_path.containsKey(current_source_name)) {
                name_path.put(current_source_name, current_source_path);
            }

        }else {
            if (bluetoothAdapter != null
                    && bluetoothAdapter.isEnabled()
                    && bluetoothAdapter.getProfileConnectionState(11) == BluetoothProfile.STATE_CONNECTED) {
            } else{
                fragmentTransaction.add(R.id.fragment_container,videoFragment);
                fragmentTransaction.commit();

                displayTab = videoTab;
            }
        }

        getMessage(this.getIntent());
    }
    private String[] permissionsArray=new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_SETTINGS,
            Manifest.permission.MANAGE_DOCUMENTS
    };
    private List<String> permissionList=new ArrayList<String>();
    //申请权限后的返回码
    public final int REQUEST_CODE_ASK_PERMISSIONS=1;
    private int number=0;
    private void checkRequiredPermission(Activity activity){
        for (String permission: permissionsArray) {
            if(ContextCompat.checkSelfPermission(activity,permission)!= PackageManager.PERMISSION_GRANTED){
                permissionList.add(permission);
            }
        }
        if (permissionList.size()>0) {
            number = permissionList.size();
            ActivityCompat.requestPermissions(activity, permissionList.toArray(new String[permissionList.size()]), REQUEST_CODE_ASK_PERMISSIONS);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults){
        switch (requestCode){
            case REQUEST_CODE_ASK_PERMISSIONS:

                break;
            default:
                super.onRequestPermissionsResult(requestCode,permissions,grantResults);

        }
    }

    public static  String  message;
    private void getMessage(Intent intent) {
        //获得的视频的动作
        Bundle actionBuddle=intent.getExtras();
        Log.i("ccc","actionBuddle"+actionBuddle);
        if(actionBuddle!=null) {
            //music_setting/music_control
            type = actionBuddle.getString("cmd");
            if (type==null){
                return;
            }
            //携带数据到fragment
            if (type.equals("video_setting")||type.equals("video_control")) {
                Log.i("ccc","displayTab"+displayTab);
                if(displayTab!=videoPlayTab){
                    videoPlayFragment = null;
                    videoPlayFragment = new VideoPlayFragment();
                    videoPlayFragment.setData(dataList, currentPosition);
                    videoPlayFragment.setArguments(actionBuddle);
                    Log.i("ccc", "actionBuddle1" + actionBuddle);
                    FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                    transaction.replace(R.id.fragment_container, videoPlayFragment);
                    transaction.commit();
                    displayTab = videoPlayTab;

                }else {
                    VideoPlayFragment videoplayFragment = (VideoPlayFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_container);
                    message = actionBuddle.getString(type);
                    Log.i("ccc", "message"+message);
                    videoplayFragment.pauseOrPlay();
                }


            }
        }

    }


    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }
    private ArrayList<FileInfo> dataList;
    @Override
    protected void onResume() {
        super.onResume();
        try {
            dataList = VideoUtils.getDataOrderByTime(this,MediaActivity.current_source_path);
        }catch (Exception e){
            Log.i("ccc","---MediaActivity---"+e);
        }
        IntentActionPlay();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (usb_out!=null)
                unregisterReceiver(usb_out);

        }catch (Exception e){
            Log.i("media",""+e);
        }

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        //根据intent的类型来进行选择
        fragmentTransaction = fragmentManager.beginTransaction();
        startFragment = intent.getStringExtra("startFragment");
        if (startFragment != null){
            current_source_name = intent.getStringExtra("name");
            current_source_path = intent.getStringExtra("path");
            if (!name_path.containsKey(current_source_name)) {
                name_path.put(current_source_name, current_source_path);
            }
           if (startFragment.equals("video")){
                if (displayTab == videoTab){
                }else{
                    fragmentTransaction.replace(R.id.fragment_container,videoFragment);
                    fragmentTransaction.commit();
                    displayTab = videoTab;

                }
            }
        }else {
            getMessage(intent);
        }
    }



    @Override
    public void onVideoFragmentInteraction(String action, ArrayList<FileInfo> arrayList, int i) {
        if (action.equals("playVideo")){
            if (arrayList.equals(videoArraryList) && currentPosition == i){
                fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.show(videoPlayFragment).hide(videoFragment);
                fragmentTransaction.commit();
            }else {
                videoArraryList = arrayList;
                currentPosition = i;
                videoPlayFragment.setData(arrayList,i);
                fragmentTransaction = fragmentManager.beginTransaction();
                if (fragmentManager.findFragmentByTag("videoPlayFragment") == null) {
                    fragmentTransaction.add(R.id.fragment_container, videoPlayFragment, "videoPlayFragment");
                    fragmentTransaction.show(videoPlayFragment).hide(videoFragment);
                    fragmentTransaction.commit();
                }else {
                    fragmentTransaction.show(videoPlayFragment).hide(videoFragment);
                    fragmentTransaction.commit();
                    videoPlayFragment.startPlaying();
                }
            }
            displayTab = videoPlayTab;
            Log.e("ccc", "videoPlayTab生效了!");

        }
    }

    @Override
    public void onVideoPlayFragmentInteraction(String action) {
        if (action.equals("VideoList")){
            fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.show(videoFragment).hide(videoPlayFragment);
            fragmentTransaction.commit();
            displayTab = videoTab;
        }
    }




    private void IntentActionPlay() {
    	if(getIntent() != null && getIntent().getData() != null) {
            fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container,videoFragment);
            fragmentTransaction.commit();
            displayTab = videoTab;

            FileInfo info = new FileInfo();
            info.isFile = true;
            info.uri = intent.getData();
            info.name = getFileName(this, info.uri);
            ArrayList<FileInfo> arrayList = new ArrayList<FileInfo>();
            arrayList.add(info);
            onVideoFragmentInteraction("playVideo", arrayList, 0);
        }
    }


    private String getFileName(final Context context, final Uri uri) {
        if (null == uri) 
            return null;
        //
        final String scheme = uri.getScheme();
        String data = null;
        if (scheme == null)
            data = uri.getPath();
        else if (ContentResolver.SCHEME_FILE.equals(scheme)) {
            data = uri.getPath();
        } else if (ContentResolver.SCHEME_CONTENT.equals(scheme) ) {
            Cursor cursor = context.getContentResolver().query(uri, new String[] {Images.ImageColumns.DATA }, null, null, null);
            if (null != cursor) {
                if (cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(Images.ImageColumns.DATA);
                    if (index > -1){
                        data = cursor.getString(index);
                    }
                }
                cursor.close();
            }
        }
        //
        if(data == null){
            data = uri.toString();
        }
        String fileName = data.substring(data.lastIndexOf("/") + 1, data.length());
        return fileName;
    }

}
