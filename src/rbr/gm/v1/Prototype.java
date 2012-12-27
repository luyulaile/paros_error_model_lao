/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package rbr.gm.v1;

import experimentalcode.franz.*;
import experimentalcode.franz.osm.OSMGraph;
import experimentalcode.franz.osm.OSMLink;
import experimentalcode.franz.osm.OSMNode;
import java.io.*;
import java.util.ArrayList;
import java.util.*;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.UIManager;

/**
 *
 * @author yilu
 */
public class Prototype implements Serializable{
    private static final long serialVersionUID = 1L;
    private double p_success_blocked_sparse;
    private double p_success_blocked_heavy;
    private double p_success_difficult_sparse;
    private double p_success_difficult_heavy;
    private double p_success_free_sparse;
    private double p_success_free_heavy;
    private double transition_sparse_heavy,transition_heavy_sparse;
    private double p_free_sparse,p_free_heavy;
    private double p_driver_difficult_sparse,p_driver_difficult_heavy,p_driver_blocked_sparse,p_driver_blocked_heavy;
    private double p_system_difficult_sparse,p_system_difficult_heavy,p_system_blocked_sparse,p_system_blocked_heavy;
    private double factor_difficult_to_turn_for_SAU=1.0;
        
    public static double gDiscount;
    
    public static double COST_SAU_MSG_DECREASE_CONTINUED=1.0; 
    public static double COST_SAU_MSG_DECREASE_NON_CONTINUED=1.5;
    public static double COST_SAU_MSG_NOP_CONTINUED=0.8;
    public static double COST_SAU_MSG_NOP_NON_CONTINUED=1.3;

    public static double NOTENGAGED_COST_SAU_MSG_NOP_CONTINUED=0.70;
    public static double NOTENGAGED_COST_SAU_MSG_NOP_NON_CONTINUED=1.2;
    public static double NOTENGAGED_COST_SAU_MSG_INCREASE_CONTINUED=0.9;
    public static double NOTENGAGED_COST_SAU_MSG_INCREASE_NON_CONTINUED=1.4;
	
    public static double COST_DRIVER_MSG_NOP_CONTINUED;
    public static double COST_DRIVER_MSG_NOP_NON_CONTINUED;
    
    public static String gInputMapFileName="map.txt";
    public String gInputConfigureFileName = "";
        

    public static String gBackupMethod="combine";
    public static String gMethod="lao";
    public static String STAGE="STORE";
    public static double gEpsilon = 0.000000;
    public static double gStartTime= 0.000000;
    public static double gWeight=1.0;

    public static int MaximumErrorNumber=4;    
    public static double ActionSuccessRate=0.70;// the probability that have the right action	
	
    public static double MsgSuccessRate=0.80;// the probability that msg change without no error
    
    public HashMap<String, StateNode> allStateList;
    public HashMap<String,String>stableState=new  HashMap<String,String>();
    public HashMap<String,String>nonStableState=new HashMap<String,String>();;
    private int countValueIteration;
    private int NumExpandedStates;
    private int NumExpandedStatesIter;
    private int NumAncestorStatesIter;
    private int Iteration;
    private Vector<StateNode> ExpandedStateList;
    private int NumSolutionStates;
    private double Residual;
    private int ExitFlag;
    public ImageIcon imageVehicle;
    public ImageIcon imageRobot;
    private boolean isActionSuccess;
    public HashMap<String,GenerateHeuristic.Edge> bestList;
    public static HashMap<String,Direction> defaultForward=new HashMap<String,Direction>();
    public static HashMap<String,Direction> nextLeft=new HashMap<String,Direction>();
    public static HashMap<String,Direction> nextRight=new HashMap<String,Direction>();
/**
 *return the Direction of the node given the original direction (link)
 * @param node the target node of the link
 * @param link the original link
 * @return 
 */
    public static Direction getDefaultForwardLink(OSMNode node,OSMLink link) {
        if(defaultForward.containsKey(node.name+"|"+link.toString()))
            return defaultForward.get(node.name+"|"+link.toString());
            //System.out.println("why not optimize it in getDefaultForward");
            OSMNode linkSource=(OSMNode) (link.getTarget()==node?link.getSource():link.getTarget());
        for (Iterator it = node.getOutLinks().iterator(); it.hasNext();) {
            OSMLink temp = (OSMLink) it.next();
            OSMNode tempTarget=(OSMNode) (temp.getTarget()==node?temp.getSource():temp.getTarget());
  
            if(temp.getId()==link.getId()&&tempTarget!=linkSource)
            {
                int result=node.getOutLinks().indexOf(temp);
               // System.out.print("result:"+ result);
               // System.out.println(" "+ Direction.values()[result]);
                defaultForward.put(node.name+"|"+link.toString(), Direction.values()[result]);
                return Direction.values()[result];
            }
        }
        
         int result=node.getInLinks().indexOf(link);
             defaultForward.put(node.name+"|"+link.toString(), Direction.values()[result+Direction.values().length/2]);
             return Direction.values()[result+Direction.values().length/2];
                                       
    }
/**
 * given a node, find the next link on the left of link temp, link temp can be an inward link, or an outward link. 
 * @param node
 * @param temp
 * @return 
 */
    public static Direction getNextLeftLink(OSMNode node, OSMLink temp) {
       if(nextLeft.containsKey(node.name+"|"+temp.toString()))
            return nextLeft.get(node.name+"|"+temp.toString()); 
       //System.out.println("why not optimize it in getNextLeft");
       if(node.getOutLinks().isEmpty()) 
           return null;
        ArrayList<OSMLink> linkList=new ArrayList<OSMLink>();
       for (Iterator it = node.getOutLinks().iterator(); it.hasNext();) {
            OSMLink link = (OSMLink) it.next();
            if(isLinkOnTheLeft(link,temp,node)==1)
                linkList.add(link);
       }
       if(linkList.isEmpty()==true)
       return null;
       else
           if(linkList.size()==1)
           {
              OSMLink link=linkList.get(0);
              int index=node.getOutLinks().indexOf(link);
              nextLeft.put(node.name+"|"+temp.toString(), Direction.values()[index]);
               return Direction.values()[index];
             }
       else
           {
               if(isLinkOnTheLeft(linkList.get(0),linkList.get(1),node)==1)
               {
                     OSMLink link=linkList.get(1);
              int index=node.getOutLinks().indexOf(link);
               nextLeft.put(node.name+"|"+temp.toString(), Direction.values()[index]);
               return Direction.values()[index];
               }
               else
               {
                   OSMLink link=linkList.get(0);
              int index=node.getOutLinks().indexOf(link);
               nextLeft.put(node.name+"|"+temp.toString(), Direction.values()[index]);
               return Direction.values()[index];
               }
                   
           }
           //System.out.println("error in getNextLeftLink");
    }

    public static Direction getNextRightLink(OSMNode node, OSMLink temp) {
              if(node.getOutLinks().isEmpty()==true) 
           return null;
             if(nextRight.containsKey(node.name+"|"+temp.toString()))
            return nextRight.get(node.name+"|"+temp.toString());  
            //System.out.println("why not optimize it in getNextRight");      
        ArrayList<OSMLink> linkList=new ArrayList<OSMLink>();
       for (Iterator it = node.getOutLinks().iterator(); it.hasNext();) {
            OSMLink link = (OSMLink) it.next();
            if(isLinkOnTheLeft(link,temp,node)==-1)
                linkList.add(link);
       }
       if(linkList.isEmpty()==true)
       return null;
       else
           if(linkList.size()==1)
           {
              OSMLink link=linkList.get(0);
              int index=node.getOutLinks().indexOf(link);
               nextRight.put(node.name+"|"+temp.toString(),Direction.values()[index]);
               return Direction.values()[index];
             }
       else
           {
               if(isLinkOnTheLeft(linkList.get(0),linkList.get(1),node)==-1)
               {
                     OSMLink link=linkList.get(1);
              int index=node.getOutLinks().indexOf(link);
               nextRight.put(node.name+"|"+temp.toString(),Direction.values()[index]);
               return Direction.values()[index];
               }
               else
               {
                   OSMLink link=linkList.get(0);
              int index=node.getOutLinks().indexOf(link);
               nextRight.put(node.name+"|"+temp.toString(),Direction.values()[index]);
               return Direction.values()[index];
               }
                   
           }
    }

    public static int isLinkOnTheLeft(OSMLink link, OSMLink temp,OSMNode node) {
        //latitude is X. longitude is Y ???
       OSMNode node1=node;
       OSMNode node2=(OSMNode)(node==(OSMNode) temp.getTarget()?temp.getSource():temp.getTarget());
       OSMNode node3=(OSMNode)(node==(OSMNode) link.getTarget()?link.getSource():link.getTarget());
       
       double result=(node1.getLat()-node3.getLat())*(node2.getLon()-node3.getLon())-(node1.getLon()-node3.getLon())*(node2.getLat()-node3.getLat());
       if(result>0)
           return -1;
       else if(result<0)
           return 1;
       else
           return 0;
    }

    public ControlledState getNewControlledState(StateNode stateNode, ActionNode actionNode) {
        ControlledState temp=new ControlledState();
		temp.node=stateNode.controlledState.node;
		temp.difficulty=stateNode.controlledState.difficulty;
		temp.direction=stateNode.controlledState.direction;
		if(stateNode.stabilityState.stability==Stability.unstable)
		{
			return temp;
		}
		else
		{
			Random rd1 = new Random();
			double number=rd1.nextFloat();
			if(stateNode.controlledState.difficulty==Difficulty.blocked&&stateNode.traffic==UncontrolledState.sparse)
			{
				if(number<=p_success_blocked_sparse)//success
				{
					isActionSuccess=true;
					return calculateNewControlledState(stateNode.controlledState,actionNode.action);
				}
				else//failure
				{
					isActionSuccess=false;
					return temp; 
				}
			}
			else
				if(stateNode.controlledState.difficulty==Difficulty.blocked&&stateNode.traffic==UncontrolledState.heavy)
				{
					if(number<=p_success_blocked_heavy)//success
					{
						isActionSuccess=true;
						return calculateNewControlledState(stateNode.controlledState,actionNode.action);
					}
					else//failure
						{
						 isActionSuccess=false;
						 return temp;
						}
				}
				else
					if(stateNode.controlledState.difficulty==Difficulty.difficult&&stateNode.traffic==UncontrolledState.sparse)
					{
						if(number<=p_success_difficult_sparse)//success
						{
							isActionSuccess=true;
							return calculateNewControlledState(stateNode.controlledState,actionNode.action);
						}
						else//failure
							{
							isActionSuccess=false;
							 return temp;
							}
					}
					else
						if(stateNode.controlledState.difficulty==Difficulty.difficult&&stateNode.traffic==UncontrolledState.heavy)
						{
							if(number<=p_success_difficult_heavy)//success
							{
								isActionSuccess=true;
								return calculateNewControlledState(stateNode.controlledState,actionNode.action);
							}
							else//failure
							{
								isActionSuccess=false;
								return temp;
							}
						}
						else
							if(stateNode.controlledState.difficulty==Difficulty.free&&stateNode.traffic==UncontrolledState.sparse)
							{
								if(number<=p_success_free_sparse)//success
								{
									isActionSuccess=true;
									return calculateNewControlledState(stateNode.controlledState,actionNode.action);
								}
								else//failure
								{
									isActionSuccess=false;
									return temp;
								}
							}
							else
								if(stateNode.controlledState.difficulty==Difficulty.free&&stateNode.traffic==UncontrolledState.heavy)
								{
									if(number<=p_success_free_heavy)//success
									{
										isActionSuccess=true;
										return calculateNewControlledState(stateNode.controlledState,actionNode.action);
									}
									else//failure
									{
										isActionSuccess=false;
										return temp;
									}
								}
						
						
		}
		return null;
    }

        public StateNode getNextState(StateNode stateNode, ActionNode actionNode,int errorMade) {
                Prototype.StateNode nextState=new Prototype.StateNode();
		nextState.controlledState=getNewControlledState(stateNode,actionNode);//Not Deterministic
		nextState.stabilityState=getNewStability(stateNode,actionNode);//Not Deterministic
		nextState.traffic=getNextTraffic(stateNode.traffic);//Not Deterministic
		nextState.driverState=getNextDriverState(stateNode,actionNode,errorMade);//Not Deterministic
//		System.out.println("In getNextState: "+nextState.toString());
//		System.out.println("In getNextState 2: "+allStateList.get(nextState.toString()));
		return allStateList.get(nextState.toString());
    }
        
    public StabilityState getNewStability(StateNode stateNode, ActionNode actionNode) {
        		StabilityState temp=new StabilityState();
		
		if(stateNode.stabilityState.stability==Stability.unstable)
		{
			double factor=0.0;
			if((stateNode.stabilityState.actionBeExecuting==Action.left||stateNode.stabilityState.actionBeExecuting==Action.right)&&stateNode.stabilityState.actionBeExecutingActor==Actor.SAU)
				{
				factor=factor_difficult_to_turn_for_SAU;
				//System.out.println("factor");
				}
			else
				factor=1.0;
				
			temp.actionBeExecuting=stateNode.stabilityState.actionBeExecuting;
			temp.actionBeExecutingActor=stateNode.stabilityState.actionBeExecutingActor;
			temp.stability=Stability.unstable;
			temp.actor=stateNode.stabilityState.actor;
			Random rd1 = new Random();
			double number=rd1.nextFloat();
			
			if(stateNode.controlledState.difficulty==Difficulty.free)
			{
				switch(stateNode.traffic)
				{
				case sparse:
					if(number<=(p_free_sparse/factor))//success
					{
						temp.stability=Stability.stable;
					}
					break;
				case heavy:	
					if(number<=(p_free_heavy/factor))//success
					{
						temp.stability=Stability.stable;
					}
					break;
				}
				return temp;
			}
			// for the situation when controlledState.difficulty==blocked
				if(stateNode.controlledState.difficulty==Difficulty.blocked)
				{
					if(stateNode.stabilityState.actor==Actor.driver)
					{
						switch(stateNode.traffic)
						{
						case sparse:
							if(number<=(p_driver_blocked_sparse/factor))//success
							{
								temp.stability=Stability.stable;
							}
							break;
						case heavy:	
							if(number<=(p_driver_blocked_heavy/factor))//success
							{
								temp.stability=Stability.stable;
							}
							break;
						}
						return temp;
					}
					if(stateNode.stabilityState.actor==Actor.SAU)
					{
						switch(stateNode.traffic)
						{
						case sparse:
							if(number<=(p_system_blocked_sparse/factor))//success
							{
								temp.stability=Stability.stable;
							}
							break;
						case heavy:	
							if(number<=(p_system_blocked_heavy/factor))//success
							{
								temp.stability=Stability.stable;
							}
							break;
						}
					}
					return temp;
				}
				
				// for the situation when controlledState.difficulty==difficult
				if(stateNode.controlledState.difficulty==Difficulty.difficult)
				{
					if(stateNode.stabilityState.actor==Actor.driver)
					{
						switch(stateNode.traffic)
						{
						case sparse:
							if(number<=p_driver_difficult_sparse)//success
							{
								temp.stability=Stability.stable;
							}
							break;
						case heavy:	
							if(number<=p_driver_difficult_heavy)//success
							{
								temp.stability=Stability.stable;
							}
							break;
						}
						return temp;
					}
					if(stateNode.stabilityState.actor==Actor.SAU)
					{
						switch(stateNode.traffic)
						{
						case sparse:
							if(number<=(p_system_difficult_sparse/factor))//success
							{
								temp.stability=Stability.stable;
							}
							break;
						case heavy:	
							if(number<=(p_system_difficult_heavy/factor))//success
							{
								temp.stability=Stability.stable;
							}
							break;
						}
						return temp;
					}
				}
				return temp;
		}
		else//when the current stability is stable
		{
			temp.actionBeExecuting=actionNode.action;
			temp.actionBeExecutingActor=actionNode.actor;
			temp.actor=actionNode.actor;
			temp.stability=Stability.unstable;
			return temp;
		}
    }

    public UncontrolledState getNextTraffic(UncontrolledState traffic) {
        Random rd1 = new Random();
		double number=rd1.nextFloat();
		if(traffic==UncontrolledState.heavy)
		{
			if(number<transition_heavy_sparse)
			    return UncontrolledState.sparse;
			else
				return UncontrolledState.heavy;		
		}
		else//sparse
		{
			if(number<transition_sparse_heavy)
				return UncontrolledState.heavy;
			else
				return UncontrolledState.sparse;		

		}
    }

    private DriverState getNextDriverState(StateNode stateNode,
			ActionNode actionNode,int i) {
	 double p;
     EngagementLevel engagement=null;
     DriverState driverState=new DriverState();
     driverState.errorMade=stateNode.driverState.errorMade+i;
	 Random r=new Random();
	      		  driverState.error=Error.middle;                    	  
                     
       					  if(actionNode.msg==Message.increase)//
					  {
							if(stateNode.driverState.engagement==EngagementLevel.notEngaged)
							{
								double temp=r.nextDouble();
								engagement=EngagementLevel.engaged;
								if(driverState.errorMade>=MaximumErrorNumber||temp<=MsgSuccessRate)
								{
								driverState.engagement=engagement;
								}
								else
								{
									driverState.engagement=EngagementLevel.notEngaged;
                                                                             driverState.errorMade++;
								}
								
							}
					  }
					  else if(actionNode.msg==Message.nop)
					  {
							if(stateNode.driverState.engagement==EngagementLevel.notEngaged)
							{
								double temp=r.nextDouble();
								engagement=EngagementLevel.notEngaged;
								if(driverState.errorMade>=MaximumErrorNumber||temp<=MsgSuccessRate)
								{
								driverState.engagement=engagement;
								}
								else
								{
									driverState.engagement=EngagementLevel.engaged;
                                                                         driverState.errorMade++;
								}
								
							 }								
							else// engaged
							{
								double temp=r.nextDouble();
								engagement=EngagementLevel.engaged;
								if(driverState.errorMade>=MaximumErrorNumber||temp<=MsgSuccessRate)
								{
								driverState.engagement=engagement;
								}
								else
								{
									driverState.engagement=EngagementLevel.notEngaged;
                                                                         driverState.errorMade++;
								}
							}
						  
					  }
					  else// Msg==decrease
					  {
							if(stateNode.driverState.engagement==EngagementLevel.engaged)
							{
								double temp=r.nextDouble();
								engagement=EngagementLevel.notEngaged;
								if(driverState.errorMade>=MaximumErrorNumber||temp<=MsgSuccessRate)
								{
								driverState.engagement=engagement;
								}
								else
								{
									driverState.engagement=EngagementLevel.engaged;
                                                                         driverState.errorMade++;
								}
																	
							}
						  
					  }	
                                           return driverState;
	}
    
        private DriverState getNextDriverState(StateNode stateNode,
			ActionNode actionNode,boolean error) {
     EngagementLevel engagement=null;
     DriverState driverState=new DriverState();
	 Random r=new Random();
	      		  driverState.error=Error.middle;                    	  
                     
       					  if(actionNode.msg==Message.increase)//
					  {
							if(stateNode.driverState.engagement==EngagementLevel.notEngaged)
							{
								double temp=r.nextDouble();
								engagement=EngagementLevel.engaged;
								if(!error||temp<=MsgSuccessRate)
								{
								driverState.engagement=engagement;
								}
								else
								{
									driverState.engagement=EngagementLevel.notEngaged;
								}
								
							}
					  }
					  else if(actionNode.msg==Message.nop)
					  {
							if(stateNode.driverState.engagement==EngagementLevel.notEngaged)
							{
								double temp=r.nextDouble();
								engagement=EngagementLevel.notEngaged;
								if(!error||temp<=MsgSuccessRate)
								{
								driverState.engagement=engagement;
								}
								else
								{
									driverState.engagement=EngagementLevel.engaged;
								}
								
							 }								
							else// engaged
							{
								double temp=r.nextDouble();
								engagement=EngagementLevel.engaged;
								if(!error||temp<=MsgSuccessRate)
								{
								driverState.engagement=engagement;
								}
								else
								{
									driverState.engagement=EngagementLevel.notEngaged; 
								}
							}
						  
					  }
					  else// Msg==decrease
					  {
							if(stateNode.driverState.engagement==EngagementLevel.engaged)
							{
								double temp=r.nextDouble();
								engagement=EngagementLevel.notEngaged;
								if(!error||temp<=MsgSuccessRate)
								{
								driverState.engagement=engagement;
								}
								else
								{
									driverState.engagement=EngagementLevel.engaged;
								}
																	
							}
						  
					  }
					  
					  return driverState;
        }




    public static Object getReversedLink(OSMNode node, OSMLink temp) {
        if(node.getOutLinks().isEmpty()==true) 
           return null;
       
       for (Iterator it = node.getOutLinks().iterator(); it.hasNext();) {
            OSMLink link = (OSMLink) it.next();
            if(isLinkOnTheLeft(link,temp,node)==0)
            {
                int index=node.getOutLinks().indexOf(link);
               return Direction.values()[index];             
            }
       }
       return null;
    }

    private double BackupForTimeUntilNextTransferOfControl(StateNode s) {
		if(s.isTerminal==true)
		{
			s.timeToTransferOfControl=0.0;
			return 0.0;
		}
		else
		{
			double result1=0.0,result2=0.0,result3=0.0,result4=0.0;
			double oldValue=s.timeToTransferOfControl;
			StateNode result=s.getNextStableState(false);
			
			
		  	  StateNode temp0=new StateNode();
				  temp0.controlledState=result.controlledState;
				  temp0.driverState=result.driverState;
				  temp0.stabilityState=result.stabilityState;
				  temp0.traffic=s.traffic;
				  temp0=allStateList.get(temp0.toString());
				  if(temp0.isTerminal==true||s.bestAction.actor==temp0.bestAction.actor)
						result1=temp0.timeToTransferOfControl;		  	  
		  	  
		  	  StateNode temp1=new StateNode();
				  temp1.controlledState=result.controlledState;
				  temp1.driverState=result.driverState;
				  temp1.stabilityState=result.stabilityState;
				  temp1.traffic=(s.traffic==UncontrolledState.sparse?UncontrolledState.heavy:UncontrolledState.sparse);
				  temp1=allStateList.get(temp1.toString());
				  if(temp1.isTerminal==true||s.bestAction.actor==temp1.bestAction.actor)
						result2=temp1.timeToTransferOfControl;
					
				  StateNode stateNode=s.getNextStableState(true);
				  StateNode temp2=new StateNode();
				  temp2.controlledState=stateNode.controlledState;
				  temp2.driverState=stateNode.driverState;
				  temp2.stabilityState=stateNode.stabilityState;
				  temp2.traffic=s.traffic;
				  temp2=allStateList.get(temp2.toString());  
				  if(temp2.isTerminal==true||s.bestAction.actor==temp2.bestAction.actor)
						result3=temp2.timeToTransferOfControl;	  
				  
				  
				  StateNode temp3=new StateNode();
				  temp3.controlledState=stateNode.controlledState;
				  temp3.driverState=stateNode.driverState;
				  temp3.stabilityState=stateNode.stabilityState;
				  temp3.traffic=temp1.traffic;
				  temp3=allStateList.get(temp3.toString());
				  if(temp3.isTerminal==true||s.bestAction.actor==temp3.bestAction.actor)
						result4=temp3.timeToTransferOfControl;	
				  
				  double P[]=new double[4];
				  P=getParamterForFourSituation(s);
				  
				  s.timeToTransferOfControl=(s.timeToNextStability+P[0]*result1+P[1]*result2+P[2]*result3+P[3]*result4);
				  //System.out.println(s.timeToTransferOfControl);
				  return s.timeToTransferOfControl-oldValue;
		}
		
		
    }

	private double BackupForPercentOfTimeSAU(StateNode s) {
		if(s.isTerminal==true) return 0.0; 
		double oldValue=s.percentOfTimeSAU;
		
		StateNode result=s.getNextStableState(true);
		
  	  StateNode temp0=new StateNode();
		  temp0.controlledState=result.controlledState;
		  temp0.driverState=result.driverState;
		  temp0.stabilityState=result.stabilityState;
		  temp0.traffic=s.traffic;
		  temp0=allStateList.get(temp0.toString());
  	  
  	  
  	  StateNode temp1=new StateNode();
		  temp1.controlledState=result.controlledState;
		  temp1.driverState=result.driverState;
		  temp1.stabilityState=result.stabilityState;
		  temp1.traffic=(s.traffic==UncontrolledState.sparse?UncontrolledState.heavy:UncontrolledState.sparse);
		  temp1=allStateList.get(temp1.toString());

		  StateNode stateNode=s.getNextStableState(true);
		  StateNode temp2=new StateNode();
		  temp2.controlledState=stateNode.controlledState;
		  temp2.driverState=stateNode.driverState;
		  temp2.stabilityState=stateNode.stabilityState;
		  temp2.traffic=s.traffic;
		  temp2=allStateList.get(temp2.toString());  
				  
		  StateNode temp3=new StateNode();
		  temp3.controlledState=stateNode.controlledState;
		  temp3.driverState=stateNode.driverState;
		  temp3.stabilityState=stateNode.stabilityState;
		  temp3.traffic=temp1.traffic;
		  temp3=allStateList.get(temp3.toString());  
		  
		  double P[]=new double[4];
		  P=getParamterForFourSituation(s);
		
		if(s.bestAction.actor==Actor.driver)
		{
			double totalSAUTime=P[0]*result.percentOfTimeSAU*result.timeToGoal+P[1]*(temp1.percentOfTimeSAU)*(temp1.timeToGoal)+P[2]*(temp2.timeToGoal)*(temp2.percentOfTimeSAU)+P[3]*(temp3.timeToGoal)*(temp3.percentOfTimeSAU);
			s.percentOfTimeSAU=totalSAUTime/s.timeToGoal;

		}
		else //best Action is taken by SAU
		{
			double totalSAUTime=s.timeToNextStability+P[0]*result.percentOfTimeSAU*result.timeToGoal+P[1]*(temp1.percentOfTimeSAU)*(temp1.timeToGoal)+P[2]*(temp2.timeToGoal)*(temp2.percentOfTimeSAU)+P[3]*(temp3.timeToGoal)*(temp3.percentOfTimeSAU);
			s.percentOfTimeSAU=totalSAUTime/s.timeToGoal;
		}
		  return s.timeToGoal-oldValue;
	}
    public void readPartObject(HashMap<String, StateNode> allStateList,Demo demo) {
         try {  
              
             	FileInputStream inStream = new FileInputStream("defaultForward.txt");  
	            ObjectInputStream objectInputStream = new ObjectInputStream(inStream);  
	            defaultForward=(HashMap<java.lang.String,rbr.gm.v1.Prototype.Direction>)objectInputStream.readObject();  
	            inStream.close();  
	            
                    FileInputStream InStream1 = new FileInputStream("nextLeft.txt");  
	            ObjectInputStream objectInputStream1 = new ObjectInputStream(InStream1);  
	            nextLeft=(HashMap<java.lang.String,rbr.gm.v1.Prototype.Direction>)objectInputStream1.readObject();  
	            InStream1.close();  
                    
                    FileInputStream InStream2 = new FileInputStream("nextRight.txt");  
	            ObjectInputStream objectInputStream2 = new ObjectInputStream(InStream2);
	            nextRight=(HashMap<java.lang.String,rbr.gm.v1.Prototype.Direction>)objectInputStream2.readObject();  
	            InStream2.close();
                    
//                    FileInputStream InStream3 = new FileInputStream("newControlledStateHashMap.txt");  
//	            ObjectInputStream objectInputStream3 = new ObjectInputStream(InStream3);
//	            newControlledStateHashMap=(HashMap<String,Prototype.ControlledState>)objectInputStream3.readObject();                    
//                   
//	            InStream3.close();
                    FileInputStream InStream4 = new FileInputStream("stableState.txt");  
	            ObjectInputStream objectInputStream4 = new ObjectInputStream(InStream4);
	            stableState=(HashMap<java.lang.String,String>)objectInputStream4.readObject();  
	            InStream4.close();
                    
                    FileInputStream InStream5 = new FileInputStream("nonstableState.txt");  
	            ObjectInputStream objectInputStream5 = new ObjectInputStream(InStream5);
	            nonStableState=(HashMap<java.lang.String,String>)objectInputStream5.readObject();  
	            InStream5.close();

         }
                 catch (Exception e) {  
	            e.printStackTrace();  
	        }          
    }    
    
    public void readObject(HashMap<String, StateNode> allStateList,Demo demo) {
        
        try {  
                
                 FileInputStream InStream4 = new FileInputStream(gInputConfigureFileName+"allStateList.txt");  
	            ObjectInputStream objectInputStream4 = new ObjectInputStream(InStream4);
	            allStateList=(HashMap<String,Prototype.StateNode>)objectInputStream4.readObject();
                    Iterator<Entry<String, StateNode>> iter = allStateList.entrySet().iterator(); 
		    while (iter.hasNext()) {
		    Map.Entry entry = iter.next();     
                    StateNode stateNode=   ((Entry<String,StateNode>) entry).getValue();
                    stateNode.controlledState.node=demo.graph.getNode(stateNode.controlledState.node.name);
                    }
                    
                    
                    
                Start.stabilityState=new StabilityState(Stability.stable,Action.forward,Actor.SAU,Actor.SAU);
                Start=allStateList.get(Start.toString());
                  this.allStateList=allStateList;
	            //System.out.println("successful"); 
         }
                 catch (Exception e) {  
	            e.printStackTrace();  
	        }          
    }    
        

    public void writeObject(HashMap<String, StateNode> allStateList,Demo demo) {
         try {  
               
             	FileOutputStream outStream = new FileOutputStream("defaultForward.txt");  
	            ObjectOutputStream objectOutputStream = new ObjectOutputStream(outStream);  
	            objectOutputStream.writeObject(defaultForward);  
	            outStream.close();  
	            
                    FileOutputStream outStream1 = new FileOutputStream("nextLeft.txt");  
	            ObjectOutputStream objectOutputStream1 = new ObjectOutputStream(outStream1);  
	            objectOutputStream1.writeObject(nextLeft);  
	            outStream1.close();  
                    
                    FileOutputStream outStream2 = new FileOutputStream("nextRight.txt");  
	            ObjectOutputStream objectOutputStream2 = new ObjectOutputStream(outStream2);
	            objectOutputStream2.writeObject(nextRight);  
	            outStream2.close();
                    
//                    FileOutputStream outStream3 = new FileOutputStream("newControlledStateHashMap.txt");  
//	            ObjectOutputStream objectOutputStream3 = new ObjectOutputStream(outStream3);
//	            objectOutputStream3.writeObject(newControlledStateHashMap);  
//	            outStream3.close();                    
                    
                    FileOutputStream outStream4 = new FileOutputStream(gInputConfigureFileName+"allStateList.txt");  
	            ObjectOutputStream objectOutputStream4 = new ObjectOutputStream(outStream4);
	            objectOutputStream4.writeObject(allStateList);  
	            outStream4.close();
	            //System.out.println("successful"); 
                    
                      FileOutputStream outStream5 = new FileOutputStream("stableState.txt");  
	            ObjectOutputStream objectOutputStream5 = new ObjectOutputStream(outStream5);
	            objectOutputStream5.writeObject(stableState);  
	            outStream5.close();
                    
                      FileOutputStream outStream6 = new FileOutputStream("nonstableState.txt");  
	            ObjectOutputStream objectOutputStream6 = new ObjectOutputStream(outStream6);
	            objectOutputStream6.writeObject(nonStableState);  
	            outStream6.close();

         }
                 catch (IOException e) {  
	            e.printStackTrace();  
	        }  
		
    }

    public void InitializeBestActionGivenStateValue(HashMap<String, StateNode> allStateList) {
       		    Iterator<Entry<String, StateNode>> iter = allStateList.entrySet().iterator(); 
		    while (iter.hasNext()) {
		    Map.Entry entry = iter.next(); 
                    ActionNode actionNode;
                    double StateValue;
                    double tempBest=-1.0;
		    StateNode stateNode =   ((Entry<String, StateNode>) entry).getValue();		    	
		      if (stateNode.isTerminal==false) { 
                           for (int j=0;j<stateNode.action.length;j++)
		          {
 		            actionNode = stateNode.action[j];
                            StateValue=0;
			    for (int i=0;i<actionNode.nextState.count;i++) {
		                StateValue += gDiscount * actionNode.nextState.prob[i] * actionNode.nextState.state[i].stateValue;
                                     
                            }
                            if(StateValue>tempBest)
                            {
                                tempBest=StateValue;
                                stateNode.bestAction=actionNode;
                                //System.out.println("find the best action");
                            }
     		         }
		      }		 
		      
		    }
    }


 
    public enum Difficulty{blocked,difficult,free};
    public enum Direction{first,second,third,fourth,fifth,sixth,negativefirst,negativesecond,negativethird,negativefourth,negativefifth,negativesixth};    
    public enum Stability{stable,unstable};
    public enum Actor{driver,SAU};
    public enum Action{left,right,forward,reverse,continued};
    public enum EngagementLevel{engaged,notEngaged};
    public enum Message{increase,decrease,nop};
    public enum UncontrolledState{sparse,heavy};
    public enum Error{middle};
    public StateNode Start;
    
    public class ControlledState  implements Serializable
	{
        private static final long serialVersionUID = 1L;
        public OSMNode node;
        public int nodeID;
    	public Difficulty difficulty;
    	public Direction direction;

    	public ControlledState(OSMNode node,Difficulty difficulty,Direction direction)
    	{
                this.node=node;
    		this.difficulty=difficulty;
    		this.direction=direction;
                this.nodeID=node.getName();
    	}
    	public ControlledState() {
			// TODO Auto-generated constructor stub
		}
		@Override public String toString() {
    	    StringBuilder result = new StringBuilder();
    	    result.append("|id:"+node.toString()+ difficulty + direction + "|");
    	    return result.toString();
    	  }
	}
    
    public class StabilityState implements Serializable
	{
		//stability
        private static final long serialVersionUID = 1L;
    	public Stability stability;
    	public Action actionBeExecuting;/*The action is still executing*/
    	public Actor actionBeExecutingActor;
    	public Actor actor;
    
    	public StabilityState(Stability stability,Action a,Actor actor,Actor exe)
    	{
    		this.stability=stability;
    		this.actionBeExecuting=a;
    		this.actor=actor;
    		this.actionBeExecutingActor=exe;
    	}
    	public StabilityState() {
		}
		@Override public String toString() {
    	    StringBuilder result = new StringBuilder();
    	    result.append("|"+stability+actionBeExecuting+actionBeExecutingActor+actor+"|");
    	    return result.toString();
    	  }
	}
    
    	public class DriverState implements Serializable{ 
             private static final long serialVersionUID = 1L;
		public Error error;
                public int errorMade;
		public EngagementLevel engagement;
    	
    	public DriverState(){  		
    	}
    	
    	/**
    	 * Construction function of DriverState
    	 * @param tired
    	 * @param sober
    	 * @param d
    	 * @param e
    	 */
    	public DriverState(Error error, EngagementLevel e,int errorMade){
    		this.error=error;
    		this.engagement=e;
                this.errorMade=errorMade;
    	}
    	
		@Override public String toString() {
    	    StringBuilder result = new StringBuilder();
    	    result.append("|"+error+engagement+errorMade+"|");
    	    return result.toString();
    	  }
	}
    
     public   class StateNode  implements Serializable
    {
        private static final long serialVersionUID = 1L;
	public int stateNo;
	int Update;
    	public boolean isStart;
    	public boolean isTerminal;


    	public double stateValue;
    	//controlled state
    	public ControlledState controlledState;
 
    	//uncontrolled state
    	public UncontrolledState traffic;
    
    	//stability
    	public StabilityState stabilityState;
    	//engagementLevel
    	public DriverState driverState;
        public double timeToTransferOfControl;
        public double timeToGoal;
        public  double percentOfTimeSAU;
        public double timeToNextStability;
    	
    	@Override public String toString() {
    	    StringBuilder result = new StringBuilder();
      	    result.append("{" +controlledState.toString() +traffic+stabilityState.toString()+driverState.toString()+"}");
    	    return result.toString();
    	  }
    	
    	transient private double g;
    	transient double f;
    	transient private double h;
    	transient private double fWeight;
    	transient private double meanFirstPassage;
    	transient private boolean isInStack;
    	transient private int Expanded;
    	
	public ActionNode bestAction;
	transient private ActionNode[] action;//Initialized in void InitializeStateNode(StateNode stateNode)
	transient private ArrayList<ActionNode> PrevAction;//Initialized in getAllNextStateDistributuion

     
       private StateNode getNextUnsuccessUnStableState() {
			if(this.stabilityState.stability==Stability.unstable)
        	{
        		System.out.println("error!, it should be stable");
        		return null;
        	}
                        
                if(nonStableState.containsKey(this.toString()))
                    return allStateList.get(nonStableState.get(this.toString()));        
                        
        	StateNode temp1=getNextState(this, this.bestAction,0);
    
        	while(!(temp1.controlledState.node==this.controlledState.node&&temp1.controlledState.direction==this.controlledState.direction))        	
        	{
                temp1=getNextState(this, this.bestAction,0);// repeate until success
       	    }
        	nonStableState.put(this.toString(), temp1.toString());
        	return temp1;
        	
		}
        
         /**
         * Only called when state in a stable state. return the state depend on the paramter IsScucess, if true, return the new state assuming action success, or else, new state assuming unsuccess
         * @return
         */
        private StateNode getNextStableState(boolean IsSucess) {
             	// to guarantee action un Success, and then turn stable.
        	if(stableState.containsKey(this.toString()+IsSucess))
                    return allStateList.get(stableState.get(this.toString()+IsSucess));
                StateNode nextState=new StateNode();
		nextState.controlledState=getNewSuccessfulControlledState(this,this.bestAction,IsSucess);//Not Deterministic
		nextState.stabilityState=getNewStability(this,this.bestAction);// Deterministic in this case
		nextState.traffic=getNextTraffic(this.traffic);//Not Deterministic
		nextState.driverState=getNextDriverState(this,this.bestAction,false);//Deterministic
                nextState=allStateList.get(nextState.toString()); 
                System.out.println(nextState.toString());
                StateNode temp2;
                if(nextState.bestAction==null)
                {
                    nextState.bestAction=new ActionNode();
                    nextState.bestAction.action=Action.continued;
                    nextState.bestAction.msg=Message.nop;
                    nextState.bestAction.actor=Actor.SAU;
                   temp2=getNextState(nextState,nextState.bestAction ,0);
                }
        	// to guarantee restore
        	else
                    temp2=getNextState(nextState, nextState.bestAction,0);
                
                if(temp2.stabilityState.stability==Stability.stable)
                {
                stableState.put(this.toString()+IsSucess, temp2.toString());
        	return temp2;
                }
                StateNode temp3;
            	 do
        	{
                //System.out.println(temp2);
                //System.out.println(temp2.bestAction);
                temp3=getNextState(temp2, temp2.bestAction,0);// repeate until success
       	        }while(temp3.stabilityState.stability!=Stability.stable);
                stableState.put(this.toString()+IsSucess, temp3.toString());
        	return temp3;
        }

        
                        


       		/**
		 * calculate the expected time to get another state successful, to update double expectedTimeForSAU;
    	double expectedTimeForDriver;
		 * @return 
		 */
        public double calculateExpectedTimeToRestoreStable()
        {            
        	if(this.isTerminal==true)
        		{
        		 this.timeToNextStability=0.0;
        		 return 0.0;
        		}    
                
                
           StateNode temp=this.getNextStableState(false);
           StateNode s=new StateNode();
      	   s.controlledState=temp.controlledState;
		   s.driverState=temp.driverState;
		   s.stabilityState=temp.stabilityState;
		   s.traffic=this.traffic;
		   s=allStateList.get(s.toString());
System.out.println("begin to getStableState(true)");                   
                   
           double P[][]=new double[4][5];
           P=getParamter(s);
           double result1=(1-P[2][2]+P[1][2])/(1-P[2][2]-P[1][1]+P[1][1]*P[2][2]-P[1][2]*P[2][1]);
                      
      	  StateNode temp1=new StateNode();
		  temp1.controlledState=s.controlledState;
		  temp1.driverState=s.driverState;
		  temp1.stabilityState=s.stabilityState;
		  temp1.traffic=(this.traffic==UncontrolledState.sparse?UncontrolledState.heavy:UncontrolledState.sparse); 
	      temp1=allStateList.get(temp1.toString());
		  P=getParamter(temp1);
		  double result2=(1-P[2][2]+P[1][2])/(1-P[2][2]-P[1][1]+P[1][1]*P[2][2]-P[1][2]*P[2][1]);
		  

		  temp=getNextStableState(true);
		  StateNode temp2=new StateNode();
		  temp2.controlledState=temp.controlledState;
		  temp2.driverState=temp.driverState;
		  temp2.stabilityState=temp.stabilityState;
		  temp2.traffic=this.traffic;
		  temp2=allStateList.get(temp2.toString());
          P=getParamter(temp2);
          double result3=(1-P[2][2]+P[1][2])/(1-P[2][2]-P[1][1]+P[1][1]*P[2][2]-P[1][2]*P[2][1]);
  System.out.println("after to getNextStableState(true)");           
				  
		  StateNode temp3=new StateNode();
		  temp3.controlledState=temp2.controlledState;
		  temp3.driverState=temp2.driverState;
		  temp3.stabilityState=temp2.stabilityState;
		  temp3.traffic=(this.traffic==UncontrolledState.sparse?UncontrolledState.heavy:UncontrolledState.sparse);
		  temp3=allStateList.get(temp3.toString());
		  P=getParamter(temp3);
		  double result4=(1-P[2][2]+P[1][2])/(1-P[2][2]-P[1][1]+P[1][1]*P[2][2]-P[1][2]*P[2][1]);

		  
		  double para[]=new double[4];
		  para=getParamterForFourSituation(this);
		  
		  this.timeToNextStability=1+para[0]*result1+para[1]*result2+para[2]*result3+para[3]*result4;
		  return this.timeToNextStability;
		  
        }

    		public void calculateExpectedPercentTimeOfSAUInControl() {
				this.percentOfTimeSAU=0.0;		
		}


		public void calculateExpectedTimeToTransferOfControl() {
			
			this.timeToTransferOfControl=0.0;			
		}

        private StateNode getNextSuccessStableState(StateNode stateNode) {
         	if(this.stabilityState.stability==Stability.unstable)
        	{
        		System.out.println("error!, it should be stable");
        		return null;
        	}
        	StateNode temp1=getNextState(this, this.bestAction,0);
    
        	while(temp1.controlledState.node==this.controlledState.node&&temp1.controlledState.direction==this.controlledState.direction&&this.driverState.engagement==temp1.driverState.engagement)	
        	{
                temp1=getNextState(this, this.bestAction,0);// repeate until success
       	    }
        	
        	return temp1;
        }

        private ControlledState getNewSuccessfulControlledState(StateNode stateNode, ActionNode actionNode,boolean IsSuccess) {
            ControlledState temp=new ControlledState();
		temp.node=stateNode.controlledState.node;
		temp.difficulty=stateNode.controlledState.difficulty;
		temp.direction=stateNode.controlledState.direction;
		if(stateNode.stabilityState.stability==Stability.unstable)
		{
			return null;
		}
		else
		{
			if(stateNode.controlledState.difficulty==Difficulty.blocked&&stateNode.traffic==UncontrolledState.sparse)
			{
				if(IsSuccess==true)//success
				{
					isActionSuccess=true;
					return calculateNewControlledState(stateNode.controlledState,actionNode.action);
				}
				else//failure
				{
					isActionSuccess=false;
					return temp; 
				}
			}
			else
				if(stateNode.controlledState.difficulty==Difficulty.blocked&&stateNode.traffic==UncontrolledState.heavy)
				{
					if(IsSuccess==true)//success
					{
						isActionSuccess=true;
						return calculateNewControlledState(stateNode.controlledState,actionNode.action);
					}
					else//failure
						{
						 isActionSuccess=false;
						 return temp;
						}
				}
				else
					if(stateNode.controlledState.difficulty==Difficulty.difficult&&stateNode.traffic==UncontrolledState.sparse)
					{
						if(IsSuccess==true)//success
						{
							isActionSuccess=true;
							return calculateNewControlledState(stateNode.controlledState,actionNode.action);
						}
						else//failure
							{
							isActionSuccess=false;
							 return temp;
							}
					}
					else
						if(stateNode.controlledState.difficulty==Difficulty.difficult&&stateNode.traffic==UncontrolledState.heavy)
						{
							if(IsSuccess==true)//success
							{
								isActionSuccess=true;
								return calculateNewControlledState(stateNode.controlledState,actionNode.action);
							}
							else//failure
							{
								isActionSuccess=false;
								return temp;
							}
						}
						else
							if(stateNode.controlledState.difficulty==Difficulty.free&&stateNode.traffic==UncontrolledState.sparse)
							{
								if(IsSuccess==true)//success
								{
									isActionSuccess=true;
									return calculateNewControlledState(stateNode.controlledState,actionNode.action);
								}
								else//failure
								{
									isActionSuccess=false;
									return temp;
								}
							}
							else
								if(stateNode.controlledState.difficulty==Difficulty.free&&stateNode.traffic==UncontrolledState.heavy)
								{
									if(IsSuccess==true)//success
									{
										isActionSuccess=true;
										return calculateNewControlledState(stateNode.controlledState,actionNode.action);
									}
									else//failure
									{
										isActionSuccess=false;
										return temp;
									}
								}
						
						
		}
		return null;
	}
		       
    }
        
        	/**
	 * Action Node is the circle, a chance node, also And node.
	 * 
	 * @author yilu
	 * 
	 */
	public static class ActionNode implements Serializable{
                private static final long serialVersionUID = 1L;
		public Action action;
		String descrption;
		public Actor actor;
		double cost;//Initailized in void InitializeStateNode()
		transient StateDistribution nextState;//Initialized in getAllNextStateDistributuion
		transient StateNode prevState;//Initialized in void InitializeStateNode(StateNode stateNode)
		public Message msg;
		
		@Override public String toString() {
    	    StringBuilder result = new StringBuilder();
    	    result.append("Action: "+action+" , "+"Actor: "+actor+" ,Msg:"+msg+",Cost:"+cost);
    	    return result.toString();
    	  }
                public ActionNode()
                {
                    
                }
		
	}

	/**
	 * StateDistribution is formed by when take an action, it may have many possible next states.
	 * 
	 * @author yilu
	 * 
	 */
	class StateDistribution implements Serializable{
                private static final long serialVersionUID = 1L;
		int count;
		StateNode[] state;
		double[] prob;
		public void PrintStateDistribution()
		{
			for(int i=0;i<count;i++)
			System.out.println(state[i].toString()+",statevalue"+state[i].stateValue+", and Prob is: "+prob[i]);
		}
	}
    
        public static Difficulty GetDifficulty(OSMNode node) {
            if( node.difficulty==0)            
		return Difficulty.free;
            else
                if(node.difficulty==1)
                    return Difficulty.difficult;
                            else
                    return Difficulty.blocked;
                
	}
        
           private double[] getParamterForFourSituation(StateNode s) {
		double P[]=new double[4];
		if(s.controlledState.difficulty==Difficulty.blocked)
		{
			if(s.traffic==UncontrolledState.heavy)
			{
				P[0]=(1-p_success_blocked_heavy)*(1-transition_heavy_sparse);//
				P[1]=(1-p_success_blocked_heavy)*(transition_heavy_sparse);//
				P[2]=(p_success_blocked_heavy)*(1-transition_heavy_sparse);//
				P[3]=(p_success_blocked_heavy)*(transition_heavy_sparse);//
			}
			else// traffic=sparse
			{
				P[0]=(1-p_success_blocked_sparse)*(1-transition_sparse_heavy);
				P[1]=(1-p_success_blocked_sparse)*(transition_sparse_heavy);
				P[2]=(p_success_blocked_sparse)*(1-transition_sparse_heavy);
				P[3]=(p_success_blocked_sparse)*(transition_sparse_heavy);
			}
		}
		else if (s.controlledState.difficulty==Difficulty.difficult)
		{
			if(s.traffic==UncontrolledState.heavy)
			{
				P[0]=(1-p_success_difficult_heavy)*(1-transition_heavy_sparse);
				P[1]=(1-p_success_difficult_heavy)*(transition_heavy_sparse);
				P[2]=(p_success_difficult_heavy)*(1-transition_heavy_sparse);
				P[3]=(p_success_difficult_heavy)*(transition_heavy_sparse);
			}
			else// traffic=sparse
			{
				P[0]=(1-p_success_difficult_sparse)*(1-transition_sparse_heavy);
				P[1]=(1-p_success_difficult_sparse)*(transition_sparse_heavy);
				P[2]=(p_success_difficult_sparse)*(1-transition_sparse_heavy);
				P[3]=(p_success_difficult_sparse)*(transition_sparse_heavy);
			}

		}
		else// free
		{
			if(s.traffic==UncontrolledState.heavy)
			{
				P[0]=(1-p_success_free_heavy)*(1-transition_heavy_sparse);
				P[1]=(1-p_success_free_heavy)*(transition_heavy_sparse);
				P[2]=(p_success_free_heavy)*(1-transition_heavy_sparse);
				P[3]=(p_success_free_heavy)*(transition_heavy_sparse);
			}
			else// traffic=sparse
			{
				P[0]=(1-p_success_free_sparse)*(1-transition_sparse_heavy);
				P[1]=(1-p_success_free_sparse)*(transition_sparse_heavy);
				P[2]=(p_success_free_sparse)*(1-transition_sparse_heavy);
				P[3]=(p_success_free_sparse)*(transition_sparse_heavy);
			}
		
		}
		
		
		return P;
	}

	    private double[][] getParamter(StateNode s) {
		
		double P[][]=new double[4][5];
		if(s.bestAction.actor==Actor.driver)
		{
		  if(s.traffic==UncontrolledState.sparse)
		  {
			if(s.controlledState.difficulty==Difficulty.blocked)
			{
				P[1][1]=(1-p_driver_blocked_sparse)*(1-transition_sparse_heavy);
				P[1][2]=(1-p_driver_blocked_sparse)*(transition_sparse_heavy);
				P[1][3]=(p_driver_blocked_sparse)*(1-transition_sparse_heavy);
				P[1][4]=(p_driver_blocked_sparse)*(transition_sparse_heavy);
				
				P[2][1]=(1-p_driver_blocked_heavy)*(1-transition_heavy_sparse);
				P[2][2]=(1-p_driver_blocked_heavy)*(transition_heavy_sparse);
				P[2][3]=(p_driver_blocked_heavy)*(transition_heavy_sparse);
				P[2][4]=(p_driver_blocked_heavy)*(1-transition_heavy_sparse);				
			}
			else if (s.controlledState.difficulty==Difficulty.difficult)
			{
				P[1][1]=(1-p_driver_difficult_sparse)*(1-transition_sparse_heavy);
				P[1][2]=(1-p_driver_difficult_sparse)*(transition_sparse_heavy);
				P[1][3]=(p_driver_difficult_sparse)*(1-transition_sparse_heavy);
				P[1][4]=(p_driver_difficult_sparse)*(transition_sparse_heavy);
				
				P[2][1]=(1-p_driver_difficult_heavy)*(1-transition_heavy_sparse);
				P[2][2]=(1-p_driver_difficult_heavy)*(transition_heavy_sparse);
				P[2][3]=(p_driver_difficult_heavy)*(transition_heavy_sparse);
				P[2][4]=(p_driver_difficult_heavy)*(1-transition_heavy_sparse);
			}
			else // free
			{
				P[1][1]=(1-p_free_sparse)*(1-transition_sparse_heavy);
				P[1][2]=(1-p_free_sparse)*(transition_sparse_heavy);
				P[1][3]=(p_free_sparse)*(1-transition_sparse_heavy);
				P[1][4]=(p_free_sparse)*(transition_sparse_heavy);
				
				P[2][1]=(1-p_free_heavy)*(1-transition_heavy_sparse);
				P[2][2]=(1-p_free_heavy)*(transition_heavy_sparse);
				P[2][3]=(p_free_heavy)*(transition_heavy_sparse);
				P[2][4]=(p_free_heavy)*(1-transition_heavy_sparse);

			
			}

		 }
		 else// traffic=heavy
		 {
				if(s.controlledState.difficulty==Difficulty.blocked)
				{
					P[2][2]=(1-p_driver_blocked_sparse)*(1-transition_sparse_heavy);
					P[2][1]=(1-p_driver_blocked_sparse)*(transition_sparse_heavy);
					P[2][3]=(p_driver_blocked_sparse)*(1-transition_sparse_heavy);
					P[2][4]=(p_driver_blocked_sparse)*(transition_sparse_heavy);
					
					P[1][2]=(1-p_driver_blocked_heavy)*(1-transition_heavy_sparse);
					P[1][1]=(1-p_driver_blocked_heavy)*(transition_heavy_sparse);
					P[1][3]=(p_driver_blocked_heavy)*(transition_heavy_sparse);
					P[1][4]=(p_driver_blocked_heavy)*(1-transition_heavy_sparse);				
				}
				else if (s.controlledState.difficulty==Difficulty.difficult)
				{
					P[2][2]=(1-p_driver_difficult_sparse)*(1-transition_sparse_heavy);
					P[2][1]=(1-p_driver_difficult_sparse)*(transition_sparse_heavy);
					P[2][3]=(p_driver_difficult_sparse)*(1-transition_sparse_heavy);
					P[2][4]=(p_driver_difficult_sparse)*(transition_sparse_heavy);
					
					P[1][2]=(1-p_driver_difficult_heavy)*(1-transition_heavy_sparse);
					P[1][1]=(1-p_driver_difficult_heavy)*(transition_heavy_sparse);
					P[1][3]=(p_driver_difficult_heavy)*(transition_heavy_sparse);
					P[1][4]=(p_driver_difficult_heavy)*(1-transition_heavy_sparse);
				}
				else // free
				{
					P[2][2]=(1-p_free_sparse)*(1-transition_sparse_heavy);
					P[2][2]=(1-p_free_sparse)*(transition_sparse_heavy);
					P[2][3]=(p_free_sparse)*(1-transition_sparse_heavy);
					P[2][4]=(p_free_sparse)*(transition_sparse_heavy);
					
					P[1][2]=(1-p_free_heavy)*(1-transition_heavy_sparse);
					P[1][1]=(1-p_free_heavy)*(transition_heavy_sparse);
					P[1][3]=(p_free_heavy)*(transition_heavy_sparse);
					P[1][4]=(p_free_heavy)*(1-transition_heavy_sparse);
				}	
		 }
		  
		}
		else// bset action.actor ==system
		{
			  if(s.traffic==UncontrolledState.sparse)
			  {
				if(s.controlledState.difficulty==Difficulty.blocked)
				{
					P[1][1]=(1-p_system_blocked_sparse)*(1-transition_sparse_heavy);
					P[1][2]=(1-p_system_blocked_sparse)*(transition_sparse_heavy);
					P[1][3]=(p_system_blocked_sparse)*(1-transition_sparse_heavy);
					P[1][4]=(p_system_blocked_sparse)*(transition_sparse_heavy);
					
					P[2][1]=(1-p_system_blocked_heavy)*(1-transition_heavy_sparse);
					P[2][2]=(1-p_system_blocked_heavy)*(transition_heavy_sparse);
					P[2][3]=(p_system_blocked_heavy)*(transition_heavy_sparse);
					P[2][4]=(p_system_blocked_heavy)*(1-transition_heavy_sparse);				
				}
				else if (s.controlledState.difficulty==Difficulty.difficult)
				{
					P[1][1]=(1-p_system_difficult_sparse)*(1-transition_sparse_heavy);
					P[1][2]=(1-p_system_difficult_sparse)*(transition_sparse_heavy);
					P[1][3]=(p_system_difficult_sparse)*(1-transition_sparse_heavy);
					P[1][4]=(p_system_difficult_sparse)*(transition_sparse_heavy);
					
					P[2][1]=(1-p_system_difficult_heavy)*(1-transition_heavy_sparse);
					P[2][2]=(1-p_system_difficult_heavy)*(transition_heavy_sparse);
					P[2][3]=(p_system_difficult_heavy)*(transition_heavy_sparse);
					P[2][4]=(p_system_difficult_heavy)*(1-transition_heavy_sparse);
				}
				else // free
				{
					P[1][1]=(1-p_free_sparse)*(1-transition_sparse_heavy);
					P[1][2]=(1-p_free_sparse)*(transition_sparse_heavy);
					P[1][3]=(p_free_sparse)*(1-transition_sparse_heavy);
					P[1][4]=(p_free_sparse)*(transition_sparse_heavy);
					
					P[2][1]=(1-p_free_heavy)*(1-transition_heavy_sparse);
					P[2][2]=(1-p_free_heavy)*(transition_heavy_sparse);
					P[2][3]=(p_free_heavy)*(transition_heavy_sparse);
					P[2][4]=(p_free_heavy)*(1-transition_heavy_sparse);

				
				}

			 }
			 else// traffic=heavy
			 {
					if(s.controlledState.difficulty==Difficulty.blocked)
					{
						P[2][2]=(1-p_system_blocked_sparse)*(1-transition_sparse_heavy);
						P[2][1]=(1-p_system_blocked_sparse)*(transition_sparse_heavy);
						P[2][3]=(p_system_blocked_sparse)*(1-transition_sparse_heavy);
						P[2][4]=(p_system_blocked_sparse)*(transition_sparse_heavy);
						
						P[1][2]=(1-p_system_blocked_heavy)*(1-transition_heavy_sparse);
						P[1][1]=(1-p_system_blocked_heavy)*(transition_heavy_sparse);
						P[1][3]=(p_system_blocked_heavy)*(transition_heavy_sparse);
						P[1][4]=(p_system_blocked_heavy)*(1-transition_heavy_sparse);				
					}
					else if (s.controlledState.difficulty==Difficulty.difficult)
					{
						P[2][2]=(1-p_system_difficult_sparse)*(1-transition_sparse_heavy);
						P[2][1]=(1-p_system_difficult_sparse)*(transition_sparse_heavy);
						P[2][3]=(p_system_difficult_sparse)*(1-transition_sparse_heavy);
						P[2][4]=(p_system_difficult_sparse)*(transition_sparse_heavy);
						
						P[1][2]=(1-p_system_difficult_heavy)*(1-transition_heavy_sparse);
						P[1][1]=(1-p_system_difficult_heavy)*(transition_heavy_sparse);
						P[1][3]=(p_system_difficult_heavy)*(transition_heavy_sparse);
						P[1][4]=(p_system_difficult_heavy)*(1-transition_heavy_sparse);
					}
					else // free
					{
						P[2][2]=(1-p_free_sparse)*(1-transition_sparse_heavy);
						P[2][2]=(1-p_free_sparse)*(transition_sparse_heavy);
						P[2][3]=(p_free_sparse)*(1-transition_sparse_heavy);
						P[2][4]=(p_free_sparse)*(transition_sparse_heavy);
						
						P[1][2]=(1-p_free_heavy)*(1-transition_heavy_sparse);
						P[1][1]=(1-p_free_heavy)*(transition_heavy_sparse);
						P[1][3]=(p_free_heavy)*(transition_heavy_sparse);
						P[1][4]=(p_free_heavy)*(1-transition_heavy_sparse);

					
					}
			 }
		
		}
		return P;
	}
        
        public void CreateAllStateList(Demo demo)
	{
		allStateList=new HashMap<String,StateNode>(demo.graph.nodeCount()*1200);
		//allStateList.put("one", new StateNode());
		int count=0;
		Start=new StateNode();
		Start.controlledState=new ControlledState(demo.startPos,GetDifficulty(demo.startPos), Direction.first);
		Start.traffic=UncontrolledState.sparse;
		Start.isStart=true;
		Start.isTerminal=false;
		Start.PrevAction=null;
                Start.driverState=new DriverState(Error.middle, EngagementLevel.engaged,0);
		Start.stateNo=count++;
		Start.stateValue=0;
		Start.Update=0;
		Start.stabilityState=new StabilityState(Stability.stable,null,null,null);
               	
                Start.timeToGoal=0.0;
		Start.timeToTransferOfControl=0.0;
		Start.percentOfTimeSAU=0.0;
                //System.out.println(Start.toString());

		allStateList.put(Start.toString(),Start);
		int maxDegree=4; 
                
                //System.out.println(demo.graph.getNodes().size());
		for(OSMNode node:demo.graph.getNodes()){
//				for(Difficulty difficulty: Difficulty.values())// we don't need to iterate with difficulty because it is deterministic given x and y
//				{
                    if(node.getOutLinks().size()>maxDegree)
                        maxDegree=node.getOutLinks().size();
                   // System.out.println(maxDegree);
				 for(int direction=0;direction<node.getOutLinks().size();direction++)//*5
				 {
					 for(UncontrolledState traffic:UncontrolledState.values())//*3
					 {
						 for(Stability stability:Stability.values())//*2
						 {
							 for(Action actionBeExecuting:Action.values())//*5
							 {
								 for(Actor actor:Actor.values())//*2
								 {
									 for(Actor lastActor:Actor.values())//*2
									 {
									 for(EngagementLevel engagement:EngagementLevel.values())//*2
									 {
                                                                          for(Error error:Error.values())
                                                                               for(int i=0;i<=MaximumErrorNumber;i++)
										 {   
//										    StringBuilder result = new StringBuilder();
//									  	    result.append("{" +"|x:" + x+"y:" + y+ difficulty + direction + "|" +traffic);
//									  	    result.append("|"+stability+actionBeExecuting+actor+"|"+engagement+"}");
									  	  StateNode temp=new StateNode();
									  	  temp.controlledState=new ControlledState(node,GetDifficulty(node),Direction.values()[direction]);
									  	  temp.stabilityState=new StabilityState(stability,actionBeExecuting,actor,lastActor);
									  	  temp.driverState=new DriverState(error,engagement,i);;
									  	  temp.traffic=traffic;
									  	  temp.stateNo=count;
									  	  temp.stateValue=0;
									  	  temp.Update=0;
									  	  temp.Expanded=0;
									  	  temp.fWeight=0;
									  	  temp.g=0;
									  	  temp.f=0;
                                                                                  
                                                                                  temp.timeToGoal=0.0;
										  temp.timeToTransferOfControl=0.0;
										  temp.percentOfTimeSAU=0.0;
                                                                                  
                                                                                  if(temp.stabilityState.stability==Stability.stable)
                                                                                  {
                                                                                      temp.h=bestList.get(node+","+temp.controlledState.direction).fValue;
                                                                                      //System.out.print("new value : "+temp.h+" , old value :"+node.hValue);
                                                                                  }
                                                                                  else
                                                                                  {
                                                                                      temp.h=bestList.get(node+","+temp.controlledState.direction).fValue+COST_SAU_MSG_NOP_CONTINUED;
                                                                                     // System.out.println("new value : "+temp.h+" , old value :"+node.hValue);
                                                                                  }
									  	  
									  
									  	  temp.meanFirstPassage=0;
						
									  	  if(node.equals(demo.startPos))
									  		     temp.isStart=true;
									  	  else
									  		  temp.isStart=false;
									  	  
									  	  if(node.equals(demo.endPos) &&temp.stabilityState.stability==Stability.stable)
									  		  {
									  		    temp.isTerminal=true;
									  		    temp.stateValue=1000000000;
									  		    temp.g=0;
									  		    temp.f=0;
									  		    temp.h=0;									  		  
									  		  }
									  	  else
									  		  temp.isTerminal=false;
									  	  allStateList.put(temp.toString(),temp);  
                                                                                  //System.out.println(temp.toString());
                                                                                  count++;
									 }
								 }
							 }
						 }
					 }
				  }
                                 }
                                 }
                                 
                                 
				 for(int direction=0;direction<node.getInLinks().size();direction++)//*4
				 {
                                     //System.out.println("In Links Number:"+node.getInLinks().size());
					 for(UncontrolledState traffic:UncontrolledState.values())//*3
					 {
						 for(Stability stability:Stability.values())//*2
						 {
							 for(Action actionBeExecuting:Action.values())//*4
							 {
								 for(Actor actor:Actor.values())//*2
								 {
									 for(Actor lastActor:Actor.values())//*2
									 {
									 for(EngagementLevel engagement:EngagementLevel.values())//*2
									 {
                                                                             for(Error error:Error.values())
                                                                                  for(int i=0;i<=MaximumErrorNumber;i++)
										 {
//										    StringBuilder result = new StringBuilder();
//									  	    result.append("{" +"|x:" + x+"y:" + y+ difficulty + direction + "|" +traffic);
//									  	    result.append("|"+stability+actionBeExecuting+actor+"|"+engagement+"}");
									  	  StateNode temp=new StateNode();
									  	  temp.controlledState=new ControlledState(node,GetDifficulty(node),Direction.values()[direction+Direction.values().length/2]);
									  	  temp.stabilityState=new StabilityState(stability,actionBeExecuting,actor,lastActor);
									  	  temp.driverState=new DriverState(error,engagement,i);
									  	  temp.traffic=traffic;
									  	  temp.stateNo=count;
									  	  temp.stateValue=0;
									  	  temp.Update=0;
									  	  temp.Expanded=0;
									  	  temp.fWeight=0;
									  	  temp.g=0;
									  	  temp.f=0;
										  temp.timeToGoal=0.0;
										  temp.timeToTransferOfControl=0.0;
										  temp.percentOfTimeSAU=0.0;                                                                                 
                                                                                  
                                                                                   if(temp.stabilityState.stability==Stability.stable)
                                                                                  {
                                                                                      temp.h=bestList.get(node+","+temp.controlledState.direction).fValue;
                                                                                      //System.out.print("new value : "+temp.h+" , old value :"+node.hValue);
                                                                                  }
                                                                                  else
                                                                                  {
                                                                                      temp.h=bestList.get(node+","+temp.controlledState.direction).fValue+COST_SAU_MSG_NOP_CONTINUED;
                                                                                      //System.out.println("new value : "+bestList.get(node+","+temp.controlledState.direction).fValue +" , old value :"+node.hValue);
                                                                                  }
									  
									  	  temp.meanFirstPassage=0;
						
									  	  if(node.equals(demo.startPos))
									  		     temp.isStart=true;
									  	  else
									  		  temp.isStart=false;
									  	  
									  	  if(node.equals(demo.endPos) &&temp.stabilityState.stability==Stability.stable)
									  		  {
									  		    temp.isTerminal=true;
									  		    temp.stateValue=10000000;
									  		    temp.g=0;
									  		    temp.f=0;
									  		    temp.h=0;									  		  
									  		  }
									  	  else
									  		  temp.isTerminal=false;
									  	  allStateList.put(temp.toString(),temp);
                                                                                 // System.out.println(temp.toString());
                                                                                  count++;
									 }
								 }
							 }
						 }
					 }
				  }
                                 }                                 
                                 }     
                }
		
	}
	
        public void Initialize(Demo demo) throws IOException
	{
		
		ReadConfigureFile();
                GenerateHeuristic gh=new GenerateHeuristic();
                
                bestList=gh.generateHeuristic(demo.startPos,demo.endPos,demo.graph);
                newControlledStateHashMap=new HashMap<String,ControlledState>();
		if(STAGE=="STORE")
                {
                   CreateAllStateList(demo);
                   System.out.println("Created State list!");
                }
                else
                {
                Start=new StateNode();
		Start.controlledState=new ControlledState(demo.startPos,GetDifficulty(demo.startPos), Direction.first);
		Start.traffic=UncontrolledState.sparse;
		Start.isStart=true;
		Start.isTerminal=false;
		Start.PrevAction=null;
                Start.driverState=new DriverState(Error.middle, EngagementLevel.engaged,0);
		Start.stateValue=0;
		Start.Update=0;
		Start.stabilityState=new StabilityState(Stability.stable,null,null,null);
               	
                Start.timeToGoal=0.0;
		Start.timeToTransferOfControl=0.0;
		Start.percentOfTimeSAU=0.0;
                }
                imageVehicle   =   new   ImageIcon("vehicle.jpg");
                imageRobot=new ImageIcon("robot.png");
		
	}
        public void InitializeAllStateList(Demo demo)
        {
              System.out.println("Begin to Initialized all state list!");
            Iterator<Entry<String, StateNode>> iter = allStateList.entrySet().iterator(); 
		while (iter.hasNext()) {
		    Map.Entry entry = iter.next(); 
		   // String key = ((Entry<String, StateNode>) entry).getKey(); 
		    StateNode val =   ((Entry<String, StateNode>) entry).getValue();
//		    System.out.println("Initialize for "+ val.toString());
		    ActionNode[] temp=InitializeStateNode(val);
		    val.action=temp;
//		    for(int i=0;i<temp.length;i++)
//		    {
//		    	System.out.println(temp[i].toString());
//		    }
		} 
		System.out.println("Initialized StateNode!");
		iter = allStateList.entrySet().iterator(); 
		while (iter.hasNext()) { 
			Map.Entry entry = iter.next(); 
		   // String key = ((Entry<String, StateNode>) entry).getKey(); 
		    StateNode val =   ((Entry<String, StateNode>) entry).getValue();
		    int i=0;
		    for(i=0;i<val.action.length;i++)
		    {
		    	//System.out.println("Before call getAllNextStateDistributuion:"+val.toString()+", actor: "+val.action[i].toString());		    	
		    	getAllNextStateDistributuion(val,val.action[i]);
		    	//Thread sync problem?????
		    	//System.out.println("After call getAllNextStateDistributuion:"+val.toString()+", actor: "+val.action[i].toString());
		    	//stateDistribution.PrintStateDistribution();
		    }
		    		    
		}
            System.out.println("Initialized all state list!");
        }
        
                public void InitializeAllStateListLAO(Demo demo)
        {
              System.out.println("Begin to Initialized all state list!");
            Iterator<Entry<String, StateNode>> iter = allStateList.entrySet().iterator(); 
		while (iter.hasNext()) {
		    Map.Entry entry = iter.next(); 
		   // String key = ((Entry<String, StateNode>) entry).getKey(); 
		    StateNode val =   ((Entry<String, StateNode>) entry).getValue();
//		    System.out.println("Initialize for "+ val.toString());
		    ActionNode[] temp=InitializeStateNode(val);
		    val.action=temp;
//		    for(int i=0;i<temp.length;i++)
//		    {
//		    	System.out.println(temp[i].toString());
//		    }
		} 
//		System.out.println("Initialized StateNode!");
//		iter = allStateList.entrySet().iterator(); 
//		while (iter.hasNext()) { 
//			Map.Entry entry = iter.next(); 
//		   // String key = ((Entry<String, StateNode>) entry).getKey(); 
//		    StateNode val =   ((Entry<String, StateNode>) entry).getValue();
//		    int i=0;
//		    for(i=0;i<val.action.length;i++)
//		    {
//		    	//System.out.println("Before call getAllNextStateDistributuion:"+val.toString()+", actor: "+val.action[i].toString());		    	
//		    	getAllNextStateDistributuion(val,val.action[i]);
//		    	//Thread sync problem?????
//		    	//System.out.println("After call getAllNextStateDistributuion:"+val.toString()+", actor: "+val.action[i].toString());
//		    	//stateDistribution.PrintStateDistribution();
//		    }
//		    		    
//		}
//            System.out.println("Initialized all state list!");
        }
    
	
	/**
	 * return all possible next StateNode given actionNode
	 * @param stateNode
	 * @param actionNode
	 * @return
	 */
	public StateDistribution getAllNextStateDistributuion(StateNode stateNode, ActionNode actionNode)
	{
		Result[] result1;

		if(stateNode.driverState.engagement==EngagementLevel.notEngaged||stateNode.stabilityState.stability==Stability.unstable||stateNode.driverState.errorMade>=MaximumErrorNumber||stateNode.controlledState.difficulty==Difficulty.free)
		{
			result1=getAllNewControlledState(stateNode,actionNode);//Not Deterministic
			
			Result[] result2=getAllNewStability(stateNode,actionNode);//Not Deterministic
			//System.out.println(realAction.toString());
			Result[] result3=getAllNextTraffic(stateNode.traffic);//Not Deterministic
			Result[] result4=getAllNextDriverState(stateNode,actionNode,0);//Not Deterministic
			int i,j,k,l,p=0;
			double sumOfProb=0.0;
			StateDistribution stateDistribution=new StateDistribution();
			int count=result1.length*result2.length*result3.length*result4.length;
              
			StateNode[] temp=new StateNode[count];
			
			double[] prob=new double[count];
			for(i=0;i<result1.length;i++)
				for(j=0;j<result2.length;j++)
					for(k=0;k<result3.length;k++)
						for(l=0;l<result4.length;l++)
					{
						 StringBuilder result = new StringBuilder();
						 result.append("{" +result1[i].temp +result3[k].temp+result2[j].temp+result4[l].temp+"}");
					    //System.out.println("In getAllNextStateDistributuion:"+result.toString());
						temp[p]=allStateList.get(result.toString());
					    //System.out.println("In getAllNextStateDistributuion2:"+temp[p].toString());
						if(temp[p].PrevAction==null)
							{
							temp[p].PrevAction=new ArrayList<ActionNode>();
							}
						temp[p].PrevAction.add(actionNode);//Initialize StateNode.PrevAction;
					    prob[p]=result1[i].probability*result2[j].probability*result3[k].probability*result4[l].probability;
					    sumOfProb+=prob[p];
						p++;
					}
			for(i=0;i<count;i++)
			{
				prob[i]/=sumOfProb;
			}
			stateDistribution.count=count;
			stateDistribution.prob=prob;
			stateDistribution.state=temp;
			actionNode.nextState=stateDistribution;//Initialize actionNode.nextState
			//System.out.println("In getAllNextStateDistributuion:"+stateNode.toString()+", actor: "+actionNode.toString());
			return stateDistribution;
		}
		else // in this case, the action may be different from the SAU advice
		{
				if(stateNode.driverState.error==Error.middle)
				{
					result1=null;
					for(Action action:Action.values())
					{
						if(action==actionNode.action)// which means that the driver follows SAU advice.//probability=MiddleError
							{
							    result1=getAllNewControlledState(stateNode,actionNode);//Not Deterministic
								Result[] result2=getAllNewStability(stateNode,actionNode);//Not Deterministic
								//System.out.println(realAction.toString());
								Result[] result3=getAllNextTraffic(stateNode.traffic);//Not Deterministic
								Result[] result4=getAllNextDriverState(stateNode,actionNode,0);//Not Deterministic
								
								int i,j,k,l,p=0;
								double sumOfProb=0.0;
								StateDistribution stateDistribution=new StateDistribution();
								int count=result1.length*result2.length*result3.length*result4.length;
								StateNode[] temp=new StateNode[count];
								
								double[] prob=new double[count];
								for(i=0;i<result1.length;i++)
									for(j=0;j<result2.length;j++)
										for(k=0;k<result3.length;k++)
											for(l=0;l<result4.length;l++)
										{
											 StringBuilder result = new StringBuilder();
											 result.append("{" +result1[i].temp +result3[k].temp+result2[j].temp+result4[l].temp+"}");
										   // System.out.println("In getAllNextStateDistributuion:"+result.toString());
											temp[p]=allStateList.get(result.toString());
										   // System.out.println("In getAllNextStateDistributuion2:"+temp[p].toString());
											if(temp[p].PrevAction==null)
												{
												temp[p].PrevAction=new ArrayList<ActionNode>();
												}
											temp[p].PrevAction.add(actionNode);//Initialize StateNode.PrevAction;
										    prob[p]=result1[i].probability*result2[j].probability*result3[k].probability*result4[l].probability;
										    sumOfProb+=prob[p];
											p++;
										}
								for(i=0;i<count;i++)
								{
									prob[i]/=sumOfProb;
									prob[i]*=ActionSuccessRate;//because the the probability is MiddleError to follow the advice.
								}
								stateDistribution.count=count;
								stateDistribution.prob=prob;
								stateDistribution.state=temp;
								actionNode.nextState=Merge(actionNode.nextState,stateDistribution);//Initialize actionNode.nextState
							}
						else// which means the driver doesn't follow the SAU advice,so the actor is Driver, and the probability is (1-MiddleError)/(Action.values().length-1)) 
							{
							ActionNode tmp=new ActionNode();
							tmp.action=action;
							tmp.actor=Actor.driver;
							tmp.msg=actionNode.msg;
							//System.out.println(actionNode.actor+"should always be Driver");
							result1=getAllNewControlledState(stateNode,tmp);//Not Deterministic
							
							Result[] result2=getAllNewStability(stateNode,tmp);//Not Deterministic
							//System.out.println(realAction.toString());
							Result[] result3=getAllNextTraffic(stateNode.traffic);//Not Deterministic
							Result[] result4=getAllNextDriverState(stateNode,tmp,1);//Not Deterministic
							
							int i,j,k,l,p=0;
							double sumOfProb=0.0;
							StateDistribution stateDistribution=new StateDistribution();
							int count=result1.length*result2.length*result3.length*result4.length;
							StateNode[] temp=new StateNode[count];
							
							double[] prob=new double[count];
							for(i=0;i<result1.length;i++)
								for(j=0;j<result2.length;j++)
									for(k=0;k<result3.length;k++)
										for(l=0;l<result4.length;l++)
									{
										 StringBuilder result = new StringBuilder();
										 result.append("{" +result1[i].temp +result3[k].temp+result2[j].temp+result4[l].temp+"}");
									   // System.out.println("In getAllNextStateDistributuion:"+result.toString());
										temp[p]=allStateList.get(result.toString());
									   // System.out.println("In getAllNextStateDistributuion2:"+temp[p].toString());
										if(temp[p].PrevAction==null)
											{
											temp[p].PrevAction=new ArrayList<ActionNode>();
											}
										temp[p].PrevAction.add(actionNode);//Initialize StateNode.PrevAction;
									    prob[p]=result1[i].probability*result2[j].probability*result3[k].probability*result4[l].probability;
									    sumOfProb+=prob[p];
										p++;
									}
							for(i=0;i<count;i++)
							{
								prob[i]/=sumOfProb;
								prob[i]*=((1-ActionSuccessRate)/(Action.values().length-1));
							}
							stateDistribution.count=count;
							stateDistribution.prob=prob;
							stateDistribution.state=temp;
							actionNode.nextState=Merge(actionNode.nextState,stateDistribution);//Initialize actionNode.nextState
							
							}						
					}
					return actionNode.nextState;
				}
		}
		return null;
	}
	
	
	private StateDistribution Merge(StateDistribution result1,
			StateDistribution result2) {
		if(result1==null)
			return result2;
		else
		{	
			StateDistribution result3=new StateDistribution();
			result3.count=result1.count+result2.count;
			result3.prob=new double[result3.count];
			result3.state=new StateNode[result3.count];
			
			for(int i=0;i<result1.count;i++)
			{
				result3.prob[i]=result1.prob[i];
				result3.state[i]=result1.state[i];
			}
			for(int i=result1.count;i<result3.count;i++)
			{
				result3.prob[i]=result2.prob[i-result1.count];
				result3.state[i]=result2.state[i-result1.count];
			}
			return result3;
		}
	}

        
        private Result[] getAllNewControlledState(StateNode stateNode, ActionNode actionNode) {
	
		ControlledState temp=new ControlledState();
		temp.node=stateNode.controlledState.node;
		temp.difficulty=stateNode.controlledState.difficulty;
		temp.direction=stateNode.controlledState.direction;
		if(stateNode.stabilityState.stability==Stability.unstable)
		{
			Result[] result=new Result[1];
			result[0]=new Result(temp.toString(),1.0);
			return result;
		}
		else
		{
			Result[]result=new Result[2];
			
			if(stateNode.controlledState.difficulty==Difficulty.blocked&&stateNode.traffic==UncontrolledState.sparse)
			{
				result[0]=new Result(temp.toString(),1-p_success_blocked_sparse);
				result[1]=new Result(calculateNewControlledState(stateNode.controlledState,actionNode.action).toString(),p_success_blocked_sparse);
				return result; 
			}
			else
				if(stateNode.controlledState.difficulty==Difficulty.blocked&&stateNode.traffic==UncontrolledState.heavy)
				{
					result[0]=new Result(temp.toString(),1-p_success_blocked_heavy);
					result[1]=new Result(calculateNewControlledState(stateNode.controlledState,actionNode.action).toString(),p_success_blocked_heavy);
					return result; 				
				}
				else
					if(stateNode.controlledState.difficulty==Difficulty.difficult&&stateNode.traffic==UncontrolledState.sparse)
					{
						result[0]=new Result(temp.toString(),1-p_success_difficult_sparse);
						result[1]=new Result(calculateNewControlledState(stateNode.controlledState,actionNode.action).toString(),p_success_difficult_sparse);
						return result; 				
					}
					else
						if(stateNode.controlledState.difficulty==Difficulty.difficult&&stateNode.traffic==UncontrolledState.heavy)
						{
							result[0]=new Result(temp.toString(),1-p_success_difficult_heavy);
							result[1]=new Result(calculateNewControlledState(stateNode.controlledState,actionNode.action).toString(),p_success_difficult_heavy);
							return result; 				
						}
						else
							if(stateNode.controlledState.difficulty==Difficulty.free&&stateNode.traffic==UncontrolledState.sparse)
							{
								result[0]=new Result(temp.toString(),1-p_success_free_sparse);
								result[1]=new Result(calculateNewControlledState(stateNode.controlledState,actionNode.action).toString(),p_success_free_sparse);
								return result; 				
								}
							else
								if(stateNode.controlledState.difficulty==Difficulty.free&&stateNode.traffic==UncontrolledState.heavy)
								{
									result[0]=new Result(temp.toString(),1-p_success_free_heavy);
									result[1]=new Result(calculateNewControlledState(stateNode.controlledState,actionNode.action).toString(),p_success_free_heavy);
									return result; 				
								}
						
						
		}
		return null;
	}
        
public HashMap<String,ControlledState>newControlledStateHashMap;       
        
private ControlledState calculateNewControlledState(
			ControlledState controlledState, Action action) {
                if(newControlledStateHashMap.containsKey(controlledState.toString()+action.toString()))
                {
                    //System.out.println("Use calculateNewControlledState!");
                    ControlledState temp=newControlledStateHashMap.get(controlledState.toString()+action.toString());
                    //System.out.println(temp.nodeID);
                    //System.out.println(temp.node==null);
                    //System.out.println(temp.toString());
                    return temp;
                }
               // System.out.println("Why not optimize in calculateNewControlledState");
    
		ControlledState newControlledState=new ControlledState();
		newControlledState.difficulty=controlledState.difficulty;
		newControlledState.direction=controlledState.direction;
		newControlledState.node=controlledState.node;
		int debugflag=0;
//                if(controlledState.node==Start.controlledState.node)
//                {
//                    System.out.println(controlledState.toString());
//                    debugflag=1;
//                }
                if(action==Action.forward)
		{
                    //System.out.println("Orignial node:"+controlledState.node);
			switch (controlledState.direction)
			{
                            case first:
	                             OSMLink temp=(OSMLink)(controlledState.node.getOutLinks().get(0));
                                    // System.out.println("link: "+temp.toString());
                                      if(temp.isOneway()==true)
                                     newControlledState.node=(OSMNode) temp.getTarget();
                                     else
                                         newControlledState.node=(OSMNode)(temp.getTarget()==controlledState.node?temp.getSource():temp.getTarget());
                                     //System.out.println("New State:"+newControlledState.node+", old State"+controlledState.node);

                                      if(getDefaultForwardLink(newControlledState.node,temp)!=null)
                                         newControlledState.direction=getDefaultForwardLink(newControlledState.node,temp);
                                     else
                                     {                                      
                                       System.out.println("It is wired");
                                     }
                                     newControlledState.difficulty=GetDifficulty(newControlledState.node);
                                     break;
                            case second:
                                     temp=(OSMLink)(controlledState.node.getOutLinks().get(1));
                                     if(temp.isOneway()==true)
                                     newControlledState.node=(OSMNode) temp.getTarget();
                                     else
                                         newControlledState.node=(OSMNode)(temp.getTarget()==controlledState.node?temp.getSource():temp.getTarget());
                                     //System.out.println("New State:"+newControlledState.node+", old State"+controlledState.node);
                                     if(getDefaultForwardLink(newControlledState.node,temp)!=null)
                                         newControlledState.direction=getDefaultForwardLink(newControlledState.node,temp);
                                     else
                                     {
                                         System.out.println("it is wired");
                                     }
                                     newControlledState.difficulty=GetDifficulty(newControlledState.node);
                                break;
                            case third:
                                     temp=(OSMLink)(controlledState.node.getOutLinks().get(2));
                                      if(temp.isOneway()==true)
                                     newControlledState.node=(OSMNode) temp.getTarget();
                                     else
                                         newControlledState.node=(OSMNode)(temp.getTarget()==controlledState.node?temp.getSource():temp.getTarget());
                                     //System.out.println("New State:"+newControlledState.node+", old State"+controlledState.node);

                                     if(getDefaultForwardLink(newControlledState.node,temp)!=null)
                                         newControlledState.direction=getDefaultForwardLink(newControlledState.node,temp);
                                     else
                                     {
                                            System.out.println("it is wired");
                                     }
                                     newControlledState.difficulty=GetDifficulty(newControlledState.node);
                                break;
                            case fourth:
                                     temp=(OSMLink)(controlledState.node.getOutLinks().get(3));
                                      if(temp.isOneway()==true)
                                     newControlledState.node=(OSMNode) temp.getTarget();
                                     else
                                         newControlledState.node=(OSMNode)(temp.getTarget()==controlledState.node?temp.getSource():temp.getTarget());
                                     //System.out.println("New State:"+newControlledState.node+", old State"+controlledState.node);

                                     if(getDefaultForwardLink(newControlledState.node,temp)!=null)
                                        newControlledState.direction=getDefaultForwardLink(newControlledState.node,temp);
                                     else
                                     {
                                            System.out.println("it is wired");
                                     }
                                     newControlledState.difficulty=GetDifficulty(newControlledState.node);
                                break;
                                
                              case fifth:
                                     temp=(OSMLink)(controlledState.node.getOutLinks().get(4));
                                      if(temp.isOneway()==true)
                                     newControlledState.node=(OSMNode) temp.getTarget();
                                     else
                                         newControlledState.node=(OSMNode)(temp.getTarget()==controlledState.node?temp.getSource():temp.getTarget());
                                    // System.out.println("New State:"+newControlledState.node+", old State"+controlledState.node);

                                     if(getDefaultForwardLink(newControlledState.node,temp)!=null)
                                        newControlledState.direction=getDefaultForwardLink(newControlledState.node,temp);
                                     else
                                     {
                                        System.out.println("it is wired");
                                     }
                                     newControlledState.difficulty=GetDifficulty(newControlledState.node);
                                break;    
                                  
                                   case sixth:
                                     temp=(OSMLink)(controlledState.node.getOutLinks().get(5));
                                      if(temp.isOneway()==true)
                                     newControlledState.node=(OSMNode) temp.getTarget();
                                     else
                                         newControlledState.node=(OSMNode)(temp.getTarget()==controlledState.node?temp.getSource():temp.getTarget());
                                     //System.out.println("New State:"+newControlledState.node+", old State"+controlledState.node);

                                     if(getDefaultForwardLink(newControlledState.node,temp)!=null)
                                        newControlledState.direction=getDefaultForwardLink(newControlledState.node,temp);
                                     else
                                     {
                                        System.out.println("it is wired");
                                     }
                                     newControlledState.difficulty=GetDifficulty(newControlledState.node);
                                break;
                            
                           // or else, nothing will change. 
                                  
  		        }
                           if(debugflag==1)          
                                       System.out.println("does it changed after forward?: "+newControlledState.toString());
                   //     System.out.println("New node id:"+newControlledState.node);
		           newControlledStateHashMap.put(controlledState.toString()+action.toString(),newControlledState);
                           return newControlledState;		
		}
		
		if(action==Action.left)
		{
			switch (controlledState.direction)
			{
                            case first:
	                             OSMLink temp=(OSMLink)(controlledState.node.getOutLinks().get(0));
                                     if(getNextLeftLink(newControlledState.node,temp)!=null)
                                             newControlledState.direction=getNextLeftLink(newControlledState.node,temp);
                                     newControlledState.difficulty=GetDifficulty(newControlledState.node);
                                     break;
                            case second:
                                     temp=(OSMLink)(controlledState.node.getOutLinks().get(1));
                                     if(getNextLeftLink(newControlledState.node,temp)!=null)                                     
                                         newControlledState.direction=getNextLeftLink(newControlledState.node,temp);
                                     newControlledState.difficulty=GetDifficulty(newControlledState.node);
                                break;
                            case third:
                                     temp=(OSMLink)(controlledState.node.getOutLinks().get(2));
                                     if(getNextLeftLink(newControlledState.node,temp)!=null)                              
                                           newControlledState.direction=getNextLeftLink(newControlledState.node,temp);
                                     newControlledState.difficulty=GetDifficulty(newControlledState.node);
                                break;
                            case fourth:
                                     temp=(OSMLink)(controlledState.node.getOutLinks().get(3));
                                     if(getNextLeftLink(newControlledState.node,temp)!=null)                                     
                                           newControlledState.direction=getNextLeftLink(newControlledState.node,temp);
                                     newControlledState.difficulty=GetDifficulty(newControlledState.node);
                                break;
                                
                             case fifth:
                                     temp=(OSMLink)(controlledState.node.getOutLinks().get(4));
                                     if(getNextLeftLink(newControlledState.node,temp)!=null)                                     
                                           newControlledState.direction=getNextLeftLink(newControlledState.node,temp);
                                     newControlledState.difficulty=GetDifficulty(newControlledState.node);
                                break;
                             case sixth:
                                     temp=(OSMLink)(controlledState.node.getOutLinks().get(5));
                                     if(getNextLeftLink(newControlledState.node,temp)!=null)                                     
                                           newControlledState.direction=getNextLeftLink(newControlledState.node,temp);
                                     newControlledState.difficulty=GetDifficulty(newControlledState.node);
                                break;
                             case negativefirst:
                                  temp=(OSMLink)(controlledState.node.getInLinks().get(0));
                                     if(getNextLeftLink(newControlledState.node,temp)!=null)                                     
                                           newControlledState.direction=getNextLeftLink(newControlledState.node,temp);
                                     newControlledState.difficulty=GetDifficulty(newControlledState.node);
                                 break;
                             case negativesecond:
                                 temp=(OSMLink)(controlledState.node.getInLinks().get(1));
                                     if(getNextLeftLink(newControlledState.node,temp)!=null)                                     
                                           newControlledState.direction=getNextLeftLink(newControlledState.node,temp);
                                     newControlledState.difficulty=GetDifficulty(newControlledState.node);
                                 break;
                             case negativethird:
                                 temp=(OSMLink)(controlledState.node.getInLinks().get(2));
                                     if(getNextLeftLink(newControlledState.node,temp)!=null)                                     
                                           newControlledState.direction=getNextLeftLink(newControlledState.node,temp);
                                     newControlledState.difficulty=GetDifficulty(newControlledState.node);
                                 break;
                             case negativefourth:
                                 temp=(OSMLink)(controlledState.node.getInLinks().get(3));
                                     if(getNextLeftLink(newControlledState.node,temp)!=null)                                     
                                           newControlledState.direction=getNextLeftLink(newControlledState.node,temp);
                                     newControlledState.difficulty=GetDifficulty(newControlledState.node);
                                 break;
                             case negativefifth:
                                 temp=(OSMLink)(controlledState.node.getInLinks().get(4));
                                     if(getNextLeftLink(newControlledState.node,temp)!=null)                                     
                                           newControlledState.direction=getNextLeftLink(newControlledState.node,temp);
                                     newControlledState.difficulty=GetDifficulty(newControlledState.node);
                                 break;
                              case negativesixth:
                                 temp=(OSMLink)(controlledState.node.getInLinks().get(5));
                                     if(getNextLeftLink(newControlledState.node,temp)!=null)                                     
                                           newControlledState.direction=getNextLeftLink(newControlledState.node,temp);
                                     newControlledState.difficulty=GetDifficulty(newControlledState.node);
                                 break;    
	
			}
                           if(debugflag==1)          
                                       System.out.println("does it changed after turn left?: "+newControlledState.toString());
     			   newControlledStateHashMap.put(controlledState.toString()+action.toString(),newControlledState);
                           return newControlledState;
		}
		
		if(action==Action.right)
		{
			 
			switch (controlledState.direction)
			{
                            case first:
	                             OSMLink temp=(OSMLink)(controlledState.node.getOutLinks().get(0));
                                     if(getNextRightLink(newControlledState.node,temp)!=null)
                                           newControlledState.direction=getNextRightLink(newControlledState.node,temp);
                                     newControlledState.difficulty=GetDifficulty(newControlledState.node);
                                     break;
                            case second:
                                     temp=(OSMLink)(controlledState.node.getOutLinks().get(1));
                                     if(getNextRightLink(newControlledState.node,temp)!=null)
                                        newControlledState.direction=getNextRightLink(newControlledState.node,temp);
                                     newControlledState.difficulty=GetDifficulty(newControlledState.node);
                                break;
                            case third:
                                     temp=(OSMLink)(controlledState.node.getOutLinks().get(2));
                                     if(getNextRightLink(newControlledState.node,temp)!=null)
                                           newControlledState.direction=getNextRightLink(newControlledState.node,temp);
                                     newControlledState.difficulty=GetDifficulty(newControlledState.node);
                                break;
                            case fourth:
                                     temp=(OSMLink)(controlledState.node.getOutLinks().get(3));
                                     if(getNextRightLink(newControlledState.node,temp)!=null)
                                           newControlledState.direction=getNextRightLink(newControlledState.node,temp);
                                     newControlledState.difficulty=GetDifficulty(newControlledState.node);
                                break;
                                
                           case fifth:
                                     temp=(OSMLink)(controlledState.node.getOutLinks().get(4));
                                     if(getNextRightLink(newControlledState.node,temp)!=null)
                                           newControlledState.direction=getNextRightLink(newControlledState.node,temp);
                                     newControlledState.difficulty=GetDifficulty(newControlledState.node);
                                break;     
                            case sixth:
                                     temp=(OSMLink)(controlledState.node.getOutLinks().get(5));
                                     if(getNextRightLink(newControlledState.node,temp)!=null)
                                           newControlledState.direction=getNextRightLink(newControlledState.node,temp);
                                     newControlledState.difficulty=GetDifficulty(newControlledState.node);
                                break;      
                            case negativefirst:
                                 temp=(OSMLink)(controlledState.node.getInLinks().get(0));
                                     if(getNextRightLink(newControlledState.node,temp)!=null)
                                           newControlledState.direction=getNextRightLink(newControlledState.node,temp);
                                     newControlledState.difficulty=GetDifficulty(newControlledState.node);
                                break;
                           case negativesecond:
                                 temp=(OSMLink)(controlledState.node.getInLinks().get(1));
                                     if(getNextRightLink(newControlledState.node,temp)!=null)
                                           newControlledState.direction=getNextRightLink(newControlledState.node,temp);
                                     newControlledState.difficulty=GetDifficulty(newControlledState.node);
                                break;     
                           case negativethird:
                                 temp=(OSMLink)(controlledState.node.getInLinks().get(2));
                                     if(getNextRightLink(newControlledState.node,temp)!=null)
                                           newControlledState.direction=getNextRightLink(newControlledState.node,temp);
                                     newControlledState.difficulty=GetDifficulty(newControlledState.node);
                                break;                 
                           case negativefourth:
                                 temp=(OSMLink)(controlledState.node.getInLinks().get(3));
                                     if(getNextRightLink(newControlledState.node,temp)!=null)
                                           newControlledState.direction=getNextRightLink(newControlledState.node,temp);
                                     newControlledState.difficulty=GetDifficulty(newControlledState.node);
                                break;  
                           case negativefifth:
                                 temp=(OSMLink)(controlledState.node.getInLinks().get(4));
                                     if(getNextRightLink(newControlledState.node,temp)!=null)
                                           newControlledState.direction=getNextRightLink(newControlledState.node,temp);
                                     newControlledState.difficulty=GetDifficulty(newControlledState.node);
                                break;                                    
                           case negativesixth:
                                 temp=(OSMLink)(controlledState.node.getInLinks().get(5));
                                     if(getNextRightLink(newControlledState.node,temp)!=null)
                                           newControlledState.direction=getNextRightLink(newControlledState.node,temp);
                                     newControlledState.difficulty=GetDifficulty(newControlledState.node);
                                break;     
                        }
                           if(debugflag==1)          
                                       System.out.println("does it changed after turn right?: "+newControlledState.toString());
                           newControlledStateHashMap.put(controlledState.toString()+action.toString(),newControlledState);
                           return newControlledState;
		}   
                
                if(action==Action.reverse)
                {
                    switch (controlledState.direction)
                    {
                        case first:
	                             OSMLink temp=(OSMLink)(controlledState.node.getOutLinks().get(0));
                                     if(getReversedLink(newControlledState.node,temp)!=null)
                                           newControlledState.direction=(Direction) getReversedLink(newControlledState.node,temp);
                                     
                                     break;
                             case second:
                                     temp=(OSMLink)(controlledState.node.getOutLinks().get(1));
                                     if(getReversedLink(newControlledState.node,temp)!=null)
                                           newControlledState.direction=(Direction) getReversedLink(newControlledState.node,temp);
                                break;
                            case third:
                                     temp=(OSMLink)(controlledState.node.getOutLinks().get(2));
                                    if(getReversedLink(newControlledState.node,temp)!=null)
                                           newControlledState.direction=(Direction) getReversedLink(newControlledState.node,temp);
                                break;
                            case fourth:
                                     temp=(OSMLink)(controlledState.node.getOutLinks().get(3));
                                    if(getReversedLink(newControlledState.node,temp)!=null)
                                           newControlledState.direction=(Direction) getReversedLink(newControlledState.node,temp);
                                break;
                                
                           case fifth:
                                     temp=(OSMLink)(controlledState.node.getOutLinks().get(4));
                                     if(getReversedLink(newControlledState.node,temp)!=null)
                                           newControlledState.direction=(Direction) getReversedLink(newControlledState.node,temp);
                                break;     
                            case sixth:
                                     temp=(OSMLink)(controlledState.node.getOutLinks().get(5));
                                     if(getReversedLink(newControlledState.node,temp)!=null)
                                           newControlledState.direction=(Direction) getReversedLink(newControlledState.node,temp);
                                break;
                                
                            case negativefirst:
                                 temp=(OSMLink)(controlledState.node.getInLinks().get(0));
                                     if(getReversedLink(newControlledState.node,temp)!=null)
                                           newControlledState.direction=(Direction) getReversedLink(newControlledState.node,temp);
                                     
                                break;
                           case negativesecond:
                                 temp=(OSMLink)(controlledState.node.getInLinks().get(1));
                                     if(getReversedLink(newControlledState.node,temp)!=null)
                                           newControlledState.direction=(Direction) getReversedLink(newControlledState.node,temp);
                                break;     
                           case negativethird:
                                 temp=(OSMLink)(controlledState.node.getInLinks().get(2));
                                     if(getReversedLink(newControlledState.node,temp)!=null)
                                           newControlledState.direction=(Direction) getReversedLink(newControlledState.node,temp);
                                break;                 
                           case negativefourth:
                                 temp=(OSMLink)(controlledState.node.getInLinks().get(3));
                                     if(getReversedLink(newControlledState.node,temp)!=null)
                                           newControlledState.direction=(Direction) getReversedLink(newControlledState.node,temp);
                                break;  
                           case negativefifth:
                                 temp=(OSMLink)(controlledState.node.getInLinks().get(4));
                                     if(getReversedLink(newControlledState.node,temp)!=null)
                                           newControlledState.direction=(Direction) getReversedLink(newControlledState.node,temp);
                                break;                                    
                           case negativesixth:
                                 temp=(OSMLink)(controlledState.node.getInLinks().get(5));
                                     if(getReversedLink(newControlledState.node,temp)!=null)
                                           newControlledState.direction=(Direction) getReversedLink(newControlledState.node,temp);
                                break;     
                    }
                }
                newControlledStateHashMap.put(controlledState.toString()+action.toString(),newControlledState);
		return newControlledState;
	}

        
        /**
	 * this function return all possible stabilityState,actionNode is an new action,
	 * @param stateNode
	 * @param actionNode
	 * @return
	 */
		private Result[] getAllNewStability(StateNode stateNode, ActionNode actionNode) {

			StabilityState temp=new StabilityState();	
			
			double factor=0.0;
			if((stateNode.stabilityState.actionBeExecuting==Action.left||stateNode.stabilityState.actionBeExecuting==Action.right)&&stateNode.stabilityState.actionBeExecutingActor==Actor.SAU)
				{
				factor=factor_difficult_to_turn_for_SAU;
				//System.out.println("factor");
				}
			else
				factor=1.0;
			
			if(stateNode.stabilityState.stability==Stability.unstable)
			{
				Result[] result=new Result[2];
				
				temp.actionBeExecuting=stateNode.stabilityState.actionBeExecuting;
				temp.actionBeExecutingActor=stateNode.stabilityState.actionBeExecutingActor;
				temp.stability=Stability.unstable;
				temp.actor=stateNode.stabilityState.actor;				
				if(stateNode.controlledState.difficulty==Difficulty.free)
				{
					switch(stateNode.traffic)
					{
					case sparse:
 	 	 				    result[0]=new Result(temp.toString(),1-p_free_sparse/factor);
							temp.stability=Stability.stable;
						    result[1]=new Result(temp.toString(),p_free_sparse/factor);//success
						break;
					case heavy:	
					    result[0]=new Result(temp.toString(),1-p_free_heavy/factor);
						temp.stability=Stability.stable;
					    result[1]=new Result(temp.toString(),p_free_heavy/factor);//success
						break;
					}
					return result;
				}
				// for the situation when controlledState.difficulty==blocked
					if(stateNode.controlledState.difficulty==Difficulty.blocked)
					{
						if(stateNode.stabilityState.actor==Actor.driver)
						{
							switch(stateNode.traffic)
							{
							case sparse:
								result[0]=new Result(temp.toString(),1-p_driver_blocked_sparse);
								temp.stability=Stability.stable;
								result[1]=new Result(temp.toString(),p_driver_blocked_sparse);//success
								break;
							case heavy:	
								result[0]=new Result(temp.toString(),1-p_driver_blocked_heavy);
								temp.stability=Stability.stable;
								result[1]=new Result(temp.toString(),p_driver_blocked_sparse);//success
								break;
							}
							return result;
						}
						if(stateNode.stabilityState.actor==Actor.SAU)
						{
							switch(stateNode.traffic)
							{
							case sparse:
								result[0]=new Result(temp.toString(),1-(p_system_blocked_sparse/factor));
								temp.stability=Stability.stable;
								result[1]=new Result(temp.toString(),p_system_blocked_sparse/factor);//success
								break;
							case heavy:	
									result[0]=new Result(temp.toString(),1-(p_system_blocked_heavy/factor));
									temp.stability=Stability.stable;
									result[1]=new Result(temp.toString(),p_system_blocked_heavy/factor);//success
								break;
							}
						}
						return result;
					}
					
					// for the situation when controlledState.difficulty==difficult
					if(stateNode.controlledState.difficulty==Difficulty.difficult)
					{
						if(stateNode.stabilityState.actor==Actor.driver)
						{
							switch(stateNode.traffic)
							{
							case sparse:
									result[0]=new Result(temp.toString(),1-p_driver_difficult_sparse);
									temp.stability=Stability.stable;
									result[1]=new Result(temp.toString(),p_driver_difficult_sparse);//success
								break;
							case heavy:	
								result[0]=new Result(temp.toString(),1-p_driver_difficult_heavy);
								temp.stability=Stability.stable;
								result[1]=new Result(temp.toString(),p_driver_difficult_heavy);//success								break;
							}
							return result;
						}
						if(stateNode.stabilityState.actor==Actor.SAU)
						{
							switch(stateNode.traffic)
							{
							case sparse:
								result[0]=new Result(temp.toString(),1-(p_system_difficult_sparse/factor));
								temp.stability=Stability.stable;
								result[1]=new Result(temp.toString(),p_system_difficult_sparse/factor);//success
								break;
							case heavy:	
									result[0]=new Result(temp.toString(),1-(p_system_difficult_heavy/factor));
									temp.stability=Stability.stable;
									result[1]=new Result(temp.toString(),p_system_difficult_heavy/factor);//success
								break;
							}
							return result;
						}
					}
					return result;
			}
			else//when the current stability is stable
			{
				Result[] result=new Result[1];
				temp.actionBeExecuting=actionNode.action;
				temp.actionBeExecutingActor=actionNode.actor;
				temp.actor=actionNode.actor;
				temp.stability=Stability.unstable;
				result[0]=new Result(temp.toString(),1.0);
				return result;
			}
		}
        
		class Result {
		String temp;
		double probability;
		public Result()
		{
			
		}
		public Result(String temp,double probability)
		{
			this.temp=temp;
			this.probability=probability;
		}
		public Result(UncontrolledState heavy, double d) {
			this.temp=heavy.toString();
			this.probability=d;
		}
	}
                
                
        	private Result[] getAllNextTraffic(UncontrolledState traffic) {
		Result[] result=new Result[2];
		if(traffic==UncontrolledState.heavy)
		{
			result[0]=new Result(UncontrolledState.heavy,1-transition_heavy_sparse);
			result[1]=new Result(UncontrolledState.sparse,transition_heavy_sparse);
		}
		else//sparse
		{
			result[0]=new Result(UncontrolledState.sparse,1-transition_sparse_heavy);
			result[1]=new Result(UncontrolledState.heavy,transition_sparse_heavy);			
		}
		return result;
	}

                
        private Result[] getAllNextDriverState(StateNode stateNode,
		ActionNode actionNode,int newError) {
	
	 Error error=null;
         int errorMade;
        EngagementLevel engagement=null;
        Result result[];
        	     
                  double p=1.0;  
						  error=Error.middle;
                                                  errorMade=stateNode.driverState.errorMade+newError;
                                                  
                                      if(errorMade<MaximumErrorNumber)            
                                      {          
                                          result=new Result[2];
        	     for(int i=0;i<2;i++)
        	    	 result[i]=new Result();
						  if(actionNode.msg==Message.increase)//
						  {
								if(stateNode.driverState.engagement==EngagementLevel.notEngaged)
								{
									double temp;
									engagement=EngagementLevel.engaged;
									temp=p*MsgSuccessRate;
									result[0].probability=temp;
									result[0].temp=("|"+error+engagement+errorMade+"|");
									
                                                                        errorMade++;
									engagement=EngagementLevel.notEngaged;
									temp=p*(1-MsgSuccessRate);
									result[1].probability=temp;
									result[1].temp=("|"+error+engagement+errorMade+"|");
									
								}
						  }
						  else if(actionNode.msg==Message.nop)
						  {
								if(stateNode.driverState.engagement==EngagementLevel.notEngaged)
								{
									double temp;
									engagement=EngagementLevel.notEngaged;
									temp=p*MsgSuccessRate;
									result[0].probability=temp;
									result[0].temp=("|"+error+engagement+errorMade+"|");
									
                                                                        errorMade++;
									engagement=EngagementLevel.engaged;
									temp=p*(1-MsgSuccessRate);
									result[1].probability=temp;
									result[1].temp=("|"+error+engagement+errorMade+"|");
									
								}
								else// engaged
								{
									double temp;
									engagement=EngagementLevel.engaged;
									temp=p*MsgSuccessRate;
									result[0].probability=temp;
									result[0].temp=("|"+error+engagement+errorMade+"|");
									
                                                                        errorMade++;
									engagement=EngagementLevel.notEngaged;
									temp=p*(1-MsgSuccessRate);
									result[1].probability=temp;
									result[1].temp=("|"+error+engagement+errorMade+"|");
								}
							  
						  }
						  else// Msg==decrease
						  {
								if(stateNode.driverState.engagement==EngagementLevel.engaged)
								{
									double temp;
									engagement=EngagementLevel.notEngaged;
									temp=p*MsgSuccessRate;
									result[0].probability=temp;
									result[0].temp=("|"+error+engagement+errorMade+"|");
									
                                                                        errorMade++;
									engagement=EngagementLevel.engaged;
									temp=p*(1-MsgSuccessRate);
									result[1].probability=temp;
									result[1].temp=("|"+error+engagement+errorMade+"|");
									
								}
							  
						  }
                                      }
                                      else  //engagement level can have error
                                      {
                                          result=new Result[1];
                                          result[0]=new Result();
                                            if(actionNode.msg==Message.increase)//
						  {
								if(stateNode.driverState.engagement==EngagementLevel.notEngaged)
								{
									double temp;
									engagement=EngagementLevel.engaged;
									temp=p*MsgSuccessRate;
									result[0].probability=temp;
									result[0].temp=("|"+error+engagement+errorMade+"|");
									
								}
						  }
						  else if(actionNode.msg==Message.nop)
						  {
								if(stateNode.driverState.engagement==EngagementLevel.notEngaged)
								{
									double temp;
									engagement=EngagementLevel.notEngaged;
									temp=p*MsgSuccessRate;
									result[0].probability=temp;
									result[0].temp=("|"+error+engagement+errorMade+"|");
								}
								else// engaged
								{
									double temp;
									engagement=EngagementLevel.engaged;
									temp=p*MsgSuccessRate;
									result[0].probability=temp;
									result[0].temp=("|"+error+engagement+errorMade+"|");									
								}
							  
						  }
						  else// Msg==decrease
						  {
								if(stateNode.driverState.engagement==EngagementLevel.engaged)
								{
									double temp;
									engagement=EngagementLevel.notEngaged;
									temp=p*MsgSuccessRate;
									result[0].probability=temp;
									result[0].temp=("|"+error+engagement+errorMade+"|");
																		
								}
							  
						  }
                                      }
		return result;
}
        
	/**
	 * Initialize StateNode.action, when Engagement Level is 0, then no action is allowed for "Driver", and Msg is controlled by System
	 * @param actionNode
	 * @return
	 */
	public ActionNode[] InitializeStateNode(StateNode stateNode)
	{
		if(stateNode.stabilityState.stability==Stability.unstable)
		{
			ActionNode[] temp=new ActionNode[2];
			ActionNode node1=new ActionNode();
			node1.actor=stateNode.stabilityState.actionBeExecutingActor;
			node1.msg=Message.nop;
			node1.action=Action.continued;
			node1.prevState=stateNode;
                        if(node1.actor==Actor.SAU)
			     node1.cost=COST_SAU_MSG_NOP_CONTINUED;
			else
				node1.cost=COST_DRIVER_MSG_NOP_CONTINUED;
			temp[0]=node1;
                        
                        ActionNode node2=new ActionNode();
			node2.actor=stateNode.stabilityState.actionBeExecutingActor;
			node2.action=Action.continued;
			node2.prevState=stateNode;
                        
                        if(stateNode.driverState.engagement==EngagementLevel.engaged)
                        {
                           node2.msg=Message.decrease;
                            if(node2.actor==Actor.SAU)
			        node2.cost=COST_SAU_MSG_DECREASE_CONTINUED;
			    else
				node2.cost=COST_DRIVER_MSG_NOP_CONTINUED;

                        }
                        else
                        {
                            node2.msg=Message.increase;
                            if(node2.actor==Actor.SAU)
			        node2.cost=NOTENGAGED_COST_SAU_MSG_INCREASE_CONTINUED;
			    else
				node2.cost=COST_DRIVER_MSG_NOP_CONTINUED;
                        }

			temp[1]=node2;                        
                        
                                               
			return temp;
		}
		
		if(stateNode.driverState.engagement==EngagementLevel.engaged)
		{
			int index=0;
			ActionNode[] temp=new ActionNode[15];
			for(Action action:Action.values())
			{
				ActionNode node1=new ActionNode();
				node1.actor=Actor.SAU;
				node1.msg=Message.decrease;
				node1.action=action;
				node1.prevState=stateNode;
				if(action==Action.continued)
					node1.cost=COST_SAU_MSG_DECREASE_CONTINUED;
				else
					node1.cost=COST_SAU_MSG_DECREASE_NON_CONTINUED;
					
				temp[index]=node1;
				index++;
				
				ActionNode node2=new ActionNode();
				node2.actor=Actor.SAU;
				node2.msg=Message.nop;
				node2.action=action;
				node2.prevState=stateNode;
				if(action==Action.continued)
					node2.cost=COST_SAU_MSG_NOP_CONTINUED;
				else
					node2.cost=COST_SAU_MSG_NOP_NON_CONTINUED;
				temp[index]=node2;
				index++;
				
				ActionNode node3=new ActionNode();
				node3.actor=Actor.driver;
				node3.msg=Message.nop;
				node3.action=action;
				node3.prevState=stateNode;
				if(action==Action.continued)
					node3.cost=COST_DRIVER_MSG_NOP_CONTINUED;
				else
					node3.cost=COST_DRIVER_MSG_NOP_NON_CONTINUED;
				temp[index]=node3;
				index++;
			}
			//System.out.println(index);
			stateNode.action=temp;	
			return temp;
		}
		else//EngagementLevel is notEngaged,so only System take action
		{
			int index=0;
			ActionNode[] temp=new ActionNode[10];
			for(Action action:Action.values())
			{
				ActionNode node1=new ActionNode();
				node1.actor=Actor.SAU;
				node1.msg=Message.increase;
				node1.action=action;
				node1.prevState=stateNode;
				if(action==Action.continued)
					node1.cost=NOTENGAGED_COST_SAU_MSG_INCREASE_CONTINUED;
				else
					node1.cost=NOTENGAGED_COST_SAU_MSG_INCREASE_NON_CONTINUED;
				temp[index]=node1;
				index++;
				
				ActionNode node2=new ActionNode();
				node2.actor=Actor.SAU;
				node2.msg=Message.nop;
				node2.action=action;
				node2.prevState=stateNode;
				if(action==Action.continued)
					node2.cost=NOTENGAGED_COST_SAU_MSG_NOP_CONTINUED;
				else
					node2.cost=NOTENGAGED_COST_SAU_MSG_NOP_NON_CONTINUED;
				temp[index]=node2;
				index++;				
			}
			stateNode.action=temp;
			return temp;
		}
		
	}
        
        public void ValueIterationOnly(int MaxIter) {
		  gStartTime=System.currentTimeMillis();
		  int                       Iter;
		  countValueIteration=0;
		  double                    diff, maxdiff, /* Bellman residual */
		                            error;
		  
		  for (Iter=0; Iter < MaxIter; Iter++) {
			//System.out.println("Iteration in value Iteration "+Iter);
			  maxdiff = 0.0;
		    Iterator<Entry<String, StateNode>> iter = allStateList.entrySet().iterator(); 
		    while (iter.hasNext()) {
		    Map.Entry entry = iter.next(); 
		    StateNode stateNode =   ((Entry<String, StateNode>) entry).getValue();		    	
		      if (stateNode.isTerminal==false) { 
			diff = BackupForValueIteration(stateNode);
			countValueIteration++;
			if (diff > maxdiff)
			  maxdiff = diff;
		      }		 
		      
		    }
		    if ( gMethod == "vi" ) {
			error = (maxdiff * gDiscount)/(1.0 - gDiscount);
			    
			    //	System.out.print("\n" +Iteration+"( "+(float)(System.currentTimeMillis()-gStartTime)/1000+" seconds), stateValue:"+Start.stateValue);
		      	System.out.println("Error bound in iteration "+Iter+": "+ error);		     
		      if ( error <= gEpsilon )
			return;
		      
		    }
		
		  }		
		  System.out.println("Iteration for:"+Iter);
	}
        
        private double BackupForValueIteration(StateNode state) {

		  double                    oldStateValue, gCost,StateValue=0,bestStateValue=-100000,temp=0;

		  ActionNode        actionNode, bestAction=null;
		  oldStateValue=state.stateValue;
		  /* Find action that minimizes expected cost */
                  //System.out.println(state.toString());
		  for (int j=0;j<state.action.length;j++)
		       {
		    actionNode = state.action[j];
                   // System.out.print("action: "+actionNode.action);
                   // System.out.print(" ,actor: "+actionNode.actor);
                    //System.out.print(", cost:"+actionNode.cost);
		    gCost = -actionNode.cost;
		    StateValue=0;
		    for (int i=0;i<actionNode.nextState.count;i++) {
		      StateValue += gDiscount * actionNode.nextState.prob[i] * actionNode.nextState.state[i].stateValue;
//		    if(actionNode.nextState.state[i].isTerminal==true)
//                    {
//                        System.out.println("we can fint the termial!");
//                        System.out.println(actionNode.toString());
//                        for(int p=0;p<actionNode.nextState.count;p++)
//                        {
//                            System.out.println("Prob= "+actionNode.nextState.prob[p]+", Value= "+actionNode.nextState.state[p].stateValue);                            
//                        }
//                        
//                        //System.out.println(StateValue);
//                    }
                     
                      
                    }
		    temp = gCost + StateValue;
                    //System.out.println(" ,value:"+temp);
		    if (temp > bestStateValue) { /* or (fCost < bestfCost) */
		    	bestStateValue = temp;     
		        bestAction = actionNode;/*update the bestAction at last, while in the policy iteration, it is crucial*/
		    }
		  }/*The value of a state is the Value of the best action which equals to sum of the probability multiply the value of next state*/
		  
		    state.stateValue = bestStateValue;
		    state.bestAction = bestAction;
		
//		    if(state.controlledState.x<4&&state.controlledState.y<4)
//System.out.println("back up state node : "+state.stateNo+state.toString()+" bestStateValue :"+bestStateValue+" oldStateValue :"+oldStateValue+", bestAction: "+bestAction);

		    return(bestStateValue - oldStateValue); /* or (bestfCost - oldfCost) */
	}
        
        public void ValueIterationForTimeToGoal(int MaxIter) {

		  int                       Iter;
		  double                    diff, maxdiff, /* Bellman residual */
		                            error;
               	   Vector<StateNode> selectedStateList= new Vector<StateNode>();		 
                     
		    Iterator<Entry<String, StateNode>> iter2 = allStateList.entrySet().iterator(); 
		    while (iter2.hasNext()) 
                    {
                       Map.Entry entry = iter2.next(); 
		       StateNode stateNode =   ((Entry<String, StateNode>) entry).getValue();
                       if(stateNode.stabilityState.stability==Stability.stable&&stateNode.bestAction!=null&&stateNode.bestAction.nextState.count!=0)
                       {
                           selectedStateList.add(stateNode);
                       }
                    }
                  



		  for(StateNode s:selectedStateList)
		  {
			  s.calculateExpectedTimeToRestoreStable();
			  s.calculateExpectedPercentTimeOfSAUInControl();
			  s.calculateExpectedTimeToTransferOfControl();
		  }
		  System.out.println("Begin to ValueIteration For Start.timeToGoal");
		  for (Iter=0; Iter < MaxIter; Iter++) {

			  maxdiff = 0.0;
		    Iterator<StateNode> iter = selectedStateList.iterator(); 
		    while (iter.hasNext()) {
		    	StateNode stateNode =  iter.next();
		      if (stateNode.isTerminal==false) { 
			diff = BackupForTimeToGoal(stateNode);
			if (diff > maxdiff)
			  maxdiff = diff;
		      }
		    }
		    if ( gMethod == "vi" ) {
			error = (maxdiff * gDiscount)/(1.0 - gDiscount);
			    
		      	//System.out.println("Error bound: "+ error);		     
		      if ( error <= 0.00001 )
			break;
		    }
		  }		
		System.out.println("Start.timeToGoal : "+Start.timeToGoal);
		System.out.println("Begin to calcuate the precent of SAU contorl time....");
		
		  for (Iter=0; Iter < MaxIter; Iter++) {

			  maxdiff = 0.0;
		    Iterator<StateNode> iter = selectedStateList.iterator(); 
		    while (iter.hasNext()) {
		    	StateNode stateNode =  iter.next();
		      if (stateNode.isTerminal==false) { 
			diff = BackupForPercentOfTimeSAU(stateNode);
			if (diff > maxdiff)
			  maxdiff = diff;
		      }
		    }
		    if ( gMethod == "vi" ) {
			error = (maxdiff * gDiscount)/(1.0 - gDiscount);
			    
		      	//System.out.println("Error bound: "+ error);		     
		      if ( error <= 0.00001 )
			break;
		    }
		  }
		  System.out.println("Start.percentOfTimeSAU : "+Start.percentOfTimeSAU);
		  
		  System.out.println("Begin to calcuate the precent of Transfer of Control....");
		
		  for (Iter=0; Iter < MaxIter; Iter++) {
			  maxdiff = 0.0;
		    Iterator<StateNode> iter = selectedStateList.iterator(); 
		    while (iter.hasNext()) {
		    	StateNode stateNode =  iter.next();
		      if (stateNode.isTerminal==false) { 
			diff = BackupForTimeUntilNextTransferOfControl(stateNode);
			if (diff > maxdiff)
			  maxdiff = diff;
		      }
		    }
		    if ( gMethod == "vi" ) {
			error = (maxdiff * gDiscount)/(1.0 - gDiscount);    
		      	//System.out.println("Error bound: "+ error);		     
		      if ( error <= gEpsilon*0.001 )
			break;
		    }
		  }
		  System.out.println("Start.timeToTransferOfControl : "+Start.timeToTransferOfControl);  

	}
        
        
                public void ValueIterationForTimeToGoalLAO(int MaxIter) {

		  int                       Iter;
		  double                    diff, maxdiff, /* Bellman residual */
		                            error;
               	   Vector<StateNode> selectedStateList= new Vector<StateNode>();		 
                   Vector<StateNode> tempList=new Vector<StateNode>();  
                   tempList.add(Start);
		    while (tempList.isEmpty()==false) 
                    {
                           StateNode temp=tempList.remove(0);
                           StateNode temp0=temp.getNextStableState(true);
                           if(selectedStateList.contains(temp0)==false)
                           {
                              selectedStateList.add(temp0);
                              tempList.add(temp0);
                              StateNode temp1=new StateNode();
                              temp1.stabilityState=temp0.stabilityState;
                              temp1.controlledState=temp0.controlledState;
                              temp1.driverState=temp0.driverState;
                              temp1.traffic=(temp0.traffic==UncontrolledState.sparse?UncontrolledState.heavy:UncontrolledState.sparse);
                              temp1=allStateList.get(temp1.toString());
                              selectedStateList.add(temp1);
                              tempList.add(temp1);
                           }

                           
                           StateNode temp2=temp.getNextStableState(false);        
                             if(selectedStateList.contains(temp2)==false)
                           {
                              selectedStateList.add(temp2);
                              tempList.add(temp2);
                              StateNode temp1=new StateNode();
                              temp1.stabilityState=temp2.stabilityState;
                              temp1.controlledState=temp2.controlledState;
                              temp1.driverState=temp2.driverState;
                              temp1.traffic=(temp2.traffic==UncontrolledState.sparse?UncontrolledState.heavy:UncontrolledState.sparse);
                              temp1=allStateList.get(temp1.toString());
                              selectedStateList.add(temp1);
                              tempList.add(temp1);
                           }                   
                    }
                  



		  for(StateNode s:selectedStateList)
		  {
			  s.calculateExpectedTimeToRestoreStable();
			  s.calculateExpectedPercentTimeOfSAUInControl();
			  s.calculateExpectedTimeToTransferOfControl();
		  }
		  System.out.println("Begin to ValueIteration For Start.timeToGoal");
		  for (Iter=0; Iter < MaxIter; Iter++) {

			  maxdiff = 0.0;
		    Iterator<StateNode> iter = selectedStateList.iterator(); 
		    while (iter.hasNext()) {
		    	StateNode stateNode =  iter.next();
		      if (stateNode.isTerminal==false) { 
			diff = BackupForTimeToGoal(stateNode);
			if (diff > maxdiff)
			  maxdiff = diff;
		      }
		    }
		    if ( gMethod == "vi" ) {
			error = (maxdiff * gDiscount)/(1.0 - gDiscount);
			    
		      	//System.out.println("Error bound: "+ error);		     
		      if ( error <= 0.00001 )
			break;
		    }
		  }		
		System.out.println("Start.timeToGoal : "+Start.timeToGoal);
		System.out.println("Begin to calcuate the precent of SAU contorl time....");
		
		  for (Iter=0; Iter < MaxIter; Iter++) {

			  maxdiff = 0.0;
		    Iterator<StateNode> iter = selectedStateList.iterator(); 
		    while (iter.hasNext()) {
		    	StateNode stateNode =  iter.next();
		      if (stateNode.isTerminal==false) { 
			diff = BackupForPercentOfTimeSAU(stateNode);
			if (diff > maxdiff)
			  maxdiff = diff;
		      }
		    }
		    if ( gMethod == "vi" ) {
			error = (maxdiff * gDiscount)/(1.0 - gDiscount);
			    
		      	//System.out.println("Error bound: "+ error);		     
		      if ( error <= 0.00001 )
			break;
		    }
		  }
		  System.out.println("Start.percentOfTimeSAU : "+Start.percentOfTimeSAU);
		  
		  System.out.println("Begin to calcuate the precent of Transfer of Control....");
		
		  for (Iter=0; Iter < MaxIter; Iter++) {
			  maxdiff = 0.0;
		    Iterator<StateNode> iter = selectedStateList.iterator(); 
		    while (iter.hasNext()) {
		    	StateNode stateNode =  iter.next();
		      if (stateNode.isTerminal==false) { 
			diff = BackupForTimeUntilNextTransferOfControl(stateNode);
			if (diff > maxdiff)
			  maxdiff = diff;
		      }
		    }
		    if ( gMethod == "vi" ) {
			error = (maxdiff * gDiscount)/(1.0 - gDiscount);    
		      	//System.out.println("Error bound: "+ error);		     
		      if ( error <= gEpsilon*0.001 )
			break;
		    }
		  }
		  System.out.println("Start.timeToTransferOfControl : "+Start.timeToTransferOfControl);  

	}
        
        	private double BackupForTimeToGoal(StateNode s) {

		  double                    oldValue;		           
          
    	  StateNode result=s.getNextStableState(false);
    	  StateNode temp0=new StateNode();
		  temp0.controlledState=result.controlledState;
		  temp0.driverState=result.driverState;
		  temp0.stabilityState=result.stabilityState;
		  temp0.traffic=s.traffic;
		  temp0=allStateList.get(temp0.toString());
    	  
    	  
    	  StateNode temp1=new StateNode();
		  temp1.controlledState=result.controlledState;
		  temp1.driverState=result.driverState;
		  temp1.stabilityState=result.stabilityState;
		  temp1.traffic=(s.traffic==UncontrolledState.sparse?UncontrolledState.heavy:UncontrolledState.sparse);
		  temp1=allStateList.get(temp1.toString());

		  StateNode stateNode=s.getNextStableState(true);
		  StateNode temp2=new StateNode();
		  temp2.controlledState=stateNode.controlledState;
		  temp2.driverState=stateNode.driverState;
		  temp2.stabilityState=stateNode.stabilityState;
		  temp2.traffic=s.traffic;
		  temp2=allStateList.get(temp2.toString());  
				  
		  StateNode temp3=new StateNode();
		  temp3.controlledState=stateNode.controlledState;
		  temp3.driverState=stateNode.driverState;
		  temp3.stabilityState=stateNode.stabilityState;
		  temp3.traffic=temp1.traffic;
		  temp3=allStateList.get(temp3.toString());  
		  
		  double P[]=new double[4];
		  P=getParamterForFourSituation(s);
		  
		  oldValue=s.timeToGoal;
		  
		  s.timeToGoal=s.timeToNextStability+P[0]*result.timeToGoal+P[1]*(temp1.timeToGoal)+P[2]*(temp2.timeToGoal)+P[3]*(temp3.timeToGoal);
		  
		  return s.timeToGoal-oldValue;
	}
        
        
        private void ReadConfigureFile() throws IOException {
		BufferedReader file = new BufferedReader(new FileReader(gInputConfigureFileName));
		String temp = file.readLine();
                gMethod=temp;
                
		temp=file.readLine();
		String[] arrayTemp=temp.split(" ");
		gDiscount=Double.parseDouble(arrayTemp[0]);

		temp = file.readLine();
		arrayTemp=temp.split(" ");
		p_success_blocked_sparse=Double.parseDouble(arrayTemp[0]);
		
		
		temp = file.readLine();
		arrayTemp=temp.split(" ");
		p_success_blocked_heavy=Double.parseDouble(arrayTemp[0]);
		
		
		temp = file.readLine();
		arrayTemp=temp.split(" ");
		p_success_difficult_sparse=Double.parseDouble(arrayTemp[0]);
		
		temp = file.readLine();
		arrayTemp=temp.split(" ");
		p_success_difficult_heavy=Double.parseDouble(arrayTemp[0]);
		
		temp = file.readLine();
		arrayTemp=temp.split(" ");
		p_success_free_sparse=Double.parseDouble(arrayTemp[0]);
		
		temp = file.readLine();
		arrayTemp=temp.split(" ");
		 p_success_free_heavy=Double.parseDouble(arrayTemp[0]);
		
		temp = file.readLine();
		arrayTemp=temp.split(" ");
		transition_sparse_heavy=Double.parseDouble(arrayTemp[0]);
		
		temp = file.readLine();
		arrayTemp=temp.split(" ");
		transition_heavy_sparse=Double.parseDouble(arrayTemp[0]);
		
		temp = file.readLine();
		arrayTemp=temp.split(" ");
		p_free_sparse=Double.parseDouble(arrayTemp[0]);
		
		temp = file.readLine();
		arrayTemp=temp.split(" ");
		p_free_heavy=Double.parseDouble(arrayTemp[0]);
		
		temp = file.readLine();
		arrayTemp=temp.split(" ");
		p_driver_difficult_sparse=Double.parseDouble(arrayTemp[0]);
		
		temp = file.readLine();
		arrayTemp=temp.split(" ");
		p_driver_difficult_heavy=Double.parseDouble(arrayTemp[0]);
		
		temp = file.readLine();
		arrayTemp=temp.split(" ");
		p_driver_blocked_sparse=Double.parseDouble(arrayTemp[0]);
		
		temp = file.readLine();
		arrayTemp=temp.split(" ");
		p_driver_blocked_heavy=Double.parseDouble(arrayTemp[0]);		
		
		temp = file.readLine();
		arrayTemp=temp.split(" ");
		p_system_difficult_sparse=Double.parseDouble(arrayTemp[0]);	
		
		temp = file.readLine();
		arrayTemp=temp.split(" ");
		p_system_difficult_heavy=Double.parseDouble(arrayTemp[0]);	
		
		temp = file.readLine();
		arrayTemp=temp.split(" ");
		p_system_blocked_sparse=Double.parseDouble(arrayTemp[0]);	
		
		temp = file.readLine();
		arrayTemp=temp.split(" ");
		p_system_blocked_heavy=Double.parseDouble(arrayTemp[0]);	
		
		
		temp = file.readLine();
		arrayTemp=temp.split(" ");
		factor_difficult_to_turn_for_SAU=Double.parseDouble(arrayTemp[0]);
		
                temp = file.readLine();
		arrayTemp=temp.split(" ");
		COST_SAU_MSG_DECREASE_CONTINUED=Double.parseDouble(arrayTemp[0]);
		
		temp = file.readLine();
		arrayTemp=temp.split(" ");
		COST_SAU_MSG_DECREASE_NON_CONTINUED=Double.parseDouble(arrayTemp[0]);
		
		temp = file.readLine();
		arrayTemp=temp.split(" ");
		COST_SAU_MSG_NOP_CONTINUED=Double.parseDouble(arrayTemp[0]);
		
		
		temp = file.readLine();
		arrayTemp=temp.split(" ");
		COST_SAU_MSG_NOP_NON_CONTINUED=Double.parseDouble(arrayTemp[0]);
				
		
		temp = file.readLine();
		arrayTemp=temp.split(" ");
		NOTENGAGED_COST_SAU_MSG_NOP_CONTINUED=Double.parseDouble(arrayTemp[0]);

		temp = file.readLine();
		arrayTemp=temp.split(" ");
		NOTENGAGED_COST_SAU_MSG_NOP_NON_CONTINUED=Double.parseDouble(arrayTemp[0]);
		
		temp = file.readLine();
		arrayTemp=temp.split(" ");
		NOTENGAGED_COST_SAU_MSG_INCREASE_CONTINUED=Double.parseDouble(arrayTemp[0]);
		
		temp = file.readLine();
		arrayTemp=temp.split(" ");
		NOTENGAGED_COST_SAU_MSG_INCREASE_NON_CONTINUED=Double.parseDouble(arrayTemp[0]);
		
		temp = file.readLine();
		arrayTemp=temp.split(" ");
		COST_DRIVER_MSG_NOP_CONTINUED=Double.parseDouble(arrayTemp[0]);

		temp = file.readLine();
		arrayTemp=temp.split(" ");
		COST_DRIVER_MSG_NOP_NON_CONTINUED=Double.parseDouble(arrayTemp[0]);
	}
        
        	public void LAOstar() {
		
		  Vector<StateNode> AncestorList;
		  StateNode node;
                  gStartTime=System.currentTimeMillis();

		  NumExpandedStates = 0;
		  /* First expand nodes along most likely path to goal,using DFS */
		  NumExpandedStatesIter = 0;
		  Stack<StateNode> stateStack=new Stack<StateNode>();
		  stateStack.add(Start);
		  boolean reachGoal=false;
		  while(stateStack.isEmpty()==false&&reachGoal==false)
		  {
			  node=stateStack.pop();// pop up the first node of the stack
		      NumExpandedStatesIter++;
		      NumExpandedStates++;
			  if(node.Expanded<1)
				  BackupForLAO(node);
			  else
				  continue;
		      //System.out.print("\nExpand node "+ node.stateNo);
			  node.Expanded=1;
                          if(node.bestAction==null)
                          {
                              System.out.println("No path Exist! Those nodes that appears to be connected may not be connected!");
                              System.out.println(node.toString());
                              continue;
                          }
                          
			  for(int i=0;i<node.bestAction.nextState.count;i++)
			  {
				  if(node.bestAction.nextState.state[i].isTerminal==true)
				  {
					  reachGoal=true;
					  break;
				  }
				  else
					  if(node.bestAction.nextState.state[i].Expanded<1&&node.bestAction.nextState.state[i].isInStack==false)
					  {
						  stateStack.add(node.bestAction.nextState.state[i]);//add the children into the stack
						  node.bestAction.nextState.state[i].isInStack=true;
					  }
			  }
		    }
		  
		  
		 // System.out.print("\n"+NumExpandedStatesIter+" expanded states along most probable path to goal");

		  for (Iteration = 1; ;Iteration++) {

		 //   System.out.print("\n" +Iteration+"( "+(float)(System.currentTimeMillis()-gStartTime)/1000+" seconds)");
		 //   System.out.print("  f: "+Start.f);
			     

		    /* Expand best partial solution */
		    NumExpandedStatesIter = 0;
		    ExpandedStateList = new Vector<StateNode>();/*ExpandedStateList is a global variable, and it means all the expanded state list*/
		    ExpandSolution(Start);

		    /* Call convergence test if solution graph has no unexpanded tip states */
		    if ((NumExpandedStatesIter==0)&& ConvergenceTest()) {
		      System.out.println("  Converged!");
		      break;
		    }

		    /* Dynamic programming step */
		    /* (Skip if backups were performed in expansion step) */
		    if( gBackupMethod == "separate")
		      {
			/* Update state costs and best partial solution */
			NumAncestorStatesIter = 0;
			/* the following condition is necessary in case no nodes are
			   expanded but the convergence test fails */
			if (NumExpandedStatesIter > 0) { 
			  AncestorList = FindAncestors(ExpandedStateList);/*only update those node */
			  /* note that expanded nodes are included as ancestors */
			  System.out.print("  ancestors: "+NumAncestorStatesIter);
			  System.out.print("  explicit graph"+ NumExpandedStates);
			  ValueIterationForLAO(AncestorList, 3);
			  AncestorList.clear();
			  }
		     }

		    ExpandedStateList.clear();
		    /* NumSolutionStates includes GOAL and WALL */
		   // System.out.print("  ExpandedStates: "+NumExpandedStatesIter);
		  }
//		  DisplaySolution();
//		  System.out.print("\nf: "+Start.f);
		  System.out.print("  NumSolutionStates: "+NumSolutionStates);
	  	  System.out.println(" NumExpandedStates : "+NumExpandedStates);
                  System.out.println("( "+(float)(System.currentTimeMillis()-gStartTime)/1000+" seconds), stateValue:"+Start.stateValue);

	}
        
                
       	private void ValueIterationForLAO(Vector<StateNode> list,int MaxIter) {
		  //gStartTime=System.currentTimeMillis();
		  int                       Iter;
		  double                    diff, maxdiff, /* Bellman residual */
		                            error;		  
		  for (Iter=0; Iter < MaxIter; Iter++) {
			//System.out.println("Iteration"+Iter);
		    maxdiff = 0.0;
		    Iterator< StateNode> iter = list.iterator(); 
		    while (iter.hasNext()) {
		    StateNode stateNode =iter.next() ;		    	
		      if (stateNode.isTerminal==false) { 
			diff = BackupForLAO(stateNode);
			if (diff > maxdiff)
			  maxdiff = diff;
		      }
		    }
		    if ( gMethod == "vi" ) {
			error = (maxdiff * gDiscount)/(1.0 - gDiscount);
//			    System.out.print("\n" +Iteration+"( "+(float)(System.currentTimeMillis()-gStartTime)/1000+" seconds), stateValue:"+Start.stateValue);
//		      	System.out.println("Error bound: "+ error);		     
		      if ( error < gEpsilon )
			return;
		    }
		
		  }
		         
        }     
                
       public boolean ConvergenceTest()
	/* From start state, perform depth-first search of all states 
	   visited by best solution. Mark each state when it is visited
	   so that it is visited only once each pass. For each state:
	   -- perform backup
	   -- update Bellman residual
	   If visit unexpanded state, exit with FALSE. 
	   If error bound <= epsilon, exit with TRUE.
	   Otherwise repeat.
	*/
	{
	  double error;

	  ExitFlag = 0;
	  do {
	    Iteration++;
	    NumSolutionStates = 0;
	    Residual = 0.0;
	    ConvergenceTestRecursive( Start );
	    if ( gDiscount < 1.0 )
	      error = (gDiscount * Residual)/(1.0 - gDiscount);
	    else
	      error = Residual;
	    System.out.print("  Error bound:"+ error);
	    System.out.print("\n"+ Iteration+"(" +(float)(System.currentTimeMillis()-gStartTime)/1000+" secs.)");
	    System.out.print("  f: "+ Start.f);
	  } while (ExitFlag==0 && (error > gEpsilon));
	  if (ExitFlag!=0)
	    return( false);
	  else
	    return(true);
	}

	public void ConvergenceTestRecursive(StateNode node )
	{
	 // System.out.println("ConvergeTest for node :"+node.stateNo);
	  StateDistribution successor;
	  double Diff;

	  /* Count this node as part of best solution */
	  NumSolutionStates++; 

	  /* Terminate recursion at goal node */
	  if (node.isTerminal == true)
	    return;

	  /* Exit convergence test if best solution has unexpanded node */
	  if (node.Expanded==0) 
	    {
		  ExitFlag = 1;
		  Diff = BackupForLAO(node);
		  if ( Diff > Residual ) Residual = Diff;
		  //return;//edit by Yi Lu
	    }

	  /* Recursively consider unvisited successor nodes */
	  
	  
	  successor=node.bestAction.nextState;
	  
	  for (int i=0;i<successor.count;i++)
	    if (successor.state[i].Update < Iteration) {
	      successor.state[i].Update = Iteration;
	      ConvergenceTestRecursive(successor.state[i]);
	    }

	  /* Do backup to improve value of node */
	  Diff = BackupForLAO(node);
	  if ( Diff > Residual ) Residual = Diff;
	}
        
        
        void ExpandSolution( StateNode node)
	/* Returns list of expanded states */
	/* if num expanded nodes exceeds limit, break */

	{
	   StateDistribution successor;

	  /* If not yet expanded, expand here.
	     Then consider successor nodes for expansion. */
	  if (node.Expanded == 0) {
	    ExpandNode(node);
	    if(gBackupMethod == "combine") 
	      BackupForLAO(node);
	  }

	  /* Assume successor states are sorted in order of probability */
          //System.out.print("node.bestAction : ");
          //System.out.println(node.bestAction==null);
          //System.out.println("best action:"+node.bestAction.toString());
          //System.out.println("best action.nextState.count: "+node.bestAction.nextState.count);
	  successor=node.bestAction.nextState;
          //System.out.println(successor.toString());
          
	  for (int i=0;i<successor.count;i++) {
	    //System.out.println(successor.state[i].toString());  
	    /* Stop if already visited this iteration or goal state */
	    if ((successor.state[i].Expanded < Iteration) && 
		(successor.state[i].isTerminal==false)) {
	      
	      /* If already expanded, just mark as visited this iteration */
	      if (successor.state[i].Expanded > 0)
		successor.state[i].Expanded = Iteration;
	      ExpandSolution(successor.state[i]);
	    }
	  }
	  /* Possibly perform backup when backtrack to this node */
	  if( gBackupMethod == "combine" )
	    BackupForLAO(node);
	}
	         
        public double BackupForLAO(StateNode state)
	/* Returns change of state value */
	{
		//Please note that the final fCost is 0 in this case, and 
		//System.out.println("backup state: "+state.stateNo);

	  double                    fCost, gCost, hCost, 
	                            bestfCost, bestgCost=99999999999.9, besthCost=99999999999.9, oldfCost,
	                            fWeightCost, bestfWeightCost, oldfWeightCost;
	  ActionNode        actionNode, bestAction=null;

	  /* used for pathmax */
	  oldfWeightCost = state.fWeight;
	  oldfCost = state.f;

	  /* Initialize to worst possible cost to guarantee improvement */
	  bestfWeightCost = 999999999999999.9;
	  bestfCost = 9999999999.9;
           if(state.action[0].nextState==null)
           {
               for(int i=0;i<state.action.length;i++)
             {
                getAllNextStateDistributuion(state,state.action[i]);
             }
           }
          
	  /* Find action that minimizes expected cost */
	  for (int i=0;i<state.action.length;i++) {
	    actionNode = state.action[i];
	    gCost = actionNode.cost;
	    hCost = 0.0;
	   // System.out.print("For actionNode: "+actionNode.action);
	    for (int j=0;j<actionNode.nextState.count;j++) {
	    
	      gCost += gDiscount *  actionNode.nextState.prob[j] * actionNode.nextState.state[j].g;
	      hCost += gDiscount *  actionNode.nextState.prob[j] * actionNode.nextState.state[j].h;
	    }
	    fCost = gCost + hCost;
	   // System.out.println(" fCost= "+fCost);
	    fWeightCost = gCost + (gWeight * hCost);
	    if (fWeightCost < bestfWeightCost) { /* or (fCost < bestfCost) */
	      bestfWeightCost = fWeightCost;
	      bestfCost = fCost;
	      bestgCost = gCost;
	      besthCost = hCost;      
	      bestAction = actionNode;/*update the bestAction at last, while in the policy iteration, it is crucial*/
	    }
	  }/*The value of a state is the Value of the best action which equals to sum of the probability multiply the value of next state*/
	  
	    state.fWeight = bestfWeightCost;
	    state.f = bestfCost;
	    state.g = bestgCost;
	    state.h = besthCost;
	    state.bestAction = bestAction;
            if(bestAction==null)
            System.out.println("Alert, best action is null");
	    /*
	  }
	    */

	  //System.out.println("back up state node : "+state.stateNo+" bestfWeightCost :"+bestfWeightCost+" oldWeightCost :"+oldfWeightCost);
	  return(bestfWeightCost - oldfWeightCost); /* or (bestfCost - oldfCost) */
	}

	void ExpandNode( StateNode node)
	{
	  node.Expanded = Iteration;
	  /* set heuristic value */
	  if (gBackupMethod == "separate")
	    AppendStateList(ExpandedStateList, node);
	  NumExpandedStatesIter++;
	  NumExpandedStates++;
	}
	
	void AppendStateList(Vector<StateNode> list, StateNode node)
	/* Append node to front of state list */
	{
		if (list==null) { /* consider possibility of empty list */
			list=new Vector<StateNode>();
		}
		else
			list.add(node);
	}
	
	Vector<StateNode> FindAncestors( Vector<StateNode> ExpandedStateList)
	{
	  Vector<StateNode> AddList, NewAddList=null, AncestorList;

	  AddList = new Vector<StateNode>();
	  AncestorList = new Vector<StateNode>();

	  /* Initial AddList consists of all expanded states */
	  for (StateNode node:ExpandedStateList) {
	    node.Update = Iteration;/*All the node in the expanded list are marked, and their Update value is Iteration*/
	    AddList.add(node);/*AddList include all the node in ExpandedStateList at first*/
	  }

	  /* Find ancestor states that need to be updated */
	  while (AddList!=null) { /* check for empty list */
	    NewAddList = new Vector<StateNode>();
	    /* For each state added to Z ... */
	    for (StateNode node:AddList) {
	      /* ... and for each parent of that state ... */
	      for (ActionNode prev:node.PrevAction) {
		/* only add a parent along a marked action arc */
		/* also, parent must be expanded */
		if ((prev== prev.prevState.bestAction) &&
		    (prev.prevState.Expanded > 0))
		  /* don't add parent that is already in ancestor list */
		  if (prev.prevState.Update < Iteration) {/*those Update value==Iteration already included in the ancestor list*/
		    NewAddList.add(prev.prevState);
		    prev.prevState.Update = Iteration;
		  }
	      }
	      AncestorList.add(node);
	      NumAncestorStatesIter++;
	    }
	    AddList.clear();
	    System.out.println("AddList is null?"+AddList.isEmpty());
	    System.out.println("NewAddList is null?"+NewAddList.isEmpty());
	    AddList = NewAddList;
	    System.out.println("AddList is null?"+AddList.isEmpty());
	    NewAddList = null;
	  }
	  return(AncestorList);
	}        
        
        

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
                    ex.printStackTrace();
                }
            }
        });
    }
        
                public static void callSimulator(String configureFileName,final String title) throws Exception {
       // LogManager.getLogManager().readConfiguration(Demo.class.getResourceAsStream("./logging.properties"));
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        final Prototype p=new Prototype();
        p.gInputConfigureFileName=configureFileName;
        java.awt.EventQueue.invokeLater(new Runnable() {
     
            @Override
            public void run() {
                try {
                    
                    Demo d = new Demo(p);
                    d.setTitle(title);
                    d.addAlgorithm(OSMDijkstra.class);
                    d.addAlgorithm(OSMSkyline.class);
                    d.addAlgorithm(DemoAlgorithm1D.class);
                    d.addAlgorithm(DemoAlgorithm2D.class);
                    d.addAlgorithm(DemoAlgorithm3D.class);
                    d.setVisible(true);
                    
                } catch (IOException ex) {
                    Logger.getLogger(Demo.class.getName()).log(Level.SEVERE, null, ex);
                    ex.printStackTrace();
                }
            }
        });
    }
        
}
