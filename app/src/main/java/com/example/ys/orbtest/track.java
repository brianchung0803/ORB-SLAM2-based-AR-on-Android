package com.example.ys.orbtest;

import static org.opencv.core.CvType.CV_32F;

import android.util.Log;

import org.opencv.core.Mat;

public class track {
    public boolean is_processing;
    public String pose;
    public String[] pose_val;
    public int count;
    public int[] id;
    //public float[][] id_pose;
    public long[] update_poses;

    public track() {
        is_processing = false;
    }

    public void parser(){
        pose = pose.substring(5, pose.length() - 2);
        System.out.println("pose = " + pose);
        pose_val = pose.split("_");
        count = Integer.parseInt(pose_val[0]);
        id = new int[count];

        update_poses = new long[count];
        for(int i = 0; i < count; i++){
            if(Integer.parseInt(pose_val[1 + i * 2])>1000) id[i] = 5;
            else{
                id[i] = Integer.parseInt(pose_val[1 + i * 2]);
            }

            pose_val[2 + i * 2] = pose_val[2 + i * 2].substring(1, pose_val[2 + i * 2].length() - 1);
            parse_float(pose_val[2 + i * 2], i);
        }
        is_processing = true;
    }

    public void parse_float(String input, int idx){
        String[] tmp_arr = input.split(",");
        Mat tmp = new Mat(4,4,CV_32F);
        for(int row=0; row<4; ++row)
        {
            for(int column=0; column<4; ++column)
            {
                tmp.put(row,column,Double.parseDouble(tmp_arr[row*4+column]));
            }
        }

        Log.i("IRL_track","mat =" + tmp);
        update_poses[idx] = tmp.getNativeObjAddr();

    }
}
