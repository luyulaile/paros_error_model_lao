package experimentalcode.franz;

import de.lmu.ifi.dbs.paros.graph.Path;
import de.lmu.ifi.dbs.utilities.Arrays2;
import experimentalcode.franz.OSMUtils.PATH_ATTRIBUTES;
import experimentalcode.franz.osm.OSMGraph;
import experimentalcode.franz.osm.OSMLink;
import experimentalcode.franz.osm.OSMNode;
import experimentalcode.franz.simplex.PointPanel;
import experimentalcode.franz.simplex.PointPanel.PointSource;
import experimentalcode.franz.simplex.SimplexControl;
//import experimentalcode.thomas.AllRoundTrips;

import java.awt.CardLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import org.jdesktop.swingx.JXMapViewer;
import org.jdesktop.swingx.mapviewer.AbstractTileFactory;
import org.jdesktop.swingx.mapviewer.GeoPosition;
import org.jdesktop.swingx.mapviewer.Waypoint;
import org.jdesktop.swingx.mapviewer.WaypointPainter;
import org.jdesktop.swingx.painter.CompoundPainter;
import org.jdesktop.swingx.painter.Painter;
import rbr.gm.v1.Prototype;
import rbr.gm.v1.Prototype.ActionNode;
import rbr.gm.v1.Prototype.Actor;
import rbr.gm.v1.Prototype.EngagementLevel;
import rbr.gm.v1.Prototype.Message;
import rbr.gm.v1.Prototype.Stability;
import rbr.gm.v1.Prototype.StateNode;
import rbr.gm.v1.ThreadForUpdate;
import rbr.gm.v1.progressUpdate;


public class Demo extends javax.swing.JFrame {
    public StateNode stateNode;
    private double temp1;
    private double temp2;
    private double temp3;
    private boolean flag=true;
    ImageIcon people_icon=new javax.swing.ImageIcon("People_icon.png");
    ImageIcon robot_icon=new javax.swing.ImageIcon("Robot_icon.png");
    ImageIcon minus_icon=new javax.swing.ImageIcon("Minus_icon.png");
    ImageIcon plus_icon=new javax.swing.ImageIcon("Plus_icon.png");
    ImageIcon equal_icon=new javax.swing.ImageIcon("Equal_icon.png");
    ImageIcon left_icon=new javax.swing.ImageIcon("Left_icon.png");
    ImageIcon right_icon=new javax.swing.ImageIcon("Right_icon.png");
    ImageIcon forward_icon=new javax.swing.ImageIcon("Forward_icon.png");
    ImageIcon pause_icon=new javax.swing.ImageIcon("Pause_icon.png");
    ImageIcon reverse_icon=new javax.swing.ImageIcon("Reverse_icon.png");
    ImageIcon lastIcon;
    private Thread progressThread;
    private void GM() {
//        busyLabel.setBusy(true);
//        System.out.println(busyLabel.isBusy());
//        busyLabel.updateUI();

        resultTableModel.setRowCount(0);
//        simplexControl1D.setPoints(Collections.EMPTY_LIST);
//        simplexControl2D.setPoints(Collections.EMPTY_LIST);
//        simplexControl3D.setPoints(Collections.EMPTY_LIST);
        pathPainter.setPath(Collections.EMPTY_LIST);
        directionPainter.clear();
        gmPathPainter.clear();

        if (graph == null) {
            JOptionPane.showMessageDialog(this, "Please load graph first.");
            return;
        }
        if (startPos == null || endPos == null) {
            JOptionPane.showMessageDialog(this, "Both, start and endpoint must be set.");
            return;
        }
        progressThread=new Thread(new progressUpdate(this));
        progressThread.start();
        
  
         repaint();
        // cancel possibly running algorithm

        
        try {
            //calculator = new AlgorithmWorker(initAlgorithm(), this);
          
          
            System.out.println(p.gMethod);
            if(p.gMethod.equals("lao"))
            {
            p.Initialize(this);    
            p.InitializeAllStateListLAO(this);
            //System.out.println("lao*");
            p.LAOstar();    
            //p.ValueIterationForTimeToGoalLAO(200);
            }
                else
                {
                      p.Initialize(this);
                    //System.out.println("VI:");
                    if(Prototype.STAGE=="STORE")
                    {  
                      p.readPartObject(p.allStateList, this);  
                      p.InitializeAllStateList(this);
           //         p.InitializeBestActionGivenStateValue(p.allStateList);
                   
                      p.ValueIterationOnly(400);
                      p.ValueIterationForTimeToGoal(250);
                      p.writeObject(p.allStateList,this);
                    }
                    else
                    {
                        jProgressBar1.setValue(10);
                        jProgressBar1.setString("Generated Heuristic!");
                        jProgressBar1.setStringPainted(true);
                        
                        jProgressBar1.setString("Loading Data...");
                        jProgressBar1.setStringPainted(true);
                        p.readObject(p.allStateList,this);
                       
                        jProgressBar1.setValue(99);

                        //System.out.println("Begin to Value Iteration for time to goal");
                        //p.ValueIterationForTimeToGoal(300);
                       
                    }
                }
            //busyLabel.setBusy(false);
            //p.allStateList.toString();
            userPanel.setVisible(true);
            debugPanel.setVisible(true);
            repaint();
            jProgressBar1.setString("Finished");
            jProgressBar1.setStringPainted(true);
            jProgressBar1.setValue(100);
            DisplaySolution(p.allStateList);
            
            threadUpdate=new ThreadForUpdate(this);
            
            btnNextStep.setEnabled(true);
            btnBegin.setEnabled(true);
            jButton1.setEnabled(false);
            
 //           cancelButton.setEnabled(true);
            //calculator.execute();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            log.log(Level.SEVERE, null, ex);
//            busyLabel.setBusy(false);
        }
    }

    public void DisplaySolution(HashMap<String, StateNode> allStateList) {
                 
        this.btnBegin.setEnabled(true);
        this.btnPause.setEnabled(false);
	      this.btnNextStep.setEnabled(true);
	      this.btnRestart.setEnabled(false);
		stateNode=p.Start;
                System.out.println("Direction: "+stateNode.controlledState.direction);
                System.out.println("Best Action:"+stateNode.bestAction.toString());
                 System.out.println(stateNode.toString());                
                 //System.out.print("State Value:"+stateNode.stateValue);
                
                this.directionPainter.setNode(stateNode.controlledState.node);
                this.directionPainter.setDirection(stateNode.controlledState.direction);
                this.directionPainter.setImage(p.imageVehicle,p.imageRobot);
                this.directionPainter.setActor(stateNode.stabilityState.actor);
                visitedNodes.add(p.Start.controlledState.node);
		 java.text.DecimalFormat   df=new   java.text.DecimalFormat( "#.# "); 
			
	      this.jTextFieldStability.setText(p.Start.stabilityState.stability.toString());
//	      this.jTextFieldDirection.setText(p.Start.controlledState.direction.toString());
              this.jLabelNextActionActor.setText("Next Action : ("+p.Start.bestAction.actor+","+p.Start.bestAction.action+")");
              
              if(stateNode.bestAction.actor==Prototype.Actor.SAU)
                  this.jLabelActorIcon.setIcon(robot_icon);
              else
                  this.jLabelActorIcon.setIcon(people_icon);
              
              if(stateNode.bestAction.msg==Prototype.Message.decrease)
              {
                  this.jLabelMsgIcon.setIcon(minus_icon);
                  this.jRadioButtonRelax.setText("Relax - Decrease Engagement");
                  this.jRadioButtonRelax.setSelected(false);
              }else
                  if(stateNode.bestAction.msg==Prototype.Message.increase)
                  {
                       this.jLabelMsgIcon.setIcon(plus_icon);
                       this.jRadioButtonRelax.setText("Attention - Increase Engagement");
                       this.jRadioButtonRelax.setSelected(true);
                  }
                  else{
                      this.jLabelMsgIcon.setIcon(equal_icon);
                      this.jRadioButtonRelax.setText("Keep Current Engagement");
                  }
      
                if(stateNode.bestAction.action==Prototype.Action.forward)
                {
                    this.jLabelNextAction.setIcon(forward_icon);
                    lastIcon=forward_icon;
                }
              else
                  if(stateNode.bestAction.action==Prototype.Action.reverse)
                  {
                      this.jLabelNextAction.setIcon(reverse_icon);
                      lastIcon=reverse_icon;
                  }
                   else
                      if(stateNode.bestAction.action==Prototype.Action.left)
                      {
                          this.jLabelNextAction.setIcon(left_icon);
                          lastIcon=left_icon;
                      }
                        else
                          if(stateNode.bestAction.action==Prototype.Action.right)
                          {
                              this.jLabelNextAction.setIcon(right_icon);
                              lastIcon=right_icon;
                          }
                              else
                              this.jLabelNextAction.setIcon(lastIcon);
              
                  if(stateNode.driverState.engagement==EngagementLevel.engaged)
                {
                    this.jTextFieldEngagement.setText("Engaged");
                 
                }
                else
                {
                     this.jTextFieldEngagement.setText("Not Engaged");
                }  
                
              
	      this.jTextFieldDifficulty.setText(p.Start.controlledState.difficulty.toString());
	      this.jTextFieldActionExecuting.setText("("+p.Start.stabilityState.actor+","+p.Start.stabilityState.actionBeExecuting+")");
	      this.jTextFieldStability.setText(p.Start.stabilityState.stability.toString());
	      this.jTextFieldLon.setText(""+p.Start.controlledState.node.getLon());
	      this.jTextFieldLat.setText(""+p.Start.controlledState.node.getLat());
              this.jTextFieldErrorCount.setText(""+p.Start.driverState.errorMade);
	      this.jTextFieldTraffic.setText(p.Start.traffic.toString());      

              		        if(stateNode.stabilityState.stability==Stability.stable)
		        {
			        jTextFieldPercentOfSAU.setText(df.format(stateNode.percentOfTimeSAU*100)+"%");
			        jTextFieldTimeToGoal.setText(df.format(stateNode.timeToGoal));
			        //textFieldTimetoTransfer.setText(df.format(stateNode.timeToTransferOfControl));
					temp1=stateNode.timeToGoal;
					temp2=stateNode.timeToTransferOfControl;
					temp3=stateNode.percentOfTimeSAU;
                               
                                        
                                     jLabelSteps.setText(df.format(stateNode.timeToTransferOfControl)+" steps");
                                    
                                    if(stateNode.bestAction.actor==Actor.SAU)
                                    jLabelTimetoControl.setText("Time to manual control");
                                  
                                else
                                {
                                    jLabelTimetoControl.setText("Time to automatic control");
                                    
                                   }         
                                

		        }
		        else
		        {
			        jTextFieldPercentOfSAU.setText(df.format(temp3*100)+"%");
			        jTextFieldTimeToGoal.setText(df.format(temp1));
			             jLabelSteps.setText(df.format(temp2)+" steps");
                                if(stateNode.bestAction.actor==Actor.SAU)
                                {
                                    jLabelTimetoControl.setText("Time to manual control");
                                    
                                }
                                else
                                {
                                    jLabelTimetoControl.setText("Time to automatic control");
                                     
                                } 
		        }
              
	        if(p.Start.isTerminal==true)
	        	this.jTextFieldNextAction.setText("");
	        else
	        	this.jTextFieldNextAction.setText(p.Start.bestAction.toString());
	            
                 
    }

        public void DisplaySolutionStepByStep(HashMap<String, StateNode> allStateList) {
        
		
		 java.text.DecimalFormat   df=new   java.text.DecimalFormat( "#.# "); 
                 //myVisitedNodesPainter.clear();

                 
                 //System.out.print("State Value:"+stateNode.stateValue);

                 
		if (stateNode.isTerminal ==false&&stateNode.bestAction!=null)
                {
                    Random random=new Random();
                    double temp=random.nextDouble();
                    if(stateNode.stabilityState.stability==Stability.unstable||stateNode.driverState.engagement==EngagementLevel.notEngaged||Prototype.ActionSuccessRate>temp||stateNode.controlledState.difficulty==Prototype.Difficulty.free)//the right action
                    {
                        EngagementLevel tempEngage=stateNode.driverState.engagement;
                        Message tempMsg=stateNode.bestAction.msg;
                        stateNode=p.getNextState(stateNode,stateNode.bestAction,0);
                        if(checkEngagement(tempEngage,tempMsg,stateNode.driverState.engagement))
                            this.jTextFieldError.setText("");
                        else
                            this.jTextFieldError.setText("Message has been ignored");
                    }
                    else// take the wrong action
                    {
 
                                ActionNode realAction=new ActionNode();
              			Random m=new Random();
				int k=m.nextInt();
				if(k<0)	k=-k;
				//System.out.println("see the k :"+k);
				k=k%(Prototype.Action.values().length-1);
				
				for(int i=0;i<k;i++)
				{
					if(Prototype.Action.values()[i]==stateNode.bestAction.action)
					{
						k++;
					}
					
				}
				realAction.msg=stateNode.bestAction.msg;
				realAction.action=Prototype.Action.values()[k];
                                realAction.actor=Actor.driver;
                               
                                Prototype.Action suggestedAction=stateNode.bestAction.action;
                                stateNode=p.getNextState(stateNode,realAction,1);
                                this.jTextFieldError.setText("Real Action : "+realAction.action+", Suggested Action: "+suggestedAction);
                              if(stateNode==null||stateNode.driverState.errorMade==Prototype.MaximumErrorNumber)
                             {
                               JOptionPane.showInternalMessageDialog(getContentPane(),
                               "Exceed Maximum Error!");
                               this.btnBegin.setEnabled(false);
                               this.btnPause.setEnabled(false);
	                       this.btnNextStep.setEnabled(false);
	                       this.btnRestart.setEnabled(true);
                               if(threadUpdate.isAlive()==true)
                               {
                                //System.out.println( "Reach terminal, flag = "+flag+", flag change to false.");
                                 this.flag=false;
                                 this.threadUpdate.suspend();
                                }
                                return;
                               }
                     
                    }
                        
                }
               
                System.out.println("Direction: "+stateNode.controlledState.direction);
                if(stateNode.bestAction!=null)
                {
                    System.out.println("Best Action:"+stateNode.bestAction.toString());
                                          
                    this.jLabelNextActionActor.setText("Next Action : ("+stateNode.bestAction.actor+","+stateNode.bestAction.action+")"); 
                    
                       if(stateNode.bestAction.actor==Prototype.Actor.SAU)
                  this.jLabelActorIcon.setIcon(robot_icon);
                       else
                  this.jLabelActorIcon.setIcon(people_icon);
                       
                         if(stateNode.bestAction.msg==Prototype.Message.decrease)
              {
                  this.jLabelMsgIcon.setIcon(minus_icon);
                  this.jRadioButtonRelax.setText("Relax - Decrease Engagement");
                  this.jRadioButtonRelax.setSelected(false);
              }else
                  if(stateNode.bestAction.msg==Prototype.Message.increase)
                  {
                       this.jLabelMsgIcon.setIcon(plus_icon);
                       this.jRadioButtonRelax.setText("Attention - Increase Engagement");
                       this.jRadioButtonRelax.setSelected(true);
                  }
                  else{
                      this.jLabelMsgIcon.setIcon(equal_icon);
                      this.jRadioButtonRelax.setText("Keep Current Engagement");
                  }
                         
               
                if(stateNode.bestAction.action==Prototype.Action.forward)
                {
                    this.jLabelNextAction.setIcon(forward_icon);
                    lastIcon=forward_icon;
                }
              else
                  if(stateNode.bestAction.action==Prototype.Action.reverse)
                  {
                      this.jLabelNextAction.setIcon(reverse_icon);
                      lastIcon=reverse_icon;
                  }
                   else
                      if(stateNode.bestAction.action==Prototype.Action.left)
                      {
                          this.jLabelNextAction.setIcon(left_icon);
                          lastIcon=left_icon;
                      }
                        else
                          if(stateNode.bestAction.action==Prototype.Action.right)
                          {
                              this.jLabelNextAction.setIcon(right_icon);
                              lastIcon=right_icon;
                          }
                              else
                              this.jLabelNextAction.setIcon(lastIcon);
                }
                System.out.println(stateNode.toString());
                
                if(visitedNodes.get(visitedNodes.size()-1).equals(stateNode.controlledState.node)==false)
                {
                    //System.out.println("Here;"+visitedNodes.get(visitedNodes.size()-1).toString());
                    //System.out.println("Compare:"+stateNode.controlledState.node.toString());
                    visitedNodes.add(stateNode.controlledState.node);
     
                    gmPathPainter.setNodes(visitedNodes);
                }
//                 System.out.println(stateNode.toString());
               
               
                this.directionPainter.setNode(stateNode.controlledState.node);
                this.directionPainter.setDirection(stateNode.controlledState.direction);
                this.directionPainter.setActor(stateNode.stabilityState.actor); 
                
	      this.jTextFieldStability.setText(stateNode.stabilityState.stability.toString());
	//      this.jTextFieldDirection.setText(stateNode.controlledState.direction.toString());
              
	      this.jTextFieldDifficulty.setText(stateNode.controlledState.difficulty.toString());
	      this.jTextFieldActionExecuting.setText("("+stateNode.stabilityState.actor+","+stateNode.stabilityState.actionBeExecuting+")");
	      this.jTextFieldStability.setText(stateNode.stabilityState.stability.toString());
	      this.jTextFieldLon.setText(""+stateNode.controlledState.node.getLon());
	      this.jTextFieldLat.setText(""+stateNode.controlledState.node.getLat());
	      this.jTextFieldTraffic.setText(stateNode.traffic.toString());
              this.jTextFieldErrorCount.setText(""+stateNode.driverState.errorMade);
             
                      

                   if(stateNode.stabilityState.stability==Stability.stable)
		        {
			        jTextFieldPercentOfSAU.setText(df.format(stateNode.percentOfTimeSAU*100)+"%");
                                if(stateNode.timeToGoal!=0||stateNode.isTerminal==true)
                                {
                                    jTextFieldTimeToGoal.setText(df.format(stateNode.timeToGoal));
                                    temp1=stateNode.timeToGoal;
                                }
                                else
                                {
                                    jTextFieldTimeToGoal.setText(df.format(temp1));
                                }
			        //textFieldTimetoTransfer.setText(df.format(stateNode.timeToTransferOfControl));
                                if(stateNode.timeToTransferOfControl!=0)
                                {
                                    jLabelSteps.setText(df.format(stateNode.timeToTransferOfControl)+" steps");
                                    temp2=stateNode.timeToTransferOfControl;
                                }
                                else
                                {
                                    jLabelSteps.setText(df.format(temp2)+" steps");
                                }
                                if(stateNode.bestAction!=null&&stateNode.bestAction.actor!=stateNode.stabilityState.actor)
                                {
                                    if(stateNode.bestAction.actor==Actor.SAU)
                                            jLabelTimetoControl.setText("Time to manual control");
                                    else
                                            jLabelTimetoControl.setText("Time to automatic control");
                                    
                                } 
					
					
					temp3=stateNode.percentOfTimeSAU;

		        }
		        else
		        {
			        jTextFieldPercentOfSAU.setText(df.format(temp3*100)+"%");
                                if(temp1>2)
			           jTextFieldTimeToGoal.setText(df.format(temp1-1));
                                else
                                   jTextFieldTimeToGoal.setText(df.format(temp1-0.5)); 
			        //textFieldTimetoTransfer.setText(df.format(temp2));
                                if(temp2>2)
                                   jLabelSteps.setText(df.format(temp2-1)+" steps");
                                else
                                   jLabelSteps.setText(df.format(temp2-0.5)+" steps");
                                if(stateNode.bestAction!=null&&stateNode.bestAction.actor!=stateNode.stabilityState.actor)
                                {
                                    if(stateNode.bestAction.actor==Actor.SAU)
                                            jLabelTimetoControl.setText("Time to manual control");
                                    else
                                            jLabelTimetoControl.setText("Time to automatic control");
                                    
                                }  
		        }
              
	        if(stateNode.isTerminal==true)
                {
                    this.jTextFieldNextAction.setText("");
                     this.jLabelNextAction.setIcon(pause_icon);
                    
                }
	        else
	        	this.jTextFieldNextAction.setText(stateNode.bestAction.toString());
	        //this.jTextFieldEngagement.setText(stateNode.driverState.toString());
                if(stateNode.driverState.engagement==EngagementLevel.engaged)
                {
                    this.jTextFieldEngagement.setText("Engaged");
                 
                }
                else
                {
                     this.jTextFieldEngagement.setText("Not Engaged");
                }
                 jXMapKit.repaint();
                
                 
                if(stateNode.isTerminal==true)
                 {
               this.btnBegin.setEnabled(false);
               this.btnPause.setEnabled(false);
	       this.btnNextStep.setEnabled(false);
	       this.btnRestart.setEnabled(true);
               if(threadUpdate.isAlive()==true)
               {
                   //System.out.println( "Reach terminal, flag = "+flag+", flag change to false.");
                   this.flag=false;
                    this.threadUpdate.suspend();
               }
             
                      //System.out.println( "Reach terminal, flag = "+flag);
                      //System.out.println(threadUpdate.isAlive());
                 }
    }

    private boolean checkEngagement(EngagementLevel tempEngage, Message tempMsg,EngagementLevel newEngage) {
        if(tempMsg==Message.nop)
            return tempEngage==newEngage;
        if(tempMsg==Message.increase)
            return newEngage==EngagementLevel.engaged;
        if(tempMsg==Message.decrease)
            return newEngage==EngagementLevel.notEngaged;
        
        return true;
    }


    
    private enum POSITIONS {

        START, END
    };
    private final Logger log = Logger.getLogger(Demo.class.getName());
    // -- Properties
    private String lru_dir;
    private boolean autoLoadGraph;
    // --
    private Result result;
    private Statistics statistics;
    public OSMNode startPos;
    public OSMNode endPos;
    private MouseListener srcDestChooser = null;
    public OSMGraph<OSMNode<OSMLink>, OSMLink<OSMNode>> graph;
    private LoadGraphWorker loadGraphWorker = null;
    private WaypointPainter<JXMapViewer> startEndPainter = new WaypointPainter<JXMapViewer>();
    private GraphPainter graphPainter = new GraphPainter();
    private PathPainter pathPainter=new PathPainter();
    private GMPathPainter gmPathPainter = new GMPathPainter();
    private NodePainter visitedNodesPainter = new NodePainter();
    private DirectionPainter directionPainter=new DirectionPainter();
    private ArrayList<OSMNode>targetNode=new ArrayList<OSMNode>();
    private ArrayList<OSMNode> visitedNodes=new ArrayList<OSMNode>();
    private NodePainter targetNodePainter = new NodePainter();
    private AlgorithmWorker calculator = null;
    private LoadGraphAction loadAction = new LoadGraphAction();
    private Map<Integer, ResultEntry> results = new HashMap<Integer, ResultEntry>();
    private StatisticsFrame statisticsFrame = null;
    // Algorithms:
    // name -> Class
    private final Hashtable<String, Class<? extends Algorithm>> algorithmMap =
            new Hashtable<String, Class<? extends Algorithm>>();
    // map result type -> layout name
    private final Map<Class, String> resultToLayoutName; // cardlayout
    private final Map<Class, SimplexControl> resultToSimplexControl;
    public Prototype p;
    private ThreadForUpdate threadUpdate;
    
    public Demo(Prototype p) throws IOException {
        log.info("start");
        this.p=p;
        initComponents();
        userPanel.setVisible(false);
        debugPanel.setVisible(false);

        // add methods to algorithms dropdown
 //       algorithmComboBox.removeAllItems();
        ((AbstractTileFactory) jXMapKit.getMainMap().getTileFactory()).setThreadPoolSize(10);
        jXMapKit.setAddressLocation(new GeoPosition(47.76168565517294, 11.559205055236816));
        refreshPainters();
        setLocationRelativeTo(null);

        // fill the resultTo*- maps
        Map<Class, String> mapToLayoutName = new Hashtable<Class, String>();
        mapToLayoutName.put(Simplex1Result.class, "simplex1d");
        mapToLayoutName.put(Simplex2Result.class, "simplex2d");
        mapToLayoutName.put(Simplex3Result.class, "simplex3d");
        resultToLayoutName = Collections.unmodifiableMap(mapToLayoutName);

        // fill the resultTo*- maps
        Map<Class, SimplexControl> mapToControl = new Hashtable<Class, SimplexControl>();

        resultToSimplexControl = Collections.unmodifiableMap(mapToControl);



        // save and restore LRU-directories
        SaveHook s = new SaveHook(this);
        s.restore();
        Runtime.getRuntime().addShutdownHook(s);

        // load graph
        if (autoLoadGraph && lru_dir != null) {
            try {
                File lru = new File(lru_dir);
                File nodes = new File(lru, "nodes.txt");
                File ways = new File(lru, "ways.txt");

                log.fine("starting worker");
                loadGraphWorker = new LoadGraphWorker(nodes, ways);
                loadGraphWorker.init();
                loadGraphWorker.addPropertyChangeListener(loadAction);
                loadGraphWorker.execute();
            } catch (Exception e) {
                log.log(Level.SEVERE, "", e);
            }
        }

    }

    /**
     * called as soon as the graph is loaded
     */
    private void graphLoaded() {
        String statusText =
                String.format("%d links, %d nodes, %d nodes with degree > 2.",
                graph.getLinkCount(),graph.getNodes().size() + graph.getNodesWithoutLinksCount(), graph.getNodes().size());
       // statusbarLabel.setText(statusText);
        graphPainter.setGraph(graph);
        Collection<OSMNode<OSMLink>> nodes = graph.getNodes();
        if (nodes.size() > 0) {
            jXMapKit.setCenterPosition(nodes.iterator().next().getGeoPosition());
        }
        jXMapKit.repaint();

//        setStartPos(graph.getNode(283588275));
//        setEndPos(graph.getNode(287610217));
        setStartPos(graph.getNode(443813279));
        setEndPos(graph.getNode(410818));
    }

    private void showStatisticsFrame() {
        if (statisticsFrame == null || !statisticsFrame.isVisible()) {
            statisticsFrame = new StatisticsFrame();
            statisticsFrame.setLocationRelativeTo(null);
        }
        statisticsFrame.setVisible(true);
        reloadStatisticsData();
    }

    private void reloadStatisticsData() {
        if (results.size() <= 0 || statisticsFrame == null || !statisticsFrame.isVisible()) {
            return;
        }

        statisticsFrame.clear();
        statisticsFrame.setData(statistics);
    }

    private void reloadAlgorithmComboBox() {
        algorithmBoxModel.removeAllElements();
        List<String> keyList = new ArrayList<String>();
        keyList.addAll(algorithmMap.keySet());
        Collections.sort(keyList);
        for (String name : keyList) {
            algorithmBoxModel.addElement(name);
        }
//        algorithmComboBox.setSelectedIndex(0);
    }

    public void addAlgorithm(Class<? extends Algorithm> aClass) {
        try {
            Algorithm instance = aClass.newInstance();
            String name = instance.getName();
            algorithmMap.put(name, aClass);

            // update GUI
            reloadAlgorithmComboBox();
            repaint();
        } catch (InstantiationException ex) {
            log.log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            log.log(Level.SEVERE, null, ex);
        }
    }

    private void chooseStartLocation(POSITIONS p) {
        JXMapViewer map = jXMapKit.getMainMap();
        if (srcDestChooser != null) {
            map.removeMouseListener(srcDestChooser);
        }

        srcDestChooser = new MousePointChooser(p);
        map.addMouseListener(srcDestChooser);
    }
    
        private void chooseEndLocation(POSITIONS p) {
        JXMapViewer map = jXMapKit.getMainMap();
        if (srcDestChooser != null) {
            map.removeMouseListener(srcDestChooser);
        }

        srcDestChooser = new MousePointChooser(p);
        map.addMouseListener(srcDestChooser);
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        attributeListModel = new javax.swing.DefaultListModel();
        algorithmBoxModel = new javax.swing.DefaultComboBoxModel();
        resultTableModel = new NoEditTableModel();
        javax.swing.JSplitPane horizontalSplit = new javax.swing.JSplitPane();
        javax.swing.JPanel rightPanel = new javax.swing.JPanel();
        jXMapKit = new org.jdesktop.swingx.JXMapKit();
        javax.swing.JPanel statusBar = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        inputPanel = new javax.swing.JPanel();
        javax.swing.JLabel sourceLabel = new javax.swing.JLabel();
        javax.swing.JLabel dstLabel = new javax.swing.JLabel();
        srcTextField = new javax.swing.JTextField();
        dstTextField = new javax.swing.JTextField();
        javax.swing.JButton setSrcButton = new javax.swing.JButton();
        javax.swing.JButton setDstButton = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();
        jProgressBar1 = new javax.swing.JProgressBar();
        userPanel = new javax.swing.JPanel();
        btnRestart = new javax.swing.JButton();
        jTextFieldTraffic = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        btnBegin = new javax.swing.JButton();
        jLabel5 = new javax.swing.JLabel();
        btnPause = new javax.swing.JButton();
        btnNextStep = new javax.swing.JButton();
        jTextFieldActionExecuting = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jTextFieldTimeToGoal = new javax.swing.JTextField();
        jLabelSteps = new javax.swing.JLabel();
        jLabelTimetoControl = new javax.swing.JLabel();
        jRadioButtonRelax = new javax.swing.JRadioButton();
        jLabelNextActionActor = new javax.swing.JLabel();
        jLabelMsg = new javax.swing.JLabel();
        jLabelMsgIcon = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jLabelActorIcon = new javax.swing.JLabel();
        jLabelNextAction = new javax.swing.JLabel();
        jLabelErrorCount = new javax.swing.JLabel();
        jTextFieldErrorCount = new javax.swing.JTextField();
        debugPanel = new javax.swing.JPanel();
        jTextFieldLon = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jTextFieldLat = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jTextFieldDifficulty = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        jTextFieldStability = new javax.swing.JTextField();
        textFieldPercentOfSAU = new javax.swing.JLabel();
        jTextFieldPercentOfSAU = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        jTextFieldNextAction = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        jTextFieldEngagement = new javax.swing.JTextField();
        jLabel10 = new javax.swing.JLabel();
        jTextFieldError = new javax.swing.JTextField();
        javax.swing.JMenuBar menuBar = new javax.swing.JMenuBar();
        javax.swing.JMenu fileMenu = new javax.swing.JMenu();
        javax.swing.JMenuItem loadGraphItem = new javax.swing.JMenuItem();
        autoloadMenuItem = new javax.swing.JCheckBoxMenuItem();
        paintGraphMenuItem = new javax.swing.JCheckBoxMenuItem();
        javax.swing.JMenuItem exitItem = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("GM");
        setMinimumSize(new java.awt.Dimension(500, 400));
        setPreferredSize(new java.awt.Dimension(1029, 650));

        horizontalSplit.setDividerLocation(385);
        horizontalSplit.setDividerSize(7);
        horizontalSplit.setMinimumSize(new java.awt.Dimension(50, 334));

        rightPanel.setPreferredSize(new java.awt.Dimension(570, 620));
        rightPanel.setLayout(new java.awt.BorderLayout());

        jXMapKit.setAddressLocationShown(false);
        jXMapKit.setDefaultProvider(org.jdesktop.swingx.JXMapKit.DefaultProviders.OpenStreetMaps);
        jXMapKit.setZoom(1);
        rightPanel.add(jXMapKit, java.awt.BorderLayout.CENTER);

        statusBar.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        statusBar.setLayout(new java.awt.GridBagLayout());
        rightPanel.add(statusBar, java.awt.BorderLayout.PAGE_END);

        horizontalSplit.setRightComponent(rightPanel);

        jPanel2.setMinimumSize(new java.awt.Dimension(400, 350));
        jPanel2.setPreferredSize(new java.awt.Dimension(450, 640));
        jPanel2.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        inputPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Determine Route"));
        inputPanel.setAlignmentX(0.3F);
        inputPanel.setAlignmentY(0.3F);
        inputPanel.setPreferredSize(new java.awt.Dimension(384, 206));
        inputPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        sourceLabel.setText("Source:");
        inputPanel.add(sourceLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(27, 31, -1, -1));

        dstLabel.setText("Destination:");
        inputPanel.add(dstLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(16, 68, -1, -1));

        srcTextField.setEditable(false);
        srcTextField.setText("Click the right button to set a Source");
        srcTextField.setToolTipText("Click the right button to set a Source");
        inputPanel.add(srcTextField, new org.netbeans.lib.awtextra.AbsoluteConstraints(92, 28, 211, -1));

        dstTextField.setEditable(false);
        dstTextField.setText("Click the right button to set a Destination");
        dstTextField.setToolTipText("Click the right button to set a Destination");
        inputPanel.add(dstTextField, new org.netbeans.lib.awtextra.AbsoluteConstraints(92, 60, 211, -1));

        setSrcButton.setText("Set");
        setSrcButton.setToolTipText("set start via mouse click");
        setSrcButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                setSrcButtonActionPerformed(evt);
            }
        });
        inputPanel.add(setSrcButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(309, 27, -1, -1));

        setDstButton.setText("Set");
        setDstButton.setToolTipText("set destination  via mouse click");
        setDstButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                setDstButtonActionPerformed(evt);
            }
        });
        inputPanel.add(setDstButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(309, 59, -1, -1));

        jButton1.setText("Calculate");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        inputPanel.add(jButton1, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 90, -1, 33));
        inputPanel.add(jProgressBar1, new org.netbeans.lib.awtextra.AbsoluteConstraints(220, 100, -1, -1));

        jPanel2.add(inputPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, -1, 130));

        userPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Route Details"));
        userPanel.setAlignmentX(0.3F);
        userPanel.setAlignmentY(0.3F);
        userPanel.setName("");
        userPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        btnRestart.setText("Restart");
        btnRestart.setEnabled(false);
        btnRestart.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRestartActionPerformed(evt);
            }
        });
        userPanel.add(btnRestart, new org.netbeans.lib.awtextra.AbsoluteConstraints(300, 30, -1, -1));
        userPanel.add(jTextFieldTraffic, new org.netbeans.lib.awtextra.AbsoluteConstraints(110, 70, 64, -1));

        jLabel8.setText("Action Executing:");
        userPanel.add(jLabel8, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 100, -1, -1));

        btnBegin.setText("Begin");
        btnBegin.setEnabled(false);
        btnBegin.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBeginActionPerformed(evt);
            }
        });
        userPanel.add(btnBegin, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 30, 72, -1));

        jLabel5.setText("Traffic:");
        userPanel.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 70, -1, -1));

        btnPause.setEnabled(false);
        btnPause.setLabel("Pause");
        btnPause.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPauseActionPerformed(evt);
            }
        });
        userPanel.add(btnPause, new org.netbeans.lib.awtextra.AbsoluteConstraints(110, 30, 69, -1));

        btnNextStep.setText("Next Step");
        btnNextStep.setEnabled(false);
        btnNextStep.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNextStepActionPerformed(evt);
            }
        });
        userPanel.add(btnNextStep, new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 30, 85, -1));
        userPanel.add(jTextFieldActionExecuting, new org.netbeans.lib.awtextra.AbsoluteConstraints(171, 100, 200, -1));

        jLabel3.setText("Time to Goal:");
        userPanel.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(190, 70, -1, -1));
        userPanel.add(jTextFieldTimeToGoal, new org.netbeans.lib.awtextra.AbsoluteConstraints(290, 70, 80, -1));

        jLabelSteps.setText("0 steps");
        userPanel.add(jLabelSteps, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 250, 80, 30));

        jLabelTimetoControl.setText("Time to manual control");
        userPanel.add(jLabelTimetoControl, new org.netbeans.lib.awtextra.AbsoluteConstraints(130, 250, 180, 25));

        jRadioButtonRelax.setLabel("Relax - Low Engagement");
        userPanel.add(jRadioButtonRelax, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 220, -1, 33));

        jLabelNextActionActor.setText("Next Action:");
        userPanel.add(jLabelNextActionActor, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 130, -1, -1));

        jLabelMsg.setText("Msg:");
        userPanel.add(jLabelMsg, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 220, 52, 33));

        jLabelMsgIcon.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Equal_icon.png"))); // NOI18N
        userPanel.add(jLabelMsgIcon, new org.netbeans.lib.awtextra.AbsoluteConstraints(330, 220, -1, -1));

        jPanel1.setBackground(new java.awt.Color(204, 255, 204));
        jPanel1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabelActorIcon.setIcon(new javax.swing.ImageIcon(getClass().getResource("/People_icon.png"))); // NOI18N
        jPanel1.add(jLabelActorIcon, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 10, 40, 40));

        jLabelNextAction.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Forward_icon.png"))); // NOI18N
        jPanel1.add(jLabelNextAction, new org.netbeans.lib.awtextra.AbsoluteConstraints(92, 10, 40, 40));

        userPanel.add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 150, 150, 60));

        jLabelErrorCount.setText("Error Count:");
        userPanel.add(jLabelErrorCount, new org.netbeans.lib.awtextra.AbsoluteConstraints(250, 130, -1, -1));
        userPanel.add(jTextFieldErrorCount, new org.netbeans.lib.awtextra.AbsoluteConstraints(320, 130, 50, -1));

        jPanel2.add(userPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 136, 390, 280));

        debugPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Debugger"));
        debugPanel.setAlignmentX(0.3F);
        debugPanel.setAlignmentY(0.3F);
        debugPanel.setPreferredSize(new java.awt.Dimension(380, 200));
        debugPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());
        debugPanel.add(jTextFieldLon, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 20, 100, -1));

        jLabel2.setText("Lon:");
        debugPanel.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 20, -1, -1));

        jTextFieldLat.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldLatActionPerformed(evt);
            }
        });
        debugPanel.add(jTextFieldLat, new org.netbeans.lib.awtextra.AbsoluteConstraints(270, 20, 110, 20));

        jLabel1.setText("Lat:");
        debugPanel.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(210, 20, 30, -1));

        jLabel4.setText("Difficulty:");
        debugPanel.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 50, -1, -1));
        debugPanel.add(jTextFieldDifficulty, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 50, 100, 20));

        jLabel7.setText("Stability:");
        debugPanel.add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 50, 70, -1));
        debugPanel.add(jTextFieldStability, new org.netbeans.lib.awtextra.AbsoluteConstraints(270, 50, 110, 20));

        textFieldPercentOfSAU.setText("SAU control time:");
        debugPanel.add(textFieldPercentOfSAU, new org.netbeans.lib.awtextra.AbsoluteConstraints(180, 80, 120, -1));
        debugPanel.add(jTextFieldPercentOfSAU, new org.netbeans.lib.awtextra.AbsoluteConstraints(300, 80, 80, -1));

        jLabel9.setText("Next Action:");
        debugPanel.add(jLabel9, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 110, -1, 22));
        debugPanel.add(jTextFieldNextAction, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 110, 260, 20));

        jLabel6.setText("Engag. Level:");
        debugPanel.add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 80, -1, -1));
        debugPanel.add(jTextFieldEngagement, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 80, 70, 20));

        jLabel10.setText("Error Detected:");
        debugPanel.add(jLabel10, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 140, -1, 20));
        debugPanel.add(jTextFieldError, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 140, 260, -1));

        jPanel2.add(debugPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 420, 390, 220));

        horizontalSplit.setLeftComponent(jPanel2);

        getContentPane().add(horizontalSplit, java.awt.BorderLayout.CENTER);

        fileMenu.setMnemonic('f');
        fileMenu.setText("File");

        loadGraphItem.addActionListener(loadAction);
        loadGraphItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.CTRL_MASK));
        loadGraphItem.setText("Load Graph");
        fileMenu.add(loadGraphItem);

        autoloadMenuItem.setSelected(true);
        autoloadMenuItem.setText("autoload graph next time");
        autoloadMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                autoloadMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(autoloadMenuItem);

        paintGraphMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_P, java.awt.event.InputEvent.CTRL_MASK));
        paintGraphMenuItem.setSelected(true);
        paintGraphMenuItem.setText("paint Graph");
        paintGraphMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                paintGraphMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(paintGraphMenuItem);

        exitItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F4, java.awt.event.InputEvent.ALT_MASK));
        exitItem.setMnemonic('x');
        exitItem.setText("Exit");
        exitItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exitItemActionPerformed(evt);
            }
        });
        fileMenu.add(exitItem);

        menuBar.add(fileMenu);

        setJMenuBar(menuBar);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void exitItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exitItemActionPerformed
        System.exit(0);
    }//GEN-LAST:event_exitItemActionPerformed

    private void setSrcButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_setSrcButtonActionPerformed
        chooseStartLocation(POSITIONS.START);
    }//GEN-LAST:event_setSrcButtonActionPerformed

    private void setDstButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_setDstButtonActionPerformed
        chooseEndLocation(POSITIONS.END);
    }//GEN-LAST:event_setDstButtonActionPerformed

    private void paintGraphMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_paintGraphMenuItemActionPerformed
        refreshPainters();
    }//GEN-LAST:event_paintGraphMenuItemActionPerformed

    private void autoloadMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_autoloadMenuItemActionPerformed
        autoLoadGraph = autoloadMenuItem.isSelected();
    }//GEN-LAST:event_autoloadMenuItemActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        
        GM();
        

    }//GEN-LAST:event_jButton1ActionPerformed

    private void btnBeginActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBeginActionPerformed
       			   // System.out.println("In Begin, flag = "+flag);
                            if (flag==true)
                                threadUpdate.start();
			    else
			    	threadUpdate.resume();
			    btnPause.setEnabled(true);
			    btnBegin.setEnabled(false);
			    btnRestart.setEnabled(false);
                            btnNextStep.setEnabled(false);
    }//GEN-LAST:event_btnBeginActionPerformed

    private void btnPauseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPauseActionPerformed
                                threadUpdate.suspend();
				flag=false;
				btnPause.setEnabled(false);
				btnBegin.setEnabled(true);
				btnNextStep.setEnabled(true);
    }//GEN-LAST:event_btnPauseActionPerformed

    private void btnRestartActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRestartActionPerformed
        pathPainter.setPath(Collections.EMPTY_LIST);
        directionPainter.clear();
        gmPathPainter.clear();
        visitedNodes.clear();
        visitedNodesPainter.clear();
        this.btnNextStep.setEnabled(true);
        this.btnBegin.setEnabled(true);
	this.btnRestart.setEnabled(false);
        stateNode=p.Start;
        DisplaySolution(p.allStateList);
        repaint();
        
    }//GEN-LAST:event_btnRestartActionPerformed

    private void btnNextStepActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNextStepActionPerformed
        DisplaySolutionStepByStep(p.allStateList);
        //btnBegin.setEnabled(true);
    }//GEN-LAST:event_btnNextStepActionPerformed

    private void jTextFieldLatActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldLatActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextFieldLatActionPerformed

    private void openSeekWindow(POSITIONS pos) {
        final SeekPositionFrame spf = new SeekPositionFrame(this.graph);
        spf.setVisible(true);
        spf.addWindowListener(new WindowPointChooser(pos));
    }

    private void refreshPainters() {
        JXMapViewer map = jXMapKit.getMainMap();
        List<Painter> list = new ArrayList<Painter>();
        if (paintGraphMenuItem.isSelected()) {
            list.add(graphPainter);
        }
        list.add(visitedNodesPainter);
        list.add(targetNodePainter);
        list.add(pathPainter);
        list.add(gmPathPainter);
        list.add(directionPainter);
        list.add(startEndPainter);
        map.setOverlayPainter(new CompoundPainter(list.toArray(new Painter[]{})));
    }

    private void highlightResult() {
//        int[] rowIDs = resultTable.getSelectedRows();
//        if (rowIDs.length == 0 || results.size() == 0 || resultTable.getRowCount() == 0) {
//            return;
//        }
//
//        List<ResultEntry> items = new ArrayList<ResultEntry>();
//        for (int rowID : rowIDs) {
//            rowID = resultTable.convertRowIndexToModel(rowID);
//            Integer id = (Integer) resultTableModel.getValueAt(rowID, 0);
//            if (results.containsKey(id)) {
//                items.add(results.get(id));
//            }
//        }
//
//        // okay, this call IS ugly
//        Class resultClass = results.values().iterator().next().getResult().getClass();
//        SimplexControl simplexControl = resultToSimplexControl.get(resultClass);
//        assert simplexControl != null;
//        List<PointSource> list = new ArrayList<PointSource>();
//        if (items.size() == 0) {
//            pathPainter.clear();
//        } else {
//            List<Path<?, ? extends OSMNode>> pathList = new ArrayList<Path<?, ? extends OSMNode>>();
//            for (ResultEntry resultEntry : items) {
//                pathList.add(resultEntry.getPath());
//                list.add(resultEntry);
//                log.info("highlighted: " + resultEntry.getPath().toString());
//            }
//            pathPainter.setPath(pathList);
//        }
//        simplexControl.setHighlight(list);
//        jXMapKit.repaint();
    }

//    private void startAndRunAlgorithm() {
//        resultTableModel.setRowCount(0);
////        simplexControl1D.setPoints(Collections.EMPTY_LIST);
////        simplexControl2D.setPoints(Collections.EMPTY_LIST);
////        simplexControl3D.setPoints(Collections.EMPTY_LIST);
//        pathPainter.setPath(Collections.EMPTY_LIST);
//        visitedNodesPainter.setNodes(Collections.EMPTY_LIST);
//        repaint();
//
//        if (graph == null) {
//            JOptionPane.showMessageDialog(this, "Please load graph first.");
//            return;
//        }
//        if (startPos == null || endPos == null) {
//            JOptionPane.showMessageDialog(this, "Both, start and endpoint must be set.");
//            return;
//        }
//
//        // cancel possibly running algorithm
//        if (calculator != null && !calculator.isDone()) {
//            resultTableModel.setRowCount(0);
//            calculator.cancel(true);
//        }
//
//  //      busyLabel.setBusy(true);
//        try {
// //           calculator = new AlgorithmWorker(initAlgorithm(), this);
// //           cancelButton.setEnabled(true);
// //           calculator.execute();
//        } catch (Exception ex) {
//            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
//            log.log(Level.SEVERE, null, ex);
////            busyLabel.setBusy(false);
//        }
//    }

    private void processResult(Algorithm algorithm) {
        result = algorithm.getResult();
        statistics = algorithm.getStatistics();
        if (result == null) {
            return;
        }

        // Set the correct card
//        CardLayout layout = (CardLayout) simplexContainer.getLayout();
//        layout.show(simplexContainer, resultToLayoutName.get(result.getClass()));

        // reset all simplex controls
//        simplexControl1D.setPoints(Collections.EMPTY_LIST);
//        simplexControl2D.setPoints(Collections.EMPTY_LIST);
//        simplexControl3D.setPoints(Collections.EMPTY_LIST);

        // clear list
        resultTableModel.setRowCount(0);
        results.clear();

        Map<Path, double[]> resultsMap = result.getResults();
        // Set column names
        Vector colNames = new Vector(result.getUnits());
        colNames.add(0, "id");
        resultTableModel.setColumnIdentifiers(colNames);

        {// set columns sortable
            TableRowSorter sorter = new TableRowSorter(resultTableModel);
//            resultTable.setRowSorter(sorter);
            for (int i = 0; i < resultTableModel.getColumnCount(); i++) {
                sorter.setComparator(i, new ResultTableColumnSorter());
            }
        }

        double[] ranges = findMaxPerColumn(resultsMap.values());
        List<PointSource> pointSourceList = new ArrayList<PointSource>();
        int i = 1;
        for (Map.Entry<Path, double[]> entry : resultsMap.entrySet()) {
            double[] absolute = entry.getValue().clone();
            double[] relative = entry.getValue().clone();
            Arrays2.div(relative, ranges);
            Arrays2.replaceNaN(relative, 0); // ranges MAY contain zeros

            int resultDimensionality = algorithm.getSelectedAttributes().size();
            ResultEntry resEntry = new ResultEntry(result, entry.getKey(), relative, absolute, i++, resultDimensionality);
            results.put(resEntry.getId(), resEntry);
            resultTableModel.addRow(resEntry.getVector());
            pointSourceList.add(resEntry);
        }
        SimplexControl simplexControl = resultToSimplexControl.get(result.getClass());
        simplexControl.setAttributNames(algorithm.getSelectedAttributes());
        simplexControl.setPoints(pointSourceList);

        if (resultsMap.size() > 0) {
 //           resultTable.getSelectionModel().clearSelection();
 //           resultTable.getSelectionModel().setSelectionInterval(0, 0);
        }
        if (statisticsFrame != null && statisticsFrame.isVisible()) {
            showStatisticsFrame();
        }
      //  showHideVisitedNodes();
    }

//    private void showHideVisitedNodes() {
//        visitedNodesPainter.clear();
//        if (statistics != null && visitedNodesItem.isSelected()) {
//            visitedNodesPainter.setNodes(statistics.getVisitedNodes());
//        }
//        jXMapKit.repaint();
//    }

    private double[] findMaxPerColumn(Collection<double[]> entries) {
        double[] maxima = null;
        for (double[] e : entries) {
            if (maxima == null) {
                maxima = e.clone();
            }
            for (int i = 0; i < e.length; i++) {
                maxima[i] = Math.max(maxima[i], e[i]);
            }
        }
        return maxima;
    }

    private void setStartPos(OSMNode node) {
        if (node == null) {
            return;
        }
        GeoPosition pos = node.getGeoPosition();
        srcTextField.setText(pos.getLatitude() + ", " + pos.getLongitude());
        startPos = node;
        updatePosPainter();
    }

    private void setEndPos(OSMNode node) {
        if (node == null) {
            return;
        }
        GeoPosition pos = node.getGeoPosition();
        dstTextField.setText(pos.getLatitude() + ", " + pos.getLongitude());
        endPos = node;
        updatePosPainter();
        targetNodePainter.clear();
        targetNode.clear();;
        targetNode.add(node);
        targetNodePainter.setNodes(targetNode);
        
    }

    private void updatePosPainter() {
        Set<Waypoint> waypoints = new HashSet<Waypoint>();
        if (startPos != null) {
            waypoints.add(new Waypoint(startPos.getGeoPosition()));
        }
        if (endPos != null) {
            waypoints.add(new Waypoint(endPos.getGeoPosition()));
        }
        startEndPainter.setWaypoints(waypoints);        
        jXMapKit.repaint();
    }

//    private Algorithm initAlgorithm() throws InstantiationException,
//            IllegalAccessException {
////        List<String> attributes = new ArrayList<String>();
////        for (Object o : attributeList.getSelectedValues()) {
////            attributes.add(o.toString());
////        }
////        String algoName = algorithmComboBox.getSelectedItem().toString();
////
////        Algorithm algorithm = algorithmMap.get(algoName).newInstance();
//        algorithm.setGraph(graph);
////        algorithm.setAttributes(attributes);
//        algorithm.setStartNode(startPos);
//        algorithm.setEndNode(endPos);
//
//        return algorithm;
//    }

    void setLruDir(String lru) {
        this.lru_dir = lru;
    }

    String getLruDir() {
        return lru_dir;
    }

    boolean isAutoLoad() {
        return autoLoadGraph;
    }

    void setAutoLoad(boolean autoLoad) {
        this.autoLoadGraph = autoLoad;
        autoloadMenuItem.setSelected(autoLoad);
    }

    /**
     * Opens the filechooser for the load graph action.
     * Upon File selection, a new SwingWorker ist started to load the files.
     *
     * Also updates the apinter as soon as the graph is loaded
     */
    class LoadGraphAction implements ActionListener, PropertyChangeListener {

        @Override
        public void actionPerformed(final ActionEvent e) {
            if (loadGraphWorker != null) {
                log.fine("canceling active worker");
                loadGraphWorker.cancel(true);
            }

            // opening the FileChooser MAY take some ugly time which freezes the GUI :-/
            // if this is an issue, but this into an invokeLater()
            JFileChooser chooser = new JFileChooser(lru_dir);
            chooser.setMultiSelectionEnabled(true);

            // Display only directories, nodes.txt and ways.txt in the dropdown
            chooser.setFileFilter(new FileFilter() {

                @Override
                public boolean accept(File f) {
                    return f.isDirectory()
                            || f.getName().equalsIgnoreCase("nodes.txt")
                            || f.getName().equalsIgnoreCase("ways.txt");
                }

                @Override
                public String getDescription() {
                    return "ways.txt & nodes.txt";
                }
            });

            // If the user pressed "okay", try to load the files
            if (JFileChooser.APPROVE_OPTION == chooser.showOpenDialog(getContentPane())) {
                // setting lru dir
                log.fine("saving least recently used dir: " + lru_dir);
                lru_dir = chooser.getSelectedFiles()[0].getParent();
                if (chooser.getSelectedFiles().length != 2) {
                    JOptionPane.showInternalMessageDialog(getContentPane(),
                            "You must load 2 files at once:\n- nodes.txt\n- ways.");
                    return;
                }

                try {
                    log.fine("starting worker");
                    loadGraphWorker = new LoadGraphWorker(chooser.getSelectedFiles());
                    loadGraphWorker.init();
                    loadGraphWorker.addPropertyChangeListener(this);
                    loadGraphWorker.execute();
                } catch (Throwable t) {
                    log.log(Level.SEVERE, "couldn't init graph loader:", t.getMessage());
                    JOptionPane.showInternalMessageDialog(getContentPane(), t.getMessage());
                }
            }
        }

        /**
         * Method that is called as soon as the graphloader task has finished
         * @param evt
         */
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getNewValue().equals(SwingWorker.StateValue.DONE)) {
                try {
                    if (!loadGraphWorker.isCancelled()) {
                        graph = loadGraphWorker.get();
                        graphLoaded();
                    }
                } catch (InterruptedException ex) {
                    log.log(Level.SEVERE, null, ex);
                } catch (ExecutionException ex) {
                    log.log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    /**
     * Mouseadapter that handles the selection of start and endpoints on the main map
     */
    class MousePointChooser extends MouseAdapter {

        private final POSITIONS targetPos;

        private MousePointChooser(POSITIONS p) {
            this.targetPos = p;
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            GeoPosition pos = jXMapKit.getMainMap().convertPointToGeoPosition(e.getPoint());
            OSMNode node = OSMUtils.getNearestNode(pos, graph);

            if (targetPos == POSITIONS.START) {
                setStartPos(node);
            } else if (targetPos == POSITIONS.END) {
            
                //setEndPos(node);
                setEndPos(graph.getNode(42440015));
                //setEndPos(graph.getNode(-73.981441,40.763584));
            }

            // we're done! remove the listener and paint the waypoints on the map
            jXMapKit.getMainMap().removeMouseListener(this);
        }
    }

    /**
     * WindowAdapter that handles the selection of start and endpoints via full
     * text search in an external window
     */
    class WindowPointChooser extends WindowAdapter {

        private final POSITIONS targetPos;

        private WindowPointChooser(POSITIONS p) {
            this.targetPos = p;
        }

        @Override
        public void windowClosed(WindowEvent e) {
            SeekPositionFrame spf = (SeekPositionFrame) e.getWindow();
            spf.removeWindowListener(this);
            OSMNode node = spf.getNode();

            if (targetPos == POSITIONS.START) {
                setStartPos(node);
            } else if (targetPos == POSITIONS.END) {
                setEndPos(node);
            }
        }
    }

    /**
     * swinworker that starts the algorithm and waits for tis termination.
     */
    class AlgorithmWorker extends SwingWorker<Void, Void> {

        private final Logger log = Logger.getLogger(AlgorithmWorker.class.getName());
        private final Component parent;
        private final Algorithm algorithm;

        AlgorithmWorker(Algorithm alg, Component parent) {
            this.algorithm = alg;
            this.parent = parent;
        }

        @Override
        protected Void doInBackground() throws Exception {
            log.info("start algorithm");
            if (algorithm != null) {
                algorithm.run();
            }
            return null;
        }

        @Override
        protected void done() {
            try {
 //               busyLabel.setBusy(false);
 //               cancelButton.setEnabled(false);
                if (!isCancelled()) {
                    get();
                    processResult(algorithm);
                }

                calculator = null;
            } catch (InterruptedException ex) {
                log.log(Level.FINE, null, ex);
            } catch (ExecutionException t) {
                log.log(Level.SEVERE, "Algorithm terminated by uncaught exception:", t);
                JOptionPane.showMessageDialog(parent, "Exception in Calculator: " + t.getMessage());
            }
        }
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.DefaultComboBoxModel algorithmBoxModel;
    private javax.swing.DefaultListModel attributeListModel;
    private javax.swing.JCheckBoxMenuItem autoloadMenuItem;
    private javax.swing.JButton btnBegin;
    private javax.swing.JButton btnNextStep;
    private javax.swing.JButton btnPause;
    private javax.swing.JButton btnRestart;
    private javax.swing.JPanel debugPanel;
    private javax.swing.JTextField dstTextField;
    private javax.swing.JPanel inputPanel;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JLabel jLabelActorIcon;
    private javax.swing.JLabel jLabelErrorCount;
    private javax.swing.JLabel jLabelMsg;
    private javax.swing.JLabel jLabelMsgIcon;
    private javax.swing.JLabel jLabelNextAction;
    private javax.swing.JLabel jLabelNextActionActor;
    private javax.swing.JLabel jLabelSteps;
    private javax.swing.JLabel jLabelTimetoControl;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    public javax.swing.JProgressBar jProgressBar1;
    private javax.swing.JRadioButton jRadioButtonRelax;
    private javax.swing.JTextField jTextFieldActionExecuting;
    private javax.swing.JTextField jTextFieldDifficulty;
    private javax.swing.JTextField jTextFieldEngagement;
    private javax.swing.JTextField jTextFieldError;
    private javax.swing.JTextField jTextFieldErrorCount;
    private javax.swing.JTextField jTextFieldLat;
    private javax.swing.JTextField jTextFieldLon;
    private javax.swing.JTextField jTextFieldNextAction;
    private javax.swing.JTextField jTextFieldPercentOfSAU;
    private javax.swing.JTextField jTextFieldStability;
    private javax.swing.JTextField jTextFieldTimeToGoal;
    private javax.swing.JTextField jTextFieldTraffic;
    private org.jdesktop.swingx.JXMapKit jXMapKit;
    private javax.swing.JCheckBoxMenuItem paintGraphMenuItem;
    private javax.swing.table.DefaultTableModel resultTableModel;
    private javax.swing.JTextField srcTextField;
    private javax.swing.JLabel textFieldPercentOfSAU;
    private javax.swing.JPanel userPanel;
    // End of variables declaration//GEN-END:variables

    public static void main(String args[]) throws Exception {
        LogManager.getLogManager().readConfiguration(Demo.class.getResourceAsStream("./logging.properties"));
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        final Prototype p=new Prototype();
        java.awt.EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {
                try {
                    Demo d = new Demo(p);
                    d.addAlgorithm(OSMDijkstra.class);
                    d.addAlgorithm(OSMSkyline.class);
                    d.addAlgorithm(DemoAlgorithm1D.class);
                    d.addAlgorithm(DemoAlgorithm2D.class);
                    d.addAlgorithm(DemoAlgorithm3D.class);
                    d.setVisible(true);
                } catch (IOException ex) {
                    Logger.getLogger(Demo.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
    }
}
class ResultTableColumnSorter implements Comparator {

    @Override
    public int compare(Object o1, Object o2) {
        double d1 = Double.parseDouble(o1.toString().split(" ")[0]);
        double d2 = Double.parseDouble(o2.toString().split(" ")[0]);
        return Double.compare(d1, d2);
    }
}

class ResultEntry implements PointSource {

    private final Result result;
    private final double[] costRelative;
    private final double[] costAbsolute;
    private final int id;
    private final Path path;
    private final int dimensionality;

    ResultEntry(Result result, Path path, double[] costRelative, double[] costAbsolute, int id, int dimensionality) {
        assert Arrays2.findNaN(costRelative) < 0 : "NaN found in costRelative: " + Arrays2.join(costRelative, "|");
        assert Arrays2.findNaN(costAbsolute) < 0 : "NaN found in costAbsolute: " + Arrays2.join(costAbsolute, "|");
        this.result = result;
        this.costRelative = costRelative;
        this.costAbsolute = costAbsolute;
        this.id = id;
        this.path = path;
        this.dimensionality = dimensionality;
    }

    public Vector getVector() {
        Vector vec = new Vector();
        vec.add(id);
        for (int i = 0; i < costRelative.length; i++) {
            String unit = "";
            if (result.getUnits().size() > i) {
                unit = result.getUnits().get(i);
            }
            vec.add(String.format(Locale.US, "%.4f %s", costAbsolute[i], unit));
        }
        return vec;
    }

    public double[] getCost() {
        return costRelative;
    }

    public int getId() {
        return id;
    }

    public Path getPath() {
        return path;
    }

    public Result getResult() {
        return result;
    }

    @Override
    public Point2D getLocation() {
        if (dimensionality == 1) {
            return new Point2D.Double(costRelative[0], 0);
        } else if (dimensionality == 2) {
            return new Point2D.Double(costRelative[0], costRelative[1]);
        } else {
            throw new UnsupportedOperationException("Unsupported cost size = " + dimensionality);
        }
    }

    @Override
    public double[] getCoordinates() {
        return costRelative;
    }

    @Override
    public String toString() {
        String s = id + ": ";
        for (int i = 0; i < costRelative.length; i++) {
            String unit = "";
            if (result.getUnits().size() > i) {
                unit = result.getUnits().get(i);
            }
            s += String.format("%.4f %s | ", costAbsolute[i], unit);
        }
        return s;
    }
}

/**
 * Link the clicks on the simplexcontrols' paintpanels with the selection of the
 * result list
 *
 * @author graf
 */
class SimplexHighlighter extends MouseAdapter {

    private final JTable target;

    SimplexHighlighter(JTable resultList) {
        this.target = resultList;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        SimplexControl control = (SimplexControl) e.getSource();
        PointPanel panel = control.getPointPanel();
        // convert to click on the panel
        e = SwingUtilities.convertMouseEvent((Component) control, e, control.getPointPanel());

        List<PointSource> pointSources2D = panel.getEpsilonPoints(e.getPoint(), panel.getPointSize() / 2);
        panel.setHighlight(pointSources2D);
        final List<PointSource> pointSources3D = control.getSourceFor(pointSources2D);

        if (pointSources3D.size() > 0 && pointSources3D.get(0) instanceof ResultEntry) {
            int[] resultIDs = new int[pointSources3D.size()];
            for (int i = 0; i < pointSources3D.size(); i++) {
                resultIDs[i] = ((ResultEntry) pointSources3D.get(i)).getId();
            }

            int maxRow = target.getModel().getRowCount();
            target.getSelectionModel().clearSelection();
            for (int rowIndex = 0; rowIndex < maxRow; rowIndex++) {
                int id = (Integer) target.getValueAt(rowIndex, 0);
                if (Arrays2.indexOf(resultIDs, id) >= 0) {
                    target.getSelectionModel().addSelectionInterval(rowIndex, rowIndex);
                }
            }
        }
    }
}

/**
 * TableModel that does not allow editing of cells
 * @author Franz
 */
class NoEditTableModel extends DefaultTableModel {

    @Override
    public boolean isCellEditable(int row, int column) {
        return false;
    }
}
