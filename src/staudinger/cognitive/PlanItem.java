/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package staudinger.cognitive;

import eps.MRA;
import eps.MRAInfo;
import eps.SkillTemplate;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;

/**
 *
 * @author Fábio Ricardo
 */
public class PlanItem implements Item{
    private MRAInfo[] executorsInfo;
    private SkillTemplate skill;
    private MRA requester;

    public PlanItem(MRAInfo[] executorsInfo, SkillTemplate skill, MRA requester) {
        this.setExecutorsInfo(executorsInfo);
        this.setSkill(skill);
        this.setRequester(requester);
    }
        
    public PlanItem(MRAInfo[] executorsInfo, SkillTemplate skill) {
        this(executorsInfo, skill, null);
    }
    
    @Override
    public Behaviour execute(){
        return requester.newRemoteExecuteBehaviour(executorsInfo, skill);
    }

    public MRAInfo[] getExecutorsInfo() {
        return executorsInfo;
    }

    public SkillTemplate getSkill() {
        return skill;
    }

    public MRA getRequester() {
        return requester;
    }

    public void setExecutorsInfo(MRAInfo[] executorsInfo) {
        this.executorsInfo = executorsInfo;
    }

    public void setSkill(SkillTemplate skill) {
        this.skill = skill;
    }
    
    public void setRequester(MRA requester) {
        this.requester = requester;
    }
}
