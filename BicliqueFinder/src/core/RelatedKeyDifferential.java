package core;

import cifras.AES;
import java.util.ArrayList;
import java.util.List;

/**
 * This class represents a differential, that is a sequence of state and key differences.
 * 
 * É necessário alterar a função computeDifferential para aceitar STARS e
 * Bicliques no meio.
 * 
 */
public class RelatedKeyDifferential implements Cloneable{

    public static void main(String[] args){
        // Como teste, é usada a biclique original de Bogdanov.
        ///Test DELTA
        Cipher cipher1 = new AES();
        Cipher cipher2 = new AES();
        ByteArray keyDiff = new ByteArray(cipher1.getBLOCK_SIZE_IN_BYTES());
//        keyDiff.set(2, 0xff);   //only DELTA
//        keyDiff.set(3, 0xff);   //only DELTA
        keyDiff.set(4, 0xff);   //only NABLA
        keyDiff.set(6, 0xff);   //only NABLA
        RelatedKeyDifferential c = new RelatedKeyDifferential(  cipher1.getInitialStateBiclique(), 
                                                        cipher1.getNUM_STATES()-1, 
                                                        keyDiff, 
                                                        8,
                                                        cipher1,
                                                        cipher2,
                                                        "nabla");
        System.out.println(c);
//        ///Test NABLA
//        Cipher cipher1 = new AES();
//        Cipher cipher2 = new AES();
//        ByteArray keyDiff = new ByteArray(cipher1.getBLOCK_SIZE_IN_BYTES());
//        keyDiff.set(2, 0xff);
//        keyDiff.set(3, 0xff);
//        RelatedKeyDifferential c = new RelatedKeyDifferential(  cipher1.getInitialStateBiclique(), 
//                                                        cipher1.getNUM_STATES()-1, 
//                                                        keyDiff, 
//                                                        8,
//                                                        cipher1,
//                                                        cipher2,
//                                                        "nabla");
//        System.out.println(c);
    }
    
    /**
     * The start state of the differential. The default value is <code>0</code>.align
     */
    private int fromState;

    /**
     * The end state of the differential. The default value is <code>0</code>.
     */
    private int toState;

    /**
     * A list of key differences. The i-th element represents the differences in 
     * the round keys of round i.  
     */
    private List<ByteArray> keyDifferences = new ArrayList<>();

    /**
     * A list of state differences. The i-th element represents the differences in 
     * the states after round i.  
     */
    private List<ByteArray> stateDifferences = new ArrayList<>();

    private ByteArray keyDifference;
    
    private int keyIndex;
    
    /**
     * Can be the values 'DELTA' or 'NABLA'.
     */
    private String type;        
    
    /**
     * The object used to generate the complete differential of a given cipher.
     */
    private Cipher cipher1;  
    
    /**
     * The object used to generate the complete differential of a given cipher.
     */
    private Cipher cipher2;  
    
    public boolean isDelta(){
        return type.equals("DELTA");
    }
    
    public int getKeyIndex(){
        return keyIndex;
    }
    
    public ByteArray getKeyDifference(){
        return keyDifference.clone();
    }
    
    public Cipher[] getCiphers(){
        Cipher ciphers[] = {cipher1, cipher2};
        return ciphers;
    }    

    public RelatedKeyDifferential(  int fromState, 
                                    ByteArray keyDifference, 
                                    int keyIndex, 
                                    Cipher cipher1, 
                                    Cipher cipher2, 
                                    String type) {
        
        this(fromState, cipher1.getNUM_STATES()-1, keyDifference, keyIndex, cipher1, cipher2, type);
    }
    
    public RelatedKeyDifferential(  int fromState, 
                                    int toState, 
                                    ByteArray keyDifference, 
                                    int keyIndex, 
                                    Cipher cipher1, 
                                    Cipher cipher2, 
                                    String type) {
        
        if(!type.toUpperCase().equals("DELTA") && !type.toUpperCase().equals("NABLA")) throw new RuntimeException("type has to be either DELTA or NABLA");
        
        this.type = type.toUpperCase();
        this.keyDifference = keyDifference;
        this.keyIndex = keyIndex;
        this.cipher1 = cipher1;
        this.cipher2 = cipher2;
        this.fromState = fromState;
        this.toState = toState;
        int i = 0;        
        
        
        for (i = 0; i < cipher1.getNUM_STATES(); i++) 
            stateDifferences.add(new ByteArray(cipher1.getBLOCK_SIZE_IN_BYTES()));    
            
        for (i = 0; i < cipher1.getNUM_KEYS(); i++) 
            keyDifferences.add(new ByteArray(cipher1.getKEY_SIZE_IN_BYTES()));
        
        propagate();
    }
    
    public final void propagate(){
        //System.out.println("begin keys");
        computeActiveWordsKeys();
        //System.out.println("end keys");
        //System.out.println("begin states");
        computeActiveWordsStates();
        //System.out.println("end states");
    }
    
    /**
     * Computa todas as PALAVRAS que são ativadas pelas diferenciais delta nas 
     * subchaves da cifra (na chave expandida).
     */
    public void computeActiveWordsKeys(){      
        ByteArray key1, key2, keyDifference;
        List<Integer> indexActiveWords = this.keyDifference.getActiveWords(cipher1.getWORD_SIZE());
        int AMOUNT_WORDS = cipher1.getBLOCK_SIZE_IN_BYTES()*8/cipher1.getWORD_SIZE();
        
        key1 = new ByteArray(AMOUNT_WORDS);
        cipher1.Reset(key1, keyIndex);
        for (int i = 1; i < 0x1<<cipher1.getWORD_SIZE(); i++) {
            key2 = key1.clone();

            for (int k = 0; k < indexActiveWords.size(); k++)
                key2.setWord(cipher1.getWORD_SIZE(),indexActiveWords.get(k), i);

            cipher2.Reset(key2, keyIndex);

            keyDifference = cipher1.getExpandedKey(key1, keyIndex).clone().getDifference(cipher2.getExpandedKey(key2, keyIndex));

            int marker = 0xA;   //Marcador de palavras ativas
            if(cipher1.getWORD_SIZE() == 8) marker = 0xAA;

            for (int k = 0; k < keyDifference.amountOfWords(cipher1.getWORD_SIZE()); k++) {
                int key = k/AMOUNT_WORDS;
                int word = k%AMOUNT_WORDS;

                if (keyDifferences.get(key).getWord(cipher1.getWORD_SIZE(),word)==0 && keyDifference.getWord(cipher1.getWORD_SIZE(),k)!=0) 
                    keyDifferences.get(key).setWord(cipher1.getWORD_SIZE(),word,marker);
            }   
        }
    }
    
    /**
     * Computa todas as PALAVRAS que são ativadas pelas diferenciais nabla nos 
     * estados internos da cifra. 
     */
    public void computeActiveWordsStates(){
        ByteArray aux;
        List<Integer> indexActiveWords = this.keyDifference.getActiveWords(cipher1.getWORD_SIZE());
        List<ByteArray> currDifferential;
        int marker, AMOUNT_WORDS = cipher1.getBLOCK_SIZE_IN_BYTES()*8/cipher1.getWORD_SIZE();
       
        for (int k = 1; k < 0x1<<cipher1.getWORD_SIZE(); k++) {
            aux = new ByteArray(AMOUNT_WORDS);
            for (int i = 0; i < indexActiveWords.size(); i++) 
                aux.setWord(cipher1.getWORD_SIZE(),indexActiveWords.get(i), k);

            currDifferential = computeDifferential(aux);
            
            if (type.endsWith("NABLA")) {
                marker = 0xB;   //Marcador de palavras ativas
                if(cipher1.getWORD_SIZE() == 8) marker = 0xBB;
            }else{
                marker = 0xA;   //Marcador de palavras ativas
                if(cipher1.getWORD_SIZE() == 8) marker = 0xAA;
            }
            
            for (int i = 0; i < this.stateDifferences.size(); i++) 
                for (int j = 0; j < AMOUNT_WORDS; j++) 
                    if(currDifferential.get(i).getWord(cipher1.getWORD_SIZE(),j) != 0)
                        this.stateDifferences.get(i).setWord(cipher1.getWORD_SIZE(),j, marker);
        }
    }
    /**
     * Calcula a diferencial a partir da diferença de chave 'keyDiff'
     * em toda a cifra.
     *
     * @param keyDiff é a diferença de chave usada para recuperar a diferencial
     *              nabla.
     * @return Retorna a diferença de cada um dos estados da cifra, dada a
     * diferença de chave keyDiff.
     */
    public List<ByteArray> computeDifferential(ByteArray keyDiff){  
        List<ByteArray> allStates1,//Representa as palavras afetadas nos estados pela key1 a partir da biclique
                        allStates2,//Representa as palavras afetadas nos estados pela key2 a partir da biclique
                        allStates3,//Representa as palavras afetadas nos estados pela key1 para a recomputação
                        allStates4,//Representa as palavras afetadas nos estados pela key2 para a recomputação
                        allStatesDiff = new ArrayList<>();
        ByteArray key1, key2, state1, state2;
                
        key1 = new ByteArray(cipher1.getKEY_SIZE_IN_BYTES()*cipher1.getAMOUNT_OF_KEYS());   //Chave Base
        key1.randomize();
        key2 = keyDiff.clone().xor(key1);                                                           //Chave Base 2

        cipher1.Reset(key1,keyIndex);                       //Usada para expandir a chave
        cipher2.Reset(key2,keyIndex);                       //Usada para expandir a chave 2

        state1 = new ByteArray(cipher1.getBLOCK_SIZE_IN_BYTES());      //Estado Base
        state1.randomize();
        state2 = state1.clone();                                       //Estado Base 2

        
        if (type.equals("NABLA")) {
            allStates1 = cipher1.encryptFullSavingStates(state1, toState, toState);
            allStates2 = cipher2.encryptFullSavingStates(state2, toState, toState);
            if(toState == cipher1.getNUM_STATES()-1){ // Significa que a biclique está no final da cifra
                allStates3 = cipher1.encryptFullSavingStates(state1, 0, toState);
                allStates4 = cipher2.encryptFullSavingStates(state2, 0, toState);
            }else if (fromState ==  0){ // Significa que a biclique está no início da cifra
                allStates3 = new ArrayList<>();
                allStates4 = new ArrayList<>();
                for (int i = 0; i < allStates1.size(); i++) {  // Dummys para não interferir na criação de allStatesDiff
                    allStates3.add(new ByteArray(cipher1.getBLOCK_SIZE_IN_BYTES()));
                    allStates3.get(i).fill(0xf);
                    allStates4.add(new ByteArray(cipher1.getBLOCK_SIZE_IN_BYTES()));
                }
            }else{// Significa que a biclique está no meio da cifra
                //TODO
                allStates3 = new ArrayList<>();
                allStates4 = new ArrayList<>();
                throw new UnsupportedOperationException("DELTA não sabe lidar com bicliques no meio, apenas nas pontas");
            }
            
        }else{//DELTA
            allStates1 = cipher1.encryptFullSavingStates(state1, fromState, toState);
            allStates2 = cipher2.encryptFullSavingStates(state2, fromState, toState);
            
            if(toState == cipher1.getNUM_STATES()-1){ // Significa que a biclique está no final da cifra
                allStates3 = new ArrayList<>();
                allStates4 = new ArrayList<>();
                for (int i = 0; i < allStates1.size(); i++) {  // Dummys para não interferir na criação de allStatesDiff
                    allStates3.add(new ByteArray(cipher1.getBLOCK_SIZE_IN_BYTES()));
                    allStates3.get(i).fill(0xf);
                    allStates4.add(new ByteArray(cipher1.getBLOCK_SIZE_IN_BYTES()));
                }
            }else if (fromState ==  0){ // Significa que a biclique está no início da cifra
                allStates3 = cipher1.encryptFullSavingStates(state1, 0, toState);
                allStates4 = cipher2.encryptFullSavingStates(state2, 0, toState);
            }else{// Significa que a biclique está no meio da cifra
                //TODO
                allStates3 = new ArrayList<>();
                allStates4 = new ArrayList<>();
                throw new UnsupportedOperationException("DELTA não sabe lidar com bicliques no meio, apenas nas pontas");
            }
            
        }
        
        for (int i = 0; i < allStates1.size(); i++)
            allStatesDiff.add(allStates1.get(i).clone().xor(allStates2.get(i)).and(allStates3.get(i).clone().xor(allStates4.get(i)))); 
        
//        System.out.println("allStatesDiff : \n");
//        for (ByteArray b : allStatesDiff) 
//            System.out.println(b);
        
        
        return allStatesDiff;
    }
    
    public RelatedKeyDifferential and(RelatedKeyDifferential other) {
            final int numKeys = keyDifferences.size();
            final int numStates = stateDifferences.size();

            for (int round = 0; round < numKeys; round++) {
                    andElementsIfNotZeroAt(keyDifferences, other.keyDifferences, round);
            }

            for (int round = 0; round < numStates; round++) {
                    andElementsIfNotZeroAt(stateDifferences, other.stateDifferences, round);
            }

            return this;
    }

    public void printKeyDifferences(){
        for (int i = 0; i < keyDifferences.size(); i++) {
            System.out.println(keyDifferences.get(i));
        }
    }

    /** 
     * Creates a deep copy of this differential.
     * @see java.lang.Object#clone()
     */
    @Override
    public RelatedKeyDifferential clone() {
        RelatedKeyDifferential copy = new RelatedKeyDifferential(this.fromState, this.toState, this.keyDifference,
                                            this.keyIndex, this.cipher1, this.cipher2, this.type);
        
        for (int round = 0; round <= this.keyDifferences.size(); round++) 
            copy.keyDifferences.set(round, this.keyDifferences.get(round).clone());
            
        for (int state = 0; state < this.stateDifferences.size(); state++)
            copy.stateDifferences.set(state, this.stateDifferences.get(state).clone());
            

        return copy;
    }

    public boolean equals(Object object) {
            try {
                RelatedKeyDifferential other = (RelatedKeyDifferential)object;

                if (other == null) {
                    return false;
                } else {
                    return  keyIndex == other.keyIndex
                            && keyDifferences.equals(other.keyDifferences)
                            && stateDifferences.equals(other.stateDifferences)
                            && keyDifference.equals(other.keyDifference)
                            && cipher1.equals(other.cipher1)
                            && cipher2.equals(other.cipher2)
                            && type.toUpperCase().equals(other.type.toUpperCase())
                            && fromState == other.fromState
                            && toState == other.toState;
                }
            } catch (Exception e) {
                return false;
            }
    }
    
    /**
     * Returns the state that begins the differential.
     */
    public int getFromState() {
        return fromState;
    }
    
    /**
     * Returns the key difference for the given round.
     */
    public ByteArray getKeyDifference(int round) {
        return keyDifferences.get(round);
    }

    /**
     * Returns the state difference for the given round.
     */
    public ByteArray getStateDifference(int round) {
        return stateDifferences.get(round);
    }

    /**
     * Sets the key difference for the given round.
     */
    public void setKeyDifference(int round, ByteArray delta) {
            keyDifferences.set(round, delta);
    }

    /**
     * Sets the state difference for the given round.
     */
    public void setStateDifference(int round, ByteArray difference) {
        stateDifferences.set(round, difference);
    }

    public RelatedKeyDifferential or(RelatedKeyDifferential other) {
        final int numKeys = keyDifferences.size();
        final int numStates = stateDifferences.size();

        for (int round = 0; round < numKeys; round++) {
            orElementsIfNotZeroAt(keyDifferences, other.keyDifferences, round);
        }

        for (int round = 0; round < numStates; round++) {
            orElementsIfNotZeroAt(stateDifferences, other.stateDifferences, round);
        }

        return this;
    }
    
    /**
     * 
     * @param relevantStates contains the indexes of the relevant states.
     * @return Returns de number of non-zero words of the relevant states of 
     * this differential.
     */
    public int getNumActive(List<Integer> relevantStates){
        int result = 0;
        if (relevantStates == null)        
            for (ByteArray b : stateDifferences) 
                result += b.countNumActiveWords(cipher1.getWORD_SIZE());
        else
            for (int i : relevantStates) 
                result += stateDifferences.get(i).countNumActiveWords(cipher1.getWORD_SIZE());
            
        return result;
    }

    /**
     * Returns a hex string representation of this differential. 
     */
    public String toHexString() {
        String result = "";
        //boolean hasKeyWrappingAtTheEnd = (keyDifferences.size() == toRound + 2);
        // For 0-th and (toRound + 1)-th element

        for (int round = 0; round < keyDifferences.size(); round++) 
            result += updateString(keyDifferences, round, " key   ");
        
        for (int state = 0; state < stateDifferences.size(); state++) 
            result += updateString(stateDifferences, state, " state ");
        

        /*if (hasKeyWrappingAtTheEnd) {
            result += updateString(keyDifferences, toRound + 1, " key   ");
            result += updateString(stateDifferences, toRound, " state ");
        } else {
            result += updateString(stateDifferences, toRound, " state ");
        }*/

        return result;
    }

    private String updateString(List<ByteArray> differences, int round, String identifier) {
            ByteArray difference = differences.get(round);
            String result = "";

            if (difference != null) {
                if (identifier.equals(" key   "))
                    result = "K" + round + " = " + difference + "\n";
                else
                    result = "#" + round + " : " + difference + "\n";
            }

            return result;
    }

    public String toString() {
            return "[" + toHexString() + "]";
    }

    /**
     * XORs all state differences and key differences of a differential instance with the 
     * related state differences and key differences of the given one. Modifies the original, 
     * and leaves the <code>other</code> differential unchanged. 
     */
    public RelatedKeyDifferential xor(RelatedKeyDifferential other) {
        final int numKeys = keyDifferences.size();
        final int numStates = stateDifferences.size();

        for (int round = 0; round < numKeys; round++) {
            xorElementsIfNotZeroAt(keyDifferences, other.keyDifferences, round);
        }

        for (int round = 0; round < numStates; round++) {
            xorElementsIfNotZeroAt(stateDifferences, other.stateDifferences, round);
        }

        return this;
    }

    private void andElementsIfNotZeroAt(List<ByteArray> first, List<ByteArray> second, int index) {
        if (first.get(index) != null && second.get(index) != null) {
            first.get(index).and(second.get(index));
        }
    }

    private void orElementsIfNotZeroAt(List<ByteArray> first, List<ByteArray> second, int index) {
        if (first.get(index) != null && second.get(index) != null) {
            first.get(index).or(second.get(index));
        }
    }

    private void xorElementsIfNotZeroAt(List<ByteArray> first, List<ByteArray> second, int index) {
        if (first.get(index) != null && second.get(index) != null) {
            first.get(index).xor(second.get(index));
        }
    }
	
}
