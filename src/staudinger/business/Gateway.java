/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package staudinger.business;

import eps.YPA;
import eps.ontology.EPSOntology;
import jade.core.Agent;
import jade.core.Runtime;
import staudinger.cognitive.*;
import staudinger.physical.*;
import jade.core.ProfileImpl;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Fábio Ricardo
 */
public class Gateway extends Agent {
    
    private ContainerController containerController;
    private AgentController agentController;
    private Runtime runtime;
    
    private int registeredProducts;
    
    public Gateway() {
        registeredProducts = 1;
        try {
            runtime = jade.core.Runtime.instance();
            containerController = runtime.createAgentContainer(new ProfileImpl(false));
            Thread.sleep(500);
        } catch (InterruptedException ex) {
            Logger.getLogger(Gateway.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @Override
    protected void setup() {
        instantiateSuportLayerAgents();
        instantiateCognitiveLayerAgents();
        instantiatePhysicalLayerAgents();
        newProduction(Box.GREEN, 3);    //pedido de um caixote verde com 3 bolinhas
    }
    
    public void newProduction(int color, int quantity) {
        serveNewProduction(color, quantity, 0);
    }
    
    public void serveNewProduction(int color, int quantity, int my_try) {
        addBehaviour(new OneShotBehaviour(this){
            @Override
            public void action() {
                String productName;
                if(my_try == 0){
                    productName = "Product" + (String.valueOf(registeredProducts));     //cria um nome para instanciar o agente com o número correto
                    System.out.println(myAgent.getLocalName() + ": Serviço de nova produção requisitado"); 
                    registeredProducts++;
                }
                else{
                    productName = "Product" + (String.valueOf(registeredProducts - 1) + "." + String.valueOf(my_try));     //cria um nome para instanciar o agente com o número correto
                    System.out.println(myAgent.getLocalName() + ": Nova tentativa para a produção requisitada");  
                }
                
                NewOrder production;
                production = new NewOrder(color, quantity);                     
                instantiate(productName, production);

                MessageTemplate mt = MessageTemplate.and(
                    MessageTemplate.or(
                        MessageTemplate.MatchPerformative(ACLMessage.INFORM),
                        MessageTemplate.MatchPerformative(ACLMessage.FAILURE)),
                    MessageTemplate.MatchOntology(EPSOntology.EPSONTOLOGYNAME));

                ACLMessage msg = myAgent.blockingReceive(mt, 3000);
                if (msg == null) {
                    System.out.println(myAgent.getLocalName() + ": Serviço de nova produção demorou muito para responder");   
                } 
                else {
                    if (msg.getPerformative() == ACLMessage.FAILURE) {
                        System.out.println(myAgent.getLocalName() + ": Serviço de nova produção falhou"); 
                    } 
                    else {
                        if("Insert".equals(msg.getContent())){
                            System.out.println(myAgent.getLocalName() + ": Serviço de nova produção foi adiado, tentando novamente...");
                            serveNewProduction(color, quantity, (my_try + 1));
                        }
                        else if("Production".equals(msg.getContent())){
                            System.out.println(myAgent.getLocalName() + ": Serviço de nova produção foi feito com sucesso!");
                            //aliveProducts--;
                        }
                    }
                }
            }
        });   
    }
    
    protected void instantiateSuportLayerAgents(){
        System.out.println(this.getLocalName() + ": Instanciando agentes da camada de suporte..."); 
        YPA ypa = new YPA();
        instantiate("Ypa", ypa);
    }
    
    protected void instantiateCognitiveLayerAgents(){
        System.out.println(this.getLocalName() + ": Instanciando agentes da camada cognitiva...");
        Instantiator instantiator = new Instantiator();
        instantiate("Instantiator", instantiator);
    }
    
    protected void instantiatePhysicalLayerAgents() {
        System.out.println(this.getLocalName() + ": Instanciando agentes da camada física...");
        StorageConveyor storage;
        storage = new StorageConveyor("p0 to p1");
        instantiate("StorageConveyor", storage);
        
        RotateConveyor rotate1;
        rotate1 = new RotateConveyor("p1 to p2", "p1 to p11", "from p1", "from p11");
        instantiate("RotateConveyor1", rotate1);
        
        RotateConveyor rotate2;
        rotate2 = new RotateConveyor("p3 to p4", "p3 to p6", "from p3", "from p6");
        instantiate("RotateConveyor2", rotate2);
        
        Conveyor conveyor1;
        conveyor1 = new Conveyor("p2 to p3");
        instantiate("Conveyor1", conveyor1);
        
        Conveyor conveyor2;
        conveyor2 = new Conveyor("p11 to p12");
        instantiate("Conveyor2", conveyor2);
        
        ResourceConveyor resourceconv;
        resourceconv = new ResourceConveyor("p6 to p7", "p6 to p8", "p7 to p8", "p8 to p9");
        instantiate("ResourceConveyor", resourceconv);
        
        MachineTool machinetool;
        machinetool = new MachineTool("p8");
        instantiate("MachineTool", machinetool);
        
        PneumaticPicking pneumatic;
        pneumatic = new PneumaticPicking("p7");
        instantiate("PneumaticPicking", pneumatic);
        
        DestinyConveyor destiny1;
        destiny1 = new DestinyConveyor("p12 to p13");
        instantiate("DestinyConveyor1", destiny1);
        
        DestinyConveyor destiny2;
        destiny2 = new DestinyConveyor("p9 to p10");
        instantiate("DestinyConveyor2", destiny2);
    }
    
    protected void instantiate(String nickname, Agent agent) {
        try {
            agentController = containerController.acceptNewAgent(nickname, agent);
            agentController.start();
        } catch (StaleProxyException ex) {
            Logger.getLogger(Gateway.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
