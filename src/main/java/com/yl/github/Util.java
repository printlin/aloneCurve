package com.yl.github;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

/**
 * @description:
 * @author: YL
 * @date: Created in 2022/10/26 18:00
 * @version: 1.0
 * @modified By:
 */
public class Util {
    /**
     * 经纬度计算距离
     * @param a 点1
     * @param b 点2
     * @return 距离
     */
    public static double getDistance(Position a, Position b){
        if(a == null || b == null){
            return 0;
        }
        double cos = (Math.cos(Math.toRadians(a.lat)) * Math.cos(Math.toRadians(b.lat)) * Math.cos(Math.toRadians(b.lng) - Math.toRadians(a.lng))
                + Math.sin(Math.toRadians(a.lat)) * Math.sin(Math.toRadians(b.lat)));
        double acos = Math.acos(cos);
        double re = 6378137 * acos;
        return Double.isNaN(re)?0:re;
    }

    /**
     * 从制定路径读取字符文件
     * @param path 路径
     * @return 文本内容
     * @throws IOException
     */
    public static String copyToString(String path) throws IOException {
        StringBuilder sb = new StringBuilder(1024);
        Reader readerFile = new FileReader(path);
        BufferedReader reader = new BufferedReader(readerFile);
        String line;
        do {
            line = reader.readLine();
            sb.append(line);
        } while (line != null);
        reader.close();
        readerFile.close();
        return sb.toString();
    }
}
