import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;


public class Servidor50 {

    TCPServer50 mTcpServer;
    Scanner sc;
    private boolean shaEncontrado = false;

    private static List<String> palabras;
    private static int contador=0;
    private static int dificultad;
    public static void main(String[] args) {
        Servidor50 objser = new Servidor50();
        palabras = new ArrayList<>();
        palabras.add("banana");
        palabras.add("apple");
        palabras.add("orange");
        palabras.add("almond");
        palabras.add("avocado");
        palabras.add("blackberry");
        palabras.add("blueberry");
        palabras.add("cherry");
        palabras.add("chestnut");
        palabras.add("fig");
        palabras.add("lemon");
        palabras.add("melon");
        palabras.add("peanut");
        palabras.add("pineapple");
        palabras.add("raspberry");
        palabras.add("tangerine");
        palabras.add("watermelon");
        palabras.add("carrot");
        palabras.add("celery");
        palabras.add("spinach");
        palabras.add("tomato");
        palabras.add("wheat");
        palabras.add("corn");
        palabras.add("turnip");
        palabras.add("onion");
        palabras.add("eggplant ");
        palabras.add("rice");
        palabras.add("rye");
        palabras.add("spinach");
        palabras.add("squash");
        objser.iniciar();
    }

    void iniciar() {
        new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        mTcpServer = new TCPServer50(
                                new TCPServer50.OnMessageReceived() {
                                    @Override
                                    public void messageReceived(String message) {
                                        synchronized (this) {
                                            ServidorRecibe(message);
                                        }
                                    }
                                }
                        );
                        mTcpServer.run();
                    }
                }
        ).start();
        //-----------------
        String salir = "n";
        sc = new Scanner(System.in);
        System.out.println("Servidor bandera 01");
        while (!salir.equals("s")) {
            salir = sc.nextLine();
            ServidorEnvia(salir);
        }
        System.out.println("Servidor bandera 02");

    }

    int contarcliente = 0;
    double[] rptacli = new double[20];
    double sumclient = 0;

    void ServidorRecibe(String llego) {
        System.out.println(llego);

        boolean shaEncontrado = this.shaEncontrado;
        if (llego != null && !llego.equals("")) {
            if (llego.trim().contains("rpta")) {
                String[] arrString = llego.split("\\s+");

                if (!shaEncontrado) {

                     for( int i =1; i<=2;i++) {
                         mTcpServer.pararClientes(i);

                     }

                }
                System.out.println("Minero: "+arrString[3]);
                System.out.println("Tiempo: "+arrString[4]);
                System.out.println("Sha: " + arrString[5] );
                System.out.println("Key: " + arrString[6]);
                System.out.println("Palabra: "+ palabras.get(contador));
                System.out.println("Verificado: "+ arrString[8]);
                contador++;

                if(contador<palabras.size()) mTcpServer.sendMessageTCPServerRango(palabras.get(contador),dificultad , false,true);

                if(contador==palabras.size()){
                    for( int i =1; i<=2;i++) {
                        mTcpServer.pararClientes(i);

                    }
                }

            }
    }

}

    void ServidorEnvia(String envia) {//El servidor tiene texto de envio
        if (envia != null) {

            System.out.println("Soy Server y envio" + envia);
            if (envia.trim().contains("envio")) {   // [envio, palabra, 2]
                System.out.println("SI TIENE ENVIO!!!");

                String[] arrayString = envia.split("\\s+");
                dificultad = Integer.parseInt(arrayString[1]);
                boolean shaEncontrado = this.shaEncontrado;
                if (mTcpServer != null) {
                    mTcpServer.sendMessageTCPServerRango(palabras.get(contador), dificultad, shaEncontrado);
                }

            } else {
                System.out.println("NO TIENE ENVIO!!!");
            }
        }
    }
}
