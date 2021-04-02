/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hackassembler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;


public class HackAssembler {
    static File fileIn = new File("C:/Users/Me/Desktop/nand2tetris/nand2tetris/nand2tetris/projects/06/max/Max.asm");
    static ArrayList<String> read = new ArrayList();
    static ArrayList<String> instList = new ArrayList();
    static File output = new File("C:/Users/Me/Desktop/nand2tetris/nand2tetris/nand2tetris/projects/06/max/Max.hack");
    static Scanner input;
    static FileWriter writer;
    static ArrayList<Label> labelList = new ArrayList();
    static int variableMem = 16;
    
    public static void main(String[] args) throws FileNotFoundException, IOException{
        //predefined symbols
        labelList.add(new Label("SP",0));
        labelList.add(new Label("LCL",1));
        labelList.add(new Label("ARG",2));
        labelList.add(new Label("THIS",3));
        labelList.add(new Label("THAT",4));
        labelList.add(new Label("SCREEN",16384));
        labelList.add(new Label("KBD",24576));
        for(int i = 0; i < 16; i++){
            labelList.add(new Label("R"+i,i));
        }
        
        //Read input
        input = new Scanner(fileIn);
        while(input.hasNext()){
            read.add(input.nextLine());
        }

        //Remove whitespace and comments
        for(int index = 0; index < read.size(); index++){
            String readLine = read.get(index);
            //Remove whitespace
            readLine = readLine.replaceAll("\\s", "");
            if(readLine.contains("//")){
                readLine = readLine.substring(0, readLine.indexOf("//"));
            }
            if(!readLine.equals(""))
                instList.add(readLine);
        }
        
        //create output file
        output.createNewFile();
        writer = new FileWriter(output);

        //assign labels to their memory locations
        int lineCount = 0;
        for(String str: instList){
            //check for (xxx) labels
            if(str.contains("(")){
                //remove parentheses
                str = str.replaceAll("[()]", "");
                //create new label
                labelList.add(new Label(str, lineCount));
            }else{
                //count how many non-label lines have passed
                lineCount++;
            }
        }
        
        //Decide what type of command each instruction is
        //Send to appropriate method to convert to binary and write to output
        for(String str : instList){
            if(str.contains("@")){
                writer.write(aCommand(str) + "\n");
            }
            if(str.contains("=") || str.contains(";")){
                writer.write(cCommand(str) + "\n");
            }
        }

        writer.flush();
        writer.close();
    }
    
    //for A-register commands: @xxx
    public static String aCommand(String str){
        //remove @
        str = str.replace("@", "");
        //check if symbol or constant
        try{
            //if successful str is a number, exception means str is a variable
            int intStr = Integer.parseInt(str);
            //convert number to binary
            str = Integer.toBinaryString(intStr);
        }catch(NumberFormatException e){
            //str is a variable symbol
            //if variable already exists return its mem location
            //if not create a new variable
            boolean exists = false;
            for(Label l : labelList){
                if(l.name.equals(str)){
                    exists = true;
                    str = Integer.toBinaryString(l.memLocation);
                }
            }
            if(!exists){
                labelList.add(new Label(str,variableMem));
                str = Integer.toBinaryString(variableMem);
                variableMem++;
            }
        }
        
        //add leading 0's so number is 15 bits long plus a leading 0
        String zeroInsert = "0";
        for(int i = str.length(); i < 15; i++){
            zeroInsert = "0" + zeroInsert;
        }
        str = zeroInsert + str;
        System.out.println("a: "+str);
        return str;  
        
         
    }
    
    //for C-register commands: dest=comp;jump
    public static String cCommand(String str){
        //start of every C-Instruction
        String start = "111";
        String comp = "0000000";
        String dest = "000";
        String jump = "000";
        //decide dest=comp or comp;jump
        if(str.contains("=")){
            //seperate dest and comp
            String[] iArray = str.split("=", 2);
            dest = destSwitch(iArray[0]);
            comp = compSwitch(iArray[1]);
        }else if(str.contains(";")){
            //seperate comp and jump
            String[] iArray = str.split(";", 2);
            comp = compSwitch(iArray[0]);
            jump = jumpSwitch(iArray[1]);
        }else{
            System.out.println("something went wrong with c command type");
        }
        
        str = start + comp + dest + jump;
        System.out.println("c: " + str);
        return str;
    }
    
    //convert dest component to binary
    public static String destSwitch(String str){
        switch(str){
            case "M":
                return "001";
            case "D":
                return "010";
            case "MD":
                return "011";
            case "A":
                return "100";
            case "AM":
                return "101";
            case "AD":
                return "110";
            case "AMD":
                return "111";
            default:
                return "dest match not found";
        }
    }
    
    //convert comp segment to binary
    public static String compSwitch(String str){
        switch(str){
            case "0":
                return "0101010";
            case "1":
                return "0111111";
            case "-1":
                return "0111010";
            case "D":
                return "0001100";
            case "A":
                return "0110000";
            case "M":
                return "1110000";
            case "!D":
                return "0001101";
            case "!A":
                return "0110001";
            case "!M":
                return "1110001";
            case "-D":
                return "0001111";
            case "-A":
                return "0110011";
            case "-M":
                return "1110011";
            case "D+1":
                return "0011111";
            case "A+1":
                return "0110111";
            case "M+1":
                return "1110111";
            case "D-1":
                return "0001110";
            case "A-1":
                return "0110010";
            case "M-1":
                return "1110010";
            case "D+A":
                return "0000010";
            case "D+M":
                return "1000010";
            case "D-A":
                return "0010011";
            case "D-M":
                return "1010011";
            case "A-D":
                return "0000111";
            case "M-D":
                return "1000111";
            case "D&A":
                return "0000000";
            case "D&M":
                return "1000000";
            case "D|A":
                return "0010101";
            case "D|M":
                return "1010101";
            default:
                return "comp match not found";
        }
    }
    
    public static String jumpSwitch(String str){
        switch(str){
            case "JGT":
                return "001";
            case "JEQ":
                return "010";
            case "JGE":
                return "011";
            case "JLT":
                return "100";
            case "JNE":
                return "101";
            case "JLE":
                return "110";
            case "JMP":
                return "111";
            default:
                return "jump match not found";        
        }
    }
    
    //Label class to keep track of symbols and their memory locations
    public static class Label{
        public String name;
        public int memLocation;
        
        public Label(String labelName, int labelMem){
            name = labelName;
            memLocation = labelMem;
        }
    }
}