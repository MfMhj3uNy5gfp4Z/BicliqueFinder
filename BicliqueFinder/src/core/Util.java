/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package core;

import cifras.AES;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Frog33
 */
public class Util {
    
    public static List<RelatedKeyDifferential> concat(RelatedKeyDifferential a, RelatedKeyDifferential b){
        ArrayList<RelatedKeyDifferential> result = new ArrayList<>();
        result.add(a);result.add(b);
        return result;
    }
    
    public static List<RelatedKeyDifferential> concat(List<RelatedKeyDifferential> a, RelatedKeyDifferential b){
        ArrayList<RelatedKeyDifferential> result = new ArrayList<>();
        result.addAll(a);result.add(b);
        return result;
    }
    
    public static long[] msToTime(long ms){
        long result[] = {0,0,0,0};
        result[3] = ms%1000;
        ms /= 1000;
        result[2] = ms%60;
        ms /= 60;
        result[1] = ms%60;
        ms /= 60;
        result[0] = ms;
        return result;
    }
    
    public static List<RelatedKeyDifferential> concat(RelatedKeyDifferential a, List<RelatedKeyDifferential> b){
        ArrayList<RelatedKeyDifferential> result = new ArrayList<>();
        result.add(a);result.addAll(b);
        return result;
    }
    
    public static List<Integer[]> getSmartAES(){
        return null;
    }
    
    public static List<Integer> toIntegerList(int i){
        List<Integer> result = new ArrayList<>(1);
        result.add(i);
        return result;
    }
    
    public static List<Integer> toIntegerList(int ints[]){
        List<Integer> result = new ArrayList<>(ints.length);
        for (int i : ints) 
            result.add(i);
        return result;
    }
    
    public static void main(String[] args){
        List<Integer[]> nablaActive = new LinkedList<>();
        Integer aux[];
        for (int j1 = 0; j1 < new AES().getBLOCK_SIZE_IN_WORDS(); j1++) {
            for (int j2 = j1+4; j2 < 16; j2+=4){ 
                aux = new Integer[2];
                aux[0] = j1;
                aux[1] = j2;
                nablaActive.add(aux);
            }
        }
        System.out.println(Arrays.deepToString(nablaActive.toArray()));
        List<Integer[]>[] partitions = partition(nablaActive,4);
        for (int i = 0; i < 4; i++) {
           System.out.println(Arrays.deepToString(partitions[i].toArray()));
        }
    }
    
    public static List<Integer[]>[] partition(List<Integer[]> lista, int k){
        List<Integer[]>[] result = new List[k];
        for (int i = 0; i < k; i++) {
            result[i] = new LinkedList<>();
        }
        int j = 0;
        for (int i = 0; i < lista.size(); i++) {
            if(result[j].size() == lista.size()/k && j < lista.size()%k){
                result[j++].add(lista.get(i));
            }else if(result[j].size() == lista.size()/k-1 && j >= lista.size()%k){
                result[j++].add(lista.get(i));
            }else{
                result[j].add(lista.get(i));
            }
        }
        return result;
    }
}
