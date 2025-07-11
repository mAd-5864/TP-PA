package pt.isec.pa.chess.model;

import pt.isec.pa.chess.model.data.ChessGame;

import java.util.List;
import java.util.Scanner;

public class Commands {
    private ChessGame game;
    private final Scanner scanner = new Scanner(System.in);

    public Commands(ChessGame game) {
        this.game = game;
    }

    public void start() {
        game.printBoard();
        System.out.println("Digite um comando ou 'exit' para sair:");

        while (!game.isGameEnded()) {
            System.out.print("> ");
            String input = scanner.nextLine().trim();

            if (input.equalsIgnoreCase("exit")) {
                System.out.println("Terminando programa...");
                break;
            }

            processCommand(input);
        }
    }

    private void processCommand(String input) {
        String[] parts = input.split("\\s+");
        if (parts.length < 2) {
            printHelp();
            return;
        }

        switch (parts[0]) {
            case "move" -> handleMoveCommand(parts);
            case "find" -> handleFindCommand(parts);
            case "save" -> handleSaveCommand(parts);
            case "load" -> handleLoadCommand(parts);
            default -> printHelp();
        }
    }

    private void handleMoveCommand(String[] parts) {
        if (parts.length == 2) {
            String positionStr = parts[1];
            List<String> moves = game.getPossibleMoves(positionStr);

            if (moves == null || moves.isEmpty()) {
                System.out.println("Nenhum movimento possível ou posição inválida.");
            } else {
                System.out.println("Movimentos possíveis:");
                for (String move : moves) {
                    System.out.print(move + " ");
                }
                System.out.println();
            }
        } else if (parts.length == 3) {
            String from = parts[1];
            String to = parts[2];
            boolean moved = game.play(from, to);
            if (moved) {
                game.printBoard();
                System.out.println("Movimento feito com sucesso.");
            }
        } else {
            printHelp();
        }
    }


    private void handleFindCommand(String[] parts) {
        if (parts.length != 2) {
            printHelp();
            return;
        }

        if (game.getPieceById(parts[1]) != null) {
            System.out.println(game.getPieceById(parts[1]));
        } else {
            System.out.println("Peça não encontrada.");
        }
    }

    private void handleSaveCommand(String[] parts) {
        if (parts.length != 2) {
            printHelp();
            return;
        }

        try {
            game.saveToFile(parts[1]);
            System.out.println("Jogo salvo com sucesso em '" + parts[1] + "'");
        } catch (Exception e) {
            System.err.println("Erro ao salvar o jogo: " + e.getMessage());
        }
    }

    private void handleLoadCommand(String[] parts) {
        if (parts.length != 2) {
            printHelp();
            return;
        }

        try {
            game.loadFromFile(parts[1]);
            game.printBoard();
        } catch (Exception e) {
            System.err.println("Erro ao carregar o jogo: " + e.getMessage());
        }
    }


    private void printHelp() {
        System.out.println("Comandos suportados:");
        System.out.println("  move <posição>               - Mostra os movimentos possíveis");
        System.out.println("  move <origem> <destino>      - Move uma peça");
        System.out.println("  find <id_da_peça>            - Mostra a posição de uma peça");
        System.out.println("  save <ficheiro>              - Salva o estado do jogo num ficheiro");
        System.out.println("  load <ficheiro>              - Carrega o estado do jogo de um ficheiro");
        System.out.println("  exit                         - Termina o programa");
    }
}
