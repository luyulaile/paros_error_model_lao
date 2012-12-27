/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package rbr.gm.v1;

import experimentalcode.franz.osm.OSMGraph;
import experimentalcode.franz.osm.OSMLink;
import experimentalcode.franz.osm.OSMNode;
import java.io.Serializable;
import java.util.*;
import rbr.gm.v1.Prototype.Action;
import rbr.gm.v1.Prototype.Direction;

    
/**
 *
 * @author yilu
 */
public class GenerateHeuristic implements Serializable{
    
        transient public List<Edge> opened = new ArrayList<Edge>();
	 transient public List<Edge> closed = new ArrayList<Edge>();
	transient public HashMap<String,Edge> bestList = new HashMap<String,Edge>();
	 transient public List<Edge> neighbour= new ArrayList<Edge>();    
    
   	transient public static Queue<OSMNode> qe = new LinkedList<OSMNode>();

	 transient public static int nodeCount;
	 transient public static final double INFINITY=50000000000.0;
//We use to generate the heuristic using Breadth First Search
   public HashMap<String,Edge> generateHeuristic(OSMNode startPos, OSMNode endPos, OSMGraph<OSMNode<OSMLink>, OSMLink<OSMNode>> graph) {
         for(OSMNode node:graph.getNodes())
        {
            node.hValue=-1;
        }
        endPos.hValue=0;
        int i=0;
        qe.offer(endPos);
        while(qe.isEmpty()==false)
        {
            OSMNode temp=qe.poll();
            OSMNode other;
            for (Iterator it = temp.getInLinks().iterator(); it.hasNext();) {
                OSMLink link = (OSMLink) it.next();
                other=(OSMNode)(temp==link.getTarget()?link.getSource():link.getTarget());
                if(other.hValue<0)
                {
                    other.hValue=temp.hValue+Prototype.NOTENGAGED_COST_SAU_MSG_NOP_CONTINUED+Prototype.NOTENGAGED_COST_SAU_MSG_NOP_NON_CONTINUED;
                    qe.offer(other);
                }
            }
        }
        
        //already generate heuristic value for node.
        
        //initialize state heuristic with Direction
       for(OSMNode node:graph.getNodes())
        {
          for(int direction=0;direction<node.getOutLinks().size();direction++)//*5
          {
            Edge temp=new Edge(node,Direction.values()[direction],0,node.hValue,node.hValue);       
             bestList.put(temp.toString(),temp);
          }
          
           for(int direction=0;direction<node.getInLinks().size();direction++)//*4
           {
               Edge temp=new Edge(node,Direction.values()[direction+Prototype.Direction.values().length/2],0,node.hValue,node.hValue);
               bestList.put(temp.toString(),temp);
           }
        }
       //begin to run A* and update fValue in bestList;
       
       
       Edge endEdge=new Edge(endPos,Direction.first,0,endPos.hValue,endPos.hValue);
        for (Iterator<Edge> it = bestList.values().iterator(); it.hasNext();) {
            Edge edge = it.next();
            edge.fValue=this.AStar(edge, endEdge);
            
        }
       System.out.println("Generated Heuristic!");
        return bestList;
    }
   
   
   public double AStar(Edge start, Edge goal)
	{
		start.gValue=0;
		start.hValue=bestList.get(start.toString()).fValue;
		start.fValue=start.gValue+start.hValue;
		opened.clear();
		closed.clear();
	    opened.add(start);
	    nodeCount++;
	    while(opened.size()>0)
	    {
	    	Edge current=FindbestAmong(opened);
//	    	System.out.print("best node in open list:("+best.x+",");
//	    	System.out.print(best.y+") and the fValue");
//	    	System.out.println(best.fValue);
	        if(current.node==goal.node)
	        {
//		    	System.out.print("("+best.x+",");
//		    	System.out.print(best.y+")");
//		    	System.out.println(best.fValue);
                    double temp=current.fValue;
                    ConstructPath(start,current,current.fValue);
	        	return temp;
	        }
	        
			opened.remove(current);
			closed.add(current);
			
			neighbour=GetNeihgbours(current);
			
			for(Edge edge : neighbour)
			{
				if (closed.contains(edge)==true)                                        
                                        continue;
				double tenative_g=current.gValue+Prototype.COST_SAU_MSG_NOP_NON_CONTINUED+Prototype.COST_SAU_MSG_NOP_CONTINUED;
				if(opened.contains(edge)==false||tenative_g<edge.gValue)// not in open list
                                {
                                    edge.hValue=bestList.get(edge.toString()).fValue;
    		                    edge.gValue=tenative_g;
                                    edge.fValue=edge.hValue+edge.gValue;
                                    opened.add(edge);
                                    edge.parent=current;
                                }
				
			}
	    }
            
            
	    return INFINITY;
	     
	}
   
   	private Edge FindbestAmong(List<Edge> opened) {
		// TODO Auto-generated method stub
		Edge best = null;
		for (Edge edge : opened) {
			if (best == null
					|| edge.fValue<best.fValue) {
				best = edge;
			}
		}

		return best;
	}
    
        private List<Edge> GetNeihgbours(Edge edge) {
		// gurantee that x and y can increase and decrease, decrease by 1
		// if y<-2, purge it
    	  List<Edge> neighbours = new ArrayList<Edge>();
    	  double h,g;
          Edge temp;
          for(Action action:Action.values())
          {
              if(action==Action.continued)
                continue;
              
              temp=calculateNewControlledState(edge,action);
              
              if(temp!=edge)
              {
    		      neighbours.add(temp);
              }
              
          }
    			 
	      return neighbours;
	}

    private void ConstructPath(Edge start,Edge current,double totalCost) {
        bestList.get(current.toString()).fValue=(totalCost-current.gValue);
        if(current.node!=start.node)                
            ConstructPath(start,current.parent,totalCost); 
    }
        
  class Edge implements Serializable{
	public Edge(OSMNode node, Direction direction,double gValue,double hValue,double fValue)
	{
	this.node=node;
	this.direction=direction;
	this.gValue=gValue;
	this.hValue=hValue;
	this.fValue=fValue;

	}

        private Edge() {
           
        }
        
        @Override
        public String toString()
        {
            StringBuilder result = new StringBuilder();
      	    result.append(this.node+","+this.direction);
    	    return result.toString();
        }

	public boolean equals(Object o ) { 
	    return ((Edge)o).node==this.node&&((Edge)o).direction==this.direction; 
	}
transient OSMNode node;
double gValue;
double hValue;
double fValue;//f(n)=g(n)+h(n)
Direction direction;
transient Edge parent;
    }


      private Edge calculateNewControlledState(
			Edge controlledState, Action action) {
		Edge newControlledState=new Edge();
		newControlledState.direction=controlledState.direction;
		newControlledState.node=controlledState.node;
		int debugflag=0;

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

                                      if( Prototype.getDefaultForwardLink(newControlledState.node,temp)!=null)
                                         newControlledState.direction= Prototype.getDefaultForwardLink(newControlledState.node,temp);
                                     else
                                     {                                      
                                       System.out.println("It is wired");
                                     }
                                      
                                     break;
                            case second:
                                     temp=(OSMLink)(controlledState.node.getOutLinks().get(1));
                                     if(temp.isOneway()==true)
                                     newControlledState.node=(OSMNode) temp.getTarget();
                                     else
                                         newControlledState.node=(OSMNode)(temp.getTarget()==controlledState.node?temp.getSource():temp.getTarget());
                                     //System.out.println("New State:"+newControlledState.node+", old State"+controlledState.node);
                                     if( Prototype.getDefaultForwardLink(newControlledState.node,temp)!=null)
                                         newControlledState.direction= Prototype.getDefaultForwardLink(newControlledState.node,temp);
                                     else
                                     {
                                         System.out.println("it is wired");
                                     }
                                      
                                break;
                            case third:
                                     temp=(OSMLink)(controlledState.node.getOutLinks().get(2));
                                      if(temp.isOneway()==true)
                                     newControlledState.node=(OSMNode) temp.getTarget();
                                     else
                                         newControlledState.node=(OSMNode)(temp.getTarget()==controlledState.node?temp.getSource():temp.getTarget());
                                     //System.out.println("New State:"+newControlledState.node+", old State"+controlledState.node);

                                     if( Prototype.getDefaultForwardLink(newControlledState.node,temp)!=null)
                                         newControlledState.direction= Prototype.getDefaultForwardLink(newControlledState.node,temp);
                                     else
                                     {
                                            System.out.println("it is wired");
                                     }
                                      
                                break;
                            case fourth:
                                     temp=(OSMLink)(controlledState.node.getOutLinks().get(3));
                                      if(temp.isOneway()==true)
                                     newControlledState.node=(OSMNode) temp.getTarget();
                                     else
                                         newControlledState.node=(OSMNode)(temp.getTarget()==controlledState.node?temp.getSource():temp.getTarget());
                                     //System.out.println("New State:"+newControlledState.node+", old State"+controlledState.node);

                                     if( Prototype.getDefaultForwardLink(newControlledState.node,temp)!=null)
                                        newControlledState.direction= Prototype.getDefaultForwardLink(newControlledState.node,temp);
                                     else
                                     {
                                            System.out.println("it is wired");
                                     }
                                    
                                break;
                                
                              case fifth:
                                     temp=(OSMLink)(controlledState.node.getOutLinks().get(4));
                                      if(temp.isOneway()==true)
                                     newControlledState.node=(OSMNode) temp.getTarget();
                                     else
                                         newControlledState.node=(OSMNode)(temp.getTarget()==controlledState.node?temp.getSource():temp.getTarget());
                                    // System.out.println("New State:"+newControlledState.node+", old State"+controlledState.node);

                                     if( Prototype.getDefaultForwardLink(newControlledState.node,temp)!=null)
                                        newControlledState.direction= Prototype.getDefaultForwardLink(newControlledState.node,temp);
                                     else
                                     {
                                        System.out.println("it is wired");
                                     }
                                    
                                break;    
                                  
                                   case sixth:
                                     temp=(OSMLink)(controlledState.node.getOutLinks().get(5));
                                      if(temp.isOneway()==true)
                                     newControlledState.node=(OSMNode) temp.getTarget();
                                     else
                                         newControlledState.node=(OSMNode)(temp.getTarget()==controlledState.node?temp.getSource():temp.getTarget());
                                     //System.out.println("New State:"+newControlledState.node+", old State"+controlledState.node);

                                     if( Prototype.getDefaultForwardLink(newControlledState.node,temp)!=null)
                                        newControlledState.direction= Prototype.getDefaultForwardLink(newControlledState.node,temp);
                                     else
                                     {
                                        System.out.println("it is wired");
                                     }
                                      
                                break;
                            
                           // or else, nothing will change. 
                                  
  		        }
                           if(debugflag==1)          
                                       System.out.println("does it changed after forward?: "+newControlledState.toString());
                   //     System.out.println("New node id:"+newControlledState.node);
		return newControlledState;		
		}
		
		if(action==Action.left)
		{
			switch (controlledState.direction)
			{
                            case first:
	                             OSMLink temp=(OSMLink)(controlledState.node.getOutLinks().get(0));
                                     if(Prototype.getNextLeftLink(newControlledState.node,temp)!=null)
                                             newControlledState.direction= Prototype.getNextLeftLink(newControlledState.node,temp);
                                  
                                     break;
                            case second:
                                     temp=(OSMLink)(controlledState.node.getOutLinks().get(1));
                                     if( Prototype.getNextLeftLink(newControlledState.node,temp)!=null)                                     
                                         newControlledState.direction= Prototype.getNextLeftLink(newControlledState.node,temp);
                                      
                                break;
                            case third:
                                     temp=(OSMLink)(controlledState.node.getOutLinks().get(2));
                                     if( Prototype.getNextLeftLink(newControlledState.node,temp)!=null)                              
                                           newControlledState.direction= Prototype.getNextLeftLink(newControlledState.node,temp);
                                      
                                break;
                            case fourth:
                                     temp=(OSMLink)(controlledState.node.getOutLinks().get(3));
                                     if( Prototype.getNextLeftLink(newControlledState.node,temp)!=null)                                     
                                           newControlledState.direction= Prototype.getNextLeftLink(newControlledState.node,temp);
                                      
                                break;
                                
                             case fifth:
                                     temp=(OSMLink)(controlledState.node.getOutLinks().get(4));
                                     if( Prototype.getNextLeftLink(newControlledState.node,temp)!=null)                                     
                                           newControlledState.direction= Prototype.getNextLeftLink(newControlledState.node,temp);
                                      
                                break;
                             case sixth:
                                     temp=(OSMLink)(controlledState.node.getOutLinks().get(5));
                                     if( Prototype.getNextLeftLink(newControlledState.node,temp)!=null)                                     
                                           newControlledState.direction= Prototype.getNextLeftLink(newControlledState.node,temp);
                                      
                                break;
                             case negativefirst:
                                  temp=(OSMLink)(controlledState.node.getInLinks().get(0));
                                     if( Prototype.getNextLeftLink(newControlledState.node,temp)!=null)                                     
                                           newControlledState.direction= Prototype.getNextLeftLink(newControlledState.node,temp);
                                      
                                 break;
                             case negativesecond:
                                 temp=(OSMLink)(controlledState.node.getInLinks().get(1));
                                     if( Prototype.getNextLeftLink(newControlledState.node,temp)!=null)                                     
                                           newControlledState.direction= Prototype.getNextLeftLink(newControlledState.node,temp);
                                      
                                 break;
                             case negativethird:
                                 temp=(OSMLink)(controlledState.node.getInLinks().get(2));
                                     if( Prototype.getNextLeftLink(newControlledState.node,temp)!=null)                                     
                                           newControlledState.direction= Prototype.getNextLeftLink(newControlledState.node,temp);
                                      
                                 break;
                             case negativefourth:
                                 temp=(OSMLink)(controlledState.node.getInLinks().get(3));
                                     if( Prototype.getNextLeftLink(newControlledState.node,temp)!=null)                                     
                                           newControlledState.direction= Prototype.getNextLeftLink(newControlledState.node,temp);
                                      
                                 break;
                             case negativefifth:
                                 temp=(OSMLink)(controlledState.node.getInLinks().get(4));
                                     if( Prototype.getNextLeftLink(newControlledState.node,temp)!=null)                                     
                                           newControlledState.direction= Prototype.getNextLeftLink(newControlledState.node,temp);
                                      
                                 break;
                              case negativesixth:
                                 temp=(OSMLink)(controlledState.node.getInLinks().get(5));
                                     if( Prototype.getNextLeftLink(newControlledState.node,temp)!=null)                                     
                                           newControlledState.direction= Prototype.getNextLeftLink(newControlledState.node,temp);
                                      
                                 break;    
	
			}
                           if(debugflag==1)          
                                       System.out.println("does it changed after turn left?: "+newControlledState.toString());
			return newControlledState;
		}
		
		if(action==Action.right)
		{
			 
			switch (controlledState.direction)
			{
                            case first:
	                             OSMLink temp=(OSMLink)(controlledState.node.getOutLinks().get(0));
                                     if( Prototype.getNextRightLink(newControlledState.node,temp)!=null)
                                           newControlledState.direction= Prototype.getNextRightLink(newControlledState.node,temp);
                                      
                                     break;
                            case second:
                                     temp=(OSMLink)(controlledState.node.getOutLinks().get(1));
                                     if( Prototype.getNextRightLink(newControlledState.node,temp)!=null)
                                        newControlledState.direction= Prototype.getNextRightLink(newControlledState.node,temp);
                                      
                                break;
                            case third:
                                     temp=(OSMLink)(controlledState.node.getOutLinks().get(2));
                                     if( Prototype.getNextRightLink(newControlledState.node,temp)!=null)
                                           newControlledState.direction= Prototype.getNextRightLink(newControlledState.node,temp);
                                      
                                break;
                            case fourth:
                                     temp=(OSMLink)(controlledState.node.getOutLinks().get(3));
                                     if( Prototype.getNextRightLink(newControlledState.node,temp)!=null)
                                           newControlledState.direction= Prototype.getNextRightLink(newControlledState.node,temp);
                                      
                                break;
                                
                           case fifth:
                                     temp=(OSMLink)(controlledState.node.getOutLinks().get(4));
                                     if( Prototype.getNextRightLink(newControlledState.node,temp)!=null)
                                           newControlledState.direction= Prototype.getNextRightLink(newControlledState.node,temp);
                                      
                                break;     
                            case sixth:
                                     temp=(OSMLink)(controlledState.node.getOutLinks().get(5));
                                     if( Prototype.getNextRightLink(newControlledState.node,temp)!=null)
                                           newControlledState.direction= Prototype.getNextRightLink(newControlledState.node,temp);
                                      
                                break;      
                            case negativefirst:
                                 temp=(OSMLink)(controlledState.node.getInLinks().get(0));
                                     if( Prototype.getNextRightLink(newControlledState.node,temp)!=null)
                                           newControlledState.direction= Prototype.getNextRightLink(newControlledState.node,temp);
                                      
                                break;
                           case negativesecond:
                                 temp=(OSMLink)(controlledState.node.getInLinks().get(1));
                                     if( Prototype.getNextRightLink(newControlledState.node,temp)!=null)
                                           newControlledState.direction= Prototype.getNextRightLink(newControlledState.node,temp);
                                      
                                break;     
                           case negativethird:
                                 temp=(OSMLink)(controlledState.node.getInLinks().get(2));
                                     if( Prototype.getNextRightLink(newControlledState.node,temp)!=null)
                                           newControlledState.direction= Prototype.getNextRightLink(newControlledState.node,temp);
                                      
                                break;                 
                           case negativefourth:
                                 temp=(OSMLink)(controlledState.node.getInLinks().get(3));
                                     if( Prototype.getNextRightLink(newControlledState.node,temp)!=null)
                                           newControlledState.direction= Prototype.getNextRightLink(newControlledState.node,temp);
                                      
                                break;  
                           case negativefifth:
                                 temp=(OSMLink)(controlledState.node.getInLinks().get(4));
                                     if( Prototype.getNextRightLink(newControlledState.node,temp)!=null)
                                           newControlledState.direction= Prototype.getNextRightLink(newControlledState.node,temp);
                                      
                                break;                                    
                           case negativesixth:
                                 temp=(OSMLink)(controlledState.node.getInLinks().get(5));
                                     if( Prototype.getNextRightLink(newControlledState.node,temp)!=null)
                                           newControlledState.direction= Prototype.getNextRightLink(newControlledState.node,temp);
                                      
                                break;     
                        }
                           if(debugflag==1)          
                                       System.out.println("does it changed after turn right?: "+newControlledState.toString());
                        return newControlledState;
		}   
                
                if(action==Action.reverse)
                {
                    switch (controlledState.direction)
                    {
                        case first:
	                             OSMLink temp=(OSMLink)(controlledState.node.getOutLinks().get(0));
                                     if( Prototype.getReversedLink(newControlledState.node,temp)!=null)
                                           newControlledState.direction=(Direction)  Prototype.getReversedLink(newControlledState.node,temp);
                                     
                                     break;
                             case second:
                                     temp=(OSMLink)(controlledState.node.getOutLinks().get(1));
                                     if( Prototype.getReversedLink(newControlledState.node,temp)!=null)
                                           newControlledState.direction=(Direction)  Prototype.getReversedLink(newControlledState.node,temp);
                                break;
                            case third:
                                     temp=(OSMLink)(controlledState.node.getOutLinks().get(2));
                                    if( Prototype.getReversedLink(newControlledState.node,temp)!=null)
                                           newControlledState.direction=(Direction)  Prototype.getReversedLink(newControlledState.node,temp);
                                break;
                            case fourth:
                                     temp=(OSMLink)(controlledState.node.getOutLinks().get(3));
                                    if( Prototype.getReversedLink(newControlledState.node,temp)!=null)
                                           newControlledState.direction=(Direction)  Prototype.getReversedLink(newControlledState.node,temp);
                                break;
                                
                           case fifth:
                                     temp=(OSMLink)(controlledState.node.getOutLinks().get(4));
                                     if( Prototype.getReversedLink(newControlledState.node,temp)!=null)
                                           newControlledState.direction=(Direction)  Prototype.getReversedLink(newControlledState.node,temp);
                                break;     
                            case sixth:
                                     temp=(OSMLink)(controlledState.node.getOutLinks().get(5));
                                     if( Prototype.getReversedLink(newControlledState.node,temp)!=null)
                                           newControlledState.direction=(Direction)  Prototype.getReversedLink(newControlledState.node,temp);
                                break;
                                
                            case negativefirst:
                                 temp=(OSMLink)(controlledState.node.getInLinks().get(0));
                                     if( Prototype.getReversedLink(newControlledState.node,temp)!=null)
                                           newControlledState.direction=(Direction)  Prototype.getReversedLink(newControlledState.node,temp);
                                     
                                break;
                           case negativesecond:
                                 temp=(OSMLink)(controlledState.node.getInLinks().get(1));
                                     if( Prototype.getReversedLink(newControlledState.node,temp)!=null)
                                           newControlledState.direction=(Direction)  Prototype.getReversedLink(newControlledState.node,temp);
                                break;     
                           case negativethird:
                                 temp=(OSMLink)(controlledState.node.getInLinks().get(2));
                                     if( Prototype.getReversedLink(newControlledState.node,temp)!=null)
                                           newControlledState.direction=(Direction)  Prototype.getReversedLink(newControlledState.node,temp);
                                break;                 
                           case negativefourth:
                                 temp=(OSMLink)(controlledState.node.getInLinks().get(3));
                                     if( Prototype.getReversedLink(newControlledState.node,temp)!=null)
                                           newControlledState.direction=(Direction)  Prototype.getReversedLink(newControlledState.node,temp);
                                break;  
                           case negativefifth:
                                 temp=(OSMLink)(controlledState.node.getInLinks().get(4));
                                     if( Prototype.getReversedLink(newControlledState.node,temp)!=null)
                                           newControlledState.direction=(Direction)  Prototype.getReversedLink(newControlledState.node,temp);
                                break;                                    
                           case negativesixth:
                                 temp=(OSMLink)(controlledState.node.getInLinks().get(5));
                                     if( Prototype.getReversedLink(newControlledState.node,temp)!=null)
                                           newControlledState.direction=(Direction)  Prototype.getReversedLink(newControlledState.node,temp);
                                break;     
                    }
                }
		return newControlledState;
	}
	
}
