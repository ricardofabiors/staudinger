/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package staudinger.cognitive;

import eps.MRA;
import eps.MRAInfo;
import eps.Skill;
import eps.SkillExecuteException;
import eps.Util;
import jade.core.ProfileImpl;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Fábio Ricardo
 */
public class Instantiator extends MRA{
    
    @Override
    protected void setup(){
        defaultSetup();
        addResponderBehaviour();
    }

    public Instantiator() {
        this.skills = new Skill[] {instantiate};
    }
    
    @Override
    protected MRAInfo getMRAInfo() {
        myMrainfo = new MRAInfo(); 
        myMrainfo.setAID(this.getLocalName());
        myMrainfo.setSkills(Util.fromSkill(getSkills()));
        return myMrainfo;
    }
    
    public Skill instantiate = new Skill(this, "instantiate", "boolean", new String[]{"string", "string", "string"}){
        @Override
        public void execute() throws SkillExecuteException {
            isBusy = true;
            String nickname = this.getArgsValues()[0];
            String className = this.getArgsValues()[1];
            String argument1 = this.getArgsValues()[2];
            Behaviour beh = new OneShotBehaviour(myMRA){
                @Override
                public void action() {
                    ContainerController containerController;
                    AgentController agentController;
                    jade.core.Runtime runtime;
                    runtime = jade.core.Runtime.instance();
                    containerController = runtime.createAgentContainer(new ProfileImpl(false));
                    try {
                        Thread.sleep(500);
                        agentController = containerController.createNewAgent(nickname, className, new String[]{argument1});
                        agentController.start();
                    } catch (StaleProxyException | InterruptedException ex) {
                        Logger.getLogger(Instantiator.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            };
            myMRA.addBehaviour(beh);
            result = "true"; 
            isBusy = false;
        }
    };
    
    @Override
    protected Skill[] getSkills() {
        return this.skills;
    }
}
