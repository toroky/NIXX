package game;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Painel do Menu Principal.
 * Exibe o título do jogo (NIXX), opções de modo de jogo (PVE, PVP, COOP) e regras.
 * Implementa efeitos visuais como botões neon e transição de fade-out.
 */
public class MenuPanel extends JPanel {

    /** Controle de transparência para o efeito de fade-out (1.0 = visível, 0.0 = transparente). */
    private float alpha = 1f;
    private final ScreenManager sm;

    /**
     * Construtor do Menu.
     * @param sm Gerenciador de telas para navegação.
     */
    public MenuPanel(final ScreenManager sm) {
        this.sm = sm;
        setBackground(Constants.COLOR_BACKGROUND);
        setLayout(new GridBagLayout()); // Centraliza os componentes verticalmente e horizontalmente

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER; // Cada componente ocupa uma linha inteira
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 20, 10); // Margens

        // Título "NIXX"
        JLabel title = new JLabel("NIXX", SwingConstants.CENTER);
        title.setForeground(Color.CYAN);
        title.setFont(new Font("Verdana", Font.BOLD, 80));
        add(title, gbc);
        
        // Subtítulo Neon Edition
        JLabel subtitle = new JLabel("NEON EDITION", SwingConstants.CENTER);
        subtitle.setForeground(new Color(200, 0, 255)); // Roxo Neon
        subtitle.setFont(new Font("Verdana", Font.PLAIN, 20));
        gbc.insets = new Insets(0, 10, 50, 10); // Espaço maior depois do subtítulo
        add(subtitle, gbc);

        gbc.insets = new Insets(10, 10, 10, 10); // Margem padrão para botões

        // Botões Neon para seleção de modo
        add(createNeonButton("1 JOGADOR (PVE)", Constants.COLOR_PLAYER1, e -> {
            ScreenManager.modoSelecionado = GameMode.PVE;
            sm.startGameWithFade(); // Inicia com efeito visual
        }), gbc);

        add(createNeonButton("2 JOGADORES (PVP)", Constants.COLOR_ENEMY_BASE, e -> {
            ScreenManager.modoSelecionado = GameMode.PVP;
            sm.startGameWithFade();
        }), gbc);

        add(createNeonButton("COOP (2 JOGADORES)", Constants.COLOR_PLAYER2, e -> {
            ScreenManager.modoSelecionado = GameMode.COOP;
            sm.startGameWithFade();
        }), gbc);

        add(createNeonButton("REGRAS", Color.LIGHT_GRAY, e -> sm.show("rules")), gbc);
        
        add(createNeonButton("SAIR", Color.GRAY, e -> System.exit(0)), gbc);
    }

    /**
     * Cria um botão estilizado com efeito neon.
     * @param text Texto do botão.
     * @param baseColor Cor principal do neon.
     * @param action Ação ao clicar.
     * @return JButton customizado.
     */
    private JButton createNeonButton(String text, Color baseColor, ActionListener action) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Fundo semi-transparente ao passar o mouse
                if (getModel().isRollover()) {
                    g2.setColor(new Color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), 50));
                } else {
                    g2.setColor(new Color(30, 30, 40));
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);

                // Borda Neon Brilhante
                g2.setColor(baseColor);
                g2.setStroke(new BasicStroke(2));
                g2.drawRoundRect(1, 1, getWidth()-2, getHeight()-2, 20, 20);

                // Texto Centralizado
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int tx = (getWidth() - fm.stringWidth(getText())) / 2;
                int ty = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.setColor(Color.WHITE);
                g2.drawString(getText(), tx, ty);

                g2.dispose();
            }
        };
        // Configurações padrão do botão swing
        btn.setFont(new Font("Verdana", Font.BOLD, 16));
        btn.setForeground(baseColor);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(300, 50));
        btn.addActionListener(action);
        return btn;
    }

    /**
     * Desenha o fundo do menu com gradiente radial.
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        
        // Fundo base
        g2.setColor(new Color(20, 20, 30));
        g2.fillRect(0, 0, getWidth(), getHeight());
        
        // Efeito de iluminação radial (foco no centro)
        RadialGradientPaint p = new RadialGradientPaint(
            new Point(getWidth()/2, getHeight()/2), 
            getWidth(),
            new float[] { 0.0f, 1.0f },
            new Color[] { new Color(30, 30, 60), new Color(10, 10, 20) }
        );
        g2.setPaint(p);
        g2.fillRect(0, 0, getWidth(), getHeight());
    }

    /**
     * Inicia a animação de fade-out (escurecimento) e depois troca para a tela do jogo.
     * @param sm Gerenciador de telas.
     */
    public void startFadeAndStart(ScreenManager sm) {
        javax.swing.Timer t = new javax.swing.Timer(30, null);
        t.addActionListener(e -> {
            alpha -= 0.05f; // Reduz opacidade
            if (alpha <= 0) {
                alpha = 0;
                t.stop();
                sm.startGameNow(); // Inicia o jogo
                alpha = 1f; // Reseta para próxima vez
            }
            repaint();
        });
        t.start();
    }

    /**
     * Sobrescreve paintChildren para desenhar a sobreposição preta do fade-out.
     */
    @Override
    protected void paintChildren(Graphics g) {
        super.paintChildren(g);
        if (alpha < 1f) {
            Graphics2D g2 = (Graphics2D) g.create();
            // Calcula opacidade inversa (quanto menor alpha, mais escuro o retângulo preto)
            int a = (int)((1f - alpha) * 255);
            a = Math.max(0, Math.min(255, a));
            g2.setColor(new Color(0, 0, 0, a));
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.dispose();
        }
    }
}
