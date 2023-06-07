/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package oldattack;

import cifras.AES;
import core.Cipher;
import cifras.Serpent;
import core.ByteArray;
import core.Difference;
import core.Differential;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Onde estão os For's com key e word dentro são específicos para Serpent.
 *  ATUALMENTE TRABALHA APENAS COM NIBBLES. DEVE SER ALTERADO PARA TRABALHAR COM PALAVRAS GENÉRICAS,
 * PRINCIPALMENTE COM BYTES, VISTO QUE MUITOS USAM ESTE TAMANHO DE PALAVRA (EM ESPECIAL, O AES).
 * 1- ***Alterar nibbles para palavras nos comentários dos atributos.***FEITO
 * 2- ***Alterar nibbles para palavras nos comentários dos métodos.***FEITO
 * 3.1 - ***Refatorar nibbles para palavras nos atributos e métodos.***FEITO
 * 3.2 - ***Alterar a constante 32 por AMOUNT_WORDS = BLOCK_SIZE_IN_BYTES*8/WORD_SIZE***FEITO
 * 4- ***Checar se o algoritmo continua funcionando.***FEITO
 * 5- ***Fazer alterações para que passe a funcionar.***FEITO
 * 6- ***Testar o AES.***FEITO
 * 7- ***Caso haja erros, encontrar os motivos para o AES não funcionar.***FEITO
 * 8- ***Checar se resultados são válidos para o AES.***FEITO
 * 9.1- ***Se não forem válidos, encontrar os motivos.***FEITO
 * 9.2- ***Encontrados os motivos, deve-se corrigí-los.***FEITO
 * 10 - Corrigir contagem de complexidade para o AES***FEITO
 * 11- Compilar resultados***FEITO
 * 12- Adicionar ao texto do SBSeg***FEITO
 * @author ´Gabriel
 */
public class Attack<T extends Cipher> {
    
    
    private static void findAllPossibleOneThread() throws Exception {
        
        LinkedList<Attack<Serpent>> ataques = new LinkedList<>();
        double percent = 0.001;
        Attack<Serpent> a;
        int iniDelta[] = new int[1];
        int iniNabla[] = new int[1];
        for (int i = 0; i < 32; i++) {
            for (int j = 0; j < 64; j++) {
                iniNabla[0] = j;
                for (int k = 0; k < 32; k++) {
                    iniDelta[0] = k;
                    a = new Attack<>(new Serpent(), new Serpent(), 31, i, iniDelta, iniNabla);
                    a.applyAttack(false);
                    if(a.isIndependent()) ataques.add(a);
                }
            }
            if((i+1)/32.0 > percent){
                System.out.printf("\rProgress : %.2f%%",((i+1)/32.0*100));
                percent += 0.001;
            }
        }
        System.out.println("\rProgress : 100%");
        ataques.sort((Attack<Serpent> o1, Attack<Serpent> o2) -> {
            if(o1.getExpTimeComplexity() > o2.getExpTimeComplexity()) return 1;
            if(o1.getExpTimeComplexity() < o2.getExpTimeComplexity()) return -1;
            return 0;
        });
        System.out.println("");
        System.out.println(ataques);
        System.exit(0);
    }
    
    public static void SerpentAttack() throws Exception{
        int input = 0;
        while(true){
            try{
                System.out.println("Choose a test option:");
                System.out.println("1 - Finds all possible bicliques where there is only\n"
                        + "one active word for \\Delta^K and one for \\nabla^K, and the\n"
                        + "active word of \\Delta^K must be form subkey K^{31}.\n"
                        + "(Can take a couple of hours to finish)");
                System.out.println("2 - Shows all the information about the best biclique found.");
                System.out.println("0 - Exit the program.");

                input = new Scanner(System.in).nextInt();

                switch (input) {
                    case 0:
                        System.exit(0);
                    case 1:
                    {
                        System.out.println("Quantas threads para realizar o teste?");
                        input = new Scanner(System.in).nextInt();
                        int partials[] = new int[input];
                        PartialAttack partialAttacks[] = new PartialAttack[input];
                        LinkedList<Attack<Serpent>> allAttacks = new LinkedList<>();
                        //findAllPossibleOneThread();
                        for (int id = 0; id < input; id++){
                            partialAttacks[id] = new PartialAttack(id,32/input, partials, new Serpent(), new Serpent());
                            new Thread(partialAttacks[id]).start();
                        }
                        int aux;
                        while(true){
                            Thread.sleep(60000);
                            aux = 0;
                            for (int id = 0; id < input; id++){
                                aux += partialAttacks[id].partials[id];  
                            }
                            //System.out.println("\r" + aux + "/32");
                            if(aux == 32) break;
                        }
                        for (int id = 0; id < input; id++) {
                            for (int i = 0; i < partialAttacks[id].ataques.size(); i++) {
                                allAttacks.add((Attack<Serpent>)partialAttacks[id].ataques.get(i));
                            }
                        }
                        allAttacks.sort((Attack<Serpent> o1, Attack<Serpent> o2) -> {
                            if(o1.getExpTimeComplexity() > o2.getExpTimeComplexity()) return 1;
                            if(o1.getExpTimeComplexity() < o2.getExpTimeComplexity()) return -1;
                            return 0;
                        });
                        System.out.println("");
                        System.out.println(allAttacks);
                        System.exit(0);
                        
                    }
                    case 2:
                    {
                        int m[] = {6};
                        int n[] = {11};
                        
                        Attack<Serpent> a = new Attack<>(new Serpent(), new Serpent(), 31, 18, m, n);
                        a.applyAttack(true);
                        System.out.println(a.isIndependent());
                        System.exit(0);
                    }
                    default:
                        throw new Error();
                }
            }catch(Error e){
                System.out.println("Select either 1, 2 or 0. "+input+ " was chosen");
            }
        }
        /*LinkedList<Attack> ataques = new LinkedList<>();
        double percent = 0.001;
        Attack a;
        int iniDelta[] = new int[1];
        int iniNabla[] = new int[1];
        for (int i = 0; i < 32; i++) {
            for (int j = 0; j < 64; j++) {
                iniNabla[0] = j;
                for (int k = 0; k < 32; k++) {
                    iniDelta[0] = k;
                    a = new Attack(31, i, iniDelta, iniNabla);
                    a.applyAttack(false);
                    if(a.isIndependent()) ataques.add(a);
                }
            }
            if((i+1)/32.0 > percent){
                System.out.printf("\rProgress : %.2f%%",((i+1)/32.0*100));
                percent += 0.001;
            }
        }
        System.out.println("\rProgress : 100%");
        ataques.sort((Attack o1, Attack o2) -> {
            if(o1.getExpTimeComplexity() > o2.getExpTimeComplexity()) return 1;
            if(o1.getExpTimeComplexity() < o2.getExpTimeComplexity()) return -1;
            return 0;
        });
        System.out.println("");
        System.out.println(ataques);
        int m[] = {6};
        int n[] = {11};
        
        Attack a = new Attack(31, 18, m, n);
        a.applyAttack(true);
        System.out.println(a.isIndependent());*/        
    }
    
    public static void AESAttack() throws Exception{
        AESAttack(-1, -1, -1, -1, -1);
    }
    
    public static void AESAttack(int numThreads, int numDeltaKeys, int numNablaKeys, 
                                int firstDeltaKey, int firstNablaKey) throws Exception{
        int input = 1;
        while(true){
            try(FileWriter fw = new FileWriter("log_attack.log")){
                if(numThreads == -1){
                    System.out.println("Choose a test option:");
                    fw.write("Choose a test option:\n");
                    System.out.println("1 - Finds all possible bicliques where there is only\n"
                            + "one active word for \\Delta^K and one for \\nabla^K, and the\n"
                            + "active word of \\Delta^K must be form subkey K^{31}.\n"
                            + "(Can take a couple of hours to finish)");
                    fw.write("1 - Finds all possible bicliques where there is only\n"
                            + "one active word for \\Delta^K and one for \\nabla^K, and the\n"
                            + "active word of \\Delta^K must be form subkey K^{31}.\n"
                            + "(Can take a couple of hours to finish)\n");
                    //System.out.println("2 - Shows all the information about the best biclique found.");
                    System.out.println("0 - Exit the program.");
                    input = new Scanner(System.in).nextInt();
                    fw.write("0 - Exit the program.   "+input+"\n");
                }

                switch (input) {
                    case 0:
                        System.exit(0);
                    case 1:
                    {
                        if(numThreads == -1){
                            System.out.println("Quantas threads para realizar o teste?");
                            numThreads = new Scanner(System.in).nextInt();
                            fw.write("Quantas threads para realizar o teste?  "+ input + "\n");
                        }
                        
                        if(firstNablaKey == -1){
                            firstNablaKey = 10;
                        }
                        
                        if(numDeltaKeys == -1){
                            System.out.println("Quantas chaves para Delta serão testadas?");
                            numDeltaKeys = new Scanner(System.in).nextInt();
                         
                        }
                        if (numDeltaKeys > new AES().getNUM_KEYS()){
                            numDeltaKeys = new AES().getNUM_KEYS();
                            fw.write("Quantas chaves para Delta serão testadas?    "+ numDeltaKeys + "\n");
                            firstDeltaKey = 0;
                        }
                        if(firstDeltaKey == -1 && numDeltaKeys < new AES().getNUM_KEYS()){
                            System.out.println("Qual a primeira chave?");
                            firstDeltaKey = new Scanner(System.in).nextInt();
                            if (firstDeltaKey + numDeltaKeys > new AES().getNUM_KEYS()) {
                                firstDeltaKey = new AES().getNUM_KEYS() - numDeltaKeys;
                            }
                            fw.write("Qual a primeira chave?    "+ firstDeltaKey + "\n");
                        }else if(firstDeltaKey == -1){
                            firstDeltaKey = 0;
                        } 
                        int partials[] = new int[numThreads];
                        PartialAttack partialAttacks[] = new PartialAttack[numThreads];
                        LinkedList<Attack<AES>> allAttacks = new LinkedList<>();
                        
                        int begin, end, resto = numDeltaKeys%numThreads, div = numDeltaKeys/numThreads;
                        if (numDeltaKeys == 1 && numThreads > 1) {
                            div = 24/numThreads;
                            for (int id = 0; id < numThreads; id++){
                                partialAttacks[id] = new PartialAttack(numThreads, id, firstDeltaKey, firstDeltaKey+1,id*div, 
                                                        (id+1)*div, firstNablaKey, partials, new AES(), new AES());
                                new Thread(partialAttacks[id]).start();
                            }
                        }else{
                            for (int id = 0; id < numThreads; id++){
                                if(id < resto){
                                    begin = id * (div + 1);
                                    end = (id+1) * (div + 1); 
                                }else if(id == resto){
                                    begin = id * (div + 1);
                                    end = begin + div; 
                                }
                                else{
                                    begin = id * div +resto;
                                    end = begin + div; 
                                }
                                partialAttacks[id] = new PartialAttack(numThreads, id, begin + firstDeltaKey, end + firstDeltaKey, partials, new AES(), new AES());

                                new Thread(partialAttacks[id]).start();
                            }
                        }
                        boolean fim;
                        int time = 0;
                        while(true){
                            Thread.sleep(60000);
                            time++;
                            fim = true;
                            for (int id = 0; id < numThreads; id++){
                                if (!partialAttacks[id].isFinished()){
                                    fim = false;
                                    break;
                                }
                            }
                            if(fim) break;
                            System.out.println("Not finished. "+time+" minutes have passed");
                            fw.write("Not finished. "+time+" minutes have passed\n");
                        }   
                        for (int id = 0; id < numThreads; id++) {
                            for (int i = 0; i < partialAttacks[id].ataques.size(); i++) {
                                allAttacks.add((Attack<AES>)partialAttacks[id].ataques.get(i));
                            }
                        }
                        allAttacks.sort((Attack<AES> o1, Attack<AES> o2) -> {
                            if(o1.getExpTimeComplexity() > o2.getExpTimeComplexity()) return 1;
                            if(o1.getExpTimeComplexity() < o2.getExpTimeComplexity()) return -1;
                            return 0;
                        });
                        System.out.println("");
                        System.out.println(allAttacks);
                        fw.write("\n"+allAttacks+"\n");
                        System.exit(0);
                        
                    }
                    /*case 2:
                    {
                        int m[] = {6};
                        int n[] = {11};
                        
                        Attack<AES> a = new Attack<>(new AES(), new AES(), 31, 18, m, n);
                        a.applyAttack(true);
                        System.out.println(a.isIndependent());
                        System.exit(0);
                    }*/
                    default:
                        throw new Error();
                }
            }catch(Error e){
                System.out.println("Select either 1, 2 or 0. "+input+ " was chosen");
            }
        }
        /*LinkedList<Attack> ataques = new LinkedList<>();
        double percent = 0.001;
        Attack a;
        int iniDelta[] = new int[1];
        int iniNabla[] = new int[1];
        for (int i = 0; i < 32; i++) {
            for (int j = 0; j < 64; j++) {
                iniNabla[0] = j;
                for (int k = 0; k < 32; k++) {
                    iniDelta[0] = k;
                    a = new Attack(31, i, iniDelta, iniNabla);
                    a.applyAttack(false);
                    if(a.isIndependent()) ataques.add(a);
                }
            }
            if((i+1)/32.0 > percent){
                System.out.printf("\rProgress : %.2f%%",((i+1)/32.0*100));
                percent += 0.001;
            }
        }
        System.out.println("\rProgress : 100%");
        ataques.sort((Attack o1, Attack o2) -> {
            if(o1.getExpTimeComplexity() > o2.getExpTimeComplexity()) return 1;
            if(o1.getExpTimeComplexity() < o2.getExpTimeComplexity()) return -1;
            return 0;
        });
        System.out.println("");
        System.out.println(ataques);
        int m[] = {6};
        int n[] = {11};
        
        Attack a = new Attack(31, 18, m, n);
        a.applyAttack(true);
        System.out.println(a.isIndependent());*/        
    }
    
    public static void main2(String[] args) throws Exception{
        /*int numThreads = -1;
        int numDeltaKeys = -1;
        int numNablaKeys = -1;
        int firstDeltaKey = -1;
        int firstNablaKey = -1;
        int debug = -1;
        System.out.println(Arrays.toString(args));
        
        try{
            for (int i = 0; i < args.length; i++) {
                debug = i;
                if (args[i].equals("-t")) {
                    numThreads = Integer.parseInt(args[++i]);
                }else if (args[i].equals("-dk")) {
                    numDeltaKeys = Integer.parseInt(args[++i]);                
                }else if (args[i].equals("-fdk")) {
                    firstDeltaKey = Integer.parseInt(args[++i]);
                }else if (args[i].equals("-nk")) {
                    numNablaKeys = Integer.parseInt(args[++i]);
                }else if (args[i].equals("-fnk")) {
                    firstNablaKey = Integer.parseInt(args[++i]);
                }else{
                    System.out.println("Esta opção não é válida");
                }
            }
            
            
            
        }catch(Exception ex){
            System.out.println("Argumentos inválidos (\"" + debug + "\")");
        }*/
        

        //SerpentAttack();
        //AESAttack(numThreads, numDeltaKeys, numNablaKeys, firstDeltaKey, firstNablaKey);
//        int partials[] = new int[1];
//        PartialAttack<AES> attack = new PartialAttack(0,new AES().getNUM_KEYS()-1, partials, new AES(), new AES());
        //attack.run();
        int nabla[] = {4, 6};
        int delta[] = {2, 3};
//        Attack<AES> a = new Attack<>(new AES(), new AES(), 10, 8, delta, nabla);
        Attack<AES> a = new Attack<>(new AES(), new AES(), 8, 8, delta, nabla, true);
//        Attack<Serpent> a = new Attack<>(new Serpent(), new Serpent(), 31, 18, delta, nabla);
        a.applyAttack(true);
//        System.out.println("Is independent. "+a.isIndependent());
        System.out.println(a);  
    }
    
    
    
    boolean same;                                       // Defines if more than one active words are active with the same value or not
    T cipher1;
    T cipher2;
    private int NUM_MITM_STATES;                       // Número de estados na parte de meet-in-the-middle do ataque. (AINDA NÃO GENERALIZADO)
    private int AMOUNT_WORDS;
    
    //Informações gerais
    private int amountAffectedWords;                        // é a quantidade de PALAVRAS afetadas                                            lowDiff[0]
    private Difference lowestDifferential;                  // é a diferencial que afeta menos bits em determinada rodada.                   lowDiff[1]
    private ByteArray fullLowestDifferential;               // é a chave expandida de lowestDifferential.                                    lowDiff[2]
    private ArrayList<Integer> posBaseKeyAffectedWords;     // contém as posições das PALAVRAS afetadas da chave base.                        lowDiff[3]
    private ArrayList<ByteArray> activeWordsDifference;     // contém as PALAVRAS ativas por ambas diferenciais nos estados internos da cifra. attack[2]
    private ArrayList<ByteArray> activeWordsDifferenceKey;  // contém as PALAVRAS ativas por ambas diferenciais nas subchaves da cifra.
    ArrayList<ByteArray> activeWordsThatAffectV;            // contém as PALAVRAS ativas que influenciam na computação da variável 'v' na fase de recomputação.
    private int roundDelta;
    private int roundNabla;
    private int stateOfV;                              // Estado onde 'v' é alocado. (AINDA NÃO GENERALIZADO)
    private int statePreBiclique;                      // Último estado antes da biclique, assumindo que a biclique está no últimos estados da cifra. (AINDA NÃO GENERALIZADO)
    private boolean independent;
    
    //Informações de Delta
    private ArrayList<ByteArray> activeWordsDeltaKey;       //contém as PALAVRAS ativas de chave da diferença Delta.                         attack[5] /lowDiff[4]
    private Differential delta;                             //contém a diferencial delta completa                                           attack[3] / getDeltaDifferential()
    private ArrayList<ByteArray> activeWordsDelta;          //contém as PALAVRAS ativas pela diferencial delta nos estados internos da cifra attack[0]
    private int[] initialWordsDelta;
    
    //Informações Nabla
    private ArrayList<ByteArray> activeWordsNablaKey;     //contém as PALAVRAS ativas de chave da diferença Nabla.                         attack[6]
    private Differential nabla;                             //contém a diferencial nabla completa                                           attack[4] / getNablaDifferential()
    private ArrayList<ByteArray> activeWordsNabla;        //contém as PALAVRAS ativas pela diferencial nabla nos estados internos da cifra attack[1]
    private int[] initialWordsNabla;
        
    //Informações para as chaves Base
    ArrayList<IndependentWord> independentWords;        //contém as PALAVRAS das subchaves que são independentes da outra differencial.
    ArrayList<IndependentWord> candidateWords;          //contém as PALAVRAS das subchaves que são candidatas a PALAVRAS de chave base.
    ArrayList<BaseKey> baseKeyCandidates;                   //contém os candidatos à chave base.
    
    //Informações de complexidade de tempo
    int numActiveSboxes;                                    //é o número total de sboxes que são ativadas na fase de recomputação.
    int numActiveSboxesForward;                             //é o número total de sboxes que são ativadas na fase de recomputação adiante nos estados internos da cifra.
    int numActiveSboxesBackward;                            //é o número total de sboxes que são ativadas na fase de recomputação para trás nos estados internos da cifra.
    int numActiveSboxesForwardKey;                          //é o número total de sboxes que são ativadas na fase de recomputação adiante nas subchaves da cifra.
    int numActiveSboxesBackwardkey;                         //é o número total de sboxes que são ativadas na fase de recomputação para trás nas subchaves da cifra.
    double expTimeComplexity;                               //é o expoente de 2 que representa a complexidade de tempo total do ataque.

    
    
    //---------------------------------Métodos---------------------------------
            
    public Attack(T cifra1, T cifra2, int roundDelta, int roundNabla, int[] initialWordsDelta, int[] initialWordsNabla, boolean same){
        this.same = same;
        this.cipher1 = cifra1;
        this.cipher2 = cifra2;
        this.AMOUNT_WORDS = cipher1.getBLOCK_SIZE_IN_BYTES()*8/cipher1.getWORD_SIZE();
        NUM_MITM_STATES = cipher1.getNUM_MITM_STATES();
        activeWordsThatAffectV = new ArrayList<>();
        for (int i = 0; i < NUM_MITM_STATES; i++) {
            activeWordsThatAffectV.add(new ByteArray(cipher1.getBLOCK_SIZE_IN_BYTES()));
        }
        activeWordsDelta = new ArrayList<>();
        activeWordsNabla = new ArrayList<>();
        activeWordsDifference = new ArrayList<>();
        activeWordsNablaKey = new ArrayList<>();
        activeWordsDeltaKey = new ArrayList<>();
        activeWordsDifferenceKey = new ArrayList<>();
        independentWords = new ArrayList<>();
        candidateWords = new ArrayList<>();
        baseKeyCandidates = new ArrayList<>();
        this.initialWordsDelta = Arrays.copyOf(initialWordsDelta, initialWordsDelta.length);
        this.initialWordsNabla = Arrays.copyOf(initialWordsNabla, initialWordsNabla.length);
        
        this.lowestDifferential = new Difference();
        this.amountAffectedWords = initialWordsDelta.length;
        this.posBaseKeyAffectedWords = new ArrayList<>();
        for (int i = 0; i < initialWordsDelta.length; i++) posBaseKeyAffectedWords.add(initialWordsDelta[i]);
        
        delta = new Differential();
        nabla = new Differential();
        for (int i = 0; i < cipher1.getNUM_STATES(); i++) {
            activeWordsDelta.add(new ByteArray(cipher1.getBLOCK_SIZE_IN_BYTES()));
            activeWordsNabla.add(new ByteArray(cipher1.getBLOCK_SIZE_IN_BYTES()));
        }
        
        numActiveSboxes = 0;
        numActiveSboxesForward = 0;
        numActiveSboxesBackward = 0;
        numActiveSboxesForwardKey = 0;
        numActiveSboxesBackwardkey = 0;
        
        this.roundDelta = roundDelta;
        this.roundNabla = roundNabla;
        independent = false;
        stateOfV = cipher1.getStateOfV();
        statePreBiclique = cipher1.getInitialStateBiclique();
        
    }
    
    /**
     * Inicializa a maior parte dos atributos de Attack.
     */
    public Attack(T cifra1, T cifra2, int roundDelta, int roundNabla, int[] initialWordsDelta, int[] initialWordsNabla) {
        this(cifra1, cifra2, roundDelta, roundNabla, initialWordsDelta, initialWordsNabla, false);
    }
    
    @Override
    public String toString(){
        String result = "Attack ( delta : {key = " + roundDelta + " words : " + posBaseKeyAffectedWords +
                "} nabla : {key = " + roundNabla + " words : " + Arrays.toString(initialWordsNabla) + "})";
        return result+"\ntime complexity : C_attack ~ 2^{" + expTimeComplexity + "}";
    }
    
    /**
     * Aplica o ataque, chamando createAttack() e depois computando as PALAVRAS
     * que afetam a variável intermediária 'v' do meet-in-the-middle na fase de
     * recomputação. Além disso, calcula quantas Sboxes são necessárias em cada
     * estado e chave para tal, e finaliza calculado a complexidadede tempo do 
     * ataque.
     * @param debug seta se serão feitos os prints de teste ou não.
     */
    public void applyAttack(boolean debug) throws Exception{
        boolean steps = debug;
        //Biclique building phase
        if(steps) System.out.println("createAttack START");
        createAttack(debug);
        if(steps) System.out.println("createAttack DONE");
        
        //Meet-in-the-middle phase
        if(steps) System.out.println("computeActiveWordsThatAffectV START");
        computeActiveWordsThatAffectV();
        if(steps) System.out.println("computeActiveWordsThatAffectV DONE");
        if(steps) System.out.println("findIndependentWords START");
        findIndependentWords();
        if(steps) System.out.println("findIndependentWords DONE");
        if(steps) System.out.println("computeCandidates START");
        computeCandidates();
        if(steps) System.out.println("computeCandidates DONE");
        if(steps) System.out.println("computeBaseKeyCandidates START");
        computeBaseKeyCandidates();
        if(steps) System.out.println("computeBaseKeyCandidates DONE");
        
        if(steps) System.out.println("countSboxes START");
        countSboxes();
        if(steps) System.out.println("countSboxes DONE");
        
        if(steps) System.out.println("computeTimeComplexity START");
        computeTimeComplexity();
        if(steps) System.out.println("computeTimeComplexity DONE");

        if(debug) printActiveWordsDeltaKey();
        if(debug) printActiveWordsNablaKey();
        if(debug) printActiveWordsDifferenceKey();
        if(debug) printActiveWordsThatAffectV();
        if(steps) printIndependentWords();
        if(steps) printCandidates();
        if(debug) printBaseKeyCandidates();
        if(debug) printTimeComplexity();
    }
    
    /**
     * Calcula todas as informações das diferenciais nabla e delta, setando os
 atributos 'activeWordsDeltaKey', 'activeWordsDelta', 'delta',
 'activeWordsNablaKey', 'activeWordsNabla', 'nabla' e
 'activeWordsDifference'.
     *
     * @param debug seta se serão feitos os prints de teste ou não.
     * @param roundDelta seta quais chaves de rodada serão usadas no cálculo das
     *              diferenciais delta. 'roundDelta' e 'roundDelta' + 1.
     * @param roundNabla seta quais chaves de rodada serão usadas no cálculo das
     *              diferenciais delta. 'roundNabla' e 'roundNabla' + 1.
     * 
     */
    public void createAttack(boolean debug){
        boolean steps = false;
        //if(initialWordsDelta == null) getLowestDiffBasic(false,28);
        
        if(steps) System.out.println("computeActiveWordsDelta START");
        computeActiveWordsDelta();
        if(steps) System.out.println("computeActiveWordsDelta DONE");
                    
        if(steps) System.out.println("computeActiveWordsNabla START");
        computeActiveWordsNabla();
        if(steps) System.out.println("computeActiveWordsNabla DONE");
        
        if(steps) System.out.println("computeActiveWordsDifference START");
        computeActiveWordsDifference();
        if(steps) System.out.println("computeActiveWordsDifference DONE");
        
        if(steps) System.out.println("computeActiveWordsDeltaKey START");
        computeActiveWordsDeltaKey();
        if(steps) System.out.println("computeActiveWordsDeltaKey DONE");
        
        if(steps) System.out.println("computeActiveWordsNablaKey START");
        computeActiveWordsNablaKey();
        if(steps) System.out.println("computeActiveWordsNablaKey DONE");
        
        if(steps) System.out.println("computeActiveWordsDifferenceKey START");
        computeActiveWordsDifferenceKey();
        if(steps) System.out.println("computeActiveWordsDifferenceKey DONE");
        
        if(steps) System.out.println("checkIndependence START");
        checkIndependence();
        if(steps) System.out.println("checkIndependence DONE");
        
        if(debug) printActiveWordsDelta();
        if(debug) printActiveWordsNabla();
        if(debug) printActiveWordsDifference();
    }
    
    /**
     * Conta a quantidade de sbox lookups necessárias  na fase de recomputação
     * do Matching with precomputations (MwP).
     * 
     *  Específico para o Serpent pois depende de saber quais são os estados
     * pré e pós aplicação de Sboxes
     * 
     * @param stateOfV é o estado interno da cifra onde a variável 'v' do MwP
     *              está setada.
     */
    public void countSboxes(){
        int[] indexesPreSboxes = cipher1.getINDEXES_OF_PRE_SBOX_STATES();
        int[] indexesPostSboxes = cipher1.getINDEXES_OF_POST_SBOX_STATES();
        int[] indexesPreAK = cipher1.getINDEXES_OF_PRE_ADD_KEY();
        int[] indexesPostAK = cipher1.getINDEXES_OF_POST_ADD_KEY();
        int[] relevantKeyPos = cipher1.getSBOX_RELEVANT_KEY_WORDS();
        
        if(cipher1.getClass() == new Serpent().getClass()){
            for (int i = 0; i < activeWordsThatAffectV.size(); i++) {
                if(i <= stateOfV && i%3 == 2){// Ida e apenas os estados que foram afetados por S-boxes
                    for (int j = 0; j < AMOUNT_WORDS; j++){
                        if(activeWordsThatAffectV.get(i).getWord(cipher1.getWORD_SIZE(),j) != 0){
                            numActiveSboxes++;
                            numActiveSboxesForward++;
                        }
                    }
                    //System.out.println("Ida, "+numActiveSboxes);
                }
                if(i <= stateOfV && i%3 == 1){// Ida e apenas as subchaves que foram afetadas por S-boxes
                    for (int j = 0; j < AMOUNT_WORDS; j++){
                        if(activeWordsNablaKey.get(i/3).getWord(cipher1.getWORD_SIZE(),j) != 0 && activeWordsThatAffectV.get(i).getWord(cipher1.getWORD_SIZE(),j) != 0){
                            numActiveSboxes++;
                            numActiveSboxesForwardKey++;
                        }
                    }
                    //System.out.println("Ida, "+numActiveSboxes);
                }
                if(i >= stateOfV && i%3 == 1){// Volta e apenas os estados que foram afetados por S-boxes
                    for (int j = 0; j < AMOUNT_WORDS; j++){
                        if(activeWordsThatAffectV.get(i).getWord(cipher1.getWORD_SIZE(),j) != 0){
                            numActiveSboxes++;
                            numActiveSboxesBackward++;
                        }
                    }
                    //System.out.println("Volta, "+numActiveSboxes);
                }
                if(i >= stateOfV && i%3 == 0){// Volta e apenas as subchaves que foram afetadas por S-boxes
                    //System.out.print("state: " + i + " subkey: " + i/3);
                    //System.out.println("countSboxes : activeWordsThatAffectV.get(i) : "+activeWordsThatAffectV.get(i) + " activeWordsThatAffectV.size() : "+activeWordsThatAffectV.size());
                    //System.out.println("countSboxes : activeWordsDeltaKey.size() : "+activeWordsDeltaKey.size() + " activeWordsNablaKey.size() : "+activeWordsNablaKey.size());
                    for (int j = 0; j < AMOUNT_WORDS; j++){
                        if(activeWordsDeltaKey.get(i/3).getWord(cipher1.getWORD_SIZE(),j) != 0 && activeWordsThatAffectV.get(i).getWord(cipher1.getWORD_SIZE(),j) != 0){ 
                            numActiveSboxes++;
                            numActiveSboxesBackwardkey++;
                            //aux++;
                        }
                    }
                    //System.out.println(" total active nibbles of key: " + aux);
                    //System.out.println("Volta, "+numActiveSboxes);
                }
            }
        }
        else if(cipher1.getClass() == new AES().getClass()) {
            for (int i : indexesPreSboxes) {
                if(i <= stateOfV){          // Ida e apenas os estados que foram afetados por S-boxes
                    for (int j = 0; j < AMOUNT_WORDS; j++){
                        if(activeWordsThatAffectV.get(i).getWord(cipher1.getWORD_SIZE(),j) != 0){
                            numActiveSboxes++;
                            numActiveSboxesForward++;
                        }
                    }
                }else if(i <= statePreBiclique){        // Volta e apenas os estados que foram afetados por S-boxes
                    for (int j = 0; j < AMOUNT_WORDS; j++){
                        if(activeWordsThatAffectV.get(i).getWord(cipher1.getWORD_SIZE(),j) != 0){
                            numActiveSboxes++;
                            numActiveSboxesBackward++;
                        }
                    }
                }
            }
            for (int i : indexesPreAK) {
                
                if(i <= stateOfV){                      // Ida e apenas as subchaves que foram afetadas por S-boxes
                    for (int j = 0; j < relevantKeyPos.length; j++){
                        if(activeWordsNablaKey.get(cipher1.getINDEX_OF_PRE_KEY(i)).getWord(cipher1.getWORD_SIZE(),relevantKeyPos[j]) != 0 && activeWordsThatAffectV.get(i).getWord(cipher1.getWORD_SIZE(),j) != 0){
                            numActiveSboxes++;
                            numActiveSboxesForwardKey++;
                        }
                    }
                }else if(i <= statePreBiclique){        // Volta e apenas as subchaves que foram afetadas por S-boxes
                    for (int j = 0; j < relevantKeyPos.length; j++){
                        if(activeWordsDeltaKey.get(cipher1.getINDEX_OF_POST_KEY(i+1)).getWord(cipher1.getWORD_SIZE(),relevantKeyPos[j]) != 0 && activeWordsThatAffectV.get(i).getWord(cipher1.getWORD_SIZE(),j) != 0){ 
                            numActiveSboxes++;
                            numActiveSboxesBackwardkey++;
                        }
                    }
                }
            }
        }else{
            // Deve ser generalizado
        }
    }
    
    /**
     * Computa todos as PALAVRAS de todos os estados internos que afetam a
     * variável 'v' na recomputação do MwP.
     * 
     * (APENAS FUNCIONA PARA BICLIQUES NO FINAL DA CIFRA)
     * 
     * @param stateOfV é o estado interno da cifra onde a variável 'v' do MwP
     *              está setada.
     * @param statePreBiclique é o último estado interno da cifra antes de
     *              entrar nos estados internos da biclique.
     */
    public void computeActiveWordsThatAffectV() throws Exception{
        int marker = 0xA;   //Marcador de palavras ativas
        if(cipher1.getWORD_SIZE() == 8) marker = 0xAA;
        int markerb = 0xB;   //Marcador de palavras ativas
        if(cipher1.getWORD_SIZE() == 8) markerb = 0xBB;
        
        Cipher cifra1 = cipher1.Reset(delta.firstSecretKey, cipher1.getNUM_ROUNDS()-1);
        Cipher cifra2 = cipher1.Reset(delta.firstSecretKey, cipher1.getNUM_ROUNDS()-1);
        
        ByteArray state1 = new ByteArray(cipher1.getBLOCK_SIZE_IN_BYTES());
        ByteArray state2 = new ByteArray(cipher1.getBLOCK_SIZE_IN_BYTES());
        
        int vWord = cipher1.getWordOfV();
        
        for (int i = 1; i < cipher1.getBLOCK_SIZE_IN_BYTES(); i++) {
            state2.setWord(cipher1.getWORD_SIZE(),vWord, i);
        
            ArrayList<ByteArray> ida1 = cifra1.encryptRoundsFromStatesSavingStates(state1,stateOfV,statePreBiclique-1);
            ArrayList<ByteArray> ida2 = cifra2.encryptRoundsFromStatesSavingStates(state2,stateOfV,statePreBiclique-1);
            ArrayList<ByteArray> volta1 = cifra1.encryptRoundsBackwardsFromStatesSavingStates(state1,stateOfV,0);
            ArrayList<ByteArray> volta2 = cifra2.encryptRoundsBackwardsFromStatesSavingStates(state2,stateOfV,0);
            
            ByteArray aux;
            
            for (int j = 0; j < volta1.size(); j++) {
                aux = volta1.get(j).clone().xor(volta2.get(j));
                for (int k = 0; k < AMOUNT_WORDS; k++) {
                    if(activeWordsThatAffectV.get(j).getWord(cipher1.getWORD_SIZE(),k) == 0 && aux.getWord(cipher1.getWORD_SIZE(),k) !=0)
                        activeWordsThatAffectV.get(j).setWord(cipher1.getWORD_SIZE(),k, marker);                
                }
            }
            for (int j = 0; j < ida1.size(); j++) {
                aux = ida1.get(j).clone().xor(ida2.get(j));
                for (int k = 0; k < AMOUNT_WORDS; k++) {
                    if(activeWordsThatAffectV.get((stateOfV+j)).getWord(cipher1.getWORD_SIZE(),k) == 0 && aux.getWord(cipher1.getWORD_SIZE(),k) !=0)
                        activeWordsThatAffectV.get((stateOfV+j)).setWord(cipher1.getWORD_SIZE(),k, marker);                
                }
            }
        }
        
        for (int i = stateOfV; i < statePreBiclique; i++) 
            activeWordsThatAffectV.set(i,activeWordsThatAffectV.get(i).clone().and(activeWordsDelta.get(i)));
        
        for (int i = 0; i < stateOfV; i++) 
            activeWordsThatAffectV.set(i,activeWordsThatAffectV.get(i).clone().and(activeWordsNabla.get(i)));       
    }
    
    /**
     * Computa a complexidade de tempo do ataque completo (Precisa que a
     * contagem das sboxes já tenha sido feita).
     * 
     *  (AINDA NÃO GENERALIZADO)
     */
    public void computeTimeComplexity(){
        int k, d;
        double C_biclique, C_precomp, C_recomp, C_falpos, totalSboxes;
        
        k = (cipher1.getKEY_SIZE_IN_BYTES()*cipher1.getAMOUNT_OF_KEYS())*8;
        d = cipher1.getWORD_SIZE();
        totalSboxes = cipher1.getNUM_SBOXES_TOTAL();
        
        C_biclique = Math.pow(2, cipher1.getWORD_SIZE()+1)*
                        ((cipher1.getNUM_STATES()-cipher1.getInitialStateBiclique())/
                            (float)(cipher1.getNUM_STATES()));
        C_precomp = Math.pow(2,cipher1.getWORD_SIZE())*
                        (((cipher1.getInitialStateBiclique()-stateOfV)/(float)(cipher1.getNUM_STATES()))+
                            stateOfV/(float)(cipher1.getNUM_STATES()));
        C_recomp = Math.pow(2, Math.log(numActiveSboxes/totalSboxes)/Math.log(2)+(2*d));
        C_falpos = Math.pow(2, cipher1.getWORD_SIZE());

        expTimeComplexity = ((k-2*d)+Math.log(C_biclique+ C_precomp + C_recomp + C_falpos)/Math.log(2));  
    }
    
    /**
     * Computa as PALAVRAS ativas tanto pelas PALAVRAS ativos das diferenciais
     * nabla quanto das diferenciais delta. 'a' são as PALAVRAS ativas somente
     * por delta, 'b' somente as ativas por nabla e '1' as ativas por ambos.
     */
    public void computeActiveWordsDifference(){
        for (int i = 0; i < activeWordsNabla.size(); i++) 
            activeWordsDifference.add(activeWordsNabla.get(i).clone().xor(activeWordsDelta.get(i)));
    }    
    
    /**
     * Computa as PALAVRAS ativas tanto pelas PALAVRAS ativAs das diferenciais
     * nabla quanto das diferenciais delta nas subchaves de cifra. 'a' são as
     * PALAVRAS ativas somente por delta, 'b' somente as ativas por nabla e '1'
     * as ativas por ambas.
     */
    public void computeActiveWordsDifferenceKey(){
        int marker = 0xA;   //Marcador de palavras ativas
        if(cipher1.getWORD_SIZE() == 8) marker = 0xAA;
        int markerb = 0xB;   //Marcador de palavras ativas
        if(cipher1.getWORD_SIZE() == 8) markerb = 0xBB;
                
        ArrayList<ByteArray> aux = new ArrayList<>();
        for (int i = 0; i < activeWordsNablaKey.size(); i++) {
            aux.add(activeWordsNablaKey.get(i).clone());
            
            for (int j = 0; j < AMOUNT_WORDS; j++) 
                if(aux.get(i).getWord(cipher1.getWORD_SIZE(),j) == marker) aux.get(i).setWord(cipher1.getWORD_SIZE(),j,markerb);
        }    
        for (int i = 0; i < activeWordsNablaKey.size(); i++) 
            activeWordsDifferenceKey.add(aux.get(i).clone().xor(activeWordsDeltaKey.get(i)));
    }
    
    /**
     * Computa todas as PALAVRAS que são ativadas pelas diferenciais delta nos 
     * estados internos da cifra.
     * 
     * @param round e round + 1 são as rodadas onde a diferença de chave 
     *              'keyDiff' será definida.
     */
    public void computeActiveWordsDelta(){
        boolean show_percentage = false;
        if(!lowestDifferential.equals(new Difference())){
            if(!same){
                for (int i : posBaseKeyAffectedWords) {
                    for (int j = 1; j < 0x1<<cipher1.getWORD_SIZE(); j++) {
                        lowestDifferential.setWord(cipher1.getWORD_SIZE(),i, j);
                        computeDeltaDifferential(0, lowestDifferential);
                        //if(debug) System.out.println(delta.keyDifference);
                        int marker = 0xA;   //Marcador de palavras ativas
                        if(cipher1.getWORD_SIZE() == 8) marker = 0xAA;
                        for (int k = 0; k < delta.stateDifferences.size(); k++) {
                            for (int l = 0; l < AMOUNT_WORDS; l++) {
                                if(delta.stateDifferences.get(k).getWord(cipher1.getWORD_SIZE(),l) != 0 && activeWordsDelta.get(k).getWord(cipher1.getWORD_SIZE(),l)==0){
                                    activeWordsDelta.get(k).setWord(cipher1.getWORD_SIZE(),l, marker);
                                }  
                            }
                        }
                        if(show_percentage){
                            double aux = Math.log(j)/Math.log(2);
                            if(aux == (int)aux) System.out.println("\rProgress: "+aux+"/"+(cipher1.getWORD_SIZE()));
                        }
                    }
                }
            }else{
                throw new RuntimeException("Ainda não foi definido (computeActiveWordsDelta)");
            }
        }else{
            if(!same){
                for (int j = 1; j < 0x1<<cipher1.getWORD_SIZE(); j++) {//É necessário?
                    ByteArray aux = new ByteArray(AMOUNT_WORDS);
                    //System.out.print("computeActiveWordsDelta : k : "+(0x1<<(initialWordsDelta.length*cipher1.getWORD_SIZE()))+" initialWordsDelta.length : "+initialWordsDelta.length);
                    //Para cada possível valor para a PALAVRA (0 a 15 nos NIBBLES e 0 a 255 nos BYTES)
                    for (int k = 1; k < 0x1<<(initialWordsDelta.length*cipher1.getWORD_SIZE()); k++) {
                        for (int l = 0; l < initialWordsDelta.length; l++) {
                            aux.setWord(cipher1.getWORD_SIZE(),initialWordsDelta[l], (k>>(0x1*(l*cipher1.getWORD_SIZE()))));
                        }
                        //if(debug) System.out.println(aux);
                        computeDeltaDifferential(0, new Difference(aux));
                        //if(debug) System.out.println(nabla.keyDifference);
                        //System.out.println("computeActiveWordsDelta : m : "+delta.stateDifferences.size()+ " n : "+AMOUNT_WORDS);
                        int marker = 0xA;   //Marcador de palavras ativas
                        if(cipher1.getWORD_SIZE() == 8) marker = 0xAA;
                        for (int m = 0; m < delta.stateDifferences.size(); m++) {
                            for (int n = 0; n < AMOUNT_WORDS; n++) {
                                if(delta.stateDifferences.get(m).getWord(cipher1.getWORD_SIZE(),n) != 0 && activeWordsDelta.get(m).getWord(cipher1.getWORD_SIZE(),n)==0){
                                    activeWordsDelta.get(m).setWord(cipher1.getWORD_SIZE(),n, marker);
                                }                                
                            }
                        }
                    }
                    if(show_percentage){
                        double aux1 = Math.log(j)/Math.log(2);
                        if(aux1 == (int)aux1) System.out.println("\rProgress: "+aux1+"/"+(0x1<<cipher1.getWORD_SIZE()));
                    }
                }
            }else{
                for (int j = 1; j < 0x1<<cipher1.getWORD_SIZE(); j++) {//É necessário?
                    ByteArray aux = new ByteArray(AMOUNT_WORDS);
                    //System.out.print("computeActiveWordsDelta : k : "+(0x1<<(initialWordsDelta.length*cipher1.getWORD_SIZE()))+" initialWordsDelta.length : "+initialWordsDelta.length);
                    //Para cada possível valor para a PALAVRA (0 a 15 nos NIBBLES e 0 a 255 nos BYTES)
                    for (int k = 1; k < 0x1<<(cipher1.getWORD_SIZE()); k++) {
                        for (int l = 0; l < initialWordsDelta.length; l++) {
                            aux.setWord(cipher1.getWORD_SIZE(),initialWordsDelta[l], k);
                        }
                        //if(debug) System.out.println(aux);
                        computeDeltaDifferential(0, new Difference(aux));
                        //if(debug) System.out.println(nabla.keyDifference);
                        //System.out.println("computeActiveWordsDelta : m : "+delta.stateDifferences.size()+ " n : "+AMOUNT_WORDS);
                        int marker = 0xA;   //Marcador de palavras ativas
                        if(cipher1.getWORD_SIZE() == 8) marker = 0xAA;
                        for (int m = 0; m < delta.stateDifferences.size(); m++) {
                            for (int n = 0; n < AMOUNT_WORDS; n++) {
                                if(delta.stateDifferences.get(m).getWord(cipher1.getWORD_SIZE(),n) != 0 && activeWordsDelta.get(m).getWord(cipher1.getWORD_SIZE(),n)==0){
                                    activeWordsDelta.get(m).setWord(cipher1.getWORD_SIZE(),n, marker);
                                }                                
                            }
                        }
                    }
                    if(show_percentage){
                        double aux1 = Math.log(j)/Math.log(2);
                        if(aux1 == (int)aux1) System.out.println("\rProgress: "+aux1+"/"+(0x1<<cipher1.getWORD_SIZE()));
                    }
                }
            }
        }
    }
    
    /**
     * Computa todas as PALAVRAS que são ativadas pelas diferenciais nabla nos 
     * estados internos da cifra.
     * 
     */
    public void computeActiveWordsNabla(){
        
        for (int j = 1; j < 0x1<<cipher1.getWORD_SIZE(); j++) {//É necessário?
            ByteArray aux;
            if(!same){
                for (int k = 1; k < 0x1<<(initialWordsNabla.length*cipher1.getWORD_SIZE()); k++) {
                    aux = new ByteArray(AMOUNT_WORDS);
                    for (int l = 0; l < initialWordsNabla.length; l++) {
                        aux.setWord(cipher1.getWORD_SIZE(),initialWordsNabla[l], (k>>(0x1*(l*cipher1.getWORD_SIZE()))));
                    }
                    //if(debug) System.out.println(aux);
                    computeNablaDifferential(0, new Difference(aux));
                    //if(debug) System.out.println(nabla.keyDifference);

                    int marker = 0xB;   //Marcador de palavras ativas
                    if(cipher1.getWORD_SIZE() == 8) marker = 0xBB;
                    for (int m = 0; m < nabla.stateDifferences.size(); m++) {
                        for (int n = 0; n < AMOUNT_WORDS; n++) {
                            if(nabla.stateDifferences.get(m).getWord(cipher1.getWORD_SIZE(),n) != 0 && activeWordsNabla.get(m).getWord(cipher1.getWORD_SIZE(),n)==0){
                                activeWordsNabla.get(m).setWord(cipher1.getWORD_SIZE(),n, marker);
                            }                                
                        }
                    }
                }
            }else{
                for (int k = 1; k < 1<<(cipher1.getWORD_SIZE()); k++) {
                    aux = new ByteArray(AMOUNT_WORDS);
                    for (int l = 0; l < initialWordsNabla.length; l++) {
                        aux.setWord(cipher1.getWORD_SIZE(),initialWordsNabla[l], k);
                    }
                    //if(debug) System.out.println(aux);
                    computeNablaDifferential(0, new Difference(aux));
                    //if(debug) System.out.println(nabla.keyDifference);

                    int marker = 0xB;   //Marcador de palavras ativas
                    if(cipher1.getWORD_SIZE() == 8) marker = 0xBB;
                    for (int m = 0; m < nabla.stateDifferences.size(); m++) {
                        for (int n = 0; n < AMOUNT_WORDS; n++) {
                            if(nabla.stateDifferences.get(m).getWord(cipher1.getWORD_SIZE(),n) != 0 && activeWordsNabla.get(m).getWord(cipher1.getWORD_SIZE(),n)==0){
                                activeWordsNabla.get(m).setWord(cipher1.getWORD_SIZE(),n, marker);
                            }                                
                        }
                    }
                }
            }
        }
    }
        
    /**
     * Computa todas as PALAVRAS que são ativadas pelas diferenciais delta nas 
     * subchaves da cifra (na chave expandida).
     * 
     * @param round e round + 1 são as rodadas onde a diferença de chave 'delta'
     *              está definida.
     */
    public void computeActiveWordsDeltaKey(){      
        
        //-----------------Getting the active words of Delta key----------------------------
        //System.out.println(delta.keyDifference);
        ArrayList<Integer> indexActiveWords = delta.keyDifference.getActiveWords(cipher1.getWORD_SIZE());
        for (int i = 0; i < cipher1.getNUM_KEYS(); i++) 
            activeWordsDeltaKey.add(new ByteArray(cipher1.getKEY_SIZE_IN_BYTES()));
        for (int i = 1; i < 0x1<<cipher1.getWORD_SIZE(); i++) {
            for (int j = 1; j < 0x1<<cipher1.getWORD_SIZE(); j++) {
                ByteArray key1 = new ByteArray(AMOUNT_WORDS);
                ByteArray key2 = key1.clone();
                
                //System.out.println(indexActiveWords);
                for (int k = 0; k < indexActiveWords.size(); k++)
                    key2.setWord(cipher1.getWORD_SIZE(),indexActiveWords.get(k), i);
                
                
                Cipher cifra1 = cipher1.Reset(key1, roundDelta);
                Cipher cifra2 = cipher2.Reset(key2, roundDelta);
//                System.out.println("keyDiff : "+ key1.clone().xor(key2));
                ByteArray keyDifference = cifra1.getExpandedKey(key1, roundDelta).clone().getDifference(cifra2.getExpandedKey(key2, roundDelta));
                //System.out.println("computeActiveWordsDeltaKey here2");
                int marker = 0xA;   //Marcador de palavras ativas
                if(cipher1.getWORD_SIZE() == 8) marker = 0xAA;
                for (int k = 0; k < keyDifference.amountOfWords(cipher1.getWORD_SIZE()); k++) {
                    int key = k/AMOUNT_WORDS;
                    int word = k%AMOUNT_WORDS;
                    if (activeWordsDeltaKey.get(key).getWord(cipher1.getWORD_SIZE(),word)==0 && keyDifference.getWord(cipher1.getWORD_SIZE(),k)!=0) 
                        activeWordsDeltaKey.get(key).setWord(cipher1.getWORD_SIZE(),word,marker);
                }   
            }
        }
        
        /*System.out.println("Active Words of the Nabla key");
        for (int i = 0; i < activeWordsNablaKey.size(); i++) {
            System.out.println("#"+i+" = "+activeWordsNablaKey.get(i));
        }*/
        //------------------------------------------------------------------------------------     
        
    }
        
    /**
     * Computa todas as PALAVRAS que são ativadas pelas diferenciais nabla nas 
     * subchaves da cifra (na chave expandida).
     * 
     * @param round e round + 1 são as rodadas onde a diferença de chave 'nabla'
     *              está definida.
     */
    public void computeActiveWordsNablaKey(){
        //-----------------Getting the active words of Nabla key----------------------------
        ArrayList<Integer> indexActiveWords = nabla.keyDifference.getActiveWords(cipher1.getWORD_SIZE());
        
        for (int i = 0; i < cipher1.getNUM_KEYS(); i++) 
            activeWordsNablaKey.add(new ByteArray(cipher1.getKEY_SIZE_IN_BYTES()));
        for (int i = 1; i < 0x1<<cipher1.getWORD_SIZE(); i++) {
            for (int j = 1; j < 0x1<<cipher1.getWORD_SIZE(); j++) {
                ByteArray key1 = new ByteArray(AMOUNT_WORDS);
                ByteArray key2 = key1.clone();
                
                for (int k = 0; k < indexActiveWords.size(); k++)
                    key2.setWord(cipher1.getWORD_SIZE(),indexActiveWords.get(k), i);
                
                
                Cipher cifra1 = cipher1.Reset(key1, roundNabla);
                Cipher cifra2 = cipher2.Reset(key2, roundNabla);
                
                ByteArray keyDifference = cifra1.getExpandedKey(key1, roundNabla).clone().getDifference(cifra2.getExpandedKey(key2, roundNabla));
                
                int marker = 0xA;   //Marcador de palavras ativas
                if(cipher1.getWORD_SIZE() == 8) marker = 0xAA;
                for (int k = 0; k < keyDifference.amountOfWords(cipher1.getWORD_SIZE()); k++) {
                    int key = k/AMOUNT_WORDS;
                    int word = k%AMOUNT_WORDS;
                    if (activeWordsNablaKey.get(key).getWord(cipher1.getWORD_SIZE(),word)==0 && keyDifference.getWord(cipher1.getWORD_SIZE(),k)!=0) 
                        activeWordsNablaKey.get(key).setWord(cipher1.getWORD_SIZE(),word,marker);
                }   
            }
        }
        
        /*System.out.println("Active Words of the Nabla key");
        for (int i = 0; i < activeWordsNablaKey.size(); i++) {
            System.out.println("#"+i+" = "+activeWordsNablaKey.get(i));
        }*/
        //------------------------------------------------------------------------------------     
        
    }
    
    public double getExpTimeComplexity() {
        return expTimeComplexity;
    }
    
    public void printActiveWordsThatAffectV(){
        System.out.println("Words that influence variable v = ");
        for (int i = 0; i < activeWordsThatAffectV.size(); i++) 
            System.out.println("#"+i+" = "+activeWordsThatAffectV.get(i));
    }
    
    public void printTimeComplexity(){
        int k, d;
        double C_biclique, C_precomp, C_recomp, C_falpos, totalSboxes;
        
        k = (cipher1.getKEY_SIZE_IN_BYTES()*cipher1.getAMOUNT_OF_KEYS())*8;
        d = cipher1.getWORD_SIZE();
        totalSboxes = cipher1.getNUM_SBOXES_TOTAL();
        
        C_biclique = Math.pow(2, cipher1.getWORD_SIZE()+1)*
                        ((cipher1.getNUM_STATES()-cipher1.getInitialStateBiclique())/
                            (float)(cipher1.getNUM_STATES()));
        C_precomp = Math.pow(2,cipher1.getWORD_SIZE())*
                        (((cipher1.getInitialStateBiclique()-stateOfV)/(float)(cipher1.getNUM_STATES()))+
                            stateOfV/(float)(cipher1.getNUM_STATES()));
        C_recomp = Math.pow(2, Math.log(numActiveSboxes/totalSboxes)/Math.log(2)+(2*d));
        C_falpos = Math.pow(2, cipher1.getWORD_SIZE());
        
        System.out.println("#Active Sboxes Forward = "+numActiveSboxesForward);
        System.out.println("#Active Sboxes Forward Key = "+numActiveSboxesForwardKey);
        System.out.println("#Active Sboxes Backward = "+numActiveSboxesBackward);
        System.out.println("#Active Sboxes Backward Key = "+numActiveSboxesBackwardkey);
        System.out.println("#Active Sboxes = "+numActiveSboxes+"/"+totalSboxes+" = "+((double)numActiveSboxes/totalSboxes*100)+"% of the whole cipher, which is");
        System.out.println("(2^{"+2*d+"}*"
                +((double)numActiveSboxes/totalSboxes)+" = "+C_recomp+" = 2^{"+(Math.log(C_recomp)/Math.log(2))+"})");
        
        System.out.println("C_attack = 2^{k-2d}*(C_biclique + C_precomp + C_recomp + C_falpos)");
        System.out.println("C_attack = 2^{"+(k-2*d)+"}*(((2^{"+cipher1.getWORD_SIZE()+"}+2^{"+cipher1.getWORD_SIZE()+"})*("+(cipher1.getNUM_STATES()-cipher1.getInitialStateBiclique())+"/"+cipher1.getNUM_STATES()+")) + (2^{"+cipher1.getWORD_SIZE()+"}*("+(cipher1.getInitialStateBiclique()-stateOfV)+"/"+cipher1.getNUM_STATES()+") + 2^{"+cipher1.getWORD_SIZE()+"}*("+stateOfV+"/"+cipher1.getNUM_STATES()+")) + 2^{"+Math.log(((double)numActiveSboxes/totalSboxes)*(2*Math.pow(2, d)))/Math.log(2)+"} + 2^{"+2*cipher1.getWORD_SIZE()+"-"+cipher1.getWORD_SIZE()+"})");
        System.out.println("C_attack = 2^{"+(k-2*d)+"}*(2^{"+Math.log(C_biclique)/Math.log(2)+"} + 2^{"+Math.log(C_precomp)/Math.log(2)+"} + 2^{"+Math.log(((double)numActiveSboxes/totalSboxes)*k)/Math.log(2)+"} + 2^{"+Math.log(C_falpos)/Math.log(2)+"})");
        System.out.println("C_attack = 2^{"+expTimeComplexity+"}");
        System.out.printf("C_attack ~ 2^{%.2f}\n",expTimeComplexity);
        
//        System.out.println("C_attack = 2^{k-2d}*(C_biclique + C_precomp + C_recomp + C_falpos)");
//        System.out.println("C_attack = 2^{248}*(((2^{4}+2^{4})*(5/96)) + (2^{4}*(75/96) + 2^{4}*(16/96)) + 2^{"+Math.log(((double)numActiveSboxes/2080)*256)/Math.log(2)+"} + 2^{8-4})");
//        System.out.println("C_attack = 2^{248}*(2^{0.736965594166206} + 2^{3.922832139477540} + 2^{"+Math.log(((double)numActiveSboxes/2080)*256)/Math.log(2)+"} + 2^4)");
//        System.out.println("C_attack = 2^{"+(248+Math.log(Math.pow(2, 0.736965594166206)+Math.pow(2, 3.922832139477540)+Math.pow(2, Math.log(((double)numActiveSboxes/2080)*256)/Math.log(2))+Math.pow(2, 4))/Math.log(2))+"}");
//        System.out.printf("C_attack ~ 2^{%.2f}\n",expTimeComplexity);
        
    }
    
    public void printActiveWordsDeltaKey(){
        System.out.println("Active Words of the Delta key");
        for (int i = 0; i < activeWordsDeltaKey.size(); i++) {
            System.out.println("K"+i+" = "+activeWordsDeltaKey.get(i));
        }
    }
    
    public void printActiveWordsNablaKey(){
        System.out.println("Active Words of the Nabla key");
        for (int i = 0; i < activeWordsNablaKey.size(); i++) {
            System.out.println("K"+i+" = "+activeWordsNablaKey.get(i));
        }
    }
    
    public void printActiveWordsDelta(){
        System.out.println("\nDelta Differentials =");
        for (int i = 0; i < activeWordsDelta.size(); i++) {
            System.out.println("#"+i+" : "+activeWordsDelta.get(i));
        }
    }
    
    public void printActiveWordsNabla(){
        System.out.println("\nNabla Differentials =");
        for (int i = 0; i < activeWordsNabla.size(); i++) {
            System.out.println("#"+i+" : "+activeWordsNabla.get(i));
        }
    }
    
    public void printActiveWordsDifference(){
        System.out.println("\nSee independence of internal states (a is for Delta, b is for Nabla, 1 is for both and 0 is none) =");
        for (int i = 0; i < activeWordsDifference.size(); i++) {
            if (i == 0) System.out.println("#"+i+" P\t="+activeWordsDifference.get(i));
            else if (i%3 == 1) System.out.println("#"+i+" AK"+((i+2)/3)+"\t="+activeWordsDifference.get(i));
            else if (i%3 == 2) System.out.println("#"+i+" S"+((i+1)/3)+"\t\t="+activeWordsDifference.get(i));
            else if (i%3 == 0 && i/3 !=AMOUNT_WORDS) System.out.println("#"+i+" L"+(i/3)+"\t\t="+activeWordsDifference.get(i));
            else System.out.println("#"+i+" AK33 = C\t="+activeWordsDifference.get(i));
        }
    }
        
    public void printActiveWordsDifferenceKey(){
        System.out.println("\nSee independence of subkeys (a is for Delta, b is for Nabla, 1 is for both and 0 is none) =");
        for (int i = 0; i < activeWordsDifferenceKey.size(); i++) {
            if (i < 10) System.out.println("K"+i+"  ="+activeWordsDifferenceKey.get(i));
            else System.out.println("K"+i+" ="+activeWordsDifferenceKey.get(i));
        }
    }
    
    /**
     * Calcula a diferencial delta a partir da diferença de chave 'keyDiff'
     * aplicada nas rodadas 'round' e 'round'+1.
     *
     * @param debug seta quais prints de teste serão feitos. 0 é nenhum, 1 é
     *              alguns e 2 são todos.
     * @param keyDiff é a diferença de chave usada para recuperar a diferencial
     *              nabla.
     */
    public void computeDeltaDifferential(int debug, Difference keyDiff){
//        System.out.println("keyDiff : "+keyDiff);
        ByteArray key1 = new ByteArray(cipher1.getKEY_SIZE_IN_BYTES()*cipher1.getAMOUNT_OF_KEYS());   //Chave Base
        ByteArray key2 = keyDiff.xorDifference(key1);                       //Chave Base 2
        
        Cipher cifra1 = cipher1.Reset(key1, roundDelta);        //Usada para expandir a chave
        Cipher cifra2 = cipher2.Reset(key2, roundDelta);        //Usada para expandir a chave 2
        
//        System.out.println("keyDiff : "+keyDiff);
//        ArrayList<ByteArray> a = cifra1.getExpandedKey(key1, roundDelta).clone().getDifference(cifra2.getExpandedKey(key2, roundDelta)).split(16);
//        for (ByteArray b : a) {
//            System.out.println(b);
//        }
//        System.out.println("");
        ByteArray state1 = new ByteArray(cipher1.getBLOCK_SIZE_IN_BYTES());     //Estado Base
        state1.randomize();
        ByteArray state2 = state1.clone();                                      //Estado Base 2
        
        //System.out.println("cipher1 encryptFullSavingStates begin");
        ArrayList<ByteArray> allStates1 = cifra1.encryptFullSavingStates(state1, cipher1.getInitialKeyBiclique());
        //System.out.println("cipher1 encryptFullSavingStates end");
        //System.out.println("cipher2 encryptFullSavingStates begin");
        ArrayList<ByteArray> allStates2 = cifra2.encryptFullSavingStates(state2, cipher1.getInitialKeyBiclique());
        //System.out.println("cipher2 encryptFullSavingStates end");
        
        ArrayList<Difference> allStatesDiff = new ArrayList<>();        
        
        for (int i = 0; i < allStates1.size(); i++){
            allStatesDiff.add(new Difference(allStates1.get(i).clone().xor(allStates2.get(i))));      
            //System.out.print(allStatesDiff.get(i));
        }
        //System.out.println("");
        delta = new Differential(1, AMOUNT_WORDS);
        delta.stateDifferences = allStatesDiff;
        delta.keyDifferences = Difference.toDifferenceArrayList(cifra1.getExpandedKey().getDifference(cifra2.getExpandedKey()).split(cipher1.getROUND_KEY_SIZE_IN_BYTES()));
        delta.fromRound = 1;
        delta.toRound = cipher1.getNUM_ROUNDS();
        delta.firstSecretKey = key1;
        delta.secondSecretKey = key2;
        delta.keyDifference = keyDiff.getDelta();
        delta.intermediateStateDifferences = allStatesDiff;
    } 
    
    /**
     * Calcula a diferencial nabla a partir da diferença de chave 'keyDiff'
     * aplicada nas rodadas 'round' e 'round'+1.
     *
     * @param debug seta quais prints de teste serão feitos. 0 é nenhum, 1 é
     *              alguns e 2 são todos.
     * @param keyDiff é a diferença de chave usada para recuperar a diferencial
     *              nabla.
     * @param round e round + 1 são as rodadas onde a diferença de chave 
     *              'keyDiff' está definida.
     */
    public void computeNablaDifferential(int debug, Difference keyDiff){        
        ByteArray key1 = new ByteArray(cipher1.getKEY_SIZE_IN_BYTES()*cipher1.getAMOUNT_OF_KEYS());   //Chave Base
        key1.randomize();
        ByteArray key2 = keyDiff.xorDifference(key1);                       //Chave Base 2
        
        Cipher cifra1 = cipher1.Reset(key1,roundNabla);                       //Usada para expandir a chave
        Cipher cifra2 = cipher2.Reset(key2, roundNabla);                      //Usada para expandir a chave 2
        
        Difference stateDiff = new Difference(cipher1.getBLOCK_SIZE_IN_BYTES()); //Diferencial dos estados
        
        ByteArray state1 = new ByteArray(cipher1.getBLOCK_SIZE_IN_BYTES());      //Estado Base
        state1.randomize();
        ByteArray state2 = stateDiff.xorDifference(state1);                 //Estado Base 2
        
        ByteArray cypherText1;
        ByteArray cypherText2;

        cifra1.setRoundOfFirstKeyToBeApplied(cipher1.getNUM_KEYS()-1);
        cifra2.setRoundOfFirstKeyToBeApplied(cipher1.getNUM_KEYS()-1);
        
        ArrayList<ByteArray> allStates1 = cifra1.encryptFullSavingStates(state1, cipher1.getNUM_KEYS()-1);
        ArrayList<ByteArray> allStates2 = cifra2.encryptFullSavingStates(state2, cipher1.getNUM_KEYS()-1);
        ArrayList<ByteArray> allStates3 = cifra1.encryptFullSavingStates(state1, 0);
        ArrayList<ByteArray> allStates4 = cifra2.encryptFullSavingStates(state2, 0);
        
        ArrayList<Difference> allStatesDiff = new ArrayList<>();
        ArrayList<Difference> allKeysDiff = new ArrayList<>();
        
        for (int i = 0; i < allStates1.size(); i++) 
            allStatesDiff.add(new Difference(allStates1.get(i).clone().xor(allStates2.get(i))).and(new Difference(allStates3.get(i).clone().xor(allStates4.get(i)))));  
        
        nabla = new Differential(1, AMOUNT_WORDS);
        nabla.stateDifferences = allStatesDiff;
        nabla.keyDifferences = Difference.toDifferenceArrayList(cifra1.getExpandedKey().getDifference(cifra2.getExpandedKey()).split(cipher1.getROUND_KEY_SIZE_IN_BYTES()));
        nabla.fromRound = 1;
        nabla.toRound = cipher1.getNUM_ROUNDS();
        nabla.firstSecretKey = key1;
        nabla.secondSecretKey = key2;
        nabla.keyDifference = keyDiff.getDelta();
        nabla.intermediateStateDifferences = allStatesDiff;
    }
        
    public void checkIndependence() {
//         Checa a indepêndencia dos estados internos
//        System.out.println("checkIndependence: activeWordsDifference.size() = "+activeWordsDifference.size());
//        System.out.println("activeWordsDifference = "+activeWordsDifference);
        for (int i = cipher1.getInitialStateBiclique(); i < activeWordsDifference.size(); i++) {
            for (int j = 0; j < cipher1.getBLOCK_SIZE_IN_BYTES()*2; j++) {
                if(activeWordsDifference.get(i).getNibble(j) == 0x1){       // It is nibble for every cipher with words bigger than or equal to a nibble
//                    System.out.println("dependent nibble "+j+" of state "+i);
                    independent = false;
                    return;
                }
            }
        }        
        
        // Checa a indepêndencia das subchaves
        for (int i = cipher1.getInitialKeyBiclique(); i < activeWordsDifferenceKey.size(); i++) {
            for (int j = 0; j < cipher1.getKEY_SIZE_IN_BYTES()*2; j++) {
                if(activeWordsDifferenceKey.get(i).getNibble(j) == 0x1){   // It is nibble for every cipher with words bigger than or equal to a nibble
//                    System.out.println("dependent nibble "+j+" of key "+i);
                    independent = false;
                    return;
                }
            }
        }
        //printActiveWordsDifferenceKey();
        independent = true;
    }
    
    public boolean isIndependent(){
        return independent;
    }
    
    public void findIndependentWords(){
        int marker = 0xA;   //Marcador de palavras ativas
        if(cipher1.getWORD_SIZE() == 8) marker = 0xAA;
        int markerb = 0xB;   //Marcador de palavras ativas
        if(cipher1.getWORD_SIZE() == 8) markerb = 0xBB;
                
        for (int i = 0; i < activeWordsDifferenceKey.size(); i++) {
            for (int j = 0; j < AMOUNT_WORDS; j++) {
                if(activeWordsDifferenceKey.get(i).getWord(cipher1.getWORD_SIZE(),j) == marker)
                    independentWords.add(new IndependentWord(cipher1.getWORD_SIZE(), i, j, true));
                else if(activeWordsDifferenceKey.get(i).getWord(cipher1.getWORD_SIZE(),j) == markerb)
                    independentWords.add(new IndependentWord(cipher1.getWORD_SIZE(), i, j, false));
            }
        }
    }
    
    public void computeCandidates(){        
        //Nabla
        ArrayList<Integer> indexActiveWords = nabla.keyDifference.getActiveWords(cipher1.getWORD_SIZE());
        boolean temp = true;
        for(int active : indexActiveWords){
            //if(temp) temp = false;
            //else{
            for (int i = 0; i < 0x1<<cipher1.getWORD_SIZE(); i++) {
                ByteArray key1 = new ByteArray(AMOUNT_WORDS);
                ByteArray key2 = key1.clone();

                key2.setWord(cipher1.getWORD_SIZE(),active, i);

                Cipher cifra1 = cipher1.Reset(key1, roundNabla);
                Cipher cifra2 = cipher2.Reset(key2, roundNabla);

                ByteArray keyDifference = cifra1.getExpandedKey(key1, roundNabla).clone().getDifference(cifra2.getExpandedKey(key2, roundNabla));
                
                
                for (int k = 0; k < keyDifference.amountOfWords(cipher1.getWORD_SIZE()); k++) {
                    int key = k/AMOUNT_WORDS;
                    int word = k%AMOUNT_WORDS;

                    /*//Busca se o word está na lista de PALAVRAS independentes
                    int aux = independentWords.indexOf(new IndependentWord(key, word, false));
                    if(aux != -1) independentWords.get(aux).valueShowedUp(true, keyDifference.getWord(cipher1.getWORD_SIZE(),k));*/
                    //Busca se o word está na lista de PALAVRAS candidatas
                    int aux = independentWords.indexOf(new IndependentWord(cifra1.getWORD_SIZE(), key, word, false));
                    if(aux != -1){
                        int aux2 = candidateWords.indexOf(new IndependentWord(cifra1.getWORD_SIZE(), key, word, active, false));
                        if(aux2 == -1){
                            candidateWords.add(independentWords.get(aux).clone());
                            aux2 = candidateWords.size()-1;
                            candidateWords.get(aux2).generatedBy = active;
                        }
                        candidateWords.get(aux2).valueShowedUp(true, keyDifference.getWord(cipher1.getWORD_SIZE(),k));
                    }
                    
                }   
            }
            //temp = false;
            //break;
            //}
        }
        //Delta
        indexActiveWords = delta.keyDifference.getActiveWords(cipher1.getWORD_SIZE());
        for(int active : indexActiveWords){
            for (int i = 0; i < 0x1<<cipher1.getWORD_SIZE(); i++) {
                ByteArray key1 = new ByteArray(AMOUNT_WORDS);
                ByteArray key2 = key1.clone();

                key2.setWord(cipher1.getWORD_SIZE(),active, i);

                Cipher cifra1 = cipher1.Reset(key1, roundDelta);
                Cipher cifra2 = cipher2.Reset(key2, roundDelta);

                ByteArray keyDifference = cifra1.getExpandedKey(key1, roundDelta).clone().getDifference(cifra2.getExpandedKey(key2, roundDelta));
                
                
                for (int k = 0; k < keyDifference.amountOfWords(cipher1.getWORD_SIZE()); k++) {
                    int key = k/AMOUNT_WORDS;
                    int word = k%AMOUNT_WORDS;

                    /*//Busca se o word está na lista de PALAVRAS independentes
                    int aux = independentWords.indexOf(new IndependentWord(key, word, true));
                    if(aux != -1) independentWords.get(aux).valueShowedUp(true, keyDifference.getWord(cipher1.getWORD_SIZE(),k));*/
                    //Busca se o word está na lista de PALAVRAS candidatas
                    int aux = independentWords.indexOf(new IndependentWord(cifra1.getWORD_SIZE(), key, word, -1, true));
                    if(aux != -1){
                        int aux2 = candidateWords.indexOf(new IndependentWord(cifra1.getWORD_SIZE(), key, word, active, true));
                        if(aux2 == -1){
                            candidateWords.add(independentWords.get(aux).clone());
                            aux2 = candidateWords.size()-1;
                            candidateWords.get(aux2).generatedBy = active;
                        }
                        candidateWords.get(aux2).valueShowedUp(true, keyDifference.getWord(cipher1.getWORD_SIZE(),k));
                    }
                }   
            }
        }
        /*for (int i = 0; i < independentWords.size(); i++) {
            if(independentWords.get(i).isCandidate())
                candidateWords.add(independentWords.get(i));
        }*/
        
        //Remove as PALAVRAS independentes que não são candidatos
        int i = 0;
        int size = candidateWords.size();
        while(i < size) {
            if(!(candidateWords.get(i).isCandidate())){
                candidateWords.remove(i);
                i--;
                size--;
            }
            i++;
        }
        
        //Remove as PALAVRAS candidatas que são afetados por mais de um word.
        LinkedList<Integer>  aux = new LinkedList<>();
        i = 0;
        size = candidateWords.size();
        while(i < size){
            //Encontra as posições das PALAVRAS "iguais" à PALAVRA 'i', caso haja.
            for (int j = i+1; j < size; j++) {
                if( candidateWords.get(i).activatedByDelta == candidateWords.get(j).activatedByDelta &&
                    candidateWords.get(i).pos == candidateWords.get(j).pos &&
                    candidateWords.get(i).subkey == candidateWords.get(j).subkey){
                    aux.add(j);
                }
            }
            //Remove dos candidatos as PALAVRAS repetidas, caso haja.
            for (int j : aux) {
                candidateWords.remove(j);
                size--;
            }
            
            //Se havia algum word repetido antes, o próprio 'i' deve ser também removido.
            if(aux.size() > 0){
                aux.clear();
                candidateWords.remove(i);
                size--;
                i--;
            }
            
            i++;
        }
    }

    public void computeBaseKeyCandidates(){
        for (int i = 0; i < candidateWords.size(); i++) {
            int aux = -1;
            for (int j = 0; j < baseKeyCandidates.size(); j++){
                if(candidateWords.get(i).subkey == baseKeyCandidates.get(j).subkey){
                    aux = j;
                    break;
                }
            }    
            
            if (aux == -1){
                int lastPos = baseKeyCandidates.size();
                baseKeyCandidates.add(new BaseKey(candidateWords.get(i).subkey));
                baseKeyCandidates.get(lastPos).candidates.add(candidateWords.get(i));
            }else baseKeyCandidates.get(aux).candidates.add(candidateWords.get(i));
        }
        
        //Remove as chaves que não são candidatas
        int i = 0;
        int size = baseKeyCandidates.size();
        while(i < size) {
            if(!(baseKeyCandidates.get(i).isCandidate())){
                baseKeyCandidates.remove(i);
                i--;
                size--;
            }
            i++;
        }
        
        baseKeyCandidates.sort((BaseKey o1, BaseKey o2) -> o1.subkey - o2.subkey);
    }
    
    public void printIndependentWords(){
        System.out.println("Words that are independent from either nabla or delta differentials: ");
        for (int i = 0; i < independentWords.size(); i++)
            System.out.println(independentWords.get(i).toStringBasic());
    }    
    
    public void printCandidates(){
        System.out.println("Words that are candidates for the Base Key: ");
        for (int i = 0; i < candidateWords.size(); i++)
            System.out.println(candidateWords.get(i));    
    }
    
    public void printBaseKeyCandidates(){
        System.out.println("Candidates for the Base Key: ");
        for (int i = 0; i < baseKeyCandidates.size(); i++)
            System.out.println(baseKeyCandidates.get(i));    
    }

    private class BaseKey{
        int subkey;
        public ArrayList<IndependentWord> candidates;

        public BaseKey(int subkey) {
            this.subkey = subkey;
            candidates = new ArrayList<>();
        }
        
        public boolean isCandidate(){
            boolean nabla = false;
            boolean delta = false;
            for (IndependentWord word : candidates) {
                if(word.activatedByDelta) delta = true;
                else nabla = true;
                if(delta && nabla) return true;
            }
            return false;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 53 * hash + this.subkey;
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final BaseKey other = (BaseKey) obj;
            if (this.subkey != other.subkey) {
                return false;
            }
            return true;
        }
        
        @Override
        public String toString(){
            String result = "K["+subkey+"] = {\n";
            for (IndependentWord word : candidates) result += word.toStringBasic() + "\n";
            return result + "}";
        }
    }
    
    private class IndependentWord implements Cloneable{
        
        int wordSize;               //Tamanho da palavra que está sendo trabalhada
        int subkey;                 //subchave à qual a palavra está associada.
        int pos;                    //posição na subchave a qual a palavra está.
        int generatedBy;            //é a posição da palavra que gerou esta palavra.
        boolean activatedByDelta;   //é verdadeiro se a palavra for ativada por delta e falso se for por nabla.
        int[] possibleValues;       //vetor que atesta se todos os valores possíveis apareceram para aquela palavra.

        public IndependentWord(int wordSize, int subkey, int pos, boolean activatedByDelta) {
            this.wordSize = wordSize;
            this.subkey = subkey;
            this.activatedByDelta = activatedByDelta;
            this.generatedBy = -1;
            this.pos = pos;
            possibleValues = new int[0x1<<cipher1.getWORD_SIZE()];
        }
        
        public IndependentWord(int wordSize, int subkey, int pos, int generatedBy, boolean activatedByDelta) {
            this.wordSize = wordSize;
            this.subkey = subkey;
            this.activatedByDelta = activatedByDelta;
            this.generatedBy = generatedBy;
            this.pos = pos;
            possibleValues = new int[0x1<<cipher1.getWORD_SIZE()];
        }
        
        public void valueShowedUp(boolean positive, int value){
            if(positive) possibleValues[value]++;
            else possibleValues[value]+=5;
        }
        
        public boolean isCandidate(){
            if (possibleValues[0] == 0) return false;
            
            int aux = possibleValues[0];
            for (int i = 1; i < possibleValues.length; i++) 
                if(possibleValues[i] != possibleValues[0]) return false;
            return true;
        }
        
        @Override
        public IndependentWord clone() {
            IndependentWord newWord = new IndependentWord(wordSize, subkey, pos, generatedBy, activatedByDelta);
            System.arraycopy(possibleValues, 0, newWord.possibleValues, 0, possibleValues.length);
            return newWord;
        }
        
        @Override
        public String toString(){
            String wordName = "word";
            if(wordSize == 8) wordName = "byte";
            else if(wordSize == 8) wordName = "nibble";
            String result = "";
            
            if(subkey<10) result += " K" + subkey + "[";
            else result += "K" + subkey + "[";
            if(pos < 10) result += " " + pos + "] from "+wordName+" ";
            else result += pos + "] from "+wordName+" ";
            if(generatedBy == -1) result += "?? of ";
            else if(generatedBy < 10) result += " " + generatedBy + " of ";
            else  result += generatedBy + " of ";
            if(activatedByDelta)   result += "delta is ";
            else        result += "nabla is ";
            if(isCandidate()) return result += "a candidate.";// (" + Arrays.toString(possibleValues) + ")";
            return result += "NOT a candidate.";// (" + Arrays.toString(possibleValues) + ")";
        }
        
        public String toStringBasic(){
            String wordName = "word";
            if(wordSize == 8) wordName = "byte";
            else if(wordSize == 8) wordName = "nibble";
            
            String result = "";
            if(subkey<10) result += " K" + subkey + "[";
            else result += "K" + subkey + "[";
            if(pos < 10) result += " " + pos + "] from "+wordName+" ";
            else result += pos + "] from "+wordName+" ";
            if(generatedBy == -1) result += "?? of ";
            else if(generatedBy < 10) result += " " + generatedBy + " of ";
            else  result += generatedBy + " of ";
            if(activatedByDelta)   return result + "delta.";
            return result + "nabla.";
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 79 * hash + this.subkey;
            hash = 79 * hash + this.pos;
            hash = 79 * hash + this.generatedBy;
            hash = 79 * hash + (this.activatedByDelta ? 1 : 0);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final IndependentWord other = (IndependentWord) obj;
            if (this.subkey != other.subkey) {
                return false;
            }
            if (this.pos != other.pos) {
                return false;
            }
            if (this.generatedBy != other.generatedBy) {
                return false;
            }
            if (this.activatedByDelta != other.activatedByDelta) {
                return false;
            }
            return true;
        }

        
    }
    
    /**
     * Inicializa a maior parte dos atributos de Attack.
     */
    /*public Attack() {
        activeWordsThatAffectV = new ArrayList<>();
        for (int i = 0; i < 91; i++) {
            activeWordsThatAffectV.add(new ByteArray(16));
        }
        activeWordsDelta = new ArrayList<>();
        activeWordsNabla = new ArrayList<>();
        activeWordsDifference = new ArrayList<>();
        activeWordsDeltaKey = new ArrayList<>();
        activeWordsNablaKey = new ArrayList<>();
        activeWordsDifferenceKey = new ArrayList<>();
        independentWords = new ArrayList<>();
        candidateWords = new ArrayList<>();
        baseKeyCandidates = new ArrayList<>();
        initialWordsNabla = new int[2];
        initialWordsNabla[0] = 5;
        initialWordsNabla[1] = 26;
        initialWordsDelta = null;
        
        delta = new Differential();
        nabla = new Differential();
        for (int i = 0; i < 97; i++) {
            activeWordsDelta.add(new ByteArray(16));
            activeWordsNabla.add(new ByteArray(16));
        }
        
        numActiveSboxes = 0;
        numActiveSboxesForward = 0;
        numActiveSboxesBackward = 0;
        numActiveSboxesForwardKey = 0;
        numActiveSboxesBackwardkey = 0;
        
        stateOfV = 75;
        statePreBiclique = 90;
        roundDelta = 31;
        roundNabla = 30;
        independent = false;
    }*/
    
    /**
     * Inicializa a maior parte dos atributos de Attack.
     */
    /*public Attack(int roundDelta, int roundNabla) {
        activeWordsThatAffectV = new ArrayList<>();
        for (int i = 0; i < 91; i++) {
            activeWordsThatAffectV.add(new ByteArray(16));
        }
        activeWordsDelta = new ArrayList<>();
        activeWordsNabla = new ArrayList<>();
        activeWordsDifference = new ArrayList<>();
        activeWordsDeltaKey = new ArrayList<>();
        activeWordsNablaKey = new ArrayList<>();
        activeWordsDifferenceKey = new ArrayList<>();
        independentWords = new ArrayList<>();
        candidateWords = new ArrayList<>();
        baseKeyCandidates = new ArrayList<>();
        initialWordsNabla = new int[2];
        initialWordsNabla[0] = 5;
        initialWordsNabla[1] = 26;
        initialWordsDelta = null;
        
        delta = new Differential();
        nabla = new Differential();
        for (int i = 0; i < 97; i++) {
            activeWordsDelta.add(new ByteArray(16));
            activeWordsNabla.add(new ByteArray(16));
        }
        
        numActiveSboxes = 0;
        numActiveSboxesForward = 0;
        numActiveSboxesBackward = 0;
        numActiveSboxesForwardKey = 0;
        numActiveSboxesBackwardkey = 0;
        
        stateOfV = 75;
        statePreBiclique = 90;
        this.roundDelta = roundDelta;
        this.roundNabla = roundNabla;
        independent = false;
    }*/
    
    /**
     * Inicializa a maior parte dos atributos de Attack.
     */
    /*public Attack(int roundDelta, int roundNabla, int[] initialWordsNabla) {
        activeWordsThatAffectV = new ArrayList<>();
        for (int i = 0; i < 91; i++) {
            activeWordsThatAffectV.add(new ByteArray(16));
        }
        activeWordsDelta = new ArrayList<>();
        activeWordsNabla = new ArrayList<>();
        activeWordsDifference = new ArrayList<>();
        activeWordsDeltaKey = new ArrayList<>();
        activeWordsNablaKey = new ArrayList<>();
        activeWordsDifferenceKey = new ArrayList<>();
        independentWords = new ArrayList<>();
        candidateWords = new ArrayList<>();
        baseKeyCandidates = new ArrayList<>();
        this.initialWordsNabla = Arrays.copyOf(initialWordsNabla, initialWordsNabla.length);
        initialWordsDelta = null;
        
        delta = new Differential();
        nabla = new Differential();
        for (int i = 0; i < 97; i++) {
            activeWordsDelta.add(new ByteArray(16));
            activeWordsNabla.add(new ByteArray(16));
        }
        
        numActiveSboxes = 0;
        numActiveSboxesForward = 0;
        numActiveSboxesBackward = 0;
        numActiveSboxesForwardKey = 0;
        numActiveSboxesBackwardkey = 0;
        
        stateOfV = 75;
        statePreBiclique = 90;
        this.roundDelta = roundDelta;
        this.roundNabla = roundNabla;
        independent = false;
    }*/
    
    /**
     * Calcula, entre outras informações, a diferencial de chave nas rodadas 
     * 'round1' e 'round1' + 1 que ativam a menor quantidade de bits nas rodadas
     * 'comparableRound' e ('comparableRound'+1).
     * 
     * @param debug seta se os prints de teste serão feitos ou não.
     * @param round1 é o primeiro índice das duas chaves consecutivas que serão
     * testadas. O máximo é 31 (Chaves variam de 0 a 32).
     * @param comparableRound é a rodada utilizada para checar a quantidade de 
     * PALAVRAS ativas na chave. Não pode ser maior que 31.
     * 
     */
    /*public void getLowestDiffBasic(boolean debug, int comparableRound){
        
        Scanner scanner = new Scanner(System.in);
        Cipher cifra = cipher1.Reset(new ByteArray(AMOUNT_WORDS));
        //Cipher cifra = new Serpent();
        //cifra.setKey(new ByteArray(32));
        ByteArray expandedKey = cifra.getExpandedKey();
        
        ByteArray expandedKeyNoSbox = cifra.getExpandedKeyNoSbox();
        
        ByteArray leastActive = null;
        ByteArray leastActiveExpandedKey = null;
        int leastNumActive = 1000;
        int numMaxActWords;
        ArrayList<ByteArray> activeWordsExpandedKey = null;
        
        for (int i = 0; i < 64; i++) {
            ArrayList<ByteArray> activeWordsExpandedKeyAux = new ArrayList<>();
            for (int j = 0; j < cipher1.getNUM_KEYS(); j++) {
                activeWordsExpandedKeyAux.add(new ByteArray(cipher1.getBLOCK_SIZE_IN_BYTES()));
            }
            numMaxActWords = 0;
            ByteArray k30k31 = null;
            ByteArray k30k31_ = null;
            ByteArray k0k1 = null;
            ByteArray k0k1_ = null;
            ByteArray expandedKeyk0k1 = null;
            ByteArray expandedKeyk0k1_ = null;
            ByteArray expandedKeyDifference = null;
            for (int j = 1; j < 16; j++) {
                k0k1 = new ByteArray(AMOUNT_WORDS);
                k0k1_ = new ByteArray(AMOUNT_WORDS);
                k0k1.copyBytes(expandedKeyNoSbox, 16*roundDelta, 0, AMOUNT_WORDS);
                k0k1_.copyBytes(expandedKeyNoSbox, 16*roundDelta, 0, AMOUNT_WORDS);

                k0k1_.setWord(cipher1.getWORD_SIZE(),i, j^k0k1_.getWord(cipher1.getWORD_SIZE(),i));
                if(debug) System.out.println("K \\oplus K' =\n"+k0k1.getDifference(k0k1_));
                if(debug) System.out.println("");
                
                expandedKeyk0k1 = cifra.getExpandedKey(k0k1,roundDelta);
                expandedKeyk0k1_ = cifra.getExpandedKey(k0k1_,roundDelta);
                expandedKeyDifference = expandedKeyk0k1.getDifference(expandedKeyk0k1_);
                if(debug) System.out.println("expandedKey k31\\oplus expandedKey k`31 =\n"+expandedKeyDifference);
                if(debug) System.out.println("");
                
                for (int k = 0; k < expandedKeyDifference.length()*2; k++) {
                    int word_ = k%AMOUNT_WORDS;
                    int key = k/AMOUNT_WORDS;
                    //Se o word atual ainda não tiver sido ativado e ele seja ativo, ative-o
                    if(activeWordsExpandedKeyAux.get(key).getWord(cipher1.getWORD_SIZE(),word_) == 0 && expandedKeyDifference.getWord(cipher1.getWORD_SIZE(),k)!=0)
                        activeWordsExpandedKeyAux.get(key).setWord(cipher1.getWORD_SIZE(),word_,0xA);
                }

                k30k31 = new ByteArray(AMOUNT_WORDS);
                k30k31_ = new ByteArray(AMOUNT_WORDS);
                k30k31.copyBytes(expandedKeyk0k1, 16*comparableRound, 0, AMOUNT_WORDS);
                k30k31_.copyBytes(expandedKeyk0k1_, 16*comparableRound, 0, AMOUNT_WORDS);
                int numActWords = k30k31.getDifference(k30k31_).countNumActiveWords();
                if(numActWords >= numMaxActWords){
                    numMaxActWords = numActWords;
                    //System.out.println("#Word = "+i+", j = "+j);
                }
            }
            if(numMaxActWords < leastNumActive){
                leastNumActive = numMaxActWords;
                leastActive = k0k1.getDifference(k0k1_);
                leastActiveExpandedKey = expandedKeyk0k1.getDifference(expandedKeyk0k1_);
                activeWordsExpandedKey = activeWordsExpandedKeyAux;
            }
            
        }
        
        if(debug){
            System.out.println("least active difference =\n"+leastActive);
            System.out.println("");
            System.out.println("Minimum #active words in K"+(roundDelta+1)+"||K"+(roundDelta+2)+"\\oplus K`"+(roundDelta+1)+"||K`"+(roundDelta+2)+"=\n"+leastNumActive);
            System.out.println("");
        }
        
        ArrayList<Integer> activeWords = new ArrayList<>();
        for (int i = 0; i < leastActive.length()*2; i++) {
            if(leastActive.getWord(cipher1.getWORD_SIZE(),i) != 0) activeWords.add(i);
        }
        this.amountAffectedWords = leastNumActive;
        this.lowestDifferential = new Difference(leastActive);
        this.fullLowestDifferential = leastActiveExpandedKey;
        this.posBaseKeyAffectedWords = activeWords;
        this.activeWordsDeltaKey = activeWordsExpandedKey;
        
        //Object[] result = {leastNumActive,new Difference(leastActive),leastActiveExpandedKey,activeWords,activeWordsExpandedKey};
        if(debug){
            System.out.println((int)amountAffectedWords);
            System.out.println(lowestDifferential);
            System.out.println((ByteArray)fullLowestDifferential);
            for (int i = 0; i < ((ArrayList<Integer>)posBaseKeyAffectedWords).size(); i++) 
                System.out.println(((ArrayList<Integer>)posBaseKeyAffectedWords).get(i));
            System.out.println("Active Words in the Expanded Key");
            for (int i = 0; i < ((ArrayList<ByteArray>)activeWordsDeltaKey).size(); i++) {
                System.out.println(((ArrayList<ByteArray>)activeWordsDeltaKey).get(i));
            }
        }
    }*/
}
