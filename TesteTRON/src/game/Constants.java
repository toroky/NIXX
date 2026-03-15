package game;

import java.awt.Color;

/**
 * Classe responsável por armazenar todas as constantes e configurações globais do jogo.
 * Centraliza valores como dimensões da tela, velocidade do jogo, cores e mecânicas de rastro.
 * Isso facilita o ajuste de parâmetros sem precisar alterar múltiplas classes.
 */
public class Constants {
    /** Largura da janela do jogo em pixels. */
    public static final int WINDOW_WIDTH = 800;
    
    /** Altura da janela do jogo em pixels. */
    public static final int WINDOW_HEIGHT = 600;
    
    /** Tamanho de cada célula da grade (grid) e tamanho das motos. */
    public static final int GRID_SIZE = 10;
    
    /** 
     * Velocidade do loop do jogo em milissegundos. 
     * Define o intervalo de tempo entre cada atualização de frame.
     * Quanto menor o valor, mais rápido o jogo roda.
     */
    public static final int GAME_SPEED_MS = 40; 
    
    /**
     * Tamanho máximo do rastro das motos.
     * Determina quantos segmentos do rastro permanecem visíveis antes de sumirem.
     * Cálculo: 50 pontos * 40ms = 2.0 segundos de rastro visível.
     */
    public static final int MAX_TRAIL_SIZE = 50; 

    // --- Cores e Estilo Visual (Tema Neon) ---

    /** Cor de fundo do jogo (Azul escuro profundo). */
    public static final Color COLOR_BACKGROUND = new Color(10, 10, 20); 
    
    /** Cor da moto do Jogador 1 (Azul Neon). */
    public static final Color COLOR_PLAYER1 = new Color(0, 100, 255); 
    /** Cor do rastro do Jogador 1. */
    public static final Color COLOR_TRAIL1 = new Color(0, 100, 255);

    /** Cor da moto do Jogador 2 (Branco Brilhante). */
    public static final Color COLOR_PLAYER2 = Color.WHITE; 
    /** Cor do rastro do Jogador 2 (Cinza claro). */
    public static final Color COLOR_TRAIL2 = new Color(200, 200, 200);

    /** 
     * Cor base dos inimigos. 
     * A cor real é calculada dinamicamente na classe EnemyBike baseada na agressividade.
     */
    public static final Color COLOR_ENEMY_BASE = new Color(255, 69, 0); // Laranja avermelhado
}
