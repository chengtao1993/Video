package com.archermind.video;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;
import java.util.ArrayList;





public class VideoPlayFragment extends Fragment implements View.OnClickListener{
    private OnFragmentInteractionListener mOnFragmentInteractionListener;
    private ArrayList<FileInfo> data;
    private int localFileId;
    private FileInfo mFileInfo;
    private FrameLayout videoPlayLayout;
    private VideoView mVideoView;
    private TextView time_current;
    private TextView time_total;
    private TextView dataSource;
    private ImageView videoplaystate;
    private boolean isPlayingComplete = false;
    private ImageView rewind;
    private ImageView last;
    private ImageView next;
    private ImageView fastforward;
    private ImageView close_video;
    private LinearLayout toastLayout;
    private ImageView toastIcon;
    private TextView toastTime;
    private SeekBar seekBar;
    private LinearLayout video_container;
    private Toast toast;
    private int num_click_rewind = 0;
    private int num_click_fastforward = 0;
    private int time_jump = 0;
    private Message message;
    private final int orientation_rewind = 1;
    private final int orientation_fastforward = 2;
    private int seekBarRatio;
    private LinearLayout volum_layout;
    private LinearLayout intensity_layout;
    private AlertDialog dialog;
    private Window dialogWindow;
    private WindowManager.LayoutParams lp;
    private AudioManager audioManager;
    private VerticalSeekBar verticalSeekBar;
    private int num_intensity;
    private int num_volume;
    private Window window;
    private WindowManager.LayoutParams lp_activity;
    private int base_distance = 400;
    private int currentTime;


    public VideoPlayController mVideoPlayController;

    private Handler displayCurrentTime = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            time_current.setText(mVideoPlayController.getCurrentTime());
            seekBar.setProgress(mVideoPlayController.getCurrentSeekBarRatio());
            if (!isPlayingComplete) {
                message = new Message();
                message.what = 1;
                sendMessageDelayed(message, 1000);
            }
        }
    };

    private Handler fastJump = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.arg1 == orientation_fastforward){
                seekBarRatio = mVideoPlayController.jump(orientation_fastforward,time_jump);
                message = new Message();
                message.what = 2;
                message.arg1 = orientation_fastforward;
                sendMessageDelayed(message,1000);
            }else if (msg.arg1 == orientation_rewind){
                seekBarRatio = mVideoPlayController.jump(orientation_rewind,time_jump);
                if (seekBarRatio != 0) {
                    message = new Message();
                    message.what = 2;
                    message.arg1 = orientation_rewind;
                    sendMessageDelayed(message, 1000);
                }else {
                    num_click_rewind = 0;
                }
            }

        }
    };

    @Override
    public void onResume() {
        super.onResume();
        //对获得的参数进行修订
       /* SystemProperties.set("service.gr.show","2");*/
//        if("prev".equals(videocontrol)){
//            //上一首
//            playlast();
//            Log.i("ccc","...prve");
//        }else if("next".equals(videocontrol)){
//            //下一首
//            playNext();
//            Log.i("ccc","...next");
//        }else if("play".equals(videosetting)){
//            Log.i("ccc","...play");
//            //播放
//            SystemProperties.set("service.gr.play","1");
//        }else if("pause".equals(videosetting)){
//            //暂停
//
//            SystemProperties.set("service.gr.play","0");
//            Log.i("ccc","...pause");
//        }

    }

    private GestureDetector gestureDetector;
    private GestureDetector.OnGestureListener onGestureListener = new GestureDetector.OnGestureListener() {
        @Override
        public boolean onDown(MotionEvent e) {
            float x = e.getX();
            dialog = new AlertDialog.Builder(getContext()).create();
            dialogWindow = dialog.getWindow();
            lp = dialogWindow.getAttributes();
            if (x < 540){
                audioManager = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
                num_volume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                volum_layout = (LinearLayout) LayoutInflater.from(getContext()).inflate(R.layout.volum_layout,null);
                verticalSeekBar = volum_layout.findViewById(R.id.volume_seekBar);
                verticalSeekBar.setProgress(100*num_volume/15);
                lp.gravity = Gravity.START | Gravity.TOP;
                lp.x = 30;
                lp.y = 380;
                dialog.setView(volum_layout);
            }else {
                num_intensity = getBrightness(getActivity());
                intensity_layout = (LinearLayout) LayoutInflater.from(getContext()).inflate(R.layout.intensity_layout,null);
                verticalSeekBar = intensity_layout.findViewById(R.id.intensity_seekBar);
                verticalSeekBar.setProgress(num_intensity);
                lp.gravity = Gravity.END | Gravity.TOP;
                lp.x = -30;
                lp.y = 380;
                dialog.setView(intensity_layout);
            }
            currentTime = mVideoView.getCurrentPosition();
            return true;
        }


        @Override
        public void onShowPress(MotionEvent e) {

        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            return false;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            float x_first = e1.getX();
            float x_last = e2.getX();
            float y_first = e1.getY();
            float y_last = e2.getY();
            if (Math.abs(distanceX) > 5 && Math.abs(distanceY) < 5){
                //快进、快退
                int time;
                if (x_last > x_first){
                    time = Math.round(60*1000*(x_last - x_first)/base_distance);
                    mVideoView.seekTo(currentTime + time);
                }else {
                    time = Math.round(60*1000*(x_first - x_last)/base_distance);
                    mVideoView.seekTo(currentTime - time);
                }


            }else if (Math.abs(distanceY) > 5 && Math.abs(distanceX) < 5){
                if (x_first < 540){
                    //音量调节
                    if (!dialog.isShowing()){
                        dialog.show();
                    }
                    int num;
                    if (y_last > y_first){
                        num = 100*num_volume/15 - Math.round(100*(y_last - y_first)/base_distance);
                        if (num < 0){
                            num = 0;
                        }
                    }else {
                        num = 100*num_volume/15 + Math.round(100*(y_first - y_last)/base_distance);
                        if (num > 100){
                            num = 100;
                        }
                    }
                    verticalSeekBar.setProgress(num);
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,num*15/100,AudioManager.FLAG_PLAY_SOUND);

                }else {
                    //亮度调节
                    stopAutoBrightness(getActivity());
                    int num;
                    if (!dialog.isShowing()){
                        dialog.show();
                    }
                    if (y_last > y_first){
                        num = num_intensity - Math.round(100*(y_last - y_first)/base_distance);
                        if (num < 0){
                            num = 0;
                        }
                    }else {
                        num = num_intensity + Math.round(100*(y_first - y_last)/base_distance);
                        if (num > 100){
                            num = 100;
                        }
                    }
                    verticalSeekBar.setProgress(num);
                    window = getActivity().getWindow();
                    lp_activity = window.getAttributes();
                    lp_activity.screenBrightness = num;
                    window.setAttributes(lp_activity);
                    saveBrightness(getActivity(),num);
                }
            }
            return true;
        }

        @Override
        public void onLongPress(MotionEvent e) {

        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {

            return false;
        }
    };

    public void setData(ArrayList<FileInfo> arrayList,int localFileId){
        data = arrayList;
        this.localFileId = localFileId;
        mFileInfo = arrayList.get(localFileId);
        Log.i("ccc","......"+data+"----"+localFileId+"****"+mFileInfo);

    }

    public void startPlaying(){
        mVideoPlayController.stop();
        mVideoPlayController.setVideoPath(mFileInfo.path);
        mVideoPlayController.start();
        dataSource.setText(mFileInfo.name);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mOnFragmentInteractionListener = (OnFragmentInteractionListener) context;
    }

    private String videocontrol;
    private String videosetting;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            videocontrol = getArguments().getString("video_control");
            videosetting = getArguments().getString("video_setting");
            Log.i("ccc","videoplaycontrol"+videocontrol);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        videoPlayLayout = (FrameLayout) inflater.inflate(R.layout.video_play_layout,null);
        volum_layout = (LinearLayout) inflater.inflate(R.layout.volum_layout,null);
        intensity_layout = (LinearLayout) inflater.inflate(R.layout.intensity_layout,null);
        seekBar = videoPlayLayout.findViewById(R.id.seek);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mVideoView.seekTo(progress * mVideoView.getDuration() / 100);
                    time_current.setText(mVideoPlayController.getCurrentTime());
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        toastLayout = (LinearLayout) inflater.inflate(R.layout.toast_layout,null);
        toastIcon = toastLayout.findViewById(R.id.toastIcon);
        toastTime = toastLayout.findViewById(R.id.toastTime);

        mVideoView = videoPlayLayout.findViewById(R.id.videoView);
        mVideoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                MediaActivity.currentPosition = -1;
                return true;
            }
        });
        mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                time_total.setText(mVideoPlayController.getTotalTime());
                isPlayingComplete = false;
                videoplaystate.setSelected(true);
                message = new Message();
                message.what = 1;
                displayCurrentTime.sendMessage(message);

            }
        });
        mVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                videoplaystate.setSelected(false);
                isPlayingComplete = true;
               /* if (MediaActivity.mCarHUDManager != null) {
                    MediaActivity.mCarHUDManager.SendHudMoudlesValue(Car.HUD_VIDEO_PLAYING_STATUS, "0");
                }
                SystemProperties.set("service.gr.play","0");*/
            }
        });

        // CH <BugId:2419> <lizhi> <20180324> modify begin
        mVideoPlayController = new VideoPlayController(getActivity(), mVideoView);
        // CH <BugId:2419> <lizhi> <20180324> modify end
        //modify by yanglin for fileinfo == null begin
        if(mFileInfo != null){
            if (mFileInfo.uri == null){
                mVideoPlayController.setVideoPath(mFileInfo.path);
            }else {
                mVideoPlayController.setVideoUri(mFileInfo.uri);
            }
            mVideoPlayController.start();
            /*if(MediaActivity.mCarHUDManager != null) {
                MediaActivity.mCarHUDManager.SendHudMoudlesValue(Car.HUD_VIDEO_PLAYING_STATUS, "1");
            }*/
            dataSource = videoPlayLayout.findViewById(R.id.data_source);
            dataSource.setText(mFileInfo.name);
            if(localFileId > 0 && localFileId < data.size()-1) {
               /* if (MediaActivity.mCarHUDManager != null) {
                    MediaActivity.mCarHUDManager.SendHudMoudlesValue(Car.HUD_VIDEO_NAME, mFileInfo.name);
                    MediaActivity.mCarHUDManager.SendHudMoudlesValue(Car.HUD_VIDEO_PRE_NAME, data.get(localFileId - 1).name);
                    MediaActivity.mCarHUDManager.SendHudMoudlesValue(Car.HUD_VIDEO_PRE_NAME, data.get(localFileId + 1).name);
                }*/
            }else if(localFileId == 0){
                /*if(MediaActivity.mCarHUDManager != null){
                    MediaActivity.mCarHUDManager.SendHudMoudlesValue(Car.HUD_VIDEO_NAME, mFileInfo.name);*/
                    //add by yanglin fixed for size = 1 begin
                    if(data.size() > 1){
                       /* if (MediaActivity.mCarHUDManager != null) {
                            MediaActivity.mCarHUDManager.SendHudMoudlesValue(Car.HUD_VIDEO_PRE_NAME, data.get(localFileId+1).name);
                        }*/
                    }
                    //add by yanglin fixed for size = 1 end
               /* }*/
            }else if(localFileId == data.size()-1){
                /*if (MediaActivity.mCarHUDManager != null) {
                    MediaActivity.mCarHUDManager.SendHudMoudlesValue(Car.HUD_VIDEO_NAME, mFileInfo.name);
                    MediaActivity.mCarHUDManager.SendHudMoudlesValue(Car.HUD_VIDEO_PRE_NAME, data.get(localFileId - 1).name);
                }*/
            }
        }
        //modify by yanglin for fileinfo == null end
        time_current = videoPlayLayout.findViewById(R.id.time_current);
        time_total = videoPlayLayout.findViewById(R.id.time_total);

        videoplaystate = videoPlayLayout.findViewById(R.id.videoplaystate);
        videoplaystate.setSelected(true);
        videoplaystate.setOnClickListener(this);

        rewind = videoPlayLayout.findViewById(R.id.rewind);
        rewind.setOnClickListener(this);

        last = videoPlayLayout.findViewById(R.id.last);
        last.setOnClickListener(this);

        next = videoPlayLayout.findViewById(R.id.next);
        next.setOnClickListener(this);

        fastforward = videoPlayLayout.findViewById(R.id.fastforward);
        fastforward.setOnClickListener(this);

        close_video = videoPlayLayout.findViewById(R.id.close_video);
        close_video.setOnClickListener(this);

        gestureDetector = new GestureDetector(getContext(),onGestureListener);

        video_container = videoPlayLayout.findViewById(R.id.video_container);
        video_container.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return gestureDetector.onTouchEvent(event);
            }
        });

        message = new Message();
        message.what = 1;
        displayCurrentTime.sendMessage(message);

        return videoPlayLayout;
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id){
            case R.id.videoplaystate:
                Log.i("ccc","点击了");
               playOrpause(view);
                break;
            case R.id.rewind:
                if(videoplaystate.isSelected()) {
                    num_click_rewind += 1;
                    if (num_click_rewind == 1) {
                        num_click_fastforward = 0;
                        time_jump = 10 * 1000;
                        toastIcon.setImageResource(R.drawable.rewind);
                        toastTime.setText("10s");
                        toast = new Toast(getContext());
                        toast.setGravity(Gravity.TOP, 0, 560);
                        toast.setView(toastLayout);
                        toast.show();
                        fastJump.removeMessages(2);
                        num_click_fastforward = 0;
                        message = new Message();
                        message.what = 2;
                        message.arg1 = orientation_rewind;
                        fastJump.sendMessage(message);
                    } else if (num_click_rewind == 2) {
                        time_jump = 20 * 1000;
                        toastTime.setText("20s");
                        toast = new Toast(getContext());
                        toast.setGravity(Gravity.TOP, 0, 560);
                        toast.setView(toastLayout);
                        toast.show();
                    } else if (num_click_rewind == 3) {
                        time_jump = 30 * 1000;
                        toastTime.setText("30s");
                        toast = new Toast(getContext());
                        toast.setGravity(Gravity.TOP, 0, 560);
                        toast.setView(toastLayout);
                        toast.show();
                    } else {
                        num_click_rewind = 0;
                        time_jump = 0;
                        fastJump.removeMessages(2);
                    }
                }
                break;
            case R.id.last:
                playlast();
                break;
            case R.id.next:
                playNext();
                break;
            case R.id.fastforward:
                if (videoplaystate.isSelected()) {
                    num_click_fastforward += 1;
                    if (num_click_fastforward == 1) {
                        num_click_rewind = 0;
                        time_jump = 10 * 1000;
                        toastIcon.setImageResource(R.drawable.fastforward);
                        toastTime.setText("10s");
                        toast = new Toast(getContext());
                        toast.setGravity(Gravity.TOP, 0, 560);
                        toast.setView(toastLayout);
                        toast.show();
                        fastJump.removeMessages(2);
                        num_click_rewind = 0;
                        message = new Message();
                        message.what = 2;
                        message.arg1 = orientation_fastforward;
                        fastJump.sendMessage(message);
                    } else if (num_click_fastforward == 2) {
                        time_jump = 20 * 1000;
                        toastTime.setText("20s");
                        toast = new Toast(getContext());
                        toast.setGravity(Gravity.TOP, 0, 560);
                        toast.setView(toastLayout);
                        toast.show();
                    } else if (num_click_fastforward == 3) {
                        time_jump = 30 * 1000;
                        toastTime.setText("30s");
                        toast = new Toast(getContext());
                        toast.setGravity(Gravity.TOP, 0, 560);
                        toast.setView(toastLayout);
                        toast.show();
                    } else {
                        num_click_fastforward = 0;
                        time_jump = 0;
                        fastJump.removeMessages(2);
                    }
                }
                break;
            case R.id.close_video:
                /*if(MediaActivity.mCarHUDManager != null) {
                    MediaActivity.mCarHUDManager.SendHudMoudlesValue(Car.HUD_VIDEO_CLOSE);
                }*/
                fastJump.removeMessages(2);
                num_click_fastforward = 0;
                num_click_rewind = 0;
                mVideoPlayController.pause();
                videoplaystate.setSelected(false);
                displayCurrentTime.removeMessages(1);
                mOnFragmentInteractionListener.onVideoPlayFragmentInteraction("VideoList");
                break;
        }
    }

    private void playOrpause(View view) {
        fastJump.removeMessages(2);
        num_click_fastforward = 0;
        num_click_rewind = 0;
        if (view.isSelected()){
            view.setSelected(false);
            mVideoPlayController.pause();
           /* if (MediaActivity.mCarHUDManager != null) {
                MediaActivity.mCarHUDManager.SendHudMoudlesValue(Car.HUD_VIDEO_PLAYING_STATUS, "0");
            }*/
           // MediaActivity.mCarHUDManager.SendHudMoudlesValue(Car.HUD_VIDEO_CLOSE);
            displayCurrentTime.removeMessages(1);
        }else {
            view.setSelected(true);
            mVideoPlayController.resume();
            /*if(MediaActivity.mCarHUDManager != null) {
                MediaActivity.mCarHUDManager.SendHudMoudlesValue(Car.HUD_VIDEO_PLAYING_STATUS, "1");
            }*/
            isPlayingComplete = false;
            message = new Message();
            message.what = 1;
            displayCurrentTime.sendMessage(message);

        }
    }

    public void pauseOrPlay(){
        if("pause".equals(MediaActivity.message)){
            if (videoplaystate.isSelected()){
                Log.i("ccc","pause");
                videoplaystate.setSelected(false);
                mVideoPlayController.pause();
               /* if (MediaActivity.mCarHUDManager != null) {
                    MediaActivity.mCarHUDManager.SendHudMoudlesValue(Car.HUD_VIDEO_PLAYING_STATUS, "0");
                }*/
                //MediaActivity.mCarHUDManager.SendHudMoudlesValue(Car.HUD_VIDEO_CLOSE);
            }
        }else if("play".equals(MediaActivity.message)){
            if(!videoplaystate.isSelected()) {
                videoplaystate.setSelected(true);
                mVideoPlayController.resume();
                /*if(MediaActivity.mCarHUDManager != null) {
                    MediaActivity.mCarHUDManager.SendHudMoudlesValue(Car.HUD_VIDEO_PLAYING_STATUS, "1");
                }*/
                isPlayingComplete = false;
                Log.i("ccc","play");
            }
        }else if("prev".equals(MediaActivity.message)){
            playlast();
            Log.i("ccc","prev");
        }else if ("next".equals(MediaActivity.message)){
            playNext();
            Log.i("ccc","next");
        }

    }

    private void playlast(){
     fastJump.removeMessages(2);
     num_click_fastforward = 0;
     num_click_rewind = 0;
     for (int i = localFileId - 1; i <= data.size();i--){
         if (i == -1){
             i = data.size();
             continue;
         }
         if(data.get(i).isFile){
             mFileInfo = data.get(i);
             localFileId = i;
             MediaActivity.currentPosition=i;
             mVideoPlayController.stop();
             if (mFileInfo.uri == null){
                 mVideoPlayController.setVideoPath(mFileInfo.path);
             }else {
                 mVideoPlayController.setVideoUri(mFileInfo.uri);
             }
             if(localFileId>0&&localFileId<data.size()-1) {
                /* if(MediaActivity.mCarHUDManager != null) {
                     MediaActivity.mCarHUDManager.SendHudMoudlesValue(Car.HUD_VIDEO_NAME, mFileInfo.name);
                     MediaActivity.mCarHUDManager.SendHudMoudlesValue(Car.HUD_VIDEO_PRE_NAME, data.get(localFileId - 1).name);
                     MediaActivity.mCarHUDManager.SendHudMoudlesValue(Car.HUD_VIDEO_PRE_NAME, data.get(localFileId + 1).name);
                 }*/
             }else if(localFileId==0){
                 /*if(MediaActivity.mCarHUDManager != null) {
                     MediaActivity.mCarHUDManager.SendHudMoudlesValue(Car.HUD_VIDEO_NAME, mFileInfo.name);
                     MediaActivity.mCarHUDManager.SendHudMoudlesValue(Car.HUD_VIDEO_PRE_NAME, data.get(localFileId + 1).name);
                 }*/
             }else if(localFileId==data.size()-1){
                 /*if(MediaActivity.mCarHUDManager != null) {
                     MediaActivity.mCarHUDManager.SendHudMoudlesValue(Car.HUD_VIDEO_NAME, mFileInfo.name);
                     MediaActivity.mCarHUDManager.SendHudMoudlesValue(Car.HUD_VIDEO_PRE_NAME, data.get(localFileId - 1).name);
                 }*/
             }
             mVideoPlayController.start();
            /* if(MediaActivity.mCarHUDManager != null) {
                 MediaActivity.mCarHUDManager.SendHudMoudlesValue(Car.HUD_VIDEO_PLAYING_STATUS, "1");
             }*/
             dataSource.setText(mFileInfo.name);

             break;
         }

     }
 }
 private void playNext(){
     fastJump.removeMessages(2);
     num_click_fastforward = 0;
     num_click_rewind = 0;
     for (int i = localFileId + 1; i <= data.size();i++){
         if (i == data.size()){
             i = -1;
             continue;
         }
         if(data.get(i).isFile){
             mFileInfo = data.get(i);
             localFileId = i;
             MediaActivity.currentPosition=i;
             mVideoPlayController.stop();
             if (mFileInfo.uri == null){
                 mVideoPlayController.setVideoPath(mFileInfo.path);
             }else {
                 mVideoPlayController.setVideoUri(mFileInfo.uri);
             }
             if(localFileId>0&&localFileId<data.size()-1) {
                 /*if(MediaActivity.mCarHUDManager != null) {
                     MediaActivity.mCarHUDManager.SendHudMoudlesValue(Car.HUD_VIDEO_NAME, mFileInfo.name);
                     MediaActivity.mCarHUDManager.SendHudMoudlesValue(Car.HUD_VIDEO_PRE_NAME, data.get(localFileId - 1).name);
                     MediaActivity.mCarHUDManager.SendHudMoudlesValue(Car.HUD_VIDEO_PRE_NAME, data.get(localFileId + 1).name);
                 }*/
             }else if(localFileId==0){
                 /*if(MediaActivity.mCarHUDManager != null) {
                     MediaActivity.mCarHUDManager.SendHudMoudlesValue(Car.HUD_VIDEO_NAME, mFileInfo.name);
                     MediaActivity.mCarHUDManager.SendHudMoudlesValue(Car.HUD_VIDEO_PRE_NAME, data.get(localFileId + 1).name);
                 }*/
             }else if(localFileId==data.size()-1){
                 /*if(MediaActivity.mCarHUDManager != null) {
                     MediaActivity.mCarHUDManager.SendHudMoudlesValue(Car.HUD_VIDEO_NAME, mFileInfo.name);
                     MediaActivity.mCarHUDManager.SendHudMoudlesValue(Car.HUD_VIDEO_PRE_NAME, data.get(localFileId - 1).name);
                 }*/
             }
             mVideoPlayController.start();
             /*if (MediaActivity.mCarHUDManager != null) {
                 MediaActivity.mCarHUDManager.SendHudMoudlesValue(Car.HUD_VIDEO_PLAYING_STATUS, "1");
             }*/
             dataSource.setText(mFileInfo.name);
             break;
         }

     }

 }



    public static int getBrightness(Activity activity) {
        int brightValue = 0;
        ContentResolver contentResolver = activity.getContentResolver();
        try {
            brightValue = Settings.System.getInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        return brightValue;
    }

    public static void stopAutoBrightness(Context context) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.System.canWrite(context)) {
                Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_WRITE_SETTINGS);
                intent.setData(Uri.parse("package:" + context.getPackageName()));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            } else {
                //有了权限，具体的动作
                Settings.System.putInt(context.getContentResolver(),
                        Settings.System.SCREEN_BRIGHTNESS_MODE,
                        Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
            }
        }


    }

    public static void saveBrightness(Context context, int brightness) {

        Uri uri = android.provider.Settings.System
                .getUriFor(Settings.System.SCREEN_BRIGHTNESS);

        android.provider.Settings.System.putInt(context.getContentResolver(),
                Settings.System.SCREEN_BRIGHTNESS, brightness);

        context.getContentResolver().notifyChange(uri, null);
    }




    public interface OnFragmentInteractionListener {
        void onVideoPlayFragmentInteraction(String action);
    }

    @Override
    public void onPause() {
        super.onPause();
       /* SystemProperties.set("service.gr.play","0");*/
    }
}
