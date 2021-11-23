package com.example.ys.orbtest;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class Config {

    public String host_ip;
    public int port;

    public Config(File file_name) {
        try{
            BufferedReader bfr = new BufferedReader(new FileReader(file_name));
            host_ip = get_config_content(bfr.readLine());
            port = Integer.parseInt(get_config_content(bfr.readLine()));

        } catch (IOException e) {
        e.printStackTrace();
        }
    }

    private String get_config_content(String content)
    {
        return content.substring(content.indexOf(":")+1).trim();
    }

}
