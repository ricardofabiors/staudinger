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

/**
 * Esta classe implementa um item num plano de execução (de um 
 * determinado agente produto).
 * 
 * @author Fábio Ricardo
 */
public class PlanItem implements Item{
    private MRAInfo[] executorsInfo;    //MRA que executará a skill   
    private SkillTemplate skill;        //skill a ser executada
    private MRA requester;              //agente pelo qual o item será executado

    /**
     * Construtor padrão da classe.
     * @param executorsInfo Lista de "MRAInfos" dos MRAs que podem executar a skill.
     * @param skill "SkillTemplate" que define a skill necessitada.
     * @param requester Agente que precisa do serviço (skill).
     */
    public PlanItem(MRAInfo[] executorsInfo, SkillTemplate skill, MRA requester) {
        this.setExecutorsInfo(executorsInfo);
        this.setSkill(skill);
        this.setRequester(requester);
    }
    
    /**
     * Executa o item. 
     * @return O comportamento de execução pronto para ser adicionado em um
     * subcomportamento.
     */
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
