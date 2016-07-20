package com.dimiprount.distancecalculator;

public class Routes {
    int id;
    String sOrigin, sDestination, sDisDur;

    public Routes(){

    }

    public int getId(){
        return this.id;
    }

    public void setId(int keyId){
        this.id = keyId;
    }

    public String getsOrigin(){
        return this.sOrigin;
    }

    public void setsOrigin(String sOrigin){
        this.sOrigin = sOrigin;
    }

    public String getsDestination(){
        return this.sDestination;
    }

    public void setsDestination(String sDestination){
        this.sDestination = sDestination;
    }

    public String getsDisDur(){
        return this.sDisDur;
    }

    public void setsDisDur(String sDisDur){
        this.sDisDur = sDisDur;
    }

}
