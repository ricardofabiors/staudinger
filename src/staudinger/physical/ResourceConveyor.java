/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package staudinger.physical;

import eps.MRA;
import eps.MRAInfo;
import eps.Skill;
import eps.SkillExecuteException;
import eps.Util;

/**
 *
 * @author Fábio Ricardo
 */
public class ResourceConveyor extends MRA{
    public static final int R1 = 0;
    public static final int R2 = 1;
    public static final int THE_END = 2;       
    
    @Override
    protected void setup(){
        defaultSetup();
        addResponderBehaviour();
    }
    
    public ResourceConveyor(String from_to1, String from_to2, String from_to3, String from_to4){
        move.addProperty(from_to1, "yes");
        move.addProperty(from_to2, "yes");
        move.addProperty(from_to3, "yes");
        move.addProperty(from_to4, "yes");
        this.skills = new Skill[] {move, stop};
    }
    
    public ResourceConveyor(){
        this("p6 to p7", "p6 to p8", "p7 to p8", "p8 to p9");
    }
    
    @Override
    protected MRAInfo getMRAInfo() {
        myMrainfo = new MRAInfo(); 
        myMrainfo.setAID(this.getLocalName());
        myMrainfo.setSkills(Util.fromSkill(getSkills()));
        return myMrainfo;
    }
    
    public Skill move = new Skill(this, "move", "boolean", new String[]{"int"}){
        @Override
        public void execute() throws SkillExecuteException {
            isBusy = true;
            int to_position = Integer.parseInt(getArgsValues()[0]);
            switch (to_position) {
                case R1:
                    System.out.println(this.myMRA.getLocalName() + ": Movendo para R1..."); 
                    result = "true";
                    break;
                case R2:
                    System.out.println(this.myMRA.getLocalName() + ": Movendo para R2..."); 
                    result = "true";
                    break;
                case THE_END:
                    System.out.println(this.myMRA.getLocalName() + ": Movendo para o final..."); 
                    result = "true";
                    break;
                default:
                    System.out.println(this.myMRA.getLocalName() + ": Destino inválido");
                    result = "false";
                    break;
            }
            isBusy = false;
        }
    };
    
    protected Skill stop = new Skill(this, "stop", "boolean", new String[]{"void"}){
        @Override
        public void execute() throws SkillExecuteException {
            System.out.println(this.myMRA.getLocalName() + ": Minha esteira parou.");         
        }
    };
    
    @Override
    protected Skill[] getSkills() {
        return this.skills;
    }
}
