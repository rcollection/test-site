package org.rx.test;

import com.google.gson.Gson;

import java.util.List;
import java.util.Map;

public class GsonTest {
    public static void main(String[] args){
        Gson gson = new Gson();
        Data data = gson.fromJson("{\"field\": 1000, \"str\":\"job\"}", Data.class);
        System.out.println(data.getStr());
    }

    public static class Data{
        private String str;
        private List<String> list;
        private Map<String, String> map;

        public String getStr() {
            return str;
        }
    }
}
