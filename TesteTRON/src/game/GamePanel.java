package game;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

/**
 * Painel Principal do Jogo (Gameplay).
 * Responsável por toda a lógica de execução, renderização, física e IA.
 * Gerencia os jogadores, inimigos, rastros, colisões e progressão de níveis.
 */
public class GamePanel extends JPanel implements ActionListener, KeyListener {

    private final ScreenManager sm;
    private final javax.swing.Timer timer;

    /** Moto do Jogador 1 (sempre presente). */
    private PlayerBike player1;
    
    /** Moto do Jogador 2 (pode ser null no modo PVE). */
    private PlayerBike player2;
    
    /** Lista de motos inimigas (IA). */
    private final ArrayList<EnemyBike> enemies = new ArrayList<>();

    // Listas encadeadas para armazenar os pontos dos rastros (performance eficiente para add/remove)
    private final LinkedList<Point> trail1 = new LinkedList<>();
    private final LinkedList<Point> trail2 = new LinkedList<>();
    private final LinkedList<Point> enemyTrailAll = new LinkedList<>();

    /** Contador de ticks do jogo (usado para eventos periódicos). */
    private int ticks = 0;
    
    /** Nível de dificuldade atual (afeta agressividade da IA e número de inimigos). */
    private int difficulty = 1;
    
    /** Atraso inicial (countdown) antes de começar a mover. */
    private int startDelay = 75; // Aprox 3 segundos de contagem

    /** Ignora os últimos N pontos do rastro para evitar colisão imediata com a própria cauda. */
    private static final int SAFE_HEAD_COUNT = 2;

    /**
     * Construtor do painel de jogo.
     * Inicializa configurações e começa o loop do jogo.
     * @param sm Gerenciador de telas.
     */
    public GamePanel(ScreenManager sm) {
        this.sm = sm;

        setPreferredSize(new Dimension(Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT));
        setBackground(Constants.COLOR_BACKGROUND);
        setFocusable(true);
        addKeyListener(this);

        initState();

        timer = new javax.swing.Timer(Constants.GAME_SPEED_MS, this);
        timer.start();
    }

    /**
     * Inicializa ou reinicializa o estado do jogo.
     * Configura posições de spawn baseadas no modo de jogo selecionado.
     */
    private void initState() {
        trail1.clear();
        trail2.clear();
        enemyTrailAll.clear();
        enemies.clear();

        // Posições iniciais ajustadas
        if (ScreenManager.modoSelecionado == GameMode.PVP) {
            // PVP: Lados opostos, frente a frente
            player1 = new PlayerBike(100, 300, Constants.GRID_SIZE);
            player1.direcao = 1; // Direita
            
            player2 = new PlayerBike(Constants.WINDOW_WIDTH - 100, 300, Constants.GRID_SIZE);
            player2.direcao = 3; // Esquerda
        } else if (ScreenManager.modoSelecionado == GameMode.COOP) {
            // COOP: Lado a lado na esquerda
            player1 = new PlayerBike(100, 200, Constants.GRID_SIZE);
            player1.direcao = 1; // Direita
            
            player2 = new PlayerBike(100, 400, Constants.GRID_SIZE);
            player2.direcao = 1; // Direita
        } else {
            // Single Player (PVE)
            player1 = new PlayerBike(200, 300, Constants.GRID_SIZE);
            player1.direcao = 1; // Direita
            player2 = null;
        }

        // No modo PVE/COOP, adiciona o primeiro inimigo
        if (ScreenManager.modoSelecionado != GameMode.PVP) {
            EnemyBike e = new EnemyBike(Constants.WINDOW_WIDTH - 100, 300, Constants.GRID_SIZE);
            e.setAggression(1);
            enemies.add(e);
        }

        ticks = 0;
        difficulty = 1;
        startDelay = 75; // Reset countdown
    }

    /**
     * Loop principal do jogo (chamado a cada tick do Timer).
     * Atualiza lógica de movimento, IA, colisões e renderização.
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        // Countdown inicial
        if (startDelay > 0) {
            startDelay--;
            repaint();
            return;
        }

        ticks++;
        // Aumenta dificuldade periodicamente (a cada 15 segundos aprox) no modo sobrevivência
        if (ticks % (15000 / Constants.GAME_SPEED_MS) == 0) {
            difficulty = Math.min(5, difficulty + 1);
        }

        // 1) Adiciona rastro ANTES de mover (para cobrir o buraco deixado pelo movimento)
        addTrailPoint(trail1, player1);
        if (player2 != null) addTrailPoint(trail2, player2);

        for (EnemyBike en : enemies) {
            enemyTrailAll.add(new Point(en.x, en.y));
        }

        // 2) Limita rastros (se o player estiver morto, o rastro deve ser limpo na colisão, mas garantimos aqui também)
        if (!player1.vivo) trail1.clear();
        if (player2 != null && !player2.vivo) trail2.clear();

        limitTrail(trail1);
        limitTrail(trail2);
        // Limita rastro dos inimigos globalmente
        while (enemyTrailAll.size() > Constants.MAX_TRAIL_SIZE) enemyTrailAll.removeFirst();

        // 3) Sets para IA (otimização de busca O(1) ao invés de O(N))
        HashSet<Point> playerTrailSet = new HashSet<>(trail1);
        if (player2 != null) playerTrailSet.addAll(trail2);
        HashSet<Point> enemyTrailSet = new HashSet<>(enemyTrailAll);

        // 4) Move players (lógica de input)
        player1.mover(getWidth(), getHeight());
        if (player2 != null) player2.mover(getWidth(), getHeight());

        // 5) Move inimigos (Lógica de IA)
        Point target = new Point(player1.x, player1.y);
        int targetDir = player1.direcao;
        
        // IA escolhe o alvo mais próximo se houver dois players vivos
        if (player2 != null && player2.vivo) {
             if (!player1.vivo) {
                 target = new Point(player2.x, player2.y);
                 targetDir = player2.direcao;
             }
        }

        for (EnemyBike en : enemies) {
            en.setAggression(difficulty);
            // Se ambos players vivos, escolhe o mais perto
            if (player1.vivo && player2 != null && player2.vivo) {
                double d1 = Math.hypot(en.x - player1.x, en.y - player1.y);
                double d2 = Math.hypot(en.x - player2.x, en.y - player2.y);
                if (d2 < d1) {
                    target = new Point(player2.x, player2.y);
                    targetDir = player2.direcao;
                } else {
                    target = new Point(player1.x, player1.y);
                    targetDir = player1.direcao;
                }
            }

            en.moverIA(getWidth(), getHeight(), playerTrailSet, enemyTrailSet, target, targetDir);
        }

        // 6) Colisões e lógica de jogo
        handleCollisions();

        // Respawn de inimigos no modo PVE e COOP (avanço de nível)
        if ((ScreenManager.modoSelecionado == GameMode.PVE || ScreenManager.modoSelecionado == GameMode.COOP) 
            && enemies.isEmpty()) {
            
            // Avança para o próximo nível (Reset posicional e aumento de dificuldade)
            startNextLevel();
        }

        repaint();
    }

    /**
     * Adiciona a posição atual da moto ao rastro.
     */
    private void addTrailPoint(LinkedList<Point> trail, LightBike b) {
        if (b == null || !b.vivo) return;
        Point p = new Point(b.x, b.y);
        // Só adiciona se for diferente do último ponto (evita duplicatas paradas)
        if (trail.isEmpty() || !trail.getLast().equals(p)) trail.add(p);
    }

    /**
     * Remove pontos antigos do rastro se exceder o tamanho máximo.
     */
    private void limitTrail(LinkedList<Point> t) {
        while (t.size() > Constants.MAX_TRAIL_SIZE) t.removeFirst();
    }

    /**
     * Verifica todas as colisões possíveis (Motos vs Paredes já é feito no mover, aqui é Motos vs Rastros/Motos).
     */
    private void handleCollisions() {
        // Colisão Player com seu próprio rastro
        if (collidesWithTrail(player1, trail1)) { player1.vivo = false; trail1.clear(); }
        if (player2 != null && collidesWithTrail(player2, trail2)) { player2.vivo = false; trail2.clear(); }

        // Colisão Player com rastro do Outro Player (PVP/COOP)
        if (player2 != null) {
            // Verifica se P1 bateu no rastro de P2
            if (collidesWithTrail(player1, trail2)) { player1.vivo = false; trail1.clear(); }
            // Verifica se P2 bateu no rastro de P1
            if (collidesWithTrail(player2, trail1)) { player2.vivo = false; trail2.clear(); }
        }

        // Colisão Cabeça x Cabeça (Player x Player) - Empate
        if (player2 != null && player1.vivo && player2.vivo) {
            if (player1.x == player2.x && player1.y == player2.y) {
                player1.vivo = false; trail1.clear();
                player2.vivo = false; trail2.clear();
            }
        }

        // Colisão Inimigos (morrem se baterem em rastros)
        Iterator<EnemyBike> it = enemies.iterator();
        while (it.hasNext()) {
            EnemyBike en = it.next();
            Point ep = new Point(en.x, en.y);
            
            // Bateu no rastro de algum player?
            if (collidesWithTrail(en, trail1) || (player2 != null && collidesWithTrail(en, trail2))) {
                it.remove();
                continue;
            }
            
            // Bateu no próprio rastro ou de outros inimigos
            // Verifica colisão com rastro inimigo global
            int ignore = Math.min(SAFE_HEAD_COUNT, enemyTrailAll.size());
            boolean crashed = false;
            
            // Itera sobre o rastro inimigo ignorando os ultimos pontos (cabeça recente)
            for (int i = 0; i < enemyTrailAll.size() - ignore; i++) {
                if (enemyTrailAll.get(i).equals(ep)) {
                    crashed = true;
                    break;
                }
            }
            if (crashed) it.remove();
        }

        // Colisão Players com rastro de inimigos
        HashSet<Point> enemySet = new HashSet<>(enemyTrailAll);
        if (enemySet.contains(new Point(player1.x, player1.y))) { player1.vivo = false; trail1.clear(); }
        if (player2 != null && enemySet.contains(new Point(player2.x, player2.y))) { player2.vivo = false; trail2.clear(); }

        // Verifica se o jogo acabou
        checkGameOver();
    }

    /**
     * Verifica se uma moto colidiu com um rastro específico.
     * @param bike A moto a verificar.
     * @param trail O rastro (lista de pontos).
     * @return true se houver colisão.
     */
    private boolean collidesWithTrail(LightBike bike, LinkedList<Point> trail) {
        if (bike == null || !bike.vivo) return false;
        Point head = new Point(bike.x, bike.y);
        
        // Percorre o rastro verificando coincidência de coordenadas
        for (Point p : trail) {
            if (p.equals(head)) return true;
        }
        return false;
    }

    /**
     * Inicia o próximo nível (reset de posição e aumento de dificuldade).
     * Usado em modos PVE e COOP.
     */
    private void startNextLevel() {
        // Reseta jogadores para posição inicial
        if (ScreenManager.modoSelecionado == GameMode.COOP) {
            player1.x = 100; player1.y = 200; player1.direcao = 1; player1.vivo = true;
            if (player2 != null) {
                player2.x = 100; player2.y = 400; player2.direcao = 1; player2.vivo = true;
            }
        } else {
            // Single Player (PVE)
            player1.x = 200; player1.y = 300; player1.direcao = 1; player1.vivo = true;
        }
        
        // Limpa todos os rastros para o novo round
        trail1.clear();
        if (player2 != null) trail2.clear();
        enemyTrailAll.clear();
        enemies.clear();
        
        // Reinicia contagem regressiva
        startDelay = 75; 
        
        // Aumenta dificuldade
        difficulty = Math.min(10, difficulty + 1);
        
        // Cria novos inimigos (quantidade escala com dificuldade)
        int spawnX = Constants.WINDOW_WIDTH - 50;
        int count = (ScreenManager.modoSelecionado == GameMode.COOP) ? difficulty + 1 : difficulty;
        count = Math.min(count, 5); // Limite máximo de 5 inimigos simultâneos
        
        int stepY = Constants.WINDOW_HEIGHT / (count + 1);
        
        for (int i = 1; i <= count; i++) {
            EnemyBike e = new EnemyBike(spawnX, stepY * i, Constants.GRID_SIZE);
            e.setAggression(Math.min(5, difficulty)); 
            enemies.add(e);
        }
    }

    /**
     * Verifica condições de vitória ou derrota.
     */
    private void checkGameOver() {
        if (ScreenManager.modoSelecionado == GameMode.PVE && !player1.vivo) {
            endGame("VOCÊ PERDEU!");
        } else if (ScreenManager.modoSelecionado == GameMode.PVP &&
                (!player1.vivo || (player2 != null && !player2.vivo))) {
            if (!player1.vivo && !player2.vivo) endGame("EMPATE!");
            else if (!player1.vivo) endGame("JOGADOR 2 VENCEU!");
            else endGame("JOGADOR 1 VENCEU!");
        } else if (ScreenManager.modoSelecionado == GameMode.COOP &&
                !player1.vivo && (player2 == null || !player2.vivo)) {
            endGame("DERROTA!");
        }
    }

    /**
     * Finaliza o jogo e exibe a tela de Game Over.
     * @param msg Mensagem de fim de jogo.
     */
    private void endGame(String msg) {
        if (timer != null) timer.stop();
        if (sm != null && sm.gameOver != null) sm.gameOver.setMessage(msg);
        if (sm != null) sm.show("gameover");
    }

    /**
     * Renderização gráfica do jogo.
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();

        // Antialiasing para visual mais suave
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Desenha rastros com efeito Neon
        drawTrail(g2, trail1, Constants.COLOR_TRAIL1);
        if (player2 != null) drawTrail(g2, trail2, Constants.COLOR_TRAIL2);
        drawTrail(g2, enemyTrailAll, Constants.COLOR_ENEMY_BASE); 

        // Desenha Players
        if (player1.vivo) {
            drawBike(g2, player1.x, player1.y, Constants.COLOR_PLAYER1);
        }
        if (player2 != null && player2.vivo) {
            drawBike(g2, player2.x, player2.y, Constants.COLOR_PLAYER2);
        }

        // Desenha Inimigos
        for (EnemyBike en : enemies) {
            drawBike(g2, en.x, en.y, en.getColor());
        }

        // HUD e Contagem
        drawHUD(g2);
        if (startDelay > 0) {
            drawCountdown(g2);
        }

        g2.dispose();
    }

    /**
     * Desenha uma moto com efeito de brilho (glow).
     */
    private void drawBike(Graphics2D g2, int x, int y, Color c) {
        // Glow externo
        g2.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 100));
        g2.fillRect(x - 2, y - 2, Constants.GRID_SIZE + 4, Constants.GRID_SIZE + 4);
        // Corpo sólido
        g2.setColor(c);
        g2.fillRect(x, y, Constants.GRID_SIZE, Constants.GRID_SIZE);
        // Núcleo branco (brilho)
        g2.setColor(Color.WHITE);
        g2.fillRect(x + 3, y + 3, 4, 4);
    }

    /**
     * Desenha informações na tela (Nível, etc).
     */
    private void drawHUD(Graphics2D g2) {
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Verdana", Font.BOLD, 14));
        String diff = "NÍVEL: " + difficulty;
        g2.drawString(diff, 20, 30);
    }

    /**
     * Desenha a contagem regressiva inicial.
     */
    private void drawCountdown(Graphics2D g2) {
        g2.setColor(Color.CYAN);
        g2.setFont(new Font("Verdana", Font.BOLD, 80));
        int seconds = (int) Math.ceil(startDelay * Constants.GAME_SPEED_MS / 1000.0);
        String text = (seconds > 0) ? String.valueOf(seconds) : "GO!";
        FontMetrics fm = g2.getFontMetrics();
        int tw = fm.stringWidth(text);
        
        // Sombra do texto
        g2.setColor(Color.BLUE);
        g2.drawString(text, (getWidth() - tw) / 2 + 4, getHeight() / 2 + 4);
        
        // Texto principal
        g2.setColor(Color.WHITE);
        g2.drawString(text, (getWidth() - tw) / 2, getHeight() / 2);
    }

    /**
     * Desenha um rastro com efeito de fade e glow.
     */
    private void drawTrail(Graphics2D g2, java.util.List<Point> t, Color base) {
        if (t.isEmpty()) return;
        int size = t.size();
        
        // Passada 1: Glow (mais largo e transparente)
        for (int i = 0; i < size; i++) {
            Point p = t.get(i);
            float alpha = 0.3f * ((float) i / Math.max(1, size - 1)); 
            g2.setColor(new Color(base.getRed(), base.getGreen(), base.getBlue(), (int)(alpha * 255)));
            g2.fillRect(p.x - 2, p.y - 2, Constants.GRID_SIZE + 4, Constants.GRID_SIZE + 4);
        }
        
        // Passada 2: Núcleo sólido
        for (int i = 0; i < size; i++) {
            Point p = t.get(i);
            // Alpha cresce conforme se aproxima da cabeça (rastro fade-in)
            float alpha = 0.5f + 0.5f * ((float) i / Math.max(1, size - 1));
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            g2.setColor(base);
            g2.fillRect(p.x, p.y, Constants.GRID_SIZE, Constants.GRID_SIZE);
        }
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
    }

    // --- Controles ---

    @Override
    public void keyPressed(KeyEvent e) {
        int k = e.getKeyCode();

        // Controles Player 1 (Setas)
        if (k == KeyEvent.VK_UP) player1.setDirecaoIfNotReverse(0);
        if (k == KeyEvent.VK_RIGHT) player1.setDirecaoIfNotReverse(1);
        if (k == KeyEvent.VK_DOWN) player1.setDirecaoIfNotReverse(2);
        if (k == KeyEvent.VK_LEFT) player1.setDirecaoIfNotReverse(3);

        // Controles Player 2 (WASD)
        if (player2 != null) {
            if (k == KeyEvent.VK_W) player2.setDirecaoIfNotReverse(0);
            if (k == KeyEvent.VK_D) player2.setDirecaoIfNotReverse(1);
            if (k == KeyEvent.VK_S) player2.setDirecaoIfNotReverse(2);
            if (k == KeyEvent.VK_A) player2.setDirecaoIfNotReverse(3);
        }

        // Atalho de Reinício Rápido
        if (k == KeyEvent.VK_R) sm.startGameWithFade();
    }

    @Override public void keyReleased(KeyEvent e) {}
    @Override public void keyTyped(KeyEvent e) {}
}
