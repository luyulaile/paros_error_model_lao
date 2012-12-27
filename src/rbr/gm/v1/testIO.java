/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package rbr.gm.v1;

/**
 *
 * @author yilu
 */
import java.io.DataInputStream;  
import java.io.DataOutputStream;  
import java.io.File;  
import java.io.FileInputStream;  
import java.io.FileOutputStream;  
import java.io.IOException;  
  
/* @comany ????(??)???????? 
* @author steven.wu 
* @since 2009.8.11 
* @description read and write stream for binary file? 
*/  
public class testIO {  
    private DataInputStream dis=null;  
    private DataOutputStream dos=null;  
    private String s_FilePath="e:/bin.dat";  
    private byte[] m_datapadding = { 0x00 }; //????????????.  
      
    public testIO() {  
        // TODO Auto-generated constructor stub  
        init();  
    }  
    private void init(){  
        try{  
            if(!new File(s_FilePath).exists()){  
                new File(s_FilePath).createNewFile();  
            }  
            dis=new DataInputStream(new FileInputStream(new File(s_FilePath)));  
            dos=new DataOutputStream(new FileOutputStream(new File(s_FilePath)));  
        }catch(Exception e){  
            e.printStackTrace();  
        }  
    }  
    public void writeBinaryStream(){  
        try{  
            if(dos!=null){  
                for(int i=0;i<2;i++){  
                    //write boolean value.  
                    dos.writeBoolean(true);  
                    //write a char value.  
                    char c='a';  
                    dos.writeChar((int)c);  
                    Double d=12.567d;  
                    dos.writeDouble(d);  
                    Float f=56.782f;  
                    dos.writeFloat(f);  
                    int k=105;  
                    dos.writeInt(k);  
                    long l=98765l;  
                    dos.writeLong(l);  
                    short st=12;  
                    dos.writeShort(st);  
                    String cs="Java write";  
                    String cs1="binary file";  
                    if(i==0){  
                        dos.writeUTF(cs);  
                    }else{  
                        dos.writeUTF(cs1);  
                    }
                    
                    String cs2="h";
                    dos.writeUTF(cs2);
                    
                    String cs3="how about now";
                    dos.writeUTF(cs3);
                    
                    dos.write(m_datapadding);  
                    
                }  
                dos.flush();  
                dos.close();  
            }  
        }catch(Exception e){  
            e.printStackTrace();  
        }  
    }  
    public void readBinaryStream(){  
        try{  
            if(dis!=null){  
                while(dis.available()>0){  
                    System.out.println(dis.available());  
                    System.out.println(dis.readBoolean());  
                    char c=(char)dis.readChar();  
                    System.out.println(c);  
                    System.out.println(dis.readDouble());  
                    System.out.println(dis.readFloat());  
                    System.out.println(dis.readInt());  
                    System.out.println(dis.readLong());  
                    System.out.println(dis.readShort());  
                    System.out.println(dis.readUTF());
                    System.out.println(dis.readUTF());
                    System.out.println(dis.readUTF());
                    System.out.println(dis.read(m_datapadding));  
                }  
            }  
        }catch(Exception e){  
            e.printStackTrace();  
        }  
    }  
    public static void main(String[] args) throws IOException {  
        testIO bin=new testIO();  
        bin.writeBinaryStream();  
        bin.readBinaryStream();  
    }  
}  
