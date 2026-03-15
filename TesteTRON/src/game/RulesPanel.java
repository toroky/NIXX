package game;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Painel de Regras.
 * Exibe instruções sobre como jogar, controles e objetivos de cada modo.
 * Usa formatação HTML para texto rico e estilização visual consistente.
 */
public class RulesPanel extends JPanel {

    private final ScreenManager sm;

    /**
     * Construtor do Painel de Regras.
     * @param sm Gerenciador de telas.
     */
    public RulesPanel(final ScreenManager sm) {
        this.sm = sm;
        setBackground(Constants.COLOR_BACKGROUND);
        setLayout(new BorderLayout());

        // Painel Central Transparente para organizar o conteúdo verticalmente
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        content.setBorder(BorderFactory.createEmptyBorder(40, 60, 40, 60));

        // Título
        JLabel title = new JLabel("REGRAS DO JOGO", SwingConstants.CENTER);
        title.setForeground(Color.CYAN);
        title.setFont(new Font("Verdana", Font.BOLD, 48));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        content.add(title);

        content.add(Box.createVerticalStrut(30));

        // Texto explicativo usando HTML para formatação
        String rulesText = 
            "<html><body style='text-align: center; font-family: Verdana; color: white;'>" +
            "<h2>OBJETIVO</h2>" +
            "<p>Sobreviva e force seus oponentes a colidirem com os rastros de luz.</p>" +
            "<br>" +
            "<h2>CONTROLES</h2>" +
            "<p><b>JOGADOR 1 (AZUL):</b> Setas Direcionais (↑ ↓ ← →)</p>" +
            "<p><b>JOGADOR 2 (BRANCO):</b> Teclas W A S D</p>" +
            "<br>" +
            "<h2>MODOS DE JOGO</h2>" +
            "<p><b>PVE:</b> Sobreviva contra IAs que ficam mais inteligentes a cada nível.</p>" +
            "<p><b>PVP:</b> Duelo mortal entre dois jogadores.</p>" +
            "<p><b>COOP:</b> Trabalhem juntos para eliminar as ondas de inimigos.</p>" +
            "</body></html>";

        JLabel textLabel = new JLabel(rulesText);
        textLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        content.add(textLabel);

        content.add(Box.createVerticalGlue());

        // Botão de voltar
        JButton back = createNeonButton("VOLTAR AO MENU", Constants.COLOR_PLAYER1);
        back.setAlignmentX(Component.CENTER_ALIGNMENT);
        back.addActionListener(e -> sm.show("menu"));
        content.add(back);

        add(content, BorderLayout.CENTER);
    }

    /**
     * Cria botão neon reutilizando lógica visual (simplificada aqui).
     */
    private JButton createNeonButton(String text, Color baseColor) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isRollover()) {
                    g2.setColor(new Color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), 50));
                } else {
                    g2.setColor(new Color(30, 30, 40));
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.setColor(baseColor);
                g2.setStroke(new BasicStroke(2));
                g2.drawRoundRect(1, 1, getWidth()-2, getHeight()-2, 20, 20);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int tx = (getWidth() - fm.stringWidth(getText())) / 2;
                int ty = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.setColor(Color.WHITE);
                g2.drawString(getText(), tx, ty);
                g2.dispose();
            }
        };
        btn.setFont(new Font("Verdana", Font.BOLD, 16));
        btn.setPreferredSize(new Dimension(250, 50));
        btn.setMaximumSize(new Dimension(250, 50));
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    /**
     * Desenha fundo com gradiente e linhas de scanline (estilo retrô).
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        // Radial Gradient Background
        RadialGradientPaint p = new RadialGradientPaint(
            new Point(getWidth()/2, getHeight()/2), 
            getWidth(),
            new float[] { 0.0f, 1.0f },
            new Color[] { new Color(30, 30, 60), new Color(10, 10, 20) }
        );
        g2.setPaint(p);
        g2.fillRect(0, 0, getWidth(), getHeight());
        
        // Efeito Scanlines (linhas horizontais finas)
        g2.setColor(new Color(0, 255, 255, 5));
        for (int i = 0; i < getHeight(); i += 4) {
            g2.drawLine(0, i, getWidth(), i);
        }
    }
}
