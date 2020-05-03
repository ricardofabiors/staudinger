/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hardware;

import jade.core.Agent;
import staudinger.cognitive.Box;

/**
 *
 * @author Fábio Ricardo
 */
public class HardwareAcess {
    
    public static final int UP = 1;
    public static final int DOWN = 0;
    public static final int LEFT = 2;
    public static final int RIGHT = 3;
    
    int readColor;
    int count = 0;
    
    public HardwareAcess(){
        readColor = Box.BLACK;
    }
    
    public String move(Agent thisAgent, int direction){
        String result;
        switch (direction) {
            case DOWN:
                System.out.println(thisAgent.getLocalName() + ": Movendo para baixo..."); 
                result = "true";
                break;
            case RIGHT:
                System.out.println(thisAgent.getLocalName() + ": Movendo para direita..."); 
                result = "true";
                break;
            default:
                System.out.println(thisAgent.getLocalName() + ": Direção inválida.");
                result = "false";
                break;
        }
        return result;
    }
    
    public String checkColor(Agent thisAgent, int desiredColor){
        String result;
        if(desiredColor == readColor){
            System.out.println(thisAgent.getLocalName() + ": A cor do caixote coincide com a desejada"); 
            result = "true";
        } 
        else {
            System.out.println(thisAgent.getLocalName() + ": A cor do caixote NÃO coincide com a desejada"); 
            result = "false";
            count++;
            if(count == 2) readColor = Box.GREEN;
        }
        return result;
    }
}
