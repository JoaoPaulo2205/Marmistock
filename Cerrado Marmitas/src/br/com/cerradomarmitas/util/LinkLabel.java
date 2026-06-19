package br.com.cerradomarmitas.util;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URI;

public class LinkLabel extends JLabel {
    private String url;

    public LinkLabel() {
    }

    public LinkLabel(String text, String url) {
        super("<html><a href='" + url + "'>" + text + "</a></html>");
        this.url = url;

        setForeground(Color.BLUE);
        setCursor(new Cursor(Cursor.HAND_CURSOR));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    System.out.println("Clicou no link");
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }
}

