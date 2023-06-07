/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package attack;

import attack.BicliqueTester;
import cifras.AES;
import core.Cipher;
import core.Biclique;
import core.ByteArray;
import core.Range;
import core.RelatedKeyDifferential;
import core.Util;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

/**
 *
 * @author Frog33
 */
public class Main {
    public static void main(String[] args) throws IOException{
//        attack(args);
        List<Biclique> bicliques = getResultBicliques();
        if (bicliques.isEmpty()) return;
        String filename = "bicliques.txt";
        
        try(PrintWriter pw = new PrintWriter(new File(filename))){
            for (int i = 0; i < bicliques.size(); i++) 
                pw.println(bicliques.get(i));
            
        } catch (FileNotFoundException ex) {
            System.out.println("Arquivo não encontrado?");
        }
   }
    
    public static List<Biclique> getResultBicliques() throws FileNotFoundException{
        List<Biclique> result = new ArrayList<>();
        List<File> files = new LinkedList<>();
        files.addAll(Arrays.asList(new File(".").listFiles()));
        File f;
        int i = 0;
        while(i < files.size()){
            if (files.get(i).isDirectory()) {
                f = new File(files.get(i).getPath());
                files.remove(i--);
                files.addAll(Arrays.asList(f.listFiles()));
            }
            i++;
        }
        Scanner scanner;
        String biclique, aux;
        i = 0;
        for (File file : files) {
            System.out.println("file "+i+"/"+(files.size()-1)+" curr file > "+file.getPath());
            scanner = new Scanner(file);
            while(scanner.hasNextLine()){
                biclique = "";
                while(scanner.hasNextLine()){
                    biclique = scanner.nextLine();
//                    System.out.println("lido: "+biclique);
                    if(biclique.equals("Biclique{")) break;
                    biclique = "";
                }
                while(scanner.hasNextLine()){
                    aux = scanner.nextLine();
//                    System.out.println("lido: "+aux);
                    if(aux.split(" ")[0].equals("data")) break;
                    biclique += "\n" + aux;
//                    System.out.println("partial biclique : "+biclique);
                }
//                System.out.println("biclique >>");
//                System.out.println(biclique);
//                System.out.println("<< biclique");
                if(!biclique.equals("")){
//                    System.out.println("final : "+biclique);
                    result.addAll(Biclique.toBicliqueAES(biclique, 9, Util.toIntegerList(0)));
                }
            }
            i++;
        }        
        result.sort((Biclique b1, Biclique b2) -> {
            if(Math.abs(b1.computeTimeComplexity(false) - b2.computeTimeComplexity(false)) < 0.001){
                if(b1.computeDataComplexity() > b2.computeDataComplexity()) return 1;
                if(b1.computeDataComplexity() < b2.computeDataComplexity()) return -1;
            }
            if(b1.computeTimeComplexity(false) > b2.computeTimeComplexity(false)) return 1;
            if(b1.computeTimeComplexity(false) < b2.computeTimeComplexity(false)) return -1;
            return 0;
        });
        return result;
    }
    
    public static <T> T[] concat(T[] array1, T[] array2) {
        T[] result = Arrays.copyOf(array1, array1.length + array2.length);
        System.arraycopy(array2, 0, result, array1.length, array2.length);
        return result;
    }
    public static void attack(String[] args){
        
        Cipher cipher = new AES();
        int keyIndexes[] = {8,8,8};
        List<Integer> wordsOfV = Util.toIntegerList(0);
        int stateOfV = 9;
        Range ranges[] = {new Range(0, 40), new Range(0, 40), new Range(0, 40)};
        String types[] = {"nabla", "nabla", "delta"};
        List<Integer[]> relevantWords = allWords(cipher);
        relevantWords.addAll(allPairsOfColumns(cipher));
        int nthreads = 1;
        
        List<Range[]> rangesOfThreads = new ArrayList<>(1);
        rangesOfThreads.add(ranges);
        
        String aux[];
        int a,b;
        
        for (int i = 0; i < args.length; i++) {
            switch(args[i]){
                case "-ki":
                    aux = args[++i].split(",");
                    keyIndexes = new int[aux.length];
                    for (int j = 0; j < keyIndexes.length; j++) 
                        keyIndexes[j] = Integer.parseInt(aux[j]);
                    break;
//                case "-kirange":
//                    aux = args[++i].split(",");
//                    keyIndexes = new int[aux.length];
//                    for (int j = 0; j < keyIndexes.length; j++) 
//                        keyIndexes[j] = Integer.parseInt(aux[i]);
//                    break;
                case "-types":
                    types = args[++i].split(",");
                    break;
                case "-nt":
                    nthreads = Integer.parseInt(args[++i]);               
                    break;
                case "-ranges":
                    aux = args[++i].split(";");
                    ranges = new Range[aux.length];
                    for (int j = 0; j < ranges.length; j++) 
                        ranges[j] = new Range(Integer.parseInt(aux[j].split(",")[0]), Integer.parseInt(aux[j].split(",")[1]));
                    break;
            }
        }
        
        Range temp1[] = ranges[0].partition(nthreads);
        Range temp2[];
        rangesOfThreads = new ArrayList<>();

        for (int j = 0; j < temp1.length; j++) {
            temp2 = new Range[ranges.length];
            temp2[0] = temp1[j];
            System.arraycopy(ranges, 1, temp2, 1, ranges.length - 1);
            rangesOfThreads.add(temp2);
        }     
        
        System.out.println("Attack {");
        System.out.println("\tkey indexes : " + Arrays.toString(keyIndexes));
        System.out.println("\ttypes : " + Arrays.toString(types));
        System.out.println("\ttesting ranges : " + Arrays.toString(ranges));
        System.out.print("\tnumber of threads : " + nthreads);
        if (nthreads == 1) 
            System.out.println("}");
        else{
            System.out.println("\n\tranges of each thread : ");
            for (Range[] r : rangesOfThreads) {
                System.out.println("\t"+Arrays.toString(r));
            }
            System.out.println("}");
        }
            
        
        for (int i = 0; i < rangesOfThreads.size(); i++) {
            ranges = rangesOfThreads.get(i);
            new Thread(new BicliqueTester( relevantWords, 
                                                    keyIndexes, 
                                                    ranges,
                                                    types,
                                                    new AES().getClass(),
                                                    stateOfV,
                                                    wordsOfV)).start();
        }
//        for (int i = 0; i < bt.independentBicliques.size(); i++) 
//            System.out.println(bt.independentBicliques.get(i));
    }
    
    public static List<Integer[]> allWords(Cipher cipher){
        List<Integer[]> result = new ArrayList<>();
        Integer aux[];
        for (int i = 0; i < cipher.getKEY_SIZE_IN_BYTES(); i++) {
            aux = new Integer[1];
            aux[0] = i;
            result.add(aux);
        }
        return result;
    }
    
    public static List<Integer[]> allPairsOfColumns(Cipher cipher){
        Integer aux[];
        List<Integer[]> result = new ArrayList<>();
        
        for (int i = 0; i < cipher.getKEY_SIZE_IN_BYTES(); i++) {
            for (int j = i+1; j < i/4*4+4; j++) {
                aux = new Integer[2];
                aux[0] = i; aux[1] = j;
                result.add(aux);
            }
        }
        return result;
    }
}
