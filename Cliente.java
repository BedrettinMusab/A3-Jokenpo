import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Cliente {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Digite o endereço IP do servidor: ");
        String enderecoServidor = scanner.nextLine();

        System.out.print("Digite a porta do servidor: ");
        int portaServidor = scanner.nextInt();
        scanner.nextLine();  // Consumir a nova linha sobrante

        try {
            Socket socket = new Socket(enderecoServidor, portaServidor);
            PrintWriter saida = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            System.out.println(entrada.readLine()); // Mensagem de boas-vindas
            String escolha = scanner.nextLine();
            saida.println(escolha);

            switch (escolha) {
                case "1":
                    tratarCriacaoDeSala(entrada, saida, scanner);
                    break;
                case "2":
                    tratarEntradaEmSala(entrada, saida, scanner);
                    break;
                case "3":
                    tratarJogoContraCpu(entrada, saida, scanner);
                    break;
                default:
                    System.out.println("Escolha inválida. Desconectando...");
                    socket.close();
                    return;
            }

            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void tratarCriacaoDeSala(BufferedReader entrada, PrintWriter saida, Scanner scanner) throws IOException {
        System.out.println(entrada.readLine()); // Mensagem de sala criada
        System.out.println(entrada.readLine()); // Código da sala

        // Aguardando jogador 2
        String mensagemServidor;
        while ((mensagemServidor = entrada.readLine()) != null) {
            System.out.println(mensagemServidor);
            if (mensagemServidor.equals("Jogador 2 entrou. Você é o jogador 1.")) {
                break;
            }
        }

        jogar(entrada, saida, scanner);
    }

    private static void tratarEntradaEmSala(BufferedReader entrada, PrintWriter saida, Scanner scanner) throws IOException {
        System.out.println(entrada.readLine()); // Digite o código da sala
        String codigoSala = scanner.nextLine();
        saida.println(codigoSala);

        // Aguardando confirmação ou erro
        String mensagemServidor;
        while ((mensagemServidor = entrada.readLine()) != null) {
            System.out.println(mensagemServidor);
            if (mensagemServidor.startsWith("Você entrou na sala.")) {
                break;
            } else if (mensagemServidor.startsWith("Código da sala inválido")) {
                return;
            }
        }

        jogar(entrada, saida, scanner);
    }

    private static void tratarJogoContraCpu(BufferedReader entrada, PrintWriter saida, Scanner scanner) throws IOException {
        System.out.println(entrada.readLine()); // Mensagem de início do jogo contra a CPU

        for (int i = 0; i < 5; i++) {
            System.out.println(entrada.readLine()); // Prompt para escolha do jogador
            String escolhaJogador = scanner.nextLine();
            saida.println(escolhaJogador);

            System.out.println(entrada.readLine()); // Escolha da CPU
            System.out.println(entrada.readLine()); // Resultado da rodada
        }

        String mensagemServidor;
        while ((mensagemServidor = entrada.readLine()) != null) {
            System.out.println(mensagemServidor);
            if (mensagemServidor.equals("Fim de jogo!")) {
                break;
            }
        }
        while ((mensagemServidor = entrada.readLine()) != null) {
            System.out.println(mensagemServidor);
        }
    }

    private static void jogar(BufferedReader entrada, PrintWriter saida, Scanner scanner) throws IOException {
        String mensagemServidor;
        while ((mensagemServidor = entrada.readLine()) != null) {
            System.out.println(mensagemServidor);
            if (mensagemServidor.startsWith("Fim de jogo!")) {
                break;
            }
            if (mensagemServidor.contains("Digite sua escolha")) {
                String escolhaJogador = scanner.nextLine();
                saida.println(escolhaJogador);
            }
        }

        // Imprimir resultados finais
        while ((mensagemServidor = entrada.readLine()) != null) {
            System.out.println(mensagemServidor);
        }
    }
}
