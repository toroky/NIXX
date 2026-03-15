package game;

/**
 * Representa a moto controlada por um jogador humano.
 * Estende LightBike e implementa a lógica de movimento baseada em input.
 */
public class PlayerBike extends LightBike {

    /**
     * Construtor do Jogador.
     * @param x Posição inicial X.
     * @param y Posição inicial Y.
     * @param step Tamanho do passo.
     */
    public PlayerBike(int x, int y, int step) {
        super(x, y, step);
    }

    /**
     * Atualiza a posição da moto baseada na direção atual.
     * Também verifica se a moto saiu dos limites da tela (Game Over).
     * @param width Largura da área de jogo.
     * @param height Altura da área de jogo.
     */
    public void mover(int width, int height) {
        if (!vivo) return;

        // Atualiza coordenadas baseado na direção
        switch (direcao) {
            case 0: y -= step; break; // Cima
            case 1: x += step; break; // Direita
            case 2: y += step; break; // Baixo
            case 3: x -= step; break; // Esquerda
        }

        // Verifica colisão com as bordas da tela
        if (x < 0 || y < 0 || x > width - Constants.GRID_SIZE || y > height - Constants.GRID_SIZE) {
            vivo = false;
        }
    }
}
