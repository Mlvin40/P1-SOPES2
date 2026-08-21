import java.util.*;

public class SimuladorPlanificacion {

    static class Proceso {
        String nombre;
        int at;
        int bt;
        int inicio;
        int ct;
        int tat;
        int wt;
        int rt;
        int orden;

        Proceso(String nombre, int at, int bt, int orden) {
            this.nombre = nombre;
            this.at = at;
            this.bt = bt;
            this.orden = orden;
        }
    }

    static class Segmento {
        String proceso;
        int inicio;
        int fin;

        Segmento(String proceso, int inicio, int fin) {
            this.proceso = proceso;
            this.inicio = inicio;
            this.fin = fin;
        }
    }

    static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {

        System.out.println("======================================");
        System.out.println(" SIMULADOR DE PLANIFICACION DE CPU");
        System.out.println("======================================");

        int cantidad = leerEnteroPositivo("Numero de procesos: ");

        List<Proceso> procesos = new ArrayList<>();

        for (int i = 0; i < cantidad; i++) {

            System.out.println("\nProceso " + (i + 1));

            System.out.print("Nombre: ");
            String nombre = scanner.nextLine().trim();

            while (nombre.isEmpty()) {
                System.out.println("Error: el nombre no puede estar vacio.");
                System.out.print("Nombre: ");
                nombre = scanner.nextLine().trim();
            }

            int at = leerEnteroNoNegativo(
                    "Tiempo de llegada (AT): "
            );

            int bt = leerEnteroPositivo(
                    "Tiempo de rafaga (BT): "
            );

            procesos.add(
                    new Proceso(nombre, at, bt, i)
            );
        }

        System.out.println("\nAlgoritmos disponibles:");
        System.out.println("1. FCFS");
        System.out.println("2. SJF no expropiativo");

        int opcion;

        while (true) {
            opcion = leerEntero("Seleccione el algoritmo: ");

            if (opcion == 1 || opcion == 2) {
                break;
            }

            System.out.println(
                    "Error: seleccione una opcion valida (1 o 2)."
            );
        }

        if (opcion == 1) {
            ejecutarFCFS(procesos);
        } else {
            ejecutarSJF(procesos);
        }
    }

    // --------------------------------------------------
    // FCFS
    // --------------------------------------------------

    static void ejecutarFCFS(List<Proceso> procesos) {

        List<Proceso> lista = copiarProcesos(procesos);

        lista.sort(
                Comparator.comparingInt((Proceso p) -> p.at)
                        .thenComparingInt(p -> p.orden)
        );

        List<Segmento> gantt = new ArrayList<>();

        int tiempo = 0;

        for (Proceso p : lista) {

            if (tiempo < p.at) {
                gantt.add(
                        new Segmento("IDLE", tiempo, p.at)
                );

                tiempo = p.at;
            }

            p.inicio = tiempo;
            p.rt = p.inicio - p.at;

            int fin = tiempo + p.bt;

            gantt.add(
                    new Segmento(p.nombre, tiempo, fin)
            );

            tiempo = fin;

            p.ct = tiempo;
            p.tat = p.ct - p.at;
            p.wt = p.tat - p.bt;
        }

        mostrarResultados(
                "FCFS",
                lista,
                gantt
        );
    }

    // --------------------------------------------------
    // SJF NO EXPROPIATIVO
    // --------------------------------------------------

    static void ejecutarSJF(List<Proceso> procesos) {

        List<Proceso> pendientes = copiarProcesos(procesos);
        List<Proceso> terminados = new ArrayList<>();
        List<Segmento> gantt = new ArrayList<>();

        int tiempo = 0;

        while (!pendientes.isEmpty()) {

            List<Proceso> disponibles = new ArrayList<>();

            for (Proceso p : pendientes) {
                if (p.at <= tiempo) {
                    disponibles.add(p);
                }
            }

            if (disponibles.isEmpty()) {

                int siguienteLlegada = pendientes.stream()
                        .mapToInt(p -> p.at)
                        .min()
                        .orElse(tiempo);

                if (tiempo < siguienteLlegada) {
                    gantt.add(
                            new Segmento(
                                    "IDLE",
                                    tiempo,
                                    siguienteLlegada
                            )
                    );
                }

                tiempo = siguienteLlegada;
                continue;
            }

            disponibles.sort(
                    Comparator.comparingInt((Proceso p) -> p.bt)
                            .thenComparingInt(p -> p.at)
                            .thenComparingInt(p -> p.orden)
            );

            Proceso p = disponibles.get(0);

            p.inicio = tiempo;
            p.rt = p.inicio - p.at;

            int fin = tiempo + p.bt;

            gantt.add(
                    new Segmento(p.nombre, tiempo, fin)
            );

            tiempo = fin;

            p.ct = tiempo;
            p.tat = p.ct - p.at;
            p.wt = p.tat - p.bt;

            terminados.add(p);
            pendientes.remove(p);
        }

        mostrarResultados(
                "SJF no expropiativo",
                terminados,
                gantt
        );
    }

    // --------------------------------------------------
    // RESULTADOS
    // --------------------------------------------------

    static void mostrarResultados(
            String algoritmo,
            List<Proceso> procesos,
            List<Segmento> gantt) {

        System.out.println("\n======================================");
        System.out.println("ALGORITMO: " + algoritmo);
        System.out.println("======================================");

        System.out.println("\nDiagrama de Gantt:");

        for (Segmento s : gantt) {
            System.out.print(
                    "| " + s.proceso +
                    " (" + s.inicio +
                    "-" + s.fin + ") "
            );
        }

        System.out.println("|");

        System.out.println("\nMetricas:");

        System.out.printf(
                "%-10s %-5s %-5s %-8s %-5s %-5s %-5s %-5s%n",
                "Proceso",
                "AT",
                "BT",
                "Inicio",
                "CT",
                "TAT",
                "WT",
                "RT"
        );

        double sumaWT = 0;
        double sumaTAT = 0;

        for (Proceso p : procesos) {

            System.out.printf(
                    "%-10s %-5d %-5d %-8d %-5d %-5d %-5d %-5d%n",
                    p.nombre,
                    p.at,
                    p.bt,
                    p.inicio,
                    p.ct,
                    p.tat,
                    p.wt,
                    p.rt
            );

            sumaWT += p.wt;
            sumaTAT += p.tat;
        }

        double promedioWT = sumaWT / procesos.size();
        double promedioTAT = sumaTAT / procesos.size();

        System.out.printf(
                "%nTiempo de espera promedio: %.2f%n",
                promedioWT
        );

        System.out.printf(
                "Tiempo de retorno promedio: %.2f%n",
                promedioTAT
        );
    }

    // --------------------------------------------------
    // VALIDACION DE DATOS
    // --------------------------------------------------

    static int leerEntero(String mensaje) {

        while (true) {

            System.out.print(mensaje);

            String entrada = scanner.nextLine().trim();

            try {
                return Integer.parseInt(entrada);
            } catch (NumberFormatException e) {
                System.out.println(
                        "Error: debe ingresar un numero entero valido."
                );
            }
        }
    }

    static int leerEnteroNoNegativo(String mensaje) {

        while (true) {

            int valor = leerEntero(mensaje);

            if (valor >= 0) {
                return valor;
            }

            System.out.println(
                    "Error: el valor no puede ser negativo."
            );
        }
    }

    static int leerEnteroPositivo(String mensaje) {

        while (true) {

            int valor = leerEntero(mensaje);

            if (valor > 0) {
                return valor;
            }

            System.out.println(
                    "Error: el valor debe ser mayor que cero."
            );
        }
    }

    static List<Proceso> copiarProcesos(
            List<Proceso> procesos) {

        List<Proceso> copia = new ArrayList<>();

        for (Proceso p : procesos) {
            copia.add(
                    new Proceso(
                            p.nombre,
                            p.at,
                            p.bt,
                            p.orden
                    )
            );
        }

        return copia;
    }
}
