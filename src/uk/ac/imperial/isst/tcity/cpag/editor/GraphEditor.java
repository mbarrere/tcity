package uk.ac.imperial.isst.tcity.cpag.editor;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.UIManager;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;


public class GraphEditor extends JFrame implements ActionListener, GraphListener {

	private static final long serialVersionUID = 508317535368185508L;
	
	private static final Logger logger = LogManager.getLogger(GraphEditor.class);
	
	private static int FRAME_WIDTH = 1000;
	private static int FRAME_HEIGHT = 600;
	
	//private static final int FONTSIZE = 16;
	private static final String APP_TITLE = "T-CITY";
	private static final String APP_INFO =
		    "Show example graph:\n"
		  + "Menu \"File\" => \"Show example\"\n"
		  + "(NOTE: it deletes current graph).\n\n"
		  + "Mouse:\n"
		  + "(action depends on mouse position):\n"
		  + "- LPM - move graph/node/edge\n"
		  + "- PPM - context menu general/node/edge\n\n"
		  + "Keyboard shortcuts:\n"
		  + "- \"q\" - show/hide grid helper\n"
		  + "- \"z\" - add node\n"
		  + "- \"x\" - add edge\n"
		  + "- Alt + \"LETTER\" - open related menu\n"
		  + "- Ctrl + \"s\" - save to file\n"
		  + "- Ctrl + \"o\" - load from file\n\n"
		  + "When cursor is over special node/edge\n"
		  + "- \"r\", \"g\", \"b\" - set color to red/green/blue\n"
		  + "- \"+\" - increase size\n"
		  + "- \"-\" - decrease size\n";
	
	
	private static final String AUTHOR_INFO =   
			  "Program: " + APP_TITLE + "\n" 
			+ "Author:  Martin Barrere \n"
			+ "         Institute for Security Science and Technology\n"
			+ "         Imperial College London\n"
			+ "         m.barrere@imperial.ac.uk\n"
			+ "Date:    May 2022\n"
			+ "Ack:     T-CITY's graphical frontend has been originally\n"
			+ "         based on Michal Tkacz's graph editor\n"
			;
	
	private static final String AUTOSAVE_FILE = "autosave-tcity.bin";
	
	WindowAdapter windowListener = new WindowAdapter() {
		@Override
		public void windowClosed(WindowEvent e) {
			// metodh dispose()
			//System.out.println("Closing 3 dispose");
			JOptionPane.showMessageDialog(null, "Program closed!");
		}

		@Override
		public void windowClosing(WindowEvent e) {
			//System.out.println("Closing 4");
			windowClosed(e);
		}
	};
	
	public void graphUpdated(Graph graph) {
		//this.status.setText(graph.getNodes().toString());
		//System.out.println("Graph editor notified of graph updates");
	};
	

	public static void main(String[] args) {
		Configurator.setRootLevel(Level.DEBUG);
        logger.debug("Entering application.");
        logger.trace("Trace - Entering application.");        		
		
		//System.setProperty("apple.eawt.quitStrategy", "CLOSE_ALL_WINDOWS");
		new GraphEditor();

	}

	GraphPanel graphPanel = new GraphPanel(null);
	JPanel sidePanel = null;
			
	JMenuBar menuBar = new JMenuBar();
	
	JMenu fileMenu = new JMenu("File");
	JMenuItem newGraphMenuItem = new JMenuItem("New graph", KeyEvent.VK_N);
	JMenuItem showExampleMenuItem = new JMenuItem("Show example", KeyEvent.VK_E);
	JMenuItem saveMenuItem = new JMenuItem("Save BINARY", KeyEvent.VK_S);
	JMenuItem loadMenuItem = new JMenuItem("Load BINARY", KeyEvent.VK_L);
	JMenuItem importJsonMenuItem = new JMenuItem("Open CPAG (JSON)", KeyEvent.VK_I);
	JMenuItem exportJsonMenuItem = new JMenuItem("Export CPAG (JSON)", KeyEvent.VK_J);
	
	JMenu toolsMenu = new JMenu("Tools");
	JMenuItem layoutLRCPAGMenuItem = new JMenuItem("Apply autolayout (left-right)"); //, KeyEvent.VK_N);
	JMenuItem layoutTDCPAGMenuItem = new JMenuItem("Apply autolayout (top-down)"); //, KeyEvent.VK_N);
	//JMenuItem importCPAGMenuItem = new JMenuItem("Import CPAG structure (JSON)"); //, KeyEvent.VK_N);
	JMenuItem exportCPAGMenuItem = new JMenuItem("Export CPAG structure only (JSON)"); //, KeyEvent.VK_E);
	
	JMenu graphMenu = new JMenu("Graph");
	JMenuItem nodesMenuItem = new JMenuItem("Show list of nodes", KeyEvent.VK_N);
	JMenuItem edgesMenuItem = new JMenuItem("Show list of edges", KeyEvent.VK_E);
		
	JMenu metricsMenu = new JMenu("CPAG Metrics");
	//JMenuItem bayesianMenuItem = new JMenuItem("Compute Bayesian risk");
	JMenuItem bayesianMenuItem = new JCheckBoxMenuItem("Auto Bayesian risk", false);
	JMenuItem hardeningMenuItem = new JCheckBoxMenuItem("Auto hardening analysis (Beta)", false);
	
	JMenu operationsMenu = new JMenu("Operations");
	JMenuItem mergeCPAGsMenuItem = new JMenuItem("Merge CPAGs");
	JMenuItem validateCPAGMenuItem = new JMenuItem("Validate CPAG");
	
	JMenu helpMenu = new JMenu("Help");
	JMenuItem drawGridCheckBoxMenuItem = new JCheckBoxMenuItem("Show grid", true);
	JMenuItem backgroundColorMenuItem = new JMenuItem("Change background colour");
	JMenuItem appMenuItem = new JMenuItem("App Info", KeyEvent.VK_A);
	JMenuItem authorMenuItem = new JMenuItem("About", KeyEvent.VK_U);
	
	//JLabel status = new JLabel("Martin");
	
	public GraphEditor() {
		super(APP_TITLE);
		this.setSize(FRAME_WIDTH, FRAME_HEIGHT);
		this.setResizable(true);
		//this.setLocationRelativeTo(null);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		//this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		//this.setContentPane(graphPanel); 		
		UIManager.put("OptionPane.messageFont", new Font("Monospaced", Font.BOLD, 12));
				
		
		//JPanel sidePanel = this.createSliderPanel();
		GraphTreePanel sidePanel = new GraphTreePanel(graphPanel.getGraph());
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, graphPanel, sidePanel);
		splitPane.setOneTouchExpandable(true);
		//Color dividerColor = ColorUtils.fromHexColor("#b5b8c9");
		Color dividerColor = ColorUtils.fromHexColor("#c9cad1");
		splitPane.setBackground(dividerColor);
		//splitPane.setBorder(BorderFactory.createLineBorder(Color.RED, 5, true));
		int divLocation = new Double(FRAME_WIDTH * 0.75).intValue();
		splitPane.setDividerLocation(divLocation);		
		//Provide minimum sizes for the two components in the split pane
		Dimension minimumSize = new Dimension(200, 100);
		graphPanel.setMinimumSize(minimumSize);
		sidePanel.setMinimumSize(minimumSize);
		this.setContentPane(splitPane);
		
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent event) {
				//System.out.println("Closing 1");
				graphPanel.serializeGraph(AUTOSAVE_FILE);
			}

			@Override
			public void windowClosing(WindowEvent event) {
				//System.out.println("Closing 2");
				windowClosed(event);
			}
		});
		
		addActionListeners();
		createMenuBar();	
		
		//this.pack();
		
		
		//showInstruction();
		
		graphPanel.enableGrid(true);		

		graphPanel.registerGraphListener(this);
		graphPanel.registerGraphListener(sidePanel);
		
		graphPanel.deserializeGraph(AUTOSAVE_FILE);
		
		this.setVisible(true);
		
		//SwingUtilities.invokeLater(this);
	}
		
	
	private void addActionListeners() {
		newGraphMenuItem.addActionListener(this);
		showExampleMenuItem.addActionListener(this);
		saveMenuItem.addActionListener(this);
		loadMenuItem.addActionListener(this);
		importJsonMenuItem.addActionListener(this);
		exportJsonMenuItem.addActionListener(this);
		
		layoutLRCPAGMenuItem.addActionListener(this);
		layoutTDCPAGMenuItem.addActionListener(this);		
		exportCPAGMenuItem.addActionListener(this);
		
		nodesMenuItem.addActionListener(this);
		edgesMenuItem.addActionListener(this);
		
		bayesianMenuItem.addActionListener(this);
		hardeningMenuItem.addActionListener(this);
		mergeCPAGsMenuItem.addActionListener(this);
		validateCPAGMenuItem.addActionListener(this);
		
		drawGridCheckBoxMenuItem.addActionListener(this);
		backgroundColorMenuItem.addActionListener(this);
		appMenuItem.addActionListener(this);
		authorMenuItem.addActionListener(this);
	}
	
	private void createMenuBar() {
		fileMenu.setMnemonic(KeyEvent.VK_F);
		fileMenu.add(newGraphMenuItem);
		//fileMenu.addSeparator();
		//fileMenu.add(saveMenuItem);
		//fileMenu.add(loadMenuItem);
		fileMenu.addSeparator();
		fileMenu.add(importJsonMenuItem);
		fileMenu.add(exportJsonMenuItem);
		//fileMenu.addSeparator();
		//fileMenu.add(showExampleMenuItem);
		
		menuBar.add(fileMenu);
		
		metricsMenu.add(bayesianMenuItem);
		if (OperationsManager.HARDENING_ENABLED) {
			metricsMenu.add(hardeningMenuItem);
		}
		//metricsMenu.addSeparator();		
		menuBar.add(metricsMenu);
		
		operationsMenu.add(mergeCPAGsMenuItem);
		operationsMenu.add(validateCPAGMenuItem);
		menuBar.add(operationsMenu);
				
		
		toolsMenu.setMnemonic(KeyEvent.VK_C);		
		toolsMenu.add(layoutLRCPAGMenuItem);
		toolsMenu.add(layoutTDCPAGMenuItem);
		toolsMenu.addSeparator();
		toolsMenu.add(exportCPAGMenuItem);
		menuBar.add(toolsMenu);
		
		/*
		graphMenu.setMnemonic(KeyEvent.VK_G);
		graphMenu.add(nodesMenuItem);
		graphMenu.add(edgesMenuItem);
		menuBar.add(graphMenu);
		*/
		
		helpMenu.setMnemonic(KeyEvent.VK_H);
		helpMenu.add(drawGridCheckBoxMenuItem);
		helpMenu.add(backgroundColorMenuItem);
		helpMenu.addSeparator();
		//helpMenu.add(appMenuItem);
		helpMenu.add(authorMenuItem);
		menuBar.add(helpMenu);
		
		setJMenuBar(menuBar);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object eSource = e.getSource();
		
		if(eSource == newGraphMenuItem) {
			graphPanel.createNewGraph();
			this.resetScreen();
		}
		
		if(eSource == showExampleMenuItem) {
			graphPanel.showExampleGraph();
		}
		
		if(eSource == saveMenuItem) {
			graphPanel.serializeGraph();
		}
		
		if(eSource == loadMenuItem) {
			graphPanel.deserializeGraph();
		}
		
		if(eSource == importJsonMenuItem) {			
			boolean importOk = graphPanel.importJSON();
			if (importOk) {
				this.resetScreen();
			}
			
		}
		
		if(eSource == exportJsonMenuItem) {
			graphPanel.exportFullJSON();
		}
		
		if(eSource == nodesMenuItem) {
			graphPanel.showNodesList();
		}
		
		if(eSource == layoutLRCPAGMenuItem) {
			graphPanel.applyAutolayout(false);
		}
		
		if(eSource == layoutTDCPAGMenuItem) {
			graphPanel.applyAutolayout(true);
		}
		
		if(eSource == exportCPAGMenuItem) {
			graphPanel.exportCPAG();
		}
		
		if(eSource == bayesianMenuItem) {
			//graphPanel.bayesianAnalysis();
			boolean analysisOk = graphPanel.setAutoBayesian(bayesianMenuItem.isSelected());
			if (!analysisOk) {
				bayesianMenuItem.setSelected(false);
			}
		}
		
		if(eSource == hardeningMenuItem) {			
			boolean analysisOk = graphPanel.setAutoHardening(hardeningMenuItem.isSelected());
			if (!analysisOk) {
				hardeningMenuItem.setSelected(false);
			}
		}
		
		if(eSource == mergeCPAGsMenuItem) {
			graphPanel.mergeCPAGs();
		}
		
		if(eSource == validateCPAGMenuItem) {
			graphPanel.validateCPAG();
		}
		
		if(eSource == edgesMenuItem) {
			graphPanel.showEdgesList();
		}
		
		if(eSource == drawGridCheckBoxMenuItem) {
			graphPanel.enableGrid(drawGridCheckBoxMenuItem.isSelected());
		}
		if(eSource == backgroundColorMenuItem) {
			graphPanel.changeBackgroundColour();
		}

		if(eSource == appMenuItem) {
			showInstruction();
		}
		
		if(eSource == authorMenuItem) {
			showAuthorInfo();
		}
	}
	
	private void resetScreen() {
		graphPanel.setAutoBayesian(false);
		this.bayesianMenuItem.setSelected(false);
		this.hardeningMenuItem.setSelected(false);
	}
	
	private void showInstruction() {
		JOptionPane.showMessageDialog(this, APP_INFO, "About", JOptionPane.INFORMATION_MESSAGE);
	}
	
	private void showAuthorInfo() {
		JOptionPane.showMessageDialog(this, AUTHOR_INFO, "About", JOptionPane.INFORMATION_MESSAGE);
	}
	
	
	public void nodeClicked(Graph graph, Node node) {
    	
    }
    
	public void edgeClicked(Graph graph, Edge edge) {
		
	}


	@Override
	public void bayesianReset(Graph graph) {
		this.bayesianMenuItem.setSelected(false);		
	}

}
