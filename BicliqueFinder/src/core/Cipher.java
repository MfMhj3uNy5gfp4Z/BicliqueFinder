/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package core;

import core.ByteArray;
import java.util.ArrayList;

/**
 *
 * @author ´Gabriel
 */
public abstract class Cipher {
    public static final int NUM_BYTES_IN_256_BITS = 256 / Byte.SIZE;
    public static final int NUM_BYTES_IN_128_BITS = 128 / Byte.SIZE;
    public static int BLOCK_SIZE_IN_BYTES = 16;
    public static int KEY_SIZE_IN_BYTES = 32;
    public static int ROUND_KEY_SIZE_IN_BYTES = 16;
    public static int NUM_ROUNDS = 32;
    public static int NUM_KEYS = 33;
    public static int WORD_SIZE_IN_BITS = 8;
    public static int EXPANDED_KEY_SIZE = (NUM_ROUNDS + 1) * NUM_BYTES_IN_128_BITS;
    
    private ByteArray secretKey;
    private ByteArray secretKeyNoSbox;
    private int roundOfFirstKeyToBeApplied;
    private int wordSize;
    
    public Cipher(ByteArray key,int round){
        setKey(key,round);
    }
    
    public Cipher(){
    }
    
    public Cipher Reset(ByteArray key,int round){
        setKey(key,round);
        return this;
    }
    
    public Cipher Reset(ByteArray key){
        setKey(key);
        return this;
    }
        
    public ByteArray getExpandedKeyNoSbox(){
        return secretKeyNoSbox;
    }
    
    protected void setWordSize(int value){
        wordSize = value;
    }
	
    public void setRoundOfFirstKeyToBeApplied(int round){
        roundOfFirstKeyToBeApplied = round;
    }
    
    public ArrayList<ByteArray> encryptFullSavingStates(ByteArray block, int fromState, int toState) {
            boolean debug = false;
//            System.out.println("fromState : "+fromState+", toState : "+toState);
            ArrayList<ByteArray> result = new ArrayList<>();
            ArrayList<ByteArray> aux;
            if(debug) System.out.println("encryptRoundsBackwardsSavingStates(block, "+fromState+", 0).reverse()");
            aux = encryptRoundsBackwardsFromStatesSavingStates(block, fromState, 0);
            if(debug) System.out.println("Size of encryptRoundsBackwardsSavingStates(block, "+fromState+", 0).reverse() = "+aux.size());
            result.addAll(aux);
//            System.out.println("backwards : \n"+result);
//            result.remove(result.size()-1);
//            if(toState!=NUM_STATES-1) aux.remove(aux.size()-1);      //Evitar repetição do estado base
            if(debug) System.out.println("encryptRoundsSavingStates(block, "+fromState+", "+toState+")");
            aux = encryptRoundsFromStatesSavingStates(block, fromState, toState);
            result.addAll(aux);
            if(debug) System.out.println("Size of encryptRoundsSavingStates(block, "+fromState+", "+toState+") = "+aux.size());
            if(debug) System.out.println("Size of encryptFullSavingStates = "+result.size());
            //System.out.println("encryptFullSavingStates["+(getNUM_STATES()-1)+"] = " + block.clone().xor(result.get(result.size()-1)));
            return result;
    }
    
    public abstract ByteArray getExpandedKey();
    public abstract int getAMOUNT_OF_KEYS();
    public abstract int getBLOCK_SIZE_IN_BYTES();
    public abstract int getBLOCK_SIZE_IN_WORDS();
    public abstract int getKEY_SIZE_IN_BYTES();
    public abstract int getROUND_KEY_SIZE_IN_BYTES();
    public abstract int getNUM_ROUNDS();
    public abstract int getNUM_KEYS();
    public abstract int getNUM_STATES();
    public abstract int getNUM_MITM_STATES();
    public abstract int getNUM_SBOXES_TOTAL();
    public abstract int getWORD_SIZE();
    public abstract int getNUM_WORDS_KEY_DELTA();
    public abstract int getNUM_WORDS_KEY_NABLA();
    public abstract int getINDEX_OF_PRE_KEY(int state);
    public abstract int getINDEX_OF_POST_KEY(int state);
    public abstract int getINDEX_OF_STATE_PRE_KEY(int index);
    public abstract int getINDEX_OF_STATE_POST_KEY(int index);
    public abstract int[] getINDEXES_OF_PRE_SBOX_STATES();
    public abstract int[] getINDEXES_OF_POST_SBOX_STATES();
    public abstract int[] getINDEXES_OF_PRE_ADD_KEY();
    public abstract int[] getINDEXES_OF_POST_ADD_KEY();
    public abstract int[] getSBOX_RELEVANT_KEY_WORDS();
    public abstract int[] getSBOX_RELEVANT_STATE_WORDS();
    public abstract int getStateOfV();      //Deve ser retirado depois
    public abstract int getWordOfV();       //Deve ser retirado depois
    
    public abstract int getInitialStateBiclique();
    public abstract int getInitialKeyBiclique();
        
    /**
    * This setKey expands a pair of round keys of 128 bits each, of rounds
    * 0 and 1.
    * 
    * @param key is the pair of keys that will be used to expand alll others
    */
    public void setKey(ByteArray key){
        setKey(key, 0);
    }
        
    /**
    * This setKey expands a pair of round keys of 128 bits each, of rounds
    * 'round' and 'round'+1.
    * 
    * @param key is the pair of keys that will be used to expand alll others
    * @param round is the first round of the pair of keys
    */
    public abstract void setKey(ByteArray key, int round);
    
    public abstract ByteArray getExpandedKey(ByteArray key, int round1);
    
    //DEPRECATED
    public abstract ArrayList<ByteArray> encryptFullSavingStates(ByteArray block, int indexOfFirstKeyToBeApplied);
    
    public abstract ArrayList<ByteArray> encryptRoundsFromStatesSavingStates(ByteArray block, int fromState, int toState);
    
    public abstract ArrayList<ByteArray> encryptRoundsBackwardsFromStatesSavingStates(ByteArray block, int fromState, int toState);
    
    public abstract ByteArray encryptRounds(ByteArray block, int fromRound, int toRound);
    
    
}
