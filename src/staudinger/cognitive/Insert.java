/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package staudinger.cognitive;

import eps.MRAInfo;
import eps.Product;
import eps.SkillTemplate;
import eps.Util;
import eps.YPAException;
import eps.YPAServices;
import eps.ontology.EPSOntology;
import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 * @author Fábio Ricardo
 */
public class Insert extends Product{
    public static final int UP = 1;
    public static final int DOWN = 0;
    public static final int LEFT = 2;
    public static final int RIGHT = 3;
    
    protected int requestedColor;
    protected Box myBox;
    private Plan myPlan;
    
    public Insert(int color) {
        this.requestedColor = color;
        this.myPlan = new Plan(this);
    }
    
    public Insert() {
        this(Box.UNKNOWN);
    }
        
    @Override
    protected void setup() {
        //permite que a cor do caixote seja passada pelo argumento do agente
        if(requestedColor == Box.UNKNOWN){
            Object[] args = getArguments();
            if(args != null){
                this.requestedColor = Integer.parseInt((String) args[0]);
            }
        }
        defaultSetup(); 
        produce();
    }
    
    @Override
    protected void produce(){
        try {
            createPlan();
        } catch (YPAException ex) {
            Logger.getLogger(Insert.class.getName()).log(Level.SEVERE, null, ex);
        }
        executePlan();
    }
    
    private void createPlan() throws YPAException{
        //rotate conveyor 1 move o caixote para a conveyor 2
        SkillTemplate st = new SkillTemplate("move", "boolean", new String[]{"int"});
        st.addProperty("p1 to p2", "yes");
        st.addProperty("p1 to p11", "yes");
        st.setArgsValues(new String[]{"3"});
        MRAInfo[] mrainfos = YPAServices.search(this, st);    //solicita serviço de busca para o YPA
        myPlan.addNewPlanItem(mrainfos, st);    //adiciona novo item no plano de execução
        
        //conveyor 2 move o caixote para a destiny conveyor 1
        SkillTemplate st1 = new SkillTemplate("move", "boolean", new String[]{"int"});
        st1.addProperty("p11 to p12", "yes");
        st1.setArgsValues(new String[]{"1"});
        MRAInfo[] mrainfos1 = YPAServices.search(this, st1);    //solicita serviço de busca para o YPA
        myPlan.addNewPlanItem(mrainfos1, st1);    //adiciona novo item no plano de execução
        
        //destiny conveyor 1 recebe o caixote 
        SkillTemplate st2 = new SkillTemplate("receive", "boolean", new String[]{"int"});
        st2.addProperty("p9 to p10", "yes");
        st2.setArgsValues(new String[]{"2"});
        MRAInfo[] mrainfos2 = YPAServices.search(this, st2);    //solicita serviço de busca para o YPA
        myPlan.addNewPlanItem(mrainfos2, st2);    //adiciona novo item no plano de execução
    }
    
    private void executePlan(){
        myPlan.execute();
    }
    
    @Override
    protected MRAInfo getMRAInfo() {
        myMrainfo = new MRAInfo(); 
        myMrainfo.setAID(this.getLocalName());
        myMrainfo.setSkills(Util.fromSkill(getSkills()));
        return myMrainfo;
    }

    @Override
    public void doDelete() {
        super.doDelete(); 
    }
    
    @Override
    protected void takeDown() {
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.setOntology(EPSOntology.EPSONTOLOGYNAME);
        msg.setSender(getAID());
        msg.addReceiver(new AID("Gateway", AID.ISLOCALNAME));
        msg.setContent("Insert");
        send(msg);
        System.out.println(getLocalName() + ": msg INFORM enviada para o Gateway");
    }

}
