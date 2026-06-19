package br.com.cerradomarmitas.util;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

public final class ImagemPerfil {
    private ImagemPerfil() {
    }

    public static void aplicar(JLabel label, String caminho, int tamanho) {
        Dimension dimensao = new Dimension(tamanho, tamanho);
        label.setPreferredSize(dimensao);
        label.setMinimumSize(dimensao);
        label.setMaximumSize(dimensao);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setVerticalAlignment(SwingConstants.CENTER);
        label.setBorder(BorderFactory.createLineBorder(Color.GRAY));

        if (Validador.campoVazio(caminho) || !new File(caminho).isFile()) {
            label.setIcon(null);
            label.setText("?");
            return;
        }

        ImageIcon original = new ImageIcon(caminho);
        int largura = original.getIconWidth();
        int altura = original.getIconHeight();
        if (largura <= 0 || altura <= 0) {
            label.setIcon(null);
            label.setText("?");
            return;
        }

        BufferedImage origem = new BufferedImage(largura, altura, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = origem.createGraphics();
        graphics.drawImage(original.getImage(), 0, 0, null);
        graphics.dispose();

        int lado = Math.min(largura, altura);
        int x = (largura - lado) / 2;
        int y = (altura - lado) / 2;
        Image quadrada = origem.getSubimage(x, y, lado, lado)
                .getScaledInstance(tamanho, tamanho, Image.SCALE_SMOOTH);
        label.setText("");
        label.setIcon(new ImageIcon(quadrada));
    }
}
