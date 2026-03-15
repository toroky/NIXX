package game;

import javax.swing.SwingUtilities;

/**
 * Classe principal que inicia a aplicação.
 * Esta é a porta de entrada do programa (Entry Point).
 */
public class Main {
    /**
     * Método main: Onde a execução do programa Java começa.
     * @param args Argumentos de linha de comando (não utilizados neste jogo).
     */
    public static void main(String[] args) {
        // SwingUtilities.invokeLater garante que a interface gráfica (GUI)
        // seja criada e atualizada na Thread de Eventos do Swing (EDT).
        // Isso é uma boa prática para evitar problemas de concorrência e travamentos.
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                // Cria a janela principal do jogo
                new GameWindow();
            }
        });
    }
}
