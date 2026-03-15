package game;

/**
 * Classe abstrata que representa a estrutura básica de uma "Light Bike" (moto de luz).
 * Define propriedades comuns como posição, direção, velocidade e estado de vida.
 * Serve de base para PlayerBike (Jogador) e EnemyBike (IA).
 */
public abstract class LightBike {
    /** Posição X atual da moto na grade. */
    public int x;
    
    /** Posição Y atual da moto na grade. */
    public int y;
    
    /** Tamanho do passo de movimento (geralmente igual ao GRID_SIZE). */
    public int step;
    
    /** 
     * Direção atual do movimento.
     * 0 = Cima, 1 = Direita, 2 = Baixo, 3 = Esquerda.
     */
    public int direcao; 
    
    /** Estado da moto: true se está viva/ativa, false se colidiu. */
    public boolean vivo = true;

    /**
     * Construtor da LightBike.
     * @param x Posição inicial X.
     * @param y Posição inicial Y.
     * @param step Tamanho do passo de movimento.
     */
    public LightBike(int x, int y, int step) {
        this.x = x;
        this.y = y;
        this.step = step;
    }

    /**
     * Define a nova direção da moto, impedindo movimentos proibidos (meia-volta imediata).
     * No TRON, uma moto não pode virar 180 graus instantaneamente.
     * @param nova A nova direção desejada (0-3).
     */
    public void setDirecaoIfNotReverse(int nova) {
        // A diferença absoluta de 2 indica direções opostas (ex: 0 e 2, 1 e 3)
        if (Math.abs(nova - direcao) != 2) {
            direcao = nova;
        }
    }

    /**
     * Calcula a distância euclidiana até outra moto.
     * Útil para IA calcular proximidade do jogador.
     * @param other A outra moto.
     * @return Distância em pixels.
     */
    public double distTo(LightBike other) {
        if (other == null) return Double.MAX_VALUE;
        return Math.hypot(this.x - other.x, this.y - other.y);
    }
}
