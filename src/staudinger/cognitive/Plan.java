/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package staudinger.cognitive;

import eps.MRA;
import eps.MRAInfo;
import eps.SkillTemplate;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.SequentialBehaviour;
import java.util.ArrayList;

/**
 *
 * @author Fábio Ricardo
 */
public class Plan {
    private SequentialBehaviour seqBehaviour;
    private MRA owner;
    private ArrayList plan;

    public Plan(MRA owner) {
        this.seqBehaviour = new SequentialBehaviour();
        this.owner = owner;
        this.plan = new ArrayList();
        this.owner.addBehaviour(this.seqBehaviour);
    }
    
    public void addNewPlanItem(MRAInfo[] mrainfos, SkillTemplate skill){
        Item newItem = createNewPlanItem(mrainfos, skill);
        plan.add(newItem);
    }
    
    public PlanItem createNewPlanItem(MRAInfo[] mrainfos, SkillTemplate skill){
        PlanItem newItem = new PlanItem(mrainfos, skill, owner);
        return newItem;
    }
    
    public void addNewDecisionItem(PlanItem decision, PlanItem choice1, PlanItem choice2){
        Item newItem = new DecisionItem(decision, choice1, choice2);
        plan.add(newItem);
    }
    
    public void execute(){
        for(int i = 0; i < plan.size(); i++){
            Behaviour executionBehaviour = ((Item) plan.get(i)).execute();
            this.seqBehaviour.addSubBehaviour(executionBehaviour);
        }
        //encerra o plano deletando o agente
        this.seqBehaviour.addSubBehaviour(new OneShotBehaviour(){
            @Override
            public void action() {
                owner.doDelete();
            }
        });
    }
}
