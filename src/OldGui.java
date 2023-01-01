import java.awt.Color;
import java.awt.Container;
import java.awt.GridLayout;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class OldGui {
	
	private static JFrame myframe;
	private static ArrayList <JPanel> components;
	private Container container;
	private int[][] matrix;
	private int n,m;
	
    public OldGui(int[][] matrix){

    	this.n = matrix.length;
    	this.m = matrix[0].length;
    	
        myframe = new JFrame("Conway's Game of Life");
        myframe.setSize( 800, 600 );
        myframe.setResizable(true);
        myframe.setLayout( new GridLayout(n,m) );
		myframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        container = myframe.getContentPane();
        components = new ArrayList < JPanel >();
        JPanel temp = null;

        // Populating Arraylist object.
        for ( int i = 0; i < n; i++ ){
        	for (int j = 0; j < m; j++) {
        		temp = new JPanel();
        		temp.setSize( 360,280 );
        		if(matrix[i][j] == 1) {
            		temp.setBackground(Color.WHITE);
        		}else {
        			temp.setBackground(Color.BLACK);
        		}
        		temp.setBorder(BorderFactory.createLineBorder(Color.black));
        		components.add(temp);
        		container.add(temp);
        	}
        }
        myframe.setVisible( true );

    }
    
    public void updateGUI(int[][] matrix) {
    	
    	int pos=0;
    	
    	for (int i = 0; i < n; i++) {
			for (int j = 0; j < m; j++) {
				if(matrix[i][j]==1) {
					components.get( pos ).setBackground( Color.WHITE );
				}else {
					components.get( pos ).setBackground( Color.BLACK );
				}
				pos++;
			}
		}
    }
}
