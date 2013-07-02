/*******************************************************************************
 * Selenium, Minecraft resource pack viewer and editor
 * 
 * Copyright (C) 2013  Jonas Konrad
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package at.yawk.selenium.ui;

import static at.yawk.selenium.Strings.t;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

import at.yawk.selenium.Selenium;

public abstract class Wizard extends JPanel {
    private static final long serialVersionUID = 1L;
    
    public static int OPTION_DONE = 1;
    public static int OPTION_ABORT = 2;
    public static int OPTION_NEXT = 4;
    
    private Window dispose;
    private JButton next;
    private JButton abort;
    private JButton done;
    
    public Wizard() {
        next = new JButton(new AbstractAction(t("Next >")) {
            private static final long serialVersionUID = 1L;
            
            @Override
            public void actionPerformed(ActionEvent arg0) {
                next();
            }
        });
        abort = new JButton(new AbstractAction(t("Abort")) {
            private static final long serialVersionUID = 1L;
            
            @Override
            public void actionPerformed(ActionEvent arg0) {
                abort();
            }
        });
        done = new JButton(new AbstractAction(t("Done")) {
            private static final long serialVersionUID = 1L;
            
            @Override
            public void actionPerformed(ActionEvent arg0) {
                done();
            }
        });
        
        JPanel buttons = new JPanel();
        buttons.setLayout(new BoxLayout(buttons, BoxLayout.X_AXIS));
        
        next.setAlignmentY(JPanel.RIGHT_ALIGNMENT);
        abort.setAlignmentY(JPanel.RIGHT_ALIGNMENT);
        done.setAlignmentY(JPanel.RIGHT_ALIGNMENT);
        
        buttons.add(abort);
        buttons.add(next);
        buttons.add(done);
        
        setLayout(new BorderLayout());
        add(buttons, BorderLayout.PAGE_END);
        
        setPreferredSize(new Dimension(500, 400));
    }
    
    protected void next() {}
    
    protected void done() {}
    
    protected void abort() {}
    
    public void setMode(int mask) {
        done.setEnabled((mask & OPTION_DONE) != 0);
        abort.setEnabled((mask & OPTION_ABORT) != 0);
        next.setEnabled((mask & OPTION_NEXT) != 0);
    }
    
    public void showWizard() {
        JDialog dialog = new JDialog();
        dispose = dialog;
        dialog.setModal(true);
        dialog.setResizable(false);
        dialog.add(this);
        dialog.pack();
        dialog.setLocationRelativeTo(Selenium.mainWindow);
        dialog.setVisible(true);
    }
    
    public void dispose() {
        if (dispose != null) {
            dispose.dispose();
        }
    }
}
