/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package staudinger.cognitive;

/**
 *
 * @author Fábio Ricardo
 */
public class Box {
    //box colors
    public static final int GREEN = 0;
    public static final int BLACK = 1;
    public static final int UNKNOWN = 5;
    
    private int color;
    protected int resources;
    private String rfid_code;
    private boolean isOpen;

    public Box(int color, String rfid_code){
        this.color = color;
        this.resources = 0;
        this.rfid_code = rfid_code;
        this.isOpen = true;
    }

    public Box(int color){
        this(color, "");
    }
    
    public Box(){
        this(UNKNOWN, "");
    } 

    public boolean isOpen(){
        return isOpen;
    }

    public void close(){
        isOpen = false;
    }

    public int getColor(){
        return color;
    }
    
    public String getCode(){
        return rfid_code;
    }
    
    public void setCode(String code){
        rfid_code = code;
    }
    
}
