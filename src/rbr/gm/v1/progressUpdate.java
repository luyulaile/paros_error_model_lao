/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package rbr.gm.v1;

import experimentalcode.franz.Demo;
import java.awt.Rectangle;

/**
 *
 * @author yilu
 */
public class progressUpdate  implements Runnable{
    
       private Demo demo;
       public progressUpdate(Demo demo)
       {
           this.demo=demo;
       }

        public void run(){

            for (int i=0; i<=100; i++){ //Progressively increment variable i

                int temp=this.demo.jProgressBar1.getValue();
                if(temp>i-1)//
                {
                    this.demo.jProgressBar1.setValue(temp); //Set value
                    i=temp;
                }
                else if(temp==i-1)
                {
                    this.demo.jProgressBar1.setValue(i); //Set value
                }
                else if(temp>=90)//temp<i-1,run too fast,we should slow down
                {
                    this.demo.jProgressBar1.setValue(i-1);
                    i--;
                }
 
                
                Rectangle progressRect = this.demo.jProgressBar1.getBounds();  
                progressRect.x = 0;  
                progressRect.y = 0;  
                this.demo.jProgressBar1.paintImmediately(progressRect); //Refresh graphics
                
                try{Thread.sleep(1200);} //Sleep 50 milliseconds

                catch (InterruptedException err){}
                // System.out.println("why no change"+i);
            }

    }

}
