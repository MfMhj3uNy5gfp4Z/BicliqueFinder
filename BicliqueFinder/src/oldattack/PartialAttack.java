/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package oldattack;

import core.Cipher;
import oldattack.Attack;
import core.Util;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Frog33
 */
public class PartialAttack<T extends Cipher> implements Runnable{
    
    private int id;
    private boolean finished;
    private int amount;
    private int begin;
    private int end;
    private int internalBegin;
    private int internalEnd;
    private T cipher1;
    private T cipher2;
    private String logName;
    public int partials[];
    public LinkedList<Attack<T>> ataques;
    private int nablaKey;
    private List<Integer[]> deltaActive;
    private List<Integer[]> nablaActive;
    
    public PartialAttack(int id, int amount, int partials[], T cipher1, T cipher2) {
        this(amount, id, id*amount, (id+1)*amount, 0, 12, cipher1.getNUM_KEYS()-1, partials, cipher1, cipher2);
    }
    
    public PartialAttack(int total, int id, int begin, int end, int partials[], T cipher1, T cipher2) {
        this(total, id, begin, end, 0, 12, cipher1.getNUM_KEYS()-1, partials, cipher1, cipher2);
    }
    
    public PartialAttack(int total, int id, int nablaKey, int begin, int end, int partials[], T cipher1, T cipher2) {
        this(total, id, begin, end, 0, 12, cipher1.getNUM_KEYS()-1, partials, cipher1, cipher2);
    }

    public PartialAttack(int total, int id, int begin, int end, int internalBegin, int internalEnd, int nablaKey, int partials[], T cipher1, T cipher2) {
        this.id = id;
        this.nablaKey = nablaKey;
        this.partials = partials;
        this.amount = end-begin;
        this.begin = begin;
        this.end = end;
        this.internalBegin = internalBegin;
        this.internalEnd = internalEnd;
        this.cipher1 = cipher1;
        this.cipher2 = cipher2;
        this.logName = "logfile_" + id + ".log";
        nablaActive = new LinkedList<>();
        deltaActive = new LinkedList<>();
        Integer aux[];
        for (int j1 = 0; j1 < cipher1.getBLOCK_SIZE_IN_WORDS(); j1++) {
            for (int j2 = j1+4; j2 < 16; j2+=4){ 
                aux = new Integer[2];
                aux[0] = j1;
                aux[1] = j2;
                deltaActive.add(aux);
            }
        }
        
        for (int j = 0; j < cipher1.getBLOCK_SIZE_IN_WORDS(); j++) {
            aux = new Integer[1];
            aux[0] = j;
            nablaActive.add(aux);
        }
        
        List<Integer[]> lista[] = Util.partition(nablaActive, total);
        nablaActive = lista[id];
        
        System.out.println( "Deltas: " + Arrays.deepToString(deltaActive.toArray())+
                            "\nNablas: "+Arrays.deepToString(nablaActive.toArray()));
        
    }
    
    private void AESSpecific(){
        int []rowToColumn = {   0,  4,  8,  12,
                                1,  5,  9,  13,
                                2,  6, 10,  14,
                                3,  7, 11,  15};
        ataques = new LinkedList<>();
        Attack<T> a;
        int iniDelta[] = new int[deltaActive.get(0).length];
        int iniNabla[] = new int[nablaActive.get(0).length];
        int keyDelta, keyNabla;
        System.out.println("begin: "+begin+" end: "+end);
        int counter = 0;
        
        // i is the round of nabla
        for (int i = begin; i < end; i++) {
            keyDelta = i;
            keyNabla = nablaKey;
            // j is the word activated for nabla
            for (Integer[] j : nablaActive) {
                for (int m = 0; m < iniNabla.length; m++)
                    iniNabla[m] = rowToColumn[j[m]];
                //iniNabla[1] = rowToColumn[j[1]];
                // k is the word activated for delta
                for (Integer[] k : deltaActive) {
                    for (int m = 0; m < iniDelta.length; m++)
                        iniDelta[m] = rowToColumn[k[m]];
                    System.out.println("cipher1 : "+cipher1+", cipher2 : "+ cipher2+", roundDelta : " +keyDelta+ 
                            ", roundNabla : "+keyNabla+", initialWordsDelta : " +Arrays.toString(iniDelta)+", initialWordsNabla : " +Arrays.toString(iniNabla));
                    a = new Attack<>(cipher1, cipher2, keyDelta, keyNabla, iniDelta, iniNabla, true);
                    try {
                        a.applyAttack(false);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    if(a.isIndependent()){
                        ataques.add(a);
                        System.out.println(a);
                    }
                    
                }
                System.out.println("id: "+id+" completion: "+(((double)++counter)/nablaActive.size()));
            }
            partials[id]++;
            System.out.println("id: "+id+" partials  : "+partials[id]);
        }
        System.out.println("FINISHED id: "+id);
        finished = true;
    }
    
    private void AESSpecificOriginal(){
        int []rowToColumn = {   0,  4,  8,  12,
                                1,  5,  9,  13,
                                2,  6, 10,  14,
                                3,  7, 11,  15};
        ataques = new LinkedList<>();
        Attack<T> a;
        int iniDelta[] = new int[2];
        int iniNabla[] = new int[2];
        int keyDelta, keyNabla;
        System.out.println("begin: "+begin+" end: "+end);
        
        // i is the round of nabla
        for (int i = begin; i < end; i++) {
            keyDelta = cipher1.getNUM_KEYS()-1;
            keyNabla = i;
            // j is the word activated for nabla
            for (int j1 = internalBegin; j1 < internalEnd; j1++) {
                for (int j2 = j1+4; j2 < 16; j2+=4){ 
                    iniNabla[0] = rowToColumn[j1];
                    iniNabla[1] = rowToColumn[j2];
                    // k is the word activated for delta
                    for (int k1 = 0; k1 < 12; k1++) {
                        for (int k2 = k1+4; k2 < 16; k2+=4) {
                            iniDelta[0] = rowToColumn[k1];
                            iniDelta[1] = rowToColumn[k2];
                            System.out.println("cipher1 : "+cipher1+", cipher2 : "+ cipher2+", roundDelta : " +keyDelta+ 
                                    ", roundNabla : "+keyNabla+", initialWordsDelta : " +Arrays.toString(iniDelta)+", initialWordsNabla : " +Arrays.toString(iniNabla));
                            a = new Attack<>(cipher1, cipher2, keyDelta, keyNabla, iniDelta, iniNabla, true);
                            try {
                                a.applyAttack(false);
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                            if(a.isIndependent()){
                                ataques.add(a);
                                System.out.println(a);
                            }
//                            System.out.println("id: "+id+" i: "+i+" j: "+j+" k: "+k);
                        }
                    }
                }
                System.out.println("id: "+id+" completion: "+(100*(j1)/(12.0)));
            }
            partials[id]++;
            System.out.println("id: "+id+" partials  : "+partials[id]);
        }
        System.out.println("FINISHED id: "+id);
        finished = true;
    }
    
    private void genericBalanced(){
        ataques = new LinkedList<>();
        Attack<T> a;
        int iniDelta[] = new int[1];
        int iniNabla[] = new int[1];
        System.out.println("begin: "+begin+" end: "+end);
        // i is the round of nabla
        for (int i = begin; i < end; i++) {
            // j is the word activated for nabla
            for (int j = 0; j < cipher1.getNUM_WORDS_KEY_NABLA(); j++) {
                iniNabla[0] = j;
                // k is the word activated for delta
                for (int k = 0; k < cipher1.getNUM_WORDS_KEY_DELTA(); k++) {
                    iniDelta[0] = k;
                    System.out.println("cipher1 : "+cipher1+", cipher2 : "+ cipher2+", roundDelta : " +(cipher1.getNUM_KEYS()-1)+ 
                            ", roundNabla : "+i+", initialWordsDelta : " +Arrays.toString(iniDelta)+", initialWordsNabla : " +Arrays.toString(iniNabla));
                    a = new Attack<>(cipher1, cipher2, cipher1.getNUM_KEYS()-1, i, iniDelta, iniNabla);
                    try {
                        a.applyAttack(false);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    if(a.isIndependent()) ataques.add(a);
                    //System.out.println("id: "+id+" i: "+i+" j: "+j+" k: "+k);

                }
            System.out.println("id: "+id+" completion: "+(100*((partials[id]*cipher1.getNUM_WORDS_KEY_NABLA()+j)/((float)((end-begin)*cipher1.getNUM_WORDS_KEY_NABLA())))));
            }
            partials[id]++;
            System.out.println("id: "+id+" partials  : "+partials[id]);
        }
        System.out.println("FINISHED id: "+id);
        finished = true;        
    }
    
    @Override
    public void run() {
//        genericBalanced();
        AESSpecific();
    }
    
    public boolean isFinished(){
        return finished;
    }
    
}
