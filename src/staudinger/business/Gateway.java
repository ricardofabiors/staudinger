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
 * Classe que modela o agente Gateway da arquitetura EPSCore, que, por sua vez,
 * é reponsável por encabeçar o MAS como um todo e conectá-lo a outros sistemas.
 * Ao criar um agente dessa classe, o mesmo instancia os agentes que constituem
 * a arquitetura e provém métodos finais para a solicitação de um novo produto.
 * 
 * @author Fábio Ricardo
 */
public class Gateway extends Agent {
    
    private ContainerController containerController;
    private AgentController agentController;
    private Runtime runtime;
    
    private int registeredProducts;    //número de produtos requisitados até o momento
    
    /**
     * Construtor padrão da classe. Cria o container onde os agentes serão 
     * inseridos e seta a variável registeredProducts para 1, de modo que a 
     * primeira produção seja identificada como "Product1".
     */
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
        instantiateCognitiveLayerAgents();
        instantiateSuportLayerAgents();
        instantiatePhysicalLayerAgents();
        newProduction(Box.GREEN, 3);    //pedido teste de um caixote verde com 3 bolinhas
    }
    
    /**
     * Método que encapsula uma nova produção. É chamado sempre que o usuário 
     * final requisitar uma nova produção pela GUI (não desenvolvida ainda).
     * @param color Cor do caixote desejado no pedido.
     * @param quantity Quantidade de bolinhas desejadas no pedido.
     */
    protected void newProduction(int color, int quantity) {
        serveNewProduction(color, quantity, 0);
    }
    
    /**
     * Método servo (provisório) usado numa nova produção. É chamado no método 
     * "newProduction" com o parâmetro "my_try" igual a 0, indicando o ínicio 
     * de uma nova produção. O método também é chamado recursivamente, tentando 
     * concluir a produção que provavelmente foi impedida pela cor do caixote
     * retirado.
     * @param color Cor do caixote desejado no pedido.
     * @param quantity Quantidade de bolinhas desejadas no pedido.
     * @param my_try Número da tentativa de produção de um mesmo pedido.
     */
    private void serveNewProduction(int color, int quantity, int my_try) {
        addBehaviour(new OneShotBehaviour(this){
            @Override
            public void action() {
                String productName;
                if(my_try == 0){
                    productName = "Product" + (String.valueOf(registeredProducts));     //cria um nome para instanciar o agente com um número único
                    System.out.println(myAgent.getLocalName() + ": Serviço de nova produção requisitado: " + productName); 
                    registeredProducts++;
                }
                else{
                    productName = "Product" + (String.valueOf(registeredProducts - 1) + "." + String.valueOf(my_try + 1));     //cria um nome para instanciar o agente com um número único e ordenado de tentativa
                    System.out.println(myAgent.getLocalName() + ": Nova tentativa para a produção " +"Product" + (String.valueOf(registeredProducts - 1)) + " requisitada");  
                }
                //cria e instancia um novo agente para a produção
                NewOrder production;
                production = new NewOrder(color, quantity);                     
                instantiate(productName, production);
                
                //especifica um template para receber um feedback se a produção foi concluída
                MessageTemplate mt = MessageTemplate.and(
                    MessageTemplate.or(
                        MessageTemplate.MatchPerformative(ACLMessage.INFORM),
                        MessageTemplate.MatchPerformative(ACLMessage.FAILURE)),
                    MessageTemplate.MatchOntology(EPSOntology.EPSONTOLOGYNAME));
                ACLMessage msg = myAgent.blockingReceive(mt, 5000);
                
                //analisa a mensagem de feedback
                if (msg == null) {
                    System.out.println(myAgent.getLocalName() + ": Serviço de produção para " + productName + "demorou muito para responder");   
                } 
                else {
                    if (msg.getPerformative() == ACLMessage.FAILURE) {
                        System.out.println(myAgent.getLocalName() + ": Serviço de produção para " + productName + " falhou"); 
                    } 
                    else {
                        if("Insert".equals(msg.getContent())){
                            System.out.println(myAgent.getLocalName() + ": Serviço de produção para " + productName + " foi adiado, tentando novamente...");
                            serveNewProduction(color, quantity, (my_try + 1));
                        }
                        else if("Production".equals(msg.getContent())){
                            System.out.println(myAgent.getLocalName() + ": Serviço de produção feito com sucesso para " + productName);
                            //aliveProducts--;
                        }
                    }
                }
            }
        });   
    }
    
    /**
     * Método chamado no "setup" do agente para instanciar todos os agentes da
     * camada cognitiva utilizando o método "instanciate".
     */
    protected void instantiateSuportLayerAgents(){
        YPA ypa = new YPA();
        System.out.println(this.getLocalName() + ": Instanciando agentes da camada de suporte..."); 
        instantiate("Ypa", ypa);
    }
    
    /**
     * Método chamado no "setup" do agente para instanciar todos os agentes padrão da
     * camada cognitiva utilizando o método "instanciate".
     */
    protected void instantiateCognitiveLayerAgents(){
        Instantiator instantiator = new Instantiator();
        System.out.println(this.getLocalName() + ": Instanciando agentes da camada cognitiva...");
        instantiate("Instantiator", instantiator);
    }
    
    /**
     * Método chamado no "setup" do agente para instanciar todos os agentes da 
     * camada física utilizando o método "instanciate".
     */
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
    
    /**
     * Instancia um novo agente utilizando o container controller da classe.
     * @param nickname O nome do agente a ser instanciado.
     * @param agent O objeto agente a ser instanciado.
     */
    protected void instantiate(String nickname, Agent agent) {
        try {
            agentController = containerController.acceptNewAgent(nickname, agent);
            agentController.start();
        } catch (StaleProxyException ex) {
            Logger.getLogger(Gateway.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
