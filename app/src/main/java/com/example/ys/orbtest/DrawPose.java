package com.example.ys.orbtest;
import static org.opencv.core.CvType.CV_32F;

import android.util.Log;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.logging.Logger;

public class DrawPose {

    double[] Kalib_data = new double[9];

    double[][] T_init = new double[4][4];
    double[][] T_angle =  new double[4][4];
    Mat VO_center_world = new Mat(3,1,CV_32F);
    Mat center_point = new Mat(3,1,CV_32F);
    Mat center_point_x = new Mat(3,1,CV_32F);
    Mat center_point_y = new Mat(3,1,CV_32F);
    Mat center_point_z = new Mat(3,1,CV_32F);
    double[] VO_x_world = new double[3];
    double[] VO_y_world = new double[3];
    double[] VO_z_world = new double[3];
    Mat Kalib = new Mat(3,3, CV_32F);

    private void printMat(Mat mat, String label){
        for(int i=0; i<mat.rows();++i)
        {
            for(int j=0;j<mat.cols();++j){
                double[] tmp = mat.get(i,j);
                Log.i(label,Integer.toString(i)+", "+Integer.toString(j)+": "+tmp[0]);
            }
        }

    }

    public DrawPose(){
        for(int i=0; i<9; ++i){
            Kalib_data[i] = 0;
//            for(int j=0;j<3;++j)
//            {
//                Kalib.put(i,j,0);
//            }
        }

        Kalib_data[0] = 737.037; //fx
        Kalib_data[2] = 340.565; //cx
        Kalib_data[4] = 699.167; //fy
        Kalib_data[5] = 218.486; //cy
        Kalib_data[8] = 1;
//        Kalib.put(0,0,737.037);
//        Kalib.put(0,2,340.565);
//        Kalib.put(1,1,699.167);
//        Kalib.put(1,2,340.565);
//        Kalib.put(2,2,1);
        Kalib.put(0,0,Kalib_data);

//        Log.i("kalib", String.valueOf(Kalib));
//        Mat result = new Mat(2,2,CV_32F);
//        Mat new_mat = new Mat(3,3,CV_32F);
//        for(int i =0; i<3;++i){
//            for(int j=0; j<3;++j){
//                new_mat.put(i,j,i);
//            }
//        }
//
//        Mat test_mat = new Mat(2,2,CV_32F);
//        for(int i =0; i<2;++i){
//            for(int j=0; j<2;++j){
//                test_mat.put(i,j,j+i*2);
//            }
//        }
//        Mat Rot = new Mat(4,4,CV_32F);
//        Rot.put(0,0,1);
//        Rot.put(0,1,2);
//        Rot.put(0,2,3);
//        Rot.put(0,3,10);
//        Rot.put(1,0,4);
//        Rot.put(1,1,5);
//        Rot.put(1,2,6);
//        Rot.put(1,3,11);
//        Rot.put(2,0,7);
//        Rot.put(2,1,8);
//        Rot.put(2,2,9);
//        Rot.put(2,3,12);
//
//        Mat tmp = new Mat(3,3,CV_32F);
//        for(int i =0; i<3; ++i){
//            for(int j=0; j<3;++j){
//                tmp.put(i,j,Rot.get(i,j));
//            }
//        }
//        printMat(tmp);

//        center_point.put(0,0,340);
//        center_point.put(1,0,240);
//        center_point.put(2,0,1);
//
//        Mat Rot = new Mat(3,3,CV_32F);
//        Rot.put(0,0,0.998);
//        Rot.put(0,1,0.001837);
//        Rot.put(0,2,0.0616);
//        Rot.put(1,0,-0.000499);
//        Rot.put(1,1,0.9997);
//        Rot.put(1,2,-0.0217);
//        Rot.put(2,0,-0.06136);
//        Rot.put(2,1,0.02165);
//        Rot.put(2,2,0.99786);
//
//        Mat Trans = new Mat(3,1,CV_32F);
//        Trans.put(0,0,-0.00095);
//        Trans.put(1,0,-0.001739);
//        Trans.put(2,0,-0.01252);
//        set_anchor((float) 1.3,Rot,Trans);




    }
    public void DrawAxis(Mat image, int[] center, float scale)
    {
        Point center_pt = new Point(center[0], center[1]);
        center_point.put(0,0,center[0]);
        center_point.put(1,0,center[1]);
        center_point.put(2,0,1);
        int radius = Math.round(10/scale);
        Imgproc.circle (
                image,                 //Matrix obj of the image
                center_pt,    //Center of the circle
                radius,                    //Radius
                new Scalar(255, 255, 255),  //Scalar object for color
                3                      //Thickness of the circle
        );
        int len = Math.round(100/scale);
        //y: blue
        Imgproc.arrowedLine(image,center_pt,new Point(center[0],center[1]-len), new Scalar(0, 0, 255),5,0,0,0.3);
        Imgproc.putText(image,"y",new Point(center[0],center[1]-len-30),2,2,new Scalar(0, 0, 255),4);
        //x: red
        Imgproc.arrowedLine(image,center_pt,new Point(center[0]-len,center[1]), new Scalar(255, 0, 0),5,0,0,0.3);
        Imgproc.putText(image,"x",new Point(center[0]-len-40,center[1]),2,2,new Scalar(255, 0, 0),4);

        //z: green
        Imgproc.arrowedLine(image,center_pt,new Point(center[0],center[1]), new Scalar(0, 255, 0),5,0,0,0.3);
        Imgproc.putText(image,"z",new Point(center[0],center[1]+40),2,2,new Scalar(0, 255, 0),4);

    }

    public void set_anchor(float depth, Mat R, Mat T){
//        Pw = inv(K*Rot)*(P_center - K*Trans)



        Mat KR_inv_tmp = new Mat(3,3,CV_32F);
        Core.gemm(Kalib, R, 1, Mat.zeros(3, 3, CV_32F), 0, KR_inv_tmp, 0);

        Mat KR_inv = new Mat(3,3,CV_32F);
        Core.invert(KR_inv_tmp,KR_inv,0);

        Mat KT = new Mat(3,1,CV_32F);
        Core.gemm(Kalib, T, 1, Mat.zeros(3, 1, CV_32F), 0, KT, 0);

        Mat p_KT = new Mat(3,1,CV_32F);
        Core.subtract(center_point,KT,p_KT);
        Core.gemm(KR_inv, p_KT, 1, Mat.zeros(3, 3, CV_32F), 0, VO_center_world, 0);
        VO_center_world.put(2,0,depth);
        printMat(VO_center_world,"center");
//        Core.gemm(new_mat, test_mat, 1, Mat.zeros(3, 3, CV_32F), 0, result, 0);
//        Core.multiply(new_mat, test_mat , result);
//        printMat(VO_center_world);

    }

    public Mat world2pixel(Mat point, Mat Rot, Mat Trans){
        Mat tmp = new Mat(3,1,CV_32F);
        Core.gemm(Rot, point, 1, Mat.zeros(3, 3, CV_32F), 0, tmp, 0);
        Mat tmp_trans = new Mat(3,1,CV_32F);
        Core.add(tmp,Trans,tmp_trans);
        Mat result = new Mat(3,1,CV_32F);
        Core.gemm(Kalib, tmp_trans, 1, Mat.zeros(3, 3, CV_32F), 0, result, 0);
        return result.clone();
    }

    public Mat Place_VO(Mat image, Mat T_w2vc, Mat transer){
        Mat Rot = new Mat(3,3,CV_32F);
        for(int i =0; i<3; ++i){
            for(int j=0; j<3;++j){
                Rot.put(i,j,T_w2vc.get(i,j));
            }
        }
        Mat Trans = new Mat(3,1,CV_32F);
        Trans.put(0,0,T_w2vc.get(0,3));
        Trans.put(1,0,T_w2vc.get(1,3));
        Trans.put(2,0,T_w2vc.get(2,3));
        Mat tmp = world2pixel(VO_center_world.clone(),Rot.clone(),Trans.clone());
        double[] x = tmp.get(0,0);
        double[] y = tmp.get(1,0);
        double[] z = tmp.get(2,0);

        Mat position = new Mat(2,1,CV_32F);
        position.put(0,0,x[0]/z[0]);
        position.put(1,0,y[0]/z[0]);
        Mat result = new Mat(2,1,CV_32F);
        Core.gemm(transer, position, 1, Mat.zeros(2, 1, CV_32F), 0, result, 0);
        return result.clone();

    }

    public void DrawAxis(Mat image, int[] center, float scale, double[][] R, double[] T)
    {
        Point center_pt = new Point(center[0], center[1]);
        int radius = Math.round(10/scale);
        Imgproc.circle (
                image,                 //Matrix obj of the image
                center_pt,    //Center of the circle
                radius,                    //Radius
                new Scalar(255, 255, 255),  //Scalar object for color
                3                      //Thickness of the circle
        );
        int len = Math.round(100/scale);
        //y: blue
        Imgproc.arrowedLine(image,center_pt,new Point(center[0],center[1]-len), new Scalar(0, 0, 255),5,0,0,0.3);
        Imgproc.putText(image,"y",new Point(center[0],center[1]-len-30),2,2,new Scalar(0, 0, 255),4);
        //x: red
        Imgproc.arrowedLine(image,center_pt,new Point(center[0]-len,center[1]), new Scalar(255, 0, 0),5,0,0,0.3);
        Imgproc.putText(image,"x",new Point(center[0]-len-40,center[1]),2,2,new Scalar(255, 0, 0),4);

        //y: green
        Imgproc.arrowedLine(image,center_pt,new Point(center[0],center[1]), new Scalar(0, 255, 0),5,0,0,0.3);
        Imgproc.putText(image,"z",new Point(center[0],center[1]+40),2,2,new Scalar(0, 255, 0),4);

    }


}
