import java.io.*; // Importación para el manejo de archivos y flujo de datos
import java.net.*; // Importación para manejo de sockets
import java.util.*; // Importación para manejo de listas, Scanner y otras utilidades
import util.VectorUtils; 
package lab;
public class Client {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Seleccione el algoritmo de ordenamiento:");
        System.out.println("1. Mergesort");
        System.out.println("2. Heapsort");
        System.out.println("3. Quicksort");
        int algorithm = scanner.nextInt();

        int[] vector = null;

        // Preguntar si el usuario desea leer el vector desde un archivo
        System.out.println("¿Desea leer el vector desde un archivo? (si/no):");
        scanner.nextLine(); 
        String respuestaArchivo = scanner.nextLine();

        if (respuestaArchivo.equalsIgnoreCase("si")) {
            System.out.println("Ingrese la ruta del archivo:");
            String filePath = scanner.nextLine();
            try {
                vector = readVectorFromFile(filePath);
                System.out.println("Vector leído desde el archivo.");
            } catch (IOException e) {
                System.err.println("Error al leer el archivo: " + e.getMessage());
                return;
            }
        } else {
            System.out.println("Ingrese el tamaño del vector:");
            int n = scanner.nextInt();
            vector = VectorUtils.generateRandomVector(n, 30);
            System.out.println("Vector generado aleatoriamente.");
        }

        // Mostrar los primeros 10 elementos del vector generado
        System.out.println("Primeros 10 elementos:");
        System.out.println(Arrays.toString(Arrays.copyOfRange(vector, 0, 10)));

        // Enviar el vector al worker con el limite de tiempo requerido
        System.out.println("Ingrese el tiempo límite (en segundos) por worker:");
        int timeLimit = scanner.nextInt();

        int[] sortedVector = null;
        long totalTime = 0;

        try (Socket socket = new Socket("192.168.101.78", 5001);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            System.out.println("Enviando datos al Worker 0...");
            out.writeObject(vector);
            out.writeInt(algorithm);
            out.writeInt(timeLimit);
            out.flush();

            // Recibir el resultado ordenado
            System.out.println("Esperando respuesta del Worker...");
            sortedVector = (int[]) in.readObject();
            totalTime = in.readLong();

        } catch (ClassNotFoundException | IOException e) {
            System.err.println("Error al comunicarse con el Worker: " + e.getMessage());
            return;
        }

        // Mostrar información del vector ordenado, incluyendo los primeros 10 elementos
        System.out.println("Vector ordenado recibido:");
        System.out.println("Tiempo total: " + totalTime / 1000.0 + " segundos");
        System.out.println("Primeros 10 elementos ordenados:");
        System.out.println(Arrays.toString(Arrays.copyOfRange(sortedVector, 0, 10)));

    }

    // Método para leer el vector desde un archivo
    private static int[] readVectorFromFile(String filePath) throws IOException {
        List<Integer> numbers = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("[,\\s]+");
                for (String part : parts) {
                    numbers.add(Integer.parseInt(part));
                }
            }
        }
        return numbers.stream().mapToInt(Integer::intValue).toArray();
    }

}
