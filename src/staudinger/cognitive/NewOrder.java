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
 * Os serviços finais do sistema Staudinger geralmente dependem da cor do caixote.
 * Por isso, ao solicitar um pedido qualquer (no momento, somente a produção está
 * disponível) que necessite pegar um caixote da pilha e verificar sua cor, um 
 * agente de "pedido genérico" pegará o caixote e verificará sua cor. Atualmente, 
 * o agente "Gateway" intancia esse agente somente para produção. Esta classe 
 * descreve tal agente.
 * 
 * @author Fábio Ricardo
 */
public class NewOrder extends Product{
    public static final int DOWN = 0;
    public static final int UP = 1;
    public static final int LEFT = 2;
    public static final int RIGHT = 3;
    
    protected int requestedColor;        //cor requisitada (usado para produção)
    protected int requestedQuantity;     //quantidade de bolinhas requisitada (usado para inserção)
    private Plan myPlan;                 //plano de execução do agente
    
    /**
     * Construtor padrão da classe que recebe a cor e a quantidade de bolinhas.
     * @param color Cor requisitada do caixote.
     * @param quantity Quantidade de bolinhas requisitada.
     */
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
    
    /**
     * Implementação do método "Produce". Cria-se o plano chamando o método 
     * "createPlan" e, em seguida, o mesmo é executado ("executePlan").
     */
    @Override
    protected void produce(){
        try {
            createPlan();
        } catch (YPAException ex) {
            Logger.getLogger(NewOrder.class.getName()).log(Level.SEVERE, null, ex);
        }
        executePlan();
    }
    
    /**
     * Cria um plano de execução adicionando "PlanItem"s ao atributo "myPlan".
     * Atualmente, o agente "novo pedido" (ou "pedido genérico") conhece as 
     * skills necessárias para realizar sua função. Portanto, tais skills são 
     * definidas nos "SkillTemplate"s e em seguida são passadas como parâmetros
     * para uma busca dos MRAs capazes de executá-las. Posteriormente, essas 
     * listas de MRAs são passadas junto com os "SkillTemplate"s num método que
     * cria/adiciona um novo item ao plano de execução. O último item é um item 
     * de decisão, que permite que o agente possa solicitar uma instanciação (ao
     * "Instantiator") de um agente do tipo produção ou inserção.
     */
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
    
    /**
     * Executa o plano de execução "myPlan", que é atributo da classe. É chamado
     * no método produce.
     */
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
