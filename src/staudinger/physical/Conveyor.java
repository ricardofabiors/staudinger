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
public class Conveyor extends MRA{
    
    public static final int FORWARD = 1;
    public static final int BACKWARD = 0;
    
    @Override
    protected void setup(){
        defaultSetup();
        addResponderBehaviour();
    }
    
    public Conveyor(String from_to){
        move.addProperty(from_to, "yes");
        this.skills = new Skill[] {move, stop};
    }
    
    public Conveyor(){
        this("p2 to p3");
    }
    
    @Override
    protected MRAInfo getMRAInfo() {
        myMrainfo = new MRAInfo(); 
        myMrainfo.setAID(this.getLocalName());
        myMrainfo.setSkills(Util.fromSkill(getSkills()));
        return myMrainfo;
    }
    
    protected Skill move = new Skill(this, "move", "boolean", new String[]{"int"}){
        @Override
        public void execute() throws SkillExecuteException {
            isBusy = true;
            int direction = Integer.parseInt(getArgsValues()[0]);
            switch (direction) {
                case FORWARD:
                    System.out.println(this.myMRA.getLocalName() + ": Movendo para frente...");
                    result = "true";
                    break;
                case BACKWARD:
                    System.out.println(this.myMRA.getLocalName() + ": Movendo para trás...");
                    result = "true";
                    break;
                default:
                    System.out.println(this.myMRA.getLocalName() + ": Direção inválida.");
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

    
