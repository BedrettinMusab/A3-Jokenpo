import java.io.*;
import java.net.*;
import java.util.*;

public class Servidor {
    private static Map<String, ManipuladorDeJogo> salasDeJogo = new HashMap<>();
    private static Random aleatorio = new Random();

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Digite o número da porta: ");
        int porta = scanner.nextInt();

        try {
            ServerSocket servidorSocket = new ServerSocket(porta);
            String ipServidor = InetAddress.getLocalHost().getHostAddress();
            System.out.println("Servidor Jokenpo está rodando...");
            System.out.println("IP do servidor: " + ipServidor);
            System.out.println("Escutando na porta: " + porta);

            while (true) {
                Socket jogadorSocket = servidorSocket.accept();
                new ManipuladorDeJogador(jogadorSocket).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static class ManipuladorDeJogador extends Thread {
        private Socket jogadorSocket;
        private PrintWriter saida;
        private BufferedReader entrada;

        public ManipuladorDeJogador(Socket jogadorSocket) {
            this.jogadorSocket = jogadorSocket;
        }

        public void run() {
            try {
                saida = new PrintWriter(jogadorSocket.getOutputStream(), true);
                entrada = new BufferedReader(new InputStreamReader(jogadorSocket.getInputStream()));

                saida.println("Bem-vindo! Digite '1' para criar uma sala, '2' para entrar em uma sala, ou '3' para jogar contra a CPU:");

                String escolha = entrada.readLine();
                if (escolha.equals("1")) {
                    criarSala();
                } else if (escolha.equals("2")) {
                    entrarSala();
                } else if (escolha.equals("3")) {
                    jogarContraCpu();
                } else {
                    saida.println("Escolha inválida. Desconectando...");
                    jogadorSocket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void criarSala() throws IOException {
            String codigoSala = gerarCodigoSala();
            saida.println("Sala criada. Seu código da sala é: " + codigoSala);

            ManipuladorDeJogo manipuladorDeJogo = new ManipuladorDeJogo(jogadorSocket, codigoSala);
            salasDeJogo.put(codigoSala, manipuladorDeJogo);
            manipuladorDeJogo.aguardarJogador2();
        }

        private void entrarSala() throws IOException {
            saida.println("Digite o código da sala:");
            String codigoSala = entrada.readLine();
            ManipuladorDeJogo manipuladorDeJogo = salasDeJogo.get(codigoSala);

            if (manipuladorDeJogo != null && manipuladorDeJogo.adicionarJogador2(jogadorSocket)) {
                salasDeJogo.remove(codigoSala);
                manipuladorDeJogo.start();
            } else {
                saida.println("Código da sala inválido ou sala cheia. Desconectando...");
                jogadorSocket.close();
            }
        }

        private void jogarContraCpu() throws IOException {
            saida.println("Você está jogando contra a CPU.");

            int vitoriasJogador = 0;
            int vitoriasCpu = 0;
            int empates = 0;
            for (int i = 0; i < 5; i++) {
                saida.println("Digite sua escolha (pedra, papel ou tesoura):");
                String escolhaJogador = entrada.readLine();
                if (escolhaJogador == null) break;

                String escolhaCpu = obterEscolhaCpu();
                String resultado = determinarVencedor(escolhaJogador, escolhaCpu);

                saida.println("CPU escolheu: " + escolhaCpu);
                saida.println(resultado);

                if (resultado.equals("Empate!")) {
                    empates++;
                } else if (resultado.equals("Você venceu!")) {
                    vitoriasJogador++;
                } else {
                    vitoriasCpu++;
                }

                if (i < 4) {
                    saida.println("Digite sua escolha (pedra, papel ou tesoura):");
                }
            }

            saida.println("Fim de jogo!");
            saida.println("Resultados: ");
            saida.println("Vitórias do jogador: " + vitoriasJogador);
            saida.println("Vitórias da CPU: " + vitoriasCpu);
            saida.println("Empates: " + empates);

            jogadorSocket.close();
        }

        private String obterEscolhaCpu() {
            int escolha = aleatorio.nextInt(3);
            switch (escolha) {
                case 0: return "pedra";
                case 1: return "papel";
                case 2: return "tesoura";
                default: return "pedra";
            }
        }

        private String determinarVencedor(String escolha1, String escolha2) {
            if (escolha1.equals(escolha2)) {
                return "Empate!";
            } else if ((escolha1.equals("pedra") && escolha2.equals("tesoura")) ||
                    (escolha1.equals("tesoura") && escolha2.equals("papel")) ||
                    (escolha1.equals("papel") && escolha2.equals("pedra"))) {
                return "Você venceu!";
            } else {
                return "CPU venceu!";
            }
        }

        private String gerarCodigoSala() {
            return String.format("%04d", aleatorio.nextInt(10000));
        }
    }

    static class ManipuladorDeJogo extends Thread {
        private Socket jogador1Socket;
        private Socket jogador2Socket;
        private PrintWriter jogador1Saida;
        private PrintWriter jogador2Saida;
        private BufferedReader jogador1Entrada;
        private BufferedReader jogador2Entrada;
        private String codigoSala;

        public ManipuladorDeJogo(Socket jogador1Socket, String codigoSala) {
            this.jogador1Socket = jogador1Socket;
            this.codigoSala = codigoSala;
        }

        public void aguardarJogador2() throws IOException {
            jogador1Saida = new PrintWriter(jogador1Socket.getOutputStream(), true);
            jogador1Entrada = new BufferedReader(new InputStreamReader(jogador1Socket.getInputStream()));
            jogador1Saida.println("Aguardando outro jogador para entrar...");
        }

        public boolean adicionarJogador2(Socket jogador2Socket) throws IOException {
            if (this.jogador2Socket == null) {
                this.jogador2Socket = jogador2Socket;
                jogador2Saida = new PrintWriter(jogador2Socket.getOutputStream(), true);
                jogador2Entrada = new BufferedReader(new InputStreamReader(jogador2Socket.getInputStream()));
                return true;
            }
            return false;
        }

        public void run() {
            try {
                jogador1Saida = new PrintWriter(jogador1Socket.getOutputStream(), true);
                jogador2Saida = new PrintWriter(jogador2Socket.getOutputStream(), true);
                jogador1Entrada = new BufferedReader(new InputStreamReader(jogador1Socket.getInputStream()));
                jogador2Entrada = new BufferedReader(new InputStreamReader(jogador2Socket.getInputStream()));

                jogador1Saida.println("Jogador 2 entrou. Você é o jogador 1.");
                jogador2Saida.println("Você entrou na sala. Você é o jogador 2.");

                int vitoriasJogador1 = 0;
                int vitoriasJogador2 = 0;
                int empates = 0;

                for (int i = 0; i < 5; i++) {
                    jogador1Saida.println("Digite sua escolha (pedra, papel ou tesoura):");
                    jogador2Saida.println("Digite sua escolha (pedra, papel ou tesoura):");

                    String escolha1 = jogador1Entrada.readLine();
                    String escolha2 = jogador2Entrada.readLine();
                    if (escolha1 == null || escolha2 == null) break;

                    String resultado = determinarVencedor(escolha1, escolha2);

                    jogador1Saida.println("Jogador 2 escolheu: " + escolha2);
                    jogador2Saida.println("Jogador 1 escolheu: " + escolha1);
                    jogador1Saida.println(resultado);
                    jogador2Saida.println(resultado);

                    if (resultado.equals("Empate!")) {
                        empates++;
                    } else if (resultado.equals("Jogador 1 venceu!")) {
                        vitoriasJogador1++;
                    } else {
                        vitoriasJogador2++;
                    }
                }

                jogador1Saida.println("Fim de jogo!");
                jogador1Saida.println("Resultados: ");
                jogador1Saida.println("Vitórias do Jogador 1: " + vitoriasJogador1);
                jogador1Saida.println("Vitórias do Jogador 2: " + vitoriasJogador2);
                jogador1Saida.println("Empates: " + empates);

                jogador2Saida.println("Fim de jogo!");
                jogador2Saida.println("Resultados: ");
                jogador2Saida.println("Vitórias do Jogador 1: " + vitoriasJogador1);
                jogador2Saida.println("Vitórias do Jogador 2: " + vitoriasJogador2);
                jogador2Saida.println("Empates: " + empates);

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    jogador1Socket.close();
                    jogador2Socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private String determinarVencedor(String escolha1, String escolha2) {
            if (escolha1.equals(escolha2)) {
                return "Empate!";
            } else if ((escolha1.equals("pedra") && escolha2.equals("tesoura")) ||
                    (escolha1.equals("tesoura") && escolha2.equals("papel")) ||
                    (escolha1.equals("papel") && escolha2.equals("pedra"))) {
                return "Jogador 1 venceu!";
            } else {
                return "Jogador 2 venceu!";
            }
        }
    }
}
