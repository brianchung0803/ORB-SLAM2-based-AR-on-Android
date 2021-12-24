package com.example.ys.orbtest;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.Utils;
import org.opencv.core.*;
import org.opencv.core.Point;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;
//Java Matlab
import static jeigen.Shortcuts.*;

import android.graphics.Bitmap;
import android.util.Log;

import jeigen.DenseMatrix;

public class Data {

    public String id;
    public String data;
//    public int count;
    public String[] parse_data;
    public String[] rot_mat;
    public String[] trans_mat;
    public String[][] corner_3d;
    public Bitmap bitmap;
    public boolean is_processing;
    public boolean pass_value;
    public Mat temp_rgb_mat;
    public Mat rgb_mat;

    public Data()
    {
        is_processing = false;
        pass_value = false;
    }

    public void parser(){
        rot_mat = new String[9];
        trans_mat = new String[3];

        data = data.substring(3, data.length() - 1);
        parse_data = data.split("_");
        //First entry
        id = parse_data[0];
        //Second entry
        rot_mat = parse_data[1].split(",");
        //Third entry
        trans_mat = parse_data[2].split(",");
        //Count
        int count = Integer.parseInt(parse_data[3]);
        if(count == 0) return;

        System.out.println("count" + count);

        String[][] type = new String[count][2];
        String[][] corner_2d = new String[count][16];
        if(count != 0) {
//            type = new String[count][2];
            corner_3d = new String[count][24];

            for (int i = 0; i < count; i++) {
                parse_data[4 + i * 3] = parse_data[4 + i * 3].substring(1, parse_data[4 + i * 3].length() - 1);
                type[i] = parse_data[4 + i * 3].split(",");
//                check_arr_type(i);
                parse_data[5 + i * 3] = parse_data[5 + i * 3].substring(1, parse_data[5 + i * 3].length() - 1);
                corner_2d[i] = parse_data[5 + i * 3].split(",");
                parse_data[6 + i * 3] = parse_data[6 + i * 3].substring(1, parse_data[6 + i * 3].length() - 1);
                corner_3d[i] = parse_data[6 + i * 3].split(",");
            }
        }
        for(int i = 0; i < type.length; i++){
            for(int j = 0; j < type[0].length; j++)
                Log.i("row" + i + "col" + j, "type" + type[i][j]);
        }
        for(int i = 0; i < count; i++) {
            create_config(type, corner_2d, i);
        }

        //Finish drawing
        is_processing = false;
        rgb_mat = temp_rgb_mat;
//        temp_rgb_mat = new Mat();
    }

    public void create_config(String [][]type, String[][] corner_2d, int i){
        //config type
        float [][] box_2d = new float[2][8];
//        Log.i("create config" , "i = " + i);
        Log.i("create config" , "1 = " + type[i][0]);
        Log.i("create config" , "2 = " + type[i][1]);
        int []box_config = new int[2];
        box_config[0] = Integer.parseInt(type[i][0]);
        box_config[1] = Integer.parseInt(type[i][1]);
        //2d box points
        for(int j = 0; j < 2; j++){
            for(int k =0; k < 8; k++){
                box_2d[j][k] = Float.parseFloat(corner_2d[i][k+(8*j)]);
            }
        }
        plot_image_with_cuboid(box_2d, box_config);
    }

    public void plot_image_with_cuboid(float [][]box_2d, int []box_config){
        DenseMatrix edge_markers = get_cuboid_draw_edge_markers(true, box_config);
        plot_image_with_cuboid_edges(edge_markers, box_2d);
    }

    public DenseMatrix get_cuboid_draw_edge_markers(boolean final_universal_object, int[] box_config){
        DenseMatrix edge_markers= zeros(12, 3);
        DenseMatrix visible_hidden_edge_pts = get_object_edge_visibility(final_universal_object, box_config);
        DenseMatrix edge_line_markers;
        if(final_universal_object){
            if(box_config[0] == 1){
                if(box_config[1] == 1)
                    edge_line_markers = new DenseMatrix("4 2 6 3 1 5 5 5 3 1 3 1");
                else
                    edge_line_markers = new DenseMatrix("2 4 6 3 1 5 5 5 3 1 3 1");
            }
            else
                edge_line_markers = new DenseMatrix("2 4 2 6 6 3 5 5 3 1 3 1");
        }
        else{
            if(box_config[0] == 1)
                edge_line_markers = new DenseMatrix("4 2 6 1 3 1 3 5 5 5 1 3");
            else
                edge_line_markers = new DenseMatrix("4 2 6 6 2 1 3 1 3 5 5 3");
        }

//        System.out.println("arr a" + visible_hidden_edge_pts);
//        System.out.println("arr b" + edge_line_markers);
        edge_markers = visible_hidden_edge_pts.concatRight(edge_line_markers.t());
        edge_markers = edge_markers.sub(1);
        return edge_markers;
    }

    public void plot_image_with_cuboid_edges(DenseMatrix edge_markers, float [][]box_2d){
        DenseMatrix line_markers = new DenseMatrix( "0 0 255 2; 0 0 255 1; 0 255 0 2; 0 255 0 1; 255 0 0 2; 255 0 0 1");
//        System.out.println("edge_markers" + edge_markers.rows);

//        if(pass_value == false) {
////            Bitmap bmp32 = bitmap.copy(Bitmap.Config.ARGB_8888, true);
//            Utils.bitmapToMat(bitmap, temp_rgb_mat);
//            pass_value = true;
//        }

        for(int edge_id = 0; edge_id < edge_markers.rows; edge_id++){
            DenseMatrix edge_conds = edge_markers.row(edge_id);
            System.out.println("edge_conds" + edge_conds);
            //Plot line
            int first = (int)(edge_conds.get(0, 0));
            int second = (int)(edge_conds.get(1, 0));
            int third = (int)(edge_conds.get(2, 0));
            System.out.println("third" + third);
            Point start = new Point(box_2d[0][first], box_2d[1][first]);
            Point end = new Point(box_2d[0][second], box_2d[1][second]);
            Scalar sc = new Scalar(line_markers.get(third, 0), line_markers.get(third, 1), line_markers.get(third, 2));
            int thickness = (int)line_markers.get(third, 3);
            int linetype = 8;
            int shift = 0;
            Imgproc.line(temp_rgb_mat, start, end, sc, thickness, linetype, shift);
        }

//        return mat;
    }

    public DenseMatrix get_object_edge_visibility(boolean final_universal_object, int[] box_config){
        DenseMatrix visible_hidden_edge_pts;
        if(final_universal_object){
            if(box_config[0] == 1){
                if(box_config[1] == 1)
                    visible_hidden_edge_pts = new DenseMatrix("3 4; 4 1; 4 8; 1 2; 2 3; 2 6; 1 5; 3 7; 5 6; 6 7; 7 8; 8 5");
                else
                    visible_hidden_edge_pts = new DenseMatrix("2 3; 3 4; 3 7; 1 2; 1 4; 2 6; 1 5; 4 8; 5 6; 6 7; 7 8; 8 5");
            }
            else
                visible_hidden_edge_pts = new DenseMatrix("2 3; 3 4; 4 1; 3 7; 4 8; 1 2; 2 6; 1 5; 5 6; 6 7; 7 8; 8 5");
        }
        else{
            if(box_config[0] == 1)
                visible_hidden_edge_pts = new DenseMatrix("7 8; 7 6; 7 1; 1 2; 2 3; 3 4; 4 1; 2 6; 3 5; 4 8; 5 8; 5 6");
            else
                visible_hidden_edge_pts = new DenseMatrix("7 8; 7 6; 7 1; 8 4; 8 5; 1 2; 2 3; 3 4; 4 1; 2 6; 3 5; 5 6");
        }
        return  visible_hidden_edge_pts;
    }
}
