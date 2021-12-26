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
    Mat Rot_now;
    Mat Trans_now;
    //public float[][] id_pose;
    public long[] update_poses;

    public track() {
        is_processing = false;
    }

    public void parser(){
        System.out.println("pose initial = " + pose);
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

    public boolean parse_pose(String data){
        System.out.println("pose initial = " + data);
        data = data.substring(5, data.length() - 2);
        System.out.println("pose = " + data);
        pose_val = pose.split("_");
        String Rot_raw = pose_val[1];
        Rot_raw = Rot_raw.substring(1, Rot_raw.length() - 1);
        if(!parse_Rot(Rot_raw)){
            return false;
        }
        String Trans_raw = pose_val[2];
        Trans_raw = Trans_raw.substring(1, Trans_raw.length() - 1);
        if(!parse_Trans(Trans_raw)){
            return false;
        }

        is_processing = true;
        return true;


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

    public boolean parse_Rot(String input){
        String[] tmp_arr = input.split(",");
        Mat tmp = new Mat(3,3,CV_32F);

        try{
            for(int row=0; row<3; ++row)
            {
                for(int column=0; column<3; ++column)
                {
                    tmp.put(row,column,Float.parseFloat(tmp_arr[row*3+column]));
                }
            }
            Rot_now = tmp;
            return true;

        }catch (java.lang.UnsupportedOperationException e){
            return false;
        }

    }

    public boolean parse_Trans(String input){
        String[] tmp_arr = input.split(",");
        Mat tmp = new Mat(3,1,CV_32F);

        try{
            for(int row=0; row<3; ++row)
            {
                for(int column=0; column<1; ++column)
                {
                    tmp.put(row,column,Float.parseFloat(tmp_arr[row]));
                }
            }
            Trans_now = tmp;
            return true;

        }catch (java.lang.UnsupportedOperationException e){
            return false;
        }

    }
}
