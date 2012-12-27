/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package rbr.gm.v1;

import experimentalcode.franz.Demo;

/**
 *
 * @author yilu
 */
    public class ThreadForUpdate extends Thread{
        Demo p;
	double secondsPerStep;
	
	public ThreadForUpdate(Demo p)
	{
		this.p=p;
		secondsPerStep=1;
	}
	
	public void run()
	{
		while(true)
	 {
		p.DisplaySolutionStepByStep(p.p.allStateList);
		
        try {
			Thread.sleep((long) (secondsPerStep*700));
		} catch (InterruptedException e) {
			e.printStackTrace();
	    }
     
             
	  }
	}

}
