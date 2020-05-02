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
public class MachineTool extends MRA{
    
    private boolean sensor;
    
    @Override
    protected void setup(){
        defaultSetup();
        addResponderBehaviour();
    }
    
    public MachineTool(String myPosition){
        this.sensor = true;
        cover.addProperty(myPosition, "yes");
        this.skills = new Skill[] {cover, stop};
    }
    
    public MachineTool(){
        this("p8");
    }
    
    @Override
    protected MRAInfo getMRAInfo() {
        myMrainfo = new MRAInfo(); 
        myMrainfo.setAID(this.getLocalName());
        myMrainfo.setSkills(Util.fromSkill(getSkills()));
        return myMrainfo;
    }
    
    protected Skill cover = new Skill(this, "cover", "boolean", new String[]{"void"}){
        @Override
        public void execute() throws SkillExecuteException {
            isBusy = true;
            if(isThereBox()){
                System.out.println(this.myMRA.getLocalName() + ": Tampando caixote..."); 
                result = "true";
            }
            else{
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
