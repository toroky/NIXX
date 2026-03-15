package game;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Painel de Fim de Jogo (Game Over).
 * Exibe a mensagem de vitória/derrota e botões para reiniciar ou voltar ao menu.
 * Mantém a estética Neon do restante do jogo.
 */
public class GameOverPanel extends JPanel {
    
    /** Label principal que exibe a mensagem (ex: "JOGADOR 1 VENCEU"). */
    private JLabel label;
    private ScreenManager sm;

    /**
     * Construtor do painel de Game Over.
     * @param sm Gerenciador de telas.
     */
    public GameOverPanel(final ScreenManager sm) {
        this.sm = sm;
        setBackground(Constants.COLOR_BACKGROUND);
        setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 30, 10);

        // Título "GAME OVER" (inicialmente, será alterado dinamicamente)
        label = new JLabel("FIM DE JOGO", SwingConstants.CENTER);
        label.setForeground(Color.RED);
        label.setFont(new Font("Verdana", Font.BOLD, 48));
        add(label, gbc);

        // Botões estilizados
        JButton restart = createNeonButton("REINICIAR", Color.CYAN);
        restart.addActionListener(e -> sm.startGameWithFade()); // Reinicia o jogo imediatamente
        add(restart, gbc);

        gbc.insets = new Insets(10, 10, 10, 10); // Menor espaçamento para o próximo
        JButton menu = createNeonButton("MENU", Color.WHITE);
        menu.addActionListener(e -> sm.show("menu")); // Volta para o menu principal
        add(menu, gbc);
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

                // Fundo semi-transparente
                if (getModel().isRollover()) {
                    g2.setColor(new Color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), 50));
                } else {
                    g2.setColor(new Color(30, 30, 40));
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);

                // Borda Neon
                g2.setColor(baseColor);
                g2.setStroke(new BasicStroke(2));
                g2.drawRoundRect(1, 1, getWidth()-2, getHeight()-2, 20, 20);

                // Texto
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int tx = (getWidth() - fm.stringWidth(getText())) / 2;
                int ty = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), tx, ty);

                g2.dispose();
            }
        };
        btn.setFont(new Font("Verdana", Font.BOLD, 20));
        btn.setForeground(baseColor);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(250, 60));
        return btn;
    }

    /**
     * Atualiza a mensagem de fim de jogo e ajusta a cor conforme o resultado.
     * @param msg Mensagem a ser exibida (ex: "JOGADOR 1 VENCEU").
     */
    public void setMessage(String msg) {
        label.setText(msg);
        // Ajusta cor dependendo da mensagem (Verde para vitória, Vermelho para derrota)
        if (msg.contains("VENCEU") || msg.contains("GANHOU")) {
            label.setForeground(Color.GREEN);
        } else if (msg.contains("PERDEU") || msg.contains("DERROTA")) {
            label.setForeground(Color.RED);
        } else {
            label.setForeground(Color.WHITE);
        }
    }
    
    /**
     * Desenha scanlines sutis no fundo.
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // Opcional: Efeito de scanline ou grid no fundo
        Graphics2D g2 = (Graphics2D) g;
        g2.setColor(new Color(255, 255, 255, 10));
        for (int i = 0; i < getHeight(); i += 4) {
            g2.drawLine(0, i, getWidth(), i);
        }
    }
}
