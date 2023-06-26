import java.util.Scanner;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;
class Cliente50 {


    public double[] sum = new double[40];
    TCPClient50 mTcpClient;
    Scanner sc;

    public static void main(String[] args) {
        Cliente50 objcli = new Cliente50();
        objcli.iniciar();
    }

    void iniciar() {
        new Thread(
                () -> {
                    mTcpClient = new TCPClient50("127.0.0.1", message -> ClienteRecibe(message));
                    mTcpClient.run();
                }
        ).start();
        //---------------------------

        String salir = "n";
        sc = new Scanner(System.in);

        while (!salir.equals("s")) {
            salir = sc.nextLine();
            ClienteEnvia(salir);
        }


    }

    void ClienteRecibe(String llego) {

        if (llego.trim().contains("evalua")) {
            String[] arrayString = llego.split("\\s+");

            boolean shaEncontrado = Boolean.parseBoolean(arrayString[4]);
            String palabra = arrayString[1];
            if (!shaEncontrado) {
                int dificultad = Integer.parseInt(arrayString[2]);
                int nroMinero = Integer.parseInt(arrayString[3]);
                procesar(palabra, dificultad, nroMinero);
            }
            if (shaEncontrado) {
                String sha = arrayString[2];
                String nonce = arrayString[3];
                int nroMinero = 3;
                verifica(palabra, sha, nonce, nroMinero);
            }

        }
    }


    void ClienteEnvia(String envia) {

        if (mTcpClient != null && !envia.equals("s")) {

            mTcpClient.sendMessage(envia);
        }
    }

    void verifica(String palabra, String sha, String nonce, int nroMinero) {

        Miner miner = new Miner(palabra, nonce);
        String shaGenerado = miner.getHash();
        if (shaGenerado.equals(sha))
            ClienteEnvia("rpta -> Minero: " + nroMinero + " " + miner.getElapsedSeconds() + " " + miner.getResult() + " " + miner.getNonce() + " " + miner.getWord() + " " + true);
        else
            ClienteEnvia("rpta -> Minero: " + nroMinero + " " + miner.getElapsedSeconds() + " " + miner.getResult() + " " + miner.getNonce() + " " + miner.getWord() + " " + true);

    }
    void procesar(String palabra, int dificultad, int nroMinero) { // implementacion para 8 hilos
        Miner[] miners = new Miner[8];
        Thread[] threads = new Thread[8];

        for (int i = 0; i < 8; i++) {
            miners[i] = new Miner(palabra, dificultad);
            threads[i] = new Thread(miners[i]);
            threads[i].start();
        }

        try {
            for (int i = 0; i < 8; i++) {
                threads[i].join();
                ClienteEnvia("rpta -> Minero: " + nroMinero + " " + miners[i].getElapsedSeconds() + " " + miners[i].getResult() + " " + miners[i].getNonce() + " " + miners[i].getWord() + " " + true);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

class Miner implements Runnable {
    private String serverWord;
    private int difficulty;
    private String word;
    private String nonce;
    private String result;
    private long startTime;
    private long endTime;

    public Miner(String serverWord, int difficulty) {
        this.serverWord = serverWord;
        this.difficulty = difficulty;
    }

    public Miner(String serverWord, String nonce){
        this.serverWord = serverWord;
        this.nonce = nonce;
    }
    public void run() {
        startTime = System.currentTimeMillis();

        while (true) {
            word = serverWord;
            nonce = generateNonce();
            result = calculateHash(word, nonce);

            if (startsWithZeros(result, difficulty)) {
                endTime = System.currentTimeMillis();
                break;
            }
        }
    }
    private boolean startsWithZeros(String str, int numZeros) {
        for (int i = 0; i < numZeros; i++) {
            if (str.charAt(i) != '0') {
                return false;
            }
        }
        return true;
    }
    private String calculateHash(String word, String nonce) {
        String data = word + nonce;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(data.getBytes());

            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }

            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return null;
    }

    public String getHash(){
        return calculateHash(this.serverWord, this.nonce);
    }
    public String getWord() {
        return word;
    }

    public String getNonce() {
        return nonce;
    }

    public String getResult() {
        return result;
    }
    private String generateNonce() {
        String characters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 8; i++) {
            int index = random.nextInt(characters.length());
            sb.append(characters.charAt(index));
        }
        return sb.toString();
    }
    public double getElapsedSeconds() {
        return (endTime - startTime) / 1000.0;
    }
}


