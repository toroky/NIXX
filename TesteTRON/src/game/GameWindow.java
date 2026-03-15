package game;

import javax.swing.JFrame;

/**
 * Janela principal da aplicação (JFrame).
 * Configura as propriedades básicas da janela como título, tamanho e comportamento de fechamento.
 * Atua como o contêiner principal para o gerenciador de telas (ScreenManager).
 */
public class GameWindow extends JFrame {

    /**
     * Construtor que inicializa a janela.
     */
    public GameWindow() {
        setTitle("NIXX - Neon Edition");
        setSize(900, 700);
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        ScreenManager sm = new ScreenManager(this);
        add(sm);

        setVisible(true);
    }
}
