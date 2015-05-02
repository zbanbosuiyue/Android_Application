package com.zhengfood.pointtoanything;

/**
 * Created by robert on 4/14/15.
 */

import java.io.Serializable;
import java.util.ArrayList;

import com.google.api.client.util.Key;

/** Implement this class from "Serializable"
 * So that you can pass this class Object to another using Intents
 * Otherwise you can't pass to another actitivy
 * */
public class Place implements Serializable {

    @Key
    public String id;

    @Key
    public String name;

    @Key
    public String reference;

    @Key
    public String icon;

    @Key
    public Photos[] photos;

    @Key
    public String vicinity;

    @Key
    public Geometry geometry;

    @Key
    public String formatted_address;

    @Key
    public String formatted_phone_number;

    @Key
    public OpeningHours opening_hours;

    @Key
    public float rating;

    @Key
    public String[] types;


    @Override
    public String toString() {
        return name + " - " + id + " - " + reference;
    }

    public static class Geometry implements Serializable
    {
        @Key
        public Location location;
    }

    public static class Location implements Serializable
    {
        @Key
        public double lat;

        @Key
        public double lng;
    }

    public static class Photos implements Serializable
    {
        @Key
        public String photo_reference;

    }

    public static class OpeningHours implements Serializable{
        @Key
        public boolean open_now;

        @Key
        public String[] weekday_text;
    }

    public static class Reviews implements Serializable{
        @Key
        public Aspects[] aspects;

        @Key
        public String author_name;

        @Key
        public float rating;

        @Key
        public String text;
    }

    public static class Aspects implements Serializable{
        @Key
        int rating;

        @Key
        String type;
    }
}


