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
import jade.lang.acl.ACLMessage;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Esta classe descreve uma produção dentro do sistema. Os agentes objetos dela 
 * representam uma produção de fato e são responsáveis por levar tais caixotes 
 * à "DestinyConveyor" adequada, passando pelo inseridor de bolinhas e pelo
 * inseridor de tampa. Na descrição atual do sistema, esse destino pode ser 
 * visto como um só, o que permite que tais agentes, em seus planos de execução,
 * saibam as skills que deverão ser solicitadas, assim como a ordem de execução
 * das mesmas. 
 * 
 * @author Fábio Ricardo
 */
public class Production extends Product{
    //"direções" ou destinos
    public static final int UP = 1;
    public static final int DOWN = 0;
    public static final int LEFT = 2;
    public static final int RIGHT = 3;
    public static final int R1 = 0;
    public static final int R2 = 1;
    public static final int THE_END = 2;   
    
    protected int requestedQuantity;    //quantidade de bolinhas requisitada 
    protected Box myBox;                //caixote atual atrelado ao agente (atributo não utilizado até então)
    private Plan myPlan;                //plano de execução do agente
    
    /**
     * Construtor da classe que recebe a quantidade de bolinhas.
     * @param quantity Quantidade de bolinhas requisitada.
     */
    public Production(int quantity) {
        this.requestedQuantity = quantity;
        this.myPlan = new Plan(this);
    }
    
    /**
     * Construtor padrão da classe. É passado "UNKNOWN" para a quantidade que será 
     * atribuída somente no método "setup" do agente, uma vez que esse será 
     * instanciado pelo agente "Instantiator" como resultado de uma skill.
     */ 
    public Production() {
        this(Box.UNKNOWN);
    }
    
    /**
     * Implementação do método "setup" do agente. Aqui é verificado através do 
     * "if" se a instanciação do agente foi feita pelo "Instantiator" ou "Gateway". 
     * Essas instanciações são diferentes porque o "Gateway" adiciona ao container
     * (método "acceptNewAgent") um agente cuja a classe foi inicializada através 
     * do construtor que recebe a quantidade, enquanto o "Instantiator" cria de fato o
     * agente através de um AgentController, e, portanto, recebe a quantidade de 
     * bolinhas pelos argumentos passados na função "createNewAgent".
     */    
    @Override
    protected void setup() {
        //permite que a quantidade de bolinhas do caixote seja passada pelo argumento do agente
        if(requestedQuantity == Box.UNKNOWN){
            Object[] args = getArguments();
            if(args != null){
                this.requestedQuantity = Integer.parseInt((String) args[0]);
            }
        }
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
            Logger.getLogger(Production.class.getName()).log(Level.SEVERE, null, ex);
        }
        executePlan();
    }
    
    /**
     * Cria um plano de execução adicionando "PlanItem"s ao atributo "myPlan".
     * Atualmente, o agente produção conhece as skills necessárias para chegar
     * aos destinos adequados. Portanto, tais skills são definidas nos "SkillTemplate"s
     * e em seguida são passadas como parâmetros para uma busca dos MRAs capazes
     * de executá-las. Posteriormente, essas listas de MRAs são passadas junto com 
     * os "SkillTemplate"s num método que cria/adiciona um novo item ao plano
     * de execução.
     */
    private void createPlan() throws YPAException{
        //rotate conveyor 1 move o caixote para a conveyor 1
        SkillTemplate st = new SkillTemplate("move", "boolean", new String[]{"int"});
        st.addProperty("p1 to p2", "yes");
        st.addProperty("p1 to p11", "yes");
        st.setArgsValues(new String[]{"0"});
        MRAInfo[] mrainfos = YPAServices.search(this, st);    //solicita serviço de busca para o YPA
        myPlan.addNewPlanItem(mrainfos, st);    //adiciona novo item no plano de execução
        
        //conveyor 1 move o caixote para a rotate conveyor 2
        SkillTemplate st1 = new SkillTemplate("move", "boolean", new String[]{"int"});
        st1.addProperty("p2 to p3", "yes");
        st1.setArgsValues(new String[]{"1"});
        MRAInfo[] mrainfos1 = YPAServices.search(this, st1);    //solicita serviço de busca para o YPA
        myPlan.addNewPlanItem(mrainfos1, st1);    //adiciona novo item no plano de execução
        
        //rotate conveyor 2 recebe o caixote 
        SkillTemplate st2 = new SkillTemplate("receive", "string", new String[]{"int"});
        st2.addProperty("from p3", "yes");
        st2.addProperty("from p6", "yes");
        st2.setArgsValues(new String[]{"1"});
        MRAInfo[] mrainfos2 = YPAServices.search(this, st2);    //solicita serviço de busca para o YPA
        myPlan.addNewPlanItem(mrainfos2, st2);    //adiciona novo item no plano de execução
        
        //rotate conveyor 2 move o caixote para a resource conveyor
        SkillTemplate st3 = new SkillTemplate("move", "boolean", new String[]{"int"});
        st3.addProperty("p3 to p4", "yes");
        st3.addProperty("p3 to p6", "yes");
        st3.setArgsValues(new String[]{"3"});
        MRAInfo[] mrainfos3 = YPAServices.search(this, st3);    //solicita serviço de busca para o YPA
        myPlan.addNewPlanItem(mrainfos3, st3);    //adiciona novo item no plano de execução
        
        //resource conveyor move o caixote para a posição do pneumatic picking
        SkillTemplate st4 = new SkillTemplate("move", "boolean", new String[]{"int"});
        st4.addProperty("p6 to p7", "yes");
        st4.addProperty("p6 to p8", "yes");
        st4.addProperty("p7 to p8", "yes");
        st4.addProperty("p8 to p9", "yes");
        st4.setArgsValues(new String[]{"0"});
        MRAInfo[] mrainfos4 = YPAServices.search(this, st4);    //solicita serviço de busca para o YPA
        myPlan.addNewPlanItem(mrainfos4, st4);    //adiciona novo item no plano de execução
        
        //pneumatic picking insere as bolinhas requisitadas
        MRAInfo mrainfo5 = new MRAInfo();
        mrainfo5.setAID("PneumaticPicking");
        SkillTemplate st5 = new SkillTemplate("insert", "boolean", new String[]{"int"});
        st5.addProperty("p7", "yes");
        st5.setArgsValues(new String[]{String.valueOf(requestedQuantity)});
        MRAInfo[] mrainfos5 = YPAServices.search(this, st5);    //solicita serviço de busca para o YPA
        myPlan.addNewPlanItem(mrainfos5, st5);    //adiciona novo item no plano de execução
        
        //resource conveyor move o caixote para a posição do machine tool
        SkillTemplate st6 = new SkillTemplate("move", "boolean", new String[]{"int"});
        st6.addProperty("p6 to p7", "yes");
        st6.addProperty("p6 to p8", "yes");
        st6.addProperty("p7 to p8", "yes");
        st6.addProperty("p8 to p9", "yes");
        st6.setArgsValues(new String[]{"1"});
        MRAInfo[] mrainfos6 = YPAServices.search(this, st6);    //solicita serviço de busca para o YPA
        myPlan.addNewPlanItem(mrainfos6, st6);    //adiciona novo item no plano de execução
        
        //machine tool tampa o caixote
        SkillTemplate st7 = new SkillTemplate("cover", "boolean", new String[]{"void"});
        st7.addProperty("p8", "yes");
        st7.setArgsValues(new String[]{""});
        MRAInfo[] mrainfos7 = YPAServices.search(this, st7);    //solicita serviço de busca para o YPA
        myPlan.addNewPlanItem(mrainfos7, st7);    //adiciona novo item no plano de execução
        
        //resource conveyor move o caixote para a posição final
        SkillTemplate st8 = new SkillTemplate("move", "boolean", new String[]{"int"});
        st8.addProperty("p6 to p7", "yes");
        st8.addProperty("p6 to p8", "yes");
        st8.addProperty("p7 to p8", "yes");
        st8.addProperty("p8 to p9", "yes");
        st8.setArgsValues(new String[]{"2"});
        MRAInfo[] mrainfos8 = YPAServices.search(this, st8);    //solicita serviço de busca para o YPA
        myPlan.addNewPlanItem(mrainfos8, st8);    //adiciona novo item no plano de execução
        
        //destiny conveyor 2 recebe o caixote 
        SkillTemplate st9 = new SkillTemplate("receive", "boolean", new String[]{"int"});
        st9.addProperty("p9 to p10", "yes");
        st9.setArgsValues(new String[]{"2"});
        MRAInfo[] mrainfos9 = YPAServices.search(this, st9);    //solicita serviço de busca para o YPA
        myPlan.addNewPlanItem(mrainfos9, st9);    //adiciona novo item no plano de execução        
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
    
    /**
     * Sobrescreve o método "takeDown" de "Agent" especificando que o agente 
     * "Production" avisará ao "Gateway" que houve uma produção.
     */
    @Override
    protected void takeDown() {
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.setOntology(EPSOntology.EPSONTOLOGYNAME);
        msg.setSender(getAID());
        msg.addReceiver(new AID("Gateway", AID.ISLOCALNAME));
        msg.setContent("Production");
        send(msg);
        System.out.println(getLocalName() + ": msg INFORM enviada para o Gateway");
    }
}
