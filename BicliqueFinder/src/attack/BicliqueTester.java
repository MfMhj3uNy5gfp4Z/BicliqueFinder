/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package attack;

import static attack.Main.allPairsOfColumns;
import static attack.Main.allWords;
import cifras.AES;
import core.Cipher;
import core.Biclique;
import core.ByteArray;
import core.Range;
import core.RelatedKeyDifferential;
import core.Util;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Frog33
 */
public class BicliqueTester implements Runnable{
    private List<List<RelatedKeyDifferential>> differentials;
    private int stateOfV;
    private List<Integer> wordsOfV;
    private Class<Cipher> classe;
    public List<Biclique> independentBicliques;
    private List<Integer[]> relevantWords;
    private int keyIndexes[];
    private Range ranges[];
    private String types[];
    
    
    public BicliqueTester(List<List<RelatedKeyDifferential>> differentials, int stateOfV, List<Integer> wordsOfV){
        this.differentials = new ArrayList<>(differentials.size());
        this.differentials.addAll(differentials);        
        this.stateOfV = stateOfV;
        this.wordsOfV = wordsOfV;
        this.classe = (Class<Cipher>)differentials.get(0).get(0).getCiphers()[0].getClass();
        
        independentBicliques = new LinkedList<>();
    }
    
    public BicliqueTester(  List<Integer[]> relevantWords,
                            int keyIndexes[],
                            Range ranges[],
                            String types[],
                            Class classe, 
                            int stateOfV,
                            List<Integer> wordsOfV){
        this.differentials = new ArrayList<>();     
        this.classe = classe;
        this.stateOfV = stateOfV;
        this.wordsOfV = wordsOfV;
        this.relevantWords = relevantWords;
        this.keyIndexes = keyIndexes;
        this.ranges = ranges;
        this.types = types;
        
        independentBicliques = new LinkedList<>();
    }
    
    private void updateCounters(int counters[]){
        for (int i = counters.length-1; i >=0; i--) {
            if (counters[i] != ranges[i].end - 1){
                counters[i]++;
                break;
            }else counters[i] = ranges[i].begin;
        }
    }
    
    private boolean isDone(int counters[]){
        for (int i = 0; i < counters.length; i++) 
            if(counters[i] != ranges[i].begin) return false;
        return true;        
    }
    
    public final void createFromRange() throws InstantiationException, IllegalAccessException{
        if(keyIndexes.length != types.length || keyIndexes.length != ranges.length)
            throw new RuntimeException("the key indexes, number of types and number of ranges must be equal");
        
        Cipher cipher = classe.newInstance();
        int mask, counters[] = new int[ranges.length], total = 0;
        ByteArray keyDifference;
        List<RelatedKeyDifferential> aux;
        int progress = 0;
        double percentage = Range.sum(ranges)/(double)100;
        System.out.print("\nBuilding Differentials...\nProgress : " + 0 + "%");
        
        long initialTime = System.currentTimeMillis();
        for (int i = 0; i < counters.length; i++) 
            counters[i] = ranges[i].begin;
        
        
        while(true){
//            System.out.println("Execution number " + total);
            aux = new ArrayList<>();
            for (int i = 0; i < counters.length; i++) {
                keyDifference = new ByteArray(cipher.getKEY_SIZE_IN_BYTES());
                for (int j = 0; j < relevantWords.get(counters[i]).length; j++) {
                    mask = (1<<cipher.getWORD_SIZE()) - 1;
                    keyDifference.setWord(cipher.getWORD_SIZE(), relevantWords.get(counters[i])[j], mask);
                }
//                System.out.println((i+1)+"° key diff : " + keyDifference);
                if(types[i].toLowerCase().equals("nabla"))
                    aux.add(new RelatedKeyDifferential( 0, 
                                                        keyDifference,
                                                        keyIndexes[i],
                                                        classe.newInstance(),
                                                        classe.newInstance(),
                                                        types[i]));
                else
                    aux.add(new RelatedKeyDifferential( 29, 
                                                        cipher.getNUM_STATES()-1,
                                                        keyDifference,
                                                        keyIndexes[i],
                                                        classe.newInstance(),
                                                        classe.newInstance(),
                                                        types[i]));
            }
            differentials.add(aux);
            
            updateCounters(counters);
            total++;
            if (total%percentage == 0) {
                progress++;
                System.out.print("\rProgress : " + progress + "%");
            }
            if (isDone(counters)) break;
        }
        long elapsedTime[] = Util.msToTime(System.currentTimeMillis() - initialTime);
        System.out.print("\rProgress : " + 100 + "%\n");
        System.out.println("Total time : " + elapsedTime[0] + "h " + elapsedTime[1] 
                + "m " + elapsedTime[2] + "s " + elapsedTime[3] + "ms.");
    }

    @Override
    public void run() {
        if (ranges != null) {
            try {
                createFromRange();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        Biclique biclique;
        int cont = 0, progress = 0;
        double percentage = Range.sum(ranges)/(double)100;
        System.out.print("\nTesting Bicliques\nProgress : " + 0 + "%");
        
        long initialTime = System.currentTimeMillis();
        for (List<RelatedKeyDifferential> curr : differentials) {
            biclique = new Biclique(curr, stateOfV, wordsOfV);
            if(biclique.checkIndependence())
                independentBicliques.add(biclique);           
            cont++;
            if (cont%percentage == 0) {
                progress++;
                System.out.print("\rProgress : " + progress + "%");
            }
        }
        long elapsedTime[] = Util.msToTime(System.currentTimeMillis() - initialTime);
        System.out.print("\rProgress : " + 100 + "%\n");
        System.out.println("Total time : " + elapsedTime[0] + "h " + elapsedTime[1] 
                + "m " + elapsedTime[2] + "s " + elapsedTime[3] + "ms.");
        System.out.println(cont + " bicliques tested.");
        sortIndependent();
        save();
    }
    
    public void sortIndependent(){
        independentBicliques.sort((Biclique b1, Biclique b2) -> {
                            if(Math.abs(b1.computeTimeComplexity(false) - b2.computeTimeComplexity(false)) < 0.001){
                                if(b1.computeDataComplexity() > b2.computeDataComplexity()) return 1;
                                if(b1.computeDataComplexity() < b2.computeDataComplexity()) return -1;
                            }
                            if(b1.computeTimeComplexity(false) > b2.computeTimeComplexity(false)) return 1;
                            if(b1.computeTimeComplexity(false) < b2.computeTimeComplexity(false)) return -1;
                            return 0;
                        });
    }
    
    public void save(){
        if (independentBicliques.isEmpty()) return;
        String filename = "";
        
        for (int i = 0; i < ranges.length; i++) 
            filename += (i+1)+"°keyIndex_"+keyIndexes[i]+"-"+(i+1)+"°range_"+ranges[i].begin+"_"+ranges[i].end+" ";
        filename += ".txt";
        
        try(PrintWriter pw = new PrintWriter(new File(filename))){
            for (int i = 0; i < independentBicliques.size(); i++) 
                pw.println(independentBicliques.get(i));
            
        } catch (FileNotFoundException ex) {
            System.out.println("Arquivo não encontrado?");
        }
    }
    
    public static void main1(String[] args) throws InstantiationException, IllegalAccessException{
//        Cipher cipher1 = new AES();
//        Cipher cipher2 = new AES();
//        ByteArray keyDiff = new ByteArray(cipher1.getBLOCK_SIZE_IN_BYTES());
//        keyDiff.set(4, 0xff);
//        keyDiff.set(6, 0xff);
//        RelatedKeyDifferential c = new RelatedKeyDifferential(0, keyDiff, 8, cipher1, cipher2, "nabla");
//
//        keyDiff = new ByteArray(cipher1.getBLOCK_SIZE_IN_BYTES());
//        keyDiff.set(0, 0xff);
//        keyDiff.set(2, 0xff);
//        RelatedKeyDifferential c2 = new RelatedKeyDifferential(0, keyDiff, 8, cipher1, cipher2, "nabla");
//
//        keyDiff = new ByteArray(cipher1.getBLOCK_SIZE_IN_BYTES());
//        keyDiff.set(9, 0xff);
//        RelatedKeyDifferential c3 = new RelatedKeyDifferential(29, cipher1.getNUM_STATES()-1, keyDiff, 8, cipher1, cipher2, "delta");
//        
//        List<List<RelatedKeyDifferential>> diffs = new ArrayList<>();
//        diffs.add(Util.concat(Util.concat(c, c2),c3));
//        BicliqueTester bt = new BicliqueTester(diffs, 9, Util.toIntegerList(0));
//        bt.run();
//        System.out.println(bt.independentBicliques);

        Cipher cipher = new AES();
        int keyIndexes[] = {8,8,8};
        List<Integer> wordsOfV = Util.toIntegerList(0);
        int stateOfV = 9;
        Range ranges[] = {new Range(0, 40), new Range(10, 20), new Range(9, 10)};
        String types[] = {"nabla", "nabla", "delta"};
        List<Integer[]> relevantWords = allWords(cipher);
        relevantWords.addAll(allPairsOfColumns(cipher));
        BicliqueTester bt = new BicliqueTester( relevantWords, 
                                                keyIndexes, 
                                                ranges,
                                                types,
                                                new AES().getClass(),
                                                stateOfV,
                                                wordsOfV);
        bt.run();
        for (int i = 0; i < bt.independentBicliques.size(); i++) {
            System.out.println(bt.independentBicliques.get(i));
            System.out.println(bt.independentBicliques.get(i).computeTimeComplexity(false));
            System.out.println(bt.independentBicliques.get(i).computeDataComplexity());
        }
    }
}
