import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import util.SortingAlgorithms;

public class Worker1 {
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(5002); // Puerto del segundo worker
        System.out.println("Worker1 listo en el puerto 5002.");

        while (true) {
            try (Socket socket = serverSocket.accept();
                 ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
                 ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream())) {

                System.out.println("Recibiendo datos del Worker anterior...");

                // Se reciben los datos de vector, algoritmo, y tiempo límite
                int[] vector = (int[]) in.readObject();
                int algorithm = in.readInt();
                int timeLimit = in.readInt();

                // se crea un executor para poder ejecutar el algoritmo de manera asincrónica
                ExecutorService executor = Executors.newSingleThreadExecutor();
                Future<Void> future = executor.submit(() -> {
                    sort(vector, algorithm);
                    return null;
                });

                // Control del tiempo de ejecución
                long startTime = System.currentTimeMillis();

                try {
                    // Se espera hasta el límite de tiempo
                    future.get(timeLimit, TimeUnit.SECONDS);
                } catch (TimeoutException e) {
                    System.out.println("No se completó en el tiempo límite. Enviando al siguiente worker...");
                    sendToNextWorker(vector, algorithm, timeLimit);
                    continue; // Si se supera el tiempo, se pasa el trabajo al siguiente worker
                } catch (InterruptedException e) {
                    System.out.println("La ejecución fue interrumpida.");
                    e.printStackTrace();
                    return; 
                } catch (ExecutionException e) {
                    System.out.println("Ocurrió un error durante la ejecución.");
                    e.printStackTrace();
                    return; 
                }

                long endTime = System.currentTimeMillis();
                long totalTime = endTime - startTime;

                // Se devuelve el vector ordenado y el tiempo total al cliente
                out.writeObject(vector);
                out.writeLong(totalTime);
                out.flush();
            } catch (ClassNotFoundException | IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void sort(int[] vector, int algorithm) {
        // Ordenar el vector de acuerdo con el algoritmo seleccionado por el usuario
        switch (algorithm) {
            case 1:
                SortingAlgorithms.mergeSort(vector); 
                break;
            case 2:
                SortingAlgorithms.heapSort(vector);
                break;
            case 3:
                SortingAlgorithms.quickSort(vector, 0, vector.length - 1); 
                break;
            default:
                throw new IllegalArgumentException("Algoritmo inválido.");
        }
    }

    private static void sendToNextWorker(int[] vector, int algorithm, int timeLimit) {
        try (Socket socket = new Socket("192.168.101.78", 5003); // Dirección y puerto del siguiente worker
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream())) {

            System.out.println("Enviando datos al siguiente Worker...");
            out.writeObject(vector);  // Enviar el vector sin ordenar
            out.writeInt(algorithm);  // Enviar el algoritmo a utilizar
            out.writeInt(timeLimit);  // Enviar el límite de tiempo
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
