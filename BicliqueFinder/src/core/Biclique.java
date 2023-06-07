package core;

import cifras.AES;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;


/**
 * A biclique, that is a delta (forward) differential and a nabla (backward) differential. 
 *  
 */
public class Biclique {

    @Override
    public String toString() {
        String result = "Biclique{\n\tdeltas :\n\t";
        for (RelatedKeyDifferential delta : deltaDifferentials) {
            result += "key : " + delta.getKeyIndex() + "\n\tkeyDiff : " + delta.getKeyDifference() + "\n\t";
        }
        result += "\n\tnablas :\n\t";
        for (RelatedKeyDifferential nabla : nablaDifferentials) {
            result += "key : " + nabla.getKeyIndex() + "\n\tkeyDiff : " + nabla.getKeyDifference() + "\n\t";
        }
        result += "\n\ttime complexity : " + getTimeComplexity();
        result += "\n\tdata complexity : " + getDataComplexity();
        return result;
    }
    
    public static List<Biclique> toBicliqueAES(String s, int stateOfV, List<Integer> wordsOfV){
        List<Biclique> result = new ArrayList<>();
        List<RelatedKeyDifferential> differentials = new ArrayList<>();
        ByteArray keyDifference;
        int keyIndex, DELTA = -5, NABLA = -6, i, amountBicliques, contBicliques = 0;
        String temp, state, diff;
        String[] splitedDiff, aux;
        
        aux = s.split("\n");
        amountBicliques = 0;
        for (String str : aux)
            if(str.equals("Biclique{")) amountBicliques++;
        
//        System.out.println(Arrays.toString(aux));
        
        i = 0;
        while(i < aux.length) {
            System.out.print("\rCurrent Biclique : "+contBicliques+"/"+(amountBicliques-1));
            differentials = new ArrayList<>();
            i++;//Linha inicial passou
            
            if (aux[i++].trim().equals("deltas :")) {
                state = "DELTA";
            }else {
                state = "NABLA";
            }
            
            while(!aux[i].trim().equals("")){
//                System.out.println("begining : "+aux[i].trim());
                keyIndex = Integer.parseInt(aux[i++].split(" ")[2]);
//                System.out.println("keyIndex : "+keyIndex);
                diff = aux[i++].split(" ")[2];
                diff = diff.substring(1, diff.length()-1);
                splitedDiff = diff.replace("|", ",").split(",");
                
                keyDifference = new ByteArray(new AES().getBLOCK_SIZE_IN_BYTES());
                for (int j = 0; j < splitedDiff.length; j++) 
                    if (!splitedDiff[j].equals("00")) 
                        keyDifference.set(j, 0xff);
                    
                
//                System.out.println("diff : " + Arrays.toString(splitedDiff));
//                System.out.println(state+" diff : "+new RelatedKeyDifferential(29, new AES().getNUM_STATES()-1,  keyDifference, keyIndex, new AES(), new AES(), state).getKeyDifference(keyIndex));
                differentials.add(new RelatedKeyDifferential(29, new AES().getNUM_STATES()-1, keyDifference, keyIndex, new AES(), new AES(), state));
            }
            i++;
            
            if (aux[i++].trim().equals("deltas :")) {
                state = "DELTA";
            }else {
                state = "NABLA";
            }
            
            while(!aux[i].trim().equals("")){
//                System.out.println("begining : "+aux[i].trim());
                keyIndex = Integer.parseInt(aux[i++].split(" ")[2]);
//                System.out.println("keyIndex : "+keyIndex);
                diff = aux[i++].split(" ")[2];
                diff = diff.substring(1, diff.length()-1);
                splitedDiff = diff.replace("|", ",").split(",");
                
                keyDifference = new ByteArray(new AES().getBLOCK_SIZE_IN_BYTES());
                for (int j = 0; j < splitedDiff.length; j++) 
                    if (!splitedDiff[j].equals("00")) 
                        keyDifference.set(j, 0xff);
                    
                
//                System.out.println(state+" diff : "+new RelatedKeyDifferential(0, keyDifference, keyIndex, new AES(), new AES(), state).getKeyDifference(keyIndex));
                differentials.add(new RelatedKeyDifferential(29, new AES().getNUM_STATES()-1, keyDifference, keyIndex, new AES(), new AES(), state));
            }
            i++;
            //Complexidade de Tempo
            temp = aux[i].split(" ")[aux[i++].split(" ").length-1].replace(",", ".");
//            System.out.println("time complexity : "+Double.parseDouble(temp.substring(3, temp.length()-1)));
            //Complexidade de Dados
            temp = aux[i].split(" ")[aux[i++].split(" ").length-1].replace(",", ".");
//            System.out.println("data complexity : "+Integer.parseInt(temp.substring(3, temp.length()-1)));
            result.add(new Biclique(differentials, stateOfV, wordsOfV));
            contBicliques++;
        }
        System.out.println("");
        return result;
    }
	
    public static void main(String args[]){
        ///AES TAO
        Cipher cipher1 = new AES();
        Cipher cipher2 = new AES();
        ByteArray keyDiff = new ByteArray(cipher1.getBLOCK_SIZE_IN_BYTES());
        keyDiff.set(4, 0xff);
        keyDiff.set(6, 0xff);
        RelatedKeyDifferential c = new RelatedKeyDifferential(29, cipher1.getNUM_STATES()-1, keyDiff, 8, cipher1, cipher2, "nabla");
        System.out.println(keyDiff);
        
        keyDiff = new ByteArray(cipher1.getBLOCK_SIZE_IN_BYTES());
        keyDiff.set(0, 0xff);
        keyDiff.set(2, 0xff);
        RelatedKeyDifferential c2 = new RelatedKeyDifferential(29, cipher1.getNUM_STATES()-1, keyDiff, 8, cipher1, cipher2, "nabla");
        System.out.println(keyDiff);

        keyDiff = new ByteArray(cipher1.getBLOCK_SIZE_IN_BYTES());
        keyDiff.set(9, 0xff);
        RelatedKeyDifferential c3 = new RelatedKeyDifferential(29, cipher1.getNUM_STATES()-1, keyDiff, 8, cipher1, cipher2, "delta");
        System.out.println(keyDiff);

        Integer aux[] = {0}; 
        Biclique b = new Biclique(Util.concat(Util.concat(c, c2), c3),9,new ArrayList<>(Arrays.asList(aux)));
//        System.out.println(b.checkIndependence());
//        System.out.println(b.onlyRelevantV());
        System.out.println(b.onlyRevelantKeys());
        b.printTimeComplexity();
//        b.printAll();

        /*///AES BOGDANOV
        Cipher cipher1 = new AES();
        Cipher cipher2 = new AES();
        ByteArray keyDiff = new ByteArray(cipher1.getBLOCK_SIZE_IN_BYTES());
        keyDiff.set(2, 0xff);
        keyDiff.set(3, 0xff);
        RelatedKeyDifferential c = new RelatedKeyDifferential(29, keyDiff, 8, cipher1, cipher2, "delta");
        //System.out.println(c);
        keyDiff = new ByteArray(cipher1.getBLOCK_SIZE_IN_BYTES());
        keyDiff.set(4, 0xff);
        keyDiff.set(6, 0xff);
        RelatedKeyDifferential c2 = new RelatedKeyDifferential(0, keyDiff, 8, cipher1, cipher2, "nabla");
        //System.out.println(c2);
        Integer temp[] = {0}; 
        Biclique b = new Biclique(Util.concat(c, c2),9,new ArrayList<>(Arrays.asList(temp)));
        //System.out.println(b.affectV);
        System.out.println(b.checkIndependence());
        System.out.println(b.onlyRelevantV());
        System.out.println(b.onlyRevelantKeys());
        b.printTimeComplexity();*/
    }

    /**
     * The name of the cipher over which the biclique is constructed. 
     * The cipher name is required to know how to render a serialized biclique.  
     */
    public Cipher cipher1;

    /**
     * The name of the cipher over which the biclique is constructed. 
     * The cipher name is required to know how to render a serialized biclique.  
     */
    public Cipher cipher2;

    /**
     * The delta (forward) differential.
     */
    public List<RelatedKeyDifferential> deltaDifferentials;

    /**
     * The nabla (backward) differential.
     */
    public List<RelatedKeyDifferential> nablaDifferentials;

    /**
     * The words that affect V.
     */
    public StateDifferential affectV;

    private int stateOfV;

    private int stateOfBiclique;

    public Biclique(RelatedKeyDifferential deltaDifferential, RelatedKeyDifferential nablaDifferential, int stateOfV, List<Integer> wordsOfV) {
        this(Util.concat(deltaDifferential, nablaDifferential),stateOfV, wordsOfV);
    }

    public Biclique(List<RelatedKeyDifferential> differentials, int stateOfV, List<Integer> wordsOfV) {
        deltaDifferentials = new LinkedList<>();
        nablaDifferentials = new LinkedList<>();
        if (differentials == null || differentials.isEmpty()) 
            throw new RuntimeException("Não há diferenciais na lista passada");   
        if (differentials.size() == 1) 
            throw new RuntimeException("Não há diferenciais o suficiente na lista passada");            

        differentials.forEach((diff) -> {
            if (diff.isDelta()) 
                deltaDifferentials.add(diff);
            else 
                nablaDifferentials.add(diff);
        });
        if (deltaDifferentials.isEmpty()) 
            throw new RuntimeException("Há apenas diferenciais NABLA");
        if (nablaDifferentials.isEmpty()) 
            throw new RuntimeException("Há apenas diferenciais DELTA"); 

        this.stateOfBiclique = deltaDifferentials.get(0).getFromState();
        this.stateOfV = stateOfV;
        cipher1 = differentials.get(0).getCiphers()[0];
        cipher2 = differentials.get(0).getCiphers()[1];

        ByteArray difference = new ByteArray(cipher1.getBLOCK_SIZE_IN_BYTES());
        for (Integer i : wordsOfV) difference.set(i,0xff);
        affectV = new StateDifferential(difference, stateOfV, cipher1, cipher2);
    }

    /**
     * Modifica 'affectV' para que permaneça somente as palavras que afetam 
     * V e são relevantes na recomputação.
     * @return Retorna a quantidade de palavras ativas relevantes forward 
     * (indice 0), backward (indice 1) e chaves (indice 2).
     */
    public List<Integer> onlyRelevantV(){
        List<Integer> result = new ArrayList<>(3);
        boolean aux;
        List<Integer> relevantForward = Util.toIntegerList(cipher1.getINDEXES_OF_PRE_SBOX_STATES());
        List<Integer> relevantBackward = Util.toIntegerList(cipher1.getINDEXES_OF_POST_SBOX_STATES());

        for (int i = 0; i < relevantForward.size(); i++) 
            if(relevantForward.get(i) >= stateOfV)
                relevantForward.remove(i--);
        for (int i = 0; i < relevantBackward.size(); i++) 
            if(relevantBackward.get(i) <= stateOfV || relevantBackward.get(i) > stateOfBiclique)
                relevantBackward.remove(i--);     
//            System.out.println(relevantForward);  
//            System.out.println(relevantBackward);
        // Pi1,i2,...  -> v 
        for (int i = 0; i < stateOfV; i++) {
            for (RelatedKeyDifferential nabla : nablaDifferentials) {
                for (int j = 0; j < cipher1.getBLOCK_SIZE_IN_WORDS(); j++) {
                    aux =   affectV.getDifference(i).getWord(cipher1.getWORD_SIZE(), j) != 0 &&
                            nabla.getStateDifference(i).getWord(cipher1.getWORD_SIZE(), j) != 0;
                    affectV.getDifference(i).setWord(cipher1.getWORD_SIZE(),j, aux ? 0xaa : 0);
                }
            }
        }
        // v <- Sj1,j2,...
        for (int i = stateOfV + 1; i < stateOfBiclique; i++) {
            for (RelatedKeyDifferential delta : deltaDifferentials) {
                for (int j = 0; j < cipher1.getBLOCK_SIZE_IN_WORDS(); j++) {
                    aux =   affectV.getDifference(i).getWord(cipher1.getWORD_SIZE(), j) != 0 &&
                            delta.getStateDifference(i).getWord(cipher1.getWORD_SIZE(), j) != 0;
                    affectV.getDifference(i).setWord(cipher1.getWORD_SIZE(),j, aux ? 0xaa : 0);
                }
            }
        }
        //Biclique
        for (int i = stateOfBiclique; i < cipher1.getNUM_STATES(); i++) 
            for (int j = 0; j < cipher1.getBLOCK_SIZE_IN_WORDS(); j++) 
                affectV.getDifference(i).setWord(cipher1.getWORD_SIZE(),j, 0);

        result.add(affectV.getNumActive(relevantForward));
        result.add(affectV.getNumActive(relevantBackward));
        return result;
    }

    /**
     * Retorna a quantidade de Sboxes relevantes (estados e chaves) na
     * Precomputação da biclique.
     * 
     * @return Retorna a quantidade de Sboxes relevantes (estados e chaves) na
     * Precomputação da biclique.
     */
    public List<Integer> onlyRevelantPrecomp(){
        List<Integer> result = new ArrayList<>(3);
        int aux = 0;
        List<Integer> relevantForward = Util.toIntegerList(cipher1.getINDEXES_OF_PRE_SBOX_STATES());
        List<Integer> relevantBackward = Util.toIntegerList(cipher1.getINDEXES_OF_POST_SBOX_STATES());
        List<Integer> relevantKeyForward = Util.toIntegerList(cipher1.getINDEXES_OF_PRE_ADD_KEY());
        List<Integer> relevantKeyBackward = Util.toIntegerList(cipher1.getINDEXES_OF_POST_ADD_KEY());

        for (int i = 0; i < relevantForward.size(); i++) 
            if(relevantForward.get(i) >= stateOfV)
                relevantForward.remove(i--);
        for (int i = 0; i < relevantBackward.size(); i++) 
            if(relevantBackward.get(i) >= stateOfBiclique || relevantBackward.get(i) <= stateOfV)
                relevantBackward.remove(i--);  
        for (int i = 0; i < relevantKeyForward.size(); i++) 
            if(relevantKeyForward.get(i) >= stateOfV)
                relevantKeyForward.remove(i--);
        for (int i = 0; i < relevantKeyBackward.size(); i++) 
            if(relevantKeyBackward.get(i) >= stateOfBiclique || relevantKeyBackward.get(i) <= stateOfV)
                relevantKeyBackward.remove(i--);  


        //states for deltas
        for (int i : relevantForward) 
            aux +=deltaDifferentials.get(0).getStateDifference(0).amountOfWords(cipher1.getWORD_SIZE());
        //keys for deltas
        for (int i : relevantKeyForward)
            aux +=cipher1.getSBOX_RELEVANT_KEY_WORDS().length;
        result.add(aux);

        aux = 0;
        //states for nablas
        for (int i : relevantBackward)
            aux += nablaDifferentials.get(0).getStateDifference(0).amountOfWords(cipher1.getWORD_SIZE());
        //keys for nablas
        for (int i : relevantKeyBackward)
            aux += cipher1.getSBOX_RELEVANT_KEY_WORDS().length;
        result.add(aux);

        return result;
    }
    
    /**
     * Retorna a quantidade de Sboxes relevantes (estados e chaves) na
     * Precomputação da biclique, ignorando as rodadas em que todos os bytes são
     * diferentes.
     * 
     * @return Retorna a quantidade de Sboxes relevantes (estados e chaves) na
     * Precomputação da biclique, ignorando as rodadas em que todos os bytes são
     * diferentes.
     */
    public List<Integer> onlyRevelantPrecompTao(){
        List<Integer> result = new ArrayList<>(3);
        int aux, cont, amountWords;
        List<Integer> relevantForward = Util.toIntegerList(cipher1.getINDEXES_OF_PRE_SBOX_STATES());
        List<Integer> relevantBackward = Util.toIntegerList(cipher1.getINDEXES_OF_POST_SBOX_STATES());
        List<Integer> relevantKeyForward = Util.toIntegerList(cipher1.getINDEXES_OF_PRE_ADD_KEY());
        List<Integer> relevantKeyBackward = Util.toIntegerList(cipher1.getINDEXES_OF_POST_ADD_KEY());
        
//        System.out.println("Pre filtro :");
//        System.out.println("relevantForward : "+relevantForward);
//        System.out.println("relevantBackward : "+relevantBackward);
//        System.out.println("relevantKeyForward : "+relevantKeyForward);
//        System.out.println("relevantKeyBackward : "+relevantKeyBackward);
//        System.out.println("-----------------------------------------");

        //Get only the relevant states for forward precomputation
        amountWords = (int)nablaDifferentials.get(0).getStateDifference(0).amountOfWords(cipher1.getWORD_SIZE());
        for (int i = 0; i < relevantForward.size(); i++) {
            //Somente os estados fora da biclique são relevantes
            if(relevantForward.get(i) >= stateOfV)
                relevantForward.remove(i--);
            //Caso todos os bytes sejam afetados, não é necessário pré-computar
            else{
                cont = 0;
                for (int j = 0; j < amountWords; j++) 
                    if (nablaDifferentials.get(0).getStateDifference(relevantForward.get(i)).getWord(cipher1.getWORD_SIZE(), j) != 0)
                        cont++;
                if(cont == amountWords)
                    relevantForward.remove(i--);
            }
        }
        
        
//        System.out.println("Pos filtro :");
//        System.out.println("relevantForward : "+relevantForward);
//        System.out.println("-----------------------------------------");
//        
        //Get only the relevant states for backward precomputation
        amountWords = (int)deltaDifferentials.get(0).getStateDifference(0).amountOfWords(cipher1.getWORD_SIZE());
        for (int i = 0; i < relevantBackward.size(); i++) {
//            System.out.println("state diference "+relevantBackward.get(i)+" : "+deltaDifferentials.get(0).getStateDifference(relevantBackward.get(i)));
            //Somente os estados fora da biclique e pós estado V são relevantes
            if(relevantBackward.get(i) >= stateOfBiclique || relevantBackward.get(i) <= stateOfV)
                relevantBackward.remove(i--); 
            //Caso todos os bytes sejam afetados, não é necessário pré-computar
            else{
                cont = 0;
                for (int j = 0; j < amountWords; j++) 
                    if (deltaDifferentials.get(0).getStateDifference(relevantBackward.get(i)).getWord(cipher1.getWORD_SIZE(), j) != 0)
                        cont++;
                if(cont == amountWords)
                    relevantBackward.remove(i--);
            } 
        }
        
//        System.out.println("Pos filtro :");
//        System.out.println("relevantBackward : "+relevantBackward);
//        System.out.println("-----------------------------------------");
//        
        //Get only the relevant keys for forward precomputation
        for (int i = 0; i < relevantKeyForward.size(); i++) {
            if(relevantKeyForward.get(i) >= stateOfV)
                relevantKeyForward.remove(i--);
            else
                if(!relevantForward.contains(relevantKeyForward.get(i)+1))
                    relevantKeyForward.remove(i--);            
        }
//        System.out.println("Pos filtro :");
//        System.out.println("relevantKeyForward : "+relevantKeyForward);
//        System.out.println("-----------------------------------------");
        
        //Get only the relevant keys for backward precomputation
        for (int i = 0; i < relevantKeyBackward.size(); i++) {
            if(relevantKeyBackward.get(i) >= stateOfBiclique || relevantKeyBackward.get(i) <= stateOfV)
                relevantKeyBackward.remove(i--);
            else
                if(!relevantBackward.contains(relevantKeyBackward.get(i)+1))
                    relevantKeyBackward.remove(i--);            
        }
        
//        System.out.println("Pos filtro :");
//        System.out.println("relevantKeyBackward : "+relevantKeyBackward);
//        System.out.println("-----------------------------------------");

//        System.out.println("relevantForward : "+relevantForward);
//        System.out.println("relevantBackward : "+relevantBackward);
//        System.out.println("relevantKeyForward : "+relevantKeyForward);
//        System.out.println("relevantKeyBackward : "+relevantKeyBackward);
        aux = 0;
        amountWords = (int)deltaDifferentials.get(0).getStateDifference(0).amountOfWords(cipher1.getWORD_SIZE());
        //states for deltas
        for (int i : relevantForward)
            aux +=amountWords;
        amountWords = (int)deltaDifferentials.get(0).getStateDifference(0).amountOfWords(cipher1.getWORD_SIZE());
        //keys for deltas
        for (int i : relevantKeyForward)
            aux += cipher1.getSBOX_RELEVANT_KEY_WORDS().length;
        result.add(aux);
        
        aux = 0;
        //states for nablas
        for (int i : relevantBackward)
            aux +=amountWords;
        amountWords = cipher1.getSBOX_RELEVANT_KEY_WORDS().length;        
        //keys for nablas
        for (int i : relevantKeyBackward)
            aux += cipher1.getSBOX_RELEVANT_KEY_WORDS().length;
        result.add(aux);
        
        return result;
    }

    /**
     * 
     * @return Retorna a quantidade de Sboxes relevantes na computação das  
     * chaves.
     */
    public List<Integer> onlyRevelantKeys(){
        int aux;
        List<Integer> result = new ArrayList<>(2);
        List<Integer> relevantForwardStates = Util.toIntegerList(cipher1.getINDEXES_OF_PRE_ADD_KEY());
        List<Integer> relevantBackwardStates = Util.toIntegerList(cipher1.getINDEXES_OF_POST_ADD_KEY());

        for (int i = 0; i < relevantForwardStates.size(); i++) 
            if(relevantForwardStates.get(i) >= stateOfV)
                relevantForwardStates.remove(i--);
        for (int i = 0; i < relevantBackwardStates.size(); i++) 
            if(relevantBackwardStates.get(i) >= stateOfBiclique || relevantBackwardStates.get(i) <= stateOfV)
                relevantBackwardStates.remove(i--);  

        int relevantWords[] = cipher1.getSBOX_RELEVANT_KEY_WORDS();

        aux = 0;
        for (int word : relevantWords) 
            for(RelatedKeyDifferential nabla : nablaDifferentials)
                for (int forwardState : relevantForwardStates) 
                    if (affectV.getDifference(forwardState).getWord(cipher1.getWORD_SIZE(), word) != 0 &&
                        nabla.getKeyDifference(cipher1.getINDEX_OF_PRE_KEY(forwardState)).getWord(cipher1.getWORD_SIZE(), word) != 0)
                        aux++;
        result.add(aux);

        aux = 0;
        for (int word : relevantWords) 
            for(RelatedKeyDifferential delta : deltaDifferentials)
                for (int backwardState : relevantBackwardStates) 
                    if (affectV.getDifference(backwardState).getWord(cipher1.getWORD_SIZE(), word) != 0 &&
                        delta.getKeyDifference(cipher1.getINDEX_OF_POST_KEY(backwardState)).getWord(cipher1.getWORD_SIZE(), word) != 0)
                        aux++;
        result.add(aux);

        return result;
    }

    /**
     * 
     * @return Retorna a quantidade de Sboxes relevantes na computação da  
     * biclique.
     */
    public int onlyRevelantBiclique(){
        int result = 0, amountWords;
        List<Integer> relevantStates = Util.toIntegerList(cipher1.getINDEXES_OF_PRE_SBOX_STATES());
        List<Integer> relevantKeys = Util.toIntegerList(cipher1.getINDEXES_OF_PRE_ADD_KEY());

        for (int i = 0; i < relevantStates.size(); i++) 
            if(relevantStates.get(i) < stateOfBiclique)
                relevantStates.remove(i--); 
        
        for (int i = 0; i < relevantKeys.size(); i++) 
            if(relevantKeys.get(i) < relevantStates.get(0))
                relevantKeys.remove(i--); 
//        
//        System.out.println("relevantStates : " + relevantStates);
//        System.out.println("relevantKeys : " + relevantKeys);

        amountWords = (int)deltaDifferentials.get(0).getStateDifference(0).amountOfWords(cipher1.getWORD_SIZE());
        //states for deltas
        for (int i = 0; i < relevantStates.size(); i++)
            result+= amountWords;
        
        //keys for deltas
        for (int i = 0; i < relevantKeys.size(); i++)
            result += cipher1.getSBOX_RELEVANT_KEY_WORDS().length;        

//        System.out.println("result : "+result);
        return result;
    }

    public boolean checkIndependence(){
        return checkIndependence(stateOfBiclique, cipher1.getNUM_STATES()-1);
    }

    public boolean checkIndependence(int fromState, int toState){
        return levelOfDependence(fromState, toState) == 0;
    }

    public int levelOfDependence(int fromState, int toState){
        int cont = 0;

        for(RelatedKeyDifferential delta : deltaDifferentials)
            for(RelatedKeyDifferential nabla : nablaDifferentials)
                for (int state = fromState; state < toState; state++) 
                    for (int word = 0; word < cipher1.getBLOCK_SIZE_IN_WORDS(); word++) 
                        if(delta.getStateDifference(state).get(word) != 0 &&
                           nabla.getStateDifference(state).get(word) != 0)
                            cont++;
        return cont;
    }

    public double computeCBiclique(){
        double multiplier, dimension, pot;
               
        multiplier = Math.log(deltaDifferentials.size() + nablaDifferentials.size())/Math.log(2);
                
        dimension = cipher1.getWORD_SIZE();
        
        pot = Math.log(onlyRevelantBiclique()/(double)cipher1.getNUM_SBOXES_TOTAL())/Math.log(2);
        
//        System.out.println(multiplier+" + "+dimension+" + "+pot);
        
        return multiplier + dimension + pot;
    }

    public double computeCKeys(){
        List<Integer> relevantKeys;
        double aux, totalKeys, potForward, potBackward;

        relevantKeys = onlyRevelantKeys();
        totalKeys = cipher1.getWORD_SIZE()*(nablaDifferentials.size() + deltaDifferentials.size());

        aux = (double)relevantKeys.get(0)/cipher1.getNUM_SBOXES_TOTAL();
        potForward = Math.log(aux)/Math.log(2);

        aux = (double)relevantKeys.get(1)/cipher1.getNUM_SBOXES_TOTAL();
        potBackward = Math.log(aux)/Math.log(2);

//            System.out.println("totalKeys : " + totalKeys);
//            System.out.println("potForward : " + potForward);
//            System.out.println("potBackward : " + potBackward);

        return totalKeys + potForward + potBackward;
    }

    public double computeCKeysTao(){
        int relevantKeys;
        double aux, totalKeys;

        relevantKeys = cipher1.getInitialKeyBiclique();
        totalKeys = cipher1.getWORD_SIZE()*(nablaDifferentials.size()-1 + deltaDifferentials.size()) + 1;

//        System.out.println("Sbox relevant words size : " + cipher1.getSBOX_RELEVANT_KEY_WORDS().length);
//        System.out.println("Relevant keys size : " + relevantKeys);
//        System.out.println("Sbox total : " + cipher1.getNUM_SBOXES_TOTAL());
        
        aux = cipher1.getSBOX_RELEVANT_KEY_WORDS().length*relevantKeys/(double)cipher1.getNUM_SBOXES_TOTAL();

        return totalKeys + Math.log(aux)/Math.log(2);
    }

    public double computeCOracle(){
        return  cipher1.getWORD_SIZE()*deltaDifferentials.size();
    }
    
    public double computeCPrecomp(){
        List<Integer> aux = onlyRevelantPrecomp();
        double s1, s2;

        s1 = Double.valueOf(aux.get(0))/cipher1.getNUM_SBOXES_TOTAL();
        s2 = Double.valueOf(aux.get(1))/cipher1.getNUM_SBOXES_TOTAL();

        double deltas = Math.pow(2, cipher1.getWORD_SIZE()*deltaDifferentials.size());
        double nablas = Math.pow(2, cipher1.getWORD_SIZE()*nablaDifferentials.size());
        double pot = Math.log(deltas * s1 + nablas * s2)/Math.log(2);
        return  pot;
    }
    
    public double computeCPrecompTao(){
        List<Integer> aux = onlyRevelantPrecompTao();
        double multiplier,pot;
        int  totalKeys;
        
        multiplier = Math.log(nablaDifferentials.size() + deltaDifferentials.size())/Math.log(2);
        totalKeys = cipher1.getWORD_SIZE()*(nablaDifferentials.size()-1 + deltaDifferentials.size());
        
//        System.out.println("precomp forward : " + temp.get(0));
//        System.out.println("precomp backward : " + temp.get(0));
//        System.out.println("Sbox total : " + (double)cipher1.getNUM_SBOXES_TOTAL());
        
        pot = Math.log((aux.get(0) + aux.get(1))/(double)cipher1.getNUM_SBOXES_TOTAL())/Math.log(2);
        
//        System.out.println(multiplier+" + "+totalKeys+" + "+pot);
        
        return multiplier + totalKeys + pot;
    }

    public double computeCRecomp(){
        List<Integer> aux = onlyRelevantV();
        double sboxes = Double.valueOf(aux.get(0) + aux.get(1));
        int totalDiffs = nablaDifferentials.size()+deltaDifferentials.size();
        double pot = Math.log(sboxes/cipher1.getNUM_SBOXES_TOTAL())/Math.log(2);
        return pot + cipher1.getWORD_SIZE()*totalDiffs;
    }

    public double computeCFalpos(){

        double numFalpos = (deltaDifferentials.size()+nablaDifferentials.size())*cipher1.getWORD_SIZE()-(double)cipher1.getWORD_SIZE();

        List<Integer> auxStates = Util.toIntegerList(cipher1.getINDEXES_OF_PRE_SBOX_STATES());

        for (int i = 0; i < auxStates.size(); i++) 
            if(auxStates.get(i) > stateOfV)
                auxStates.remove(i--);
        
//        System.out.println("auxStates : "+auxStates);

        double totalSboxes = auxStates.size() * cipher1.getSBOX_RELEVANT_STATE_WORDS().length/(double)cipher1.getNUM_SBOXES_TOTAL();
//        System.out.println("totalSboxes : "+totalSboxes);
        totalSboxes = Math.log(totalSboxes)/Math.log(2);
        
//        System.out.println(numFalpos+" + "+totalSboxes);
        return numFalpos + totalSboxes;
    }
    
    public String getTimeComplexity(){
        int numGroups = cipher1.getKEY_SIZE_IN_BYTES()*8 - (deltaDifferentials.size()+nablaDifferentials.size())*cipher1.getWORD_SIZE();
        double groupComplexity = computeTimeComplexityTao(false);
        return "TimeComplexity = 2^{"+numGroups+"} * 2^{"+new DecimalFormat("0.00").format(groupComplexity)+"} = 2^{"+new DecimalFormat("0.00").format(numGroups+groupComplexity)+"}";        
    }

    public double computeTimeComplexity(boolean debug){
        return computeTimeComplexityTao(debug);
    }
    
    public double computeTimeComplexityTao(boolean debug){
        double Cbiclique, Cprecomp, Ckeys, Crecomp, Cfalpos, Coracle;
        Cbiclique = Math.pow(2, computeCBiclique());
        Ckeys =     Math.pow(2, computeCKeysTao());
        Cprecomp =  Math.pow(2, computeCPrecompTao());
        Crecomp =   Math.pow(2, computeCRecomp());
        Cfalpos =   Math.pow(2, computeCFalpos()); 
        Coracle =   Math.pow(2, computeCOracle()); 

        if(debug){
            System.out.println("Cbiclique : "+ computeCBiclique());
            System.out.println("Coracle : "+ computeCOracle());
            System.out.println("Ckeys : "+ computeCKeysTao());
            System.out.println("Cprecomp : "+ computeCPrecompTao());
            System.out.println("Crecomp : "+ computeCRecomp());
            System.out.println("Cfalpos : "+ computeCFalpos());
        }

        return Math.log(Cbiclique + Ckeys + Cprecomp + Crecomp + Cfalpos + Coracle)/Math.log(2);
    }
    
    public int computeDataComplexity(){
        int result = 0;
        boolean active;
        for (int i = 0; i < cipher1.getBLOCK_SIZE_IN_WORDS(); i++) {
            active = false;
            for (RelatedKeyDifferential diff : deltaDifferentials) 
                if (diff.getStateDifference(cipher1.getNUM_STATES() - 1).getWord(cipher1.getWORD_SIZE(), i) != 0) 
                    active = true;
            for (RelatedKeyDifferential diff : nablaDifferentials) 
                if (diff.getStateDifference(cipher1.getNUM_STATES() - 1).getWord(cipher1.getWORD_SIZE(), i) != 0) 
                    active = true;
            if(active) result++;
        }
        return result * cipher1.getWORD_SIZE();
    }
    
    public String getDataComplexity(){
        return "DataComplexity = 2^{"+computeDataComplexity()+"}";        
    }

    public void printTimeComplexity(){
        System.out.println(getTimeComplexity());
    }
    
    public void printAll(){
        System.out.println("deltas : ");
        for (RelatedKeyDifferential keyDiff : deltaDifferentials) 
            System.out.println(keyDiff);
        
        System.out.println("nablas : ");
        for (RelatedKeyDifferential keyDiff : nablaDifferentials) 
            System.out.println(keyDiff);
        
    }

    public boolean equals(Object object) {
        try {
            Biclique other = (Biclique)object;

            if (other == null) {
                return false;
            } else {
                return  deltaDifferentials.equals(other.deltaDifferentials)
                        && nablaDifferentials.equals(other.nablaDifferentials);
            }
        } catch (Exception e) {
            return false;
        }
    }	
}
