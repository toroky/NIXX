package game;

import javax.swing.JPanel;
import java.awt.CardLayout;

/**
 * Gerenciador de telas do jogo.
 * Utiliza um CardLayout para alternar entre diferentes painéis (Menu, Jogo, Regras, Game Over).
 * Controla o fluxo de navegação entre as diferentes telas.
 */
public class ScreenManager extends JPanel {
    /** Layout que permite empilhar componentes e mostrar um de cada vez. */
    private final CardLayout layout;
    
    // Referências para os painéis do jogo
    public MenuPanel menu;
    public RulesPanel rules;
    public GamePanel game;
    public GameOverPanel gameOver;

    /** Armazena o modo de jogo selecionado atualmente (PVE, PVP, COOP). */
    public static GameMode modoSelecionado = GameMode.PVE;

    /**
     * Construtor do gerenciador de telas.
     * @param window Referência à janela principal (não usada diretamente aqui, mas útil se precisar).
     */
    public ScreenManager(GameWindow window) {
        layout = new CardLayout();
        setLayout(layout);

        // Inicializa os painéis fixos
        menu = new MenuPanel(this);
        rules = new RulesPanel(this);
        gameOver = new GameOverPanel(this);

        // Adiciona os painéis ao CardLayout com identificadores (strings)
        add(menu, "menu");
        add(rules, "rules");
        add(gameOver, "gameover");

        // Começa exibindo o menu
        show("menu");
    }

    /**
     * Inicia a transição visual (fade out) no menu antes de começar o jogo.
     * O MenuPanel chamará startGameNow() quando a animação terminar.
     */
    public void startGameWithFade() {
        menu.startFadeAndStart(this);
    }

    /**
     * Inicia efetivamente o jogo.
     * Cria uma nova instância de GamePanel para garantir um estado limpo.
     */
    public void startGameNow() {
        // Remove instância antiga do jogo se existir
        if (game != null) remove(game);
        
        // Cria nova instância do jogo
        game = new GamePanel(this);
        add(game, "game");
        
        // Mostra a tela do jogo
        show("game");

        // Garante que o painel do jogo receba o foco do teclado
        javax.swing.SwingUtilities.invokeLater(() -> {
            game.requestFocusInWindow();
            game.requestFocus();
        });
    }

    /**
     * Exibe o painel identificado pelo nome.
     * @param name Nome do painel ("menu", "rules", "game", "gameover").
     */
    public void show(String name) {
        layout.show(this, name);
    }
}
