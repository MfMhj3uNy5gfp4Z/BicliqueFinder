/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package core;

import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Frog33
 */
public class Range {
    public int begin;
    public int end;
    
    public Range(List<Integer> range){
        this(range.get(0), range.get(1));
    }
    
    public Range(int range[]){
        this(range[0], range[1]);
    }
    
    public Range(int a, int b){
        begin = a;
        end = b;
    }
    
    public Range(int b){
        begin = 0;
        end = b;
    }
    
    public int get(int i){
        if(i < 0 || i >= size())
            throw new RuntimeException("cannot get value outside of the range");
        return begin + i;
    }
    
    public int size(){
        return end - begin;
    }
    
    public static int sum(Range ranges[]){
        int result = 1;
        for (Range r : ranges) 
            result*=r.size();        
        return result;
    }
    
    public Range[] partition(int num){
        if(num > size())
            throw new RuntimeException("cannot partition Range when the amount "
                    + "of partitions is bigger than the size of it. (" + size() 
                    + " < " + num + ")");
        
        
        Range result[] = new Range[num];
        int ini = begin,fim = begin, step = size() / num, rest = size() % num;
        
        for (int i = 0; i < result.length; i++) {
            fim += step;
            if (i < rest) 
                result[i] = new Range(ini, ++fim);
            else
                result[i] = new Range(ini, fim);
            ini = fim;
        }
        return result;
    }
    
    public Range copy(){
        return new Range(begin,end);
    }
    
    @Override
    public String toString(){
        return "(" + begin + " ... " + (end-1) + ")";// + " size : "+size();
    }
    
    public static void main(String[] args){
        Range r = new Range(7, 15);
        System.out.println(r);
        System.out.println(Arrays.toString(r.partition(9)));
    }
}
