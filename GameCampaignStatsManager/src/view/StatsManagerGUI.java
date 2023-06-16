package view;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.InputMismatchException;

import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.ToolTipManager;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeSelectionModel;

import controller.BattleStatsManager;
import model.Person;
import model.UnitType;

public class StatsManagerGUI extends JFrame {

	/**Obligatory serial version*/
	private static final long serialVersionUID = 1L;

	private JPanel p;

	private CardLayout cl;
	
	private static BattleStatsManager manager;
	
	private static boolean showDead;
	
	public static final String VIEW = "View";
	
	public StatsManagerGUI() {

		p = new JPanel();
		cl = new CardLayout();
		p.setLayout(cl);
		
		//Window formatting
	    setDefaultCloseOperation(EXIT_ON_CLOSE);
	    setSize(900, 700);
	    setTitle("Fantasy Campaign Stats Manager");

		manager = new BattleStatsManager();
	      
	    p.add(new IntroPanel(), "Intro");	    
	    cl.show(p, "Intro");
	    getContentPane().add(p, BorderLayout.CENTER);

	    setVisible(true);
	}

	@SuppressWarnings("unused")
	public static void main(String[] args) {
		StatsManagerGUI hg = new StatsManagerGUI();
	}
	
	private void switchToPanel(JPanel panel, String name) {
		BattleStatsManager.selectedObject = null;
		p.add(panel, name);
		cl.show(p, name);
		validate();
		repaint();
	}	
	private String validateInputName(String s, String field) {
		//Assume the string is not null or empty, because that just means a random name
		//should be used
		if (s == null || s.equals("")) {
			throw new IllegalArgumentException("Input for " + field + " cannot be null or empty");
		}
		s.trim();
//		if (s.length() > 24) {
//			throw new IllegalArgumentException("Too long name for " + field);
//		}
		if (s.length() < 1) {
			throw new IllegalArgumentException("Input for " + field + " cannot just be whitespace");
		}
//		for (int q = 0; q < s.length(); q++) {
//			char c = s.charAt(q);
//			if (!Character.isLetter(c)
//					&& !Character.isDigit(c)
//					&& c != ' '
//					&& c != '-'
//					&& c != '_'
//					&& c != '.'
//					) {
//				throw new IllegalArgumentException(field + " contains an illegal character");
//			}
//		}
		return s;
	}
	private int parseValidDigitWithinBounds(String s, int min, int max, String field)
			throws IllegalArgumentException {
		s.trim();
		try {
			int num = Integer.parseInt(s);
			if (num >= min && num <= max) {
				return num;
			}
		} catch (InputMismatchException e) {
			throw new IllegalArgumentException("Entered a non-integer for " + field);
		}
		throw new IllegalArgumentException("Entered an out-of-bounds value for " + field);
	}
	
	private class IntroPanel extends JPanel {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public IntroPanel() {
			setLayout(new BorderLayout());
			
			JButton newUnitType = new JButton("New Unit Type");
			newUnitType.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					switchToPanel(new UnitCreatorPanel(), VIEW);
				}
			});
			
			JPanel typesList = new JPanel(new GridLayout(Math.max(1,
					manager.getTypesList().size()), 1));
			for (int q = 0; q < manager.getTypesList().size(); q++) {
				typesList.add(new JLabel(manager.getTypesList().get(q).getName()));
			}
			JScrollPane unitScroller = new JScrollPane(typesList);
			
			JButton selectAndStart = new JButton("Select Unit And Start");
			selectAndStart.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					switchToPanel(new PickUnitPanel(), VIEW);
				}
			});
			
			JButton loadAndStart = new JButton("Load Unit to Manage");
			loadAndStart.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					JFileChooser fc = new JFileChooser("./saves");
					int val = fc.showOpenDialog(StatsManagerGUI.this);
					if (val == JFileChooser.APPROVE_OPTION) {
						File file = fc.getSelectedFile();
						try {
							manager.loadStats(file);
							switchToPanel(new PrimaryManagerPanel(), VIEW);
						} catch (Exception ex) {
							JOptionPane.showMessageDialog(null, ex.getMessage());
							ex.printStackTrace();
						}
					}
				}
			});
			
			JPanel topPart = new JPanel(new BorderLayout());
			topPart.add(unitScroller, BorderLayout.CENTER);
			topPart.add(newUnitType, BorderLayout.LINE_END);
			
			JPanel bottomPart = new JPanel(new GridLayout(1, 2));
			bottomPart.add(selectAndStart);
			bottomPart.add(loadAndStart);
			
			add(topPart);
			add(bottomPart, BorderLayout.PAGE_END);
			

		}
	}
	
	private class UnitCreatorPanel extends JPanel {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public UnitCreatorPanel() {
			setLayout(new BorderLayout());
			
			JPanel typesList = new JPanel(new GridLayout(manager.getTypesList().size() + 1, 2));
			ArrayList<JTextField> numbers = new ArrayList<>(manager.getTypesList().size());
			JTextField individuals = new JTextField();
			typesList.add(individuals);
			typesList.add(new JLabel("Individual Soldier"));
			for (int q = 0; q < manager.getTypesList().size(); q++) {
				numbers.add(new JTextField());
				typesList.add(numbers.get(q));
				typesList.add(new JLabel(manager.getTypesList().get(q).getName()));
			}
			JScrollPane unitScroller = new JScrollPane(typesList);

			JTextField unitName = new JTextField();
			
			JButton confirm = new JButton("Confirm");
			confirm.setBackground(Color.GREEN);
			confirm.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					try {
						UnitType creation = new UnitType();
						creation.setName(validateInputName(unitName.getText(), "Name"));
						
						ArrayList<Integer> startIdxs = new ArrayList<>();
						for (int q = 0; q < numbers.size(); q++) {
							JTextField field = numbers.get(q);
							if (field.getText() != null && !field.getText().equals("")) {
								startIdxs.add(creation.getSubUnits().size()); //Log the index of the first unit of this type
								int amount = parseValidDigitWithinBounds(field.getText(),
										1, Integer.MAX_VALUE, manager.getTypesList().get(q).getName());
								for (int w = 0; w < amount; w++) {
									creation.addUnit(manager.getTypesList().get(q).clone());
								}
							}
						}
						
						if (individuals.getText() != null && !individuals.getText().equals("")) {
							int amount = parseValidDigitWithinBounds(individuals.getText(),
									1, 1000, "Individual Soldier");
							for (int q = 0; q < amount; q++) {
								creation.addMember();
							}
						}
						
						if (creation.isEmpty()) {
							throw new IllegalArgumentException("Unit must contain some sub-unit(s)");
						}
						switchToPanel(new SelectLeaderPanel(creation, startIdxs), VIEW);
					} catch (Exception ex) {
						JOptionPane.showMessageDialog(null, ex.getMessage());
						return;
					}
				}
			});
			
			JButton cancel = new JButton("Cancel");
			cancel.setBackground(Color.RED);
			cancel.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					switchToPanel(new IntroPanel(), VIEW);
				}
			});
			
			JPanel topPart = new JPanel(new BorderLayout());
			
			JPanel buttons = new JPanel(new GridLayout(2, 1));
			buttons.add(cancel);
			buttons.add(confirm);
			
			topPart.add(unitScroller);
			topPart.add(buttons, BorderLayout.LINE_END);
			
			add(topPart);
			add(unitName, BorderLayout.PAGE_END);
		}
	}
	
	private class SelectLeaderPanel extends JPanel {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public SelectLeaderPanel(UnitType creation, ArrayList<Integer> startIndexes) {
			setLayout(new BorderLayout());

			int numCandidates = startIndexes.size();
			if (!creation.getMembers().isEmpty()) {
				numCandidates++;
			}
			
			JPanel officersList = new JPanel();
			officersList.setLayout(new GridLayout(numCandidates, 1));
			ButtonGroup group = new ButtonGroup();
			JRadioButton individual = new JRadioButton("Individual Soldier");
			if (!creation.getMembers().isEmpty()) {
				group.add(individual);
				officersList.add(individual);
			}
			ArrayList<JRadioButton> subs = new ArrayList<>(startIndexes.size());
			for (int q = 0; q < startIndexes.size(); q++) {
				JRadioButton rb = new JRadioButton(creation.getSubUnits().get(startIndexes.get(q)).getName());
				group.add(rb);
				subs.add(rb);
				officersList.add(rb);
			}
			JScrollPane officerScroller = new JScrollPane(officersList);
			
			JTextField rankName = new JTextField();
			
			JButton confirm = new JButton("Confirm");
			confirm.setBackground(Color.GREEN);
			confirm.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (group.getSelection() == null) {
						JOptionPane.showMessageDialog(null, "You must select a leading unit");
						return;
					}
					if (rankName.getText() == null || rankName.getText().equals("")) {
						JOptionPane.showMessageDialog(null, "You must provide a rank title");
						return;
					}
					try {
						String name = validateInputName(rankName.getText(), "Rank name");
						if (individual.isSelected()) {
							creation.setOfficer(name, -1);
						} else {
							for (int q = 0; q < subs.size(); q++) {
								if (subs.get(q).isSelected()) {
									int idx = startIndexes.get(q);
									creation.setOfficer(name, idx);
//									creation.getSubUnits().get(idx).setOfficer(name, idx);
									break;
								}
							}
						}
						manager.getTypesList().add(creation);
						switchToPanel(new IntroPanel(), VIEW);
					} catch (Exception ex) {
						JOptionPane.showMessageDialog(null, ex.getMessage());
					}
				}
			});
			
			JButton cancel = new JButton("Cancel");
			cancel.setBackground(Color.RED);
			cancel.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					switchToPanel(new IntroPanel(), VIEW);
				}
			});
			
			JPanel topPart = new JPanel(new BorderLayout());
			
			JPanel buttons = new JPanel(new GridLayout(2, 1));
			buttons.add(cancel);
			buttons.add(confirm);
			
			topPart.add(officerScroller);
			topPart.add(buttons, BorderLayout.LINE_END);
			
			add(topPart);
			add(rankName, BorderLayout.PAGE_END);

		}
	}
	
	private class PickUnitPanel extends JPanel {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public PickUnitPanel() {
			setLayout(new BorderLayout());
			
			JPanel typesList = new JPanel(new GridLayout(manager.getTypesList().size(), 1));
			ArrayList<JRadioButton> typeButtons = new ArrayList<>();
			ButtonGroup group = new ButtonGroup();
			for (int q = 0; q < manager.getTypesList().size(); q++) {
				typeButtons.add(new JRadioButton(manager.getTypesList().get(q).getName() + " (" + manager.getTypesList().get(q).getOfficerTitle() + ")"));
				group.add(typeButtons.get(q));
				typesList.add(typeButtons.get(q));
			}
			JScrollPane typesScroller = new JScrollPane(typesList);
			
			JTextField unitName = new JTextField();
			
			JButton confirm = new JButton("Confirm");
			confirm.setBackground(Color.GREEN);
			confirm.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (group.getSelection() == null) {
						JOptionPane.showMessageDialog(null, "Select a unit type");
						return;
					}
					if (unitName.getText() == null || unitName.getText().equals("")) {
						JOptionPane.showMessageDialog(null, "Enter a name for the unit");
						return;
					}
					try {
						String name = validateInputName(unitName.getText(), "Name");
						for (int q = 0; q < typeButtons.size(); q++) {
							if (typeButtons.get(q).isSelected()) {
								manager.setTrackedUnit(manager.getTypesList().get(q).clone());
								manager.getTrackedUnit().setName(name);
								break;
							}
						}
						switchToPanel(new PrimaryManagerPanel(), VIEW);
					} catch (Exception ex) {
						JOptionPane.showMessageDialog(null, ex.getMessage());
					}
				}
			});
			
			JButton cancel = new JButton("Cancel");
			cancel.setBackground(Color.RED);
			cancel.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					switchToPanel(new IntroPanel(), VIEW);
				}
			});
			
			JPanel topPart = new JPanel(new BorderLayout());
			
			JPanel buttons = new JPanel(new GridLayout(2, 1));
			buttons.add(cancel);
			buttons.add(confirm);
			
			topPart.add(typesScroller);
			topPart.add(buttons, BorderLayout.LINE_END);
			
			add(topPart);
			add(unitName, BorderLayout.PAGE_END);
		}
	}
	
	private class PrimaryManagerPanel extends JPanel {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public PrimaryManagerPanel() {
			setLayout(new BorderLayout());
			
			JTree tree = getUnitList();
			tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
			tree.addTreeSelectionListener(new TreeSelectionListener() {
				@Override
				public void valueChanged(TreeSelectionEvent e) {
					DefaultMutableTreeNode node = (DefaultMutableTreeNode)tree.getLastSelectedPathComponent();
					Object data = node.getUserObject();
					if (data instanceof Person) {
						Person p = (Person)data;
						StringBuilder sb = new StringBuilder("<html>Character Data<br/>");
						sb.append("<html>ID: " + p.getId() + "<br/>");
						sb.append("<html>Rank/Role: " + p.getRank() + "<br/>");
						sb.append("<html>Name: " + p.getName() + "<br/>");
						sb.append("<html>Alive: " + p.isAlive() + "<br/>");
						sb.append("<html>Total Score: " + p.getScore() + "<br/>");
						sb.append("<html>Join Time: " + p.getJoinTime() + "<br/>");
						sb.append("<html><br/><html>Battle Data:<br/>");
						ArrayList<int[]> battles = p.getBattles();
						for (int q = 0; q < battles.size(); q++) {
							int[] b = battles.get(q);
							String name = BattleStatsManager.battles.get(b[0]).getName();
							sb.append("<html>" + name + ": ");
							if (b[1] == Person.DIED) {
								sb.append("Died");
							} else {
								sb.append(b[1] + " kills");
							}
							sb.append("<br/>");
						}
						sb.append("<html><br/>");
						if (!p.getPromotions().isEmpty()) {
							sb.append("<html>They received " + p.getPromotions().size() + " promotions after:<br/>");
							for (int q = 0; q < p.getPromotions().size(); q++) {
								int idx = p.getPromotions().get(q);
								if (idx == -1) {
									sb.append("<html>Before any battle<br/>");
									continue;
								}
								String name = BattleStatsManager.battles.get(idx).getName();
								sb.append("<html>" + name + "<br/>");
							}
						}
						JOptionPane.showMessageDialog(null, sb.toString());
					} else if (data instanceof UnitType) {
						BattleStatsManager.selectedObject = data;
					}
				}
			});
			
			add(new JScrollPane(tree));
			
			JButton names = new JButton("Give Names");
			names.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					switchToPanel(new GiveNamesPanel(), VIEW);
				}
			});
			JButton battle = new JButton("Register Battle");
			battle.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					switchToPanel(new RegisterBattlePanel(), VIEW);
				}
			});
			JButton organization = new JButton("Organization");
			organization.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					switchToPanel(new OrganizationPanel(), VIEW);
				}
			});
			JButton check = new JButton("Check Unit");
			check.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (BattleStatsManager.selectedObject == null) {
						JOptionPane.showMessageDialog(null, "Select a unit to check");
						return;
					}
					String report = manager.getUnitStats();
					JOptionPane.showMessageDialog(null, report);
				}
			});
			JButton roles = new JButton("Special Roles");
			roles.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					switchToPanel(new GiveRolesPanel(), VIEW);
				}
			});
			JButton toggleShowDead = new JButton("Show/Hide Dead");
			toggleShowDead.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					showDead = !showDead;
					switchToPanel(new PrimaryManagerPanel(), VIEW);
				}
			});
			JButton save = new JButton("Save");
			save.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					try {
						manager.saveStats();
						JOptionPane.showMessageDialog(null, "Saved!");
					} catch (Exception ex) {
						JOptionPane.showMessageDialog(null, ex.getMessage());
						ex.printStackTrace();
					}
				}
			});
			JButton print = new JButton("Print Data");
			print.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					try {
						manager.printStats();
						JOptionPane.showMessageDialog(null, "Printed!");
					} catch (Exception ex) {
						JOptionPane.showMessageDialog(null, ex.getMessage());
					}
				}
			});
			
			JPanel buttons = new JPanel(new GridLayout(4, 2));
			buttons.add(names);
			buttons.add(battle);
			buttons.add(organization);
			buttons.add(check);
			buttons.add(roles);
			buttons.add(toggleShowDead);
			buttons.add(save);
			buttons.add(print);
			add(buttons, BorderLayout.PAGE_END);
		}
	}
	private JTree getUnitList() {
		UnitType tracked = manager.getTrackedUnit();
		DefaultMutableTreeNode root = new DefaultMutableTreeNode(tracked);
		JTree ret = new JTree(root);
		
		recursiveNodeAddition(root, tracked);
		
		ToolTipManager.sharedInstance().registerComponent(ret);
		ImageIcon participant = new ImageIcon("images/participant.png");
		participant = new ImageIcon(participant.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH));
		ImageIcon absent = new ImageIcon("images/absent.png");
		absent = new ImageIcon(absent.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH));
		ImageIcon alive = new ImageIcon("images/alive.png");
		alive = new ImageIcon(alive.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH));
		ImageIcon dead = new ImageIcon("images/dead.png");
		dead = new ImageIcon(dead.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH));
		if (participant != null && absent != null && alive != null && dead != null) {
			ret.setCellRenderer(new MyRenderer(participant, absent, alive, dead));
		}
		return ret;
	}
	private void recursiveNodeAddition(DefaultMutableTreeNode parent, UnitType unit) {
		for (int q = 0; q < unit.getMembers().size(); q++) {
			if (unit.getMembers().get(q).isAlive() || showDead) {
				parent.add(new DefaultMutableTreeNode(unit.getMembers().get(q)));
			}
		}
		for (int q = 0; q < unit.getSubUnits().size(); q++) {
			DefaultMutableTreeNode branch = new DefaultMutableTreeNode(unit.getSubUnits().get(q));
			parent.add(branch);
			recursiveNodeAddition(branch, unit.getSubUnits().get(q));
		}
	}
	//Credit to docs.oracle.com for the JTree tutorial and for this MyRenderer class
	private class MyRenderer extends DefaultTreeCellRenderer {
	    /**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		Icon participatingIcon;
	    Icon absentIcon;
	    Icon aliveIcon;
	    Icon deadIcon;

	    public MyRenderer(Icon participating, Icon absent, Icon alive, Icon dead) {
	        this.participatingIcon = participating;
	        this.absentIcon = absent;
	        this.aliveIcon = alive;
	        this.deadIcon = dead;
	    }

	    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel,
	                        boolean expanded, boolean leaf, int row, boolean hasFocus) {

	    	super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
	        
	        if (leaf) {
		        DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
		        Person nodeInfo = (Person)node.getUserObject();
		        if (nodeInfo.isAlive()) {
		        	setIcon(aliveIcon);
		        } else {
		        	setIcon(deadIcon);
		        }
	        	if (nodeInfo.isParticipating()) {
		        	setToolTipText("Participant");
	        	} else {
		        	setToolTipText("Non-Participant");
	        	}
	        } else {
		        DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
		        UnitType nodeInfo = (UnitType)node.getUserObject();
	        	if (nodeInfo.isParticipating()) {
		        	setIcon(participatingIcon);
	        	} else {
		        	setIcon(absentIcon);
	        	}
	        } 

	        return this;
	    }
	}
	
	private class GiveNamesPanel extends JPanel {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public GiveNamesPanel() {
			setLayout(new BorderLayout());

			JTree tree = getUnitList();
			tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
			tree.addTreeSelectionListener(new TreeSelectionListener() {
				@Override
				public void valueChanged(TreeSelectionEvent e) {
					DefaultMutableTreeNode node = (DefaultMutableTreeNode)tree.getLastSelectedPathComponent();
					BattleStatsManager.selectedObject = node.getUserObject();
				}
			});
			
			JTextField nameField = new JTextField();
			JButton confirm = new JButton("Confirm Name");
			confirm.setBackground(Color.GREEN);
			confirm.setForeground(Color.BLACK);
			confirm.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (BattleStatsManager.selectedObject == null) {
						JOptionPane.showMessageDialog(null, "Must select a unit or person");
						return;
					}
					if (nameField.getText() == null || nameField.getText().equals("")) {
						JOptionPane.showMessageDialog(null, "Must enter a name");
						return;
					}
					try {
						String name = validateInputName(nameField.getText(), "Name");
						manager.renameSelectedObject(name); //This method auto-sets selectedObject to null
						nameField.setText("");
						tree.validate();
						tree.repaint();
					} catch (Exception ex) {
						JOptionPane.showMessageDialog(null, ex.getMessage());
					}
				}
			});
			
			JButton done = new JButton("Done");
			done.setBackground(Color.CYAN);
			done.setForeground(Color.BLACK);
			done.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					BattleStatsManager.selectedObject = null;
					switchToPanel(new PrimaryManagerPanel(), VIEW);
				}
			});
			
			JButton random = new JButton("Load Random Names");
			random.setBackground(Color.YELLOW);
			random.setForeground(Color.BLACK);
			random.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (BattleStatsManager.selectedObject == null
							|| !(BattleStatsManager.selectedObject instanceof UnitType)) {
						JOptionPane.showMessageDialog(null, "You must select a unit");
						return;
					}
					UnitType unit = (UnitType)BattleStatsManager.selectedObject;
					
					JFileChooser fc = new JFileChooser("./namelists");
					int val = fc.showOpenDialog(StatsManagerGUI.this);
					if (val == JFileChooser.APPROVE_OPTION) {
						File file = fc.getSelectedFile();
						int num = -1;
						while (num == -1) {
							String input = JOptionPane.showInputDialog("How many possible names?");
							try {
								num = parseValidDigitWithinBounds(input, 1, Integer.MAX_VALUE, "Name Count");
								manager.giveNames(file, unit, num);
								tree.validate();
								tree.repaint();
							} catch (Exception ex) {
								JOptionPane.showMessageDialog(null, ex.getMessage());
								ex.printStackTrace();
							}
						}
					}

				}
			});
			
			JPanel bottom = new JPanel(new GridLayout(1, 4));
			bottom.add(random);
			bottom.add(nameField);
			bottom.add(confirm);
			bottom.add(done);
			
			add(new JScrollPane(tree));
			add(bottom, BorderLayout.PAGE_END);
			
		}
	}
	
	private class RegisterBattlePanel extends JPanel {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public RegisterBattlePanel () {
			setLayout(new BorderLayout());
			
			JTree tree = getUnitList();
			tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
			tree.addTreeSelectionListener(new TreeSelectionListener() {
				@Override
				public void valueChanged(TreeSelectionEvent e) {
					DefaultMutableTreeNode node = (DefaultMutableTreeNode)tree.getLastSelectedPathComponent();
					BattleStatsManager.selectedObject = node.getUserObject();
				}
			});
			
			JButton toggle = new JButton("Toggle Participation");
			toggle.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (BattleStatsManager.selectedObject == null) {
						JOptionPane.showMessageDialog(null, "Must select a unit or person");
						return;
					}
					if (BattleStatsManager.selectedObject instanceof Person) {
						Person p = (Person)BattleStatsManager.selectedObject;
						p.setParticipating(!p.isParticipating());
					} else if (BattleStatsManager.selectedObject instanceof UnitType) {
						UnitType u = (UnitType)BattleStatsManager.selectedObject;
						u.setParticipating(!u.isParticipating());
					}
					tree.validate();
					tree.repaint();
					BattleStatsManager.selectedObject = null;
				}
			});
			
			JTextField battleName = new JTextField();
			
			JButton next = new JButton("Next (no undoing)");
			next.setBackground(Color.GREEN);
			next.setForeground(Color.BLACK);
			next.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (battleName.getText() == null || battleName.getText().equals("")) {
						JOptionPane.showMessageDialog(null, "Name battle at the top of the screen");
						return;
					}
					try {
						String name = validateInputName(battleName.getText(), "Name");
						manager.startBattle(name);
						switchToPanel(new ManualKDEntryPanel(), VIEW);
					} catch (Exception ex) {
						JOptionPane.showMessageDialog(null, ex.getMessage());
					}
				}
			});
			
			JButton cancel = new JButton("Cancel");
			cancel.setBackground(Color.RED);
			cancel.setForeground(Color.BLACK);
			cancel.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					manager.removeAllParticipants(); //Auto-sets selectedObject to null
					switchToPanel(new PrimaryManagerPanel(), VIEW);
				}
			});
			
			JPanel buttons = new JPanel(new GridLayout(1, 3));
			buttons.add(toggle);
			buttons.add(next);
			buttons.add(cancel);
			
			add(battleName, BorderLayout.NORTH);
			add(new JScrollPane(tree));
			add(buttons, BorderLayout.PAGE_END);
		}
	}
	
	private class ManualKDEntryPanel extends JPanel {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public ManualKDEntryPanel() {
			setLayout(new BorderLayout());

			JTree tree = getUnitList();
			tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
			tree.addTreeSelectionListener(new TreeSelectionListener() {
				@Override
				public void valueChanged(TreeSelectionEvent e) {
					DefaultMutableTreeNode node = (DefaultMutableTreeNode)tree.getLastSelectedPathComponent();
					Object data = node.getUserObject();
					if (data instanceof Person && ((Person)data).isParticipating()
							&& ((Person)data).getCurrentBattleScore() == Person.UNDETERMINED) {
						Person p = (Person)data;
						String[] opts = {"Alive", "Dead"};
						int decision = JOptionPane.showOptionDialog(null,
								"Did " + p.toString() + " survive or die?", "SURVIVAL",
								JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE,
								null, opts, opts[0]);
						if (decision == 0) { //If they lived, get kill count
							p.setSafe(true);
							String num = JOptionPane.showInputDialog("What was their kill count?");
							try {
								int count = parseValidDigitWithinBounds(num, 0, Integer.MAX_VALUE, "Kills");
								manager.registerKills(p, count);
							} catch (Exception ex) {
								JOptionPane.showMessageDialog(null, ex.getMessage() + ". Please try again");
							}
						} else if (decision == 1) { //If they died, confirm
							String[] opts2 = {"Yes", "No"};
							int decision2 = JOptionPane.showOptionDialog(null,
									"Are you sure you want to kill off " + p.toString() + "?", "SURVIVAL",
									JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE,
									null, opts2, opts2[0]);
							if (decision2 == 0) {
								manager.registerDeath(p);
							}
						}
					}
				}
			});
			
			JTextField allKills = new JTextField();
			JTextField allDeaths = new JTextField();

			JButton done = new JButton("Done");
			done.setBackground(Color.GREEN);
			done.setForeground(Color.BLACK);
			done.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (allKills.getText() == null || allKills.getText().equals("")) {
						JOptionPane.showMessageDialog(null, "Enter kill count");
					}
					if (allDeaths.getText() == null || allDeaths.getText().equals("")) {
						JOptionPane.showMessageDialog(null, "Enter death count");
					}
					try {
						int kills = parseValidDigitWithinBounds(allKills.getText(),
								0, Integer.MAX_VALUE, "Other Kills");
						int deaths = parseValidDigitWithinBounds(allDeaths.getText(),
								0, Integer.MAX_VALUE, "Other Deaths");
						ArrayList<Person> check = manager.getNamedPeopleToCheck();
						for (int q = 0; q < check.size(); q++) {
							Person p = check.get(q);
							String[] opts = {"Alive", "Dead", "Undetermined"};
							int decision = JOptionPane.showOptionDialog(null,
									"Did " + p.toString() + " survive or die?", "SURVIVAL",
									JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE,
									null, opts, opts[0]);
							if (decision == 0) { //If they lived, just set safe
								p.setSafe(true);
								JOptionPane.showMessageDialog(null, p.toString() + " lived!");
							} else if (decision == 1) { //If they died, confirm
								String[] opts2 = {"Yes", "No"};
								int decision2 = JOptionPane.showOptionDialog(null,
										"Are you sure you want to kill off " + p.toString() + "?", "SURVIVAL",
										JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE,
										null, opts2, opts2[0]);
								if (decision2 == 0) {
									manager.registerDeath(p);
								} else if (decision2 == 1) { //If they changed their mind, give another chance
									q--;
								}
							} else if (decision == 2) { //If unsure, do nothing
								//Nothing
							}
						}
						
						BattleStatsManager.selectedObject = null;
						manager.handleBattle(kills, deaths);
						JOptionPane.showMessageDialog(null, "Battle Registered!");
						switchToPanel(new PrimaryManagerPanel(), VIEW);
					} catch (Exception ex) {
						JOptionPane.showMessageDialog(null, ex.getMessage());
					}
				}
			});
			
			JPanel countsPanel = new JPanel(new GridLayout(1, 4));
			countsPanel.add(new JLabel("Other Kills:"));
			countsPanel.add(allKills);
			countsPanel.add(new JLabel("Other Deaths:"));
			countsPanel.add(allDeaths);
			
			add(new JScrollPane(tree));
			add(countsPanel, BorderLayout.NORTH);
			add(done, BorderLayout.SOUTH);
		}
	}
	
	private class OrganizationPanel extends JPanel {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public OrganizationPanel() {
			setLayout(new BorderLayout());
			
			JTree tree = getUnitList();
			tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
			tree.addTreeSelectionListener(new TreeSelectionListener() {
				@Override
				public void valueChanged(TreeSelectionEvent e) {
					DefaultMutableTreeNode node = (DefaultMutableTreeNode)tree.getLastSelectedPathComponent();
					BattleStatsManager.selectedObject = node;
				}
			});

			JTextField numberField = new JTextField();
			
			JButton addTroops = new JButton("Reinforce Unit");
			addTroops.createToolTip();
			addTroops.setToolTipText("Adds a specified amount to the unit");
			addTroops.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (BattleStatsManager.selectedObject == null
							|| !(((DefaultMutableTreeNode)BattleStatsManager.selectedObject).getUserObject()
							instanceof UnitType)) {
						JOptionPane.showMessageDialog(null, "You must select a unit to reinforce");
						return;
					}
					if (numberField.getText() == null || numberField.getText().equals("")) {
						JOptionPane.showMessageDialog(null, "You must enter a number of reinforcements");
						return;
					}
					try {
						UnitType unit = (UnitType)((DefaultMutableTreeNode)BattleStatsManager.selectedObject).getUserObject();
						int num = parseValidDigitWithinBounds(numberField.getText(),
								0, Integer.MAX_VALUE, "Reinforcements");
						int battlesPassed = BattleStatsManager.battles.size();
						for (int q = 0; q < num; q++) {
							unit.addMember(battlesPassed);
						}
						BattleStatsManager.selectedObject = null;
						switchToPanel(new OrganizationPanel(), VIEW);
					} catch (Exception ex) {
						JOptionPane.showMessageDialog(null, ex.getMessage());
					}
				}
			});
			
			JButton promote = new JButton("Promote Character");
			promote.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (BattleStatsManager.selectedObject == null
							|| !(((DefaultMutableTreeNode)BattleStatsManager.selectedObject).getUserObject()
							instanceof Person)) {
						JOptionPane.showMessageDialog(null, "You must select a unit to reinforce");
						return;
					}
					DefaultMutableTreeNode personNode = (DefaultMutableTreeNode)BattleStatsManager.selectedObject;
					Person p = (Person)personNode.getUserObject();
					if (!p.isAlive()) {
						JOptionPane.showMessageDialog(null, "Cannot promote a dead person");
						return;
					}
					DefaultMutableTreeNode unitNode = (DefaultMutableTreeNode)personNode.getParent();
					UnitType u = (UnitType)unitNode.getUserObject();
					String title = u.getOfficerTitle();
					for (int q = 0; q < u.getMembers().size(); q++) {
						Person test = u.getMembers().get(q);
						if (test.isAlive() && test.getRank() != null
								&& test.getRank().contains(title)) {
							JOptionPane.showMessageDialog(null, "Someone else already leads this unit");
							return;
						}
					}
					p.promote(title, BattleStatsManager.battles.size() - 1);
					tree.validate();
					tree.repaint();
				}
			});
			
			JButton autoReinforce = new JButton("Auto-Reinforce");
			autoReinforce.createToolTip();
			autoReinforce.setToolTipText("Gets all sub-units back to their original number");
			autoReinforce.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (BattleStatsManager.selectedObject == null
							|| !(((DefaultMutableTreeNode)BattleStatsManager.selectedObject).getUserObject()
							instanceof UnitType)) {
						JOptionPane.showMessageDialog(null, "You must select a unit to reinforce");
						return;
					}
					String[] opts = {"Yes", "No"};
					int decision = JOptionPane.showOptionDialog(null,
							"Auto-reinforce this unit and all its sub-units?", "AUTO-REINFORCE",
							JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE,
							null, opts, opts[0]);
					if (decision == 1) {
						return;
					}
					UnitType unit = (UnitType)((DefaultMutableTreeNode)BattleStatsManager.selectedObject).getUserObject();
					int battlesPassed = BattleStatsManager.battles.size();
					autoReinforceHelper(unit, battlesPassed);
					
					BattleStatsManager.selectedObject = null;
					JOptionPane.showMessageDialog(null, "Done");
					switchToPanel(new OrganizationPanel(), VIEW);
				}
			});
			
			JButton autoPromote = new JButton("Auto-Promote All");
			autoPromote.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					String[] opts = {"Yes", "No"};
					int decision = JOptionPane.showOptionDialog(null,
							"Auto-promote for ALL units?", "AUTO-PROMOTE",
							JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE,
							null, opts, opts[0]);
					if (decision == 1) {
						return;
					}
					autoPromoteHelper(BattleStatsManager.trackedUnit,
							BattleStatsManager.battles.size() - 1);
					
					BattleStatsManager.selectedObject = null;
					JOptionPane.showMessageDialog(null, "Done");
					tree.validate();
					tree.repaint();
				}
			});
			
			JButton back = new JButton("Back");
			back.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					switchToPanel(new PrimaryManagerPanel(), VIEW);
				}
			});
			
			JPanel buttons = new JPanel(new GridLayout(1, 6));
			buttons.add(numberField);
			buttons.add(addTroops);
			buttons.add(promote);
			buttons.add(autoReinforce);
			buttons.add(autoPromote);
			buttons.add(back);
			
			add(new JScrollPane(tree));
			add(buttons, BorderLayout.SOUTH);
		}
	}
	private void autoReinforceHelper(UnitType unit, int battlesPassed) {
		int needed = 0;
		for (int q = 0; q < unit.getMembers().size(); q++) {
			Person p = unit.getMembers().get(q);
			if (p.getJoinTime() == 0) {
				needed++;
			}
			if (p.isAlive()) {
				needed--;
			}
		}
		for (int q = 0; q < needed; q++) {
			unit.addMember(battlesPassed);
		}
		for (int q = 0; q < unit.getSubUnits().size(); q++) {
			autoReinforceHelper(unit, battlesPassed);
		}
	}
	private void autoPromoteHelper(UnitType unit, int battleId) {
		boolean found = false;
		for (int q = 0; q < unit.getMembers().size(); q++) {
			Person p = unit.getMembers().get(q);
			if (p.isAlive() && p.getRank() != null
					&& p.getRank().contains(unit.getOfficerTitle())) {
				found = true;
				break;
			}
		}
		if (!found) {
			for (int q = 0; q < unit.getMembers().size(); q++) {
				Person p = unit.getMembers().get(q);
				if (p.isAlive()) {
					p.promote(unit.getOfficerTitle(), battleId);
					break;
				}
			}
		}
		for (int q = 0; q < unit.getSubUnits().size(); q++) {
			autoPromoteHelper(unit.getSubUnits().get(q), battleId);
		}
	}
	
	private class GiveRolesPanel extends JPanel {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public GiveRolesPanel() {
			setLayout(new BorderLayout());
			
			JTree tree = getUnitList();
			tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
			tree.addTreeSelectionListener(new TreeSelectionListener() {
				@Override
				public void valueChanged(TreeSelectionEvent e) {
					DefaultMutableTreeNode node = (DefaultMutableTreeNode)tree.getLastSelectedPathComponent();
					BattleStatsManager.selectedObject = node.getUserObject();
				}
			});
			
			JTextField promoName = new JTextField();
			
			JButton promote = new JButton("Give Role");
			promote.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (promoName.getText() == null || promoName.getText().equals("")) {
						JOptionPane.showMessageDialog(null, "You must give a name");
						return;
					}
					if (BattleStatsManager.selectedObject == null) {
						JOptionPane.showMessageDialog(null, "You must select a person or unit");
						return;
					}
					try {
						String name = validateInputName(promoName.getText(), "Name");
						int battleId = BattleStatsManager.battles.size() - 1;
						if (BattleStatsManager.selectedObject instanceof Person) {
							Person p = (Person)BattleStatsManager.selectedObject;
							String[] opts = {"Yes", "No"};
							int decision = JOptionPane.showOptionDialog(null,
									"Treat role as a promotion?", "PROMOTE",
									JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE,
									null, opts, opts[0]);
							if (decision == 0) {
								p.promote(name, battleId);
							} else if (decision == 1) {
								p.setRank(name);
							}
						} else if (BattleStatsManager.selectedObject instanceof UnitType) {
							UnitType u = (UnitType)BattleStatsManager.selectedObject;
							String[] opts = {"Yes", "No"};
							int decision = JOptionPane.showOptionDialog(null,
									"Are you sure you want to give this role?", "PROMOTE",
									JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE,
									null, opts, opts[0]);
							if (decision == 0) {
								giveRolesHelper(u, name);
							}
						}
						promoName.setText("");
						tree.validate();
						tree.repaint();
					} catch (Exception ex) {
						JOptionPane.showMessageDialog(null, ex.getMessage());
					}
				}
			});

			JButton addRole = new JButton("Add Role");
			addRole.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (promoName.getText() == null || promoName.getText().equals("")) {
						JOptionPane.showMessageDialog(null, "You must give a name");
						return;
					}
					if (BattleStatsManager.selectedObject == null) {
						JOptionPane.showMessageDialog(null, "You must select a person or unit");
						return;
					}
					try {
						String name = validateInputName(promoName.getText(), "Name");
						int battleId = BattleStatsManager.battles.size() - 1;
						if (BattleStatsManager.selectedObject instanceof Person) {
							Person p = (Person)BattleStatsManager.selectedObject;
							String[] opts = {"Yes", "No"};
							int decision = JOptionPane.showOptionDialog(null,
									"Treat role as a promotion?", "PROMOTE",
									JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE,
									null, opts, opts[0]);
							if (decision == 0) {
								if (p.getRank() != null) {
									name = name + " " + p.getRank();
								}
								p.promote(name, battleId);
							} else if (decision == 1) {
								if (p.getRank() != null) {
									name = name + " " + p.getRank();
								}
								p.setRank(name);
							}
						} else if (BattleStatsManager.selectedObject instanceof UnitType) {
							UnitType u = (UnitType)BattleStatsManager.selectedObject;
							String[] opts = {"Yes", "No"};
							int decision = JOptionPane.showOptionDialog(null,
									"Are you sure you want to give this role?", "PROMOTE",
									JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE,
									null, opts, opts[0]);
							if (decision == 0) {
								giveRolesHelper(u, name);
							}
						}
						promoName.setText("");
						tree.validate();
						tree.repaint();
					} catch (Exception ex) {
						JOptionPane.showMessageDialog(null, ex.getMessage());
					}
				}
			});
			
			JButton done = new JButton("Done");
			done.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					switchToPanel(new PrimaryManagerPanel(), VIEW);
				}
			});
			
			JPanel bottom = new JPanel(new GridLayout(1, 4));
			bottom.add(promoName);
			bottom.add(promote);
			bottom.add(addRole);
			bottom.add(done);
			
			add(new JScrollPane(tree));
			add(bottom, BorderLayout.PAGE_END);
		}
	}
	private void giveRolesHelper(UnitType u, String name) {
		for (int q = 0; q < u.getMembers().size(); q++) {
			Person p = u.getMembers().get(q);
			if (p.getRank() == null) {
				p.setRank(name);
			} else {
				String real = name + " " + p.getRank();
				p.setRank(real);
			}
		}
		for (int q = 0; q < u.getSubUnits().size(); q++) {
			giveRolesHelper(u.getSubUnits().get(q), name);
		}
	}

}
