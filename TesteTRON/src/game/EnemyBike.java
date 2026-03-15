package game;

import java.awt.*;
import java.util.*;

/**
 * Representa uma moto controlada pela Inteligência Artificial (IA).
 * Possui lógica para evitar obstáculos e perseguir o jogador com base em um nível de agressividade.
 */
public class EnemyBike extends LightBike {

    /** Nível de agressividade da IA (1 a 5). */
    private int aggression = 1; 
    
    /** Cor atual da moto (muda conforme agressividade). */
    private Color color;
    
    /** Gerador de números aleatórios para decisões da IA. */
    private Random rand = new Random();

    /**
     * Construtor da IA.
     * @param startX Posição inicial X.
     * @param startY Posição inicial Y.
     * @param gridSize Tamanho do passo.
     */
    public EnemyBike(int startX, int startY, int gridSize) {
        super(startX, startY, gridSize);
        this.direcao = 3; // Começa indo para a esquerda por padrão (oposto ao player)
        updateColor();
    }

    /**
     * Define o nível de agressividade da IA.
     * @param level Nível de 1 (Passivo) a 5 (Muito Agressivo).
     */
    public void setAggression(int level) {
        this.aggression = Math.max(1, Math.min(5, level));
        updateColor();
    }
    
    /**
     * Retorna a cor atual da moto inimiga.
     * @return Objeto Color.
     */
    public Color getColor() {
        return color;
    }

    /**
     * Atualiza a cor da moto baseado na agressividade.
     * Transição visual de Laranja (Passivo) para Vermelho (Agressivo).
     */
    private void updateColor() {
        // R: 255, G: varia de 100 a 0 conforme agressividade aumenta
        int green = Math.max(0, 100 - (aggression - 1) * 25);
        this.color = new Color(255, green, 0);
    }

    /**
     * Movimenta a moto (método básico de atualização de posição).
     * @param width Largura da tela.
     * @param height Altura da tela.
     */
    public void mover(int width, int height) {
        if (!vivo) return;

        switch (direcao) {
            case 0: y -= step; break;
            case 1: x += step; break;
            case 2: y += step; break;
            case 3: x -= step; break;
        }

        if (x < 0 || y < 0 || x > width - Constants.GRID_SIZE || y > height - Constants.GRID_SIZE) {
            vivo = false;
        }
    }

    /**
     * Lógica principal da Inteligência Artificial.
     * Decide a próxima direção baseada em obstáculos e alvo.
     * 
     * @param width Largura da tela.
     * @param height Altura da tela.
     * @param playerTrails Conjunto de pontos ocupados por rastros de jogadores.
     * @param enemyTrails Conjunto de pontos ocupados por rastros de inimigos.
     * @param target Posição do alvo (jogador) para perseguir.
     * @param targetDir Direção do alvo (pode ser usada para predição - não usada atualmente).
     */
    public void moverIA(int width, int height, HashSet<Point> playerTrails, HashSet<Point> enemyTrails, Point target, int targetDir) {
        // Lista de movimentos possíveis: 0=Cima, 1=Direita, 2=Baixo, 3=Esquerda
        ArrayList<Integer> validMoves = new ArrayList<>();
        
        // Passo 1: Verifica quais direções são seguras (não matam a moto imediatamente)
        for (int d = 0; d < 4; d++) {
            // Não pode ir na direção oposta (regra da moto)
            if ((this.direcao == 0 && d == 2) || (this.direcao == 2 && d == 0) ||
                (this.direcao == 1 && d == 3) || (this.direcao == 3 && d == 1)) {
                continue;
            }

            Point next = getNextPosition(d);
            
            // Checa limites da tela
            if (next.x < 0 || next.x >= width || next.y < 0 || next.y >= height) continue;
            
            // Checa colisão com rastros (obstáculos)
            if (playerTrails.contains(next) || enemyTrails.contains(next)) continue;
            
            validMoves.add(d);
        }

        // Se não houver saída, mantém direção e aceita o destino (Game Over)
        if (validMoves.isEmpty()) {
            mover(width, height); 
            return;
        }

        // Passo 2: Decide comportamento baseado na agressividade
        int chosenDir;
        
        // Cálculo de probabilidade de comportamento agressivo
        // Nível 1: 0% chance (100% Passivo)
        // Nível 5: 80% chance (Muito Agressivo/Perseguidor)
        boolean aggressiveMode = rand.nextInt(100) < ((aggression - 1) * 20); 

        if (aggressiveMode) {
            // Modo Agressivo: Escolhe o movimento que mais aproxima do alvo (jogador)
            chosenDir = getBestMoveToTarget(validMoves, target);
        } else {
            // Modo Passivo: Prioriza sobrevivência e movimentos aleatórios seguros
            if (validMoves.contains(this.direcao)) {
                // Se a direção atual é segura, tem 90% de chance de manter (evita zigue-zague frenético)
                if (rand.nextInt(100) < 90) {
                    chosenDir = this.direcao;
                } else {
                    chosenDir = validMoves.get(rand.nextInt(validMoves.size()));
                }
            } else {
                // Se a direção atual vai bater, é obrigado a virar para qualquer lado seguro
                chosenDir = validMoves.get(rand.nextInt(validMoves.size()));
            }
        }

        setDirecaoIfNotReverse(chosenDir);
        mover(width, height);
    }

    /**
     * Calcula a coordenada futura dado uma direção.
     * @param dir Direção desejada.
     * @return Ponto representando a próxima posição.
     */
    private Point getNextPosition(int dir) {
        int nx = x;
        int ny = y;
        switch(dir) {
            case 0: ny -= step; break;
            case 1: nx += step; break;
            case 2: ny += step; break;
            case 3: nx -= step; break;
        }
        return new Point(nx, ny);
    }

    /**
     * Algoritmo guloso (greedy) para encontrar a direção que minimiza a distância ao alvo.
     * @param moves Lista de movimentos válidos.
     * @param target Posição do alvo.
     * @return Melhor direção.
     */
    private int getBestMoveToTarget(ArrayList<Integer> moves, Point target) {
        int bestDir = moves.get(0);
        double minDist = Double.MAX_VALUE;

        for (int dir : moves) {
            Point p = getNextPosition(dir);
            double dist = Math.hypot(p.x - target.x, p.y - target.y);
            if (dist < minDist) {
                minDist = dist;
                bestDir = dir;
            }
        }
        return bestDir;
    }
}
