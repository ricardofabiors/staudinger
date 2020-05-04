/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package staudinger.physical;

import eps.MRA;
import eps.MRAInfo;
import eps.Skill;
import eps.SkillExecuteException;
import eps.Util;
import hardware.HardwareAcess;
import staudinger.cognitive.Box;

/**
 * Modela os módulos Rotate Conveyor Belt do demosntrador Staudinger e é responsável
 * por receber, mover e verificar a cor dos caixotes.
 * 
 * @author Fábio Ricardo
 */
public class RotateConveyor extends MRA{
    //"direções" (ou destinos)
    public static final int UP = 1;
    public static final int DOWN = 0;
    public static final int LEFT = 2;
    public static final int RIGHT = 3;
    
    protected int readColor = Box.BLACK;
    HardwareAcess acess = new HardwareAcess();
    
    /**
     * Construtor padrão da classe. Seta as propriedades da skill "move", "receive"
     * e "checkColor", além de adicioná-las ao vetor de skills.
     * @param to1 Indica o ponto final do recebimento realizado pela skill. 
     * Exemplo: "p1".
     * @param to2
     * @param from1
     * @param from2
     */
    public RotateConveyor(String to1, String to2, String from1, String from2){
        move.addProperty(to1, "yes");
        move.addProperty(to2, "yes");
        receive.addProperty(from1, "yes");
        receive.addProperty(from2, "yes");
        checkColor.addProperty(from1, "yes");
        checkColor.addProperty(from2, "yes");
        this.skills = new Skill[] {move, receive, checkColor, stop};
    }
    
    @Override
    protected void setup(){
        defaultSetup();
        addResponderBehaviour();
    }
    
    protected Skill move = new Skill(this, "move", "boolean", new String[]{"int"}){
        @Override
        public void execute() throws SkillExecuteException {
            isBusy = true;
            int direction = Integer.parseInt(getArgsValues()[0]);
            
            result = acess.move(myMRA, direction);
            
            isBusy = false;
        }
    };
    
    public Skill receive = new Skill(this, "receive", "boolean", new String[]{"int"}){
        @Override
        public void execute() throws SkillExecuteException {
            isBusy = true;
            int direction = Integer.parseInt(getArgsValues()[0]);
            switch (direction) {
                case UP:
                    System.out.println(this.myMRA.getLocalName() + ": Recebendo de cima..."); 
                    result = "true";
                    break;
                case RIGHT:
                    System.out.println(this.myMRA.getLocalName() + ": Recebendo da direita..."); 
                    result = "true";
                    break;
                default:
                    System.out.println(this.myMRA.getLocalName() + ": Direção inválida.");
                    result = "false"; 
                    break;
            }
            isBusy = false;
        }
    };
    
    public Skill checkColor = new Skill(this, "checkColor", "boolean", new String[]{"int"}){
        @Override
        public void execute() throws SkillExecuteException {
            isBusy = true;
            int desiredColor = Integer.parseInt(getArgsValues()[0]);
            
            result = acess.checkColor(myMRA, desiredColor);
            
            isBusy = false;
        }
    };
    
    protected Skill stop = new Skill(this, "stop", "boolean", new String[]{"void"}){
        @Override
        public void execute() throws SkillExecuteException {
            System.out.println(this.myMRA.getLocalName() + ": Minha esteira parou.");         
        }
    };
    
    @Override
    protected MRAInfo getMRAInfo() {
        myMrainfo = new MRAInfo(); 
        myMrainfo.setAID(this.getLocalName());
        myMrainfo.setSkills(Util.fromSkill(getSkills()));
        return myMrainfo;
    }
    
    @Override
    protected Skill[] getSkills() {
        return this.skills;
    }  
}
