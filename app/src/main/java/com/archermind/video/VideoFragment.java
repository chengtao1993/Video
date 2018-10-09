package com.archermind.video;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


import com.archermind.video.MediaActivity;
import com.archermind.video.R;
/*import android.os.SystemProperties;*/

import java.util.ArrayList;
import com.archermind.video.FileInfo;

import com.archermind.video.VideoFragment;



public class VideoFragment extends Fragment implements View.OnClickListener{
    private OnFragmentInteractionListener onFragmentInteractionListener;
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private Context mContext;
    private ImageView source_switch;
    private ImageView order_switch;
    private TextView noFile;
    private ListView dataDisplay;
    private FrameLayout videoFragmentView;
    private ArrayList<FileInfo> dataListView;
    private VideoAdapter mVideoAdapter;

    private AlertDialog sourceDialog;
    private AlertDialog orderDialog;


    public VideoFragment() {
        // Required empty public constructor
    }


    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment VideoFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static VideoFragment newInstance(String param1, String param2) {
        VideoFragment fragment = new VideoFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    private String videocontrol;
    private String videosetting;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
            videocontrol = getArguments().getString("video_control");
            videosetting = getArguments().getString("video_setting");
            Log.i("ccc","videocontrol"+videocontrol);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        videoFragmentView = (FrameLayout) inflater.inflate(R.layout.fragment_video, null, false);
        dataListView = VideoUtils.getDataOrderByTime(getActivity(),MediaActivity.current_source_path);
        dataDisplay = videoFragmentView.findViewById(R.id.dataDisplay);
        mVideoAdapter = new VideoAdapter(mContext,dataListView);
        dataDisplay.setAdapter(mVideoAdapter);
        dataDisplay.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (dataListView.get(i).isFile){
                    onFragmentInteractionListener.onVideoFragmentInteraction("playVideo",dataListView,i);
                }else {
                    dataListView = VideoUtils.scanLocalFile(dataListView.get(i).path);
                    if (dataListView.size() == 0){
                        Toast.makeText(mContext,"aaaaa",Toast.LENGTH_SHORT).show();
                        noFile = videoFragmentView.findViewById(R.id.noFile);
                        noFile.setVisibility(View.VISIBLE);
                    }else {
                        mVideoAdapter.setData(dataListView);
                        mVideoAdapter.notifyDataSetChanged();
                    }
                }
            }
        });

        return videoFragmentView;
    }



    @Override
    public void onResume() {
        super.onResume();
        //获取参数
        /*SystemProperties.set("service.gr.show","2");*/
        if("prev".equals(videocontrol)){
            //上一首
            Log.i("ccc","prve");
        }else if("next".equals(videocontrol)){
            //下一首
            Log.i("ccc","next");
        }else if("play".equals(videosetting)){
            Log.i("ccc","play");
            //播放
        }else if("pause".equals(videosetting)){
            //暂停
            Log.i("ccc","pause");
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
        onFragmentInteractionListener = (OnFragmentInteractionListener) context;

    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onClick(View v) {


    }

    private int sourceDialogId = 1;
    private Handler sourceDialogAutoHide = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == sourceDialogId) {
                if (sourceDialog != null && sourceDialog.isShowing()) {
                    sourceDialog.dismiss();
                }
            }
        }
    };

    public interface OnFragmentInteractionListener {
        void onVideoFragmentInteraction(String action,ArrayList<FileInfo> arrayList,int i);
    }

}
