package core;

import cifras.AES;
import java.util.ArrayList;
import java.util.List;

/**
 * This class represents a Statedifferential, that is a sequence of state and key differences.
 * 
 */
public class StateDifferential implements Cloneable{

    public static void main(String[] args){
        Cipher cipher1 = new AES();
        Cipher cipher2 = new AES();
        ByteArray diff = new ByteArray(cipher1.getBLOCK_SIZE_IN_BYTES());
        diff.set(0, 0xff);
        StateDifferential c = new StateDifferential(diff, 9, cipher1, cipher2);
        System.out.println(c);
    }
            
    private ByteArray difference;
    /**
     * state of the Statedifferential. The default value is <code>0</code>.align
     */
    private int stateIndex;
    
    /**
     * A list of state differences. The i-th element represents the differences in 
     * the states after round i.  
     */
    private List<ByteArray> stateDifferences = new ArrayList<>();

    /**
     * The object used to generate the complete Statedifferential of a given cipher.
     */
    private Cipher cipher1;  
    
    /**
     * The object used to generate the complete Statedifferential of a given cipher.
     */
    private Cipher cipher2;  
    
    public Cipher[] getCiphers(){
        Cipher ciphers[] = {cipher1, cipher2};
        return ciphers;
    }    

    public StateDifferential(ByteArray difference, int keyIndex, Cipher cipher1, Cipher cipher2, String type) {
        this(difference, keyIndex, cipher1, cipher2);
    }
    
    public StateDifferential(ByteArray difference, int stateIndex, Cipher cipher1, Cipher cipher2) {

        this.difference = difference;
        this.stateIndex = stateIndex;
        this.cipher1 = cipher1;
        this.cipher2 = cipher2;
        int i = 0;        
                
        for (i = 0; i < cipher1.getNUM_STATES(); i++) 
            stateDifferences.add(new ByteArray(cipher1.getBLOCK_SIZE_IN_BYTES()));    
        
        propagate();
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
     * Computa todas as PALAVRAS que são ativadas pelas diferenciais nabla nos 
     * estados internos da cifra. 
     */
    public final void propagate(){
        ByteArray aux;
        List<Integer> indexActiveWords = this.difference.getActiveWords(cipher1.getWORD_SIZE());
        List<ByteArray> currStateDifferential;
        int marker, AMOUNT_WORDS = cipher1.getBLOCK_SIZE_IN_BYTES()*8/cipher1.getWORD_SIZE();
       
        for (int k = 1; k < 0x1<<cipher1.getWORD_SIZE(); k++) {
            aux = new ByteArray(AMOUNT_WORDS);
            for (int i = 0; i < indexActiveWords.size(); i++) 
                aux.setWord(cipher1.getWORD_SIZE(),indexActiveWords.get(i), k);

            currStateDifferential = computeStateDifferential(aux);
            
            marker = 0xA;   //Marcador de palavras ativas
            if(cipher1.getWORD_SIZE() == 8) marker = 0xAA;
                        
            for (int i = 0; i < this.stateDifferences.size(); i++) 
                for (int j = 0; j < AMOUNT_WORDS; j++) 
                    if(currStateDifferential.get(i).getWord(cipher1.getWORD_SIZE(),j) != 0)
                        this.stateDifferences.get(i).setWord(cipher1.getWORD_SIZE(),j, marker);
        }
    }
    /**
     * Calcula a diferencial a partir da diferença de chave 'keyDiff'
     * em toda a cifra.
     *
     * @param keyDiff é a diferença de chave usada para recuperar a diferencial
     *              nabla.
     */
    public List<ByteArray> computeStateDifferential(ByteArray stateDiff){  
        List<ByteArray> allStates1,allStates2, allStatesDiff = new ArrayList<>();
        ByteArray key, state1, state2;
                
        key = new ByteArray(cipher1.getKEY_SIZE_IN_BYTES()*cipher1.getAMOUNT_OF_KEYS());   //Chave Base
           
        cipher1.Reset(key);                       //Usada para expandir a chave
        cipher2.Reset(key);                       //Usada para expandir a chave 2

        state1 = new ByteArray(cipher1.getBLOCK_SIZE_IN_BYTES());      //Estado Base
        state1.randomize();
        state2 = state1.clone().xor(stateDiff);                                       //Estado Base 2

        allStates1 = cipher1.encryptFullSavingStates(state1, stateIndex, cipher1.getNUM_STATES()-1);
        allStates2 = cipher2.encryptFullSavingStates(state2, stateIndex, cipher1.getNUM_STATES()-1);
        
        for (int i = 0; i < allStates1.size(); i++)
            allStatesDiff.add(allStates1.get(i).clone().xor(allStates2.get(i))); 
        
        return allStatesDiff;
    }
    
    public StateDifferential and(StateDifferential other) {
        final int numStates = stateDifferences.size();

        for (int round = 0; round < numStates; round++) 
            andElementsIfNotZeroAt(stateDifferences, other.stateDifferences, round);

        return this;
    }

    /** 
     * Creates a deep copy of this Statedifferential.
     * @see java.lang.Object#clone()
     */
    @Override
    public StateDifferential clone() {
        StateDifferential copy = new StateDifferential(this.difference,
                                            this.stateIndex, this.cipher1, this.cipher2);
         
        for (int state = 0; state < this.stateDifferences.size(); state++)
            copy.stateDifferences.set(state, this.stateDifferences.get(state).clone());
            

        return copy;
    }

    public boolean equals(Object object) {
            try {
                StateDifferential other = (StateDifferential)object;

                if (other == null) {
                    return false;
                } else {
                    return  stateIndex == other.stateIndex
                            && stateDifferences.equals(other.stateDifferences)
                            && difference.equals(other.difference)
                            && cipher1.equals(other.cipher1)
                            && cipher2.equals(other.cipher2);
                }
            } catch (Exception e) {
                return false;
            }
    }

    /**
     * Returns the state difference for the given round.
     */
    public ByteArray getDifference(int round) {
        return stateDifferences.get(round);
    }

    /**
     * Sets the state difference for the given round.
     */
    public void setdifference(int round, ByteArray difference) {
        stateDifferences.set(round, difference);
    }

    public StateDifferential or(StateDifferential other) {
        final int numStates = stateDifferences.size();

        for (int round = 0; round < numStates; round++) 
            orElementsIfNotZeroAt(stateDifferences, other.stateDifferences, round);
        

        return this;
    }

    /**
     * Returns a hex string representation of this Statedifferential. 
     */
    public String toHexString() {
        String result = "";
        //boolean hasKeyWrappingAtTheEnd = (keyDifferences.size() == toRound + 2);
        // For 0-th and (toRound + 1)-th element

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
     * XORs all state differences and key differences of a Statedifferential instance with the 
     * related state differences and key differences of the given one. Modifies the original, 
     * and leaves the <code>other</code> Statedifferential unchanged. 
     */
    public StateDifferential xor(StateDifferential other) {
        final int numStates = stateDifferences.size();

        for (int round = 0; round < numStates; round++) 
            xorElementsIfNotZeroAt(stateDifferences, other.stateDifferences, round);        

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
