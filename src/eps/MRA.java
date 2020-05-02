/*
 * Copyright (c) Andre Cavalcante 2008-2015
 * All right reserved
 */
package eps;

import eps.ontology.Execute;
import eps.ontology.EPSOntology;
import jade.content.ContentElement;
import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.OntologyException;
import jade.content.onto.basic.Action;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OntologyServer;
import jade.core.behaviours.WakerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import jade.proto.ContractNetInitiator;
import jade.proto.SSContractNetResponder;
import java.io.IOException;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class define one Mecatronic Agent. To create a mecatronic agent, create a
 * class extends MRA.
 *
 * @author andre
 */
public abstract class MRA extends Agent {

    private Behaviour autorunBeh;
    public final static String MRA_AGENT_NAME = "mra";
    
    protected Skill[] skills;
    protected MRAInfo myMrainfo;
    protected String cost = "1";
    protected boolean isBusy = false;
    
    public static final String GREEN = "\033[0;32m";
    public static final String RESET = "\u001B[0m";

    public MRA() {
    }

    /**
     * The Mecatronic Agent MUST implements this method to return its information
     * @return the MRAInfo from this Mecatronic Agent.
     */
    protected abstract MRAInfo getMRAInfo();

    /**
     * The Mecatronic Agent MUST implements this method to return an array
     * of the truly skills of this MRA.
     */
    protected abstract Skill[] getSkills();

    /**
     * This method executed after initialization father agent allowing son
     * initialization.
     */
    protected void init() {
    }

    /**
     * This method executed in OneShotBehaviour after initialization agent (end)
     * Automatic code executed.
     */
    protected void autorun() {
    }

    /**
     * Call to stop a CyclicBehaviour.
     */
    protected void stopAutorun() {
        removeBehaviour(autorunBeh);
    }

    @Override
    protected void setup() {
        defaultSetup();
        //Execute the autorun
        addBehaviour(new WakerBehaviour(this, 1000) {
            @Override
            public void onWake() {
                init();

                autorunBeh = new CyclicBehaviour(myAgent) {
                    @Override
                    public void action() {
                        autorun();
                    }
                };

                myAgent.addBehaviour(autorunBeh);
            }
        });
    }
    
    protected void defaultSetup() {
        //registry the language and ontology
        getContentManager().registerLanguage(new SLCodec());
        getContentManager().registerOntology(EPSOntology.instance());

        //Registry the MRA in YPA
        try {
            YPAServices.registry(this, getMRAInfo());
        } catch (YPAException ex) {
            System.out.println("YPA return with error. Exception: " + ex);
        }

        addBehaviour(new OntologyServer(this, EPSOntology.instance(), ACLMessage.REQUEST, this));
    }

    @Override
    protected void takeDown() {
    }

    /**
     * Execute a local skill with a remote call.
     *
     * @param exec
     * @param request
     */
    public void serveExecuteRequest(Execute exec, ACLMessage request) {
        ACLMessage msg = request.createReply();
        try {
            ContentElement ce = getContentManager().extractContent(request);
            if (ce instanceof Action) {
                Execute ex = (Execute) ((Action) ce).getAction();
                SkillTemplate st = ex.getSkillTemplate();
                boolean b = false;
                for (Skill sk : getSkills()) {
                    if (st.equals((SkillBase) sk)) {
                        sk.setArgsValues(st.getArgsValues());
                        sk.execute();
                        msg.setPerformative(ACLMessage.INFORM);
                        msg.setContent(sk.getResult());
                        b = true;
                        break;
                    }
                }
                if (!b) {
                    msg.setPerformative(ACLMessage.FAILURE);
                    msg.setContent("Skill not found!!!");
                }
            }
        } catch (Codec.CodecException | OntologyException | SkillExecuteException ex) {
            msg.setPerformative(ACLMessage.FAILURE);
            msg.setContent(ex.toString());
        }
        send(msg);
    }

    /**
     * Call this method to create a Initiator for an agent execute remotely
     *
     * @param mraInfo
     * @param st the template to execute in target agent
     * @return the return of execute skill;
     * @throws eps.SkillExecuteException Error
     *
     */
    public String executeRemoteSkill(MRAInfo mraInfo, SkillTemplate st) throws SkillExecuteException {
        
        String result = "";
        
        Execute ex = new Execute();
        ex.setMRAInfo(mraInfo);
        ex.setSkillTemplate(st);
        Action act = new Action(getAID(), ex);
        ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
        request.setSender(getAID());
        request.addReceiver(new AID(mraInfo.getAID(), false));
        request.setLanguage(new SLCodec().getName());
        request.setOntology(EPSOntology.EPSONTOLOGYNAME);
        try {
            getContentManager().fillContent(request, act);
            send(request);

            MessageTemplate mt = MessageTemplate.and(
                    MessageTemplate.or(
                            MessageTemplate.MatchPerformative(ACLMessage.INFORM),
                            MessageTemplate.MatchPerformative(ACLMessage.FAILURE)),
                    MessageTemplate.MatchOntology(EPSOntology.EPSONTOLOGYNAME));
            ACLMessage msg = this.blockingReceive(mt, 2000);
            if (msg == null) {
                throw new SkillExecuteException("Timeout Error in YPA.");
            } else {
                if (msg.getPerformative() == ACLMessage.FAILURE) {
                    throw new SkillExecuteException(msg.getContent());
                } else {
                    result = msg.getContent();
                }
            }
        } catch (Codec.CodecException | OntologyException ex1) {
            throw new SkillExecuteException("Error generating execute Skill. Exception: ", ex1);
        }
        
        return result;
    }

    protected void addResponderBehaviour() {
        //cria um template para especificar as mensagens que interessam
        MessageTemplate template = MessageTemplate.and(
            MessageTemplate.MatchProtocol("fipa-contract-net"),
            MessageTemplate.MatchPerformative(ACLMessage.CFP)
        );
        
        //adiciona um comportamento cíclico que permite adicionar comportamentos de "participante" simultâneos
        addBehaviour(new CyclicBehaviour(this) {
            @Override
            public void action() {
                ACLMessage cfp = myAgent.receive(template);
                if (cfp != null) {
                    myAgent.addBehaviour(new SSContractNetResponder(myAgent, cfp){
                        @Override
                        protected ACLMessage handleCfp(ACLMessage cfp) {
                            return serveHandleCfp(cfp);
                        }
                        @Override
                        protected ACLMessage handleAcceptProposal(ACLMessage cfp, ACLMessage propose, ACLMessage accept) {
                            return serveHandleAcceptProposal(cfp, propose, accept); 
                        }
                    });
                }
                else {
                    block();
                }
            }
        });
    }
    
    protected ACLMessage serveHandleCfp(ACLMessage cfp) {
        System.out.println(getLocalName() + ": Cfp recebido de " + cfp.getSender().getLocalName());
        ACLMessage reply = cfp.createReply();
        reply.setPerformative(ACLMessage.REFUSE);       //por default, o perfomativo é refuse
        try {                        
            Execute exc = (Execute) cfp.getContentObject();
            SkillTemplate requestedSkill = (SkillTemplate) exc.getSkillTemplate();
            System.out.println(getLocalName() + ": Extraindo skill");
            for (Skill skill : skills) {
                //verifica se a skill solicitada é válida e muda o perfomativo, se for o caso
                if ((Util.fromSkill(skill)).equals(requestedSkill)) { 
                    if (!isBusy) {
                        reply.setPerformative(ACLMessage.PROPOSE);
                        reply.setContent(cost);
                        System.out.println(getLocalName() + ": Skill coincidente");
                    }
                    else System.out.println(getLocalName() + ": Ocupado!");
                }
            }
        } catch (UnreadableException ex) {
            Logger.getLogger(MRA.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("ContentObject inválido.");
        }  
        System.out.println(getLocalName() + ": Propose enviada para " + cfp.getSender().getLocalName());
        return reply;
    }
    
    protected ACLMessage serveHandleAcceptProposal(ACLMessage cfp, ACLMessage propose, ACLMessage accept) {
        System.out.println(getLocalName() + ": Accept recebida de " + accept.getSender().getLocalName());
        ACLMessage reply = accept.createReply();
        try {                        
            Execute exc = (Execute) cfp.getContentObject();
            SkillTemplate requestedSkill = exc.getSkillTemplate();
            for (Skill sk : skills) {
                //verifica se a skill solicitada é válida e muda o perfomativo, se for o caso
                if ((Util.fromSkill(sk)).equals(requestedSkill)) { 
                    sk.setArgsTypes(requestedSkill.getArgsTypes());
                    sk.setArgsValues(requestedSkill.getArgsValues());
                    try {
                        sk.execute();
                        if("failed".equals(sk.getResult())) {
                            reply.setPerformative(ACLMessage.FAILURE);
                            reply.setContent("failed");
                        }
                        else {
                            reply.setPerformative(ACLMessage.INFORM);
                            reply.setContent(sk.getResult());
                        }
                    } catch (SkillExecuteException ex) {
                        Logger.getLogger(MRA.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        } catch (UnreadableException ex) {
            Logger.getLogger(MRA.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("ContentObject inválido.");
        }
        System.out.println(getLocalName() + ": Resposta enviada ao Accept de " + accept.getSender().getLocalName());
        return reply; 
    }

    public Behaviour newRemoteExecuteBehaviour(MRAInfo[] executers, SkillTemplate skill){
        Agent requester = this;
        Behaviour cfp_execution_beh = new ContractNetInitiator(requester, null){
            int onEnd_result;
            @Override
            protected Vector prepareCfps(ACLMessage cfp) {
                Vector v = servePrepareCfps(requester, cfp, executers, skill);
                return v;
            }
            
            @Override
            protected void handleAllResponses(Vector responses, Vector acceptances) {
                serveHandleAllResponses(responses, acceptances, requester.getLocalName());
            }
            
            @Override
            protected void handleInform(ACLMessage inform) {                
                onEnd_result = serveHandleInform(inform, requester.getLocalName());
            }  
            
            @Override
            public int onEnd(){
                return onEnd_result;
            }
        };
        
        return cfp_execution_beh;
    }
    
    protected Vector servePrepareCfps(Agent requester, ACLMessage cfp, MRAInfo[] mrainfos, SkillTemplate skill){
        System.out.println(requester.getLocalName() + ": Preparando cfp");
        Vector v = new Vector();
        
        for (MRAInfo mrainfo : mrainfos){
            cfp = new ACLMessage(ACLMessage.CFP);
            cfp.setProtocol("fipa-contract-net");
            String executerName = mrainfo.getAID();
            cfp.addReceiver(new AID(executerName, AID.ISLOCALNAME));
            Execute exc = new Execute();
            exc.setMRAInfo(mrainfo);
            exc.setSkillTemplate(skill);

            try {
                cfp.setContentObject(exc);
            } catch (IOException ex) {
                Logger.getLogger(MRA.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            v.add(cfp);
        }
        return v;
    }
    
    protected void serveHandleAllResponses(Vector responses, Vector acceptances, String requesterName){
        ACLMessage bestPropose = null;
        int bestCost = 1000;        //infinito
        System.out.println(requesterName + ": Lendo proposes");
        for (int i = 0; i < responses.size(); ++i) {
            ACLMessage propose = (ACLMessage) responses.get(i);
            if (propose.getPerformative() == ACLMessage.PROPOSE) {
                int executer_cost = Integer.parseInt(propose.getContent());
                System.out.println(requesterName + ": Custo de " + executer_cost + " para o " + propose.getSender().getLocalName());
                if (bestPropose == null || executer_cost < bestCost) {
                    bestPropose = propose;
                    bestCost = executer_cost;
                }
            }
        }
        if (bestPropose != null) {
            ACLMessage accept = bestPropose.createReply();
            accept.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
            accept.setContent("accept");
            acceptances.add(accept);
            System.out.println(requesterName + ": Accept enviado para " + bestPropose.getSender().getLocalName());
        }
    }
    
    protected int serveHandleInform(ACLMessage inform, String requesterName) {                
        switch (inform.getContent()) {
            case "true":
                System.out.println(inform.getSender().getLocalName() + ": Resultado da skill solicitada -> true");
                System.out.println(MRA.GREEN + inform.getSender().getLocalName() + ": Skill realizada com sucesso para o agente " + requesterName + RESET);
                return 1;
            case "false":
                System.out.println(inform.getSender().getLocalName() + ": Resultado da skill solicitada -> false");
                System.out.println(MRA.GREEN + inform.getSender().getLocalName() + ": Skill realizada com sucesso para o agente " + requesterName + RESET);
                return 0;
            default:
                return 2;
        }
    }

}
