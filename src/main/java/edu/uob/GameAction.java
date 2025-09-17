package edu.uob;

import java.util.*;

public class GameAction{
    private LinkedList<String> triggers;
    private LinkedList<String> subjects;
    private LinkedList<String> consumed;
    private LinkedList<String> produced;
    private String narration;

    public GameAction(){
        this.triggers = new LinkedList<>();
        this.subjects = new LinkedList<>();
        this.consumed = new LinkedList<>();
        this.produced = new LinkedList<>();
    }

    public void addTrigger(String trigger){
        this.triggers.add(trigger);
    }

    public void addSubject(String subject){
        this.subjects.add(subject);
    }

    public void addConsumed(String consumed){
        this.consumed.add(consumed);
    }

    public void addProduced(String produced){
        this.produced.add(produced);
    }

    public void setNarration(String narration){
        this.narration = narration;
    }

    public LinkedList<String> getTriggers(){
        return this.triggers;
    }

    public LinkedList<String> getSubjects(){
        return this.subjects;
    }

    public String getNarration(){
        return this.narration;
    }

    public LinkedList<String> getConsumed(){
        return this.consumed;
    }

    public LinkedList<String> getProduced(){
        return this.produced;
    }
}
