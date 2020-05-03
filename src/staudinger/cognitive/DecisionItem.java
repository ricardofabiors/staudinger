/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package staudinger.cognitive;

import eps.MRA;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.FSMBehaviour;

/**
 *
 * @author Fábio Ricardo
 */
public class DecisionItem implements Item {
    private PlanItem decision, choice0, choice1;
    private MRA requester;

    public DecisionItem(PlanItem decision, PlanItem choice0, PlanItem choice1) {
        this.decision = decision;
        this.choice0 = choice0;
        this.choice1 = choice1;
        this.requester = decision.getRequester();
    }
    
    @Override
    public Behaviour execute(){
        FSMBehaviour myFSM = new FSMBehaviour(requester);
        
        //registra os nós que fazem parte da FSM
        myFSM.registerFirstState(this.decision.execute(), "decision");
        myFSM.registerLastState(this.choice0.execute(), "choice0");
        myFSM.registerLastState(this.choice1.execute(), "choice1");
        
        //registra as transições
        myFSM.registerTransition("decision", "choice0", 0);
        myFSM.registerTransition("decision", "choice1", 1);
        
        return myFSM; 
    }
    
}
