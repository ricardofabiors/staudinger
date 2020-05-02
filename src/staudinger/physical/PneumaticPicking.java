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
public class PneumaticPicking extends MRA{
    
    private boolean sensor;
    
    @Override
    protected void setup(){
        defaultSetup();
        addResponderBehaviour();
    }
    
    public PneumaticPicking(String myPosition){
        this.sensor = true;
        insert.addProperty(myPosition, "yes");
        this.skills = new Skill[] {insert, stop};
    }
    
    public PneumaticPicking(){
        this("p7");
    }
    
    @Override
    protected MRAInfo getMRAInfo() {
        myMrainfo = new MRAInfo(); 
        myMrainfo.setAID(this.getLocalName());
        myMrainfo.setSkills(Util.fromSkill(getSkills()));
        return myMrainfo;
    }
    
    protected Skill insert = new Skill(this, "insert", "boolean", new String[]{"int"}){
        @Override
        public void execute() throws SkillExecuteException {
            isBusy = true;
            int quantity = Integer.parseInt(getArgsValues()[0]);
            if(quantity != 0 && isThereBox()){
                System.out.println(this.myMRA.getLocalName() + ": Inserindo " + getArgsValues()[0] + " produtos..."); 
                result = "true";
            }
            else{
                System.out.println(this.myMRA.getLocalName() + ": Nenhum produto para inserir"); 
                result = "false";
            }
            isBusy = false;
        }
    };
    
    protected Skill stop = new Skill(this, "stop", "boolean", new String[]{"void"}){
        @Override
        public void execute() throws SkillExecuteException {
            System.out.println(this.myMRA.getLocalName() + ": O módulo parou.");         
        }
    };
    
    @Override
    protected Skill[] getSkills() {
        return this.skills;
    }
    
    private boolean isThereBox(){
        if(sensor){
            return true;
        }
        else return false;
    }
    
}
