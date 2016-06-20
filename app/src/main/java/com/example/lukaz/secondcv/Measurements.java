package com.example.lukaz.secondcv;

import java.util.Date;

/**
 * Created by lukaz on 14.6.2016.
 */
public class Measurements {

    private String _cas;
    private int _result;
    private String _kot1;
    private String _kot2;

    public Measurements(){
    }

    public void set_cas(String cas) {
        this._cas = cas;
    }

    public void set_result(int result) {
        this._result = result;
    }

    public void set_kot1(String kot1) {
        this._kot1 = kot1;
    }

    public void set_kot2(String kot2) {
        this._kot2 = kot2;
    }

    public String get_cas() {
        return _cas;
    }

    public int get_result() {
        return _result;
    }

    public String get_kot1() {
        return _kot1;
    }

    public String get_kot2() {
        return _kot2;
    }

    public Measurements(String cas, int result, String kot1, String kot2) {
        this._cas = cas;
        this._result = result;
        this._kot1 = kot1;
        this._kot2 = kot2;
    }

}