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
public class StorageConveyor extends MRA{
    
    private boolean sensorIn;
    
    @Override
    protected void setup(){
        defaultSetup();
        addResponderBehaviour();
    }
    
    public StorageConveyor(String from_to){
        this.sensorIn = true;
        getNewBox.addProperty(from_to, "yes");
        this.skills = new Skill[] {getNewBox, stop};
    }
    
    public StorageConveyor(){
        this("p0 to p1");
    }
    
    @Override
    protected MRAInfo getMRAInfo() {
        myMrainfo = new MRAInfo(); 
        myMrainfo.setAID(this.getLocalName());
        myMrainfo.setSkills(Util.fromSkill(getSkills()));
        return myMrainfo;
    }
    
    protected Skill getNewBox = new Skill(this, "getNewBox", "boolean", new String[]{"void"}){   
        @Override
        public void execute() throws SkillExecuteException {
            isBusy = true;
            if(isThereBox()){
                System.out.println(this.myMRA.getLocalName() + ": Pegando e movendo um novo caixote..."); 
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
            System.out.println(this.myMRA.getLocalName() + ": Minha esteira parou.");         
        }
    };
    
    @Override
    protected Skill[] getSkills() {
        return this.skills;
    }
    
    private boolean isThereBox(){
        if(sensorIn){
            return true;
        }
        else return false;
    }
}
