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

    private TextView data_source;
    private TextView orderBy;
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
        source_switch = videoFragmentView.findViewById(R.id.source_switch);
        source_switch.setOnClickListener(this);
        order_switch = videoFragmentView.findViewById(R.id.order_switch);
        order_switch.setOnClickListener(this);
        orderBy = videoFragmentView.findViewById(R.id.orderBy);
        data_source = videoFragmentView.findViewById(R.id.data_source);
        data_source.setOnClickListener(this);
        data_source.setText(MediaActivity.current_source_name);
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

   public void resourceChanged(){
       if (orderBy.getText().equals("按创建时间排序")){
           data_source.setText(MediaActivity.current_source_name);
           dataListView = VideoUtils.getDataOrderByTime(getActivity(),MediaActivity.current_source_path);
       }else if (orderBy.getText().equals("按文件名称排序")){
           data_source.setText(MediaActivity.current_source_name);
           dataListView = VideoUtils.getDataOrderByName(getActivity(),MediaActivity.current_source_path);
       }
       mVideoAdapter.setData(dataListView);
       mVideoAdapter.notifyDataSetChanged();

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
        int id = v.getId();
        switch (id){
            case R.id.order_switch:
                orderDialog = new AlertDialog.Builder(getActivity()).create();
                LinearLayout order_layout = (LinearLayout) LayoutInflater.from(getActivity()).inflate(R.layout.order_layout,null);
                TextView orderByName = order_layout.findViewById(R.id.orderByName);
                orderByName.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dataListView = VideoUtils.getDataOrderByName(getActivity(),MediaActivity.current_source_path);
                        mVideoAdapter.setData(dataListView);
                        mVideoAdapter.notifyDataSetChanged();
                        orderBy.setText("按文件名称排序");
                        orderDialog.dismiss();
                    }
                });
                TextView orderByTime = order_layout.findViewById(R.id.orderByTime);
                orderByTime.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dataListView = VideoUtils.getDataOrderByTime(getActivity(),MediaActivity.current_source_path);
                        mVideoAdapter.setData(dataListView);
                        mVideoAdapter.notifyDataSetChanged();
                        orderBy.setText("按创建时间排序");
                        orderDialog.dismiss();
                    }
                });
                orderDialog.setView(order_layout);
                orderDialog.show();
                Window dialogWindow = orderDialog.getWindow();
                WindowManager.LayoutParams lp = dialogWindow.getAttributes();
                lp.height = 400;
                lp.width = 942;
                lp.gravity = Gravity.TOP|Gravity.START;
                lp.x = 68;
                lp.y = 340;
                dialogWindow.setAttributes(lp);
                break;
            case R.id.source_switch:
            case R.id.data_source:
                sourceDialog = new AlertDialog.Builder(getActivity()).create();
                LinearLayout source_layout = (LinearLayout) LayoutInflater.from(getActivity()).inflate(R.layout.source_layout,null);
                for (String key : MediaActivity.name_path.keySet()) {
                    LinearLayout item = (LinearLayout) LayoutInflater.from(getActivity()).inflate(R.layout.source_item_layout, null);
                    TextView item_title;
                    item_title = item.findViewById(R.id.source_title);
                    item_title.setText(key);
                    if (key.equals("本地")) {
                        Drawable left = getActivity().getDrawable(R.drawable.folder);
                        item_title.setCompoundDrawablesWithIntrinsicBounds(left, null, null, null);
                    } else {
                        Drawable left = getActivity().getDrawable(R.drawable.usb);
                        item_title.setCompoundDrawablesWithIntrinsicBounds(left, null, null, null);
                    }
                    item_title.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (data_source.getText().equals(((TextView) v).getText())) {

                            } else {
                                MediaActivity.current_source_name = (String) ((TextView) v).getText();
                                MediaActivity.current_source_path = (String) MediaActivity.name_path.get(MediaActivity.current_source_name);
                                if (orderBy.getText().equals("按创建时间排序")) {
                                    data_source.setText(MediaActivity.current_source_name);
                                    dataListView = VideoUtils.getDataOrderByTime(getActivity(), MediaActivity.current_source_path);
                                } else if (orderBy.getText().equals("按文件名称排序")) {
                                    data_source.setText(MediaActivity.current_source_name);
                                    dataListView = VideoUtils.getDataOrderByName(getActivity(), MediaActivity.current_source_path);
                                }
                                mVideoAdapter.setData(dataListView);
                                mVideoAdapter.notifyDataSetChanged();
                            }
                            sourceDialog.dismiss();
                        }
                    });
                    source_layout.addView(item);
                }
                sourceDialog.setView(source_layout);
                sourceDialog.show();
                sourceDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        sourceDialogAutoHide.removeMessages(sourceDialogId);
                    }
                });
                Window dialogWindow1 = sourceDialog.getWindow();
                WindowManager.LayoutParams lp1 = dialogWindow1.getAttributes();
                lp1.height = 400;
                lp1.width = 942;
                lp1.gravity = Gravity.TOP | Gravity.START;
                lp1.x = 68;
                lp1.y = 126;
                dialogWindow1.setAttributes(lp1);
                Message message = new Message();
                message.what = sourceDialogId;
                sourceDialogAutoHide.sendMessageDelayed(message, 8000);
                break;
        }
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
