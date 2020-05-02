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
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Fábio Ricardo
 */
public class NewOrder extends Product{
    public static final int DOWN = 0;
    public static final int UP = 1;
    public static final int LEFT = 2;
    public static final int RIGHT = 3;
    
    protected int requestedColor;
    protected int requestedQuantity;
    private Plan myPlan;
    
    public NewOrder(int color, int quantity) {
        this.requestedColor = color;
        this.requestedQuantity = quantity;
        this.myPlan = new Plan(this);
    }
        
    @Override
    protected void setup() {
        defaultSetup(); 
        produce();
    }
    
    @Override
    protected void produce(){
        try {
            createPlan();
        } catch (YPAException ex) {
            Logger.getLogger(NewOrder.class.getName()).log(Level.SEVERE, null, ex);
        }
        executePlan();
    }
    
    private void createPlan() throws YPAException{
        //pega um novo caixote
        SkillTemplate st = new SkillTemplate("getNewBox", "boolean", new String[]{"void"});
        st.addProperty("p0 to p1", "yes");
        MRAInfo[] mrainfos = YPAServices.search(this, st);    //solicita serviço de busca para o YPA
        myPlan.addNewPlanItem(mrainfos, st);    //adiciona novo item no plano de execução
        
        //rotate conveyor recebe o caixote
        SkillTemplate st0 = new SkillTemplate("receive", "boolean", new String[]{"int"});
        st0.addProperty("from p1", "yes");
        st0.addProperty("from p11", "yes");
        st0.setArgsValues(new String[]{"1"});
        MRAInfo[] mrainfos0 = YPAServices.search(this, st0);    //solicita serviço de busca para o YPA
        myPlan.addNewPlanItem(mrainfos0, st0);    //adiciona novo item no plano de execução
        
        //rotate conveyor analisa a cor do caixote
        SkillTemplate st1 = new SkillTemplate("checkColor", "boolean", new String[]{"int"});
        st1.addProperty("from p1", "yes");
        st1.addProperty("from p11", "yes");
        st1.setArgsValues(new String[]{String.valueOf(requestedColor)});
        MRAInfo[] mrainfos1 = YPAServices.search(this, st1);    //solicita serviço de busca para o YPA
        PlanItem decision = myPlan.createNewPlanItem(mrainfos1, st1);    //adiciona novo item no plano de execução
        
        //possível instanciação de um agente do tipo Insert
        SkillTemplate st2 = new SkillTemplate("instantiate", "boolean", new String[]{"string", "string", "string"});
        st2.setArgsValues(new String[]{("Insert (from " + this.getLocalName() + ")"), "staudinger.cognitive.Insert", String.valueOf(requestedColor)});
        MRAInfo[] mrainfos2 = YPAServices.search(this, st2);    //solicita serviço de busca para o YPA
        PlanItem choice0 = myPlan.createNewPlanItem(mrainfos2, st2);    //adiciona novo item no plano de execução        
        
        //possível instanciação de um agente do tipo Production
        SkillTemplate st3 = new SkillTemplate("instantiate", "boolean", new String[]{"string", "string", "string"});
        st3.setArgsValues(new String[]{("Production (from " + this.getLocalName() + ")"), "staudinger.cognitive.Production", String.valueOf(requestedQuantity)});
        MRAInfo[] mrainfos3 = YPAServices.search(this, st3);    //solicita serviço de busca para o YPA
        PlanItem choice1 = myPlan.createNewPlanItem(mrainfos3, st3);    //adiciona novo item no plano de execução        
        
        myPlan.addNewDecisionItem(decision, choice0, choice1);    //adiciona novo item de decisão no plano de execução
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

}
