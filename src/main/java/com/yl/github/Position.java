package com.yl.github;

/**
 * @description: 轨迹点对象
 * @author: YL
 * @date: Created in 2020/12/24 11:24
 * @version: 1.0
 * @modified By:
 */
public class Position implements Cloneable{
    public Double lat;//纬度
    public Double lng;//经度
    public Double direction;//朝向
    public Double speed;//速度
    public Double height;//海拔
    public Long time;//时间戳

    public double countMile = 0;//驾驶里程 m
    public long countTime = 0;//驾驶时长 ms
    public double maxSpeed = 0;//最快速度 km/h

    public Position(){}

    public Position(double lat, double lng){
        this.lat = lat;
        this.lng = lng;
    }

    @Override
    public Position clone() throws CloneNotSupportedException {
        return (Position)super.clone();
    }

    @Override
    public String toString() {
        return "lat:" + lat+"\tlng:"+lng+"\tspeed:"+speed+"\ttime:"+time;
    }
}
