package com.chess.gui;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import java.awt.Dimension   ;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Table {
    private final JFrame gameFrame;

    private static Dimension OUTER_FRAME_DIMENSION = new Dimension(600,600);

    public Table(){
        this.gameFrame = new JFrame("jChess");
        this.gameFrame.setSize(OUTER_FRAME_DIMENSION);
        this.gameFrame.setVisible(true);
        final JMenuBar tableMenuBar = new JMenuBar();
        populateMenuBar(tableMenuBar);
        this.gameFrame.setJMenuBar(tableMenuBar);
        
    }
        
        private void populateMenuBar(final JMenuBar tableMenuBar) {
            tableMenuBar.add(createFileMenu());
        }

        private JMenu createFileMenu() {
            final JMenu fileMenu = new JMenu("file");
            final JMenuItem openPGN = new JMenuItem("Load PGN File");
            openPGN.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e){
                    System.out.println("PGN file open here");
                }
            });
            fileMenu.add(openPGN);
            return fileMenu;
        }
}
