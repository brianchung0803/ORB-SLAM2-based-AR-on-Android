package com.example.ys.orbtest;

import static org.opencv.core.CvType.CV_32F;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONException;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvException;
import org.opencv.core.Mat;

import org.json.JSONObject;
import org.json.JSONArray;
import java.net.URI;
import java.net.URISyntaxException;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.client.WebSocketClient;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class OrbTest extends Activity implements CameraBridgeViewBase.CvCameraViewListener2 {

    private static final String TAG = "OrbTest::Activity@@JAVA";
    private GLSurfaceView glSurfaceView;
    private CameraBridgeViewBase mOpenCvCameraView;
    private SeekBar seek;
    private TextView myTextView;
    public static double SCALE = 1;
    private static long count = 0;
    String host = null;
    private float[] pose_;
    private boolean keyframe_=false;
    private boolean relocalize_=false;
    private Socket socket=null;
    private Integer frame_no = 0;
    private boolean is_subscribe_bbox = false;
    private boolean is_subscribe_pose = false;
    public boolean voc_done = false;
    ///
    private WebSocketClient myWebSocketClient;
    Config SLAM_config;
    Data rcv_server_data;
    track rcv_server_pose;
    Button VO_place;
    public  boolean send_vo = false;
    //long IRL_id=0;


    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };


    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
//        Log.i(TAG, "called onCreate");
        MatrixState.set_projection_matrix(445f, 445f, 319.5f, 239.500000f, 850, 480, 0.01f, 100f);
        super.onCreate(savedInstanceState);
        //hide the status bar
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //hide the title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_orb_test);

        String filepath = "/storage/emulated/0/SLAM/Calibration/mi6.yaml";
        String txt_path = Environment.getExternalStorageDirectory().getAbsolutePath()+"/Download";
        File config_file = new File(txt_path,"config.txt");
        SLAM_config = new Config(config_file);
        rcv_server_data = new Data();
        rcv_server_pose = new track();

        try {
            readFileOnLine(filepath);
        } catch (Exception e) {
            e.printStackTrace();
        }

        conn();



        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.tutorial1_activity_java_surface_view);
        mOpenCvCameraView.setMaxFrameSize(640, 480);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);

        //opengl图层
        glSurfaceView = (GLSurfaceView) findViewById(R.id.glSurfaceView);
        //OpenGL ES 2.0
        glSurfaceView.setEGLContextClientVersion(2);
        //设置透明背景
        glSurfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        glSurfaceView.getHolder().setFormat(PixelFormat.TRANSLUCENT);
        final MyRender earthRender = new MyRender(this);
        glSurfaceView.setRenderer(earthRender);
        // 设置渲染模式为主动渲染
        glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
        glSurfaceView.setZOrderOnTop(true);
//        glSurfaceView.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                if (event != null) {
//                    // Convert touch coordinates into normalized device
//                    // coordinates, keeping in mind that Android's Y
//                    // coordinates are inverted.
//                    final float normalizedX = ((event.getX() / (float) v.getWidth()) * 2 - 1) * 4f;
//                    final float normalizedY = (-((event.getY() / (float) v.getHeight()) * 2 - 1)) * 1.5f;
//
//                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
//                        glSurfaceView.queueEvent(new Runnable() {
//                            @Override
//                            public void run() {
//                                earthRender.handleTouchPress(
//                                        normalizedX, normalizedY);
//                            }
//                        });
//                    } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
//                        glSurfaceView.queueEvent(new Runnable() {
//                            @Override
//                            public void run() {
//                                earthRender.handleTouchDrag(
//                                        normalizedX, normalizedY);
//                            }
//                        });
//                    } else if (event.getAction() == MotionEvent.ACTION_UP) {
//                        glSurfaceView.queueEvent(new Runnable() {
//                            @Override
//                            public void run() {
//                                earthRender.handleTouchUp(
//                                        normalizedX, normalizedY);
//                            }
//                        });
//                    }
//
//                    return true;
//                } else {
//                    return false;
//                }
//            }
//        });


//        new Thread(new Runnable() {
//
//            @Override
//            public void run() {
//                try {
//                    socket = new Socket(SLAM_config.host_ip, SLAM_config.port);
//                    System.out.println("Keyframe socket, host_ip:" + SLAM_config.host_ip + ", port:" + SLAM_config.port);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }).start();

        VO_place = (Button) findViewById(R.id.placeVo);
        VO_place.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                send_vo = true;
            }
        });
        myTextView = (TextView) findViewById(R.id.myTextView);
        seek = (SeekBar) findViewById(R.id.mySeekBar);
        //初始化
        seek.setProgress(60);
        seek.setOnSeekBarChangeListener(seekListener);
        myTextView.setText("Scale:" + SCALE);
    }

    public void conn()
    {
        ///////////////////////////Web Socket ROS_Bridge
        URI uri;
        try {
            uri = new URI("ws://192.168.50.74:9090/");
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return;
        }

        myWebSocketClient = new WebSocketClient(uri)  {
            @Override
            public void onOpen(ServerHandshake serverHandshake) {
                Log.i("Websocket", "Opened");
                myWebSocketClient.send("Hello from " + Build.MANUFACTURER + " " + Build.MODEL);
            }

            @Override
            public void onMessage(String s) {
                final String message = s;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String[] entries = message.split(":");
                        String data = entries[3].substring(0, entries[3].length() - 6);
                        String topic = entries[3].substring(2, 4);
                        System.out.println("topic" + topic);
                        System.out.println("msg" + message);
                        //Bounding box
//                        if(rcv_server_data.is_processing == false && topic.equals("BB")) {
//                            rcv_server_data.is_processing = true;
//                            rcv_server_data.data = data;
//                            rcv_server_data.parser();
//                        }
                        //Pose value
                        if(rcv_server_pose.is_processing == false && topic.equals("UP")){
                            rcv_server_pose.pose = data;
                            rcv_server_pose.parser();
                        }
                    }
                }).start();
            }

            @Override
            public void onClose(int i, String s, boolean b) {
                Log.i("Websocket", "Closed " + s);
            }

            @Override
            public void onError(Exception e) {
                Log.i("Websocket", "Error " + e.getMessage());
            }
        };

        myWebSocketClient.connect();
        //////////////////////////////////////////////////////////
    }


//    public void send(View view) {
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                Socket socket;
//                try {
//
//                    int post = 30000;
////                    String host = "192.168.0.16";
//                    socket = new Socket(host, post);
//
//                    DataOutputStream out = new DataOutputStream(socket.getOutputStream());
//
////                    String str = editText.getText().toString().trim();
//
//                    sendTextMsg(out, str);
//                    out.close();
//                    socket.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }).start();
//    }

    public void GetResult(boolean keyframe, boolean relocalize, boolean is_voc)
    {
//        pose_=pose;
        keyframe_=keyframe;
        relocalize_=relocalize;
        voc_done = is_voc;
    }
    @Override
    public void onPause() {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();

        glSurfaceView.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
        glSurfaceView.onResume();
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();

    }

    public void onCameraViewStarted(int width, int height) {
    }

    public void onCameraViewStopped() {
    }

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }


    private native float[] CVTest(long matAddr, long id, int[] update_ids, long[] update_poses_address);  //调用 c++代码


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)

    void web_sock_send(final String encoded_img)
    {
        new Thread(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void run() {
                JSONObject obj = new JSONObject();
                JSONObject obj1 = new JSONObject();
                String data_header;
                if(send_vo){
                    data_header = frame_no.toString() + "_T_" + encoded_img;
                }else{
                    data_header = frame_no.toString() + "_F_" + encoded_img;
                }

                try{
                    obj1.put("data", data_header);
                    obj.put("op","publish");
                    obj.put("topic","/chatter");
                    obj.put("msg", obj1);
                }
                catch(JSONException e){
                    Log.i("error sending", "gg");
                }
                String data = "";
                String data1 = "";
                String op1 = "advertise";
                String topic1 = "/chatter";
                String type1 = "std_msgs/String";
                data = "{\"op\": \"" + op1 + "\"";
                data += ",\"topic\":\"" + topic1 + "\"";
                data += ",\"type\":\"" + type1 + "\"}";
                //For handshaking
                myWebSocketClient.send(data);


                //For subscription pose
//                if(!is_subscribe_pose) {
//                    String op = "subscribe";
//                    String id = "002";
//                    String topic = "/pose_update";
//                    String type = "std_msgs/String";
//                    String subs = "";
//                    subs = "{\"op\": \"" + op + "\"";
//                    subs += ",\"id\":\"" + id + "\"";
//                    subs += ",\"topic\":\"" + topic + "\"";
//                    subs += ",\"type\":\"" + type + "\"}";
//                    is_subscribe_pose = true;
//                    myWebSocketClient.send(subs);
//                }

                //For subscription bounding box
                if(!is_subscribe_bbox) {
                    String op = "subscribe";
                    String id = "001";
                    String topic = "/IRL_SLAM";
                    String type = "std_msgs/String";
                    String subs = "";
                    subs = "{\"op\": \"" + op + "\"";
                    subs += ",\"id\":\"" + id + "\"";
                    subs += ",\"topic\":\"" + topic + "\"";
                    subs += ",\"type\":\"" + type + "\"}";
                    is_subscribe_bbox = true;
                    myWebSocketClient.send(subs);
                }

                //Bitmap data
                myWebSocketClient.send(obj.toString());

                frame_no++;
//                try {
////                    int port = 30001;
////                    Log.v("m1y",String.valueOf(byteArray.length));
////                    Log.v("m1y",host);
//                    ////////////////////////////////
//                    socket = new Socket(SLAM_config.host_ip, SLAM_config.port);
//                    System.out.println("Keyframe detected!");
//                    OutputStreamWriter out = new OutputStreamWriter(socket.getOutputStream(), "UTF-8");
//                    out.write(obj.toString());
//                    System.out.println("Keyframe detected and sent!");
//                    socket.shutdownOutput();
//                    ////////////////////////////////

////                    out.writeInt(byteArray.length);
////                    DataOutputStream out = new DataOutputStream(socket.getOutputStream());
////                    out.write(byteArray);
////                    out.close();
//
////                    socket.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                    Log.v("m1y","no connection!");
//                }
            }
        }).start();


    }

    void send_image(Bitmap bitmap)
    {
        if(bitmap==null) return;
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,80,stream);
        final byte[] byteArray = stream.toByteArray();
        //cast to string
        final String encoded_img = Base64.encodeToString(byteArray, Base64.DEFAULT);
        //create json object


        System.out.println("Keyframe bitmap: row: "+bitmap.getHeight()+"column: "+bitmap.getWidth());
        System.out.println("Keyframe size!"+byteArray.length);
        web_sock_send(encoded_img);
//        String txt_path = Environment.getExternalStorageDirectory().getAbsolutePath()+"/Download";
//        File tmp_ = new File(txt_path,"tmp.jpg");
//
//        try (FileOutputStream out = new FileOutputStream(tmp_)) {
//            bitmap.compress(Bitmap.CompressFormat.JPEG,80,out);
//            // PNG is a lossless format, the compression factor (100) is ignored
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        final Socket new_socket= socket;
    }
    /**
     * 处理图像的函数，这个函数在相机刷新每一帧都会调用一次，而且每次的输入参数就是当前相机视图信息
     * * @param inputFrame
     * @return
     */
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {

        Mat original = inputFrame.rgba().clone();
        Mat rgb = inputFrame.rgba();
        Bitmap bitmap = Bitmap.createBitmap(original.width(), original.height(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(original,bitmap);
        //if(voc_done)
        //{
        send_image(bitmap);
        //}

//        if(rcv_server_data.is_processing == false) rcv_server_data.temp_rgb_mat = rgb.clone();
//
//        if(rcv_server_data.rgb_mat == null || rcv_server_data.is_processing == true) {
//            Log.i("current mat value", "value=" + rcv_server_data.rgb_mat);
//            Log.i("current is_process", "value=" + rcv_server_data.is_processing);
//            Log.i("onCameraFrame:", "return rgb");
//            return rgb;
//        }
//        else {
////            try {
//                Log.i("onCameraFrame:", "return rgb_mat" );
//            return rcv_server_data.rgb_mat;
//        }
//            } catch (CvException e) {
//                Log.i("onCameraFrame error:", "error = " + e);
//            }
//        }
        //return  rgb;


//        Log.i("image col",Integer.toString(rgb.cols()));
//        Log.i("image row",Integer.toString(rgb.rows()));
        long startTime = System.currentTimeMillis();
        int[] arr;
        long[] cvArr;

        if(rcv_server_pose.is_processing)
        {
            arr = rcv_server_pose.id;
            cvArr = rcv_server_pose.update_poses;
            rcv_server_pose.is_processing = false;
        }
        else{

            arr= new int[0];
            cvArr = new long[0];
        }


        //float[] poseMatrix = CVTest(rgb.getNativeObjAddr(),frame_no,arr,cvArr); //从slam系统获得相机位姿矩阵
        return  rgb;
//
//        long endTime = System.currentTimeMillis();
//        frame_no++;
//        //System.out.println("pose"+poseMatrix.length);
//        //if(poseMatrix.length==16)
//        //{
//       //     System.out.println("tracking took " + (endTime - startTime) + " milliseconds");
//        //    for(int i =0; i<4;++i)
//        //    {
//       //         for(int j=0; j<4;++j)
//        //        {
//        //            System.out.println("pose "+i+", "+j+": "+poseMatrix[i*4+j]);
//         //       }
//
//         //   }
//
//       // }
//
//        //System.out.println();
//
//        return  rgb;
//
//
//
//        if (poseMatrix.length != 0) {
//            double[][] pose = new double[4][4];
//            System.out.println("one posematrix is below========");
//            for (int i = 0; i < poseMatrix.length / 4; i++) {
//                for (int j = 0; j < 4; j++) {
//
//                    if (j == 3 && i != 3) {
//                        pose[i][j] = poseMatrix[i * 4 + j] * SCALE;
//                    } else {
//                        pose[i][j] = poseMatrix[i * 4 + j];
//                    }
//                    System.out.print(pose[i][j] + "\t ");
//                }
//
//                System.out.print("\n");
//            }
//
//            if(keyframe_)
//            {
//                Bitmap bitmap = Bitmap.createBitmap(original.width(), original.height(), Bitmap.Config.ARGB_8888);
//                Utils.matToBitmap(original,bitmap);
//                send_image(bitmap);
//            }
//            System.out.println("frame Keyframe:" + keyframe_);
//            System.out.println("frame relocalize:" + relocalize_);
////            Log.d("frame Keyframe:",Integer.toString(poseMatrix.length));
////            Log.i("frame reloc:",Float.toString(poseMatrix[15]));
//            double[][] R = new double[3][3];
//            double[] T = new double[3];
//
//            for (int i = 0; i < 3; i++) {
//                for (int j = 0; j < 3; j++) {
//                    R[i][j] = pose[i][j];
//                }
//            }
//            for (int i = 0; i < 3; i++) {
//                T[i] = pose[i][3];
//            }
//            RealMatrix rotation = new Array2DRowRealMatrix(R);
//            RealMatrix translation = new Array2DRowRealMatrix(T);
//            MatrixState.set_model_view_matrix(rotation, translation);
//
//            MyRender.flag = true;
//            count++;
//
//        } else {
//            //如果没有得到相机的位姿矩阵，就不画立方体
//            MyRender.flag = false;
//        }

//      CVTest(rgb.getNativeObjAddr());

    }


    void printMatrix(RealMatrix input) {
        double matrixtoarray[][] = input.getData();
        for (int i = 0; i < matrixtoarray.length; i++) {
            for (int j = 0; j < matrixtoarray[0].length; j++) {
                System.out.print(matrixtoarray[i][j] + "\t");
            }
            System.out.print("\n");
        }
    }


    /**
     * 用于测试java读取文件权限的函数
     **/
    void readFileOnLine(String strFileName) throws Exception {

        int REQUEST_EXTERNAL_STORAGE = 1;
        String[] PERMISSIONS_STORAGE = {
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };
        int permission = ActivityCompat.checkSelfPermission(OrbTest.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    OrbTest.this,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
        FileInputStream fis = new FileInputStream(new File(strFileName));
        StringBuffer sBuffer = new StringBuffer();
        DataInputStream dataIO = new DataInputStream(fis);
        String strLine = null;
        while ((strLine = dataIO.readLine()) != null) {
            Log.i(TAG, strLine + "+++++++++++++++++++++++++++++++++++++++");
        }
        dataIO.close();
        fis.close();
    }


    private SeekBar.OnSeekBarChangeListener seekListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            Log.i(TAG, "onStopTrackingTouch");
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            Log.i(TAG, "onStartTrackingTouch");
        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress,
                                      boolean fromUser) {
            Log.i(TAG, "onProgressChanged");
            if (progress > 50) {
                SCALE = (progress - 50) * 10;
            } else {
                SCALE = (50 - progress) * 0.5;
            }
            myTextView.setText("当前值 为: " + SCALE);

        }
    };

}
